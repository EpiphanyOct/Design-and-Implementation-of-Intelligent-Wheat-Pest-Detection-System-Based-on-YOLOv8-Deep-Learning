-- =============================================
-- 小麦害虫检测系统 - 数据库初始化脚本
-- 版本: 1.0.0
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS wheat_pest_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE wheat_pest_db;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户唯一标识',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `user_role` VARCHAR(20) NOT NULL DEFAULT 'VIEWER' COMMENT '用户角色：ADMIN/INSPECTOR/VIEWER',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '电话',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 登录记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `login_record` (
    `login_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '登录记录ID',
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `login_status` TINYINT DEFAULT 1 COMMENT '登录状态：0-失败，1-成功',
    PRIMARY KEY (`login_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录记录表';

-- =============================================
-- 害虫信息表
-- =============================================
CREATE TABLE IF NOT EXISTS `pest_info` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `pest_id` VARCHAR(32) NOT NULL COMMENT '害虫唯一标识',
    `name` VARCHAR(100) NOT NULL COMMENT '害虫名称',
    `scientific_name` VARCHAR(100) COMMENT '拉丁学名',
    `category` VARCHAR(50) COMMENT '分类',
    `image_url` VARCHAR(255) COMMENT '害虫图片URL',
    `feature_description` TEXT COMMENT '特征描述',
    `harm_characteristics` TEXT COMMENT '危害特点',
    `control_method` TEXT COMMENT '防治方法',
    `occurrence_pattern` TEXT COMMENT '发生规律',
    `description` TEXT COMMENT '描述',
    `common_symptoms` VARCHAR(1000) COMMENT '常见症状',
    `suggested_prevention` TEXT COMMENT '建议防治措施',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pest_id` (`pest_id`),
    INDEX `idx_name` (`name`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='害虫信息表';

-- =============================================
-- 害虫检测记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `pest_detection_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` VARCHAR(32) COMMENT '用户ID',
    `image_path` VARCHAR(500) COMMENT '图片路径',
    `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `pest_type` VARCHAR(100) COMMENT '识别的害虫类型',
    `confidence` DECIMAL(5,4) COMMENT '置信度(0-1)',
    `diagnosis_result` TEXT COMMENT '诊断结果',
    `prevention_advice` TEXT COMMENT '防治建议',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/COMPLETED/FAILED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_upload_time` (`upload_time`),
    INDEX `idx_pest_type` (`pest_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='害虫检测记录表';

-- =============================================
-- 环境监测数据表
-- =============================================
CREATE TABLE IF NOT EXISTS `environment_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据ID',
    `plot_id` VARCHAR(32) NOT NULL COMMENT '地块ID',
    `temperature` DECIMAL(5,2) COMMENT '温度(℃)',
    `humidity` DECIMAL(5,2) COMMENT '湿度(%)',
    `light_intensity` DECIMAL(10,2) COMMENT '光照强度(lx)',
    `growth_status` VARCHAR(100) COMMENT '生长状态',
    `collect_time` DATETIME NOT NULL COMMENT '采集时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_plot_id` (`plot_id`),
    INDEX `idx_collect_time` (`collect_time`),
    INDEX `idx_plot_time` (`plot_id`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境监测数据表';

-- =============================================
-- 地块信息表
-- =============================================
CREATE TABLE IF NOT EXISTS `plot_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地块ID',
    `plot_id` VARCHAR(32) NOT NULL COMMENT '地块唯一标识',
    `plot_name` VARCHAR(100) COMMENT '地块名称',
    `region` VARCHAR(100) COMMENT '所属区域',
    `crop_type` VARCHAR(50) COMMENT '种植作物类型',
    `area` DECIMAL(10,2) COMMENT '面积(亩)',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
    `description` TEXT COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plot_id` (`plot_id`),
    INDEX `idx_region` (`region`),
    INDEX `idx_crop_type` (`crop_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地块信息表';

-- =============================================
-- 虫害预警表
-- =============================================
CREATE TABLE IF NOT EXISTS `pest_alert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警ID',
    `plot_id` VARCHAR(32) NOT NULL COMMENT '地块ID',
    `pest_type` VARCHAR(100) COMMENT '害虫类型',
    `alert_level` VARCHAR(20) COMMENT '预警等级：低/中/高',
    `alert_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
    `description` TEXT COMMENT '预警描述',
    `confirmed` TINYINT DEFAULT 0 COMMENT '是否确认：0-未确认，1-已确认',
    `confirm_time` DATETIME COMMENT '确认时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_plot_id` (`plot_id`),
    INDEX `idx_alert_time` (`alert_time`),
    INDEX `idx_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='虫害预警表';

-- =============================================
-- 预警设置表
-- =============================================
CREATE TABLE IF NOT EXISTS `alert_setting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设置ID',
    `alert_name` VARCHAR(100) NOT NULL COMMENT '预警名称',
    `pest_type` VARCHAR(100) COMMENT '害虫类型',
    `alert_level` VARCHAR(20) COMMENT '预警等级：低/中/高',
    `threshold` INT COMMENT '阈值',
    `alert_method` VARCHAR(50) COMMENT '预警方式：短信/邮件/系统通知',
    `receiver` VARCHAR(200) COMMENT '接收对象',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_pest_type` (`pest_type`),
    INDEX `idx_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警设置表';

-- =============================================
-- 检测报告表
-- =============================================
CREATE TABLE IF NOT EXISTS `pest_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    `report_id` VARCHAR(32) NOT NULL COMMENT '报告编号',
    `report_name` VARCHAR(200) NOT NULL COMMENT '报告名称',
    `report_period` VARCHAR(50) COMMENT '报告周期',
    `pest_type` VARCHAR(100) COMMENT '虫害类型',
    `area` VARCHAR(200) COMMENT '发生区域',
    `damage_level` VARCHAR(20) COMMENT '危害程度',
    `main_data` TEXT COMMENT '主要数据',
    `suggestions` TEXT COMMENT '建议措施',
    `file_path` VARCHAR(500) COMMENT '报告文件路径',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_report_id` (`report_id`),
    INDEX `idx_report_name` (`report_name`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检测报告表';

-- =============================================
-- 设备信息表
-- =============================================
CREATE TABLE IF NOT EXISTS `device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_id` VARCHAR(32) NOT NULL COMMENT '设备编号',
    `device_name` VARCHAR(100) COMMENT '设备名称',
    `device_type` VARCHAR(50) COMMENT '设备类型',
    `connection_status` VARCHAR(20) DEFAULT 'OFFLINE' COMMENT '连接状态：ONLINE/OFFLINE',
    `device_status` VARCHAR(20) DEFAULT 'STOPPED' COMMENT '设备状态：RUNNING/STOPPED/ERROR',
    `start_time` DATETIME COMMENT '开启时间',
    `stop_time` DATETIME COMMENT '关闭时间',
    `parameters` JSON COMMENT '参数设置',
    `plot_id` VARCHAR(32) COMMENT '关联地块ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`),
    INDEX `idx_device_type` (`device_type`),
    INDEX `idx_plot_id` (`plot_id`),
    INDEX `idx_connection_status` (`connection_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备信息表';

-- =============================================
-- 数据管理表
-- =============================================
CREATE TABLE IF NOT EXISTS `data_management` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据ID',
    `data_id` VARCHAR(32) NOT NULL COMMENT '数据编号',
    `data_source` VARCHAR(100) COMMENT '数据来源',
    `data_type` VARCHAR(50) COMMENT '数据类型',
    `collect_time` DATETIME COMMENT '采集时间',
    `data_content` TEXT COMMENT '数据内容',
    `data_status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '数据状态',
    `backup_time` DATETIME COMMENT '备份时间',
    `restore_time` DATETIME COMMENT '恢复时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_id` (`data_id`),
    INDEX `idx_data_type` (`data_type`),
    INDEX `idx_collect_time` (`collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据管理表';
