package org.dromara.system.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.springframework.validation.annotation.Validated;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BizType;
import org.dromara.system.domain.vo.ChapterListVo;
import org.dromara.system.domain.vo.ChapterVo;
import org.dromara.system.domain.bo.ChapterInsertBo;
import org.dromara.system.domain.bo.ChapterQueryBo;
import org.dromara.system.domain.bo.ChapterUpdateBo;
import org.dromara.system.service.ChapterService;
import org.dromara.common.mybatis.core.domain.PageQuery;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/chapter")
public class ChapterController {

    private final ChapterService chapterService;

    @SaCheckPermission("system:chapter:list")
    @GetMapping("/list")
    public IPage<ChapterListVo> list(ChapterQueryBo bo, PageQuery pageQuery) {
        return chapterService.selectPage(bo, pageQuery);
    }

    @SaCheckPermission("system:chapter:query")
    @GetMapping("/{id}")
    public ChapterVo getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return chapterService.selectById(id);
    }

    @SaCheckPermission("system:chapter:add")
    @Log(title = "小说章节", businessType = BizType.INSERT)
    @PostMapping("/insert")
    public void add(@Validated @RequestBody ChapterInsertBo bo) {
        chapterService.insertByBo(bo);
    }

    @SaCheckPermission("system:chapter:edit")
    @Log(title = "小说章节", businessType = BizType.UPDATE)
    @PostMapping("/update")
    public void update(@Validated @RequestBody ChapterUpdateBo bo) {
        chapterService.updateByBo(bo);
    }

    @SaCheckPermission("system:chapter:remove")
    @Log(title = "小说章节", businessType = BizType.DELETE)
    @PostMapping("/{ids}")
    public void delete(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        chapterService.deleteByIds(List.of(ids));
    }
}
