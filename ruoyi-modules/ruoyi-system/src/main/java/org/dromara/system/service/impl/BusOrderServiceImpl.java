package org.dromara.system.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.dromara.system.domain.bo.BusOrderBo;
import org.dromara.system.domain.vo.BusOrderVo;
import org.dromara.system.domain.BusOrder;
import org.dromara.system.mapper.BusOrderMapper;
import org.dromara.system.service.IBusOrderService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 民宿订单Service业务层处理
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BusOrderServiceImpl implements IBusOrderService {

    private final BusOrderMapper baseMapper;
    /**
     * 分页查询当前登录用户关联的订单
     */
    @Override
    public TableDataInfo<BusOrderVo> queryMyOrderPage(BusOrderBo bo, PageQuery pageQuery) {
        // ruoyi-vue-plus 获取当前登录人，一般用 LoginHelper
        Long userId = LoginHelper.getUserId();
        Page<BusOrderVo> page = pageQuery.build();
        Page<BusOrderVo> result = baseMapper.selectMyOrderPage(page, bo, userId);
        return TableDataInfo.build(result);
    }
    /**
     * 接受预约 (0 -> 1)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmOrder(Long id) {
        BusOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        // 校验前置状态
        if (!"0".equals(order.getStatus())) {
            throw new ServiceException("只有[待确认]的订单才能接受预约");
        }
        
        order.setStatus("1"); // 变为已确认
        return baseMapper.updateById(order) > 0;
    }

    /**
     * 入住登记 (1 -> 2)
     * 通常登记时需要补录一些备注信息或客户需求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean registerCheckIn(BusOrderBo bo) {
        BusOrder order = baseMapper.selectById(bo.getId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        // 校验前置状态
        if (!"1".equals(order.getStatus())) {
            throw new ServiceException("只有[已确认]的订单才能办理入住登记");
        }

        order.setStatus("2"); // 变为待审核/已登记
        // 如果前端传来了新的备注，进行更新
        if (StringUtils.isNotBlank(bo.getRemark())) {
            order.setRemark(bo.getRemark());
        }
        return baseMapper.updateById(order) > 0;
    }

    /**
     * 入住审核 (2 -> 3)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean auditCheckIn(Long id) {
        BusOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        // 校验前置状态
        if (!"2".equals(order.getStatus())) {
            throw new ServiceException("只有[待审核]的订单才能通过审核");
        }

        order.setStatus("3"); // 变为已入住
        return baseMapper.updateById(order) > 0;
    }

    /**
     * 取消订单 (-> 5)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Long id) {
        BusOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 校验状态：通常只有 未入住 之前的状态可以取消
        // 假设: 0待确认, 1已确认 可以取消。2已登记(待审核) 如果想取消可能需要特殊处理，这里假设也可以。
        // 3已入住 和 4已完成 不可取消
        String status = order.getStatus();
        if ("3".equals(status) || "4".equals(status) || "5".equals(status)) {
            throw new ServiceException("当前订单状态不可取消");
        }

        order.setStatus("5"); // 变为已取消
        return baseMapper.updateById(order) > 0;
    }

    /**
     * 不分页查询当前登录用户关联的订单（导出用）
     */
    @Override
    public List<BusOrderVo> queryMyOrderList(BusOrderBo bo) {
        Long userId = LoginHelper.getUserId();
        return baseMapper.selectMyOrderList(bo, userId);
    }
    /**
     * 查询民宿订单
     *
     * @param id 主键
     * @return 民宿订单
     */
    @Override
    public BusOrderVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询民宿订单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 民宿订单分页列表
     */
    @Override
    public TableDataInfo<BusOrderVo> queryPageList(BusOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BusOrder> lqw = buildQueryWrapper(bo);
        Page<BusOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的民宿订单列表
     *
     * @param bo 查询条件
     * @return 民宿订单列表
     */
    @Override
    public List<BusOrderVo> queryList(BusOrderBo bo) {
        LambdaQueryWrapper<BusOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BusOrder> buildQueryWrapper(BusOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BusOrder> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(BusOrder::getId);
        lqw.eq(bo.getUserId() != null, BusOrder::getUserId, bo.getUserId());
        lqw.eq(bo.getRoomId() != null, BusOrder::getRoomId, bo.getRoomId());
        lqw.eq(bo.getStartDate() != null, BusOrder::getStartDate, bo.getStartDate());
        lqw.eq(bo.getEndDate() != null, BusOrder::getEndDate, bo.getEndDate());
        lqw.eq(bo.getTotalAmount() != null, BusOrder::getTotalAmount, bo.getTotalAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), BusOrder::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增民宿订单
     *
     * @param bo 民宿订单
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BusOrderBo bo) {
        BusOrder add = MapstructUtils.convert(bo, BusOrder.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改民宿订单
     *
     * @param bo 民宿订单
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BusOrderBo bo) {
        BusOrder update = MapstructUtils.convert(bo, BusOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BusOrder entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除民宿订单信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
