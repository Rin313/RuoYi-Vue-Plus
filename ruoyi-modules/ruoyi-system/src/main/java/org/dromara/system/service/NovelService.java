package org.dromara.system.service;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.BizException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.dromara.system.domain.vo.NovelVisitorVo;
import org.dromara.system.domain.vo.NovelVo;
import org.dromara.system.domain.Chapter;
import org.dromara.system.domain.Novel;
import org.dromara.system.mapper.ChapterMapper;
import org.dromara.system.mapper.NovelMapper;

import java.util.List;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class NovelService {
    @Data
    @AutoMapper(target = Novel.class, reverseConvertGenerate = false)
    public static class NovelInsertBo {
        @NotBlank(message = "标题不能为空")
        private String title;
        private String author;
        private String intro;
        private String category;
        /**
         * 封面
         */
        private String url;
        /**
         * 0-正常 1-停用
         */
        private String status;
    }
    @Data
    @AutoMapper(target = Novel.class, reverseConvertGenerate = false)
    public static class NovelUpdateBo {
        @NotNull(message = "主键ID不能为空")
        private Long id;
        @NotBlank(message = "标题不能为空")
        private String title;
        private String author;
        private String url;
        private String intro;
        private String category;
        private String status;
    }
    @Data
    public static class NovelQueryBo {
        private String title;
        private String author;
        private String intro;
        private String category;
        private String status;
        private LocalDateTime beginTime;
        private LocalDateTime endTime;
    }
    @Transactional(rollbackFor = Exception.class)
    public void importNovel(MultipartFile img, NovelInsertBo tNovelSubmitBo) {
        Novel novel = MapstructUtils.convert(tNovelSubmitBo, Novel.class);
        baseMapper.insert(novel);
        //if (ObjectUtils.isNotEmpty(img)) {
            // String originalFilename = file.getOriginalFilename();
            //Long novelId = novel.getId();
        //}
    }
    private final NovelMapper baseMapper;
    public void updateByBo(NovelUpdateBo bo) {
        baseMapper.updateById(MapstructUtils.convert(bo, Novel.class));
    }
    public NovelVo selectById(Long id){
        return baseMapper.selectVoById(id);
    }
    public IPage<NovelVo> selectPage(NovelQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Novel> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), Novel::getTitle, bo.getTitle());
        lqw.like(StringUtils.isNotBlank(bo.getAuthor()), Novel::getAuthor, bo.getAuthor());
        lqw.like(StringUtils.isNotBlank(bo.getIntro()), Novel::getIntro, bo.getIntro());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), Novel::getCategory, bo.getCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Novel::getStatus, bo.getStatus());
        lqw.ge(ObjectUtils.isNotEmpty(bo.getBeginTime()), Novel::getCreateTime,bo.getBeginTime());
        lqw.le(ObjectUtils.isNotEmpty(bo.getEndTime()), Novel::getCreateTime,bo.getEndTime());
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }
    public IPage<NovelVisitorVo> selectPageForVisitor(NovelQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Novel> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), Novel::getTitle, bo.getTitle());
        lqw.like(StringUtils.isNotBlank(bo.getAuthor()), Novel::getAuthor, bo.getAuthor());
        lqw.like(StringUtils.isNotBlank(bo.getIntro()), Novel::getIntro, bo.getIntro());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), Novel::getCategory, bo.getCategory());
        lqw.eq(Novel::getStatus, "0");
        lqw.ge(ObjectUtils.isNotEmpty(bo.getBeginTime()), Novel::getCreateTime,bo.getBeginTime());
        lqw.le(ObjectUtils.isNotEmpty(bo.getEndTime()), Novel::getCreateTime,bo.getEndTime());
        return baseMapper.selectVoPage(pageQuery.build(), lqw , NovelVisitorVo.class);
    }
    public void deleteByIds(Collection<Long> ids) {
        baseMapper.deleteByIds(ids);
    }
    public void addViewCount(Long id) {
        // 使用 setSql 实现原子性更新，避免并发导致的数据不一致
        baseMapper.update(null,
            new LambdaUpdateWrapper<Novel>()
                .setSql("view_count = view_count + 1")
                .eq(Novel::getId, id)
        );
    }
}
