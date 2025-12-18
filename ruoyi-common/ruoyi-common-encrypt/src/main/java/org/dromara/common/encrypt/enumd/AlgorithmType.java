package org.dromara.common.encrypt.enumd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.common.encrypt.core.encryptor.*;

/**
 * 算法名称
 *
 */
@Getter
@AllArgsConstructor
public enum AlgorithmType {

    /**
     * 默认走yml配置
     */
    DEFAULT(null),

    /**
     * aes
     */
    AES(AesEncryptor.class),

    /**
     * rsa
     */
    RSA(RsaEncryptor.class);

    private final Class<? extends AbstractEncryptor> clazz;
}
