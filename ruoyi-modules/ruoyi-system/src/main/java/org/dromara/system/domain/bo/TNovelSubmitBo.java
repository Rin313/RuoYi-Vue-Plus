package org.dromara.system.domain.bo;

import org.dromara.system.domain.TNovel;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 小说宽业务对象 t_novel
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@AutoMapper(target = TNovel.class, reverseConvertGenerate = false)
public class TNovelSubmitBo {

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
