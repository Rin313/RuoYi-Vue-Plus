package org.dromara.system.domain.bo;

import org.dromara.system.domain.Action;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = Action.class, reverseConvertGenerate = false)
public class ActionUpdateBo{
    private Long id;
    private String content;
}