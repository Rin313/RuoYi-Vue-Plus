package org.dromara.system.controller.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.BizException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BizType;
import org.dromara.common.satoken.utils.LoginHelper;

import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.bo.SysUserPasswordBo;
import org.dromara.system.domain.bo.SysUserProfileBo;
import org.dromara.system.domain.vo.ProfileUserVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人信息 业务处理
 *
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController {

    private final SysUserService userService;

    /**
     * 修改用户信息
     */
    @RepeatSubmit
    @Log(title = "个人信息", businessType = BizType.UPDATE)
    @PostMapping("/update")
    public void updateProfile(@Validated @RequestBody SysUserProfileBo profile) {
        SysUserBo user = BeanUtil.toBean(profile, SysUserBo.class);
        user.setUserId(LoginHelper.getUserId());
        String username = LoginHelper.getUsername();
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            throw new BizException("修改用户'" + username + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            throw new BizException("修改用户'" + username + "'失败，邮箱账号已存在");
        }
        userService.updateUserProfile(user);
    }

    /**
     * 重置密码
     *
     * @param bo 新旧密码
     */
    @RepeatSubmit
    @Log(title = "个人信息", businessType = BizType.UPDATE)
    @PostMapping("/updatePwd")
    public void updatePwd(@Validated @RequestBody SysUserPasswordBo bo) {
        SysUserVo user = userService.selectUserById(LoginHelper.getUserId());
        String password = user.getPassword();
        if (!BCrypt.checkpw(bo.getOldPassword(), password)) {
            throw new BizException("修改密码失败，旧密码错误");
        }
        if (BCrypt.checkpw(bo.getNewPassword(), password)) {
            throw new BizException("新密码不能与旧密码相同");
        }
        userService.resetUserPwd(user.getUserId(), BCrypt.hashpw(bo.getNewPassword()));
    }

    // /**
    //  * 头像上传
    //  *
    //  * @param avatarfile 用户头像
    //  */
    // @RepeatSubmit
    // @Log(title = "用户头像", businessType = BizType.UPDATE)
    // @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public AvatarVo avatar(@RequestPart("avatarfile") MultipartFile avatarfile) {
    //     if (ObjectUtils.isNotEmpty(avatarfile)&&!avatarfile.isEmpty()) {//疑似一个校验长度，一个校验内容，未测试，待询问AI
    //         String extension = FileUtil.extName(avatarfile.getOriginalFilename());
    //         if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
    //             throw new BizException("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
    //         }
    //         // SysOssVo oss = ossService.upload(avatarfile);
    //         // String avatar = oss.getUrl();
    //         // boolean updateSuccess = DataPermissionHelper.ignore(() -> userService.updateUserAvatar(LoginHelper.getUserId(), oss.getOssId()));
    //         // if (updateSuccess) {
    //         //     return R.ok(new AvatarVo(avatar));
    //         // }
    //     }
    //     throw new BizException("上传图片异常，请联系管理员");
    // }

    /**
     * 用户头像信息
     *
     * @param imgUrl 头像地址
     */
    public record AvatarVo(String imgUrl) {}

}
