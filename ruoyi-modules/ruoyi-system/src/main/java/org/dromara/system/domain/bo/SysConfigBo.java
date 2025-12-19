package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import org.dromara.system.domain.SysConfig;

/**
 * 参数配置业务对象 sys_config
 *
 */

@Data
@AutoMapper(target = SysConfig.class, reverseConvertGenerate = false)
public class SysConfigBo {

    /**
     * 参数主键
     */
    private Long configId;

    /**
     * 参数键名
     */
    @NotBlank(message = "参数键名不能为空")
    @Size(min = 0, max = 100, message = "参数键名长度不能超过{max}个字符")
    private String configKey;

    /**
     * 参数键值
     */
    @NotBlank(message = "参数键值不能为空")
    @Size(min = 0, max = 500, message = "参数键值长度不能超过{max}个字符")
    private String configValue;

    /**
     * 备注
     */
    private String remark;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;

}
