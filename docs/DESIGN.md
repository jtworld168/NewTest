# 无人超市管理系统 — 系统设计文档

## 目录

- [系统架构](#系统架构)
- [技术栈](#技术栈)
- [数据库设计](#数据库设计)
- [接口设计](#接口设计)
- [认证与权限](#认证与权限)
- [核心业务流程](#核心业务流程)
- [前端架构](#前端架构)

---

## 系统架构

```
┌─────────────────────────────────────────────────┐
│                    客户端                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ 后台管理  │  │ 微信小程序│  │  UniApp  │       │
│  │ Vue3+TS  │  │  原生WXML │  │  Vue3    │       │
│  │ Element+ │  │          │  │          │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │              │              │             │
│       └──────────────┼──────────────┘             │
│                      │ HTTP/REST                  │
│                      ▼                            │
│  ┌─────────────────────────────────────────┐     │
│  │         Spring Boot 3.2.5               │     │
│  │  ┌───────────┐  ┌────────────────────┐  │     │
│  │  │ Sa-Token  │  │    Controller      │  │     │
│  │  │   认证     │  │   (16个控制器)     │  │     │
│  │  └───────────┘  └────────┬───────────┘  │     │
│  │                          │              │     │
│  │  ┌────────────────────────────────────┐ │     │
│  │  │   Service Layer (11个服务)         │ │     │
│  │  │   LambdaQueryWrapper + 业务逻辑    │ │     │
│  │  └────────────────┬───────────────────┘ │     │
│  │                   │                     │     │
│  │  ┌────────────────┴───────────────────┐ │     │
│  │  │   MyBatis-Plus (Mapper层)          │ │     │
│  │  └────────────────┬───────────────────┘ │     │
│  └───────────────────┼─────────────────────┘     │
│                      │                            │
│  ┌───────┐  ┌────────┴──┐  ┌─────────┐           │
│  │ Redis │  │  MySQL 8  │  │ MongoDB │           │
│  │ 缓存   │  │  主数据库  │  │ 操作日志 │           │
│  └───────┘  └───────────┘  └─────────┘           │
└─────────────────────────────────────────────────┘
```

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 应用框架 |
| MyBatis-Plus | 3.5.6 | ORM 框架（LambdaQueryWrapper 查询） |
| Sa-Token | 1.39.0 | 登录认证与权限管理 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存（可选） |
| MongoDB | 6.0+ | 操作日志存储（可选） |
| EasyExcel | 4.0.3 | Excel 导入导出 |
| Hutool BCrypt | 5.8.32 | 密码加密 |
| SpringDoc OpenAPI | 2.5.0 | Swagger API 文档 |
| Spring Boot Actuator | 3.2.5 | 系统监控 |

### 前端

| 技术 | 用途 |
|------|------|
| Vue 3 + TypeScript | 后台管理系统框架 |
| Element Plus | 后台管理 UI 组件库 |
| ECharts | 数据统计图表 |
| Pinia | 状态管理 |
| Vue Router | 路由管理 |
| Axios | HTTP 客户端 |

### 移动端

| 技术 | 用途 |
|------|------|
| 微信小程序原生 | 顾客端（WXML + WXSS + JS） |
| UniApp (Vue 3) | 跨平台顾客端 |

---

## 数据库设计

### ER 关系图

```
                    ┌──────────┐
                    │ category │
                    │──────────│
                    │ id (PK)  │
                    │ name     │
                    └────┬─────┘
                         │ 1:N
                         ▼
┌──────────┐      ┌──────────┐      ┌───────────────┐
│  store   │      │ product  │      │ store_product  │
│──────────│      │──────────│      │───────────────│
│ id (PK)  │◄────►│ id (PK)  │◄────►│ id (PK)       │
│ name     │  N:M │ name     │  N:M │ store_id (FK)  │
│ address  │      │ price    │      │ product_id(FK) │
│ status   │      │ stock    │      │ store_price    │
└────┬─────┘      │ barcode  │      │ store_stock    │
     │            │ image    │      └───────────────┘
     │            │ status   │
     │            └──┬───┬───┘
     │               │   │
     │    ┌──────────┘   └──────────┐
     │    │                         │
     │    ▼                         ▼
     │  ┌──────────┐         ┌───────────┐
     │  │cart_item  │         │order_items │
     │  │──────────│         │───────────│
     │  │ id (PK)  │         │ id (PK)   │
     │  │ user_id  │         │ order_id  │◄────┐
     │  │product_id│         │product_id │     │
     │  │ quantity │         │ quantity  │     │
     │  └──┬───────┘         │ subtotal  │     │
     │     │                 └───────────┘     │
     │     │                                   │
     │     ▼                                   │
     │  ┌──────────┐      ┌──────────┐         │
     │  │   user   │      │  order   │─────────┘
     │  │──────────│      │──────────│
     │  │ id (PK)  │◄────►│ id (PK)  │
     │  │ username │  1:N │ user_id  │
     │  │ password │      │ store_id │◄────────── store
     │  │ role     │      │ status   │
     │  │ avatar   │      │ total_   │
     │  │is_hotel_ │      │  amount  │
     │  │ employee │      └──┬───┬───┘
     │  └──┬───────┘         │   │
     │     │                 │   │
     │     ▼                 │   ▼
     │  ┌───────────┐        │ ┌──────────┐
     │  │user_coupon│        │ │ payment  │
     │  │───────────│        │ │──────────│
     │  │ id (PK)   │◄───────┘ │ id (PK)  │
     │  │ user_id   │          │ order_id │
     │  │ coupon_id │          │ amount   │
     │  │ status    │          │ method   │
     │  └──┬────────┘          │ status   │
     │     │                   └──────────┘
     │     ▼
     │  ┌──────────┐
     │  │  coupon  │
     │  │──────────│
     │  │ id (PK)  │
     │  │ name     │
     │  │ discount │
     │  │min_amount│
     │  │total_cnt │
     │  │remain_cnt│
     └──┴──────────┘
```

### 表说明

| 表名 | 说明 | 记录数(测试) |
|------|------|-------------|
| `user` | 用户表（管理员/员工/顾客） | 5 |
| `category` | 商品分类表 | 4 |
| `product` | 总商品表 | 8 |
| `store` | 店铺表 | 3 |
| `store_product` | 店铺商品表（店铺独立定价和库存） | 16 |
| `coupon` | 优惠券面额表（模板） | 3 |
| `user_coupon` | 用户优惠券表（具体发放记录） | 3 |
| `cart_item` | 购物车表 | 3 |
| `order` | 订单表（支持单品和多品订单） | 3 |
| `order_items` | 订单商品明细表（一个订单中的多个商品） | 3 |
| `payment` | 支付记录表 | 3 |
| `operation_logs` | 操作日志表（MongoDB） | — |

### 外键关系

| 外键 | 从表.字段 | 主表.字段 |
|------|-----------|-----------|
| `fk_product_category` | product.category_id | category.id |
| `fk_user_coupon_user` | user_coupon.user_id | user.id |
| `fk_user_coupon_coupon` | user_coupon.coupon_id | coupon.id |
| `fk_sp_store` | store_product.store_id | store.id |
| `fk_sp_product` | store_product.product_id | product.id |
| `fk_order_user` | order.user_id | user.id |
| `fk_order_store` | order.store_id | store.id |
| `fk_order_product` | order.product_id | product.id |
| `fk_order_user_coupon` | order.user_coupon_id | user_coupon.id |
| `fk_order_item_order` | order_items.order_id | order.id |
| `fk_order_item_product` | order_items.product_id | product.id |
| `fk_cart_user` | cart_item.user_id | user.id |
| `fk_cart_product` | cart_item.product_id | product.id |
| `fk_payment_order` | payment.order_id | order.id |

### 约束与索引

- **唯一约束**：user.username、category.name、product.barcode、store.name、payment.transaction_no、store_product(store_id+product_id)
- **CHECK 约束**：product.price>0、product.stock>=0、coupon.discount>0、order.total_amount>0、cart_item.quantity>0、payment.amount>0
- **逻辑删除**：所有表均使用 `deleted` 字段实现软删除（0=正常，1=已删除）

---

## 接口设计

### 接口总览

系统共有 **16 个控制器**，**150+ 个 REST 接口**：

| 控制器 | 路径前缀 | 功能 | 接口数 |
|--------|----------|------|--------|
| AuthController | /api/auth | 登录注册退出 | 3 |
| UserController | /api/users | 用户管理 | 10 |
| CategoryController | /api/categories | 分类管理 | 9 |
| ProductController | /api/products | 商品管理 | 13 |
| StoreController | /api/stores | 店铺管理 | 9 |
| StoreProductController | /api/store-products | 店铺商品管理 | 9 |
| OrderController | /api/orders | 订单管理 | 13 |
| OrderItemController | /api/order-items | 订单明细管理 | 9 |
| CouponController | /api/coupons | 优惠券模板管理 | 9 |
| UserCouponController | /api/user-coupons | 用户优惠券管理 | 10 |
| CartItemController | /api/cart-items | 购物车管理 | 10 |
| PaymentController | /api/payments | 支付管理 | 11 |
| SalesController | /api/sales | 销量统计 | 4 |
| ExcelController | /api/excel | Excel 导入导出 | 6 |
| FileUploadController | /api/file | 文件上传下载 | 3 |
| OperationLogController | /api/logs | 操作日志 | 2 |

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录或 Token 过期 |
| 500 | 服务器内部错误 |

### 订单明细接口（order_items）的作用

`order_items` 表是订单的**商品明细表**，用于支持**一个订单包含多个商品**的场景：

| 接口 | 说明 |
|------|------|
| `GET /api/order-items/getByOrderId/{orderId}` | 查询某个订单的所有商品明细（品名、数量、单价、小计） |
| `POST /api/orders/addMultiItem` | 创建多商品订单时，自动批量插入 order_items 记录 |

**业务流程**：
1. 用户在购物车选择多个商品 → 点击结算
2. 后端 `addMultiItem` 创建一条 `order` 记录 + N 条 `order_items` 记录
3. 每条 `order_item` 记录包含：商品ID、数量、下单时单价（已计算员工折扣）、小计金额
4. `order.total_amount` = 所有 `order_items.subtotal` 之和 — 优惠券折扣

---

## 认证与权限

### Sa-Token 认证流程

```
┌────────┐     POST /api/auth/login      ┌──────────┐
│  客户端 │ ──────────────────────────────►│  后端    │
│        │     { username, password }     │          │
│        │                                │ BCrypt   │
│        │◄────────────────────────────── │ 校验密码  │
│        │     { user, token }            │          │
│        │                                │ StpUtil  │
│        │     GET /api/products/list     │ .login() │
│        │ ──────────────────────────────►│          │
│        │     Header: satoken=xxx        │ 拦截器    │
│        │                                │ 校验Token│
│        │◄────────────────────────────── │          │
│        │     { code:200, data:[...] }   │          │
└────────┘                                └──────────┘
```

### 公开接口（无需 Token）

以下接口允许匿名访问，用于顾客浏览商品：

- `POST /api/auth/login`、`POST /api/auth/register`
- `GET /api/products/**`、`GET /api/categories/**`
- `GET /api/stores/**`、`GET /api/store-products/**`
- `GET /api/coupons/**`
- `GET /file/**`（静态文件访问）
- `GET /swagger-ui/**`、`GET /v3/api-docs/**`
- `GET /actuator/**`

### 受保护接口（需要 Token）

所有写操作（POST/PUT/DELETE）和敏感数据查询都需要 Sa-Token 认证。

---

## 核心业务流程

### 下单流程

```
1. 用户浏览商品 → 加入购物车
2. 购物车页面 → 选择商品 → 点击结算
3. 系统检查：
   a. 是否为酒店员工？→ 计算内购价（price × employeeDiscountRate）
   b. 是否有可用优惠券且满足最低消费？→ 提示使用优惠券
4. 创建订单：POST /api/orders/addMultiItem
   - 创建 order 记录（user_id, store_id, total_amount, status=PENDING）
   - 创建 N 条 order_items 记录（product_id, quantity, price_at_purchase, subtotal）
   - 如果使用优惠券：标记 user_coupon 为 USED，coupon.remaining_count -= 1
5. 跳转支付页面 → 选择支付方式 → 确认支付
6. 创建 payment 记录 → 更新 order.status = PAID
```

### 员工折扣逻辑

```
if (user.isHotelEmployee && product.employeeDiscountRate != null) {
    priceAtPurchase = price × employeeDiscountRate;  // 如 10 × 0.8 = 8.0
} else {
    priceAtPurchase = price;
}
```

**注意**：员工折扣与优惠券**不可叠加使用**。

### 优惠券使用流程

```
1. 管理员创建优惠券模板（coupon表）→ 设置面额、最低消费、数量
2. 发放优惠券给用户（user_coupon表）→ 状态为 AVAILABLE
3. 用户下单时：
   a. 检查订单金额 >= coupon.min_amount
   b. 检查 user_coupon.status == AVAILABLE
   c. 订单总价 -= coupon.discount
   d. user_coupon.status = USED，记录使用时间
   e. coupon.remaining_count -= 1
```

---

## 前端架构

### 后台管理系统

```
frontend/
├── src/
│   ├── api/           # API 请求模块（16个文件，每个对应一个后端控制器）
│   ├── layout/        # 侧边栏 + 顶部导航布局
│   ├── router/        # 路由配置（含权限守卫）
│   ├── stores/        # Pinia 状态管理（用户信息）
│   ├── types/         # TypeScript 类型定义
│   └── views/         # 页面组件
│       ├── dashboard/ # 仪表盘（统计概览 + 低库存预警）
│       ├── statistics/# 数据统计（柱状图/折线图/饼图）
│       ├── user/      # 用户管理
│       ├── product/   # 商品管理
│       ├── category/  # 分类管理
│       ├── order/     # 订单管理
│       ├── store/     # 店铺管理
│       └── ...        # 其他管理页面
```

### 微信小程序

```
miniprogram/
├── app.js / app.json / app.wxss  # 全局配置
├── utils/api.js                   # API 请求封装
├── utils/filter.wxs               # WXS 过滤器
└── pages/
    ├── index/       # 首页（商品展示 + 轮播图 + 扫码）
    ├── category/    # 分类页（侧边栏分类导航）
    ├── cart/        # 购物车（增减商品 + 优惠券 + 结算）
    ├── orders/      # 订单列表（状态筛选）
    ├── order-detail/# 订单详情（商品明细 + 条形码）
    ├── payment/     # 支付页面
    ├── profile/     # 个人中心（头像 + 员工折扣标识）
    ├── coupons/     # 我的优惠券
    ├── login/       # 登录页
    ├── register/    # 注册页
    └── scan/        # 扫码页
```

### 主题设计

- **主色调**：香槟色 `#C9A96E`
- **辅助色**：深棕 `#8B6F47`、浅金 `#E8D5B5`
- **背景色**：`#FFF9F0`（温暖米白）
