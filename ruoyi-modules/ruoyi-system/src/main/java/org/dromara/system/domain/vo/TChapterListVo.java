package org.dromara.system.domain.vo;

import org.dromara.system.domain.TChapter;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;



/**
 * 小说章节视图对象 t_chapter
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = TChapter.class)
public class TChapterListVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 关联小说ID
     */
    @ExcelProperty(value = "关联小说ID")
    private Long novelId;

    /**
     * 标题
     */
    @ExcelProperty(value = "标题")
    private String title;

    /**
     * 章节序号
     */
    @ExcelProperty(value = "章节序号")
    private Long chapterIndex;


}
