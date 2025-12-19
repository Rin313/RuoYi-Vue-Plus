package org.dromara.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录对象
 *
 */

@Data
public class LoginBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 授权类型
     */
    @NotBlank(message = "{auth.grant.type.not.blank}")
    private String grantType;
    /**
     * 邀请码
     */
    private String InviteCode;
    
    private Boolean register;//当前端设计想要拆分出两个页面时，登录时自动注册不是预期的行为，应该明确指示
}
