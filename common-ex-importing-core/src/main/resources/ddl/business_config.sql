CREATE TABLE IF NOT EXISTS `export_business_config` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `config_type` varchar(20) NOT NULL COMMENT '配置类型：IMPORT/EXPORT',
    `business_system` varchar(64) NOT NULL COMMENT '业务系统',
    `business_type` varchar(64) NOT NULL COMMENT '业务类型',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_type_business_system_type` (`config_type`, `business_system`, `business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='业务配置表';


