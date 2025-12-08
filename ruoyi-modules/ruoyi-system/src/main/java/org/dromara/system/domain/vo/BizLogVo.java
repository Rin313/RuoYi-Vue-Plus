package org.dromara.system.domain.vo;

import org.dromara.system.domain.BizLog;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;




@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BizLog.class)
public class BizLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * [{"asset_name","amount","before","after"},]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String,Object>> assetLog;

    /**
     * 业务类型
     */
    private String bizType;

    private Long createBy;
    private Date createTime;


}
