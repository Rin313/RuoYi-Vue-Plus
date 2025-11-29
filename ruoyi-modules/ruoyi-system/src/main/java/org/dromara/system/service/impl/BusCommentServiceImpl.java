package org.dromara.system.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.collection.CollUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.bo.BusCommentBo;
import org.dromara.system.domain.vo.BusCommentVo;
import org.dromara.system.domain.BusComment;
import org.dromara.system.mapper.BusCommentMapper;
import org.dromara.system.mapper.BusRoomMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.IBusCommentService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collection;

/**
 * 评价Service业务层处理
 *
 * @author Lion Li
 * @date 2025-11-22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BusCommentServiceImpl implements IBusCommentService {

    private final BusCommentMapper baseMapper;
    private final BusRoomMapper busRoomMapper; // 需要注入房间Mapper
    private final SysUserMapper sysUserMapper; // 需要注入用户Mapper (系统自带)

    /**
     * 分页查询评价列表
     */
    @Override
    public TableDataInfo<BusCommentVo> queryPageList(BusCommentBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BusComment> lqw = buildQueryWrapper(bo);
        
        // === 核心逻辑 1: 增加房东权限过滤 ===
        // 如果不是管理员，只查询自己创建的房间下的评价
        // 逻辑: select * from bus_comment where room_id IN (select id from bus_room where create_by = ?)
        // if (!LoginHelper.isAdmin()) {
            Long currentUserId = LoginHelper.getUserId();
            // 使用 inSql 子查询实现过滤
            lqw.inSql(BusComment::getRoomId, 
                "SELECT id FROM bus_room WHERE create_by = " + currentUserId);
        //}

        Page<BusCommentVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        
        // === 核心逻辑 2: 填充额外字段 (房间名、用户名) ===
        this.fillExtraData(result.getRecords());

        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的评价列表 (导出等场景用)
     */
    @Override
    public List<BusCommentVo> queryList(BusCommentBo bo) {
        LambdaQueryWrapper<BusComment> lqw = buildQueryWrapper(bo);
        
        // 同样的权限过滤逻辑
        //if (!LoginHelper.isAdmin()) {
            Long currentUserId = LoginHelper.getUserId();
            lqw.inSql(BusComment::getRoomId, 
                "SELECT id FROM bus_room WHERE create_by = " + currentUserId);
        //}

        List<BusCommentVo> list = baseMapper.selectVoList(lqw);
        
        // 同样的数据填充逻辑
        this.fillExtraData(list);
        
        return list;
    }

    /**
     * 填充房间名称和用户昵称
     * 使用内存组装代替 SQL Join，避免 N+1 问题，性能更好
     */
    private void fillExtraData(List<BusCommentVo> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 1. 提取所有的 roomId 和 userId
        List<Long> roomIds = list.stream().map(BusCommentVo::getRoomId).distinct().collect(Collectors.toList());
        List<Long> userIds = list.stream().map(BusCommentVo::getUserId).distinct().collect(Collectors.toList());

        // 2. 批量查询房间信息，转为 Map<Id, Title>
        Map<Long, String> roomMap = Map.of();
        if (CollUtil.isNotEmpty(roomIds)) {
             // 假设 BusRoomMapper 继承了 BaseMapperPlus，有 selectVoBatchIds 或 selectBatchIds
             // 这里为了演示清晰，手动构建 Map
             roomMap = busRoomMapper.selectBatchIds(roomIds).stream()
                 .collect(Collectors.toMap(r -> r.getId(), r -> r.getTitle()));
        }

        // 3. 批量查询用户信息，转为 Map<Id, NickName>
        Map<Long, String> userMap = Map.of();
        if (CollUtil.isNotEmpty(userIds)) {
            userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(u -> u.getUserId(), u -> u.getNickName()));
        }

        // 4. 回填数据到 VO
        Map<Long, String> finalRoomMap = roomMap;
        Map<Long, String> finalUserMap = userMap;
        list.forEach(vo -> {
            vo.setRoomTitle(finalRoomMap.getOrDefault(vo.getRoomId(), "未知房间"));
            vo.setUserName(finalUserMap.getOrDefault(vo.getUserId(), "未知用户"));
        });
    }

    private LambdaQueryWrapper<BusComment> buildQueryWrapper(BusCommentBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<BusComment> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BusComment::getCreateTime); // 通常评价按时间倒序
        lqw.eq(bo.getUserId() != null, BusComment::getUserId, bo.getUserId());
        lqw.eq(bo.getRoomId() != null, BusComment::getRoomId, bo.getRoomId());
        lqw.eq(bo.getOrderId() != null, BusComment::getOrderId, bo.getOrderId());
        lqw.like(StringUtils.isNotBlank(bo.getContent()), BusComment::getContent, bo.getContent()); // 内容通常用模糊查询
        lqw.eq(bo.getRate() != null, BusComment::getRate, bo.getRate());
        return lqw;
    }

    /**
     * 查询评价
     *
     * @param id 主键
     * @return 评价
     */
    @Override
    public BusCommentVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    // /**
    //  * 分页查询评价列表
    //  *
    //  * @param bo        查询条件
    //  * @param pageQuery 分页参数
    //  * @return 评价分页列表
    //  */
    // @Override
    // public TableDataInfo<BusCommentVo> queryPageList(BusCommentBo bo, PageQuery pageQuery) {
    //     LambdaQueryWrapper<BusComment> lqw = buildQueryWrapper(bo);
    //     Page<BusCommentVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
    //     return TableDataInfo.build(result);
    // }

    // /**
    //  * 查询符合条件的评价列表
    //  *
    //  * @param bo 查询条件
    //  * @return 评价列表
    //  */
    // @Override
    // public List<BusCommentVo> queryList(BusCommentBo bo) {
    //     LambdaQueryWrapper<BusComment> lqw = buildQueryWrapper(bo);
    //     return baseMapper.selectVoList(lqw);
    // }

    // private LambdaQueryWrapper<BusComment> buildQueryWrapper(BusCommentBo bo) {
    //     Map<String, Object> params = bo.getParams();
    //     LambdaQueryWrapper<BusComment> lqw = Wrappers.lambdaQuery();
    //     lqw.orderByAsc(BusComment::getId);
    //     lqw.eq(bo.getUserId() != null, BusComment::getUserId, bo.getUserId());
    //     lqw.eq(bo.getRoomId() != null, BusComment::getRoomId, bo.getRoomId());
    //     lqw.eq(bo.getOrderId() != null, BusComment::getOrderId, bo.getOrderId());
    //     lqw.eq(StringUtils.isNotBlank(bo.getContent()), BusComment::getContent, bo.getContent());
    //     lqw.eq(bo.getRate() != null, BusComment::getRate, bo.getRate());
    //     return lqw;
    // }

    /**
     * 新增评价
     *
     * @param bo 评价
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(BusCommentBo bo) {
        BusComment add = MapstructUtils.convert(bo, BusComment.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改评价
     *
     * @param bo 评价
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(BusCommentBo bo) {
        BusComment update = MapstructUtils.convert(bo, BusComment.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(BusComment entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除评价信息
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
