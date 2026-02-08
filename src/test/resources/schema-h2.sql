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
