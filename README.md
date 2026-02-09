# 基于Java的校园跑腿接单与配送系统

## 项目简介

本项目是一个基于Java技术栈开发的校园跑腿接单与配送系统，旨在为高校师生提供便捷的跑腿服务。系统采用前后端分离架构，后端使用Spring Boot + MyBatis-Plus + MySQL，前端使用Uniapp开发微信小程序。

## 技术架构

### 后端技术栈
- **Spring Boot 3.2.0** - 核心框架
- **MyBatis-Plus 3.5.5** - ORM框架
- **MySQL 8.0** - 数据库
- **Redis** - 缓存
- **JWT** - 身份认证
- **WebSocket** - 实时通信
- **Knife4j** - API文档

### 前端技术栈
- **Uniapp** - 跨平台开发框架
- **Vue 3** - 前端框架
- **微信小程序** - 运行平台

## 项目结构

```
campus-errand-system/
├── campus-errand-api/          # 后端API服务
│   ├── src/main/java/com/campus/errand/
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # 数据访问层
│   │   ├── entity/             # 实体类
│   │   └── config/             # 配置类
│   └── src/main/resources/
│       └── application.yml     # 配置文件
│
├── campus-errand-common/       # 公共模块
│   └── src/main/java/com/campus/errand/
│       ├── common/             # 公共类
│       ├── entity/             # 基础实体
│       └── util/               # 工具类
│
├── campus-errand-uniapp/       # 前端Uniapp项目
│   ├── pages/                  # 页面目录
│   ├── utils/                  # 工具函数
│   └── static/                 # 静态资源
│
└── sql/                        # 数据库脚本
    └── init.sql                # 初始化脚本
```

## 核心功能

### 用户模块
- 微信授权登录
- 实名认证
- 个人信息管理
- 地址簿管理

### 任务模块
- 发布跑腿任务
- 任务浏览与搜索
- 抢单接单
- 任务状态跟踪

### 订单模块
- 订单创建与管理
- 订单状态流转
- 订单评价

### 支付模块
- 钱包管理
- 余额支付
- 交易流水

### 消息模块
- 系统通知
- 订单消息推送

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+
- HBuilderX (Uniapp开发)

### 后端启动

1. 创建数据库
```sql
mysql -u root -p < sql/init.sql
```

2. 修改配置文件
编辑 `campus-errand-api/src/main/resources/application.yml`，配置数据库和Redis连接信息。

3. 编译运行
```bash
mvn clean install
mvn spring-boot:run -pl campus-errand-api
```

### 前端启动

1. 使用HBuilderX打开 `campus-errand-uniapp` 目录

2. 点击运行到微信小程序模拟器

## API文档
┌─────────────────────────────────────────────────────────────┐
│                        前端层                                │
├─────────────────┬─────────────────┬─────────────────────────┤
│   微信小程序端   │     H5端        │     后台管理Web端        │
│   (Uniapp)      │   (Uniapp)      │    (Vue.js + ElementUI) │
└────────┬────────┴────────┬────────┴───────────┬─────────────┘
         │                 │                    │
         └─────────────────┴────────────────────┘
                             │
                    ┌────────▼────────┐
                    │    API网关层     │
                    │  (统一接口管理)   │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
┌────────▼────────┐ ┌────────▼────────┐ ┌───────▼─────────┐
│   业务服务层     │ │   业务服务层     │ │   业务服务层     │
│   用户服务       │ │   订单服务       │ │   支付服务       │
│  (User Service) │ │ (Order Service) │ │(Payment Service)│
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │
         └───────────────────┼───────────────────┘
                             │
                    ┌────────▼────────┐
                    │   数据访问层     │
                    │   MyBatis-Plus  │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │   数据存储层     │
                    │   MySQL 8.0     │
                    │   Redis (缓存)   │
                    └─────────────────┘
启动后端服务后，访问以下地址查看API文档：
- Knife4j文档：http://localhost:8080/api/doc.html

## 数据库设计

### 核心表
- `tb_user` - 用户表
- `tb_user_wallet` - 用户钱包表
- `tb_transaction` - 交易流水表
- `tb_task` - 跑腿任务表
- `tb_evaluation` - 评价表
- `tb_message` - 消息通知表

## 开发计划

- [x] 项目架构搭建
- [x] 数据库设计
- [x] 基础代码生成
- [x] 前端框架搭建
- [ ] 用户模块开发
- [ ] 任务模块开发
- [ ] 订单模块开发
- [ ] 支付模块开发
- [ ] 消息推送功能
- [ ] 后台管理系统

## 贡献指南

欢迎提交Issue和Pull Request。

## 许可证

MIT License
