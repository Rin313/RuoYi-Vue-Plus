package org.dromara.common.mybatis.handler;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.satoken.utils.LoginHelper;

import java.util.Date;

@Slf4j
public class InjectionMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            Date current = new Date();
            // strictInsertFill 默认只在字段值为 null 时填充
            this.strictInsertFill(metaObject, "createTime", Date.class, current);
            this.strictInsertFill(metaObject, "updateTime", Date.class, current);

            LoginUser loginUser = LoginHelper.getLoginUser();
            if (loginUser != null) {
                Long userId = loginUser.getUserId();
                this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            }
        } catch (Exception e) {
            throw new BizException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            // 更新时间 - 始终覆盖
            this.setFieldValByName("updateTime", new Date(), metaObject);
        } catch (Exception e) {
            throw new BizException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }
}