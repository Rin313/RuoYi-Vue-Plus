package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BizType;
import org.dromara.common.mybatis.core.domain.PageQuery;

import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysRoleBo;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.SysRoleService;
import org.dromara.system.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 角色信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    private final SysRoleService roleService;
    private final SysUserService userService;

    /**
     * 获取角色信息列表
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public IPage<SysRoleVo> list(SysRoleBo role, PageQuery pageQuery) {
        return roleService.selectPageRoleList(role, pageQuery);
    }

    /**
     * 导出角色信息列表
     */
    @Log(title = "角色管理", businessType = BizType.EXPORT)
    @SaCheckPermission("system:role:export")
    @PostMapping("/export")
    public void export(SysRoleBo role, HttpServletResponse response) {
        List<SysRoleVo> list = roleService.selectRoleList(role);
        ExcelUtil.exportExcel(list, "角色数据", SysRoleVo.class, response);
    }

    /**
     * 根据角色编号获取详细信息
     *
     * @param roleId 角色ID
     */
    @SaCheckPermission("system:role:query")
    @GetMapping(value = "/{roleId}")
    public SysRoleVo getInfo(@PathVariable Long roleId) {
        return roleService.selectRoleById(roleId);
    }

    /**
     * 新增角色
     */
    @SaCheckPermission("system:role:add")
    @Log(title = "角色管理", businessType = BizType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public void add(@Validated @RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        if (!roleService.checkRoleNameUnique(role)) {
            throw new BizException("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            throw new BizException("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        roleService.insertRole(role);

    }

    /**
     * 修改保存角色
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BizType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public void update(@Validated @RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        if (!roleService.checkRoleNameUnique(role)) {
            throw new BizException("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            throw new BizException("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        if (roleService.updateRole(role) > 0) {
            roleService.cleanOnlineUserByRole(role.getRoleId());
        }
        throw new BizException("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BizType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/changeStatus")
    public void changeStatus(@RequestBody SysRoleBo role) {
        roleService.checkRoleAllowed(role);
        roleService.updateRoleStatus(role.getRoleId(), role.getStatus());
    }

    /**
     * 删除角色
     *
     * @param roleIds 角色ID串
     */
    @SaCheckPermission("system:role:remove")
    @Log(title = "角色管理", businessType = BizType.DELETE)
    @DeleteMapping("/{roleIds}")
    public void delete(@PathVariable Long[] roleIds) {
        roleService.deleteRoleByIds(List.of(roleIds));
    }

    /**
     * 获取角色选择框列表
     *
     * @param roleIds 角色ID串
     */
    @SaCheckPermission("system:role:query")
    @GetMapping("/optionselect")
    public List<SysRoleVo> optionselect(@RequestParam(required = false) Long[] roleIds) {
        return roleService.selectRoleByIds(roleIds == null ? null : List.of(roleIds));
    }

    /**
     * 查询已分配用户角色列表
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/authUser/allocatedList")
    public IPage<SysUserVo> allocatedList(SysUserBo user, PageQuery pageQuery) {
        return userService.selectAllocatedList(user, pageQuery);
    }

    /**
     * 查询未分配用户角色列表
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/authUser/unallocatedList")
    public IPage<SysUserVo> unallocatedList(SysUserBo user, PageQuery pageQuery) {
        return userService.selectUnallocatedList(user, pageQuery);
    }

    /**
     * 取消授权用户
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BizType.GRANT)
    @RepeatSubmit()
    @PutMapping("/authUser/cancel")
    public void cancelAuthUser(@RequestBody SysUserRole userRole) {
        roleService.deleteAuthUser(userRole);
    }

    /**
     * 批量取消授权用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID串
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BizType.GRANT)
    @RepeatSubmit()
    @PutMapping("/authUser/cancelAll")
    public void cancelAuthUserAll(Long roleId, Long[] userIds) {
        roleService.deleteAuthUsers(roleId, userIds);
    }

    /**
     * 批量选择用户授权
     *
     * @param roleId  角色ID
     * @param userIds 用户ID串
     */
    @SaCheckPermission("system:role:edit")
    @Log(title = "角色管理", businessType = BizType.GRANT)
    @RepeatSubmit()
    @PutMapping("/authUser/selectAll")
    public void selectAuthUserAll(Long roleId, Long[] userIds) {
        roleService.insertAuthUsers(roleId, userIds);
    }

}
