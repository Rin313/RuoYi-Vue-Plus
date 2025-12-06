package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serial;

/**
 * 小说章节对象 chapter
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@TableName("chapter")
public class Chapter{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 关联小说ID
     */
    private Long novelId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 章节序号
     */
    private Long chapterIndex;


}
