CREATE TABLE `export_task_process`
(
    `id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `business_system` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务系统',
    `business_type`   varchar(50)                                                  NOT NULL COMMENT '业务类型',
    `task_no`         varchar(64)                                                  NOT NULL COMMENT '任务唯一编号',
    `task_name`       varchar(255)                                                 DEFAULT NULL COMMENT '任务名称',
    `status`          tinyint(2) DEFAULT '0' COMMENT '状态：0-初始,1-进行中,2-完成,3-失败',
    `file_name`       varchar(200)                                                 DEFAULT NULL COMMENT '文件名',
    `file_url`        varchar(500)                                                 DEFAULT NULL COMMENT '文件路径',
    `message`         varchar(255)                                                 DEFAULT NULL COMMENT '反馈消息',
    `start_time`      datetime                                                     DEFAULT NULL COMMENT '开始时间',
    `end_time`        datetime                                                     DEFAULT NULL COMMENT '结束时间',
    `creator`         varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作用户',
    `request_fingerprint` varchar(64)                                              DEFAULT NULL COMMENT '导出请求指纹，用于判重',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_no` (`task_no`),
    KEY `idx_creator_status` (`creator`, `status`),
    KEY `idx_biz_status` (`business_system`, `business_type`, `status`),
    KEY `idx_request_fingerprint_status` (`request_fingerprint`, `status`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导出任务表';
