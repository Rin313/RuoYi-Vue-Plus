package org.dromara.system.domain.bo;

import org.dromara.system.domain.Chapter;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
@AutoMapper(target = Chapter.class, reverseConvertGenerate = false)
public class ChapterUpdateBo {
    @NotNull(message = "主键ID不能为空")
    private Long id;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private Long chapterIndex;
}