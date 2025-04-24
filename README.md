# 云图分享平台项目介绍

一款企业级的图片管理平台，支持公共图库、私有图库和团队共享图库三大核心功能。采用分布式存储和智能检索

技术，实现图片的高效管理。整合 AI 能力实现智能阔图任务，支持 WebSocket 实现多人实时协同编辑。


## 技术选型

### 后端

- Java Spring Boot 框架
- MySQL 数据库 + MyBatis-Plus 框架 + MyBatis X 
- Redis 分布式缓存 + Caffeine 本地缓存
- Jsoup 数据抓取
- ⭐️ COS 对象存储
- ⭐️ ShardingSphere 分库分表
- ⭐️ Sa-Token 权限控制
- ⭐️ WebSocket 双向通信
- ⭐️ JUC 并发和异步编程
- ⭐️ AI 绘图大模型接入
- ⭐️ 多种设计模式的运用
- ⭐️ 多角度项目优化：性能、成本、安全性等



### 前端

- Vue 3 框架
- Vite 打包工具
- Ant Design Vue 组件库
- Axios 请求库
- Pinia 全局状态管理
- 其他组件：数据可视化、图片编辑等
- ⭐️ 前端工程化：ESLint + Prettier + TypeScript
- ⭐️ OpenAPI 前端代码生成

项目架构设计图：



![](https://pic.yupi.icu/1/1732691889100-e562c709-cffa-477d-9329-1dc5ac1d35c8-20241204144304741-20241204145344935-20241204145354234.png)


## 快速开始

### 后端启动

1. 配置数据库和中间件 
2. 修改配置文件 
3. 运行 mvn spring-boot:run


### 前端启动
1. 安装依赖：npm install
2. 开发环境：npm run dev
3. 生产构建：npm run build
