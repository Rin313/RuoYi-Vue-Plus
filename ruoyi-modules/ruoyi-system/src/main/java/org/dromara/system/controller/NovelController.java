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
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BizType;
import org.dromara.system.domain.vo.NovelVo;
import org.dromara.system.domain.bo.NovelInsertBo;
import org.dromara.system.domain.bo.NovelQueryBo;
import org.dromara.system.domain.bo.NovelUpdateBo;
import org.dromara.system.service.NovelService;
import org.springdoc.core.annotations.ParameterObject;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;

/**
 * 小说
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/novel")
public class NovelController {

    private final NovelService novelService;
    
    /**
     * 导入TXT小说
     *
     * @param file TXT文件
     * @param tNovelSubmitBo 附加信息
     */
    @SaCheckPermission("system:novel:add")
    @Log(title = "小说", businessType = BizType.INSERT)
    @RepeatSubmit()
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importTxt(@RequestPart(required=false) MultipartFile file,
                             @Validated(AddGroup.class) @ParameterObject @ModelAttribute NovelInsertBo tNovelSubmitBo) {
        novelService.importTxtNovel(file, tNovelSubmitBo);
    }
    /**
     * 增加小说浏览量
     * @param id 小说ID
     */
    @SaIgnore
    @RateLimiter(time = 300, count = 1, limitType = LimitType.IP)
    @PostMapping("/view/{id}")
    public void addViewCount(@PathVariable("id") Long id) {
        novelService.addViewCount(id);
    }

    /**
     * 查询小说列表
     */
    @SaCheckPermission("system:novel:list")
    @GetMapping("/list")
    public IPage<NovelVo> list(NovelQueryBo bo, PageQuery pageQuery) {
        return novelService.queryPageList(bo, pageQuery);
    }

    // /**
    //  * 导出小说列表
    //  */
    // @SaCheckPermission("system:novel:export")
    // @Log(title = "小说", businessType = BusinessType.EXPORT)
    // @PostMapping("/export")
    // public void export(TNovelBo bo, HttpServletResponse response) {
    //     List<TNovelVo> list = tNovelService.queryList(bo);
    //     ExcelUtil.exportExcel(list, "小说", TNovelVo.class, response);
    // }

    /**
     * 获取小说详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:novel:query")
    @GetMapping("/{id}")
    public NovelVo getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return novelService.queryById(id);
    }

    /**
     * 修改小说
     */
    @SaCheckPermission("system:novel:edit")
    @Log(title = "小说", businessType = BizType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public void edit(@Validated(EditGroup.class) @RequestBody NovelUpdateBo bo) {
        novelService.updateByBo(bo);
    }

    /**
     * 删除小说
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:novel:remove")
    @Log(title = "小说", businessType = BizType.DELETE)
    @DeleteMapping("/{ids}")
    public void remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        novelService.deleteWithValidByIds(List.of(ids));
    }
}
