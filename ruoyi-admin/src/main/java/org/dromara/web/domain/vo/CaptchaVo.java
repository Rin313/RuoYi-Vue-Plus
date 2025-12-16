package org.dromara.web.domain.vo;

import lombok.Data;

/**
 * 验证码信息
 *
 */
@Data
public class CaptchaVo {

    private String uuid;

    /**
     * 验证码图片
     */
    private String img;

}
