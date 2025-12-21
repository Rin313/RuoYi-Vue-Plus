package org.dromara.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.system.domain.SysNotice;
import org.dromara.system.domain.bo.NoticeQueryBo;
import org.dromara.system.domain.bo.SysNoticeBo;
import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.mapper.SysNoticeMapper;
import org.springframework.stereotype.Service;
import java.util.List;


@RequiredArgsConstructor
@Service
public class SysNoticeService {

    private final SysNoticeMapper baseMapper;
    public IPage<SysNoticeVo> selectPageNoticeList(NoticeQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getNoticeTitle()), SysNotice::getNoticeTitle, bo.getNoticeTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeType()), SysNotice::getNoticeType, bo.getNoticeType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()),SysNotice::getStatus,bo.getStatus());
        lqw.orderByDesc(SysNotice::getCreateTime);
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }
    public void insertNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        baseMapper.insert(notice);
    }
    public int updateNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        return baseMapper.updateById(notice);
    }
    public int deleteNoticeById(Long id) {
        return baseMapper.deleteById(id);
    }
    public int deleteNoticeByIds(List<Long> ids) {
        return baseMapper.deleteByIds(ids);
    }
}
