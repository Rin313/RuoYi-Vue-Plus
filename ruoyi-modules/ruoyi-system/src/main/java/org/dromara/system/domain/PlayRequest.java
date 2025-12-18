package org.dromara.system.domain;

import java.util.List;

import lombok.Data;

@Data
public class PlayRequest {
    private Long novelId;
    private String userAction;
    private NovelContext context;
    private List<CharacterProfile> characters; 
    private List<ChatMessage> currentChapterHistory;
}