package org.dromara.common.web.core;

import org.dromara.common.core.exception.BizException;

/**
 * web层通用数据处理
 *
 * @author Lion Li
 */
public class BaseController {

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected void toAjax(int rows) {
        if(rows<=0)throw new BizException();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected void toAjax(boolean result) {
        if(!result)throw new BizException();
    }

}
