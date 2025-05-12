package org.dromara.workflow.service;

import org.dromara.common.core.domain.dto.UserDTO;

import java.util.List;

/**
 * 流程设计器-获取办理人
 *
 * @author AprilWind
 */
public interface IFlwTaskAssigneeService {

    /**
     * 根据存储标识符（storageId）解析分配类型和ID，并获取对应的用户列表
     * 支持单个标识（例如 "user:123" 或 "456"），格式非法将返回空列表
     *
     * @param storageIds 包含分配类型和ID的字符串
     * @return 匹配的用户列表，格式非法返回空列表
     */
    List<UserDTO> fetchUsersByStorageIds(String storageIds);

}
