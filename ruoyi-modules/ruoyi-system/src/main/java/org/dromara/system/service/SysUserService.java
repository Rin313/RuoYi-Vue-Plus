package org.dromara.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
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
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.dto.UserDTO;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.utils.*;
import org.dromara.common.json.utils.JsonUtils;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
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
    public void insertUserAuth(Long userId, List<Long> roleIds) {
        insertUserRole(userId, roleIds, true);
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
    private void insertUserRole(Long userId, List<Long> roleIds, boolean clear) {
        if (ArrayUtil.isEmpty(roleIds)) {
            return;
        }

        // 非超级管理员，禁止包含超级管理员角色
        if (!LoginHelper.isSuperAdmin(userId)) {
            roleIds.remove(SystemConstants.SUPER_ADMIN_ID);
        }

        // 校验是否有权限访问这些角色（含数据权限控制）
        if (roleMapper.selectRoleCount(roleIds) != roleIds.size()) {
            throw new BizException("没有权限访问角色的数据");
        }

        // 是否清除原有绑定
        if (clear) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        }

        // 批量插入用户-角色关联
        List<SysUserRole> list = StreamUtils.toList(roleIds,
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
    private static final String BIZ_TYPE_TASK = "TASK";
    /*
    * 任务状态包括未领取、已领取、可领取
    * 日常任务每天都能领取，成长任务只能领取一次
    * 需要的接口：显示包含状态和任务说明的任务视图、领取奖励
    * 每项任务的具体逻辑我会自行实现，返回值都为boolean表示当前是否满足条件 
    */
    /**
     * 任务定义枚举
     * 每个任务都需要具体业务逻辑，没有必要额外加表允许改描述
     */
    @Getter
    @AllArgsConstructor
    public enum TaskDef {
        // ID, 标题, 类型(Daily/Growth), 描述说明
        DAILY_READ("read", "阅读30分钟", "Daily", "阅读三十分钟完成任务"),
        DAILY_LIKE("like", "评论5次/点赞10次", "Daily", "任选一篇小说去评论/点赞"),
        DAILY_SIGN("sign", "签到1次", "Daily", "点击按钮去签到"),
        GROWTH_INVITE("invite", "邀请好友", "Growth", "邀请1人领取书币"),
        GROWTH_AINOVEL("ainovel", "发布AI小说", "Growth", "发布1次AI创作"),
        GROWTH_TOPUP("topup", "首次充值", "Growth", "首次充值领取书币"),
        ;
        public final String key;
        public final String name;
        public final String type;
        public final String description;
        // 根据ID查找枚举的便捷方法
        public static TaskDef getById(String key) {
            return Arrays.stream(values())
                    .filter(t -> t.key.equals(key))
                    .findFirst()
                    .orElseThrow(() -> new BizException("任务不存在: " + key));
        }
    }

    /**
     * 1. 获取任务列表视图
     */
    public List<TaskVo> getTaskListView(Long userId) {
        Date now = new Date();
        TaskDef[] tasks = TaskDef.values();
        
        // 预构建所有任务的 bizKey
        Map<String, String> taskKeyMap = new HashMap<>();
        for (TaskDef task : tasks) {
            taskKeyMap.put(task.key, buildBizKey(task, now));
        }

        // 批量查询已领取的日志（一次IO）
        List<BizLog> logs = bizLogMapper.selectList(new LambdaQueryWrapper<BizLog>()
            .select(BizLog::getBizKey) // 稍微优化，只查需要的字段
            .eq(BizLog::getCreateBy, userId)
            .eq(BizLog::getBizType, BIZ_TYPE_TASK)
            .in(BizLog::getBizKey, taskKeyMap.values()));
        
        Set<String> claimedKeys = logs.stream().map(BizLog::getBizKey).collect(Collectors.toSet());

        List<TaskVo> result = new ArrayList<>(tasks.length);
        for (TaskDef task : tasks) {
            String bizKey = taskKeyMap.get(task.key);
            int status;

            if (claimedKeys.contains(bizKey)) {
                status = 2; // 已领取
            } else {
                // 没领过，才去跑逻辑判断
                boolean isMet = checkTaskCondition(userId, task.key);
                status = isMet ? 1 : 0; // 1:可领取, 0:未完成
            }

            result.add(TaskVo.builder()
                .taskKey(task.key)
                .name(task.name)
                .desc(task.description)
                .type(task.type)        // 前端可能需要区分显示
                .status(status)
                .build());
        }
        return result;
    }

    /**
     * 2. 领取奖励接口
     */
    public void claimReward(Long userId, String taskKey) {
        TaskDef task = TaskDef.getById(taskKey);
        Date now = new Date();
        String bizKey = buildBizKey(task, now);

        // 这里的查库必不可少，防止并发或直接调接口
        boolean exists = bizLogMapper.exists(new LambdaQueryWrapper<BizLog>()
            .eq(BizLog::getCreateBy, userId)
            .eq(BizLog::getBizType, BIZ_TYPE_TASK)
            .eq(BizLog::getBizKey, bizKey));

        if (exists) {
            throw new BizException("任务奖励已领取");
        }

        if (!checkTaskCondition(userId, taskKey)) {
            throw new BizException("未满足领取条件");
        }

        // 动态读取奖励配置，方便运营随时调整数值
        String configKey = "task.reward." + taskKey;
        String rewardJson = configService.selectConfigByKey(configKey);
        Map<String, Integer> rewards = JSONUtil.toBean(rewardJson, Map.class);

        // 核心：发放资产 + 记日志
        bizLogService.updateAssets(userId, bizKey, BIZ_TYPE_TASK, rewards);
    }
    /**
     * 任务具体的判断逻辑
     */
    private boolean checkTaskCondition(Long userId, String taskId) {
        switch (taskId) {
            case "sign":
                return isSignedToday(userId);
            case "read":
                return true;//没必要为这点垃圾奖励和前端协调什么心跳机制 
            case "like":
                return false;
            case "invite":
                return false;
            case "topup":
                return false;
            case "ainovel":
                return false;
            default:
                return false;
        }
    }

    private String buildBizKey(TaskDef task, Date date) {
        // Growth任务Key固定，Daily任务Key带日期
        if ("Daily".equals(task.type)) {
            return StrUtil.format("TASK:{}:{}", task.key, DateUtil.format(date, "yyyyMMdd"));
        }
        return "TASK:" + task.key;
    }

    @Data
    @Builder
    public static class TaskVo {
        private String taskKey;
        private String name;
        private String desc;
        private String type;
        private Integer status; // 0:未完成, 1:可领取, 2:已领取
    }
    private boolean isSignedToday(Long userId){
        LocalDate today = LocalDate.now();
        Set<LocalDate> signedDates = getSignedDateSet(userId);
        return signedDates.contains(today);
    }
    private static final String BIZ_TYPE = "分享奖励";

    public Map<String, Integer> share() {
        Long userId = LoginHelper.getUserId();
        String today = DateUtil.today(); // yyyy-MM-dd
        String bizKey = "share:" + userId + ":" + today;

        boolean exists = bizLogMapper.exists(
            new LambdaQueryWrapper<BizLog>().eq(BizLog::getBizKey, bizKey)
        );
        if (exists) {
            throw new BizException("今日已经分享过了");
        }

        // 读取奖励配置 {"coin":10,"diamond":1}
        String json = configService.selectConfigByKey("share.reward");
        Map<String, Integer> rewards = JsonUtils.parseObject(json, new TypeReference<>() {});

        // 发放
        bizLogService.updateAssets(userId, bizKey, BIZ_TYPE, rewards);

        return rewards;
    }
    /*
    * 通过配置表sys_config单独设置每一天的奖励、连续签到的奖励、允许的最大断签日期
    * 当用户未签到的天数超过断签日期，或者是从未签到过，应该开启一个新的签到周期
    * 如果用户第一天签到，第二天没有签到，那么第三天签到时，应该获得的是第三天的奖励
    * 允许补签错过的日期，不能补签该周期前的日期，也不能补签未来的日期
    * 如果不存在特定天数的奖励，获取default奖励，连续签到的奖励则不循环
    * 返回签到视图，包括整个周期的签到奖励、签到状态（SIGNED/CLAIMABLE/MISSED/FUTURE）、连续签到奖励、连续签到状态
    */
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

    private List<SignInViewVo.StreakRewardInfo> buildStreakRewards(Long userId, CycleInfo cycle) {
        List<SignInViewVo.StreakRewardInfo> list = new ArrayList<>();
        JSONObject streakRule = getStreakRuleJson();
        if (streakRule.isEmpty()) return list;

        String cycleKey = Optional.ofNullable(cycle.getCycleStartDate())
                .map(LocalDate::toString).orElse("none");

        // 查询本周期已领取的奖励 (bizKey格式: 周期日期:天数)
        Set<Integer> claimed = bizLogMapper.selectList(Wrappers.<BizLog>lambdaQuery()
                .eq(BizLog::getCreateBy, userId)
                .eq(BizLog::getBizType, BIZ_TYPE_STREAK)
                .likeRight(BizLog::getBizKey, cycleKey + ":")
        ).stream()
                .map(log -> NumberUtil.parseInt(StrUtil.subAfter(log.getBizKey(), ":", false)))
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
        
        int todayIndex;
        if (cycle.getCycleStartDate() == null) {
            // 新周期，今天是第1天
            todayIndex = 1;
        } else {
            // 周期内的自然日序号
            todayIndex = (int) ChronoUnit.DAYS.between(cycle.getCycleStartDate(), today) + 1;
        }

        Map<String, Integer> reward = getDailyReward(todayIndex);
        bizLogService.updateAssets(userId, today.toString(), BIZ_TYPE_SIGN, reward);
    }

    // ================= 视图构建 =================

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

        view.setDailyRewards(buildDailyRewards(cycle.getCycleStartDate(), signedDates, today));
        view.setStreakRewards(buildStreakRewards(userId, cycle));

        return view;
    }

    /**
     * 根据实际日期判断签到状态
     * 状态说明:
     *   - SIGNED: 已签到
     *   - CLAIMABLE: 可签到（今天且未签到）
     *   - MISSED: 已错过（周期内过去的日期且未签到）
     *   - FUTURE: 未来的日期
     */
    private List<SignInViewVo.DailyRewardInfo> buildDailyRewards(
            LocalDate cycleStartDate, Set<LocalDate> signedDates, LocalDate today) {
        
        List<SignInViewVo.DailyRewardInfo> list = new ArrayList<>();
        
        // 新周期：以今天作为起点展示预览
        LocalDate effectiveStart = (cycleStartDate != null) ? cycleStartDate : today;
        int currentDayIndex = (int) ChronoUnit.DAYS.between(effectiveStart, today) + 1;
        int displayDays = Math.max(currentDayIndex + 3, 7);

        for (int day = 1; day <= displayDays; day++) {
            LocalDate dateForDay = effectiveStart.plusDays(day - 1);
            
            SignInViewVo.DailyRewardInfo info = new SignInViewVo.DailyRewardInfo();
            info.setDay(day);
            //info.setDate(dateForDay.toString());
            info.setReward(getDailyReward(day));
            
            // 根据日期判断状态
            if (signedDates.contains(dateForDay)) {
                info.setStatus("SIGNED");
            } else if (dateForDay.equals(today)) {
                info.setStatus("CLAIMABLE");
            } else if (dateForDay.isBefore(today)) {
                info.setStatus("MISSED");
            } else {
                info.setStatus("FUTURE");
            }
            
            list.add(info);
        }
        return list;
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
        String bizKey = cycleKey + ":" + days;

        boolean claimed = bizLogMapper.exists(Wrappers.<BizLog>lambdaQuery()
                .eq(BizLog::getCreateBy, userId)
                .eq(BizLog::getBizType, BIZ_TYPE_STREAK)
                .eq(BizLog::getBizKey, bizKey));
        if (claimed) {
            throw new BizException("本周期已领取该奖励");
        }

        bizLogService.updateAssets(userId, bizKey, BIZ_TYPE_STREAK, toIntMap(rewardJson));
    }

    // ================= 核心周期计算 =================

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

        // 2. 断签判断：实际断签天数 = between - 1
        long daysSinceLastSign = ChronoUnit.DAYS.between(lastSigned, today);
        int missedDays = (int) daysSinceLastSign - 1; // 不含今天
        if (!isSignedToday && missedDays > maxBreakDays) {
            return new CycleInfo(null, 0, 0); // 开启新周期
        }

        // 3. 从lastSigned往前遍历计算周期
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

        // 4. 计算真正连续天数（从lastSigned无间断往前）
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
                .select(BizLog::getBizKey)
                .eq(BizLog::getCreateBy, userId)
                .eq(BizLog::getBizType, BIZ_TYPE_SIGN)
                .orderByDesc(BizLog::getCreateTime)
                .last("LIMIT 366")
        ).stream()
                .map(log -> LocalDate.parse(log.getBizKey()))
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
