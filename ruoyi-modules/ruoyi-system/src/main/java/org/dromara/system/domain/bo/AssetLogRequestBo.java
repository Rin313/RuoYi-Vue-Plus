package org.dromara.system.domain.bo;

import org.dromara.system.domain.AssetLog;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
@AutoMapper(target = AssetLog.class, reverseConvertGenerate = false)
public class AssetLogRequestBo {
    @NotNull(message = "虚拟币类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long assetType;
    @NotNull(message = "关联业务ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long refId;
}
