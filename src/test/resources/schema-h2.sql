CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL,
    `password`    VARCHAR(100) NOT NULL,
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `role`        INT          NOT NULL DEFAULT 2,
    `is_hotel_employee` BOOLEAN DEFAULT FALSE,
    `avatar`      VARCHAR(500) DEFAULT NULL,
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT          DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT          DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_category_name` UNIQUE (`name`)
);

CREATE TABLE IF NOT EXISTS `product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100)   NOT NULL,
    `price`       DECIMAL(10,2)  NOT NULL,
    `stock`       INT            NOT NULL DEFAULT 0,
    `description` VARCHAR(500)   DEFAULT NULL,
    `category_id` BIGINT         DEFAULT NULL,
    `employee_discount_rate` DECIMAL(3,2) DEFAULT NULL,
    `barcode`     VARCHAR(100) DEFAULT NULL,
    `image`       VARCHAR(500) DEFAULT NULL,
    `status`      INT          DEFAULT 1,
    `stock_alert_threshold` INT DEFAULT 10,
    `create_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT            DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
    CONSTRAINT `chk_product_price` CHECK (`price` > 0),
    CONSTRAINT `chk_product_stock` CHECK (`stock` >= 0)
);

CREATE TABLE IF NOT EXISTS `coupon` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(100)   NOT NULL,
    `discount`        DECIMAL(10,2)  NOT NULL,
    `min_amount`      DECIMAL(10,2)  NOT NULL DEFAULT 0,
    `total_count`     INT            NOT NULL DEFAULT 0,
    `remaining_count` INT            NOT NULL DEFAULT 0,
    `start_time`      TIMESTAMP      DEFAULT NULL,
    `end_time`        TIMESTAMP      DEFAULT NULL,
    `create_time`     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time`     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`         INT            DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `chk_coupon_discount` CHECK (`discount` > 0),
    CONSTRAINT `chk_coupon_min_amount` CHECK (`min_amount` >= 0),
    CONSTRAINT `chk_coupon_total` CHECK (`total_count` >= 0),
    CONSTRAINT `chk_coupon_remaining` CHECK (`remaining_count` >= 0)
);

CREATE TABLE IF NOT EXISTS `user_coupon` (
    `id`          BIGINT    NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT    NOT NULL,
    `coupon_id`   BIGINT    NOT NULL,
    `status`      INT       NOT NULL DEFAULT 0,
    `use_time`    TIMESTAMP DEFAULT NULL,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT       DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_user_coupon_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_user_coupon_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`)
);

CREATE TABLE IF NOT EXISTS `cart_item` (
    `id`          BIGINT    NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT    NOT NULL,
    `product_id`  BIGINT    NOT NULL,
    `quantity`    INT       NOT NULL DEFAULT 1,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT       DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
    CONSTRAINT `chk_cart_quantity` CHECK (`quantity` > 0)
);

CREATE TABLE IF NOT EXISTS `store` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL,
    `address`     VARCHAR(500) DEFAULT NULL,
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `image`       VARCHAR(500) DEFAULT NULL,
    `safety_stock` INT         DEFAULT 10,
    `status`      INT          DEFAULT 1,
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT          DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `store_product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT,
    `store_id`    BIGINT         NOT NULL,
    `product_id`  BIGINT         NOT NULL,
    `store_price` DECIMAL(10,2)  DEFAULT NULL,
    `store_stock` INT            DEFAULT 0,
    `status`      INT            DEFAULT 1,
    `create_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT            DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_sp_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
    CONSTRAINT `fk_sp_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
);

CREATE TABLE IF NOT EXISTS `order` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT         NOT NULL,
    `store_id`          BIGINT         DEFAULT NULL,
    `product_id`        BIGINT         DEFAULT NULL,
    `quantity`          INT            DEFAULT 1,
    `price_at_purchase` DECIMAL(10,2)  DEFAULT NULL,
    `total_amount`      DECIMAL(10,2)  NOT NULL,
    `user_coupon_id`    BIGINT         DEFAULT NULL,
    `status`            INT            NOT NULL DEFAULT 0,
    `create_time`       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time`       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`           INT            DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_order_store` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`),
    CONSTRAINT `fk_order_user_coupon` FOREIGN KEY (`user_coupon_id`) REFERENCES `user_coupon` (`id`),
    CONSTRAINT `chk_order_total` CHECK (`total_amount` > 0)
);

CREATE TABLE IF NOT EXISTS `order_items` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT,
    `order_id`          BIGINT         NOT NULL,
    `product_id`        BIGINT         NOT NULL,
    `quantity`          INT            NOT NULL DEFAULT 1,
    `price_at_purchase` DECIMAL(10,2)  NOT NULL,
    `subtotal`          DECIMAL(10,2)  NOT NULL,
    `create_time`       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time`       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`           INT            DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
    CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
    CONSTRAINT `chk_oi_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `chk_oi_price` CHECK (`price_at_purchase` > 0),
    CONSTRAINT `chk_oi_subtotal` CHECK (`subtotal` > 0)
);

CREATE TABLE IF NOT EXISTS `payment` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT,
    `order_id`        BIGINT         NOT NULL,
    `amount`          DECIMAL(10,2)  NOT NULL,
    `payment_method`  INT            NOT NULL,
    `payment_status`  INT            NOT NULL DEFAULT 0,
    `payment_time`    TIMESTAMP      DEFAULT NULL,
    `transaction_no`  VARCHAR(100)   DEFAULT NULL,
    `create_time`     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time`     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`         INT            DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
    CONSTRAINT `uk_transaction_no` UNIQUE (`transaction_no`),
    CONSTRAINT `chk_payment_amount` CHECK (`amount` > 0)
);