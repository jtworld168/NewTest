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
    `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 插入测试数据

-- 用户（管理员、员工、顾客）
INSERT INTO `user` (`username`, `password`, `phone`, `role`, `is_hotel_employee`, `avatar`) VALUES
('admin', 'CHANGE_ME_ON_FIRST_LOGIN', '13800000000', 0, 0, NULL),
('emp_zhang', '123456', '13900001111', 1, 1, NULL),
('emp_li', '123456', '13900002222', 1, 1, NULL),
('cust_wang', '123456', '13700001111', 2, 0, NULL),
('cust_zhao', '123456', '13700002222', 2, 0, NULL);

-- 商品分类
INSERT INTO `category` (`name`, `description`) VALUES
('饮料', '各类饮品'),
('零食', '各类休闲零食'),
('日用品', '日常生活用品'),
('速食', '方便食品');

-- 商品
INSERT INTO `product` (`name`, `price`, `stock`, `description`, `category_id`, `employee_discount_rate`, `barcode`, `image`, `status`, `stock_alert_threshold`) VALUES
('可口可乐 330ml', 3.00, 200, '经典碳酸饮料', 1, 0.80, '6901939621035', NULL, 1, 10),
('百事可乐 330ml', 3.00, 150, '百事碳酸饮料', 1, 0.80, '6922255451427', NULL, 1, 10),
('农夫山泉 550ml', 2.00, 300, '天然矿泉水', 1, 0.90, '6921168593088', NULL, 1, 10),
('乐事薯片 原味 70g', 6.50, 100, '经典原味薯片', 2, 0.85, '6924743921023', NULL, 1, 10),
('奥利奥饼干 97g', 7.90, 80, '夹心巧克力饼干', 2, 0.85, '6901668044457', NULL, 1, 10),
('抽纸 3包装', 12.90, 60, '柔软面巾纸', 3, 0.75, '6920174751581', NULL, 1, 10),
('洗手液 500ml', 15.00, 40, '抑菌洗手液', 3, 0.70, '6902083890193', NULL, 1, 10),
('康师傅红烧牛肉面', 4.50, 120, '经典方便面', 4, 0.80, '6920152430132', NULL, 1, 10);

-- 优惠券面额
INSERT INTO `coupon` (`name`, `discount`, `min_amount`, `total_count`, `remaining_count`, `start_time`, `end_time`) VALUES
('满20减5', 5.00, 20.00, 100, 80, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
('满50减10', 10.00, 50.00, 50, 40, '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
('新人券 满10减3', 3.00, 10.00, 200, 190, '2026-01-01 00:00:00', '2026-06-30 23:59:59');

-- 用户优惠券
INSERT INTO `user_coupon` (`user_id`, `coupon_id`, `status`) VALUES
(4, 1, 0),
(4, 3, 0),
(5, 2, 0);

-- 店铺
INSERT INTO `store` (`name`, `address`, `phone`, `status`) VALUES
('总店', '市中心商业街1号', '400-000-0001', 1),
('东区分店', '东区购物广场2楼', '400-000-0002', 1),
('西区分店', '西区步行街88号', '400-000-0003', 1);

-- 店铺商品
INSERT INTO `store_product` (`store_id`, `product_id`, `store_price`, `store_stock`, `status`) VALUES
(1, 1, 3.00, 100, 1), (1, 2, 3.00, 80, 1), (1, 3, 2.00, 150, 1), (1, 4, 6.50, 50, 1),
(1, 5, 7.90, 40, 1), (1, 6, 12.90, 30, 1), (1, 7, 15.00, 20, 1), (1, 8, 4.50, 60, 1),
(2, 1, 3.50, 60, 1), (2, 3, 2.50, 80, 1), (2, 4, 7.00, 30, 1), (2, 8, 5.00, 40, 1),
(3, 2, 3.50, 50, 1), (3, 5, 8.50, 25, 1), (3, 6, 13.90, 20, 1), (3, 7, 16.00, 15, 1);

-- 购物车
INSERT INTO `cart_item` (`user_id`, `product_id`, `quantity`) VALUES
(4, 1, 2),
(4, 4, 1),
(5, 3, 3);

-- 订单
INSERT INTO `order` (`user_id`, `store_id`, `product_id`, `quantity`, `price_at_purchase`, `total_amount`, `user_coupon_id`, `status`) VALUES
(4, 1, 1, 2, 3.00, 6.00, NULL, 2),
(5, 2, 3, 1, 2.00, 2.00, NULL, 1),
(2, 1, 4, 1, 5.53, 5.53, NULL, 2);

-- 订单商品明细
INSERT INTO `order_items` (`order_id`, `product_id`, `quantity`, `price_at_purchase`, `subtotal`) VALUES
(1, 1, 2, 3.00, 6.00),
(2, 3, 1, 2.00, 2.00),
(3, 4, 1, 5.53, 5.53);

-- 支付记录
INSERT INTO `payment` (`order_id`, `amount`, `payment_method`, `payment_status`, `payment_time`, `transaction_no`) VALUES
(1, 6.00, 0, 1, '2026-02-01 10:30:00', 'WX20260201103000001'),
(2, 2.00, 1, 1, '2026-02-02 14:20:00', 'ZFB20260202142000001'),
(3, 5.53, 2, 1, '2026-02-03 09:15:00', 'CASH20260203091500001');

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
    `barcode`     VARCHAR(100) DEFAULT NULL COMMENT '商品条形码',
    `image`       VARCHAR(500) DEFAULT NULL COMMENT '商品图片URL',
    `status`      INT          DEFAULT 1 COMMENT '上架状态：0-下架，1-上架',
    `stock_alert_threshold` INT DEFAULT 10 COMMENT '库存预警阈值',
    `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_product_name` (`name`),
    UNIQUE KEY `uk_product_barcode` (`barcode`),
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
    CONSTRAINT `chk_coupon_total` CHECK (`total_count` >= 0),
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

-- 店铺表
CREATE TABLE IF NOT EXISTS `store` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `address`     VARCHAR(500) DEFAULT NULL COMMENT '店铺地址',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `image`       VARCHAR(500) DEFAULT NULL COMMENT '店铺图片URL',
    `safety_stock` INT         DEFAULT 10 COMMENT '安全库存阈值',
    `status`      INT          DEFAULT 1 COMMENT '店铺状态：0-关闭，1-营业',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_store_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='店铺表';

-- 店铺商品表
CREATE TABLE IF NOT EXISTS `store_product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '店铺商品ID',
    `store_id`    BIGINT         NOT NULL COMMENT '店铺ID',
    `product_id`  BIGINT         NOT NULL COMMENT '商品ID（关联总商品表）',
    `store_price` DECIMAL(10,2)  DEFAULT NULL COMMENT '店铺售价（可与总商品表价格不同）',
    `store_stock` INT            DEFAULT 0 COMMENT '店铺库存',
    `safety_stock` INT           DEFAULT 10 COMMENT '安全库存阈值',
    `status`      INT            DEFAULT 1 COMMENT '上架状态：0-下架，1-上架',
    `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_sp_store_id` (`store_id`),
    KEY `idx_sp_product_id` (`product_id`),
    UNIQUE KEY `uk_store_product` (`store_id`, `product_id`),
    CONSTRAINT `fk_sp_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
    CONSTRAINT `fk_sp_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='店铺商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `user_id`           BIGINT         NOT NULL COMMENT '用户ID',
    `store_id`          BIGINT         DEFAULT NULL COMMENT '店铺ID',
    `product_id`        BIGINT         DEFAULT NULL COMMENT '商品ID（兼容单商品订单）',
    `quantity`          INT            DEFAULT 1 COMMENT '购买数量',
    `price_at_purchase` DECIMAL(10,2)  DEFAULT NULL COMMENT '下单时单价（已计算员工折扣）',
    `total_amount`      DECIMAL(10,2)  NOT NULL COMMENT '订单总金额',
    `user_coupon_id`    BIGINT         DEFAULT NULL COMMENT '用户优惠券ID',
    `status`            INT            NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已完成，3-已取消',
    `create_time`       DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           INT            DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_store_id` (`store_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_coupon_id` (`user_coupon_id`),
    KEY `idx_order_status` (`status`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_order_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
    CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
    CONSTRAINT `fk_order_user_coupon` FOREIGN KEY (`user_coupon_id`) REFERENCES `user_coupon` (`id`),
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
