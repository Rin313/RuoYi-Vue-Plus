package org.dromara.system.service;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.bo.ChapterInsertBo;
import org.dromara.system.domain.bo.ChapterUpdateBo;
import org.dromara.system.domain.Chapter;
import org.dromara.system.domain.ChatMessage;
import org.dromara.system.mapper.ChapterMapper;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Service
public class ChapterService {
    @Data
    @AutoMapper(target = Chapter.class)
    public static class ChapterVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long id;
        private Long storyId;
        private String title;
        private Long chapterIndex;
    }
    @Data
    public class ChapterQueryBo {
        @NotNull(message = "关联小说ID不能为空")
        private Long storyId;
    }
    private final ChapterMapper baseMapper;
    public IPage<ChapterVo> selectPage(ChapterQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Chapter> lqw = Wrappers.lambdaQuery();
        lqw.eq(Chapter::getStoryId, bo.getStoryId());
        lqw.orderByAsc(Chapter::getChapterIndex);
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }
    public Chapter getById(Long id){
        return baseMapper.selectById(id);
    }
    public void insert(ChapterInsertBo bo) {
        baseMapper.insert(MapstructUtils.convert(bo, Chapter.class));
    }
    public void update(ChapterUpdateBo bo) {
        baseMapper.updateById(MapstructUtils.convert(bo, Chapter.class));
    }
    public void deleteByIds(Collection<Long> ids) {
        baseMapper.deleteByIds(ids);
    }
    public void addMessages(Chapter chapter,ChatMessage userMessage,ChatMessage modelMessage){
        List<ChatMessage> msgs=ObjectUtils.defaultIfNull(chapter.getChatHistory(), new ArrayList<>());
        msgs.add(userMessage);
        msgs.add(modelMessage);
        baseMapper.updateById(chapter);
    }
}
