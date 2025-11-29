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
import org.dromara.system.domain.vo.BusFavoriteVo;
import org.dromara.system.domain.bo.BusFavoriteBo;
import org.dromara.system.service.IBusFavoriteService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 收藏
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/favorite")
public class BusFavoriteController extends BaseController {

    private final IBusFavoriteService busFavoriteService;

    /**
     * 查询收藏列表
     */
    @SaCheckPermission("system:favorite:list")
    @GetMapping("/list")
    public TableDataInfo<BusFavoriteVo> list(BusFavoriteBo bo, PageQuery pageQuery) {
        return busFavoriteService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出收藏列表
     */
    @SaCheckPermission("system:favorite:export")
    @Log(title = "收藏", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BusFavoriteBo bo, HttpServletResponse response) {
        List<BusFavoriteVo> list = busFavoriteService.queryList(bo);
        ExcelUtil.exportExcel(list, "收藏", BusFavoriteVo.class, response);
    }

    /**
     * 获取收藏详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:favorite:query")
    @GetMapping("/{id}")
    public R<BusFavoriteVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(busFavoriteService.queryById(id));
    }

    /**
     * 新增收藏
     */
    @SaCheckPermission("system:favorite:add")
    @Log(title = "收藏", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BusFavoriteBo bo) {
        return toAjax(busFavoriteService.insertByBo(bo));
    }

    /**
     * 修改收藏
     */
    @SaCheckPermission("system:favorite:edit")
    @Log(title = "收藏", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BusFavoriteBo bo) {
        return toAjax(busFavoriteService.updateByBo(bo));
    }

    /**
     * 删除收藏
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:favorite:remove")
    @Log(title = "收藏", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(busFavoriteService.deleteWithValidByIds(List.of(ids), true));
    }
}
