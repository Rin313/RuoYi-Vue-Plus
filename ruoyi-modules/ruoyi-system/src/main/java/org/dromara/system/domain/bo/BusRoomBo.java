package org.dromara.system.domain.bo;

import org.dromara.system.domain.BusRoom;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 民宿房源业务对象 bus_room
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BusRoom.class, reverseConvertGenerate = false)
public class BusRoomBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 房间名称
     */
    @NotBlank(message = "房间名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String title;

    /**
     * 房间简介/民宿特色
     */
    private String info;

    /**
     * 每晚价格
     */
    @NotNull(message = "每晚价格不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long price;

    /**
     * 图片地址(多张逗号分隔)
     */
    private String images;

    /**
     * 配套设施
     */
    private String facilities;

    /**
     * 状态(0上架 1下架)
     */
    private String status;


}
