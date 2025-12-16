package org.dromara.system.domain.bo;

import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.system.domain.Novel;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 小说业务对象 novel
 *
 */
@Data
@AutoMapper(target = Novel.class, reverseConvertGenerate = false)
public class NovelInsertBo {
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
    /**
     * 0-正常 1-停用
     */
    private String status;


}
