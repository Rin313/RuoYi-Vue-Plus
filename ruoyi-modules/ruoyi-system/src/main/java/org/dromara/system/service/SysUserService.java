package org.dromara.system.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.dto.UserDTO;
import org.dromara.common.core.enums.BizRule;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.utils.*;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.BizLog;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.SysUserRole;
import org.dromara.system.domain.bo.SysUserBo;
import org.dromara.system.domain.vo.SignStatusVo;
import org.dromara.system.domain.vo.SysRoleVo;
import org.dromara.system.domain.vo.SysUserExportVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public void retroSign(LocalDate date) {
        Long userId = LoginHelper.getUserId();
        LocalDate today = LocalDate.now();
        if (!date.isBefore(today)) {
            throw new BizException("只能补签过去的日期");
        }
        SysUserVo userVo = selectUserById(userId);
        List<String> signRecord = userVo.getSignRecord();
        // 检查是否已签到
        if (signRecord.contains(date.toString())) {
            throw new BizException("该日期已经签到过了");
        }
        // 添加补签记录
        signRecord.add(date.toString());
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setSignRecord(signRecord);
        baseMapper.updateById(user);
    }

    public void sign() {
        Map<String,Object> map=JsonUtils.parseMap(configService.selectConfigByKey("sign_rewards"));
        
        bizLogService.executeRule(LoginHelper.getUserId(),BizRule.SIGN);
    }
    /**
     * 计算连续签到天数
     */
    public int getConsecutiveSignDays(Long userId) {
        SysUserVo user = selectUserById(userId);
        List<String> signRecord = user.getSignRecord();
        if (signRecord == null || signRecord.isEmpty()) {
            return 0;
        }
        // 将日期字符串转换为 LocalDate 并排序（降序，最近的日期在前）
        List<LocalDate> sortedDates = signRecord.stream()
            .map(dateStr -> LocalDate.parse(dateStr))
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 确定起始日期：如果今天已签到从今天开始，否则从昨天开始
        LocalDate startDate;
        if (sortedDates.contains(today)) {
            startDate = today;
        } else if (sortedDates.contains(yesterday)) {
            startDate = yesterday;
        } else {
            // 既没有今天也没有昨天的签到记录，连续天数为0
            return 0;
        }

        int consecutiveDays = 0;
        LocalDate expectedDate = startDate;

        for (LocalDate signDate : sortedDates) {
            if (signDate.equals(expectedDate)) {
                consecutiveDays++;
                expectedDate = expectedDate.minusDays(1);
            } else if (signDate.isBefore(expectedDate)) {
                // 日期不连续，跳出循环
                break;
            }
        }

        return consecutiveDays;
    }
    // 配置Key
    private static final String KEY_DAILY_REWARD = "sign.daily.rewards";
    private static final String KEY_STREAK_REWARD = "sign.streak.rewards";

    /**
     * 核心接口：获取签到面板信息
     */
    public Map<String, Object> getSignInfo(Long userId) {
        // 1. 获取所有签到日志（按时间正序）
        List<BizLog> logs = getSignLogs(userId);
        
        // 2. 计算周期开始时间
        Date cycleStart = calculateCycleStart(logs);
        
        // 3. 构建30天日历状态
        Map<Integer, Boolean> calendar = new HashMap<>();
        // 今天的索引（0-29）
        long daysBetween = DateUtil.betweenDay(cycleStart, new Date(), true);
        // 如果超过29天（第31天），说明其实今天是新周期的第一天，但用户还没签到
        if (daysBetween >= 30) {
            cycleStart = new Date(); // 逻辑重置
            daysBetween = 0;
            calendar.clear();
        }

        // 填充已签到的天数
        int streak = 0; // 连续签到天数计算
        boolean isBroken = false;
        
        // 这种算法虽然O(N)但很直观
        for (int i = 0; i < 30; i++) {
            Date targetDay = DateUtil.offsetDay(cycleStart, i);
            boolean signed = isSignedOnDate(logs, targetDay);
            calendar.put(i + 1, signed);
        }

        // 计算连续签到（截至到昨天/今天）
        // 这种简单逻辑直接查最近的连续记录即可，为了快速，这里简化处理：
        // 实际业务建议在Redis存一下streak，或者像下面这样倒序查log
        int currentStreak = calculateStreak(logs, new Date());

        // 4. 连续签到奖励领取状态
        List<String> claimedStreaks = getClaimedStreakLogs(userId, cycleStart);

        return Map.of(
            "cycleStart", cycleStart,
            "todayIndex", daysBetween + 1, // 第几天
            "isSignedToday", calendar.get((int)daysBetween + 1),
            "calendar", calendar,
            "streakDay", currentStreak,
            "claimedStreaks", claimedStreaks
        );
    }

    /**
     * 动作：签到
     */
    @Transactional
    public void sign(Long userId) {
        // 1. 检查今日是否已签（利用BizRule的LIMIT=1限制）
        // 实际上checkLimit只管次数，不管是不是周期内的，所以还是得配合BizLogService
        if (!bizLogService.checkLimit(userId, BizRule.SIGN)) {
            throw new BizException("今天已经签到过了");
        }

        // 2. 计算今天是周期的第几天
        List<BizLog> logs = getSignLogs(userId);
        Date cycleStart = calculateCycleStart(logs);
        long dayIndex = DateUtil.betweenDay(cycleStart, new Date(), true) + 1; // 1-30

        if (dayIndex > 30) {
            // 新周期第一天
            dayIndex = 1;
        }

        // 3. 拿奖励配置
        Map<String, Integer> reward = getRewardConfig(KEY_DAILY_REWARD, String.valueOf(dayIndex));
        if (reward == null) reward = Map.of("coin", 10); // 兜底低保

        // 4. 执行发奖 (checkLimit通过后)
        bizLogService.execute(userId, BizRule.SIGN.getBizCode(), BizRule.SIGN.getBizType(), reward);
    }

    /**
     * 动作：补签
     * @param dayIndex 周期内的第几天 (1-30)
     */
    @Transactional
    public void resign(Long userId, int dayIndex) {
        // 1. 算出那一天是几号
        List<BizLog> logs = getSignLogs(userId);
        Date cycleStart = calculateCycleStart(logs);
        // 如果当前是新周期第一天且未签到，cycleStart是旧的，这里逻辑会稍微复杂。
        // 为了“莽撞”，我们假设补签只能补当前显示周期内的。
        if (DateUtil.betweenDay(cycleStart, new Date(), true) >= 30) {
            // 既然当前周期结束了，你补哪门子签？直接让前端刷新开启新周期
            throw new BizException("当前周期已结束，请先签到开启新周期");
        }

        Date targetDate = DateUtil.offsetDay(cycleStart, dayIndex - 1);
        if (DateUtil.isSameDay(targetDate, new Date())) throw new BizException("今天请直接签到");
        if (targetDate.after(new Date())) throw new BizException("未来无法补签");

        // 2. 检查那天是否签过
        if (isSignedOnDate(logs, targetDate)) {
            throw new BizException("那天已经签过了");
        }

        // 3. 拿那天的奖励
        Map<String, Integer> reward = getRewardConfig(KEY_DAILY_REWARD, String.valueOf(dayIndex));
        
        // 4. 执行补签（扣钱+插日志+发奖）
        bizLogService.executeResign(userId, targetDate, reward == null ? Map.of("coin", 10) : reward);
    }

    /**
     * 动作：领取连签奖励
     * @param targetStreak 目标连签天数，如 7, 14, 30
     */
    @Transactional
    public void claimStreakReward(Long userId, int targetStreak) {
        // 1. 检查是否已领取
        Date cycleStart = calculateCycleStart(getSignLogs(userId));
        Long count = bizLogMapper.selectCount(new LambdaQueryWrapper<BizLog>()
            .eq(BizLog::getCreateBy, userId)
            .eq(BizLog::getBizCode, BizRule.SIGN_STREAK.getBizCode())
            .eq(BizLog::getBizType, String.valueOf(targetStreak)) // bizType存天数
            .ge(BizLog::getCreateTime, cycleStart)); // 必须是本周期内的
        
        if (count > 0) throw new BizException("本周期该奖励已领取");

        // 2. 检查连签是否达标
        int currentStreak = calculateStreak(getSignLogs(userId), new Date());
        if (currentStreak < targetStreak) throw new BizException("连续签到天数不足");

        // 3. 发奖
        Map<String, Integer> reward = getRewardConfig(KEY_STREAK_REWARD, String.valueOf(targetStreak));
        if (reward == null) throw new BizException("该天数没有配置奖励");

        // 这里bizType存天数 "7", "14"
        bizLogService.execute(userId, BizRule.SIGN_STREAK.getBizCode(), String.valueOf(targetStreak), reward);
    }

    // ================== 私有 脏逻辑区 ==================

    private List<BizLog> getSignLogs(Long userId) {
        return bizLogMapper.selectList(new LambdaQueryWrapper<BizLog>()
            .eq(BizLog::getCreateBy, userId)
            .eq(BizLog::getBizCode, BizRule.SIGN.getBizCode())
            .orderByAsc(BizLog::getCreateTime)); // 按时间正序，方便推算周期
    }

    // 核心算法：寻找当前30天周期的起点
    private Date calculateCycleStart(List<BizLog> logs) {
        if (logs.isEmpty()) return new Date(); // 没签过，今天就是起点

        Date start = logs.get(0).getCreateTime();
        for (int i = 1; i < logs.size(); i++) {
            Date current = logs.get(i).getCreateTime();
            // 如果某次签到距离上一个起点超过30天，它就是新的起点
            if (DateUtil.betweenDay(start, current, true) >= 30) {
                start = current;
            }
        }
        
        // 检查这个起点是不是太老了（比如上次玩是半年前）
        // 如果起点距离今天超过30天，说明周期已过期，今天（或者最近一次签到）应当是新起点
        if (DateUtil.betweenDay(start, new Date(), true) >= 30) {
            return new Date(); // 视为新周期开始
        }
        return start;
    }

    private boolean isSignedOnDate(List<BizLog> logs, Date date) {
        for (BizLog log : logs) {
            if (DateUtil.isSameDay(log.getCreateTime(), date)) return true;
        }
        return false;
    }

    private int calculateStreak(List<BizLog> logs, Date baseDate) {
        // 简单粗暴：从昨天开始往前倒推
        int streak = 0;
        // 如果今天签了，streak至少是1
        if (isSignedOnDate(logs, baseDate)) streak++;
        
        Date checkDay = DateUtil.offsetDay(baseDate, -1);
        while (true) {
            if (isSignedOnDate(logs, checkDay)) {
                streak++;
                checkDay = DateUtil.offsetDay(checkDay, -1);
            } else {
                break;
            }
        }
        return streak;
    }

    private List<String> getClaimedStreakLogs(Long userId, Date cycleStart) {
        List<BizLog> logs = bizLogMapper.selectList(new LambdaQueryWrapper<BizLog>()
            .eq(BizLog::getCreateBy, userId)
            .eq(BizLog::getBizCode, BizRule.SIGN_STREAK.getBizCode())
            .ge(BizLog::getCreateTime, cycleStart));
        return logs.stream().map(BizLog::getBizType).toList();
    }

    // 解析配置 JSON -> Map<Day, Rewards> -> Map<Asset, Count>
    // 假设配置格式: {"1": {"coin": 100}, "3": {"gold": 10}}
    @SneakyThrows
    private Map<String, Integer> getRewardConfig(String configKey, String subKey) {
        String json = configService.selectConfigByKey(configKey);
        if (StrUtil.isBlank(json)) return null;
        JsonNode root = objectMapper.readTree(json);
        JsonNode node = root.get(subKey);
        if (node == null) return null;
        return objectMapper.convertValue(node, new TypeReference<Map<String, Integer>>() {});
    }
}
