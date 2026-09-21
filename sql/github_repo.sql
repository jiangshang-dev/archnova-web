-- 开源项目。前台只展示 status = 1，隐藏不用删数据。
-- mysql -uroot -p123456 -h127.0.0.1 -P3308 archnova < sql/github_repo.sql

USE archnova;

CREATE TABLE IF NOT EXISTS github_repo (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(128)  NOT NULL,
    description    VARCHAR(512)  NULL,
    language       VARCHAR(64)   NULL,
    html_url       VARCHAR(512)  NOT NULL,
    homepage       VARCHAR(512)  NULL,
    stars          INT           NOT NULL DEFAULT 0,
    github_updated VARCHAR(10)   NULL,
    sort_num       INT           NOT NULL DEFAULT 0,
    status         TINYINT       NOT NULL DEFAULT 1 COMMENT '1 展示 0 隐藏',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_github_repo_name (name)
) COMMENT '开源项目';
