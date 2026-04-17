# 小麦害虫检测系统 


## 项目简介

小麦害虫检测系统是一款专为农业害虫监测与管理领域设计的智能化平台。该系统集成了用户管理、虫害识别、虫害信息库、实时监测、数据管理、预警设置、报告生成以及设备控制等多项功能，旨在提升小麦害虫监测的效率与准确性，优化农业资源管理，确保小麦生产的安全与稳定。

## 主要功能

- **用户管理**：支持不同角色的权限设置，确保系统操作的安全性和保密性
- **虫害识别**：上传虫害图像并获取快速准确的识别结果
- **虫害信息库**：详细记录各类害虫的特征、习性及防治方法
- **实时监测**：通过监测设备实时采集虫害数据
- **数据管理**：对监测数据进行存储、备份和查询
- **预警设置**：根据害虫数量或其他指标设置预警阈值
- **报告生成**：根据监测和识别数据生成详细的分析报告
- **设备控制**：远程控制监测设备，确保设备的高效运行

## 技术栈

### 后端
- **语言**：Java 11
- **框架**：Spring Boot 2.7+
- **数据库**：MySQL 8.0
- **ORM**：JPA / MyBatis
- **构建工具**：Maven 3.8+

### 前端
- **语言**：JavaScript (ES6+)
- **框架**：React 18
- **UI组件库**：Ant Design 5
- **状态管理**：React Hooks
- **HTTP客户端**：Axios
- **日期处理**：Day.js

### 开发工具
- **IDE**：Eclipse / IntelliJ IDEA / VS Code
- **版本控制**：Git
- **容器化**：Docker (可选)

## 项目结构

```
pest-query-system/
├── README.md                      # 项目说明
├── .gitignore                     # Git忽略文件
├── LICENSE                        # 开源协议
├── docs/                          # 文档文件夹
│   ├── api-documentation.md       # API文档
│   └── deployment-guide.md        # 部署指南
├── iteration-1/                   # 迭代1说明
├── src/                           # 源代码
│   ├── frontend/                  # 前端代码
│   │   ├── components/            # 组件
│   │   ├── pages/                 # 页面
│   │   ├── services/              # API调用服务
│   │   └── assets/                # 图片、样式文件
│   ├── backend/                   # 后端代码
│   │   ├── controllers/           # 控制器
│   │   ├── models/                # 数据模型
│   │   ├── routes/                # 路由定义
│   │   ├── services/              # 业务逻辑
│   │   └── tests/                 # 后端测试文件
│   └── database/                  # 数据库脚本
│       ├── migrations/            # 数据库迁移文件
│       └── seeds/                 # 初始数据
├── tests/                         # 测试文件
│   ├── acceptance-tests/          # 验收测试
│   └── unit-tests/                # 单元测试
├── scripts/                       # 工具脚本
└── third-party/                   # 第三方代码
    └── README.md                  # 第三方代码说明
```

## 快速开始

### 环境要求

- Java 11 或更高版本
- Node.js 16 或更高版本
- MySQL 8.0 或更高版本
- Maven 3.8 或更高版本

### 后端部署

1. 克隆项目
```bash
git clone https://github.com/yourusername/pest-query-system.git
cd pest-query-system
```

2. 配置数据库
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE wheat_pest_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 执行数据库迁移
mysql -u root -p wheat_pest_db < src/database/migrations/001_initial_schema.sql
```

3. 修改数据库配置
编辑 `src/backend/src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wheat_pest_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. 构建并运行
```bash
cd src/backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端部署

1. 安装依赖
```bash
cd src/frontend
npm install
```

2. 配置API地址
编辑 `.env` 文件：
```
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

3. 启动开发服务器
```bash
npm start
```

前端应用将在 `http://localhost:3000` 启动

## API文档

详细的API文档请参考 [docs/api-documentation.md](docs/api-documentation.md)

### 主要API接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/wheat-pest-detection/detect | POST | 害虫图片识别 |
| /api/wheat-pest-detection/history | GET | 查询检测历史 |
| /api/wheat-pest-detection/batch-detect | POST | 批量害虫检测 |
| /api/wheat-pest-detection/pest-info | GET | 查询害虫信息 |
| /api/wheat-pest/environment-data | POST | 上传环境数据 |
| /api/wheat-pest/pest-warnings | GET | 获取虫害预警 |


## 开发团队

- 项目负责人：[Your Name]
- 后端开发：[Developer Name]
- 前端开发：[Developer Name]
- 测试人员：[Tester Name]

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

## 更新日志

### v1.0.0 (2024-04-17)
- 初始版本发布
- 实现用户管理、虫害识别、虫害信息库等核心功能




