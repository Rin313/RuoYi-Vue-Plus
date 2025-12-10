package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serial;

@Data
@TableName
public class Chapter{

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
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
