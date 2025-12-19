package org.dromara.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String apiKey;
    private String model = "gemini-2.5-flash-preview-09-2025";
    
    // HTTP 配置
    private int timeout = 6000;           // 请求超时
    //private int maxRetryAttempts = 3;   // 最大重试次数
    
    // 连接池配置
    private int maxConnections = 64;
    private int maxConnectionsPerHost = 16;
    
    // 生成配置
    private int maxOutputTokens = 256;
    private float temperature = 0.8f;   // 创意写作适合较高温度
    private float topP = 0.95f;
}