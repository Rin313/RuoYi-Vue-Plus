package org.dromara.system.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(autoResultMap=true)
public class Story extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private Long novelId;
    private String previousSummary;
    @TableField(typeHandler = JacksonTypeHandler.class)
    //包含用户选择或创建的角色在内的全部角色
    private List<org.dromara.system.domain.Novel.Character> characters;
    @TableLogic
    private String delFlag;
}
//前文摘要（不手动编辑，第一章摘要生成之前的叙述应该写进主线里）
