package org.dromara.common.encrypt.core;

import lombok.Data;
import org.dromara.common.encrypt.enumd.EncodeType;

/**
 * 加密上下文 用于encryptor传递必要的参数。
 *
 */
@Data
public class EncryptContext {

    /**
     * 安全秘钥
     */
    private String password;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 编码方式，base64/hex
     */
    private EncodeType encode;

}
