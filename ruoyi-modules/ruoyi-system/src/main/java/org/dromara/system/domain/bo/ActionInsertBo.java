package org.dromara.system.domain.bo;

import org.dromara.system.domain.Action;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = Action.class, reverseConvertGenerate = false)
public class ActionInsertBo{
    //0 点赞,1 评论  
    private Integer actionType;
    private Integer targetType;
    private Long targetId;
    private String content;
}