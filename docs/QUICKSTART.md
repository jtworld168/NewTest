# 无人超市管理系统 — 快速上手指南

## 目录

- [项目简介](#项目简介)
- [环境要求](#环境要求)
- [快速启动](#快速启动)
- [后端启动](#后端启动)
- [后台管理系统启动](#后台管理系统启动)
- [微信小程序启动](#微信小程序启动)
- [UniApp启动](#uniapp启动)
- [默认账号](#默认账号)
- [常用接口测试](#常用接口测试)
- [常见问题](#常见问题)

---

## 项目简介

无人超市管理系统是一套支持多店铺管理的零售解决方案，包含：

| 模块 | 技术栈 | 说明 |
|------|--------|------|
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus + MySQL 8 | RESTful API 服务 |
| 后台管理 | Vue 3 + TypeScript + Element Plus + ECharts | 管理员控制面板 |
| 微信小程序 | 原生小程序 | 顾客端（C端） |
| UniApp | Vue 3 + UniApp | 跨平台顾客端 |

---

## 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端构建工具 |
| MySQL | 8.0+ | 数据库 |
| Node.js | 18+ | 前端构建 |
| npm | 9+ | 前端包管理 |
| Redis | 6+ | 缓存（可选，系统会自动检测） |
| MongoDB | 6+ | 操作日志（可选，系统会自动检测） |

---

## 快速启动

### 1. 克隆项目

```bash
git clone https://github.com/jtworld168/NewTest.git
cd NewTest
```

### 2. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source sql/init.sql;
```

该脚本会自动：
- 创建 `unmanned_supermarket` 数据库
- 创建全部 12 张表（含外键、索引、约束）
- 插入测试数据（5个用户、8个商品、4个分类、3个店铺等）

### 3. 启动后端

```bash
# 修改数据库配置（如果不是默认的 root/root）
# 编辑 src/main/resources/application.yml

# 构建并启动
mvn clean package -DskipTests
java -jar target/unmanned-supermarket-0.0.1-SNAPSHOT.jar

# 或直接运行
mvn spring-boot:run
```

后端启动后访问：
- API 服务：http://localhost:8080
- Swagger 文档：http://localhost:8080/swagger-ui/index.html
- Actuator 健康检查：http://localhost:8080/actuator/health

### 4. 启动后台管理系统

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173，使用管理员账号登录。

### 5. 启动微信小程序

1. 下载 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 导入 `miniprogram/` 目录
3. AppID 填写：`wxe25d5363380f040d`
4. 确保后端已启动（http://localhost:8080）

### 6. 启动 UniApp（可选）

1. 下载 [HBuilderX](https://www.dcloud.io/hbuilderx.html)
2. 导入 `uniapp/` 目录
3. 运行到微信小程序或 H5

---

## 默认账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | CHANGE_ME_ON_FIRST_LOGIN | 管理员 | 后台管理系统 |
| emp_zhang | 123456 | 员工 | 酒店员工，享受内购价 |
| emp_li | 123456 | 员工 | 酒店员工 |
| cust_wang | 123456 | 顾客 | 普通顾客 |
| cust_zhao | 123456 | 顾客 | 普通顾客 |

> **注意**：首次部署后请立即修改 admin 密码。密码使用 Hutool BCrypt 加密存储。

---

## 常用接口测试

### 登录获取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"CHANGE_ME_ON_FIRST_LOGIN"}'
```

响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "user": { "id": 1, "username": "admin", "role": "ADMIN" },
    "token": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
  }
}
```

### 带 Token 请求

```bash
# 查询商品列表（公开接口，无需 Token）
curl http://localhost:8080/api/products/list

# 新增商品（需要 Token）
curl -X POST http://localhost:8080/api/products/add \
  -H "Content-Type: application/json" \
  -H "satoken: YOUR_TOKEN_HERE" \
  -d '{"name":"测试商品","price":9.90,"stock":100,"categoryId":1}'
```

### 查看 Swagger 文档

浏览器访问 http://localhost:8080/swagger-ui/index.html 查看所有 API 接口详情。

---

## 环境切换

本项目支持三套环境（开发、测试、生产），通过 Spring Profiles 和前端环境变量切换：

### 后端切换

```bash
# 开发环境（默认）
mvn spring-boot:run

# 测试环境
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 生产环境
java -jar target/unmanned-supermarket-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

配置文件：`application-dev.yml` / `application-test.yml` / `application-prod.yml`

### 前端切换

```bash
npm run dev                    # 开发（使用 .env.development）
npm run build                  # 生产（使用 .env.production）
npm run build -- --mode test   # 测试（使用 .env.test）
```

> 详细部署指南请参阅 [DEPLOYMENT.md](./DEPLOYMENT.md)。

---

## 常见问题

### Q: 启动报数据库连接失败？
**A**: 检查 `src/main/resources/application.yml` 中的数据库地址、用户名、密码配置。确保 MySQL 已启动且 `unmanned_supermarket` 数据库已创建。

### Q: Redis/MongoDB 未安装怎么办？
**A**: Redis 和 MongoDB 为可选服务。系统使用 `@ConditionalOnBean` 注解，未检测到连接时会自动跳过，不影响核心功能。

### Q: 前端请求后端跨域报错？
**A**: 后端已配置 CORS（WebMvcConfig），允许所有来源。如果仍然报错，检查后端是否正在运行。

### Q: 图片上传到哪里了？
**A**: 图片存储在后端项目根目录的 `file/` 文件夹中，通过 `http://localhost:8080/file/xxx.jpg` 访问。

### Q: 如何导出 Excel 表格？
**A**: 在后台管理系统的商品、用户、订单页面，点击"导出Excel"按钮。也可以直接调用：
```bash
curl -o products.xlsx http://localhost:8080/api/excel/export/products \
  -H "satoken: YOUR_TOKEN"
```

### Q: 小程序扫码功能在模拟器上不能用？
**A**: 扫码功能需要在真机上测试，模拟器不支持摄像头调用。
