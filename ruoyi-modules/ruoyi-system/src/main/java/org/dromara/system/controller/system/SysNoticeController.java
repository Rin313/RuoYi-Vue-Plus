package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BizType;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.system.domain.bo.NoticeQueryBo;
import org.dromara.system.domain.bo.SysNoticeBo;
import org.dromara.system.domain.vo.SysNoticeVo;
import org.dromara.system.service.SysNoticeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 公告 信息操作处理
 *
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController {

    private final SysNoticeService noticeService;
    @SaIgnore
    @GetMapping("/list/visitor")
    public IPage<SysNoticeVo> listForVisitor(PageQuery pageQuery) {
        NoticeQueryBo bo=new NoticeQueryBo();
        bo.setNoticeType("2");
        bo.setStatus("0");
        return noticeService.selectPageNoticeList(bo, pageQuery);
    }

    /**
     * 获取通知公告列表
     */
    @SaCheckPermission("system:notice:list")
    @GetMapping("/list")
    public IPage<SysNoticeVo> list(NoticeQueryBo notice, PageQuery pageQuery) {
        notice.setNoticeType("2");
        return noticeService.selectPageNoticeList(notice, pageQuery);
    }

    /**
     * 新增通知公告
     */
    @SaCheckPermission("system:notice:add")
    @Log(title = "通知公告", businessType = BizType.INSERT)
    @PostMapping("insert")
    public void add(@Validated @RequestBody SysNoticeBo notice) {
        notice.setNoticeType("2");
        noticeService.insertNotice(notice);
    }

    /**
     * 修改通知公告
     */
    @SaCheckPermission("system:notice:edit")
    @Log(title = "通知公告", businessType = BizType.UPDATE)
    @PostMapping("update")
    public void update(@Validated @RequestBody SysNoticeBo notice) {
        noticeService.updateNotice(notice);
    }

    /**
     * 删除通知公告
     *
     */
    @SaCheckPermission("system:notice:remove")
    @Log(title = "通知公告", businessType = BizType.DELETE)
    @PostMapping("/{ids}")
    public void delete(@PathVariable List<Long> ids) {
        noticeService.deleteNoticeByIds(ids);
    }
}