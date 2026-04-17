# 部署指南

## 系统要求

### 硬件环境

**服务器端**:
- CPU: Intel Xeon E5-2650 V2 或同等性能
- 内存: 32GB 或更高
- 硬盘: 4TB 存储空间
- 网络: 100Mbps 带宽

**客户端**:
- CPU: Intel i3-12300 或同等性能
- 内存: 64GB 或更高
- 硬盘: 128GB SSD
- 显卡: 支持HDMI 2.0输出

### 软件环境

**服务器端**:
- 操作系统: Windows Server 2003 / Ubuntu 22.04 LTS
- 运行时: .NET Framework 4.8 / Java 11
- 数据库: MySQL 8.0
- Web服务器: Nginx / Apache

**客户端**:
- 操作系统: Windows 8.1 或更高
- 浏览器: Chrome 90+, Firefox 88+, Edge 90+
- 运行时: .NET Framework 4.0

## 部署方式

### 方式一：传统部署

#### 1. 环境准备

**安装Java**
```bash
# Ubuntu
sudo apt update
sudo apt install openjdk-11-jdk

# 验证安装
java -version
```

**安装MySQL**
```bash
# Ubuntu
sudo apt install mysql-server-8.0

# 安全配置
sudo mysql_secure_installation
```

**安装Node.js**
```bash
# 使用nvm安装
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 16
nvm use 16

# 验证安装
node -v
npm -v
```

#### 2. 数据库部署

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE wheat_pest_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户
CREATE USER 'wheat_pest'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON wheat_pest_db.* TO 'wheat_pest'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# 执行数据库迁移
cd /path/to/project
mysql -u wheat_pest -p wheat_pest_db < src/database/migrations/001_initial_schema.sql

# 导入初始数据
mysql -u wheat_pest -p wheat_pest_db < src/database/seeds/initial_data.sql
```

#### 3. 后端部署

```bash
# 进入后端目录
cd src/backend

# 修改配置文件
vim src/main/resources/application.properties

# 关键配置项
spring.datasource.url=jdbc:mysql://localhost:3306/wheat_pest_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=wheat_pest
spring.datasource.password=your_password

# 构建项目
mvn clean package -DskipTests

# 运行应用
java -jar target/wheat-pest-detection-1.0.0.jar

# 或使用Maven运行
mvn spring-boot:run
```

#### 4. 前端部署

```bash
# 进入前端目录
cd src/frontend

# 安装依赖
npm install

# 配置API地址
echo "REACT_APP_API_BASE_URL=http://localhost:8080/api" > .env

# 构建生产版本
npm run build

# 部署到Nginx
sudo cp -r build/* /var/www/html/

# 或使用serve运行
npm install -g serve
serve -s build -l 3000
```

## 5. Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /var/www/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 上传文件
    location /uploads/ {
        alias /var/www/uploads/;
    }
}
```

#### 3. 启动服务

```bash
# 构建并启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 配置说明

### 后端配置 (application.properties)

```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/api

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/wheat_pest_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=wheat_pest
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# 文件上传配置
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB

# 日志配置
logging.level.root=INFO
logging.level.com.topwheat.pestdetect=DEBUG
logging.file.name=logs/wheat-pest-detection.log

# JWT配置
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000

# 图片存储路径
app.upload.path=/var/www/uploads/
app.upload.url=/uploads/
```

### 前端配置 (.env)

```
# API基础URL
REACT_APP_API_BASE_URL=http://localhost:8080/api

# 图片基础URL
REACT_APP_IMAGE_BASE_URL=http://localhost:8080/uploads

# 系统名称
REACT_APP_SYSTEM_NAME=小麦害虫检测系统

# 版本号
REACT_APP_VERSION=1.0.0
```

## 系统监控

### 健康检查

```bash
# 后端健康检查
curl http://localhost:8080/api/actuator/health

# 数据库连接检查
curl http://localhost:8080/api/actuator/health/db
```

### 日志监控

```bash
# 实时查看日志
tail -f logs/wheat-pest-detection.log

# 使用grep过滤
tail -f logs/wheat-pest-detection.log | grep ERROR
```

## 备份与恢复

### 数据库备份

```bash
# 完整备份
mysqldump -u wheat_pest -p wheat_pest_db > backup_$(date +%Y%m%d).sql

# 定时备份（crontab）
0 2 * * * mysqldump -u wheat_pest -p'password' wheat_pest_db > /backup/db_$(date +\%Y\%m\%d).sql
```

### 数据库恢复

```bash
# 恢复数据
mysql -u wheat_pest -p wheat_pest_db < backup_20240101.sql
```

## 常见问题

### 1. 数据库连接失败

**问题**: 应用启动时报数据库连接错误

**解决方案**:
- 检查MySQL服务是否启动
- 验证数据库配置是否正确
- 确认用户权限是否足够

### 2. 前端无法访问API

**问题**: 前端页面报错，无法获取数据

**解决方案**:
- 检查后端服务是否正常运行
- 验证API地址配置是否正确
- 检查跨域配置

### 3. 图片上传失败

**问题**: 上传图片时报错

**解决方案**:
- 检查上传目录权限
- 验证文件大小限制
- 确认文件格式支持

## 安全建议

1. **修改默认密码**: 部署后立即修改所有默认账号密码
2. **启用HTTPS**: 生产环境使用HTTPS加密通信
3. **防火墙配置**: 只开放必要的端口
4. **定期更新**: 及时更新系统和依赖库
5. **日志审计**: 定期检查系统日志
