package org.dromara.system.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_log")
public class AssetLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 资产类型
     */
    private Long assetType;

    /**
     * 变动数量
     */
    private Long amount;

    /**
     * 变动前数量
     */
    private Long beforeAmount;

    /**
     * 变动后数量
     */
    private Long afterAmount;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 关联业务ID
     */
    private Long refId;

    /**
     * 备注
     */
    private String remark;


}
