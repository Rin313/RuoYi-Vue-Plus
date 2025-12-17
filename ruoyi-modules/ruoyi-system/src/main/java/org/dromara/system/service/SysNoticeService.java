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

import java.util.Arrays;

/**
 * 公告 服务层实现
 *
 */
@RequiredArgsConstructor
@Service
public class SysNoticeService {

    private final SysNoticeMapper baseMapper;

    /**
     * 分页查询通知公告列表
     *
     * @param notice    查询条件
     * @param pageQuery 分页参数
     * @return 通知公告分页列表
     */
    public IPage<SysNoticeVo> selectPageNoticeList(NoticeQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getNoticeTitle()), SysNotice::getNoticeTitle, bo.getNoticeTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeType()), SysNotice::getNoticeType, bo.getNoticeType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()),SysNotice::getStatus,bo.getStatus());
        lqw.orderByDesc(SysNotice::getCreateTime);
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }

    /**
     * 新增公告
     *
     * @param bo 公告信息
     * @return 结果
     */
    public void insertNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        baseMapper.insert(notice);
    }

    /**
     * 修改公告
     *
     * @param bo 公告信息
     * @return 结果
     */
    public int updateNotice(SysNoticeBo bo) {
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        return baseMapper.updateById(notice);
    }

    /**
     * 删除公告对象
     *
     */
    public int deleteNoticeById(Long id) {
        return baseMapper.deleteById(id);
    }

    /**
     * 批量删除公告信息
     *
     */
    public int deleteNoticeByIds(Long[] ids) {
        return baseMapper.deleteByIds(Arrays.asList(ids));
    }
}
