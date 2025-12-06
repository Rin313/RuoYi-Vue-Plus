package org.dromara.system.domain.vo;

import org.dromara.system.domain.TNovel;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;



/**
 * 小说视图对象 t_novel
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = TNovel.class)
public class TNovelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 标题
     */
    @ExcelProperty(value = "标题")
    private String title;

    /**
     * 作者名称
     */
    @ExcelProperty(value = "作者名称")
    private String author;

    /**
     * 封面
     */
    @ExcelProperty(value = "封面")
    private String url;

    /**
     * 小说简介
     */
    @ExcelProperty(value = "小说简介")
    private String intro;

    /**
     * 分类
     */
    @ExcelProperty(value = "分类")
    private String category;

    /**
     * 0-正常 1-停用
     */
    @ExcelProperty(value = "0-正常 1-停用")
    private String status;

    /**
     * 浏览量
     */
    @ExcelProperty(value = "浏览量")
    private Long viewCount;


}
