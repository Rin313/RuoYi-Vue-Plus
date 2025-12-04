package org.dromara.common.core.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * 密码登录对象
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordLoginBody extends LoginBody {
    /**
     * 邮箱
     */
    @Email(message = "{user.email.not.valid}")
    private String email;
    /**
     * 用户名
     */
    @Length(min = 2, max = 30, message = "{user.username.length.valid}")
    private String username;
    /**
     * 手机号
     */
    private String phonenumber;
    /**
     * 用户密码
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = 5, max = 30, message = "{user.password.length.valid}")
//    @Pattern(regexp = RegexConstants.PASSWORD, message = "{user.password.format.valid}")
    private String password;
    /**
     * 验证码（除了密码登录，邮箱/手机/第三方一般都不需要额外保护）
     */
    private String code;
    /**
     * 唯一标识
     */
    private String uuid;
    /**
     * 注册模式，不拆分接口减少维护难度
     */
    private Boolean register=false;

}
