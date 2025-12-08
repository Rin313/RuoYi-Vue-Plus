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
    /** 可补签日期列表 */
    private List<String> retroDates;
    /** 连续签到奖励列表 */
    private List<StreakRewardInfo> streakRewards;

    @Data
    public static class DailyRewardInfo {
        private int day;
        private Map<String, Integer> reward;
        private boolean signed;
    }

    @Data
    public static class StreakRewardInfo {
        private int days;
        private Map<String, Integer> reward;
        /** LOCKED-未达成 / CLAIMABLE-可领取 / CLAIMED-已领取 */
        private String status;
    }
}