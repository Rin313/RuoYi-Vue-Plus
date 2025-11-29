package org.dromara.system.domain.bo;

import org.dromara.system.domain.BusComment;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 评价业务对象 bus_comment
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BusComment.class, reverseConvertGenerate = false)
public class BusCommentBo extends BaseEntity {

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

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long orderId;

    /**
     * 评价内容
     */
    @NotBlank(message = "评价内容不能为空", groups = { AddGroup.class, EditGroup.class })
    private String content;

    /**
     * 评分(1-5)
     */
    private Long rate;


}
