package org.dromara.web.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.domain.model.RegisterBody;
import org.dromara.common.core.exception.user.CaptchaException;
import org.dromara.common.core.exception.user.CaptchaExpireException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.log.event.LogininforEvent;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * 注册校验方法
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysRegisterService {

    private final ISysUserService userService;
    private final SysUserMapper userMapper;
    private final CaptchaProperties captchaProperties;

    /**
     * 注册
     */
    public void register(RegisterBody registerBody) {
        String email = registerBody.getEmail();
        String password = registerBody.getPassword();

        boolean captchaEnabled = captchaProperties.getEnable();
        if (captchaEnabled) {
            validateCaptcha(email, registerBody.getCode(), registerBody.getUuid());
        }
        SysUserBo sysUser = new SysUserBo();
        sysUser.setUserName(email);
        sysUser.setNickName(email);
        sysUser.setPassword(BCrypt.hashpw(password));
        sysUser.setInviteCode(userService.getUniqueInviteCode());
        if(StringUtils.isNotEmpty(registerBody.getInviteCode())){
            SysUserVo parent=userService.selectUserByInviteCode(registerBody.getInviteCode());
            if(ObjectUtils.isEmpty(parent))
                throw new UserException("user.invitecode.unknown");
            sysUser.setParentId(parent.getUserId());
        }
        boolean exist = userMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, sysUser.getEmail()));
        if (exist) {
            throw new UserException("user.register.save.error", email);
        }
        boolean regFlag = userService.registerUser(sysUser);
        if (!regFlag) {
            throw new UserException("user.register.error");
        }
        recordLogininfor(email, Constants.REGISTER, MessageUtils.message("user.register.success"));
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!StringUtils.equalsIgnoreCase(code, captcha)) {
            recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     * @return
     */
    private void recordLogininfor(String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }

}
