# Archnova Web

极构科技的对外网站与运营后台。访客浏览服务、案例和开源项目，买家在商城领取源码；管理员在后台维护内容、处理留言和在线客服。

官网负责获客，商城负责交付，后台把案例、仓库、订单和客服收在同一套系统里。

## 功能

**官网（无需登录）**

- 首页、服务介绍、项目案例与案例详情
- 开源项目展示，可从 GitHub 同步仓库后选择是否对外展示
- 联系留言，记录客户端 IP
- 右下角在线客服（WebSocket），会话同样记录访客 IP

**源码商城**

- 邮箱验证码注册、登录、个人中心
- 商品详情（Markdown）、积分支付与支付宝 / 微信下单
- 支付完成后在订单中发放源码地址
- 待支付订单经 RabbitMQ 延迟消息超时取消（默认 24 小时）

**管理后台**

- 数据概览、站点联系方式
- 案例、开源仓库、源码商品与订单
- 买家积分调整、客户留言、在线客服回复

## 技术栈

| 部分 | 技术 |
| --- | --- |
| 后端 | Spring Boot 4.0.8、Java 21、Spring Security、JWT、MyBatis-Plus、Hutool |
| 数据与消息 | MySQL 8、RabbitMQ |
| 前端 | Vue 3、Vite、Ant Design Vue、Vue Router、Axios |
| 其它 | WebSocket 客服、邮件验证码、阿里云 OSS 文件存储 |

后台与商城接口使用 JWT，无状态鉴权。官网公开接口不要求登录。

## 目录

```
archnova-web/
├── src/main/java/com/archnova/   # Spring Boot 后端
├── src/main/resources/           # application.yml
├── archnova-views/               # 官网与商城（Vue 3）
├── archnova-back/                # 管理后台（Vue 3）
├── sql/                          # 建库与升级脚本
└── docs/
```

## 本地启动

需要 JDK 21、Maven、MySQL 8、RabbitMQ，以及 Node.js（前端）。

### 1. 初始化数据库

按 `application.yml` 中的地址执行。默认是 `127.0.0.1:3308`，库名 `archnova`。

```bash
mysql -uroot -p -h127.0.0.1 -P3308 < sql/init.sql
mysql -uroot -p -h127.0.0.1 -P3308 archnova < sql/github_repo.sql
mysql -uroot -p -h127.0.0.1 -P3308 archnova < sql/shop.sql
```

`sql/init.sql` 已包含案例详情图字段。若 `shop.sql` 开头的 `ALTER TABLE` 提示 `detail_images` 已存在，注释掉该语句后重新执行即可，后面的商城建表仍需要跑完。

### 2. 配置后端

编辑 `src/main/resources/application.yml`：

- 数据源账号，以及 RabbitMQ 地址
- `archnova.jwt-secret`
- `archnova.oss`：图片上传。类型支持 `local`、`aliyun`、`minio`、`rustfs`，当前实现为阿里云 OSS
- `spring.mail`：商城邮箱验证码（按需补充邮件配置）
- `archnova.pay.alipay` / `archnova.pay.wechat`：现金支付。未配置时订单会停在待支付，积分支付不依赖这两项

### 3. 启动

```bash
# 后端 http://127.0.0.1:8088
mvn spring-boot:run

# 官网 http://127.0.0.1:5173
cd archnova-views && npm install && npm run dev

# 后台 http://127.0.0.1:5174
cd archnova-back && npm install && npm run dev
```

前端开发环境通过 Vite 把 `/dev-api` 代理到 `http://127.0.0.1:8088`，见各自目录下的 `.env.development`。生产构建时接口前缀在 `.env.production` 中配置，同域部署可留空。

首次启动且库中没有 `admin` 时，后端会创建管理账号：

| 端 | 地址 | 账号 |
| --- | --- | --- |
| 管理后台 | http://127.0.0.1:5174 | `admin` / `admin123` |
| 商城 | 官网「登录」 | 自行注册 |

## 构建

```bash
mvn -DskipTests package
cd archnova-views && npm run build
cd archnova-back && npm run build
```

前端产物分别在 `archnova-views/dist` 与 `archnova-back/dist`。
