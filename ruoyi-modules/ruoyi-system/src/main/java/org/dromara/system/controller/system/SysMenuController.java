package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.lang.tree.Tree;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BizType;
import org.dromara.common.satoken.utils.LoginHelper;

import org.dromara.system.domain.SysMenu;
import org.dromara.system.domain.bo.SysMenuBo;
import org.dromara.system.domain.vo.RouterVo;
import org.dromara.system.domain.vo.SysMenuVo;
import org.dromara.system.service.SysMenuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单信息
 *
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("/getRouters")
    public List<RouterVo> getRouters() {
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(LoginHelper.getUserId());
        return menuService.buildMenus(menus);
    }

    /**
     * 获取菜单列表
     */
    @SaCheckRole(value = {SystemConstants.SUPER_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public List<SysMenuVo> list(SysMenuBo menu) {
        List<SysMenuVo> menus = menuService.selectMenuList(menu, LoginHelper.getUserId());
        return menus;
    }

    /**
     * 根据菜单编号获取详细信息
     *
     * @param menuId 菜单ID
     */
    @SaCheckRole(value = {SystemConstants.SUPER_ADMIN_ROLE_KEY}, mode = SaMode.OR)
    @SaCheckPermission("system:menu:query")
    @GetMapping(value = "/{menuId}")
    public SysMenuVo getInfo(@PathVariable Long menuId) {
        return menuService.selectMenuById(menuId);
    }

    /**
     * 获取菜单下拉树列表
     */
    @SaCheckPermission("system:menu:query")
    @GetMapping("/treeselect")
    public List<Tree<Long>> treeselect(SysMenuBo menu) {
        List<SysMenuVo> menus = menuService.selectMenuList(menu, LoginHelper.getUserId());
        return menuService.buildMenuTreeSelect(menus);
    }

    /**
     * 加载对应角色菜单列表树
     *
     * @param roleId 角色ID
     */
    @SaCheckPermission("system:menu:query")
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public MenuTreeSelectVo roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        List<SysMenuVo> menus = menuService.selectMenuList(LoginHelper.getUserId());
        MenuTreeSelectVo selectVo = new MenuTreeSelectVo(
            menuService.selectMenuListByRoleId(roleId),
            menuService.buildMenuTreeSelect(menus));
        return selectVo;
    }

    /**
     * 新增菜单
     */
    @SaCheckRole(SystemConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:add")
    @Log(title = "菜单管理", businessType = BizType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public void add(@Validated @RequestBody SysMenuBo menu) {
        // if (!menuService.checkMenuNameUnique(menu)) {
        //     return R.fail("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        // } else 
            if (SystemConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            throw new BizException("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        menuService.insertMenu(menu);
    }

    /**
     * 修改菜单
     */
    @SaCheckRole(SystemConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:edit")
    @Log(title = "菜单管理", businessType = BizType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/update")
    public void update(@Validated @RequestBody SysMenuBo menu) {
        // if (!menuService.checkMenuNameUnique(menu)) {
        //     return R.fail("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        // } else 
        if (SystemConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            throw new BizException("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            throw new BizException("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        menuService.updateMenu(menu);
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     */
    @SaCheckRole(SystemConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:remove")
    @Log(title = "菜单管理", businessType = BizType.DELETE)
    @PostMapping("/{menuId}")
    public void delete(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            throw new BizException("存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            throw new BizException("菜单已分配,不允许删除");
        }
        menuService.deleteMenuById(menuId);
    }

    /**
     * 角色菜单列表树信息
     *
     * @param checkedKeys 选中菜单列表
     * @param menus       菜单下拉树结构列表
     */
    public record MenuTreeSelectVo(List<Long> checkedKeys, List<Tree<Long>> menus) {
    }

    /**
     * 批量级联删除菜单
     *
     * @param menuIds 菜单ID串
     */
    @SaCheckRole(SystemConstants.SUPER_ADMIN_ROLE_KEY)
    @SaCheckPermission("system:menu:remove")
    @Log(title = "菜单管理", businessType = BizType.DELETE)
    @PostMapping("/cascade/{menuIds}")
    public void delete(@PathVariable("menuIds") Long[] menuIds) {
        List<Long> menuIdList = List.of(menuIds);
        if (menuService.hasChildByMenuId(menuIdList)) {
            throw new BizException("存在子菜单,不允许删除");
        }
        menuService.deleteMenuById(menuIdList);
    }

}
