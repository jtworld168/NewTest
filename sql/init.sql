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

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`        VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `price`       DECIMAL(10,2)  NOT NULL COMMENT '商品价格',
    `stock`       INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
    `description` VARCHAR(500)   DEFAULT NULL COMMENT '商品描述',
    `category_id` BIGINT         DEFAULT NULL COMMENT '分类ID',
    `employee_discount_rate` DECIMAL(3,2) DEFAULT NULL COMMENT '员工折扣率，如0.80表示八折',
    `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_product_name` (`name`),
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
    CONSTRAINT `chk_product_price` CHECK (`price` > 0),
    CONSTRAINT `chk_product_stock` CHECK (`stock` >= 0),
    CONSTRAINT `chk_product_discount` CHECK (`employee_discount_rate` IS NULL OR (`employee_discount_rate` > 0 AND `employee_discount_rate` <= 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- 优惠券面额表
CREATE TABLE IF NOT EXISTS `coupon` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '优惠券面额ID',
    `name`            VARCHAR(100)   NOT NULL COMMENT '优惠券名称',
    `discount`        DECIMAL(10,2)  NOT NULL COMMENT '折扣金额',
    `min_amount`      DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '最低使用金额',
    `total_count`     INT            NOT NULL DEFAULT 0 COMMENT '发放总数量',
    `remaining_count` INT            NOT NULL DEFAULT 0 COMMENT '剩余可领取数量',
    `start_time`      DATETIME       DEFAULT NULL COMMENT '生效时间',
    `end_time`        DATETIME       DEFAULT NULL COMMENT '过期时间',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_coupon_name` (`name`),
    CONSTRAINT `chk_coupon_discount` CHECK (`discount` > 0),
    CONSTRAINT `chk_coupon_min_amount` CHECK (`min_amount` >= 0),
    CONSTRAINT `chk_coupon_total_count` CHECK (`total_count` >= 0),
    CONSTRAINT `chk_coupon_remaining` CHECK (`remaining_count` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券面额表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `user_coupon` (
    `id`          BIGINT    NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
    `user_id`     BIGINT    NOT NULL COMMENT '用户ID',
    `coupon_id`   BIGINT    NOT NULL COMMENT '优惠券面额ID',
    `status`      INT       NOT NULL DEFAULT 0 COMMENT '状态：0-可用，1-已使用，2-已过期',
    `use_time`    DATETIME  DEFAULT NULL COMMENT '使用时间',
    `create_time` DATETIME  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT       DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_uc_user_id` (`user_id`),
    KEY `idx_uc_coupon_id` (`coupon_id`),
    CONSTRAINT `fk_user_coupon_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_user_coupon_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户优惠券表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `user_id`           BIGINT         NOT NULL COMMENT '用户ID',
    `product_id`        BIGINT         NOT NULL COMMENT '商品ID',
    `quantity`          INT            NOT NULL DEFAULT 1 COMMENT '购买数量',
    `price_at_purchase` DECIMAL(10,2)  NOT NULL COMMENT '下单时单价（已计算员工折扣）',
    `total_amount`      DECIMAL(10,2)  NOT NULL COMMENT '订单总金额',
    `user_coupon_id`    BIGINT         DEFAULT NULL COMMENT '用户优惠券ID',
    `status`            INT            NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已完成，3-已取消',
    `create_time`       DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_coupon_id` (`user_coupon_id`),
    KEY `idx_order_status` (`status`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
    CONSTRAINT `fk_order_user_coupon` FOREIGN KEY (`user_coupon_id`) REFERENCES `user_coupon` (`id`),
    CONSTRAINT `chk_order_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `chk_order_price` CHECK (`price_at_purchase` > 0),
    CONSTRAINT `chk_order_total` CHECK (`total_amount` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- 订单商品明细表
CREATE TABLE IF NOT EXISTS `order_items` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id`          BIGINT         NOT NULL COMMENT '订单ID',
    `product_id`        BIGINT         NOT NULL COMMENT '商品ID',
    `quantity`          INT            NOT NULL DEFAULT 1 COMMENT '购买数量',
    `price_at_purchase` DECIMAL(10,2)  NOT NULL COMMENT '下单时单价（已计算员工折扣）',
    `subtotal`          DECIMAL(10,2)  NOT NULL COMMENT '小计金额（单价×数量）',
    `create_time`       DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_oi_order_id` (`order_id`),
    KEY `idx_oi_product_id` (`product_id`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
    CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
    CONSTRAINT `chk_oi_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `chk_oi_price` CHECK (`price_at_purchase` > 0),
    CONSTRAINT `chk_oi_subtotal` CHECK (`subtotal` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单商品明细表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart_item` (
    `id`          BIGINT    NOT NULL AUTO_INCREMENT COMMENT '购物车商品ID',
    `user_id`     BIGINT    NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT    NOT NULL COMMENT '商品ID',
    `quantity`    INT       NOT NULL DEFAULT 1 COMMENT '数量',
    `create_time` DATETIME  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT       DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_cart_user_id` (`user_id`),
    KEY `idx_cart_product_id` (`product_id`),
    KEY `idx_cart_user_product` (`user_id`, `product_id`),
    CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
    CONSTRAINT `chk_cart_quantity` CHECK (`quantity` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车表';

-- 订单支付表
CREATE TABLE IF NOT EXISTS `payment` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `order_id`        BIGINT         NOT NULL COMMENT '订单ID',
    `amount`          DECIMAL(10,2)  NOT NULL COMMENT '支付金额',
    `payment_method`  INT            NOT NULL COMMENT '支付方式：0-微信支付，1-支付宝，2-现金，3-银行卡',
    `payment_status`  INT            NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
    `payment_time`    DATETIME       DEFAULT NULL COMMENT '支付时间',
    `transaction_no`  VARCHAR(100)   DEFAULT NULL COMMENT '交易流水号',
    `create_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    UNIQUE KEY `uk_transaction_no` (`transaction_no`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
    CONSTRAINT `chk_payment_amount` CHECK (`amount` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单支付表';
