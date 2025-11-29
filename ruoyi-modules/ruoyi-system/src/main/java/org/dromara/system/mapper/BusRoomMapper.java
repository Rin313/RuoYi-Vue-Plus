package org.dromara.system.mapper;

import org.dromara.system.domain.BusRoom;
import org.dromara.system.domain.bo.BusRoomBo;
import org.dromara.system.domain.vo.BusRoomVo;
import org.springframework.data.repository.query.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 民宿房源Mapper接口
 *
 * @author Lion Li
 * @date 2025-11-22
 */
public interface BusRoomMapper extends BaseMapperPlus<BusRoom, BusRoomVo> {
    /**
     * 查询我的收藏列表（联表查询）
     * 
     * @param page 分页对象
     * @param bo 查询条件
     * @param userId 当前用户ID
     * @return
     */
    Page<BusRoomVo> selectFavoritePageList(@Param("page") Page<BusRoom> page, 
                                           @Param("bo") BusRoomBo bo, 
                                           @Param("userId") Long userId);

}
