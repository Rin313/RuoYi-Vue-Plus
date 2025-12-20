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
import org.dromara.system.domain.Novel;
import org.dromara.system.service.NovelService;
import org.dromara.system.service.NovelService.NovelInsertBo;
import org.dromara.system.service.NovelService.NovelQueryBo;
import org.dromara.system.service.NovelService.NovelUpdateBo;
import org.dromara.system.service.NovelService.NovelVo;
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
    @SaCheckPermission("system:novel:add")
    @Log(title = "小说", businessType = BizType.INSERT)
    @PostMapping(value = "/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void insert(@RequestPart(required=false) MultipartFile img,
                             @Validated @ParameterObject @ModelAttribute NovelInsertBo tNovelSubmitBo) {
        novelService.importNovel(img, tNovelSubmitBo);
    }
    @SaCheckPermission("system:novel:edit")
    @Log(title = "小说", businessType = BizType.UPDATE)
    @PostMapping(value = "/update",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void update(@RequestPart(required=false) MultipartFile img,@Validated @ParameterObject @ModelAttribute NovelUpdateBo bo) {
        novelService.update(img,bo);
    }
    @SaCheckPermission("system:novel:list")
    @GetMapping("/list")
    public IPage<NovelVo> list(NovelQueryBo bo, PageQuery pageQuery) {
        return novelService.selectPage(bo, pageQuery);
    }
    @SaCheckPermission("system:novel:remove")
    @Log(title = "小说", businessType = BizType.DELETE)
    @PostMapping("/{ids}")
    public void delete(@NotEmpty(message = "主键不能为空")
                          @PathVariable List<Long> ids) {
        novelService.deleteByIds(ids);
    }
    @SaCheckPermission("system:novel:list")
    @GetMapping("/{id}")
    public Novel getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return novelService.getById(id);
    }
    @SaIgnore
    @RateLimiter(time = 300, count = 1, limitType = LimitType.IP)
    @PostMapping("/view/{id}")
    public void addViewCount(@PathVariable Long id) {
        novelService.addViewCount(id);
    }
    @SaIgnore
    @GetMapping("/list/visitor")
    public IPage<NovelVo> listForVisitor(NovelQueryBo bo, PageQuery pageQuery) {
        return novelService.selectPageForVisitor(bo, pageQuery);
    }
}