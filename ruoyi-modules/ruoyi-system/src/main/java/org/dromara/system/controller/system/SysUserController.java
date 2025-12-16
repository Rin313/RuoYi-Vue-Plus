package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.excel.core.ExcelResult;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BizType;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;

import org.dromara.system.domain.bo.SysRoleBo;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.*;
import org.dromara.system.listener.SysUserImportListener;
import org.dromara.system.service.SysRoleService;
import org.dromara.system.service.SysUserService;
import org.dromara.system.service.SysUserService.TaskVo;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



/**
 * 用户信息
 *
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    
    @GetMapping("task/info")
    public List<TaskVo> getTaskInfo() {
        return userService.getTaskListView(LoginHelper.getUserId());
    }

    @PostMapping("task/claim")
    public void postMethodName(String taskKey) {
        userService.claimReward(LoginHelper.getUserId(), taskKey);
    }
    

     /**
     * 获取签到首页信息
     */
    @GetMapping("/sign/info")
    public SignInViewVo getSignInfo() {
        return userService.getSignInView(LoginHelper.getUserId());
    }

    /**
     * 执行签到
     */
    @PostMapping("/sign/do")
    public void doSign() {
        userService.doSign(LoginHelper.getUserId());
    }

    /**
     * 补签
     * @param date yyyy-MM-dd
     */
    @PostMapping("/sign/retro")
    public void retroSign(String date) {
        userService.doRetroSign(LoginHelper.getUserId(), date);
    }

    /**
     * 领取连签奖励
     * @param days 配置表里的key，如 7
     */
    @PostMapping("/sign/streak")
    public void claimStreak(Integer days) {
        userService.claimStreakReward(LoginHelper.getUserId(), days);
    }
    /**
     * 记录用户分享
     * @return 
     * 
     */
    @PostMapping("/share")
    public Map<String,Integer> share() {
        return userService.share();
    }
    /**
     * 获取用户列表
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public IPage<SysUserVo> list(SysUserBo user, PageQuery pageQuery) {
        return userService.selectPageUserList(user, pageQuery);
    }

    /**
     * 导出用户列表
     */
    @Log(title = "用户管理", businessType = BizType.EXPORT)
    @SaCheckPermission("system:user:export")
    @PostMapping("/export")
    public void export(SysUserBo user, HttpServletResponse response) {
        List<SysUserExportVo> list = userService.selectUserExportList(user);
        ExcelUtil.exportExcel(list, "用户数据", SysUserExportVo.class, response);
    }

    /**
     * 导入数据
     *
     * @param file          导入文件
     * @param updateSupport 是否更新已存在数据
     */
    @Log(title = "用户管理", businessType = BizType.IMPORT)
    @SaCheckPermission("system:user:import")
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importData(@RequestPart("file") MultipartFile file, boolean updateSupport) throws Exception {
        ExcelResult<SysUserImportVo> result = ExcelUtil.importExcel(file.getInputStream(), SysUserImportVo.class, new SysUserImportListener(updateSupport));
        return result.getAnalysis();
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getInfo")
    public UserInfoVo getInfo() {
        UserInfoVo userInfoVo = new UserInfoVo();
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUserVo user = userService.selectUserById(loginUser.getUserId());
        if (ObjectUtil.isNull(user)) {
            throw new BizException("没有权限访问用户数据!");
        }
        userInfoVo.setUser(user);
        userInfoVo.setPermissions(loginUser.getMenuPermission());
        userInfoVo.setRoles(loginUser.getRolePermission());
        return userInfoVo;
    }

    /**
     * 根据用户编号获取详细信息
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    public SysUserInfoVo getInfo(@PathVariable(required = false) Long userId) {
        SysUserInfoVo userInfoVo = new SysUserInfoVo();
        if (ObjectUtil.isNotNull(userId)) {
            SysUserVo sysUser = userService.selectUserById(userId);
            userInfoVo.setUser(sysUser);
            userInfoVo.setRoleIds(roleService.selectRoleListByUserId(userId));
        }
        SysRoleBo roleBo = new SysRoleBo();
        roleBo.setStatus(SystemConstants.NORMAL);
        List<SysRoleVo> roles = roleService.selectRoleList(roleBo);
        userInfoVo.setRoles(LoginHelper.isSuperAdmin(userId) ? roles : StreamUtils.filter(roles, r -> !r.isSuperAdmin()));
        return userInfoVo;
    }

    /**
     * 新增用户
     */
    @SaCheckPermission("system:user:add")
    @Log(title = "用户管理", businessType = BizType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public void add(@Validated @RequestBody SysUserBo user) {
        if (!userService.checkUserNameUnique(user)) {
            throw new BizException("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            throw new BizException("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            throw new BizException("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        user.setInviteCode(userService.getUniqueInviteCode());
        userService.insertUser(user);
    }

    /**
     * 修改用户
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BizType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/update")
    public void update(@Validated @RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        if (!userService.checkUserNameUnique(user)) {
            throw new BizException("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            throw new BizException("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            throw new BizException("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        userService.updateUser(user);
    }
    /**
     * 删除用户
     *
     * @param userIds 角色ID串
     */
    @SaCheckPermission("system:user:remove")
    @Log(title = "用户管理", businessType = BizType.DELETE)
    @PostMapping("/{userIds}")
    public void delete(@PathVariable Long[] userIds) {
        if (ArrayUtil.contains(userIds, LoginHelper.getUserId())) {
            throw new BizException("当前用户不能删除");
        }
        userService.deleteUserByIds(userIds);
    }

    /**
     * 重置密码
     */
    @SaCheckPermission("system:user:resetPwd")
    @Log(title = "用户管理", businessType = BizType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/resetPwd")
    public void resetPwd(@RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        userService.resetUserPwd(user.getUserId(), user.getPassword());
    }

    /**
     * 根据用户编号获取授权角色
     *
     * @param userId 用户ID
     */
    @SaCheckPermission("system:user:query")
    @GetMapping("/authRole/{userId}")
    public SysUserInfoVo authRole(@PathVariable Long userId) {
        SysUserVo user = userService.selectUserById(userId);
        List<SysRoleVo> roles = roleService.selectRolesAuthByUserId(userId);
        SysUserInfoVo userInfoVo = new SysUserInfoVo();
        userInfoVo.setUser(user);
        userInfoVo.setRoles(LoginHelper.isSuperAdmin(userId) ? roles : StreamUtils.filter(roles, r -> !r.isSuperAdmin()));
        return userInfoVo;
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户Id
     * @param roleIds 角色ID串
     */
    @SaCheckPermission("system:user:edit")
    @Log(title = "用户管理", businessType = BizType.GRANT)
    @RepeatSubmit()
    @PostMapping("/authRole")
    public void insertAuthRole(Long userId, List<Long> roleIds) {
        userService.insertUserAuth(userId, roleIds);
    }
}
