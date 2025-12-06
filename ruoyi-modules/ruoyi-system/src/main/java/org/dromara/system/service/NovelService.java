package org.dromara.system.service;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;

import cn.hutool.core.util.ReUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.dromara.system.domain.bo.NovelQueryBo;
import org.dromara.system.domain.bo.NovelUpdateBo;
import org.dromara.system.domain.bo.NovelVisitorQueryBo;
import org.dromara.system.domain.bo.NovelInsertBo;
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
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class NovelService {

    private final NovelMapper baseMapper;
    private final ChapterMapper tChapterMapper;

    /**
     * 正则匹配优化：
     * ^\s* : 行首允许有空白字符
     * 第 : 必须以"第"开头
     * [0-9...]+ : 中文或阿拉伯数字
     * 章 : 必须包含"章"
     * \s : 必须包含空格（对应要求：包含"章 "）
     */
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("^\\s*第[0-9一二三四五六七八九十百千万]+章\\s.*");
    @Transactional(rollbackFor = Exception.class)
    public void importTxtNovel(MultipartFile file, NovelInsertBo tNovelSubmitBo) {
        Novel novel = MapstructUtils.convert(tNovelSubmitBo, Novel.class);
        baseMapper.insert(novel);
        if (ObjectUtils.isNotEmpty(file)) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".txt"))
                throw new BizException("仅支持txt格式文件");
            Long novelId = novel.getId();
            List<Chapter> chapters = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder currentContent = new StringBuilder();
                String currentTitle = null; // 初始无标题
                long chapterIndex = 0L;
                boolean firstChapterFound = false;

                while ((line = reader.readLine()) != null) {
                    if (ReUtil.isMatch(CHAPTER_PATTERN, line)) {
                        // 遇到章节标题
                        if (firstChapterFound) {
                            // 如果已经是第一个章节之后，保存上一章
                            Chapter chapter = new Chapter();
                            chapter.setNovelId(novelId);
                            chapter.setTitle(currentTitle);
                            chapter.setContent(currentContent.toString());
                            chapter.setChapterIndex(chapterIndex++);
                            chapters.add(chapter);
                        } else {
                            // 第一次遇到章节标题，标记为已找到第一章
                            firstChapterFound = true;
                        }

                        // 更新当前章节标题
                        currentTitle = line.trim();
                        currentContent.setLength(0); // 清空内容缓冲区
                    } else {
                        // 非标题行
                        if (firstChapterFound) {
                            // 只有在第一章之后才追加内容
                            currentContent.append(line).append("\n");
                        }
                        // 如果还没找到第一章，直接丢弃此行（即抛弃序言）
                    }
                }
                // 循环结束后，处理最后一章（仅当至少有一个章节被识别）
                if (firstChapterFound && currentContent.length() > 0) {
                    Chapter chapter = new Chapter();
                    chapter.setNovelId(novelId);
                    chapter.setTitle(currentTitle);
                    chapter.setContent(currentContent.toString());
                    chapter.setChapterIndex(chapterIndex);
                    chapters.add(chapter);
                }
                // 如果没有任何有效章节，抛出异常（因为序言不再被接受）
                if (chapters.isEmpty()) {
                    throw new BizException("未解析到有效章节，请确保文件包含符合'第x章'格式的章节标题");
                }
                tChapterMapper.insertBatch(chapters);

            } catch (IOException e) {
                throw new BizException("读取文件失败: " + e.getMessage());
            }
        }
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
    public IPage<NovelVisitorVo> selectPageForVisitor(NovelVisitorQueryBo bo, PageQuery pageQuery) {
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

    public void updateByBo(NovelUpdateBo bo) {
        baseMapper.updateById(MapstructUtils.convert(bo, Novel.class));
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
