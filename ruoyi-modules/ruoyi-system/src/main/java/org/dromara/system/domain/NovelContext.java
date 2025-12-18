package org.dromara.system.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NovelContext {
    // === 强静态 (最容易命中缓存) ===
    private String worldSetting;      
    private String mainStoryline;     
    private String writingStyle;      

    // === 动态 (每章变化) ===
    private String previousSummary;   // 截止上一章的剧情摘要
}