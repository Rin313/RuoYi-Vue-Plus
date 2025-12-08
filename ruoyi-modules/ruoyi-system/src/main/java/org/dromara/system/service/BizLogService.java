package org.dromara.system.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    /**
     * 资产变更+保存日志（CAS乐观锁重试）
     */
    public void updateAssets(Long userId, String bizCode, String bizType, Map<String, Integer> changes) {
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
                BizLog log = new BizLog();
                log.setBizCode(bizCode);
                log.setBizType(bizType);
                log.setAssetLog(assetLog);
                bizLogMapper.insert(log);
                return;
            } else log.warn("用户[{}]资产更新并发冲突，正在进行第{}次重试...", userId, i + 1);
        }
        throw new BizException("系统繁忙，请稍后重试");
    }
    //允许手动设置日志时间
    public void updateAssets(Long userId, String bizCode, String bizType, Map<String, Integer> changes,Date createTime) {
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
                BizLog log = new BizLog();
                log.setBizCode(bizCode);
                log.setBizType(bizType);
                log.setAssetLog(assetLog);
                log.setCreateTime(createTime);
                bizLogMapper.insert(log);
                return;
            } else log.warn("用户[{}]资产更新并发冲突，正在进行第{}次重试...", userId, i + 1);
        }
        throw new BizException("系统繁忙，请稍后重试");
    }
    //根据枚举修改资产
    public void executeRule(Long userId, BizRule rule) {
        if (!checkLimit(userId, rule))
            throw new BizException("已达到限制次数");
        updateAssets(userId, rule.getBizCode(), rule.getBizType(), rule.getAssetRule());
    }
    public void executeRule(Long userId, BizRule rule,Date creatTime) {
        if (!checkLimit(userId, rule))
            throw new BizException("已达到限制次数");
        updateAssets(userId, rule.getBizCode(), rule.getBizType(), rule.getAssetRule(),creatTime);
    }
    public boolean checkLimit(Long userId, BizRule rule) {
        if ("NONE".equals(rule.getLimitType())) return true;
        Date now = new Date();
        Date startTime = switch (rule.getLimitType()) {
            case "DAY"   -> DateUtil.beginOfDay(now);
            case "WEEK"  -> DateUtil.beginOfWeek(now);
            case "MONTH" -> DateUtil.beginOfMonth(now);
            default      -> null;  // ALL
        };
        return bizLogMapper.selectCount(Wrappers.<BizLog>lambdaQuery()
            .eq(BizLog::getCreateBy, userId)
            .eq(BizLog::getBizCode, rule.getBizCode())
            .ge(startTime != null, BizLog::getCreateTime, startTime)
        ) < rule.getLimit();
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