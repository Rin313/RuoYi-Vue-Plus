package org.dromara.system.domain.vo;

import org.dromara.system.domain.BusRoom;
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
 * 民宿房源视图对象 bus_room
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BusRoom.class)
public class BusRoomVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 房间名称
     */
    @ExcelProperty(value = "房间名称")
    private String title;

    /**
     * 房间简介/民宿特色
     */
    @ExcelProperty(value = "房间简介/民宿特色")
    private String info;

    /**
     * 每晚价格
     */
    @ExcelProperty(value = "每晚价格")
    private Long price;

    /**
     * 图片地址(多张逗号分隔)
     */
    @ExcelProperty(value = "图片地址(多张逗号分隔)")
    private String images;

    /**
     * 配套设施
     */
    @ExcelProperty(value = "配套设施")
    private String facilities;

    /**
     * 状态(0上架 1下架)
     */
    @ExcelProperty(value = "状态(0上架 1下架)")
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;
    /**
     * 收藏记录ID (如果未收藏则为空)
     */
    private Long favoriteId; 
}
