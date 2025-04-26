CREATE TABLE `excel_task_process` (
                                      `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                      `type` TINYINT(2) NOT NULL COMMENT '类型：1-导入,2-导出',
                                      `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '状态：0-初始,1-进行中,2-完成,3-失败',
                                      `source_file` VARCHAR(500) COMMENT '源文件',
                                      `estimate_count` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '预估总记录数',
                                      `total_count` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '实际总记录数',
                                      `success_count` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '成功记录数',
                                      `failed_count` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '失败记录数',
                                      `file_name` VARCHAR(200) DEFAULT NULL COMMENT '文件名',
                                      `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
                                      `message` VARCHAR(255) DEFAULT NULL COMMENT '反馈消息',
                                      `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
                                      `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
                                      `creator` VARCHAR(50) DEFAULT NULL COMMENT 'chua',
                                      `business_code` VARCHAR(50) DEFAULT NULL COMMENT '业务编码',
                                      PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='导入导出任务';
