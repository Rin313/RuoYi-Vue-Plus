package org.dromara.system.domain.bo;

import java.time.LocalDateTime;

import org.dromara.system.domain.BizLog;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = BizLog.class, reverseConvertGenerate = false)
public class BizLogQueryBo {
    private Long userId;
    private Long assetType;
    private Long amount;
    private String businessType;
    private Long refId;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
}
