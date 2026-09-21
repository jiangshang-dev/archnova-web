-- 已有库升级：案例详情图、源码商城
-- mysql -uroot -p123456 -h127.0.0.1 -P3308 archnova < sql/shop.sql

USE archnova;

ALTER TABLE project_case
    ADD COLUMN detail_images TEXT NULL COMMENT '详情图片，多个地址用英文逗号分隔' AFTER cover;

CREATE TABLE IF NOT EXISTS shop_customer (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64)  NULL,
    points      INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_shop_username (username)
) COMMENT '商城买家';

CREATE TABLE IF NOT EXISTS shop_product (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    title        VARCHAR(128)  NOT NULL,
    summary      VARCHAR(512)  NOT NULL,
    cover        VARCHAR(512)  NULL,
    detail_images TEXT         NULL,
    content_md   MEDIUMTEXT    NULL,
    price_cent   INT           NOT NULL DEFAULT 0 COMMENT '人民币价格，单位分',
    points_price INT           NOT NULL DEFAULT 0 COMMENT '积分价格',
    source_url   VARCHAR(512)  NULL COMMENT '支付完成后交付的源码地址',
    sort_num     INT           NOT NULL DEFAULT 0,
    status       TINYINT       NOT NULL DEFAULT 1,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '源码商品';

CREATE TABLE IF NOT EXISTS shop_order (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no      VARCHAR(32)  NOT NULL,
    customer_id   BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    product_title VARCHAR(128) NOT NULL,
    pay_type      VARCHAR(16)  NOT NULL COMMENT 'POINTS/ALIPAY/WECHAT',
    amount_cent   INT          NOT NULL DEFAULT 0,
    points_cost   INT          NOT NULL DEFAULT 0,
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '0待支付 1已完成 2已取消',
    source_url    VARCHAR(512) NULL,
    client_ip     VARCHAR(64)  NULL,
    pay_time      DATETIME     NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_customer_status (customer_id, status)
) COMMENT '源码订单';

INSERT INTO shop_product (title, summary, cover, content_md, price_cent, points_price, source_url, sort_num, status)
SELECT '微服务低代码平台源码',
       '可二次开发的后台底座，支付完成后发放仓库地址。',
       NULL,
       '## 包含什么\n\n- 账号与权限\n- 业务模块骨架\n- Vue 3 管理端\n\n## 交付方式\n\n支付完成后，在订单里查看源码地址。',
       19900,
       1990,
       'https://github.com/jiangshang-dev/nova-cloud',
       1,
       1
WHERE NOT EXISTS (SELECT 1 FROM shop_product WHERE title = '微服务低代码平台源码');
