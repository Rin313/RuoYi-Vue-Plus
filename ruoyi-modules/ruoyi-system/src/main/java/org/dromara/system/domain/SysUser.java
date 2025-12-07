package org.dromara.system.domain;

import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value="sys_user",autoResultMap=true)
public class SysUser extends BaseEntity {

    @TableId(value = "user_id")
    private Long userId;

    private String userName;

    private String nickName;

    private String email;

    private String phonenumber;

    /**
     * 性别（0男 1女 2未知）
     */
    private String sex;

    /**
     * 头像
     */
    private Long avatar;

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
     * 邀请码分享次数
     */
    private Long totalShareCount;
    /**
     * 签到记录
     */
     @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> signRecord;

    public boolean isSuperAdmin() {
        return SystemConstants.SUPER_ADMIN_ID.equals(this.userId);
    }
}
