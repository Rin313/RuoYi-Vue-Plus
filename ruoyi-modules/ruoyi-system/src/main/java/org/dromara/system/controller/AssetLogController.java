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
import org.dromara.common.log.enums.BizType;
import org.dromara.system.domain.vo.AssetLogVo;
import org.dromara.system.domain.bo.AssetLogQueryBo;
import org.dromara.system.service.AssetLogService;
import org.dromara.common.mybatis.core.domain.PageQuery;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/assetlog")
public class AssetLogController {

    private final AssetLogService assetLogService;

    @SaCheckPermission("system:assetlog:list")
    @GetMapping("/list")
    public IPage<AssetLogVo> list(AssetLogQueryBo bo, PageQuery pageQuery) {
        return assetLogService.selectPage(bo, pageQuery);
    }
    //现在我需要什么数据表？商品表？业务表？如何将流水变化应用到项目的各个模块
    // @SaCheckPermission("system:chapter:add")
    // @Log(title = "小说章节", businessType = BizType.INSERT)
    // @RepeatSubmit()
    // @PostMapping()
    // public void add(@Validated(AddGroup.class) @RequestBody ChapterInsertBo bo) {
    //     chapterService.insertByBo(bo);
    // }
}
