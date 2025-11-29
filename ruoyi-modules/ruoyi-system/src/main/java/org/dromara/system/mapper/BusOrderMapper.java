package org.dromara.system.mapper;

import org.dromara.system.domain.BusOrder;
import org.dromara.system.domain.bo.BusOrderBo;
import org.dromara.system.domain.vo.BusOrderVo;
import org.springframework.data.repository.query.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 民宿订单Mapper接口
 *
 * @author Lion Li
 * @date 2025-11-22
 */
public interface BusOrderMapper extends BaseMapperPlus<BusOrder, BusOrderVo> {/**
     * 分页查询当前用户关联的订单（用户为预订人或房东）
     */
    Page<BusOrderVo> selectMyOrderPage(@Param("page") Page<BusOrderVo> page,
                                       @Param("bo") BusOrderBo bo,
                                       @Param("userId") Long userId);

    /**
     * 导出/不分页查询当前用户关联的订单
     */
    List<BusOrderVo> selectMyOrderList(@Param("bo") BusOrderBo bo,
                                       @Param("userId") Long userId);
}
