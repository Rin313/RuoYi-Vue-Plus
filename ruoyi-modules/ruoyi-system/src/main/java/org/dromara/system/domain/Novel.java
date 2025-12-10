package org.dromara.system.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName
public class Novel extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 作者名称
     */
    private String author;

    /**
     * 封面
     */
    private String url;

    /**
     * 小说简介
     */
    private String intro;

    /**
     * 分类
     */
    private String category;

    /**
     * 0-正常 1-停用
     */
    private String status;

    /**
     * 浏览量
     */
    private Long viewCount;


}
