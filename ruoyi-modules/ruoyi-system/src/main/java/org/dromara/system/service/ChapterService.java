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

import java.util.List;
import java.util.Collection;

/**
 * 小说章节Service业务层处理
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChapterService {

    private final ChapterMapper baseMapper;

    /**
     * 查询小说章节
     *
     * @param id 主键
     * @return 小说章节
     */
    public ChapterVo queryById(Long id){
        return baseMapper.selectVoById(id, ChapterVo.class);
    }

    /**
     * 分页查询小说章节列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 小说章节分页列表
     */
    public IPage<ChapterListVo> queryPageList(ChapterQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Chapter> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }

    // /**
    //  * 查询符合条件的小说章节列表
    //  *
    //  * @param bo 查询条件
    //  * @return 小说章节列表
    //  */
    // public List<ChapterListVo> queryList(ChapterBo bo) {
    //     LambdaQueryWrapper<Chapter> lqw = buildQueryWrapper(bo);
    //     return baseMapper.selectVoList(lqw);
    // }

    private LambdaQueryWrapper<Chapter> buildQueryWrapper(ChapterQueryBo bo) {
        LambdaQueryWrapper<Chapter> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getNovelId() != null, Chapter::getNovelId, bo.getNovelId());
        lqw.orderByAsc(Chapter::getChapterIndex);
        return lqw;
    }

    /**
     * 新增小说章节
     *
     * @param bo 小说章节
     * @return 是否新增成功
     */
    public void insertByBo(ChapterInsertBo bo) {
        baseMapper.insert(MapstructUtils.convert(bo, Chapter.class));
    }

    /**
     * 修改小说章节
     *
     * @param bo 小说章节
     * @return 是否修改成功
     */
    public void updateByBo(ChapterUpdateBo bo) {
        baseMapper.updateById(MapstructUtils.convert(bo, Chapter.class));
    }
    /**
     * 校验并批量删除小说章节信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    public void deleteWithValidByIds(Collection<Long> ids) {
        baseMapper.deleteByIds(ids);
    }
}
