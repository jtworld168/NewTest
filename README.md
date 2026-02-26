# 智慧零售系统 (Unmanned Supermarket)

一套支持多店铺管理的智慧零售解决方案，包含 Spring Boot 后端、Vue 3 管理后台、微信小程序与 UniApp 客户端。

## 模块概览

| 模块 | 技术栈 | 说明 |
|------|--------|------|
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus + MySQL 8 | RESTful API 服务 |
| 后台管理 | Vue 3 + TypeScript + Element Plus + ECharts | 管理员控制面板 |
| 微信小程序 | 原生小程序 | 顾客端（C端） |
| UniApp | Vue 3 + UniApp | 跨平台顾客端 |

## 快速上手

详细的环境搭建、启动步骤和接口示例请阅读 **[docs/QUICKSTART.md](docs/QUICKSTART.md)**。

部署说明请参阅 **[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)**，系统设计文档请参阅 **[docs/DESIGN.md](docs/DESIGN.md)**。

## 如何使用 GitHub Copilot 修改本仓库代码

您可以通过以下步骤让 GitHub Copilot Coding Agent 帮您修改代码：

1. **在 GitHub 上创建 Issue**：在本仓库的 [Issues](../../issues) 页面点击 **New issue**，用自然语言描述您希望进行的改动（例如"在商品列表接口中增加按分类过滤的功能"）。
2. **指派给 Copilot**：在 Issue 右侧的 **Assignees** 中选择 `Copilot`，然后提交 Issue。
3. **等待 Copilot 分析**：Copilot 会自动分析您的需求、在新分支上进行代码修改，并创建一个 Pull Request。
4. **Review 并合并**：在 Pull Request 页面审查 Copilot 提交的改动，满意后合并到主分支即可。

> **提示**：Issue 描述越具体，Copilot 的修改结果越精准。例如，可以指明涉及的文件路径、期望的接口格式或业务规则。