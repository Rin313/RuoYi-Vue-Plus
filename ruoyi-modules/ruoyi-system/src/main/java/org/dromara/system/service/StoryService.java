package org.dromara.system.service;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.satoken.utils.LoginHelper;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.Story;
import org.dromara.system.domain.Novel;
import org.dromara.system.domain.Novel.Character;
import org.dromara.system.mapper.StoryMapper;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoryService {
    private final NovelService novelService;
    @Data
    @AutoMapper(target = Story.class, reverseConvertGenerate = false)
    public static class StoryInsertBo {
        @NotNull(message = "关联小说ID不能为空")
        private Long novelId;
        private Character character;
    }
    @Data
    @AutoMapper(target = Story.class, reverseConvertGenerate = false)
    public static class StoryUpdateBo {
        @NotNull(message = "主键ID不能为空")
        private Long id;
        @NotNull(message = "关联小说ID不能为空")
        private Long novelId;
        private String previousSummary;
        private List<Character> characters;
    }
    @Data
    public static class StoryQueryBo {
        @NotNull(message = "关联小说ID不能为空")
        private Long novelId;
    }
    private final StoryMapper baseMapper;
    public Long insert(StoryInsertBo bo) {
        Novel novel=novelService.getById(bo.novelId);
        List<Character> characters=ObjectUtils.defaultIfNull(novel.getCharacters(), new ArrayList<>());
        Character character=bo.getCharacter();
        Boolean isPreset=false;
        for(Character c:characters){
            if(c.getName().equals(character.getName())){//&&c.getBaseDescription().equals(character.getBaseDescription())
                c.setIsPlayer(true);
                isPreset=true;
                break;
            }
        }
        if(!isPreset){
            character.setIsPlayer(true);
            characters.add(character);
        }
        Story story=new Story();
        story.setNovelId(bo.getNovelId());
        story.setCharacters(characters);
        baseMapper.insert(story);
        return story.getId();
    }
    public void updateByBo(StoryUpdateBo bo) {
        baseMapper.updateById(MapstructUtils.convert(bo, Story.class));
    }
    public void updateById(Story story) {
        baseMapper.updateById(story);
    }
    public IPage<Story> selectPage(StoryQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Story> lqw = Wrappers.lambdaQuery();
        lqw.eq(Story::getNovelId, bo.getNovelId());
        lqw.eq(Story::getCreateBy, LoginHelper.getUserId());
        lqw.orderByDesc(Story::getUpdateTime);
        return baseMapper.selectPage(pageQuery.build(), lqw);
    }
    public Story getById(Long id){
        return baseMapper.selectById(id);
    }
    public void deleteByIds(Collection<Long> ids) {
        baseMapper.deleteByIds(ids);
    }
}
