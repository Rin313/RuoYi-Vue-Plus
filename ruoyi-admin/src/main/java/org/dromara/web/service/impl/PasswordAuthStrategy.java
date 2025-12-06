package org.dromara.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.domain.model.PasswordLoginBody;
import org.dromara.common.core.enums.LoginType;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.core.utils.ip.AddressUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.SysConfigService;
import org.dromara.system.service.SysUserService;
import org.dromara.web.domain.vo.LoginVo;
import org.dromara.web.service.IAuthStrategy;
import org.dromara.web.service.SysLoginService;
import org.springframework.stereotype.Service;

/**
 * 密码认证策略
 *
 * @author Michelle.Chung
 */
@Slf4j
@Service("password" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class PasswordAuthStrategy implements IAuthStrategy {

    private final CaptchaProperties captchaProperties;
    private final SysLoginService loginService;
    private final SysUserService userService;
    private final SysUserMapper userMapper;
    private final SysConfigService configService;

    @Override
    public LoginVo login(String body) {
        PasswordLoginBody loginBody = JsonUtils.parseObject(body, PasswordLoginBody.class);
        if (captchaProperties.getEnable()) {
            validateCaptcha(loginBody.getCode(), loginBody.getUuid());
        }
        ValidatorUtils.validate(loginBody);
        SysUserVo userVo;
        SysUserBo sysUser = new SysUserBo();
        if(ObjectUtil.isNotNull(loginBody.getUsername())){
            userVo=userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, loginBody.getUsername()));
            sysUser.setUserName(loginBody.getUsername());
        }
        else if(ObjectUtil.isNotNull(loginBody.getEmail())){
            userVo=userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, loginBody.getEmail()));
            sysUser.setEmail(loginBody.getEmail());
        }
        else if(ObjectUtil.isNotNull(loginBody.getPassword())){
            userVo=userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhonenumber, loginBody.getPhonenumber()));
            sysUser.setPhonenumber(loginBody.getPhonenumber());
        }
        else throw new BizException("无效输入");
        String inputValue=StringUtils.firstNonBlank(sysUser.getUserName(),sysUser.getEmail(),sysUser.getPhonenumber());
        LoginUser loginUser;
        if(loginBody.getRegister()){
            if (!configService.selectRegisterEnabled())
                throw new BizException("当前系统已关闭注册");
            if(ObjectUtil.isNotNull(userVo))
                throw new BizException("用户已存在");
            sysUser.setNickName("用户_"+RandomUtil.randomString(8));
            sysUser.setPassword(BCrypt.hashpw(loginBody.getPassword()));
            sysUser.setInviteCode(userService.getUniqueInviteCode());
            if(StringUtils.isNotEmpty(loginBody.getInviteCode())){
                SysUserVo parent=userService.selectUserByInviteCode(loginBody.getInviteCode());
                if(ObjectUtils.isEmpty(parent))
                    throw new UserException("user.invitecode.unknown");
                sysUser.setParentId(parent.getUserId());
            }
            SysUser t = MapstructUtils.convert(sysUser, SysUser.class);
            userMapper.insert(t);
            loginService.recordLogininfor(inputValue, Constants.REGISTER, MessageUtils.message("user.register.success"));
            loginUser = loginService.buildLoginUser(userMapper.selectVoById(t.getUserId()));
        }
        else{
            if (ObjectUtil.isNull(userVo)) {
                log.info("登录用户：{} 不存在.", inputValue);
                throw new BizException("用户不存在或密码错误");
            } else if (SystemConstants.DISABLE.equals(userVo.getStatus())) {
                log.info("登录用户：{} 已被停用.", inputValue);
                throw new UserException("user.blocked", inputValue);
            }
            loginService.checkLogin(LoginType.PASSWORD, inputValue, () -> !BCrypt.checkpw(loginBody.getPassword(), userVo.getPassword()));
            // 此处可根据登录用户的数据不同 自行创建 loginUser
            loginUser = loginService.buildLoginUser(userVo);
        }
        loginUser.setClientKey(AddressUtils.getClientType());
        SaLoginParameter model = new SaLoginParameter();
        // 自定义分配 不同用户体系 不同 token 授权时间 不设置默认走全局 yml 配置
        // 例如: 后台用户30分钟过期 app用户1天过期
        model.setTimeout(1800);
        model.setActiveTimeout(1800);
        // 生成token
        LoginHelper.login(loginUser, model);

        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        return loginVo;
    }

    /**
     * 校验验证码
     *
     * @param code     验证码
     * @param uuid     唯一标识
     */
    private void validateCaptcha(String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!StringUtils.equalsIgnoreCase(code, captcha)) {
            throw new CaptchaException();
        }
    }

}
