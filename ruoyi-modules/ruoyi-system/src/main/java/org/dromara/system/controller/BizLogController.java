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
import org.dromara.system.domain.vo.BizLogVo;
import org.dromara.system.domain.bo.BizLogQueryBo;
import org.dromara.system.service.BizLogService;
import org.dromara.common.mybatis.core.domain.PageQuery;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/bizLog")
public class BizLogController {

    private final BizLogService BizLogService;

    @SaCheckPermission("system:bizLog:list")
    @GetMapping("/list")
    public IPage<BizLogVo> list(BizLogQueryBo bo, PageQuery pageQuery) {
        return null;
        //return BizLogService.selectPage(bo, pageQuery);
    }
    // @SaCheckPermission("system:chapter:add")
    // @Log(title = "小说章节", businessType = BizType.INSERT)
    // @RepeatSubmit()
    // @PostMapping()
    // public void add(@Validated(AddGroup.class) @RequestBody ChapterInsertBo bo) {
    //     chapterService.insertByBo(bo);
    // }
}
