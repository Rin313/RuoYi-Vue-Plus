package org.dromara.system.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BizType;
import org.dromara.system.domain.vo.NovelVisitorVo;
import org.dromara.system.domain.vo.NovelVo;
import org.dromara.system.service.NovelService;
import org.springdoc.core.annotations.ParameterObject;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/novel")
public class NovelController {
    private final NovelService novelService;
    @SaCheckPermission("novel:add")
    @Log(title = "小说", businessType = BizType.INSERT)
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importNovel(@RequestPart(required=false) MultipartFile img,
                             @Validated @ParameterObject @ModelAttribute NovelService.NovelInsertBo tNovelSubmitBo) {
        novelService.importNovel(img, tNovelSubmitBo);
    }
    @SaIgnore
    @RateLimiter(time = 300, count = 1, limitType = LimitType.IP)
    @PostMapping("/view/{id}")
    public void addViewCount(@PathVariable Long id) {
        novelService.addViewCount(id);
    }

    @SaCheckPermission("novel:list")
    @GetMapping("/list")
    public IPage<NovelVo> list(NovelService.NovelQueryBo bo, PageQuery pageQuery) {
        return novelService.selectPage(bo, pageQuery);
    }
    @SaIgnore
    @GetMapping("/list/visitor")
    public IPage<NovelVisitorVo> listForVisitor(NovelService.NovelQueryBo bo, PageQuery pageQuery) {
        return novelService.selectPageForVisitor(bo, pageQuery);
    }

    @SaCheckPermission("novel:query")
    @GetMapping("/{id}")
    public NovelVo getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return novelService.selectById(id);
    }
    @SaCheckPermission("novel:edit")
    @Log(title = "小说", businessType = BizType.UPDATE)
    @PostMapping("update")
    public void update(@Validated @RequestBody NovelService.NovelUpdateBo bo) {
        novelService.updateByBo(bo);
    }
    @SaCheckPermission("novel:remove")
    @Log(title = "小说", businessType = BizType.DELETE)
    @PostMapping("/{ids}")
    public void delete(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        novelService.deleteByIds(List.of(ids));
    }
}
