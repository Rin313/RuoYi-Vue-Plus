package org.dromara.system.domain;

import lombok.Data;
import java.util.List;
@Data
public class ChapterSettlementRequest {
    //当前小说上下文（包含旧的摘要）
    private NovelContext context;
    //当前所有角色的列表（包含本章开始时的状态）
    private List<CharacterProfile> characters;
    //本章生成的纯文本内容（去除指令后的故事正文）
    private String pureStoryText;
}