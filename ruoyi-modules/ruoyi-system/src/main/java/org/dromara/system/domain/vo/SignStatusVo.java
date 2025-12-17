package org.dromara.system.domain.vo;

import java.time.LocalDate;
import java.util.Map;

import lombok.Data;

@Data
public class SignStatusVo {
    /** 周期开始日期 */
    private LocalDate cycleStart;
    /** 当前是周期第几天 */
    private Integer currentDay;
    /** 当前连续签到天数 */
    private Integer currentStreak;
    /** 每天签到状态 {天数: 状态} FUTURE/MISSED/CLAIMABLE/SIGNED */
    private Map<Integer, String> dayStatus;
    /** 每天奖励配置 {天数: {资产: 数量}} */
    private Map<Integer, Map<String, Integer>> dayRewards;
    /** 连续签到奖励状态 {天数: 状态} LOCKED/CLAIMABLE/CLAIMED */
    private Map<Integer, String> streakStatus;
    /** 连续签到奖励配置 {天数: {资产: 数量}} */
    private Map<Integer, Map<String, Integer>> streakRewards;
}