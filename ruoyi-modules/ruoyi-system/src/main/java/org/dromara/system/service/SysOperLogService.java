package org.dromara.system.service;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.ObjectUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.log.event.OperLogEvent;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.system.domain.SysOperLog;
import org.dromara.system.domain.bo.SysOperLogBo;
import org.dromara.system.domain.vo.SysOperLogVo;
import org.dromara.system.mapper.SysOperLogMapper;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 操作日志 服务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysOperLogService {

    private final SysOperLogMapper baseMapper;

    /**
     * 操作日志记录
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        SysOperLogBo operLog = MapstructUtils.convert(operLogEvent, SysOperLogBo.class);
        insertOperlog(operLog);
    }

    /**
     * 分页查询操作日志列表
     *
     * @param operLog   查询条件
     * @param pageQuery 分页参数
     * @return 操作日志分页列表
     */
    public IPage<SysOperLogVo> selectPageOperLogList(SysOperLogBo operLog, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOperLog> lqw = buildQueryWrapper(operLog);
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            lqw.orderByDesc(SysOperLog::getOperId);
        }
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }

    private LambdaQueryWrapper<SysOperLog> buildQueryWrapper(SysOperLogBo operLog) {
        return new LambdaQueryWrapper<SysOperLog>()
            .like(StringUtils.isNotBlank(operLog.getOperIp()), SysOperLog::getOperIp, operLog.getOperIp())
            .like(StringUtils.isNotBlank(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
            .eq(operLog.getBusinessType() != null && operLog.getBusinessType() > 0,
                SysOperLog::getBusinessType, operLog.getBusinessType())
            .func(f -> {
                if (ArrayUtil.isNotEmpty(operLog.getBusinessTypes())) {
                    f.in(SysOperLog::getBusinessType, Arrays.asList(operLog.getBusinessTypes()));
                }
            })
            .eq(operLog.getStatus() != null,
                SysOperLog::getStatus, operLog.getStatus())
            .like(StringUtils.isNotBlank(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
            .ge(ObjectUtils.isNotEmpty(operLog.getBeginTime()), SysOperLog::getOperTime,operLog.getBeginTime())
            .le(ObjectUtils.isNotEmpty(operLog.getEndTime()), SysOperLog::getOperTime,operLog.getEndTime());
    }

    /**
     * 新增操作日志
     *
     * @param bo 操作日志对象
     */
    public void insertOperlog(SysOperLogBo bo) {
        SysOperLog operLog = MapstructUtils.convert(bo, SysOperLog.class);
        operLog.setOperTime(new Date());
        baseMapper.insert(operLog);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    public List<SysOperLogVo> selectOperLogList(SysOperLogBo operLog) {
        LambdaQueryWrapper<SysOperLog> lqw = buildQueryWrapper(operLog);
        return baseMapper.selectVoList(lqw.orderByDesc(SysOperLog::getOperId));
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    public SysOperLogVo selectOperLogById(Long operId) {
        return baseMapper.selectVoById(operId);
    }
}
