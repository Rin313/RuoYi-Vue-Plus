package org.dromara.system.service;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.util.ReUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.dromara.system.domain.bo.TNovelBo;
import org.dromara.system.domain.bo.TNovelSubmitBo;
import org.dromara.system.domain.vo.TNovelVo;
import org.dromara.system.domain.TChapter;
import org.dromara.system.domain.TNovel;
import org.dromara.system.mapper.TChapterMapper;
import org.dromara.system.mapper.TNovelMapper;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 小说Service业务层处理
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TNovelService {

    private final TNovelMapper baseMapper;
    private final TChapterMapper tChapterMapper;

    /**
     * 正则匹配优化：
     * ^\s* : 行首允许有空白字符
     * 第 : 必须以"第"开头
     * [0-9...]+ : 中文或阿拉伯数字
     * 章 : 必须包含"章"
     * \s : 必须包含空格（对应要求：包含"章 "）
     */
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("^\\s*第[0-9一二三四五六七八九十百千万]+章\\s.*");

    /**
     * 导入TXT小说
     *
     * @param file     文件
     * @param tNovelSubmitBo 附加信息（可选）
     */
    @Transactional(rollbackFor = Exception.class)
    public void importTxtNovel(MultipartFile file, TNovelSubmitBo tNovelSubmitBo) {
        TNovel novel = MapstructUtils.convert(tNovelSubmitBo, TNovel.class);
        validEntityBeforeSave(novel);
        baseMapper.insert(novel);
        if (ObjectUtils.isNotEmpty(file)) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".txt"))
                throw new BizException("仅支持txt格式文件");
            Long novelId = novel.getId();
            List<TChapter> chapters = new ArrayList<>();
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
                            TChapter chapter = new TChapter();
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
                    TChapter chapter = new TChapter();
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

    /**
     * 查询小说
     *
     * @param id 主键
     * @return 小说
     */
    public TNovelVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询小说列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 小说分页列表
     */
    public TableDataInfo<TNovelVo> queryPageList(TNovelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TNovel> lqw = buildQueryWrapper(bo);
        Page<TNovelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的小说列表
     *
     * @param bo 查询条件
     * @return 小说列表
     */
    public List<TNovelVo> queryList(TNovelBo bo) {
        LambdaQueryWrapper<TNovel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TNovel> buildQueryWrapper(TNovelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TNovel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(TNovel::getId);
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), TNovel::getTitle, bo.getTitle());
        lqw.like(StringUtils.isNotBlank(bo.getAuthor()), TNovel::getAuthor, bo.getAuthor());
        lqw.like(StringUtils.isNotBlank(bo.getIntro()), TNovel::getIntro, bo.getIntro());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), TNovel::getCategory, bo.getCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), TNovel::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
                TNovel::getCreateTime, params.get("beginTime"), params.get("endTime"));
        return lqw;
    }

    /**
     * 修改小说
     *
     * @param bo 小说
     * @return 是否修改成功
     */
    public void updateByBo(TNovelBo bo) {
        TNovel update = MapstructUtils.convert(bo, TNovel.class);
        validEntityBeforeSave(update);
        baseMapper.updateById(update);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TNovel entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除小说信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    public void deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        baseMapper.deleteByIds(ids);
    }

    public void addViewCount(Long id) {
        // 使用 setSql 实现原子性更新，避免并发导致的数据不一致
        baseMapper.update(null,
            new LambdaUpdateWrapper<TNovel>()
                .setSql("view_count = view_count + 1")
                .eq(TNovel::getId, id)
        );
    }
}
