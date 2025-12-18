package org.dromara.common.core.config.xss;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import java.beans.PropertyEditorSupport;

@ControllerAdvice
public class XssBinderAdvice {
    //表单/URL 参数处理
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(XssUtil.clean(text));
            }
        });
    }
}