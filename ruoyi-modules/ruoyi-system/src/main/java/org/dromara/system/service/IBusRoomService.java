package org.dromara.system.service;

import org.dromara.system.domain.vo.BusRoomVo;
import org.dromara.system.domain.bo.BusRoomBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 民宿房源Service接口
 *
 * @author Lion Li
 * @date 2025-11-22
 */
public interface IBusRoomService {

    /**
     * 查询我的收藏房源列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 收藏房源分页列表
     */
    TableDataInfo<BusRoomVo> queryFavoritePageList(BusRoomBo bo, PageQuery pageQuery);
    /**
     * 查询民宿房源
     *
     * @param id 主键
     * @return 民宿房源
     */
    BusRoomVo queryById(Long id);

    /**
     * 分页查询民宿房源列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 民宿房源分页列表
     */
    TableDataInfo<BusRoomVo> queryPageList(BusRoomBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的民宿房源列表
     *
     * @param bo 查询条件
     * @return 民宿房源列表
     */
    List<BusRoomVo> queryList(BusRoomBo bo);

    /**
     * 新增民宿房源
     *
     * @param bo 民宿房源
     * @return 是否新增成功
     */
    Boolean insertByBo(BusRoomBo bo);

    /**
     * 修改民宿房源
     *
     * @param bo 民宿房源
     * @return 是否修改成功
     */
    Boolean updateByBo(BusRoomBo bo);

    /**
     * 校验并批量删除民宿房源信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
