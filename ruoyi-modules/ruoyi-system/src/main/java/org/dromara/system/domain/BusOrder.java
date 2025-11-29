package org.dromara.system.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 民宿订单对象 bus_order
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_order")
public class BusOrder extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 预订人ID(关联sys_user)
     */
    private Long userId;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 入住日期
     */
    private Date startDate;

    /**
     * 离店日期
     */
    private Date endDate;

    /**
     * 总金额
     */
    private Long totalAmount;

    /**
     * 状态(0待确认/已预约 1已确认/待入住 2已入住 3已完成 4已取消)
     */
    private String status;

    /**
     * 备注/客户需求
     */
    private String remark;

 /** 状态：0待确认/已预约 */
    public static final String STATUS_WAIT_CONFIRM = "0";
    /** 状态：1已确认/待入住 */
    public static final String STATUS_CONFIRMED = "1";
    /** 状态：2待审核/已登记 */
    public static final String STATUS_REGISTERED = "2";
    /** 状态：3已入住 */
    public static final String STATUS_CHECKED_IN = "3";
    /** 状态：4已完成 */
    public static final String STATUS_FINISHED = "4";
    /** 状态：5已取消 */
    public static final String STATUS_CANCELLED = "5";
}
