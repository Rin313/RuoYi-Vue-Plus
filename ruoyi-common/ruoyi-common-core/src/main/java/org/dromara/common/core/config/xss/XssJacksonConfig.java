package org.dromara.common.core.config.xss;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XssJacksonConfig {
    //JSON处理
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer xssCustomizer() {
        return builder -> {
            SimpleModule xssModule = new SimpleModule("XssProtectionModule");
            xssModule.addDeserializer(String.class, new XssStringDeserializer());
            builder.modules(xssModule);
        };
    }
    
    static class XssStringDeserializer extends JsonDeserializer<String> {
        
        // 委托给默认反序列化器，保持原有行为
        private final StringDeserializer delegate = StringDeserializer.instance;
        
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) 
                throws java.io.IOException {
            String value = delegate.deserialize(p, ctxt);
            return XssUtil.clean(value);
        }
    }
}