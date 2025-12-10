package org.dromara.system.service;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.ObjectUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import org.dromara.system.domain.bo.ActionInsertBo;
import org.dromara.system.domain.bo.ActionQueryBo;
import org.dromara.system.domain.bo.ActionUpdateBo;
import org.dromara.system.domain.vo.ActionVo;
import org.dromara.system.domain.Action;
import org.dromara.system.mapper.ActionMapper;
import java.util.Collection;
@Slf4j
@RequiredArgsConstructor
@Service
public class ActionService {
    private final ActionMapper baseMapper;
    public ActionVo selectById(Long id){
        return baseMapper.selectVoById(id, ActionVo.class);
    }
    public IPage<ActionVo> selectPage(ActionQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Action> lqw = Wrappers.lambdaQuery();
        lqw.eq(Action::getTargetType, bo.getTargetType());
        lqw.eq(Action::getTargetId, bo.getTargetId());
        lqw.eq(ObjectUtils.isNotNull(bo.getActionType()),Action::getActionType,bo.getActionType());
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }
    public void insertByBo(ActionInsertBo bo) {
        Action action=MapstructUtils.convert(bo, Action.class);
        action.setUsername(LoginHelper.getLoginUser().getNickname());
        baseMapper.insert(action);
    }
    public void updateByBo(ActionUpdateBo bo) {
        baseMapper.updateById(MapstructUtils.convert(bo, Action.class));
    }
    public void deleteByIds(Collection<Long> ids) {
        baseMapper.deleteByIds(ids);
    }
}
