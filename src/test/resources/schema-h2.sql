CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL,
    `password`    VARCHAR(100) NOT NULL,
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `role`        INT          NOT NULL DEFAULT 2,
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
    `create_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    `deleted`     INT            DEFAULT 0,
    PRIMARY KEY (`id`)
);
