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
public class Novel extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableId
    private Long id;
    private String title;
    private String author;
    //封面
    private String url;
    private String intro;
    private String category;
    private String status;
    private Long viewCount;
    private String worldSetting;      
    private String mainStoryline;
    private String writingStyle;
    @TableField(typeHandler = JacksonTypeHandler.class)
    //可供选择的初始角色
    private List<Character> characters;
    @Data
    public static class Character{
        private String name;
        private String baseDescription;
        // 当前状态、持有物、位置等
        private String currentAttributes;
        private Boolean isPlayer=false;
    }
}
