package org.dromara.system.domain.vo;

import org.dromara.system.domain.AssetLog;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;




@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AssetLog.class)
public class AssetLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ExcelProperty(value = "ID")
    private Long id;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 资产类型
     */
    @ExcelProperty(value = "资产类型")
    private Long assetType;

    /**
     * 变动数量
     */
    @ExcelProperty(value = "变动数量")
    private Long amount;

    /**
     * 变动前数量
     */
    @ExcelProperty(value = "变动前数量")
    private Long beforeAmount;

    /**
     * 变动后数量
     */
    @ExcelProperty(value = "变动后数量")
    private Long afterAmount;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型")
    private String businessType;

    /**
     * 关联业务ID
     */
    @ExcelProperty(value = "关联业务ID")
    private Long refId;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;


}
