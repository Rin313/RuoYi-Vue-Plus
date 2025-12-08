package org.dromara.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.dto.UserDTO;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.utils.*;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.BizLog;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SignInViewVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserExportVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserService {

    private final SysUserMapper baseMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final BizLogService bizLogService;
    private final BizLogMapper bizLogMapper;
    private final SysConfigService configService;

    public IPage<SysUserVo> selectPageUserList(SysUserBo user, PageQuery pageQuery) {
        return baseMapper.selectPageUserList(pageQuery.build(), this.buildQueryWrapper(user));
    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUserExportVo> selectUserExportList(SysUserBo user) {
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getNickName()), "u.nick_name", user.getNickName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .ge(ObjectUtils.isNotEmpty(user.getBeginTime()), "u.create_time",user.getBeginTime())
            .le(ObjectUtils.isNotEmpty(user.getEndTime()), "u.create_time",user.getEndTime())
            .orderByAsc("u.user_id");
        return baseMapper.selectUserExportList(wrapper);
    }

    private Wrapper<SysUser> buildQueryWrapper(SysUserBo user) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getDelFlag, SystemConstants.NORMAL)
            .eq(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId())
            .in(StringUtils.isNotBlank(user.getUserIds()), SysUser::getUserId, StringUtils.splitTo(user.getUserIds(), Convert::toLong))
            .like(StringUtils.isNotBlank(user.getUserName()), SysUser::getUserName, user.getUserName())
            .like(StringUtils.isNotBlank(user.getNickName()), SysUser::getNickName, user.getNickName())
            .eq(StringUtils.isNotBlank(user.getStatus()), SysUser::getStatus, user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), SysUser::getPhonenumber, user.getPhonenumber())
            .ge(ObjectUtils.isNotEmpty(user.getBeginTime()), SysUser::getCreateTime,user.getBeginTime())
            .le(ObjectUtils.isNotEmpty(user.getEndTime()), SysUser::getCreateTime,user.getEndTime())
            .orderByAsc(SysUser::getUserId);
        return wrapper;
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public IPage<SysUserVo> selectAllocatedList(SysUserBo user, PageQuery pageQuery) {
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .eq(ObjectUtil.isNotNull(user.getRoleId()), "r.role_id", user.getRoleId())
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .orderByAsc("u.user_id");
        return baseMapper.selectAllocatedList(pageQuery.build(), wrapper);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public IPage<SysUserVo> selectUnallocatedList(SysUserBo user, PageQuery pageQuery) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(user.getRoleId());
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .and(w -> w.ne("r.role_id", user.getRoleId()).or().isNull("r.role_id"))
            .notIn(CollUtil.isNotEmpty(userIds), "u.user_id", userIds)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .orderByAsc("u.user_id");
        return baseMapper.selectUnallocatedList(pageQuery.build(), wrapper);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    public SysUserVo selectUserByUserName(String userName) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, userName));
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    public SysUserVo selectUserById(Long userId) {
        SysUserVo user = baseMapper.selectVoById(userId);
        if (ObjectUtil.isNull(user)) {
            return user;
        }
        user.setRoles(roleMapper.selectRolesByUserId(user.getUserId()));
        if(user.getSignRecord()==null)user.setSignRecord(new ArrayList<String>());
        return user;
    }

    /**
     * 通过用户ID串查询用户
     *
     * @param userIds 用户ID串
     * @return 用户列表信息
     */
    public List<SysUserVo> selectUserByIds(List<Long> userIds) {
        return baseMapper.selectUserList(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserId, SysUser::getUserName, SysUser::getNickName)
            .eq(SysUser::getStatus, SystemConstants.NORMAL)
            .in(CollUtil.isNotEmpty(userIds), SysUser::getUserId, userIds));
    }
    /**
     * 通过邀请码查询用户
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    public SysUserVo selectUserByInviteCode(String inviteCode) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getInviteCode, inviteCode));
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    public String selectUserRoleGroup(Long userId) {
        List<SysRoleVo> list = roleMapper.selectRolesByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysRoleVo::getRoleName);
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean checkUserNameUnique(SysUserBo user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUserName, user.getUserName())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     */
    public boolean checkPhoneUnique(SysUserBo user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhonenumber, user.getPhonenumber())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     */
    public boolean checkEmailUnique(SysUserBo user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEmail, user.getEmail())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param userId 用户ID
     */
    public void checkUserAllowed(Long userId) {
        if (ObjectUtil.isNotNull(userId) && LoginHelper.isSuperAdmin(userId)) {
            throw new BizException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    public void checkUserDataScope(Long userId) {
        if (ObjectUtil.isNull(userId)) {
            return;
        }
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        if (baseMapper.countUserById(userId) == 0) {
            throw new BizException("没有权限访问用户数据！");
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertUser(SysUserBo user) {
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 新增用户信息
        baseMapper.insert(sysUser);
        user.setUserId(sysUser.getUserId());
        // 新增用户与角色管理
        insertUserRole(user, false);
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUserBo user) {
        // 新增用户与角色管理
        insertUserRole(user, true);
        SysUser sysUser = MapstructUtils.convert(user, SysUser.class);
        // 防止错误更新后导致的数据误删除
        int flag = baseMapper.updateById(sysUser);
        if (flag < 1) {
            throw new BizException("修改用户{}信息失败", user.getUserName());
        }
        return flag;
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        insertUserRole(userId, roleIds, true);
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 帐号状态
     * @return 结果
     */
    public int updateUserStatus(Long userId, String status) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getStatus, status)
                .eq(SysUser::getUserId, userId));
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    public int updateUserProfile(SysUserBo user) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(ObjectUtil.isNotNull(user.getNickName()), SysUser::getNickName, user.getNickName())
                .set(SysUser::getPhonenumber, user.getPhonenumber())
                .set(SysUser::getEmail, user.getEmail())
                .set(SysUser::getSex, user.getSex())
                .eq(SysUser::getUserId, user.getUserId()));
    }

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    public boolean updateUserAvatar(Long userId, Long avatar) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getAvatar, avatar)
                .eq(SysUser::getUserId, userId)) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 结果
     */
    public int resetUserPwd(Long userId, String password) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getPassword, password)
                .eq(SysUser::getUserId, userId));
    }

    /**
     * 新增用户角色信息
     *
     * @param user  用户对象
     * @param clear 清除已存在的关联数据
     */
    private void insertUserRole(SysUserBo user, boolean clear) {
        this.insertUserRole(user.getUserId(), user.getRoleIds(), clear);
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     * @param clear   清除已存在的关联数据
     */
    private void insertUserRole(Long userId, Long[] roleIds, boolean clear) {
        if (ArrayUtil.isEmpty(roleIds)) {
            return;
        }

        List<Long> roleList = new ArrayList<>(Arrays.asList(roleIds));

        // 非超级管理员，禁止包含超级管理员角色
        if (!LoginHelper.isSuperAdmin(userId)) {
            roleList.remove(SystemConstants.SUPER_ADMIN_ID);
        }

        // 校验是否有权限访问这些角色（含数据权限控制）
        if (roleMapper.selectRoleCount(roleList) != roleList.size()) {
            throw new BizException("没有权限访问角色的数据");
        }

        // 是否清除原有绑定
        if (clear) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        }

        // 批量插入用户-角色关联
        List<SysUserRole> list = StreamUtils.toList(roleList,
            roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            });
        userRoleMapper.insertBatch(list);
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 防止更新失败导致的数据删除
        int flag = baseMapper.deleteById(userId);
        if (flag < 1) {
            throw new BizException("删除用户失败!");
        }
        return flag;
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(userId);
            checkUserDataScope(userId);
        }
        List<Long> ids = List.of(userIds);
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
        // 防止更新失败导致的数据删除
        int flag = baseMapper.deleteByIds(ids);
        if (flag < 1) {
            throw new BizException("删除用户失败!");
        }
        return flag;
    }

    /**
     * 通过用户ID查询用户账户
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    @Cacheable(cacheNames = CacheNames.SYS_USER_NAME, key = "#userId")
    public String selectUserNameById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserName).eq(SysUser::getUserId, userId));
        return ObjectUtils.notNullGetter(sysUser, SysUser::getUserName);
    }

    /**
     * 通过用户ID查询用户账户
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    @Cacheable(cacheNames = CacheNames.SYS_NICKNAME, key = "#userId")
    public String selectNicknameById(Long userId) {
        SysUser sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getNickName).eq(SysUser::getUserId, userId));
        return ObjectUtils.notNullGetter(sysUser, SysUser::getNickName);
    }

    /**
     * 通过用户ID查询用户账户
     *
     * @param userIds 用户ID 多个用逗号隔开
     * @return 用户账户
     */
    public String selectNicknameByIds(String userIds) {
        List<String> list = new ArrayList<>();
        for (Long id : StringUtils.splitTo(userIds, Convert::toLong)) {
            String nickname = SpringUtils.getAopProxy(this).selectNicknameById(id);
            if (StringUtils.isNotBlank(nickname)) {
                list.add(nickname);
            }
        }
        return StringUtils.joinComma(list);
    }

    /**
     * 通过用户ID查询用户列表
     *
     * @param userIds 用户ids
     * @return 用户列表
     */
    public List<UserDTO> selectListByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return List.of();
        }
        List<SysUserVo> list = baseMapper.selectVoList(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserId, SysUser::getUserName,
                SysUser::getNickName, SysUser::getEmail,
                SysUser::getPhonenumber, SysUser::getSex, SysUser::getStatus,
                SysUser::getCreateTime)
            .eq(SysUser::getStatus, SystemConstants.NORMAL)
            .in(SysUser::getUserId, userIds));
        return BeanUtil.copyToList(list, UserDTO.class);
    }

    /**
     * 通过角色ID查询用户ID
     *
     * @param roleIds 角色ids
     * @return 用户ids
     */
    public List<Long> selectUserIdsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, roleIds));
        return StreamUtils.toList(userRoles, SysUserRole::getUserId);
    }

    /**
     * 通过角色ID查询用户
     *
     * @param roleIds 角色ids
     * @return 用户
     */
    public List<UserDTO> selectUsersByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }

        // 通过角色ID获取用户角色信息
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, roleIds));

        // 获取用户ID列表
        Set<Long> userIds = StreamUtils.toSet(userRoles, SysUserRole::getUserId);

        return this.selectListByIds(new ArrayList<>(userIds));
    }

    /**
     * 根据用户 ID 列表查询用户名称映射关系
     *
     * @param userIds 用户 ID 列表
     * @return Map，其中 key 为用户 ID，value 为对应的用户名称
     */
    public Map<Long, String> selectUserNamesByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<SysUser> list = baseMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getUserId, SysUser::getNickName)
                .in(SysUser::getUserId, userIds)
        );
        return StreamUtils.toMap(list, SysUser::getUserId, SysUser::getNickName);
    }
    public String getUniqueInviteCode() {
        String code;
        boolean isUnique = false;
        int retryCount = 0;
        while (!isUnique) {
            if (retryCount > 100) {
                throw new BizException("生成邀请码失败，请稍后重试");
            }
            // 生成6位字母数字验证码
            code = RandomUtil.randomString(6);
            Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getInviteCode, code)
            );
            if (count == 0) {
                return code;
            }
            retryCount++;
        }
        return null;
    }
    public void share() {
        baseMapper.update(null,
            new LambdaUpdateWrapper<SysUser>()
                .setSql("total_share_count = total_share_count + 1")
                .eq(SysUser::getUserId, LoginHelper.getUserId())
        );
    }

    private static final String KEY_DAILY_RULE = "sign.daily.rule";
    private static final String KEY_STREAK_RULE = "sign.streak.rule";
    private static final String KEY_MAX_BREAK_DAYS = "sign.max.break.days";

    // bizType 中文标识
    private static final String BIZ_TYPE_SIGN = "签到";
    private static final String BIZ_TYPE_STREAK = "连续签到奖励";

    @Data
    @AllArgsConstructor
    private static class CycleInfo {
        private LocalDate cycleStartDate;
        private int signedDaysInCycle;  // 周期内累计签到天数
        private int currentStreak;       // 当前真正连续的天数
    }

    // ================= 签到视图 =================

    public SignInViewVo getSignInView(Long userId) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> signedDates = getSignedDateSet(userId);
        boolean isSignedToday = signedDates.contains(today);

        CycleInfo cycle = getCycleInfo(signedDates, isSignedToday);

        SignInViewVo view = new SignInViewVo();
        view.setSignedToday(isSignedToday);
        view.setSignedDays(cycle.getSignedDaysInCycle());
        view.setCurrentStreak(cycle.getCurrentStreak());
        view.setCycleStartDate(Optional.ofNullable(cycle.getCycleStartDate())
                .map(LocalDate::toString).orElse(null));

        view.setDailyRewards(buildDailyRewards(cycle.getSignedDaysInCycle()));
        view.setRetroDates(buildRetroDates(cycle, signedDates, today));
        view.setStreakRewards(buildStreakRewards(userId, cycle));

        return view;
    }

    private List<SignInViewVo.DailyRewardInfo> buildDailyRewards(int signedDays) {
        List<SignInViewVo.DailyRewardInfo> list = new ArrayList<>();
        int displayDays = Math.max(signedDays + 3, 7);

        for (int day = 1; day <= displayDays; day++) {
            SignInViewVo.DailyRewardInfo info = new SignInViewVo.DailyRewardInfo();
            info.setDay(day);
            info.setReward(getDailyReward(day));
            info.setSigned(day <= signedDays);
            list.add(info);
        }
        return list;
    }

    private List<String> buildRetroDates(CycleInfo cycle, Set<LocalDate> signedDates, LocalDate today) {
        if (cycle.getCycleStartDate() == null) {
            return List.of();
        }
        List<String> retroDates = new ArrayList<>();
        for (LocalDate d = cycle.getCycleStartDate(); d.isBefore(today); d = d.plusDays(1)) {
            if (!signedDates.contains(d)) {
                retroDates.add(d.toString());
            }
        }
        return retroDates;
    }

    private List<SignInViewVo.StreakRewardInfo> buildStreakRewards(Long userId, CycleInfo cycle) {
        List<SignInViewVo.StreakRewardInfo> list = new ArrayList<>();
        JSONObject streakRule = getStreakRuleJson();
        if (streakRule.isEmpty()) return list;

        String cycleKey = Optional.ofNullable(cycle.getCycleStartDate())
                .map(LocalDate::toString).orElse("none");

        // 查询本周期已领取的奖励 (bizCode格式: 周期日期:天数)
        Set<Integer> claimed = bizLogMapper.selectList(Wrappers.<BizLog>lambdaQuery()
                .eq(BizLog::getCreateBy, userId)
                .eq(BizLog::getBizType, BIZ_TYPE_STREAK)
                .likeRight(BizLog::getBizCode, cycleKey + ":")
        ).stream()
                .map(log -> NumberUtil.parseInt(StrUtil.subAfter(log.getBizCode(), ":", false)))
                .collect(Collectors.toSet());

        int streak = cycle.getCurrentStreak();

        streakRule.keySet().stream()
                .filter(k -> !"default".equals(k))
                .map(Integer::parseInt)
                .sorted()
                .forEach(days -> {
                    SignInViewVo.StreakRewardInfo info = new SignInViewVo.StreakRewardInfo();
                    info.setDays(days);
                    info.setReward(toIntMap(streakRule.getJSONObject(String.valueOf(days))));

                    if (claimed.contains(days)) {
                        info.setStatus("CLAIMED");
                    } else if (streak >= days) {
                        info.setStatus("CLAIMABLE");
                    } else {
                        info.setStatus("LOCKED");
                    }
                    list.add(info);
                });

        return list;
    }

    // ================= 核心方法 =================

    @Transactional(rollbackFor = Exception.class)
    public void doSign(Long userId) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> signedDates = getSignedDateSet(userId);

        if (signedDates.contains(today)) {
            throw new BizException("今天已经签到过了");
        }

        CycleInfo cycle = getCycleInfo(signedDates, false);
        int todayIndex = cycle.getSignedDaysInCycle() + 1;

        Map<String, Integer> reward = getDailyReward(todayIndex);
        // bizCode=日期, bizType=中文标识
        bizLogService.updateAssets(userId, today.toString(), BIZ_TYPE_SIGN, reward);
    }

    @Transactional(rollbackFor = Exception.class)
    public void doRetroSign(Long userId, String dateStr) {
        LocalDate targetDate = LocalDate.parse(dateStr);
        LocalDate today = LocalDate.now();

        if (!targetDate.isBefore(today)) {
            throw new BizException("只能补签过去的日期");
        }

        Set<LocalDate> signedDates = getSignedDateSet(userId);
        if (signedDates.contains(targetDate)) {
            throw new BizException("该日期已签到");
        }

        CycleInfo cycle = getCycleInfo(signedDates, signedDates.contains(today));
        if (cycle.getCycleStartDate() == null || targetDate.isBefore(cycle.getCycleStartDate())) {
            throw new BizException("不能补签当前周期之前的日期");
        }

        Map<String, Integer> reward = getDailyReward(-1); // 补签用default
        bizLogService.updateAssets(userId, dateStr, BIZ_TYPE_SIGN, reward);
    }

    @Transactional(rollbackFor = Exception.class)
    public void claimStreakReward(Long userId, Integer days) {
        LocalDate today = LocalDate.now();
        Set<LocalDate> signedDates = getSignedDateSet(userId);
        CycleInfo cycle = getCycleInfo(signedDates, signedDates.contains(today));

        if (cycle.getCurrentStreak() < days) {
            throw new BizException("连续签到天数不足，当前" + cycle.getCurrentStreak() + "天");
        }

        JSONObject ruleJson = getStreakRuleJson();
        JSONObject rewardJson = ruleJson.getJSONObject(String.valueOf(days));
        if (rewardJson == null) {
            throw new BizException("不存在" + days + "天的连续签到奖励");
        }

        String cycleKey = Optional.ofNullable(cycle.getCycleStartDate())
                .map(LocalDate::toString).orElse("none");
        String bizCode = cycleKey + ":" + days;

        boolean claimed = bizLogMapper.exists(Wrappers.<BizLog>lambdaQuery()
                .eq(BizLog::getCreateBy, userId)
                .eq(BizLog::getBizType, BIZ_TYPE_STREAK)
                .eq(BizLog::getBizCode, bizCode));
        if (claimed) {
            throw new BizException("本周期已领取该奖励");
        }

        bizLogService.updateAssets(userId, bizCode, BIZ_TYPE_STREAK, toIntMap(rewardJson));
    }

    // ================= 核心周期计算（已修复） =================

    private CycleInfo getCycleInfo(Set<LocalDate> signedDates, boolean isSignedToday) {
        if (signedDates.isEmpty()) {
            return new CycleInfo(null, 0, 0);
        }

        int maxBreakDays = getMaxBreakDays();
        LocalDate today = LocalDate.now();

        // 1. 找最近一次签到
        LocalDate lastSigned = signedDates.stream()
                .filter(d -> !d.isAfter(today))
                .max(Comparator.naturalOrder())
                .orElse(null);
        if (lastSigned == null) {
            return new CycleInfo(null, 0, 0);
        }

        // 2. 【修复】断签判断：实际断签天数 = between - 1
        long daysSinceLastSign = ChronoUnit.DAYS.between(lastSigned, today);
        int missedDays = (int) daysSinceLastSign - 1; // 不含今天
        if (!isSignedToday && missedDays > maxBreakDays) {
            return new CycleInfo(null, 0, 0); // 开启新周期
        }

        // 3. 【修复】从lastSigned往前遍历计算周期
        int signedDaysInCycle = 0;
        int consecutiveMissed = 0;
        LocalDate cycleStart = null;

        for (int i = 0; i <= 365; i++) {
            LocalDate cursor = lastSigned.minusDays(i);
            if (signedDates.contains(cursor)) {
                signedDaysInCycle++;
                cycleStart = cursor;
                consecutiveMissed = 0;
            } else {
                consecutiveMissed++;
                if (consecutiveMissed > maxBreakDays) {
                    break;
                }
            }
        }

        // 4. 【修复】计算真正连续天数（从lastSigned无间断往前）
        int streak = 0;
        for (int i = 0; i <= 365; i++) {
            LocalDate cursor = lastSigned.minusDays(i);
            if (signedDates.contains(cursor)) {
                streak++;
            } else {
                break; // 遇到断签立即停止
            }
        }

        return new CycleInfo(cycleStart, signedDaysInCycle, streak);
    }

    // ================= 辅助方法 =================

    private Set<LocalDate> getSignedDateSet(Long userId) {
        return bizLogMapper.selectList(Wrappers.<BizLog>lambdaQuery()
                .select(BizLog::getBizCode) // 直接取日期
                .eq(BizLog::getCreateBy, userId)
                .eq(BizLog::getBizType, BIZ_TYPE_SIGN)
                .orderByDesc(BizLog::getCreateTime)
                .last("LIMIT 366")
        ).stream()
                .map(log -> LocalDate.parse(log.getBizCode()))
                .collect(Collectors.toSet());
    }

    private int getMaxBreakDays() {
        return NumberUtil.parseInt(configService.selectConfigByKey(KEY_MAX_BREAK_DAYS), 1);
    }

    private JSONObject getDailyRuleJson() {
        String val = configService.selectConfigByKey(KEY_DAILY_RULE);
        return StrUtil.isNotBlank(val) ? JSONUtil.parseObj(val) : new JSONObject();
    }

    private JSONObject getStreakRuleJson() {
        String val = configService.selectConfigByKey(KEY_STREAK_RULE);
        return StrUtil.isNotBlank(val) ? JSONUtil.parseObj(val) : new JSONObject();
    }

    private Map<String, Integer> getDailyReward(int dayIndex) {
        JSONObject json = getDailyRuleJson();
        JSONObject node = json.getJSONObject(String.valueOf(dayIndex));
        if (node == null) {
            node = json.getJSONObject("default");
        }
        return node != null ? toIntMap(node) : Map.of("coin", 10);
    }

    private Map<String, Integer> toIntMap(JSONObject json) {
        Map<String, Integer> map = new HashMap<>();
        json.forEach((k, v) -> map.put(k, Convert.toInt(v)));
        return map;
    }
    

}
