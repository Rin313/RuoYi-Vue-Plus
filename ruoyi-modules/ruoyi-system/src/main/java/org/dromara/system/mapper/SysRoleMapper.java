package org.dromara.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.system.domain.SysRole;
import org.dromara.system.domain.vo.SysRoleVo;

import java.util.List;

/**
 * 角色表 数据层
 *
 * @author Lion Li
 */
public interface SysRoleMapper extends BaseMapperPlus<SysRole, SysRoleVo> {

    /**
     * 分页查询角色列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 包含角色信息的分页结果
     */
    default Page<SysRoleVo> selectPageRoleList(@Param("page") Page<SysRole> page, @Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 根据条件查询角色数据
     *
     * @param queryWrapper 查询条件
     * @return 角色数据集合信息
     */
    default List<SysRoleVo> selectRoleList(@Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper) {
        return this.selectVoList(queryWrapper);
    }

    /**
     * 根据角色ID集合查询角色数量
     *
     * @param roleIds 角色ID列表
     * @return 匹配的角色数量
     */
    default long selectRoleCount(List<Long> roleIds) {
        return this.selectCount(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roleIds));
    }

    /**
     * 根据角色ID查询角色信息
     *
     * @param roleId 角色ID
     * @return 对应的角色信息
     */
    default SysRoleVo selectRoleById(Long roleId) {
        return this.selectVoById(roleId);
    }
    /**
     * 根据用户ID查询角色（包含过期时间）
     *
     * @param userId 用户ID
     * @return 角色列表（包含expire_time）
     */
    @Select("""
        SELECT 
            r.role_id,
            r.role_name,
            r.role_key,
            r.role_sort,
            r.status,
            ur.expire_time
        FROM sys_role r
        INNER JOIN sys_user_role ur ON r.role_id = ur.role_id
        WHERE ur.user_id = #{userId}
        AND (ur.expire_time IS NULL OR ur.expire_time > NOW())
        """)
    List<SysRoleVo> selectRolesByUserId(@Param("userId") Long userId);

}
