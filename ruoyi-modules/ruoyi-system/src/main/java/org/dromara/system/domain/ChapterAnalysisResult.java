package org.dromara.system.domain;

import java.util.List;

import lombok.Data;

@Data
public class ChapterAnalysisResult {
    private String newSummary;
    private List<CharacterUpdate> characterUpdates;

    @Data
    public static class CharacterUpdate {
        private String name;
        private String newAttributes;
    }
}