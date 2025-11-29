package org.dromara.system.service;

import org.dromara.system.domain.vo.BusCommentVo;
import org.dromara.system.domain.bo.BusCommentBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 评价Service接口
 *
 * @author Lion Li
 * @date 2025-11-22
 */
public interface IBusCommentService {

    /**
     * 查询评价
     *
     * @param id 主键
     * @return 评价
     */
    BusCommentVo queryById(Long id);

    /**
     * 分页查询评价列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 评价分页列表
     */
    TableDataInfo<BusCommentVo> queryPageList(BusCommentBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的评价列表
     *
     * @param bo 查询条件
     * @return 评价列表
     */
    List<BusCommentVo> queryList(BusCommentBo bo);

    /**
     * 新增评价
     *
     * @param bo 评价
     * @return 是否新增成功
     */
    Boolean insertByBo(BusCommentBo bo);

    /**
     * 修改评价
     *
     * @param bo 评价
     * @return 是否修改成功
     */
    Boolean updateByBo(BusCommentBo bo);

    /**
     * 校验并批量删除评价信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
