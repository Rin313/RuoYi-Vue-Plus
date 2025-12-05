package org.dromara.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class R<T> implements Serializable {//只能在ResponseAdvice和全局异常处理类中使用，未验证是否会导致RepeatSubmitAspect失效

    @Serial
    private static final long serialVersionUID = 1L;
    public static final int SUCCESS = 200;
    public static final int FAIL = 500;

    private int code;

    private String msg;

    private T data;

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS,msg,data);
    }
    public static <T> R<T> ok(String msg) {
        return ok(msg,null);
    }
    public static <T> R<T> ok(T data) {
        return ok("操作成功",data);
    }
    public static <T> R<T> ok() {
        return ok("操作成功",null);
    }
    public static <T> R<T> fail(int code,String msg,T data) {
        return new R<>(code,msg,data);
    }
    public static <T> R<T> fail(String msg, T data) {
        return fail(FAIL,msg,data);
    }
    public static <T> R<T> fail(int code, String msg) {
        return fail(code,msg,null);
    }
    public static <T> R<T> fail(String msg) {
        return fail(FAIL,msg, null);
    }
    public static <T> R<T> fail(T data) {
        return fail(FAIL,"操作失败",data);
    }
    public static <T> R<T> fail() {
        return fail(FAIL,"操作失败",null);
    }
    public static <T> R<T> status(boolean flag) {
        return flag ? ok() : fail();
    }
}