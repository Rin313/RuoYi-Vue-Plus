package org.dromara.system.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.system.domain.vo.ChapterListVo;
import org.dromara.system.domain.vo.ChapterVo;
import org.dromara.system.domain.bo.ChapterInsertBo;
import org.dromara.system.domain.bo.ChapterQueryBo;
import org.dromara.system.domain.bo.ChapterUpdateBo;
import org.dromara.system.service.ChapterService;
import org.dromara.common.mybatis.core.domain.PageQuery;

/**
 * 小说章节
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/chapter")
public class ChapterController {

    private final ChapterService chapterService;

    /**
     * 查询小说章节列表
     */
    @SaCheckPermission("system:chapter:list")
    @GetMapping("/list")
    public IPage<ChapterListVo> list(ChapterQueryBo bo, PageQuery pageQuery) {
        return chapterService.queryPageList(bo, pageQuery);
    }

    // /**
    //  * 导出小说章节列表
    //  */
    // @SaCheckPermission("system:chapter:export")
    // @Log(title = "小说章节", businessType = BusinessType.EXPORT)
    // @PostMapping("/export")
    // public void export(TChapterBo bo, HttpServletResponse response) {
    //     List<TChapterListVo> list = chapterService.queryList(bo);
    //     ExcelUtil.exportExcel(list, "小说章节", TChapterListVo.class, response);
    // }

    /**
     * 获取小说章节详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:chapter:query")
    @GetMapping("/{id}")
    public ChapterVo getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return chapterService.queryById(id);
    }

    /**
     * 新增小说章节
     */
    @SaCheckPermission("system:chapter:add")
    @Log(title = "小说章节", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public void add(@Validated(AddGroup.class) @RequestBody ChapterInsertBo bo) {
        chapterService.insertByBo(bo);
    }

    /**
     * 修改小说章节
     */
    @SaCheckPermission("system:chapter:edit")
    @Log(title = "小说章节", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public void edit(@Validated(EditGroup.class) @RequestBody ChapterUpdateBo bo) {
        chapterService.updateByBo(bo);
    }

    /**
     * 删除小说章节
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:chapter:remove")
    @Log(title = "小说章节", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public void remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        chapterService.deleteWithValidByIds(List.of(ids));
    }
}
