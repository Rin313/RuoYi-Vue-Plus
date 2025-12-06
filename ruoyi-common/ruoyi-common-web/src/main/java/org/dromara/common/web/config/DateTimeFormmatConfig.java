package org.dromara.common.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Configuration
public class DateTimeFormmatConfig implements WebMvcConfigurer {

    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendPattern(" HH:mm:ss").optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    @Override
    public void addFormatters(FormatterRegistry registry) {//请求参数/路径参数的LocalDateTime可以额外解析"yyyy-MM-dd"
        registry.addConverter(String.class, LocalDateTime.class, source -> {
            if (!StringUtils.hasText(source)) return null;
            String s = source.trim();
            return s.contains("T") ? LocalDateTime.parse(s) : LocalDateTime.parse(s, FLEXIBLE_FORMATTER);
        });
    }
}