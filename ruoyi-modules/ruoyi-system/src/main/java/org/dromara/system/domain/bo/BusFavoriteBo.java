package org.dromara.system.domain.bo;

import org.dromara.system.domain.BusFavorite;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 收藏业务对象 bus_favorite
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BusFavorite.class, reverseConvertGenerate = false)
public class BusFavoriteBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 房间ID
     */
    @NotNull(message = "房间ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long roomId;


}
