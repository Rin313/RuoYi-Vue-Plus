package org.dromara.system.domain.bo;

import org.dromara.system.domain.Novel;

import java.time.LocalDateTime;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 小说业务对象 novel
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@AutoMapper(target = Novel.class, reverseConvertGenerate = false)
public class NovelQueryBo {
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

    /**
     * 0-正常 1-停用
     */
    private String status;

    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
