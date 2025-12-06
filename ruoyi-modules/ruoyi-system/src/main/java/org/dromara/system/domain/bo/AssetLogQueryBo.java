package org.dromara.system.domain.bo;

import java.time.LocalDateTime;

import org.dromara.system.domain.AssetLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = AssetLog.class, reverseConvertGenerate = false)
public class AssetLogQueryBo {
    private Long userId;
    private Long assetType;
    private Long amount;
    private String businessType;
    private Long refId;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
