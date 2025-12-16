package org.dromara.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.metadata.IPage;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BizType;
import org.dromara.common.mybatis.core.domain.PageQuery;

import org.dromara.system.domain.bo.SysOperLogBo;
import org.dromara.system.domain.vo.SysOperLogVo;
import org.dromara.system.service.SysOperLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController {

    private final SysOperLogService operLogService;

    /**
     * 获取操作日志记录列表
     */
    @SaCheckPermission("monitor:operlog:list")
    @GetMapping("/list")
    public IPage<SysOperLogVo> list(SysOperLogBo operLog, PageQuery pageQuery) {
        return operLogService.selectPageOperLogList(operLog, pageQuery);
    }

    /**
     * 导出操作日志记录列表
     */
    @Log(title = "操作日志", businessType = BizType.EXPORT)
    @SaCheckPermission("monitor:operlog:export")
    @PostMapping("/export")
    public void export(SysOperLogBo operLog, HttpServletResponse response) {
        List<SysOperLogVo> list = operLogService.selectOperLogList(operLog);
        ExcelUtil.exportExcel(list, "操作日志", SysOperLogVo.class, response);
    }
}
