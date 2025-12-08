* 当需要确认“是否有数据被实际修改”来决定下一步流程（如乐观锁、扣库存），才使用int返回值(受影响的行数)，insert和常规的update/delete一般直接调用方法。
* 只使用分页查询（具体说明待详写）


你的问题非常典型，反映了资产系统设计中常见的困惑。我来为你提供一个优雅的解决方案。

## 核心问题分析

你的设计思路正确，但缺少了**资产账户表**和**业务规则解耦**机制。让我们从架构层面重新思考。

## 优化方案：三层架构设计

### 1. **资产账户表**（必须先有账户才能记录流水）

```sql
CREATE TABLE `asset_account` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `asset_type` int NOT NULL COMMENT '资产类型（1:积分, 2:硬币, ...）',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '当前余额',
  `frozen_balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '冻结余额',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_asset` (`user_id`,`asset_type`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='虚拟资产账户表';
```

### 2. **优化后的流水表**

```sql
CREATE TABLE `asset_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `transaction_no` varchar(32) NOT NULL COMMENT '交易流水号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `asset_type` int NOT NULL COMMENT '资产类型',
  `amount` decimal(10,2) NOT NULL COMMENT '变动数量',
  `transaction_type` tinyint NOT NULL COMMENT '交易类型（1:收入, 2:支出, 3:冻结, 4:解冻）',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型枚举',
  `business_id` varchar(64) NOT NULL COMMENT '业务唯一标识（格式：模块_业务ID）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1:成功, 2:失败, 3:处理中）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `ext_data` json DEFAULT NULL COMMENT '扩展数据（存储业务特定信息）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_no` (`transaction_no`),
  UNIQUE KEY `uk_business` (`business_type`,`business_id`) COMMENT '业务幂等性约束',
  KEY `idx_user_time` (`user_id`,`create_time`),
  KEY `idx_business_info` (`business_type`,`business_id`)
) ENGINE=InnoDB COMMENT='资产交易流水表';
```

### 3. **业务规则配置表**（核心解耦设计）

```sql
CREATE TABLE `business_rule_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `module_type` varchar(30) NOT NULL COMMENT '模块类型（task,daily,growth,consume...）',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型',
  `asset_type` int NOT NULL COMMENT '资产类型',
  `amount` decimal(10,2) NOT NULL COMMENT '奖励/消耗金额',
  `limit_type` tinyint DEFAULT NULL COMMENT '限制类型（1:每日,2:每周,3:每月,4:永久,5:累计）',
  `limit_times` int DEFAULT NULL COMMENT '限制次数',
  `condition_config` json DEFAULT NULL COMMENT '条件配置（JSON格式）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1:启用,0:禁用）',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`),
  KEY `idx_business_type` (`business_type`)
) ENGINE=InnoDB COMMENT='业务规则配置表';
```

## 核心架构设计

### **服务分层设计**

```
┌─────────────────────────────────────────────────────┐
│                业务模块（签到、任务、消费）               │
└──────────────────────────┬──────────────────────────┘
                           │ 调用资产服务接口
                           ▼
┌─────────────────────────────────────────────────────┐
│             资产服务层（AssetService）                │
│  ├─ 资产账户管理                                      │
│  ├─ 交易处理（支持收入/支出/冻结）                     │
│  └─ 业务规则验证                                      │
└───────────────┬─────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────┐
│              规则引擎层（RuleEngine）                 │
│  ├─ 频次限制检查（每日/每周/每月）                     │
│  ├─ 条件验证（阅读时长、分享次数等）                   │
│  └─ 奖励计算                                         │
└───────────────┬─────────────────────────────────────┘
                │ 查询配置
                ▼
┌─────────────────────────────────────────────────────┐
│              配置层（配置表+缓存）                    │
└─────────────────────────────────────────────────────┘
```

### **核心资产服务示例代码**

```java
@Service
public class AssetService {
  
    @Autowired
    private AssetAccountMapper accountMapper;
  
    @Autowired
    private AssetTransactionMapper transactionMapper;
  
    @Autowired
    private BusinessRuleService ruleService;
  
    /**
     * 统一资产变动接口
     */
    @Transactional(rollbackFor = Exception.class)
    public AssetTransactionDTO changeAsset(AssetChangeRequest request) {
        // 1. 参数验证
        validateRequest(request);
      
        // 2. 生成幂等性ID
        String transactionNo = generateTransactionNo(request);
      
        // 3. 检查是否已处理（幂等性）
        AssetTransaction exist = transactionMapper.selectByTransactionNo(transactionNo);
        if (exist != null) {
            return convertToDTO(exist);
        }
      
        // 4. 获取业务规则
        BusinessRule rule = ruleService.getRule(request.getBusinessType());
        if (rule == null) {
            throw new BusinessException("业务规则未配置");
        }
      
        // 5. 验证业务规则（频次限制、条件等）
        ruleService.validateRule(rule, request.getUserId(), request.getExtParams());
      
        // 6. 资产变动（使用乐观锁）
        AssetAccount account = getOrCreateAccount(request.getUserId(), rule.getAssetType());
      
        BigDecimal newBalance;
        if (rule.getChangeType() == ChangeType.INCOME) {
            newBalance = account.getBalance().add(rule.getAmount());
        } else {
            newBalance = account.getBalance().subtract(rule.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("余额不足");
            }
        }
      
        int updated = accountMapper.updateBalance(
            account.getId(), 
            newBalance, 
            account.getVersion()
        );
      
        if (updated == 0) {
            throw new ConcurrentException("资产变更冲突，请重试");
        }
      
        // 7. 记录流水
        AssetTransaction transaction = createTransaction(request, transactionNo, rule, account);
        transactionMapper.insert(transaction);
      
        // 8. 发布资产变动事件（异步处理后续逻辑）
        eventPublisher.publishEvent(new AssetChangedEvent(transaction));
      
        return convertToDTO(transaction);
    }
  
    /**
     * 查询用户资产
     */
    public List<AssetAccountDTO> getUserAssets(Long userId) {
        return accountMapper.selectByUserId(userId);
    }
  
    /**
     * 资产消费（解锁章节等）
     */
    public AssetTransactionDTO consumeAsset(AssetConsumeRequest request) {
        AssetChangeRequest changeRequest = new AssetChangeRequest();
        changeRequest.setUserId(request.getUserId());
        changeRequest.setBusinessType("CONSUME_" + request.getModule());
        changeRequest.setExtParams(Map.of(
            "targetId", request.getTargetId(),
            "targetType", request.getTargetType()
        ));
        return changeAsset(changeRequest);
    }
}
```

### **规则引擎服务**

```java
@Service
public class BusinessRuleService {
  
    @Autowired
    private BusinessRuleConfigMapper ruleConfigMapper;
  
    private Map<String, BusinessRule> ruleCache = new ConcurrentHashMap<>();
  
    /**
     * 获取业务规则
     */
    public BusinessRule getRule(String businessType) {
        return ruleCache.computeIfAbsent(businessType, key -> {
            BusinessRuleConfig config = ruleConfigMapper.selectByBusinessType(businessType);
            if (config == null) return null;
            return convertToRule(config);
        });
    }
  
    /**
     * 验证业务规则
     */
    public void validateRule(BusinessRule rule, Long userId, Map<String, Object> extParams) {
        // 1. 基础状态验证
        if (!rule.isActive()) {
            throw new BusinessException("该业务暂不可用");
        }
      
        // 2. 频次限制验证
        if (rule.getLimitType() != null) {
            Integer count = queryUserBusinessCount(userId, rule.getBusinessType(), rule.getLimitType());
            if (count >= rule.getLimitTimes()) {
                throw new BusinessException("今日次数已用完");
            }
        }
      
        // 3. 自定义条件验证
        if (rule.getConditions() != null) {
            validateCustomConditions(rule.getConditions(), userId, extParams);
        }
    }
  
    /**
     * 自定义条件验证（可扩展）
     */
    private void validateCustomConditions(RuleCondition condition, Long userId, Map<String, Object> extParams) {
        switch (condition.getType()) {
            case "READING_TIME":
                validateReadingTime(userId, condition.getThreshold());
                break;
            case "SHARE_COUNT":
                validateShareCount(userId, condition.getThreshold(), condition.getPeriod());
                break;
            case "LEVEL":
                validateUserLevel(userId, condition.getThreshold());
                break;
            default:
                // 可扩展其他条件
                break;
        }
    }
}
```

### **业务模块调用示例**

```java
@RestController
@RequestMapping("/api/task")
public class TaskController {
  
    @Autowired
    private AssetService assetService;
  
    /**
     * 签到
     */
    @PostMapping("/signin")
    public ApiResponse signIn(@RequestParam Long userId) {
        AssetChangeRequest request = new AssetChangeRequest();
        request.setUserId(userId);
        request.setBusinessType("DAILY_SIGNIN");
      
        AssetTransactionDTO result = assetService.changeAsset(request);
      
        return ApiResponse.success(result);
    }
  
    /**
     * 完成阅读任务
     */
    @PostMapping("/reading/complete")
    public ApiResponse completeReading(@RequestParam Long userId, 
                                      @RequestParam Integer minutes) {
        AssetChangeRequest request = new AssetChangeRequest();
        request.setUserId(userId);
        request.setBusinessType("TASK_READING");
        request.setExtParams(Map.of("readingMinutes", minutes));
      
        AssetTransactionDTO result = assetService.changeAsset(request);
      
        return ApiResponse.success(result);
    }
}

@RestController
@RequestMapping("/api/consume")
public class ConsumeController {
  
    @Autowired
    private AssetService assetService;
  
    /**
     * 解锁小说章节
     */
    @PostMapping("/novel/chapter/unlock")
    public ApiResponse unlockChapter(@RequestParam Long userId, 
                                    @RequestParam Long chapterId) {
        AssetConsumeRequest request = new AssetConsumeRequest();
        request.setUserId(userId);
        request.setModule("NOVEL");
        request.setTargetId(chapterId);
        request.setTargetType("CHAPTER");
      
        AssetTransactionDTO result = assetService.consumeAsset(request);
      
        return ApiResponse.success(result);
    }
}
```

## 配置示例

### **业务规则配置数据**

```sql
-- 签到规则
INSERT INTO business_rule_config 
(rule_code, rule_name, module_type, business_type, asset_type, amount, limit_type, limit_times, condition_config)
VALUES 
('SIGNIN_DAILY', '每日签到', 'daily', 'DAILY_SIGNIN', 1, 10.00, 1, 1, NULL),

-- 阅读任务
('READING_30MIN', '阅读30分钟', 'growth', 'TASK_READING', 1, 50.00, 1, 3, 
 '{"type":"READING_TIME","threshold":30,"operator":"gte"}'),

-- 分享任务
('SHARE_CONTENT', '分享内容', 'growth', 'TASK_SHARE', 2, 5.00, 1, 2,
 '{"type":"SHARE_COUNT","threshold":1,"period":"daily"}'),

-- 章节解锁消费
('CHAPTER_UNLOCK', '章节解锁', 'consume', 'CONSUME_NOVEL', 2, -10.00, 5, NULL, NULL);
```

## 关键设计原则

### 1. **解耦原则**

- 业务模块不直接操作资产，通过服务接口调用
- 规则可配置，新增业务只需添加配置，无需修改代码

### 2. **幂等性原则**

- 每个业务操作有唯一标识，避免重复奖励
- 支持重试，保证数据一致性

### 3. **扩展性原则**

- 通过 `ext_data` JSON字段存储业务特定信息
- 规则引擎可扩展新的验证条件

### 4. **性能优化**

- 账户表避免每次都从流水统计余额
- 使用缓存存储规则配置
- 流水表按时间分区存储

## 部署建议

1. **初始阶段**：使用上述3张表即可支持大部分场景
2. **扩展阶段**：可添加 `user_achievement`（用户成就记录）和 `asset_exchange_rate`（资产兑换比例）
3. **监控**：记录资产变动大盘，设置异常告警

这个方案的核心优势在于：

- **表结构精简**：只需3张核心表
- **开发友好**：新增业务只需配置，无需编码
- **维护简单**：所有规则可视化配置
- **扩展性强**：支持未来新业务快速接入

需要根据你的具体业务场景调整细节，但这个架构应该能解决你当前的困惑。
