package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@TableName("biz_log")
public class BizLog {

    @TableId
    private Long id;
    //业务中文标识
    private String bizType;
    //可灵活根据业务需求，绑定一些id、日期、符号，可用于条件、状态的判断
    private String bizKey;
    //[{"asset_name","amount","before","after"},]
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String,Object>> assetLog;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}