package org.dromara.system.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.system.domain.BusOrder;
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
 * 民宿订单视图对象 bus_order
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BusOrder.class)
public class BusOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 预订人ID(关联sys_user)
     */
    @ExcelProperty(value = "预订人ID(关联sys_user)")
    private Long userId;

    /**
     * 房间ID
     */
    @ExcelProperty(value = "房间ID")
    private Long roomId;

    /**
     * 入住日期
     */
    @ExcelProperty(value = "入住日期")
    private Date startDate;

    /**
     * 离店日期
     */
    @ExcelProperty(value = "离店日期")
    private Date endDate;

    /**
     * 总金额
     */
    @ExcelProperty(value = "总金额")
    private Long totalAmount;

    /**
     * 状态(0待确认/已预约 1已确认/待入住 2已入住 3已完成 4已取消)
     */
    @ExcelProperty(value = "状态(0待确认/已预约 1已确认/待入住 2已入住 3已完成 4已取消)")
    private String status;

    /**
     * 备注/客户需求
     */
    @ExcelProperty(value = "备注/客户需求")
    private String remark;

/** 额外字段：房间名称 */
    private String roomTitle;

    /** 额外字段：客户名称（sys_user.nick_name 或 user_name） */
    private String customerName;
}
