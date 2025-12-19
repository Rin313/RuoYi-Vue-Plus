package org.dromara.common.json.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.json.handler.BigNumberSerializer;
import org.dromara.common.json.handler.CustomDateDeserializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonConfig {

    @Bean
    public Module registerJavaTimeModule() {
        // 全局配置序列化返回 JSON 处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
        javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        javaTimeModule.addDeserializer(Date.class, new CustomDateDeserializer());
        return javaTimeModule;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            builder.timeZone(TimeZone.getDefault());//TODO，待解决时区的环境依赖，应该手动指定时区而非依赖服务器时区
            log.info("初始化 jackson 配置");
        };
    }

}
/* js中json parse的历史包袱：无法处理超过安全范围的整数，会丢精度。
方案：
前端手动处理特定参数
后端注解处理特定参数
前端在json parse前，统一用正则预处理超出安全范围的整数
后端统一将Long类型转为字符串。
*/