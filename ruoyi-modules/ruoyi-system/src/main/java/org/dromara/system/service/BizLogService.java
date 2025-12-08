package org.dromara.system.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.enums.BizRule;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.dromara.system.domain.bo.BizLogQueryBo;
import org.dromara.system.domain.vo.BizLogVo;
import org.dromara.system.domain.BizLog;
import org.dromara.system.domain.SysUser;
import org.dromara.system.mapper.BizLogMapper;
import org.dromara.system.mapper.SysUserMapper;
@Slf4j
@RequiredArgsConstructor
@Service
public class BizLogService {
    private final BizLogMapper bizLogMapper;
    private final SysUserMapper sysUserMapper;
    public void executeRule(Long userId, BizRule rule) {
        if (!checkLimit(userId, rule))
            throw new BizException("已达到限制次数");
        updateAssets(userId, rule.getBizCode(), rule.getBizType(), rule.getAssetRule());
    }
    /**
     * 不走枚举，直接操纵资产（适用于非规则类，如管理员后台扣款、转账等）
     */
    public void execute(Long userId, String bizCode, String bizType, Map<String, Integer> changes) {
        updateAssets(userId, bizCode, bizType, changes);
    }
    public boolean checkLimit(Long userId, BizRule rule) {
        if ("NONE".equals(rule.getLimitType())) return true;
        LambdaQueryWrapper<BizLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizLog::getCreateBy, userId)
            .eq(BizLog::getBizCode, rule.getBizCode());
        Date now = new Date();
        switch (rule.getLimitType()) {
            case "DAY" -> wrapper.ge(BizLog::getCreateTime, DateUtil.beginOfDay(now));
            case "WEEK" -> wrapper.ge(BizLog::getCreateTime, DateUtil.beginOfWeek(now));
            case "MONTH" -> wrapper.ge(BizLog::getCreateTime, DateUtil.beginOfMonth(now));
            case "ALL" -> {
                // 永久限制，不需要时间条件
            }
            default -> {
                return true;
            }
        }
        return bizLogMapper.selectCount(wrapper) < rule.getLimit();
    }
    /**
     * 核心资产变更逻辑（CAS乐观锁重试）
     */
    private void updateAssets(Long userId, String bizCode, String bizType, Map<String, Integer> changes) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            // 必须查最新的Version
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) throw new BizException("用户不存在");
            Map<String, Integer> currentAssets = user.getAssets();
            if (currentAssets == null) currentAssets = new HashMap<>();
            List<Map<String, Object>> assetLog = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : changes.entrySet()) {
                String assetKey = entry.getKey();
                Integer changeAmount = entry.getValue();
                Integer beforeAmount = currentAssets.getOrDefault(assetKey, 0);
                Integer afterAmount = beforeAmount + changeAmount;
                if (afterAmount < 0) {
                     throw new BizException("余额不足");
                }
                currentAssets.put(assetKey, afterAmount);
                assetLog.add(Map.of("asset_name", assetKey,"amount", changeAmount,"before", beforeAmount,"after", afterAmount));
            }
            user.setAssets(currentAssets);
            // updateById会检查version
            int rows = sysUserMapper.updateById(user);
            if (rows > 0) {
                saveBizLog(userId, bizCode, bizType, assetLog);
                return;
            } else log.warn("用户[{}]资产更新并发冲突，正在进行第{}次重试...", userId, i + 1);
        }
        throw new BizException("系统繁忙，请稍后重试");
    }
    /**
     * 异步或同步保存日志
     */
    private void saveBizLog(Long userId, String bizCode, String bizType, List<Map<String, Object>> assetLog) {
        BizLog log = new BizLog();
        log.setBizCode(bizCode);
        log.setBizType(bizType);
        log.setAssetLog(assetLog);
        bizLogMapper.insert(log);
    }
    //补签专用，用于不创建额外字段
    @Transactional(rollbackFor = Exception.class)
    public void executeResign(Long userId, Date targetDate, Map<String, Integer> rewards) {
        // 1. 扣除补签卡或货币（假设补签一次100金币，这里写死，你可以做成配置）
        updateAssets(userId, "resign_cost", "COST", Map.of("coin", -100));
        // 注意：补签算作签到，bizCode用SIGN
        updateAssets(userId, BizRule.SIGN.getBizCode(), BizRule.SIGN.getBizType(), rewards);
        // 3. 【极度莽撞】刚插进去是当前时间，立马把上一条（刚插的）更新为指定时间
        BizLog lastLog = bizLogMapper.selectOne(new LambdaQueryWrapper<BizLog>()
                .eq(BizLog::getCreateBy, userId)
                .eq(BizLog::getBizCode, BizRule.SIGN.getBizCode())
                .orderByDesc(BizLog::getId)
                .last("LIMIT 1"));
        if (lastLog != null) {
            lastLog.setCreateTime(targetDate); // 修改为历史时间
            bizLogMapper.updateById(lastLog);
        }
    }

    // private final BizLogMapper baseMapper;
    // public IPage<BizLogVo> selectPage(BizLogQueryBo bo, PageQuery pageQuery) {
    //     LambdaQueryWrapper<BizLog> lqw = Wrappers.lambdaQuery();
    //     // lqw.eq(bo.getUserId() != null, BizLog::getUserId, bo.getUserId());
    //     // lqw.eq(bo.getAssetType() != null, BizLog::getAssetType, bo.getAssetType());
    //     // lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), BizLog::getBusinessType, bo.getBusinessType());
    //     // lqw.eq(bo.getRefId() != null, BizLog::getRefId, bo.getRefId());
    //     // lqw.ge(ObjectUtils.isNotEmpty(bo.getBeginTime()), BizLog::getCreateTime,bo.getBeginTime());
    //     // lqw.le(ObjectUtils.isNotEmpty(bo.getEndTime()), BizLog::getCreateTime,bo.getEndTime());
    //     return baseMapper.selectVoPage(pageQuery.build(), lqw);
    // }

}