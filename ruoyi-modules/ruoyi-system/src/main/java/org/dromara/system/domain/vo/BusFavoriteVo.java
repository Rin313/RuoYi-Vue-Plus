package org.dromara.system.domain.vo;

import org.dromara.system.domain.BusFavorite;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 收藏视图对象 bus_favorite
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BusFavorite.class)
public class BusFavoriteVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 房间ID
     */
    @ExcelProperty(value = "房间ID")
    private Long roomId;


}
