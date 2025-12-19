package org.dromara.common.core.config;

//自定义的业务常量硬编码，需要即时调控的配置用config表，密钥、环境敏感变量（数据库连接、功能开关）、内置变量写yml + record + @ConfigurationProperties + @ConfigurationPropertiesScan
public class BizProperties {
    public static final Boolean autoRegister=false;
}

/*
配置方案：
@Value：运行时才报错、无智能补全
Environment：动态获取有性能损耗+手写转型+@Value所有缺点
使用@ConfigurationProperties将所有配置装到Map中：手写转型+@Value所有缺点
配置类加@ConfigurationProperties，业务配置类加@EnableConfigurationProperties：过时的写法
配置类加@ConfigurationProperties和@Component
配置类加 @ConfigurationProperties，启动类加@ConfigurationPropertiesScan

*/