package org.dromara.system.service;

import org.dromara.system.domain.vo.BusFavoriteVo;
import org.dromara.system.domain.bo.BusFavoriteBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 收藏Service接口
 *
 * @author Lion Li
 * @date 2025-11-22
 */
public interface IBusFavoriteService {

    /**
     * 查询收藏
     *
     * @param id 主键
     * @return 收藏
     */
    BusFavoriteVo queryById(Long id);

    /**
     * 分页查询收藏列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 收藏分页列表
     */
    TableDataInfo<BusFavoriteVo> queryPageList(BusFavoriteBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的收藏列表
     *
     * @param bo 查询条件
     * @return 收藏列表
     */
    List<BusFavoriteVo> queryList(BusFavoriteBo bo);

    /**
     * 新增收藏
     *
     * @param bo 收藏
     * @return 是否新增成功
     */
    Boolean insertByBo(BusFavoriteBo bo);

    /**
     * 修改收藏
     *
     * @param bo 收藏
     * @return 是否修改成功
     */
    Boolean updateByBo(BusFavoriteBo bo);

    /**
     * 校验并批量删除收藏信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
