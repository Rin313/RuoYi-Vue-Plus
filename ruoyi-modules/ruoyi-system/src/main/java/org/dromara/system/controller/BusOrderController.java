package org.dromara.system.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckLogin;
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
import org.dromara.system.domain.vo.BusOrderVo;
import org.dromara.system.domain.bo.BusOrderBo;
import org.dromara.system.service.IBusOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 民宿订单
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/order")
public class BusOrderController extends BaseController {

    private final IBusOrderService busOrderService;

    /**
     * 查询民宿订单列表
     */
    @SaCheckPermission("system:order:list")
    @GetMapping("/list")
    public TableDataInfo<BusOrderVo> list(BusOrderBo bo, PageQuery pageQuery) {
        return busOrderService.queryPageList(bo, pageQuery);
    }

    /**
     * 当前用户相关的订单列表（房东或预订人）
     */
    @SaCheckLogin  // 或者定义一个单独的权限 @SaCheckPermission("system:order:myList")
    @GetMapping("/my-list")
    public TableDataInfo<BusOrderVo> myList(BusOrderBo bo, PageQuery pageQuery) {
        return busOrderService.queryMyOrderPage(bo, pageQuery);
    }
/**
     * 接受预约 (状态0 -> 1)
     */
    //@SaCheckPermission("bus:order:confirm")
    @Log(title = "民宿订单-接受预约", businessType = BusinessType.UPDATE)
    @PostMapping("/confirm/{id}")
    public R<Void> confirm(@PathVariable Long id) {
        return toAjax(busOrderService.confirmOrder(id));
    }

    /**
     * 入住登记 (状态1 -> 2)
     * 这里使用 Body 接收，因为登记可能需要填写备注信息
     */
    //@SaCheckPermission("bus:order:register")
    @Log(title = "民宿订单-入住登记", businessType = BusinessType.UPDATE)
    @PostMapping("/register")
    public R<Void> register(@RequestBody BusOrderBo bo) {
        if (bo.getId() == null) {
            return R.fail("订单ID不能为空");
        }
        return toAjax(busOrderService.registerCheckIn(bo));
    }

    /**
     * 入住审核 (状态2 -> 3)
     */
    //@SaCheckPermission("bus:order:audit")
    @Log(title = "民宿订单-入住审核", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/{id}")
    public R<Void> audit(@PathVariable Long id) {
        return toAjax(busOrderService.auditCheckIn(id));
    }

    /**
     * 取消订单 (状态变为 5)
     */
    //@SaCheckPermission("bus:order:cancel")
    @Log(title = "民宿订单-取消", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        return toAjax(busOrderService.cancelOrder(id));
    }
    /**
     * 导出当前用户相关的订单
     */
    @SaCheckLogin  // 或自定义权限
    @Log(title = "我的民宿订单", businessType = BusinessType.EXPORT)
    @PostMapping("/myExport")
    public void myExport(BusOrderBo bo, HttpServletResponse response) {
        List<BusOrderVo> list = busOrderService.queryMyOrderList(bo);
        ExcelUtil.exportExcel(list, "我的民宿订单", BusOrderVo.class, response);
    }


    /**
     * 导出民宿订单列表
     */
    @SaCheckPermission("system:order:export")
    @Log(title = "民宿订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BusOrderBo bo, HttpServletResponse response) {
        List<BusOrderVo> list = busOrderService.queryList(bo);
        ExcelUtil.exportExcel(list, "民宿订单", BusOrderVo.class, response);
    }

    /**
     * 获取民宿订单详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:order:query")
    @GetMapping("/{id}")
    public R<BusOrderVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(busOrderService.queryById(id));
    }

    /**
     * 新增民宿订单
     */
    @SaCheckPermission("system:order:add")
    @Log(title = "民宿订单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BusOrderBo bo) {
        return toAjax(busOrderService.insertByBo(bo));
    }

    /**
     * 修改民宿订单
     */
    @SaCheckPermission("system:order:edit")
    @Log(title = "民宿订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BusOrderBo bo) {
        return toAjax(busOrderService.updateByBo(bo));
    }

    /**
     * 删除民宿订单
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:order:remove")
    @Log(title = "民宿订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(busOrderService.deleteWithValidByIds(List.of(ids), true));
    }
}
