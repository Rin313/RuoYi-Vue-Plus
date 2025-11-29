package org.dromara.system.service.impl;

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
import org.dromara.system.domain.bo.BusRoomBo;
import org.dromara.system.domain.vo.BusRoomVo;
import org.dromara.system.domain.BusRoom;
import org.dromara.system.mapper.BusRoomMapper;
import org.dromara.system.service.IBusRoomService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 民宿房源Service业务层处理
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BusRoomServiceImpl implements IBusRoomService {

    private final BusRoomMapper baseMapper;

    @Override
    public TableDataInfo<BusRoomVo> queryFavoritePageList(BusRoomBo bo, PageQuery pageQuery) {
        // 1. 获取当前登录用户ID
        Long userId = LoginHelper.getUserId();

        // 2. 构建分页对象
        Page<BusRoom> page = pageQuery.build();

        // 3. 调用自定义 Mapper XML 查询
        // 注意：这里不再使用 LambdaQueryWrapper，而是直接把 bo 传给 XML 处理
        Page<BusRoomVo> result = baseMapper.selectFavoritePageList(page, bo, userId);

        return TableDataInfo.build(result);
    }

    /**
     * 查询民宿房源
     *
     * @param id 主键
     * @return 民宿房源
     */
    @Override
    public BusRoomVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询民宿房源列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 民宿房源分页列表
     */
    @Override
    public TableDataInfo<BusRoomVo> queryPageList(BusRoomBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BusRoom> lqw = buildQueryWrapper(bo);
        Page<BusRoomVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的民宿房源列表
     *
     * @param bo 查询条件
     * @return 民宿房源列表
     */
    @Override
    public List<BusRoomVo> queryList(BusRoomBo bo) {
        LambdaQueryWrapper<BusRoom> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<BusRoom> buildQueryWrapper(BusRoomBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BusRoom> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(BusRoom::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getTitle()), BusRoom::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getInfo()), BusRoom::getInfo, bo.getInfo());
        lqw.eq(bo.getPrice() != null, BusRoom::getPrice, bo.getPrice());
        lqw.eq(StringUtils.isNotBlank(bo.getImages()), BusRoom::getImages, bo.getImages());
        lqw.eq(StringUtils.isNotBlank(bo.getFacilities()), BusRoom::getFacilities, bo.getFacilities());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), BusRoom::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增民宿房源
     *
     * @param bo 民宿房源
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BusRoomBo bo) {
        BusRoom add = MapstructUtils.convert(bo, BusRoom.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改民宿房源
     *
     * @param bo 民宿房源
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BusRoomBo bo) {
        BusRoom update = MapstructUtils.convert(bo, BusRoom.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BusRoom entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除民宿房源信息
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
