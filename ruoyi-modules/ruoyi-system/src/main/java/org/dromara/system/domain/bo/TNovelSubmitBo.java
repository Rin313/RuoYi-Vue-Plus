package org.dromara.system.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.system.domain.TNovel;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 小说业务对象 t_novel
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@AutoMapper(target = TNovel.class, reverseConvertGenerate = false)
public class TNovelSubmitBo {
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


}
