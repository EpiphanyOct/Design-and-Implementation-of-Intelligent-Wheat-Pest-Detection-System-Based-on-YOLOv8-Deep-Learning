# =============================================
# 小麦害虫检测系统 - 数据库初始化脚本
# =============================================

set -e

echo "========================================="
echo "小麦害虫检测系统数据库初始化脚本"
echo "========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 数据库配置
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-wheat_pest_db}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-rootpassword}"

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# 检查MySQL连接
echo -e "${YELLOW}检查MySQL连接...${NC}"
if ! mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASS}" -e "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${RED}无法连接到MySQL数据库，请检查配置${NC}"
    exit 1
fi
echo -e "${GREEN}MySQL连接成功${NC}"

# 创建数据库
echo -e "${YELLOW}创建数据库...${NC}"
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASS}" <<EOF
CREATE DATABASE IF NOT EXISTS ${DB_NAME} 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
EOF

echo -e "${GREEN}数据库创建成功${NC}"

# 执行数据库迁移
echo -e "${YELLOW}执行数据库迁移...${NC}"
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" < "${PROJECT_ROOT}/src/database/migrations/001_initial_schema.sql"
echo -e "${GREEN}数据库迁移完成${NC}"

# 导入初始数据
echo -e "${YELLOW}导入初始数据...${NC}"
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" < "${PROJECT_ROOT}/src/database/seeds/initial_data.sql"
echo -e "${GREEN}初始数据导入完成${NC}"

# 创建应用用户
echo -e "${YELLOW}创建应用用户...${NC}"
mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASS}" <<EOF
CREATE USER IF NOT EXISTS 'wheat_pest'@'localhost' IDENTIFIED BY 'wheat_pest_password';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO 'wheat_pest'@'localhost';
FLUSH PRIVILEGES;
EOF
echo -e "${GREEN}应用用户创建完成${NC}"

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}数据库初始化完成！${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "数据库信息:"
echo "  主机: ${DB_HOST}"
echo "  端口: ${DB_PORT}"
echo "  数据库名: ${DB_NAME}"
echo "  应用用户: wheat_pest"
echo ""

