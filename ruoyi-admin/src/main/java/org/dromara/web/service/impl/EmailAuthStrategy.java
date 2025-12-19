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
import org.dromara.common.core.BizException;
import org.dromara.common.core.config.BizProperties;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.model.EmailLoginBody;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.enums.LoginType;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.core.utils.ip.AddressUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.SysConfigService;
import org.dromara.system.service.SysUserService;
import org.dromara.web.domain.vo.LoginVo;
import org.dromara.web.service.IAuthStrategy;
import org.dromara.web.service.SysLoginService;
import org.springframework.stereotype.Service;

/**
 * 邮件认证策略
 *
 */
@Slf4j
@Service("email" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class EmailAuthStrategy implements IAuthStrategy {

    private final SysLoginService loginService;
    private final SysUserMapper userMapper;
    private final SysUserService userService;
    private final SysConfigService configService;

    @Override
    public LoginVo login(String body) {
        EmailLoginBody loginBody = JsonUtils.parseObject(body, EmailLoginBody.class);
        ValidatorUtils.validate(loginBody);
        String email = loginBody.getEmail();
        String emailCode = loginBody.getEmailCode();
        SysUserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
        LoginUser loginUser;
        if (ObjectUtil.isNull(user)) {
            if(BizProperties.autoRegister&&configService.selectRegisterEnabled()){
                if(!validateEmailCode(email, emailCode))
                    throw new BizException("无效的验证码");
                SysUser sysUser=new SysUser();
                sysUser.setEmail(email);
                sysUser.setNickName("用户_"+RandomUtil.randomString(8));
                sysUser.setPassword(BCrypt.hashpw(loginBody.getPassword()));
                sysUser.setInviteCode(userService.getUniqueInviteCode());
                if(StringUtils.isNotEmpty(loginBody.getInviteCode())){
                    SysUserVo parent=userService.selectUserByInviteCode(loginBody.getInviteCode());
                    if(ObjectUtils.isEmpty(parent))
                        throw new BizException("无效的邀请码");
                    sysUser.setParentId(parent.getUserId());
                }
                userMapper.insert(sysUser);
                loginService.recordLogininfor(email, Constants.REGISTER, MessageUtils.message("user.register.success"));
                loginUser = loginService.buildLoginUser(userMapper.selectVoById(sysUser.getUserId()));
            }
            else{
                log.info("登录用户：{} 不存在.", email);
                throw new BizException("用户不存在或验证码错误");
            }
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", email);
            throw new BizException(MessageUtils.message("user.blocked", email));
        } else{
            loginService.checkLogin(LoginType.EMAIL, user.getUserName(), () -> !validateEmailCode(email, emailCode));
            // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
            loginUser = loginService.buildLoginUser(user);
        }
        loginUser.setClientKey(AddressUtils.getClientType());
        SaLoginParameter model = new SaLoginParameter();
        // 自定义分配 不同用户体系 不同 token 授权时间 不设置默认走全局 yml 配置
        // 例如: 后台用户30分钟过期 app用户1天过期
        model.setTimeout(72000);
        model.setActiveTimeout(72000);
        // 生成token
        LoginHelper.login(loginUser, model);

        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        return loginVo;
    }

    /**
     * 校验邮箱验证码
     */
    private boolean validateEmailCode(String email, String emailCode) {
        String code = RedisUtils.getCacheObject(GlobalConstants.CAPTCHA_CODE_KEY + email);
        if (StringUtils.isBlank(code)) {
            loginService.recordLogininfor(email, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new BizException("验证码已失效");
        }
        return code.equals(emailCode);
    }

}
