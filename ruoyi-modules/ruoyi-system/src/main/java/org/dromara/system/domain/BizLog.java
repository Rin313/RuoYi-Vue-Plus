package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Data;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@TableName("biz_log")
public class BizLog {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;
    /**
     * 可记忆的业务的唯一标识
     */
    private String bizCode;
    /**
     * 业务类型
     */
    private String bizType;
    /**
     * [{"asset_name","amount","before","after"},]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String,Object>> assetLog;

    
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
