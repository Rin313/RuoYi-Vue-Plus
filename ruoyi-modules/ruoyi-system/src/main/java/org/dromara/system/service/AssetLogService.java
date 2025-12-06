package org.dromara.system.service;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.bo.AssetLogQueryBo;
import org.dromara.system.domain.bo.AssetLogRequestBo;
import org.dromara.system.domain.vo.AssetLogVo;
import org.dromara.system.domain.AssetLog;
import org.dromara.system.mapper.AssetLogMapper;
@Slf4j
@RequiredArgsConstructor
@Service
public class AssetLogService {
    private final AssetLogMapper baseMapper;
    public IPage<AssetLogVo> selectPage(AssetLogQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AssetLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getUserId() != null, AssetLog::getUserId, bo.getUserId());
        lqw.eq(bo.getAssetType() != null, AssetLog::getAssetType, bo.getAssetType());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), AssetLog::getBusinessType, bo.getBusinessType());
        lqw.eq(bo.getRefId() != null, AssetLog::getRefId, bo.getRefId());
        lqw.ge(ObjectUtils.isNotEmpty(bo.getBeginTime()), AssetLog::getCreateTime,bo.getBeginTime());
        lqw.le(ObjectUtils.isNotEmpty(bo.getEndTime()), AssetLog::getCreateTime,bo.getEndTime());
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }
    public void insertByBo(AssetLogRequestBo bo) {
        //baseMapper.insert(MapstructUtils.convert(bo, Chapter.class));
    }
}