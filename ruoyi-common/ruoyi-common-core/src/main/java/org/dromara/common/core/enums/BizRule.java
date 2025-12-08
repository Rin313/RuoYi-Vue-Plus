package org.dromara.common.core.enums;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BizRule {//要想不堆叠枚举，就不得不引入配置用的JSON，才能灵活设置签到第几天获得什么，连续签到几天获得什么之类的。
    SIGN("sign","签到",1,"DAY"),
    SIGN_STREAK("sign_steak","连续签到",1,"DAY",Map.of());
    //可记忆的业务标识
    private String bizCode;
    //业务类型
    private String bizType;
    //限制次数
    private Integer limit;
    //DAY/WEEK/MONTH/ALL/NONE
    private String limitType="NONE";
    //JSON，{"asset_name":amount,}
    private Map<String,Integer> assetRule;
    BizRule(String bizCode,String bizType,Integer limit,String limitType){
        this(bizCode,bizType,limit,limitType,null);
    }
}









// // ========== 枚举定义 ==========
// public enum ErrorCode {
//     USER_NOT_FOUND("E001"),
//     INVALID_PARAM("E002"),
//     SYSTEM_ERROR("E003");

//     private final String code;
//     private String message;      // 从数据库加载
//     private Integer httpStatus;  // 从数据库加载

//     ErrorCode(String code) {
//         this.code = code;
//     }

//     // getter/setter
//     public String getCode() { return code; }
//     public String getMessage() { return message; }
//     public Integer getHttpStatus() { return httpStatus; }

//     // ⭐ 内部初始化方法
//     void initialize(String message, Integer httpStatus) {
//         this.message = message;
//         this.httpStatus = httpStatus;
//     }
// }

// // ========== Spring 启动时初始化 ==========
// @Component
// public class ErrorCodeInitializer implements ApplicationRunner {

//     @Autowired
//     private ErrorCodeRepository repository;

//     @Override
//     public void run(ApplicationArguments args) {
//         List<ErrorCodeConfig> configs = repository.findAll();
        
//         Map<String, ErrorCodeConfig> configMap = configs.stream()
//             .collect(Collectors.toMap(ErrorCodeConfig::getCode, c -> c));

//         // 为每个枚举注入数据库配置
//         for (ErrorCode errorCode : ErrorCode.values()) {
//             ErrorCodeConfig config = configMap.get(errorCode.getCode());
//             if (config != null) {
//                 errorCode.initialize(config.getMessage(), config.getHttpStatus());
//             }
//         }
        
//         System.out.println("✅ ErrorCode 枚举初始化完成");
//     }
// }