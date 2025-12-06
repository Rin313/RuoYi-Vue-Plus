package org.dromara.system.domain.bo;

import org.dromara.system.domain.Novel;

import java.time.LocalDateTime;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = Novel.class, reverseConvertGenerate = false)
public class NovelVisitorQueryBo {
    /**
     * 标题
     */
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

    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
