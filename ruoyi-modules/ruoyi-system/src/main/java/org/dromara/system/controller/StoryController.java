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
import org.dromara.system.domain.Story;
import org.dromara.system.service.StoryService;
import org.dromara.system.service.StoryService.StoryInsertBo;
import org.dromara.system.service.StoryService.StoryQueryBo;
import org.dromara.system.service.StoryService.StoryUpdateBo;
import org.dromara.common.mybatis.core.domain.PageQuery;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/story")
public class StoryController {
    private final StoryService storyService;
    @SaCheckPermission("system:novel:list")
    @GetMapping("/list")
    public IPage<Story> list(StoryQueryBo bo, PageQuery pageQuery) {
        return storyService.selectPage(bo, pageQuery);
    }
    @SaCheckPermission("system:novel:list")
    @GetMapping("/{id}")
    public Story getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return storyService.getById(id);
    }
    @SaCheckPermission("system:story:add")
    @Log(title = "故事", businessType = BizType.INSERT)
    @PostMapping("/insert")
    public Long insert(@Validated @RequestBody StoryInsertBo bo) {
        return storyService.insert(bo);
    }
    @SaCheckPermission("system:novel:edit")
    @Log(title = "小说", businessType = BizType.UPDATE)
    @PostMapping("update")
    public void update(@Validated @RequestBody StoryUpdateBo bo) {
        storyService.updateByBo(bo);
    }
    @SaCheckPermission("system:novel:remove")
    @Log(title = "小说", businessType = BizType.DELETE)
    @PostMapping("/{ids}")
    public void delete(@NotEmpty(message = "主键不能为空")
                          @PathVariable List<Long> ids) {
        storyService.deleteByIds(ids);
    }
}