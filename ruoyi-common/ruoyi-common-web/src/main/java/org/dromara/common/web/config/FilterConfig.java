package org.dromara.common.web.config;

import org.dromara.common.web.filter.RepeatableFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.context.annotation.Bean;

/**
 * Filter配置
 *
 */
@AutoConfiguration
public class FilterConfig {
    @Bean
    @FilterRegistration(name = "repeatableFilter", urlPatterns = "/*")
    public RepeatableFilter repeatableFilter() {
        return new RepeatableFilter();
    }

}
