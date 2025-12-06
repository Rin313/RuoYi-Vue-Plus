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
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.system.domain.vo.TChapterListVo;
import org.dromara.system.domain.vo.TChapterVo;
import org.dromara.system.domain.bo.TChapterBo;
import org.dromara.system.service.TChapterService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 小说章节宽
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/chapter")
public class TChapterController extends BaseController {

    private final TChapterService tChapterService;

    /**
     * 查询小说章节列表
     */
    @SaCheckPermission("system:chapter:list")
    @GetMapping("/list")
    public TableDataInfo<TChapterListVo> list(TChapterBo bo, PageQuery pageQuery) {
        return tChapterService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出小说章节列表
     */
    @SaCheckPermission("system:chapter:export")
    @Log(title = "小说章节宽", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(TChapterBo bo, HttpServletResponse response) {
        List<TChapterListVo> list = tChapterService.queryList(bo);
        ExcelUtil.exportExcel(list, "小说章节宽", TChapterListVo.class, response);
    }

    /**
     * 获取小说章节详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:chapter:query")
    @GetMapping("/{id}")
    public TChapterVo getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return tChapterService.queryById(id);
    }

    /**
     * 新增小说章节
     */
    @SaCheckPermission("system:chapter:add")
    @Log(title = "小说章节宽", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public void add(@Validated(AddGroup.class) @RequestBody TChapterBo bo) {
        toAjax(tChapterService.insertByBo(bo));
    }

    /**
     * 修改小说章节
     */
    @SaCheckPermission("system:chapter:edit")
    @Log(title = "小说章节宽", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public void edit(@Validated(EditGroup.class) @RequestBody TChapterBo bo) {
        toAjax(tChapterService.updateByBo(bo));
    }

    /**
     * 删除小说章节
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:chapter:remove")
    @Log(title = "小说章节宽", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public void remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        toAjax(tChapterService.deleteWithValidByIds(List.of(ids), true));
    }
}
