-- 极构科技官网
-- 库：archnova    默认账号由后端首次启动创建：admin / admin123
-- mysql -uroot -p123456 < sql/init.sql

CREATE DATABASE IF NOT EXISTS archnova DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE archnova;

DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS chat_session;
DROP TABLE IF EXISTS contact_message;
DROP TABLE IF EXISTS project_case;
DROP TABLE IF EXISTS site_config;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(128) NOT NULL,
    real_name   VARCHAR(64)  NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) COMMENT '后台账号';

CREATE TABLE site_config (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key   VARCHAR(64)  NOT NULL,
    config_value TEXT         NULL,
    remark       VARCHAR(128) NULL,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) COMMENT '站点联系方式等可配置项';

CREATE TABLE project_case (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(128)  NOT NULL,
    summary     VARCHAR(512)  NOT NULL,
    content     TEXT          NULL,
    category    VARCHAR(32)   NOT NULL COMMENT '小程序/App/网站/企业系统/桌面应用',
    tech_stack  VARCHAR(255)  NULL,
    cover       VARCHAR(512)  NULL,
    detail_images TEXT        NULL COMMENT '详情图片，多个地址用英文逗号分隔',
    demo_url    VARCHAR(512)  NULL,
    github_url  VARCHAR(512)  NULL,
    sort_num    INT           NOT NULL DEFAULT 0,
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    deleted     TINYINT       NOT NULL DEFAULT 0,
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_status_sort (status, sort_num)
) COMMENT '项目案例';

CREATE TABLE contact_message (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(64)  NOT NULL,
    mobile       VARCHAR(64)  NULL,
    email        VARCHAR(128) NULL,
    company      VARCHAR(128) NULL,
    service_type VARCHAR(32)  NULL,
    content      TEXT         NOT NULL,
    client_ip    VARCHAR(64)  NULL,
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '联系我们留言';

CREATE TABLE chat_session (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    visitor_token VARCHAR(64)  NOT NULL,
    visitor_name  VARCHAR(64)  NULL,
    client_ip     VARCHAR(64)  NULL,
    last_message  VARCHAR(500) NULL,
    unread_count  INT          NOT NULL DEFAULT 0,
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '0进行中 1已结束',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_visitor_token (visitor_token)
) COMMENT '在线客服会话';

CREATE TABLE chat_message (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id  BIGINT       NOT NULL,
    sender_type VARCHAR(16)  NOT NULL COMMENT 'visitor/admin',
    content     VARCHAR(1000) NOT NULL,
    client_ip   VARCHAR(64)  NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_session (session_id)
) COMMENT '在线客服消息';

INSERT INTO site_config (config_key, config_value, remark) VALUES
('company_name', '极构科技', '站点名称'),
('slogan', '把业务想法做成能上线的产品', '一句话介绍'),
('location', '北京·通州', '所在地'),
('email', '1135365441@qq.com', '邮箱'),
('wechat_id', 'rylzx1135365441', '微信号'),
('wechat_qr', '/contact/wechat-qr.jpg', '微信二维码'),
('github_url', 'https://github.com/jiangshang-dev', 'GitHub 主页'),
('github_user', 'jiangshang-dev', 'GitHub 用户名'),
('phone', '', '电话，可留空');

INSERT INTO project_case (title, summary, content, category, tech_stack, demo_url, github_url, sort_num, status) VALUES
('微服务低代码平台',
 '给团队一套能改的后台骨架：账号权限、业务模块和前端工作台可以按项目继续长。',
 '面向需要快速起步、又不愿被成品低代码绑死的团队。平台拆成可独立演进的服务，业务页面和接口可以按模块追加，适合内部管理系统、运营后台和行业应用的底座。\n\n交付时会一起交代部署方式和二次开发入口，后续新增模块不用推倒重来。',
 '企业系统', 'Java,Spring Cloud,Vue 3', NULL, 'https://github.com/jiangshang-dev/nova-cloud', 1, 1),
('AI 笔记与学习客户端',
 '把笔记、刷题和 AI 讲解放进同一个桌面客户端，适合知识付费或内部培训产品。',
 '客户端覆盖本地笔记、内容编辑和 AI 辅助讲解。可以按你的题库、课程或文档重新组织信息结构，而不是套一个通用聊天窗口。\n\n适合培训机构、职业考试和企业内部知识产品做第一版可安装的客户端。',
 'App', 'Java,Vue 3,Electron', NULL, 'https://github.com/jiangshang-dev/ruankaotong-web', 2, 1),
('垂直领域智能体工作台',
 '把文档解析、对话和工具调用收成可交付的智能体服务，而不是演示用的单页聊天。',
 '后端负责智能体编排、工具调用和业务接口，前端提供可嵌入业务系统的工作台。适合已有资料库、审批流或客服场景，希望把 AI 接进现有流程的团队。',
 '企业系统', 'Python,AgentScope,Vue 3', NULL, 'https://github.com/jiangshang-dev/agentscope-agent-service', 3, 1),
('Redis 可视化管理工具',
 '给运维和开发一个跨平台的 Redis 连接与数据浏览客户端。',
 '支持多实例连接和数据查看，减少只能靠命令行排查的成本。这类工具型产品可以按你的内部规范继续加权限、审计和常用操作。',
 '桌面应用', 'TypeScript,Electron', NULL, 'https://github.com/jiangshang-dev/redis-manager-desktop', 4, 1),
('面试学习应用',
 '面向学习场景的刷题产品：题库、练习记录和讲解可以按你的内容重新组织。',
 '从内容结构、练习流程到客户端界面可以按目标用户重做。适合个人 IP、培训团队把已有题库做成可传播的应用，而不是再发一份文档。',
 'App', 'HTML,ArkTS', NULL, 'https://github.com/jiangshang-dev/mianshi', 5, 1),
('业务门户与管理后台',
 '对外展示站和管理后台分开做：访客不用注册，内部人员登录后维护案例、留言和客服。',
 '官网负责把服务范围、案例和联系方式讲清楚；后台负责改联系方式、上下架案例、查看留言，并用在线客服回复访客。这套结构可以直接复用到你的品牌站或产品介绍站。',
 '网站', 'Vue 3,Spring Boot,WebSocket', NULL, NULL, 6, 1);
