package org.dromara.system.domain.vo;

import org.dromara.system.domain.BusComment;
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
 * 评价视图对象 bus_comment
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BusComment.class)
public class BusCommentVo implements Serializable {

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

    /**
     * 订单ID
     */
    @ExcelProperty(value = "订单ID")
    private Long orderId;

    /**
     * 评价内容
     */
    @ExcelProperty(value = "评价内容")
    private String content;

    /**
     * 评分(1-5)
     */
    @ExcelProperty(value = "评分(1-5)")
    private Long rate;

    /**
     * 房间名称 (数据库不存在，需填充)
     */
    @ExcelProperty(value = "房间名称")
    private String roomTitle;

    /**
     * 客户名称 (数据库不存在，需填充)
     */
    @ExcelProperty(value = "客户名称")
    private String userName;
}
