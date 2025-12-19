package org.dromara.common.core.config;

import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.ProxyOptions;
import com.google.genai.types.ProxyType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    private final GeminiProperties properties;

    public GeminiConfig(GeminiProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Client geminiClient() {
        // 配置HTTP选项：超时和重试策略
        HttpOptions httpOptions = HttpOptions.builder()
            .timeout(properties.getTimeout())
            // .retryOptions(
            //     HttpRetryOptions.builder()
            //         .attempts(properties.getMaxRetryAttempts())
            //         .httpStatusCodes(408, 429)
            //         .build()
            // )
            .build();
        
        // 配置连接池 + 代理设置
        ClientOptions clientOptions = ClientOptions.builder()
            .maxConnections(properties.getMaxConnections())
            .maxConnectionsPerHost(properties.getMaxConnectionsPerHost())
            // ✅ 添加代理配置
            .proxyOptions(
                ProxyOptions.builder()
                    .type(ProxyType.Known.SOCKS) 
                    .host("127.0.0.1")
                    .port(10808)
                    .build()
            )
            .build();
        return Client.builder()
            .apiKey(properties.getApiKey())
            .httpOptions(httpOptions)
            .clientOptions(clientOptions)
            .build();
    }
}