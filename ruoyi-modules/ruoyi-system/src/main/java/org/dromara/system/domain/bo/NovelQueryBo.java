package org.dromara.system.domain.bo;

import org.dromara.system.domain.TNovel;

import java.time.LocalDateTime;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 小说业务对象 t_novel
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@AutoMapper(target = TNovel.class, reverseConvertGenerate = false)
public class NovelQueryBo {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    /**
     * 作者名称
     */
    private String author;

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

    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
