package org.dromara.system.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.BizException;
import org.dromara.common.core.service.ConfigService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.ObjectUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.domain.PageQuery;
import org.dromara.common.redis.utils.CacheUtils;
import org.dromara.system.domain.SysConfig;
import org.dromara.system.domain.bo.SysConfigBo;
import org.dromara.system.domain.vo.SysConfigVo;
import org.dromara.system.mapper.SysConfigMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数配置 服务层实现
 *
 */
@RequiredArgsConstructor
@Service
public class SysConfigService implements ConfigService {

    private final SysConfigMapper baseMapper;

    /**
     * 分页查询参数配置列表
     *
     * @param config    查询条件
     * @param pageQuery 分页参数
     * @return 参数配置分页列表
     */
    public IPage<SysConfigVo> selectPageConfigList(SysConfigBo config, PageQuery pageQuery) {
        LambdaQueryWrapper<SysConfig> lqw = buildQueryWrapper(config);
        return baseMapper.selectVoPage(pageQuery.build(), lqw);
    }

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    public SysConfigVo selectConfigById(Long configId) {
        return baseMapper.selectVoById(configId);
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    @Cacheable(cacheNames = CacheNames.SYS_CONFIG, key = "#configKey")
    public String selectConfigByKey(String configKey) {
        SysConfig retConfig = baseMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
            .eq(SysConfig::getConfigKey, configKey));
        return ObjectUtils.notNullGetter(retConfig, SysConfig::getConfigValue, StringUtils.EMPTY);
    }

    /**
     * 获取注册开关
     * @return true开启，false关闭
     */
    public boolean selectRegisterEnabled() {
        String configValue = this.selectConfigByKey("sys.account.registerUser");
        return Convert.toBool(configValue);
    }

    /**
     * 查询参数配置列表
     *
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    public List<SysConfigVo> selectConfigList(SysConfigBo config) {
        LambdaQueryWrapper<SysConfig> lqw = buildQueryWrapper(config);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysConfig> buildQueryWrapper(SysConfigBo bo) {
        LambdaQueryWrapper<SysConfig> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getConfigName()), SysConfig::getConfigName, bo.getConfigName());
        lqw.like(StringUtils.isNotBlank(bo.getConfigKey()), SysConfig::getConfigKey, bo.getConfigKey());
        lqw.ge(ObjectUtils.isNotEmpty(bo.getBeginTime()), SysConfig::getCreateTime,bo.getBeginTime());
        lqw.le(ObjectUtils.isNotEmpty(bo.getEndTime()), SysConfig::getCreateTime,bo.getEndTime());
        lqw.orderByAsc(SysConfig::getConfigId);
        return lqw;
    }

    /**
     * 新增参数配置
     *
     * @param bo 参数配置信息
     * @return 结果
     */
    @CachePut(cacheNames = CacheNames.SYS_CONFIG, key = "#bo.configKey")
    public String insertConfig(SysConfigBo bo) {
        SysConfig config = MapstructUtils.convert(bo, SysConfig.class);
        baseMapper.insert(config);
        return config.getConfigValue();
    }

    /**
     * 修改参数配置
     *
     * @param bo 参数配置信息
     * @return 结果
     */
    @CachePut(cacheNames = CacheNames.SYS_CONFIG, key = "#bo.configKey")
    public String updateConfig(SysConfigBo bo) {
        SysConfig config = MapstructUtils.convert(bo, SysConfig.class);
        if (config.getConfigId() != null) {
            SysConfig temp = baseMapper.selectById(config.getConfigId());
            if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey())) {
                CacheUtils.evict(CacheNames.SYS_CONFIG, temp.getConfigKey());
            }
            baseMapper.updateById(config);
        } else {
            CacheUtils.evict(CacheNames.SYS_CONFIG, config.getConfigKey());
            baseMapper.update(config, new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, config.getConfigKey()));
        }
        return config.getConfigValue();
    }

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    public void deleteConfigByIds(List<Long> configIds) {
        List<SysConfig> list = baseMapper.selectByIds(configIds);
        list.forEach(config -> {
            CacheUtils.evict(CacheNames.SYS_CONFIG, config.getConfigKey());
        });
        baseMapper.deleteByIds(configIds);
    }

    /**
     * 重置参数缓存数据
     */
    public void resetConfigCache() {
        CacheUtils.clear(CacheNames.SYS_CONFIG);
    }

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数配置信息
     * @return 结果
     */
    public boolean checkConfigKeyUnique(SysConfigBo config) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysConfig>()
            .eq(SysConfig::getConfigKey, config.getConfigKey())
            .ne(ObjectUtil.isNotNull(config.getConfigId()), SysConfig::getConfigId, config.getConfigId()));
        return !exist;
    }

    /**
     * 根据参数 key 获取参数值
     *
     * @param configKey 参数 key
     * @return 参数值
     */
    @Override
    public String getConfigValue(String configKey) {
        return SpringUtils.getAopProxy(this).selectConfigByKey(configKey);
    }

    /**
     * 根据参数 key 获取 Map 类型的配置
     *
     * @param configKey 参数 key
     * @return Dict 对象，如果配置为空或无法解析，返回空 Dict
     */
    @Override
    public Dict getConfigMap(String configKey) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseMap(configValue);
    }

    /**
     * 根据参数 key 获取 Map 类型的配置列表
     *
     * @param configKey 参数 key
     * @return Dict 列表，如果配置为空或无法解析，返回空列表
     */
    @Override
    public List<Dict> getConfigArrayMap(String configKey) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseArrayMap(configValue);
    }

    /**
     * 根据参数 key 获取指定类型的配置对象
     *
     * @param configKey 参数 key
     * @param clazz     目标对象类型
     * @return 对象实例，如果配置为空或无法解析，返回 null
     */
    @Override
    public <T> T getConfigObject(String configKey, Class<T> clazz) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseObject(configValue, clazz);
    }

    /**
     * 根据参数 key 获取指定类型的配置列表=
     *
     * @param configKey 参数 key
     * @param clazz     目标元素类型
     * @return 指定类型列表，如果配置为空或无法解析，返回空列表
     */
    @Override
    public <T> List<T> getConfigArray(String configKey, Class<T> clazz) {
        String configValue = getConfigValue(configKey);
        return JsonUtils.parseArray(configValue, clazz);
    }

}
