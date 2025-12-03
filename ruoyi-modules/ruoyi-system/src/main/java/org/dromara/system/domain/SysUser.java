package org.dromara.system.domain;

import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * 用户信息宽对象 sys_user
 *
 * @author Lion Li
 * @date 2025-12-03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机
     */
    private String phonenumber;

    /**
     * 性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 头像
     */
    private Long avatar;

    /**
     * 密码
     */
    @TableField(
        insertStrategy = FieldStrategy.NOT_EMPTY,
        updateStrategy = FieldStrategy.NOT_EMPTY,
        whereStrategy = FieldStrategy.NOT_EMPTY
    )
    private String password;

    /**
     * 状态(0正常 1停用)
     */
    private String status;

    /**
     * 删除标志
     */
    @TableLogic
    private String delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 本人邀请码(6位)
     */
    private String inviteCode;

    /**
     * 邀请人
     */
    private Long parentId;

    /**
     * 现金余额
     */
    private Long balance;

    /**
     * 佣金余额
     */
    private Long commissionBalance;

    /**
     * 书币余额
     */
    private Long coinBalance;

    /**
     * 积分余额
     */
    private Long pointBalance;

    /**
     * 连续签到天数
     */
    private Long signinContinuousDays;

    /**
     * 上次签到时间
     */
    private Date lastSignin;

    /**
     * 总分享次数
     */
    private Long totalShareCount;

    /**
     * 上次分享时间
     */
    private Date lastShare;

    public SysUser(Long userId) {
        this.userId = userId;
    }

    public boolean isSuperAdmin() {
        return SystemConstants.SUPER_ADMIN_ID.equals(this.userId);
    }
}
