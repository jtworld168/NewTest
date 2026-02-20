# 无人超市管理系统 — 本地测试与 Nginx 部署文档

## 目录

- [本地开发测试](#本地开发测试)
- [生产环境部署](#生产环境部署)
- [Nginx 配置](#nginx-配置)
- [后端部署](#后端部署)
- [数据库部署](#数据库部署)
- [系统监控](#系统监控)
- [常见部署问题](#常见部署问题)

---

## 本地开发测试

### 1. 后端本地运行

```bash
# 1. 确保 MySQL 已启动并初始化数据库
mysql -u root -p < sql/init.sql

# 2. 修改配置文件（如有需要）
vim src/main/resources/application.yml

# 3. 运行测试
mvn test

# 4. 启动后端（开发模式）
mvn spring-boot:run

# 5. 验证服务
curl http://localhost:8080/actuator/health
# 返回: {"status":"UP"}
```

### 2. 后台管理系统本地运行

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
# 访问 http://localhost:5173

# 构建生产版本
npm run build
# 产出目录: frontend/dist/
```

### 3. 微信小程序本地调试

1. 打开微信开发者工具
2. 导入 `miniprogram/` 目录
3. 填写 AppID：`wxe25d5363380f040d`
4. 在「设置 → 项目设置」中勾选「不校验合法域名」
5. 确保后端运行在 `http://localhost:8080`

### 4. 接口联调测试

```bash
# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"CHANGE_ME_ON_FIRST_LOGIN"}'

# 测试商品列表（公开接口）
curl http://localhost:8080/api/products/list

# 测试需要认证的接口
TOKEN="上一步获取的token"
curl http://localhost:8080/api/users/list -H "satoken: $TOKEN"

# 测试文件上传
curl -X POST http://localhost:8080/api/file/upload \
  -H "satoken: $TOKEN" \
  -F "file=@test-image.jpg"

# 测试 Excel 导出
curl -o products.xlsx http://localhost:8080/api/excel/export/products \
  -H "satoken: $TOKEN"
```

---

## 生产环境部署

### 部署架构

```
                        ┌─────────────┐
                        │   浏览器/    │
                        │   小程序     │
                        └──────┬──────┘
                               │
                               ▼
                     ┌─────────────────┐
                     │     Nginx       │
                     │   (80/443端口)   │
                     │                 │
                     │  /  → 前端静态  │
                     │  /api → 后端    │
                     │  /file → 文件   │
                     └──┬──────────┬───┘
                        │          │
                        ▼          ▼
              ┌──────────┐  ┌──────────┐
              │ 前端静态  │  │  后端    │
              │ dist/    │  │ :8080   │
              └──────────┘  └────┬─────┘
                                 │
                    ┌────────────┼────────────┐
                    ▼            ▼            ▼
              ┌──────────┐ ┌──────────┐ ┌──────────┐
              │  MySQL   │ │  Redis   │ │ MongoDB  │
              │  :3306   │ │  :6379   │ │  :27017  │
              └──────────┘ └──────────┘ └──────────┘
```

### 服务器环境准备

```bash
# 以 Ubuntu 22.04 为例

# 1. 安装 JDK 17
sudo apt update
sudo apt install openjdk-17-jdk -y
java -version

# 2. 安装 MySQL 8
sudo apt install mysql-server -y
sudo mysql_secure_installation

# 3. 安装 Nginx
sudo apt install nginx -y
nginx -v

# 4. 安装 Node.js 18（构建前端用，也可在本地构建后上传）
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y

# 5. 安装 Redis（可选）
sudo apt install redis-server -y

# 6. 安装 MongoDB（可选）
# 参考 https://www.mongodb.com/docs/manual/tutorial/install-mongodb-on-ubuntu/
```

---

## Nginx 配置

### 基础配置（HTTP）

创建配置文件 `/etc/nginx/conf.d/supermarket.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或服务器IP

    # 前端静态文件
    location / {
        root /var/www/supermarket/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;  # Vue Router history 模式
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 文件上传大小限制
        client_max_body_size 10m;
    }

    # 文件访问（图片等静态资源）
    location /file/ {
        proxy_pass http://127.0.0.1:8080/file/;
        proxy_set_header Host $host;

        # 缓存静态文件
        proxy_cache_valid 200 1d;
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    # Swagger 文档（生产环境建议关闭）
    location /swagger-ui/ {
        proxy_pass http://127.0.0.1:8080/swagger-ui/;
        proxy_set_header Host $host;
    }

    location /v3/api-docs {
        proxy_pass http://127.0.0.1:8080/v3/api-docs;
        proxy_set_header Host $host;
    }

    # Actuator 监控（限制内网访问）
    location /actuator/ {
        proxy_pass http://127.0.0.1:8080/actuator/;
        proxy_set_header Host $host;
        # allow 10.0.0.0/8;    # 仅允许内网访问
        # deny all;
    }

    # 访问日志
    access_log /var/log/nginx/supermarket_access.log;
    error_log /var/log/nginx/supermarket_error.log;
}
```

### HTTPS 配置（推荐）

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    # SSL 证书（可用 Let's Encrypt 免费获取）
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 前端静态文件
    location / {
        root /var/www/supermarket/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;

        # Gzip 压缩
        gzip on;
        gzip_types text/css application/javascript application/json;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 10m;
    }

    # 文件访问
    location /file/ {
        proxy_pass http://127.0.0.1:8080/file/;
        proxy_set_header Host $host;
        expires 7d;
    }
}
```

### 获取 SSL 证书（Let's Encrypt）

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

---

## 后端部署

### 1. 构建 JAR 包

```bash
# 在开发机器上
mvn clean package -DskipTests
# 产出: target/unmanned-supermarket-0.0.1-SNAPSHOT.jar
```

### 2. 上传到服务器

```bash
scp target/unmanned-supermarket-0.0.1-SNAPSHOT.jar user@server:/opt/supermarket/
```

### 3. 创建生产配置文件

在服务器创建 `/opt/supermarket/application-prod.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/unmanned_supermarket?useSSL=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:supermarket_user}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:}

  mongodb:
    uri: mongodb://localhost:27017/unmanned_supermarket

file:
  upload-dir: /opt/supermarket/file

sa-token:
  token-name: satoken
  timeout: 86400
  is-concurrent: true
  token-style: uuid
```

### 4. 创建 systemd 服务

创建 `/etc/systemd/system/supermarket.service`：

```ini
[Unit]
Description=Unmanned Supermarket Backend
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/supermarket
ExecStart=/usr/bin/java -jar -Xms256m -Xmx512m \
    -Dspring.profiles.active=prod \
    -Dspring.config.additional-location=/opt/supermarket/application-prod.yml \
    /opt/supermarket/unmanned-supermarket-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

# 环境变量（敏感信息）
Environment=DB_PASSWORD=your_db_password
Environment=REDIS_PASSWORD=your_redis_password

[Install]
WantedBy=multi-user.target
```

### 5. 启动服务

```bash
# 重新加载 systemd
sudo systemctl daemon-reload

# 启动
sudo systemctl start supermarket

# 设置开机自启
sudo systemctl enable supermarket

# 查看状态
sudo systemctl status supermarket

# 查看日志
sudo journalctl -u supermarket -f
```

---

## 数据库部署

### MySQL 配置

```bash
# 创建数据库用户
mysql -u root -p
```

```sql
-- 创建专用数据库用户
CREATE USER 'supermarket_user'@'localhost' IDENTIFIED BY 'your_strong_password';
GRANT ALL PRIVILEGES ON unmanned_supermarket.* TO 'supermarket_user'@'localhost';
FLUSH PRIVILEGES;

-- 初始化数据库
source /opt/supermarket/sql/init.sql;
```

### 数据库备份

```bash
# 手动备份
mysqldump -u supermarket_user -p unmanned_supermarket > backup_$(date +%Y%m%d).sql

# 定时备份（添加到 crontab）
# 每天凌晨 3 点备份
0 3 * * * mysqldump -u supermarket_user -pYOUR_PASSWORD unmanned_supermarket > /opt/backups/supermarket_$(date +\%Y\%m\%d).sql
```

---

## 前端部署

### 1. 修改 API 基础路径

部署前，修改 `frontend/src/api/request.ts`：

```typescript
// 开发环境
const BASE_URL = 'http://localhost:8080'

// 生产环境（通过 Nginx 代理后前后端同域，无需跨域）
// const BASE_URL = ''
```

### 2. 构建并部署

```bash
cd frontend

# 构建
npm run build

# 部署到 Nginx 目录
sudo mkdir -p /var/www/supermarket/frontend
sudo cp -r dist/* /var/www/supermarket/frontend/
```

### 3. 微信小程序部署

1. 修改 `miniprogram/utils/api.js` 中的 `baseUrl` 为生产域名
2. 在微信公众平台配置服务器域名
3. 使用微信开发者工具上传代码并提交审核

---

## 系统监控

### Spring Boot Actuator

| 端点 | 说明 |
|------|------|
| `/actuator/health` | 健康检查（数据库、Redis、磁盘空间） |
| `/actuator/info` | 应用信息 |
| `/actuator/metrics` | 性能指标（JVM、HTTP 请求统计） |
| `/actuator/env` | 环境变量 |

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 查看 JVM 内存
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# 查看 HTTP 请求统计
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Nginx 监控

```bash
# 实时查看访问日志
tail -f /var/log/nginx/supermarket_access.log

# 查看错误日志
tail -f /var/log/nginx/supermarket_error.log

# Nginx 状态
sudo nginx -t     # 检查配置
sudo systemctl status nginx
```

---

## 常见部署问题

### Q: Nginx 启动失败？
```bash
# 检查配置语法
sudo nginx -t
# 检查端口占用
sudo lsof -i :80
```

### Q: 后端连不上 MySQL？
```bash
# 检查 MySQL 状态
sudo systemctl status mysql
# 检查连接
mysql -u supermarket_user -p -e "SELECT 1"
```

### Q: 前端页面刷新后 404？
确保 Nginx 配置中有 `try_files $uri $uri/ /index.html;`，这是 Vue Router history 模式必需的。

### Q: 文件上传后访问不到图片？
```bash
# 检查文件目录权限
sudo chown -R www-data:www-data /opt/supermarket/file/
sudo chmod 755 /opt/supermarket/file/
```

### Q: 小程序请求被拒绝？
在微信公众平台 → 开发管理 → 开发设置 → 服务器域名中添加：
- request 合法域名：`https://your-domain.com`
- uploadFile 合法域名：`https://your-domain.com`
- downloadFile 合法域名：`https://your-domain.com`

### Q: 如何查看后端日志？
```bash
# systemd 日志
sudo journalctl -u supermarket -f --since "1 hour ago"
```

### Q: 如何更新部署？
```bash
# 1. 构建新 JAR
mvn clean package -DskipTests

# 2. 上传到服务器
scp target/unmanned-supermarket-0.0.1-SNAPSHOT.jar user@server:/opt/supermarket/

# 3. 重启服务
sudo systemctl restart supermarket

# 4. 更新前端（如有变动）
cd frontend && npm run build
sudo cp -r dist/* /var/www/supermarket/frontend/
```
