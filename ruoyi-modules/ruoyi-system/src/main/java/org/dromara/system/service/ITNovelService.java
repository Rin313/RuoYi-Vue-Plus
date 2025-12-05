package org.dromara.system.service;

import org.dromara.system.domain.vo.TNovelVo;
import org.springframework.web.multipart.MultipartFile;
import org.dromara.system.domain.bo.TNovelBo;
import org.dromara.system.domain.bo.TNovelSubmitBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 小说宽Service接口
 *
 * @author Lion Li
 * @date 2025-12-04
 */
public interface ITNovelService {
    /**
     * 查询小说宽
     *
     * @param id 主键
     * @return 小说宽
     */
    TNovelVo queryById(Long id);

    /**
     * 分页查询小说宽列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 小说宽分页列表
     */
    TableDataInfo<TNovelVo> queryPageList(TNovelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的小说宽列表
     *
     * @param bo 查询条件
     * @return 小说宽列表
     */
    List<TNovelVo> queryList(TNovelBo bo);

    // /**
    //  * 新增小说宽
    //  *
    //  * @param bo 小说宽
    //  * @return 是否新增成功
    //  */
    // Boolean insertByBo(TNovelBo bo);

    /**
     * 修改小说宽
     *
     * @param bo 小说宽
     * @return 是否修改成功
     */
    Boolean updateByBo(TNovelBo bo);

    /**
     * 校验并批量删除小说宽信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 导入TXT小说
     *
     * @param file     文件
     * @param tNovelSubmitBo 附加信息（可选）
     */
    void importTxtNovel(MultipartFile file, TNovelSubmitBo tNovelSubmitBo);

    boolean addViewCount(Long id);
}
