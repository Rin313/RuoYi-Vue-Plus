package org.dromara.system.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.system.domain.vo.BusCommentVo;
import org.dromara.system.domain.bo.BusCommentBo;
import org.dromara.system.service.IBusCommentService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 评价
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/comment")
public class BusCommentController extends BaseController {

    private final IBusCommentService busCommentService;

    /**
     * 查询评价列表
     */
    @SaCheckPermission("system:comment:list")
    @GetMapping("/list")
    public TableDataInfo<BusCommentVo> list(BusCommentBo bo, PageQuery pageQuery) {
        return busCommentService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出评价列表
     */
    @SaCheckPermission("system:comment:export")
    @Log(title = "评价", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BusCommentBo bo, HttpServletResponse response) {
        List<BusCommentVo> list = busCommentService.queryList(bo);
        ExcelUtil.exportExcel(list, "评价", BusCommentVo.class, response);
    }

    /**
     * 获取评价详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:comment:query")
    @GetMapping("/{id}")
    public R<BusCommentVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(busCommentService.queryById(id));
    }

    /**
     * 新增评价
     */
    @SaCheckPermission("system:comment:add")
    @Log(title = "评价", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BusCommentBo bo) {
        return toAjax(busCommentService.insertByBo(bo));
    }

    /**
     * 修改评价
     */
    @SaCheckPermission("system:comment:edit")
    @Log(title = "评价", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BusCommentBo bo) {
        return toAjax(busCommentService.updateByBo(bo));
    }

    /**
     * 删除评价
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:comment:remove")
    @Log(title = "评价", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(busCommentService.deleteWithValidByIds(List.of(ids), true));
    }
}
