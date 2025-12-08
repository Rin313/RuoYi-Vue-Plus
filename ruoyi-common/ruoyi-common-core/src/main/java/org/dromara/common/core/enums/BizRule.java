// package org.dromara.common.core.enums;

// import java.util.Map;

// import lombok.AllArgsConstructor;
// import lombok.Getter;

// @Getter
// @AllArgsConstructor
// public enum BizRule {
//     SIGN("sign","签到",1,"DAY"),
//     SIGN_STREAK("sign_streak","连续签到",1,"DAY"),
//     RETRO_SIGN("retro_sign","补签");
//     //可灵活根据业务需求，绑定一些id、日期、符号等，用于条件、状态的判断
//     private String bizCode;
//     //业务中文标识
//     private String bizType;
//     //限制次数
//     private Integer limit;
//     //DAY/WEEK/MONTH/ALL/NONE
//     private String limitType;
//     //JSON，{"asset_name":amount,}
//     private Map<String,Integer> assetRule;
//     BizRule(String bizCode,String bizType,Integer limit,String limitType){
//         this(bizCode,bizType,limit,limitType,null);
//     }
//     BizRule(String bizCode,String bizType){
//         this(bizCode,bizType,Integer.MAX_VALUE,"NONE",null);
//     }
// }