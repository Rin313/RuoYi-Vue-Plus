package org.dromara.system.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BizType;
import org.dromara.system.domain.vo.ActionVo;
import org.dromara.system.domain.bo.ActionInsertBo;
import org.dromara.system.domain.bo.ActionQueryBo;
import org.dromara.system.domain.bo.ActionUpdateBo;
import org.dromara.system.service.ActionService;
import org.dromara.common.mybatis.core.domain.PageQuery;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/action")
public class ActionController {

    private final ActionService actionService;

    @SaIgnore
    @GetMapping("/list")
    public IPage<ActionVo> list(ActionQueryBo bo, PageQuery pageQuery) {
        return actionService.selectPage(bo, pageQuery);
    }

    @SaCheckPermission("system:chapter:add")
    @Log(businessType = BizType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public void add(@Validated(AddGroup.class) @RequestBody ActionInsertBo bo) {
        actionService.insertByBo(bo);
    }

    @SaCheckPermission("system:chapter:edit")
    @Log(businessType = BizType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public void update(@Validated(EditGroup.class) @RequestBody ActionUpdateBo bo) {
        actionService.updateByBo(bo);
    }

    @SaCheckPermission("system:chapter:remove")
    @Log(businessType = BizType.DELETE)
    @DeleteMapping("/{ids}")
    public void delete(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        actionService.deleteByIds(List.of(ids));
    }
}
