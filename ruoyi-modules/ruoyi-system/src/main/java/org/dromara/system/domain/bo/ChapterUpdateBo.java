package org.dromara.system.domain.bo;

import org.dromara.system.domain.Chapter;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
@AutoMapper(target = Chapter.class, reverseConvertGenerate = false)
public class ChapterUpdateBo {
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;
    @NotBlank(message = "标题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;
    private String content;
    private Long chapterIndex;
}