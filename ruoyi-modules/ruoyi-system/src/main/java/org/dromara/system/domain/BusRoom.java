package org.dromara.system.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 民宿房源对象 bus_room
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_room")
public class BusRoom extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 房间名称
     */
    private String title;

    /**
     * 房间简介/民宿特色
     */
    private String info;

    /**
     * 每晚价格
     */
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
