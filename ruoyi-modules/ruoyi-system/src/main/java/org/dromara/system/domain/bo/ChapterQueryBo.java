package org.dromara.system.domain.bo;

import org.dromara.system.domain.Chapter;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@AutoMapper(target = Chapter.class, reverseConvertGenerate = false)
public class ChapterQueryBo {
    @NotNull(message = "关联小说ID不能为空")
    private Long novelId;
}
