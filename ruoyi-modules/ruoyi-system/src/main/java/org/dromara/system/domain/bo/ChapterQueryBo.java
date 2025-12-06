package org.dromara.system.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.system.domain.Chapter;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小说章节业务对象 chapter
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@AutoMapper(target = Chapter.class, reverseConvertGenerate = false)
public class ChapterQueryBo {
    /**
     * 关联小说ID
     */
    @NotNull(message = "关联小说ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long novelId;
}
