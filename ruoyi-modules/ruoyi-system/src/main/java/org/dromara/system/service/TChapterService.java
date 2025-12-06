package org.dromara.system.service;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.system.domain.bo.TChapterBo;
import org.dromara.system.domain.vo.TChapterListVo;
import org.dromara.system.domain.vo.TChapterVo;
import org.dromara.system.domain.TChapter;
import org.dromara.system.mapper.TChapterMapper;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 小说章节宽Service业务层处理
 *
 * @author Lion Li
 * @date 2025-12-04
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TChapterService {

    private final TChapterMapper baseMapper;

    /**
     * 查询小说章节宽
     *
     * @param id 主键
     * @return 小说章节宽
     */
    public TChapterVo queryById(Long id){
        return baseMapper.selectVoById(id, TChapterVo.class);
    }

    /**
     * 分页查询小说章节列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 小说章节宽分页列表
     */
    public TableDataInfo<TChapterListVo> queryPageList(TChapterBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TChapter> lqw = buildQueryWrapper(bo);
        Page<TChapterListVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的小说章节列表
     *
     * @param bo 查询条件
     * @return 小说章节宽列表
     */
    public List<TChapterListVo> queryList(TChapterBo bo) {
        LambdaQueryWrapper<TChapter> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TChapter> buildQueryWrapper(TChapterBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TChapter> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(TChapter::getChapterIndex);
        lqw.eq(bo.getNovelId() != null, TChapter::getNovelId, bo.getNovelId());
        return lqw;
    }

    /**
     * 新增小说章节宽
     *
     * @param bo 小说章节宽
     * @return 是否新增成功
     */
    public void insertByBo(TChapterBo bo) {
        TChapter add = MapstructUtils.convert(bo, TChapter.class);
        validEntityBeforeSave(add);
        baseMapper.insert(add);
    }

    /**
     * 修改小说章节宽
     *
     * @param bo 小说章节宽
     * @return 是否修改成功
     */
    public void updateByBo(TChapterBo bo) {
        TChapter update = MapstructUtils.convert(bo, TChapter.class);
        validEntityBeforeSave(update);
        baseMapper.updateById(update);
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TChapter entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除小说章节宽信息
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
}
