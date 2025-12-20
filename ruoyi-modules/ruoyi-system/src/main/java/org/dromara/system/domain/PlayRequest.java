package org.dromara.system.domain;

import lombok.Data;

@Data
public class PlayRequest {
    private Long chapterId;
    private String userAction;
}