package org.dromara.system.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerationOptions {
    // 温度：越高越有创造力（故事模式推荐 0.9，严谨模式推荐 0.2-0.5）
    private Double temperature;
    
    // 核采样：控制词汇多样性
    private Double topP;
    
    // 最大输出 Token 数
    private Integer maxOutputTokens;
    
    // 是否开启思考模型 (如果后续需要支持 thinking 模式)
    private Boolean enableThinking; 
}