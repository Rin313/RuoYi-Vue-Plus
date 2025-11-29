package org.dromara.system.domain.bo;

import org.dromara.system.domain.BusOrder;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 民宿订单业务对象 bus_order
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BusOrder.class, reverseConvertGenerate = false)
public class BusOrderBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 预订人ID(关联sys_user)
     */
    @NotNull(message = "预订人ID(关联sys_user)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 房间ID
     */
    @NotNull(message = "房间ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long roomId;

    /**
     * 入住日期
     */
    @NotNull(message = "入住日期不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date startDate;

    /**
     * 离店日期
     */
    @NotNull(message = "离店日期不能为空", groups = { AddGroup.class, EditGroup.class })
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


}
