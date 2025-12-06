package org.dromara.system.domain.bo;

import org.dromara.system.domain.Chapter;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 小说章节业务对象 chapter
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@AutoMapper(target = Chapter.class, reverseConvertGenerate = false)
public class ChapterUpdateBo {
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;
    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空", groups = { AddGroup.class, EditGroup.class })
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