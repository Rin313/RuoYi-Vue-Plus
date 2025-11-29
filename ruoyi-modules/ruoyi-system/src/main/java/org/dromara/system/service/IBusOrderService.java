package org.dromara.system.service;

import org.dromara.system.domain.vo.BusOrderVo;
import org.dromara.system.domain.bo.BusOrderBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 民宿订单Service接口
 *
 * @author Lion Li
 * @date 2025-11-22
 */
public interface IBusOrderService {
    TableDataInfo<BusOrderVo> queryMyOrderPage(BusOrderBo bo, PageQuery pageQuery);
    /**
     * 接受预约 (0 -> 1)
     */
    Boolean confirmOrder(Long id);

    /**
     * 入住登记 (1 -> 2)
     */
    Boolean registerCheckIn(BusOrderBo bo);

    /**
     * 入住审核 (2 -> 3)
     */
    Boolean auditCheckIn(Long id);

    /**
     * 取消订单 (-> 5)
     */
    Boolean cancelOrder(Long id);

    List<BusOrderVo> queryMyOrderList(BusOrderBo bo);
    /**
     * 查询民宿订单
     *
     * @param id 主键
     * @return 民宿订单
     */
    BusOrderVo queryById(Long id);

    /**
     * 分页查询民宿订单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 民宿订单分页列表
     */
    TableDataInfo<BusOrderVo> queryPageList(BusOrderBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的民宿订单列表
     *
     * @param bo 查询条件
     * @return 民宿订单列表
     */
    List<BusOrderVo> queryList(BusOrderBo bo);

    /**
     * 新增民宿订单
     *
     * @param bo 民宿订单
     * @return 是否新增成功
     */
    Boolean insertByBo(BusOrderBo bo);

    /**
     * 修改民宿订单
     *
     * @param bo 民宿订单
     * @return 是否修改成功
     */
    Boolean updateByBo(BusOrderBo bo);

    /**
     * 校验并批量删除民宿订单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
