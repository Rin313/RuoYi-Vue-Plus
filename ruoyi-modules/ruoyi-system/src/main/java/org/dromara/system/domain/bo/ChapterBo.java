package org.dromara.system.domain.bo;

import org.dromara.system.domain.TChapter;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 小说章节业务对象 t_chapter
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TChapter.class, reverseConvertGenerate = false)
public class ChapterBo extends BaseEntity {
    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;
    /**
     * 关联小说ID
     */
    @NotNull(message = "关联小说ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long novelId;
}
