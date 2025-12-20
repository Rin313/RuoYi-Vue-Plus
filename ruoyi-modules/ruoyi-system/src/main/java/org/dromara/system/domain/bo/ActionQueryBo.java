package org.dromara.system.domain.bo;

import org.dromara.system.domain.Action;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@AutoMapper(target = Action.class, reverseConvertGenerate = false)
public class ActionQueryBo {
    //0 点赞,1 评论  
    private Integer actionType;
    @NotNull(message = "关联内容类型不能为空")
    private Integer targetType;
    @NotNull(message = "关联内容ID不能为空")
    private Long targetId;
}
