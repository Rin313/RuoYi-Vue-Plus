package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.dromara.common.mybatis.core.domain.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("action")
public class Action extends BaseEntity{
    @TableId
    private Long id;
    //0 点赞,1 评论,2 收藏  
    private Integer targetType;
    private Long targetId;
    private String content;
    private String username;
}