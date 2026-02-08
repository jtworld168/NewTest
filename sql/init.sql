-- 创建数据库
CREATE DATABASE IF NOT EXISTS unmanned_supermarket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE unmanned_supermarket;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `role`        INT          NOT NULL DEFAULT 2 COMMENT '角色：0-管理员，1-员工，2-顾客',
    `is_hotel_employee` TINYINT(1) DEFAULT 0 COMMENT '是否酒店员工：0-否，1-是',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 插入默认管理员（部署后请立即修改密码）
INSERT INTO `user` (`username`, `password`, `phone`, `role`) VALUES ('admin', 'CHANGE_ME_ON_FIRST_LOGIN', '13800000000', 0);

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`        VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `price`       DECIMAL(10,2)  NOT NULL COMMENT '商品价格',
    `stock`       INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
    `description` VARCHAR(500)   DEFAULT NULL COMMENT '商品描述',
    `employee_discount_rate` DECIMAL(3,2) DEFAULT NULL COMMENT '员工折扣率，如0.80表示八折',
    `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `name`        VARCHAR(100)   NOT NULL COMMENT '优惠券名称',
    `discount`    DECIMAL(10,2)  NOT NULL COMMENT '折扣金额',
    `min_amount`  DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '最低使用金额',
    `start_time`  DATETIME       DEFAULT NULL COMMENT '生效时间',
    `end_time`    DATETIME       DEFAULT NULL COMMENT '过期时间',
    `status`      INT            NOT NULL DEFAULT 0 COMMENT '状态：0-可用，1-已使用，2-已过期',
    `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `user_id`           BIGINT         NOT NULL COMMENT '用户ID',
    `product_id`        BIGINT         NOT NULL COMMENT '商品ID',
    `quantity`          INT            NOT NULL DEFAULT 1 COMMENT '购买数量',
    `price_at_purchase` DECIMAL(10,2)  NOT NULL COMMENT '下单时单价（已计算员工折扣）',
    `total_amount`      DECIMAL(10,2)  NOT NULL COMMENT '订单总金额',
    `coupon_id`         BIGINT         DEFAULT NULL COMMENT '优惠券ID',
    `status`            INT            NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已完成，3-已取消',
    `create_time`       DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_coupon_id` (`coupon_id`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
    CONSTRAINT `fk_order_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';
