# 智慧零售系统 — 多环境部署文档

## 目录

- [环境概览](#环境概览)
- [一、开发环境 (Development)](#一开发环境-development)
- [二、测试环境 (Testing)](#二测试环境-testing)
- [三、生产环境 (Production)](#三生产环境-production)
- [四、Nginx 代理配置](#四nginx-代理配置)
- [五、后端部署详细步骤](#五后端部署详细步骤)
- [六、前端部署详细步骤](#六前端部署详细步骤)
- [七、微信小程序部署详细步骤](#七微信小程序部署详细步骤)
- [八、UniApp 部署详细步骤](#八uniapp-部署详细步骤)
- [九、数据库部署与维护](#九数据库部署与维护)
- [十、SSL 证书配置](#十ssl-证书配置)
- [十一、系统监控](#十一系统监控)
- [十二、常见问题排查](#十二常见问题排查)
- [附录：完整部署检查清单](#附录完整部署检查清单)

---

## 环境概览

本项目支持三套环境，通过 Spring Boot Profiles 和前端环境变量切换：

| 环境 | 后端 Profile | 前端 Mode | 数据库 | 用途 |
|------|-------------|-----------|--------|------|
| 开发 (dev) | `dev` | `development` | `unmanned_supermarket` (本地) | 本地开发调试 |
| 测试 (test) | `test` | `test` | `unmanned_supermarket_test` | 功能测试、联调 |
| 生产 (prod) | `prod` | `production` | `unmanned_supermarket` (服务器) | 正式上线运营 |

### 部署架构图

```
                         ┌──────────────┐
                         │  浏览器/手机  │
                         │  管理系统/    │
                         │  小程序/App  │
                         └──────┬───────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │      Nginx (80/443)  │
                     │                      │
                     │  /         → 前端    │
                     │  /api/*    → 后端    │
                     │  /file/*   → 文件    │
                     │  /swagger  → 文档    │
                     └────┬─────────┬───────┘
                          │         │
                          ▼         ▼
                    ┌──────────┐  ┌──────────────┐
                    │ 前端静态  │  │  Spring Boot │
                    │ dist/    │  │  :8080       │
                    └──────────┘  └──┬────┬────┬─┘
                                    │    │    │
                          ┌─────────┘    │    └─────────┐
                          ▼              ▼              ▼
                    ┌──────────┐  ┌──────────┐  ┌──────────┐
                    │ MySQL 8  │  │  Redis   │  │ MongoDB  │
                    │  :3306   │  │  :6379   │  │  :27017  │
                    └──────────┘  └──────────┘  └──────────┘
```

### 配置文件对应关系

```
项目根目录
├── src/main/resources/
│   ├── application.yml          # 通用配置（默认激活 dev）
│   ├── application-dev.yml      # 开发环境（SQL日志、全部Actuator端点）
│   ├── application-test.yml     # 测试环境（独立测试数据库）
│   └── application-prod.yml     # 生产环境（SSL、环境变量注入密码）
├── frontend/
│   ├── .env.development         # 开发：VITE_API_BASE_URL=http://localhost:8080
│   ├── .env.test                # 测试：VITE_API_BASE_URL=http://test-server:8080
│   └── .env.production          # 生产：VITE_API_BASE_URL=（空，Nginx同域代理）
├── miniprogram/app.js           # globalData.baseUrl 手动切换
└── uniapp/utils/api.js          # BASE_URL 手动切换
```

---

## 一、开发环境 (Development)

### 1.1 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行 |
| Maven | 3.8+ | 后端构建 |
| MySQL | 8.0+ | 数据库 |
| Node.js | 18+ | 前端构建 |
| npm | 9+ | 前端包管理 |
| Redis | 6+（可选） | 缓存 |
| MongoDB | 6+（可选） | 操作日志 |
| 微信开发者工具 | 最新版 | 小程序调试 |
| HBuilderX | 最新版（可选） | UniApp 开发 |

### 1.2 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本（创建库、表、测试数据）
source sql/init.sql;
```

### 1.3 启动后端

```bash
# 方式一：Maven 直接运行（默认 dev 环境）
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/unmanned-supermarket-0.0.1-SNAPSHOT.jar

# 方式三：指定环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

验证后端：
```bash
curl http://localhost:8080/actuator/health
# 返回: {"status":"UP","components":{...}}

# Swagger 文档
open http://localhost:8080/swagger-ui/index.html
```

### 1.4 启动前端

```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

### 1.5 启动小程序

1. 打开微信开发者工具
2. 导入项目目录 `miniprogram/`
3. AppID 填写：`wxe25d5363380f040d`
4. 设置 → 项目设置 → 勾选「不校验合法域名」
5. 确保后端已运行在 `http://localhost:8080`

### 1.6 启动 UniApp

1. 下载 HBuilderX
2. 打开 `uniapp/` 目录
3. 运行 → 运行到浏览器（或小程序模拟器）

---

## 二、测试环境 (Testing)

### 2.1 创建测试数据库

```sql
CREATE DATABASE unmanned_supermarket_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE unmanned_supermarket_test;
SOURCE sql/init.sql;
```

### 2.2 启动后端（测试环境）

```bash
# 激活 test 环境
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 或 JAR 包方式
java -jar target/unmanned-supermarket-0.0.1-SNAPSHOT.jar --spring.profiles.active=test
```

### 2.3 运行单元测试

```bash
# 运行全部测试（使用 H2 内存数据库）
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest
mvn test -Dtest=OrderServiceTest
```

### 2.4 构建前端（测试模式）

```bash
cd frontend

# 使用测试环境配置构建
npm run build -- --mode test

# 产出目录: frontend/dist/
```

---

## 三、生产环境 (Production)

### 3.1 服务器环境准备

以 **Ubuntu 22.04 LTS** 为例：

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 JDK 17
sudo apt install openjdk-17-jdk -y
java -version  # 验证

# 安装 MySQL 8
sudo apt install mysql-server -y
sudo mysql_secure_installation  # 安全初始化

# 安装 Nginx
sudo apt install nginx -y
nginx -v  # 验证

# 安装 Redis（可选）
sudo apt install redis-server -y

# 安装 Node.js 18（用于构建前端）
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y
```

### 3.2 创建项目目录

```bash
sudo mkdir -p /opt/supermarket/{file,logs,backup}
sudo chown -R www-data:www-data /opt/supermarket
sudo mkdir -p /var/www/supermarket/frontend
```

### 3.3 创建数据库和用户

```sql
-- 登录 MySQL
sudo mysql -u root -p

-- 创建生产数据库
CREATE DATABASE unmanned_supermarket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建专用用户（不要使用 root）
CREATE USER 'supermarket_user'@'localhost' IDENTIFIED BY '你的强密码_这里修改';
GRANT ALL PRIVILEGES ON unmanned_supermarket.* TO 'supermarket_user'@'localhost';
FLUSH PRIVILEGES;

-- 初始化表结构和数据
USE unmanned_supermarket;
SOURCE /opt/supermarket/sql/init.sql;
```

### 3.4 部署后端

详见 [五、后端部署详细步骤](#五后端部署详细步骤)。

### 3.5 部署前端

详见 [六、前端部署详细步骤](#六前端部署详细步骤)。

### 3.6 部署小程序

详见 [七、微信小程序部署详细步骤](#七微信小程序部署详细步骤)。

---

## 四、Nginx 代理配置

### 4.1 HTTP 配置

创建 `/etc/nginx/conf.d/supermarket.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或 IP

    # ---------- 前端静态文件 ----------
    location / {
        root /var/www/supermarket/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;  # Vue Router history 模式
        
        # Gzip 压缩
        gzip on;
        gzip_types text/css application/javascript application/json image/svg+xml;
        gzip_min_length 1024;
    }

    # ---------- 后端 API 代理 ----------
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 文件上传大小限制
        client_max_body_size 10m;
        
        # 超时设置
        proxy_connect_timeout 30s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # ---------- 文件访问（图片等） ----------
    location /file/ {
        proxy_pass http://127.0.0.1:8080/file/;
        proxy_set_header Host $host;
        
        # 静态文件缓存
        proxy_cache_valid 200 1d;
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    # ---------- Swagger 文档（生产环境建议关闭） ----------
    location /swagger-ui/ {
        proxy_pass http://127.0.0.1:8080/swagger-ui/;
        proxy_set_header Host $host;
        # 生产环境可添加 IP 白名单限制
        # allow 你的管理IP;
        # deny all;
    }

    location /v3/api-docs {
        proxy_pass http://127.0.0.1:8080/v3/api-docs;
        proxy_set_header Host $host;
    }

    # ---------- Actuator 监控（限内网访问） ----------
    location /actuator/ {
        proxy_pass http://127.0.0.1:8080/actuator/;
        proxy_set_header Host $host;
        # 生产环境限制访问
        # allow 10.0.0.0/8;
        # allow 172.16.0.0/12;
        # deny all;
    }

    # ---------- 日志 ----------
    access_log /var/log/nginx/supermarket_access.log;
    error_log /var/log/nginx/supermarket_error.log;
}
```

### 4.2 HTTPS 配置（推荐生产使用）

```nginx
# HTTP → HTTPS 重定向
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    # SSL 证书
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # 前端
    location / {
        root /var/www/supermarket/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
        gzip on;
        gzip_types text/css application/javascript application/json;
    }

    # API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 10m;
    }

    # 文件
    location /file/ {
        proxy_pass http://127.0.0.1:8080/file/;
        proxy_set_header Host $host;
        expires 7d;
    }

    access_log /var/log/nginx/supermarket_access.log;
    error_log /var/log/nginx/supermarket_error.log;
}
```

### 4.3 验证并启用 Nginx

```bash
# 测试配置语法
sudo nginx -t

# 重新加载配置
sudo systemctl reload nginx

# 查看状态
sudo systemctl status nginx
```

---

## 五、后端部署详细步骤

### 步骤 1：构建 JAR 包

在开发机器上执行：

```bash
# 运行测试 + 构建
mvn clean package

# 如果测试环境与生产数据库不同，可跳过测试
mvn clean package -DskipTests

# 产出文件
ls -la target/unmanned-supermarket-0.0.1-SNAPSHOT.jar
```

### 步骤 2：上传到服务器

```bash
# 上传 JAR 包
scp target/unmanned-supermarket-0.0.1-SNAPSHOT.jar user@server:/opt/supermarket/

# 上传 SQL 脚本
scp sql/init.sql user@server:/opt/supermarket/sql/
```

### 步骤 3：创建 systemd 服务

创建 `/etc/systemd/system/supermarket.service`：

```ini
[Unit]
Description=Unmanned Supermarket Backend
After=network.target mysql.service redis.service
Wants=mysql.service

[Service]
Type=simple
User=www-data
Group=www-data
WorkingDirectory=/opt/supermarket

# 启动命令（使用 prod 环境）
ExecStart=/usr/bin/java \
    -jar /opt/supermarket/unmanned-supermarket-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=prod \
    -Xms256m -Xmx512m \
    -XX:+UseG1GC

Restart=on-failure
RestartSec=10

# 环境变量（敏感信息通过此处注入）
Environment=DB_HOST=localhost
Environment=DB_PORT=3306
Environment=DB_USERNAME=supermarket_user
Environment=DB_PASSWORD=你的数据库密码
Environment=REDIS_HOST=localhost
Environment=REDIS_PASSWORD=你的Redis密码
Environment=UPLOAD_DIR=/opt/supermarket/file

# 日志输出
StandardOutput=journal
StandardError=journal
SyslogIdentifier=supermarket

[Install]
WantedBy=multi-user.target
```

### 步骤 4：启动服务

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start supermarket

# 设置开机自启
sudo systemctl enable supermarket

# 查看状态
sudo systemctl status supermarket

# 查看日志（实时）
sudo journalctl -u supermarket -f

# 查看最近日志
sudo journalctl -u supermarket --since "10 minutes ago"
```

### 步骤 5：验证

```bash
# 健康检查
curl http://localhost:8080/actuator/health
# 返回: {"status":"UP"}

# 测试公开接口
curl http://localhost:8080/api/products/list

# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"CHANGE_ME_ON_FIRST_LOGIN"}'
```

### 更新部署

```bash
# 1. 在开发机构建新 JAR
mvn clean package -DskipTests

# 2. 上传到服务器
scp target/unmanned-supermarket-0.0.1-SNAPSHOT.jar user@server:/opt/supermarket/

# 3. 重启服务
sudo systemctl restart supermarket

# 4. 确认启动成功
sudo systemctl status supermarket
curl http://localhost:8080/actuator/health
```

---

## 六、前端部署详细步骤

### 步骤 1：修改环境配置

检查 `frontend/.env.production`：

```env
# 生产环境通过 Nginx 代理，前后端同域，BASE_URL 为空
VITE_API_BASE_URL=
VITE_APP_TITLE=智慧零售系统
```

> **说明**：生产环境前端通过 Nginx 代理访问后端 `/api/*`，无需跨域，`VITE_API_BASE_URL` 设为空即可。

### 步骤 2：构建生产版本

```bash
cd frontend

# 安装依赖
npm install

# 构建生产版本
npm run build

# 产出目录
ls -la dist/
```

### 步骤 3：部署到服务器

```bash
# 方式一：直接上传 dist 目录
scp -r dist/* user@server:/var/www/supermarket/frontend/

# 方式二：打包后上传
tar czf frontend-dist.tar.gz -C dist .
scp frontend-dist.tar.gz user@server:/tmp/
ssh user@server "tar xzf /tmp/frontend-dist.tar.gz -C /var/www/supermarket/frontend/"
```

### 步骤 4：设置权限

```bash
sudo chown -R www-data:www-data /var/www/supermarket/frontend/
sudo chmod -R 755 /var/www/supermarket/frontend/
```

### 步骤 5：验证

```bash
# 通过 Nginx 访问
curl http://your-domain.com/
# 应返回 index.html 内容

# 验证 API 代理
curl http://your-domain.com/api/products/list
# 应返回商品列表 JSON
```

### 更新前端

```bash
cd frontend
npm run build
scp -r dist/* user@server:/var/www/supermarket/frontend/
# Nginx 无需重启，静态文件立即生效
```

---

## 七、微信小程序部署详细步骤

### 步骤 1：修改生产 API 地址

编辑 `miniprogram/app.js`，修改 `globalData`：

```javascript
globalData: {
    userInfo: null,
    // 开发环境: http://localhost:8080/api
    // 生产环境: https://your-domain.com/api
    baseUrl: 'https://your-domain.com/api',
    fileBaseUrl: 'https://your-domain.com'
}
```

### 步骤 2：配置服务器域名

登录 [微信公众平台](https://mp.weixin.qq.com/)：

1. 进入「开发管理」→「开发设置」→「服务器域名」
2. 添加以下域名：

| 类型 | 域名 |
|------|------|
| request 合法域名 | `https://your-domain.com` |
| uploadFile 合法域名 | `https://your-domain.com` |
| downloadFile 合法域名 | `https://your-domain.com` |

> **注意**：微信小程序要求 **HTTPS**，必须配置 SSL 证书。

### 步骤 3：上传代码

1. 打开微信开发者工具
2. 导入 `miniprogram/` 目录
3. 点击右上角「上传」
4. 填写版本号和备注

### 步骤 4：提交审核

1. 登录微信公众平台
2. 进入「版本管理」
3. 在「开发版本」中点击「提交审核」
4. 填写审核信息（功能页面、测试账号等）
5. 等待审核通过（通常 1-3 个工作日）

### 步骤 5：发布上线

审核通过后：

1. 在「版本管理」→「审核版本」中点击「发布」
2. 选择全量发布或分阶段发布
3. 发布后用户搜索小程序即可使用

### 测试账号配置

在提交审核时，需提供测试账号：

| 字段 | 值 |
|------|-----|
| 测试账号 | cust_wang |
| 测试密码 | 123456 |

---

## 八、UniApp 部署详细步骤

### 步骤 1：修改 API 地址

编辑 `uniapp/utils/api.js`：

```javascript
// 生产环境
const BASE_URL = 'https://your-domain.com/api'
const FILE_BASE_URL = 'https://your-domain.com'
```

### 步骤 2：发布为 H5

```bash
# 使用 HBuilderX
# 发行 → 网站-H5 → 配置发行路径 → 发行

# 产出目录: unpackage/dist/build/h5/
```

部署 H5 版本到 Nginx：
```bash
scp -r unpackage/dist/build/h5/* user@server:/var/www/supermarket/uniapp/
```

Nginx 添加配置：
```nginx
location /app/ {
    alias /var/www/supermarket/uniapp/;
    try_files $uri $uri/ /app/index.html;
}
```

### 步骤 3：发布为微信小程序

1. HBuilderX → 发行 → 小程序-微信
2. 在微信开发者工具中上传并提交审核

---

## 九、数据库部署与维护

### 9.1 生产数据库配置

编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf`：

```ini
[mysqld]
# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 性能优化
innodb_buffer_pool_size = 256M
max_connections = 200
query_cache_size = 64M

# 日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2

# 安全
bind-address = 127.0.0.1  # 仅允许本地连接
```

### 9.2 数据库备份

```bash
# 手动备份
mysqldump -u supermarket_user -p unmanned_supermarket > /opt/supermarket/backup/db_$(date +%Y%m%d_%H%M%S).sql

# 恢复备份
mysql -u supermarket_user -p unmanned_supermarket < /opt/supermarket/backup/db_20260101_120000.sql
```

设置自动备份（crontab）：

```bash
sudo crontab -e
```

添加：
```
# 每天凌晨 3 点备份数据库
0 3 * * * mysqldump -u supermarket_user -p'你的密码' unmanned_supermarket | gzip > /opt/supermarket/backup/db_$(date +\%Y\%m\%d).sql.gz

# 保留最近 30 天的备份
0 4 * * * find /opt/supermarket/backup/ -name "*.sql.gz" -mtime +30 -delete
```

---

## 十、SSL 证书配置

### 使用 Let's Encrypt（免费）

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 自动获取证书并配置 Nginx
sudo certbot --nginx -d your-domain.com

# 验证自动续期
sudo certbot renew --dry-run
```

证书会自动安装到 Nginx 配置中，并设置自动续期定时任务。

---

## 十一、系统监控

### 11.1 Spring Boot Actuator

| 端点 | URL | 说明 |
|------|-----|------|
| 健康检查 | `/actuator/health` | MySQL/Redis/磁盘状态 |
| 应用信息 | `/actuator/info` | 版本信息 |
| JVM 指标 | `/actuator/metrics` | 内存、GC、线程 |

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# JVM 内存
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP 请求统计
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### 11.2 Nginx 日志监控

```bash
# 实时访问日志
tail -f /var/log/nginx/supermarket_access.log

# 实时错误日志
tail -f /var/log/nginx/supermarket_error.log

# 统计请求量
cat /var/log/nginx/supermarket_access.log | wc -l

# 统计错误状态码
grep " 500 " /var/log/nginx/supermarket_access.log | wc -l
```

### 11.3 服务器资源监控

```bash
# CPU 和内存
htop

# 磁盘使用
df -h

# Java 进程
ps aux | grep java

# 端口检查
sudo ss -tlnp | grep -E '8080|80|443|3306|6379'
```

---

## 十二、常见问题排查

### Q1: Nginx 启动失败？
```bash
sudo nginx -t                    # 检查配置语法
sudo lsof -i :80                 # 检查 80 端口占用
sudo systemctl status nginx      # 查看错误信息
```

### Q2: 后端连不上 MySQL？
```bash
sudo systemctl status mysql      # 检查 MySQL 状态
mysql -u supermarket_user -p -e "SELECT 1"  # 测试连接
sudo journalctl -u supermarket --since "5 min ago"  # 查看后端日志
```

### Q3: 前端页面刷新后 404？
确保 Nginx 配置中有：
```nginx
try_files $uri $uri/ /index.html;
```
这是 Vue Router history 模式必需的。

### Q4: 文件上传后图片访问不到？
```bash
# 检查文件目录
ls -la /opt/supermarket/file/

# 检查权限
sudo chown -R www-data:www-data /opt/supermarket/file/
sudo chmod 755 /opt/supermarket/file/
```

### Q5: 小程序请求被拒绝？
1. 确保服务器已配置 SSL（HTTPS）
2. 在微信公众平台添加服务器域名
3. 域名必须已备案（国内服务器）

### Q6: Redis/MongoDB 未安装影响系统吗？
不影响。系统使用 `@ConditionalOnBean` 注解，检测不到连接时自动跳过，核心功能正常运行。

### Q7: 如何切换环境？
```bash
# 后端切换
java -jar app.jar --spring.profiles.active=prod  # 生产
java -jar app.jar --spring.profiles.active=test  # 测试
mvn spring-boot:run                               # 开发（默认 dev）

# 前端切换
npm run dev                    # 开发（使用 .env.development）
npm run build                  # 生产（使用 .env.production）
npm run build -- --mode test   # 测试（使用 .env.test）
```

### Q8: 如何更新部署？
```bash
# 后端更新
mvn clean package -DskipTests
scp target/*.jar user@server:/opt/supermarket/
sudo systemctl restart supermarket

# 前端更新
cd frontend && npm run build
scp -r dist/* user@server:/var/www/supermarket/frontend/
# Nginx 无需重启

# 小程序更新
# 修改代码 → 微信开发者工具上传 → 微信公众平台提交审核 → 发布
```

---

## 附录：完整部署检查清单

### 部署前检查

- [ ] 服务器已安装 JDK 17、MySQL 8、Nginx
- [ ] MySQL 已创建数据库和用户
- [ ] SQL 初始化脚本已执行
- [ ] SSL 证书已配置（小程序必需 HTTPS）
- [ ] 防火墙开放 80、443 端口

### 后端部署

- [ ] JAR 包已上传到 `/opt/supermarket/`
- [ ] systemd 服务已创建并启动
- [ ] 环境变量（数据库密码等）已配置
- [ ] `/opt/supermarket/file/` 目录权限正确
- [ ] `curl http://localhost:8080/actuator/health` 返回 UP

### 前端部署

- [ ] `.env.production` 中 `VITE_API_BASE_URL` 已设为空
- [ ] `npm run build` 构建成功
- [ ] `dist/` 内容已复制到 `/var/www/supermarket/frontend/`
- [ ] Nginx 配置中 `try_files` 已设置

### Nginx

- [ ] `sudo nginx -t` 配置检查通过
- [ ] HTTP → HTTPS 重定向正常
- [ ] `/api/` 代理到后端 8080
- [ ] `/file/` 代理到后端文件服务
- [ ] 浏览器访问首页正常

### 小程序

- [ ] `app.js` 中 `baseUrl` 已改为生产地址
- [ ] 微信公众平台已配置服务器域名
- [ ] 代码已上传并提交审核
- [ ] 测试账号信息已提供给审核

### 数据库

- [ ] 自动备份定时任务已配置
- [ ] `bind-address` 限制为本地连接
- [ ] 生产数据库密码已设置强密码
