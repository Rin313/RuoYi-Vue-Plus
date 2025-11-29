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
import org.dromara.system.domain.vo.BusRoomVo;
import org.dromara.system.domain.bo.BusRoomBo;
import org.dromara.system.service.IBusRoomService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 民宿房源
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/room")
public class BusRoomController extends BaseController {

    private final IBusRoomService busRoomService;

    @SaCheckPermission("system:room:list")
    @GetMapping("/my-favorites")
    public TableDataInfo<BusRoomVo> myFavorites(BusRoomBo bo, PageQuery pageQuery) {
        return busRoomService.queryFavoritePageList(bo, pageQuery);
    }

    /**
     * 查询民宿房源列表
     */
    @SaCheckPermission("system:room:list")
    @GetMapping("/list")
    public TableDataInfo<BusRoomVo> list(BusRoomBo bo, PageQuery pageQuery) {
        return busRoomService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出民宿房源列表
     */
    @SaCheckPermission("system:room:export")
    @Log(title = "民宿房源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BusRoomBo bo, HttpServletResponse response) {
        List<BusRoomVo> list = busRoomService.queryList(bo);
        ExcelUtil.exportExcel(list, "民宿房源", BusRoomVo.class, response);
    }

    /**
     * 获取民宿房源详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:room:query")
    @GetMapping("/{id}")
    public R<BusRoomVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(busRoomService.queryById(id));
    }

    /**
     * 新增民宿房源
     */
    @SaCheckPermission("system:room:add")
    @Log(title = "民宿房源", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BusRoomBo bo) {
        return toAjax(busRoomService.insertByBo(bo));
    }

    /**
     * 修改民宿房源
     */
    @SaCheckPermission("system:room:edit")
    @Log(title = "民宿房源", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BusRoomBo bo) {
        return toAjax(busRoomService.updateByBo(bo));
    }

    /**
     * 删除民宿房源
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:room:remove")
    @Log(title = "民宿房源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(busRoomService.deleteWithValidByIds(List.of(ids), true));
    }
}
