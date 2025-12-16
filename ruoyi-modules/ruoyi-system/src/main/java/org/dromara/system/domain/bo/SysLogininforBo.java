package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.SysLogininfor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 系统访问记录业务对象 sys_logininfor
 *
 */

@Data
@AutoMapper(target = SysLogininfor.class, reverseConvertGenerate = false)
public class SysLogininforBo {

    /**
     * 访问ID
     */
    private Long infoId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 客户端
     */
    private String clientKey;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态（0成功 1失败）
     */
    private String status;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 访问时间
     */
    private Date loginTime;

    private LocalDateTime beginTime;
    private LocalDateTime endTime;


}
