package org.dromara.system.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.system.domain.vo.TNovelVo;
import org.dromara.system.domain.bo.TNovelBo;
import org.dromara.system.domain.bo.TNovelSubmitBo;
import org.dromara.system.service.ITNovelService;
import org.springdoc.core.annotations.ParameterObject;
import org.dromara.common.mybatis.core.page.TableDataInfo;
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
public class TNovelController extends BaseController {

    private final ITNovelService tNovelService;
    
    /**
     * 导入TXT小说
     *
     * @param file TXT文件
     * @param tNovelSubmitBo 附加信息（可选）
     * @return 结果
     */
    @SaCheckPermission("system:novel:add")
    @Log(title = "小说", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void importTxt(@RequestPart(required=false) MultipartFile file,
                             @Validated(AddGroup.class) @ParameterObject @ModelAttribute TNovelSubmitBo tNovelSubmitBo) {
        tNovelService.importTxtNovel(file, tNovelSubmitBo);
    }
    /**
     * 增加小说浏览量
     * 
     * @param id 小说ID
     */
    @SaIgnore
    @RateLimiter(time = 300, count = 1, limitType = LimitType.IP)
    @PostMapping("/view/{id}")
    public void addViewCount(@PathVariable("id") Long id) {
        boolean result = tNovelService.addViewCount(id);
        if(!result)throw new BizException();
    }

    /**
     * 查询小说列表
     */
    @SaCheckPermission("system:novel:list")
    @GetMapping("/list")
    public TableDataInfo<TNovelVo> list(TNovelBo bo, PageQuery pageQuery) {
        return tNovelService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出小说列表
     */
    @SaCheckPermission("system:novel:export")
    @Log(title = "小说", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(TNovelBo bo, HttpServletResponse response) {
        List<TNovelVo> list = tNovelService.queryList(bo);
        ExcelUtil.exportExcel(list, "小说", TNovelVo.class, response);
    }

    /**
     * 获取小说详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:novel:query")
    @GetMapping("/{id}")
    public TNovelVo getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return tNovelService.queryById(id);
    }

    // /**
    //  * 新增小说
    //  */
    // @SaCheckPermission("system:novel:add")
    // @Log(title = "小说", businessType = BusinessType.INSERT)
    // @RepeatSubmit()
    // @PostMapping()
    // public R<Void> add(@Validated(AddGroup.class) @RequestBody TNovelBo bo) {
    //     return toAjax(tNovelService.insertByBo(bo));
    // }

    /**
     * 修改小说
     */
    @SaCheckPermission("system:novel:edit")
    @Log(title = "小说", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public void edit(@Validated(EditGroup.class) @RequestBody TNovelBo bo) {
        toAjax(tNovelService.updateByBo(bo));
    }

    /**
     * 删除小说
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:novel:remove")
    @Log(title = "小说", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public void remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        toAjax(tNovelService.deleteWithValidByIds(List.of(ids), true));
    }
}
