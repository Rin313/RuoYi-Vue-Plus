package org.dromara.system.service;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.bo.ChapterInsertBo;
import org.dromara.system.domain.bo.ChapterQueryBo;
import org.dromara.system.domain.bo.ChapterUpdateBo;
import org.dromara.system.domain.vo.ChapterListVo;
import org.dromara.system.domain.vo.ChapterVo;
import org.dromara.system.domain.Chapter;
import org.dromara.system.mapper.ChapterMapper;
import java.util.Collection;
@Slf4j
@RequiredArgsConstructor
@Service
public class ChapterService {
    private final ChapterMapper baseMapper;
    public ChapterVo selectById(Long id){
        return baseMapper.selectVoById(id, ChapterVo.class);
    }
    public IPage<ChapterListVo> selectPage(ChapterQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Chapter> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getNovelId() != null, Chapter::getNovelId, bo.getNovelId());
        lqw.orderByAsc(Chapter::getChapterIndex);
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }
    public void insertByBo(ChapterInsertBo bo) {
        baseMapper.insert(MapstructUtils.convert(bo, Chapter.class));
    }
    public void updateByBo(ChapterUpdateBo bo) {
        baseMapper.updateById(MapstructUtils.convert(bo, Chapter.class));
    }
    public void deleteByIds(Collection<Long> ids) {
        baseMapper.deleteByIds(ids);
    }
}
