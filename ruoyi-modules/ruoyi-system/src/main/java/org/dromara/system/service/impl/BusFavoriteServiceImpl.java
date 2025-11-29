package org.dromara.system.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.bo.BusFavoriteBo;
import org.dromara.system.domain.vo.BusFavoriteVo;
import org.dromara.system.domain.BusFavorite;
import org.dromara.system.mapper.BusFavoriteMapper;
import org.dromara.system.service.IBusFavoriteService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 收藏Service业务层处理
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BusFavoriteServiceImpl implements IBusFavoriteService {

    private final BusFavoriteMapper baseMapper;

    /**
     * 查询收藏
     *
     * @param id 主键
     * @return 收藏
     */
    @Override
    public BusFavoriteVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询收藏列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 收藏分页列表
     */
    @Override
    public TableDataInfo<BusFavoriteVo> queryPageList(BusFavoriteBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BusFavorite> lqw = buildQueryWrapper(bo);
        Page<BusFavoriteVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的收藏列表
     *
     * @param bo 查询条件
     * @return 收藏列表
     */
    @Override
    public List<BusFavoriteVo> queryList(BusFavoriteBo bo) {
        LambdaQueryWrapper<BusFavorite> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BusFavorite> buildQueryWrapper(BusFavoriteBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BusFavorite> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(BusFavorite::getId);
        lqw.eq(bo.getUserId() != null, BusFavorite::getUserId, bo.getUserId());
        lqw.eq(bo.getRoomId() != null, BusFavorite::getRoomId, bo.getRoomId());
        return lqw;
    }

    /**
     * 新增收藏
     *
     * @param bo 收藏
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BusFavoriteBo bo) {
        BusFavorite add = MapstructUtils.convert(bo, BusFavorite.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改收藏
     *
     * @param bo 收藏
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BusFavoriteBo bo) {
        BusFavorite update = MapstructUtils.convert(bo, BusFavorite.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BusFavorite entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除收藏信息
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
