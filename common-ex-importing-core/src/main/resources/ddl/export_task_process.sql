CREATE TABLE `export_task_process` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
   `business_system` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务系统',
   `business_type` varchar(50) NOT NULL COMMENT '业务类型',
   `status` tinyint(2) DEFAULT '0' COMMENT '状态：0-初始,1-进行中,2-完成,3-失败',
   `file_name` varchar(200) DEFAULT NULL COMMENT '文件名',
   `file_url` varchar(500) DEFAULT NULL COMMENT '文件路径',
   `message` varchar(255) DEFAULT NULL COMMENT '反馈消息',
   `start_time` datetime DEFAULT NULL COMMENT '开始时间',
   `end_time` datetime DEFAULT NULL COMMENT '结束时间',
   `creator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作用户',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导出任务表';
