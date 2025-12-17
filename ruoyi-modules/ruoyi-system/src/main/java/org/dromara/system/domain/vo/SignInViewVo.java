package org.dromara.system.domain.vo;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class SignInViewVo {
    /** 周期开始日期，null表示新周期 */
    private String cycleStartDate;
    /** 周期内已签天数 */
    private int signedDays;
    /** 当前连续签到天数 */
    private int currentStreak;
    /** 今日是否已签 */
    private boolean signedToday;
    /** 累计签到奖励进度 */
    private List<DailyRewardInfo> dailyRewards;
    /** 连续签到奖励列表 */
    private List<StreakRewardInfo> streakRewards;
    private Map<String,Integer> nextDayReward;
    private Map<String, Integer> retroCost;

    @Data
    public static class DailyRewardInfo {
        private int day;
        private String date;
        private Map<String, Integer> reward;
        private String status;
    }

    @Data
    public static class StreakRewardInfo {
        private int days;
        private Map<String, Integer> reward;
        /** LOCKED-未达成 / CLAIMABLE-可领取 / CLAIMED-已领取 */
        private String status;
    }
}