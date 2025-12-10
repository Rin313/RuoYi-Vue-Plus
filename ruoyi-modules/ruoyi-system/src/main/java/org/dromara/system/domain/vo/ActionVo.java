package org.dromara.system.domain.vo;

import org.dromara.system.domain.Action;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Action.class)
public class ActionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    //0 点赞,1 评论  
    private Integer actionType;
    private Integer targetType;
    private Long targetId;
    private String content;
    private String username;
    private Date createTime;

}