CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL,
    `password`    VARCHAR(100) NOT NULL,
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `role`        INT          NOT NULL DEFAULT 2,
    `is_hotel_employee` BOOLEAN DEFAULT FALSE,
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT          DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100)   NOT NULL,
    `price`       DECIMAL(10,2)  NOT NULL,
    `stock`       INT            NOT NULL DEFAULT 0,
    `description` VARCHAR(500)   DEFAULT NULL,
    `employee_discount_rate` DECIMAL(3,2) DEFAULT NULL,
    `create_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT            DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `coupon` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100)   NOT NULL,
    `discount`    DECIMAL(10,2)  NOT NULL,
    `min_amount`  DECIMAL(10,2)  NOT NULL DEFAULT 0,
    `start_time`  TIMESTAMP      DEFAULT NULL,
    `end_time`    TIMESTAMP      DEFAULT NULL,
    `status`      INT            NOT NULL DEFAULT 0,
    `create_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT            DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `order` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT         NOT NULL,
    `total_amount` DECIMAL(10,2)  NOT NULL,
    `coupon_id`    BIGINT         DEFAULT NULL,
    `status`       INT            NOT NULL DEFAULT 0,
    `create_time`  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time`  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`      INT            DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_order_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`)
);

CREATE TABLE IF NOT EXISTS `order_item` (
    `id`                BIGINT         NOT NULL AUTO_INCREMENT,
    `order_id`          BIGINT         NOT NULL,
    `product_id`        BIGINT         NOT NULL,
    `quantity`          INT            NOT NULL DEFAULT 1,
    `price_at_purchase` DECIMAL(10,2)  NOT NULL,
    `create_time`       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time`       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`           INT            DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`),
    CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
);