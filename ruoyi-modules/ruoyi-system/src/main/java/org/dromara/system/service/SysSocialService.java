package org.dromara.system.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.domain.SysSocial;
import org.dromara.system.domain.bo.SysSocialBo;
import org.dromara.system.domain.vo.SysSocialVo;
import org.dromara.system.mapper.SysSocialMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 社会化关系Service业务层处理
 *
 * @author thiszhc
 * @date 2023-06-12
 */
@RequiredArgsConstructor
@Service
public class SysSocialService {

    private final SysSocialMapper baseMapper;


    /**
     * 查询社会化关系
     */
    public SysSocialVo queryById(String id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 授权列表
     */
    public List<SysSocialVo> queryList(SysSocialBo bo) {
        LambdaQueryWrapper<SysSocial> lqw = new LambdaQueryWrapper<SysSocial>()
            .eq(ObjectUtil.isNotNull(bo.getUserId()), SysSocial::getUserId, bo.getUserId())
            .eq(StringUtils.isNotBlank(bo.getAuthId()), SysSocial::getAuthId, bo.getAuthId())
            .eq(StringUtils.isNotBlank(bo.getSource()), SysSocial::getSource, bo.getSource());
        return baseMapper.selectVoList(lqw);
    }

    public List<SysSocialVo> queryListByUserId(Long userId) {
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysSocial>().eq(SysSocial::getUserId, userId));
    }


    /**
     * 新增社会化关系
     */
    public Boolean insertByBo(SysSocialBo bo) {//不知道这段几把在写啥
        SysSocial add = MapstructUtils.convert(bo, SysSocial.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            if (add != null) {
                bo.setId(add.getId());
            } else {
                return false;
            }
        }
        return flag;
    }

    /**
     * 更新社会化关系
     */
    public void updateByBo(SysSocialBo bo) {
        SysSocial update = MapstructUtils.convert(bo, SysSocial.class);
        validEntityBeforeSave(update);
        baseMapper.updateById(update);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysSocial entity) {
        //做一些数据校验,如唯一约束
    }


    /**
     * 删除社会化关系
     */
    public void deleteWithValidById(Long id) {
        baseMapper.deleteById(id);
    }


    /**
     * 根据 authId 查询用户信息
     *
     * @param authId 认证id
     * @return 授权信息
     */
    public List<SysSocialVo> selectByAuthId(String authId) {
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysSocial>().eq(SysSocial::getAuthId, authId));
    }

}
