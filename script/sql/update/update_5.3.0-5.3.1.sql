ALTER TABLE `flow_node` DROP COLUMN `skip_any_node`;
ALTER TABLE `flow_node`
    ADD COLUMN `ext` text NULL COMMENT '扩展属性' AFTER `update_time`;

INSERT INTO sys_dict_type VALUES (16, '000000', '按钮权限', 'wf_button_permission', 103, 1, sysdate(), NULL, NULL, '按钮权限列表');
INSERT INTO sys_dict_data VALUES (60, '000000', 1, '是否弹窗选人', '1', 'wf_button_permission', '', 'default', 'N', 103, 1, sysdate(), NULL, NULL,'是否弹窗选人');
