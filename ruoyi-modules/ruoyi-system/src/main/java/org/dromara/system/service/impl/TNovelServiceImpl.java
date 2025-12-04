package org.dromara.system.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.bean.BeanUtil;
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
import org.dromara.system.service.ITNovelService;

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
 * 小说宽Service业务层处理
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TNovelServiceImpl implements ITNovelService {

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTxtNovel(MultipartFile file, TNovelSubmitBo tNovelSubmitBo) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        // 获取小说标题（去掉.txt后缀）
        if(StringUtils.isEmpty(fileName))
            throw new ServiceException("未知标题");
        TNovel novel = new TNovel();
        BeanUtil.copyProperties(tNovelSubmitBo, novel);
        novel.setTitle(fileName.substring(0, fileName.lastIndexOf(".")));
        baseMapper.insert(novel);
        Long novelId = novel.getId();
        // 2. 读取章节内容
        List<TChapter> chapters = new ArrayList<>();
        // 强制使用 UTF-8 编码读取
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            StringBuilder currentContent = new StringBuilder();
            // 初始标题：如果第一行不是"第X章"，则前面的内容归为"序言"
            String currentTitle = "序言"; 
            long chapterIndex = 0L;
            
            boolean firstChapterFound = false;

            while ((line = reader.readLine()) != null) {
                // 判断是否是新章节标题
                if (ReUtil.isMatch(CHAPTER_PATTERN, line)) {
                    // 遇到新章节标题，保存上一章的内容
                    if (currentContent.length() > 0) {
                        // 如果是第一章之前的内容（且不为空），保存为"序言"章节
                        // 如果是第一章之后，正常保存
                        TChapter chapter = new TChapter();
                        chapter.setNovelId(novelId);
                        chapter.setTitle(currentTitle);
                        chapter.setContent(currentContent.toString());
                        // 设置排序索引
                        chapter.setChapterIndex(chapterIndex++);
                        chapters.add(chapter);
                        
                        // 清空缓冲区
                        currentContent.setLength(0);
                    }

                    // 更新当前标题 (去掉首尾空格)
                    currentTitle = line.trim();
                    firstChapterFound = true;
                } else {
                    // 不是标题，追加内容，补回换行符
                    currentContent.append(line).append("\n");
                }
            }

            // 3. 循环结束后，保存最后一章（缓存中的内容）
            if (currentContent.length() > 0) {
                // 如果全文都没有匹配到正则，说明格式不对，或者是一篇短文
                // 此时将其作为"序言"或者"正文"保存
                if (!firstChapterFound) {
                    currentTitle = "正文";
                }
                TChapter chapter = new TChapter();
                chapter.setNovelId(novelId);
                chapter.setTitle(currentTitle);
                chapter.setContent(currentContent.toString());
                chapter.setChapterIndex(chapterIndex);
                chapters.add(chapter);
            }
            if (!chapters.isEmpty()) {
                tChapterMapper.insertBatch(chapters);
            } else {
                throw new ServiceException("未解析到有效内容，请检查文件编码是否为UTF-8，或章节标题是否符合'第x章 '格式");
            }

        } catch (IOException e) {
            throw new ServiceException("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 查询小说宽
     *
     * @param id 主键
     * @return 小说宽
     */
    @Override
    public TNovelVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询小说宽列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 小说宽分页列表
     */
    @Override
    public TableDataInfo<TNovelVo> queryPageList(TNovelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TNovel> lqw = buildQueryWrapper(bo);
        Page<TNovelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的小说宽列表
     *
     * @param bo 查询条件
     * @return 小说宽列表
     */
    @Override
    public List<TNovelVo> queryList(TNovelBo bo) {
        LambdaQueryWrapper<TNovel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TNovel> buildQueryWrapper(TNovelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TNovel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(TNovel::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getTitle()), TNovel::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getAuthor()), TNovel::getAuthor, bo.getAuthor());
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), TNovel::getUrl, bo.getUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getIntro()), TNovel::getIntro, bo.getIntro());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), TNovel::getCategory, bo.getCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), TNovel::getStatus, bo.getStatus());
        lqw.eq(bo.getViewCount() != null, TNovel::getViewCount, bo.getViewCount());
        return lqw;
    }

    /**
     * 新增小说宽
     *
     * @param bo 小说宽
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(TNovelBo bo) {
        TNovel add = MapstructUtils.convert(bo, TNovel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改小说宽
     *
     * @param bo 小说宽
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(TNovelBo bo) {
        TNovel update = MapstructUtils.convert(bo, TNovel.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TNovel entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除小说宽信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public boolean addViewCount(Long id) {
        // 使用 setSql 实现原子性更新，避免并发导致的数据不一致
        int rows = baseMapper.update(null,
            new LambdaUpdateWrapper<TNovel>()
                .setSql("view_count = IFNULL(view_count, 0) + 1")
                .eq(TNovel::getId, id)
        );
        return rows > 0;
    }
}
