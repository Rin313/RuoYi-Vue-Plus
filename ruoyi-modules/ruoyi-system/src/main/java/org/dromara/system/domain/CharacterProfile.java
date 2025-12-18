package org.dromara.system.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CharacterProfile {
    private String name;
    // --- 静态部分 (Static) ---
    private String baseDescription;   // 外貌、性格、背景
    
    // --- 动态部分 (Dynamic) ---
    private String currentAttributes; // 当前状态、持有物、情绪
    private boolean isPlayer;
}