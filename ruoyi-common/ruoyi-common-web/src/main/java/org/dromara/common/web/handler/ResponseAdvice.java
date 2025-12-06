package org.dromara.common.web.handler;

import org.dromara.common.core.domain.R;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages="org.dromara") // 指定扫描范围
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果接口返回的类型本身就是 R，那就不用再包装了，防止重复包裹
        //return !returnType.getParameterType().equals(R.class); 
        // 建议改为下面这种判断，防止 R 的子类也被漏掉
        //return !R.class.isAssignableFrom(returnType.getParameterType());
        // 这里的判断逻辑可以自定，比如加上自定义注解才包装
        // 防止 Swagger 或 Spring 自身的接口被包装 
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 1. 如果返回值是 String 类型，需要特殊处理
        // 因为 StringHttpMessageConverter 会直接返回字符串，而不会使用 Jackson 序列化
        if (body instanceof R)
            return body;
        if (body instanceof String) {
            return JsonUtils.toJsonString(R.ok(body));
        }
        if (body == null) {
            return R.ok();
        }
        return R.ok(body);
    }
}