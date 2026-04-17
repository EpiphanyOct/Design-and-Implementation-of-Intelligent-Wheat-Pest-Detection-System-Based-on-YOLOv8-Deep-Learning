# =============================================
# 小麦害虫检测系统 - 构建脚本
# =============================================

set -e

echo "========================================="
echo "小麦害虫检测系统构建脚本"
echo "========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# 构建后端
echo -e "${YELLOW}[1/3] 构建后端...${NC}"
cd "${PROJECT_ROOT}/src/backend"
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}后端构建成功${NC}"
else
    echo -e "${RED}后端构建失败${NC}"
    exit 1
fi

# 构建前端
echo -e "${YELLOW}[2/3] 构建前端...${NC}"
cd "${PROJECT_ROOT}/src/frontend"
npm install
npm run build
if [ $? -eq 0 ]; then
    echo -e "${GREEN}前端构建成功${NC}"
else
    echo -e "${RED}前端构建失败${NC}"
    exit 1
fi

# 复制构建产物
echo -e "${YELLOW}[3/3] 复制构建产物...${NC}"
mkdir -p "${PROJECT_ROOT}/dist"
cp "${PROJECT_ROOT}/src/backend/target/"*.jar "${PROJECT_ROOT}/dist/"
cp -r "${PROJECT_ROOT}/src/frontend/build" "${PROJECT_ROOT}/dist/frontend"

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}构建完成！${NC}"
echo -e "${GREEN}后端: dist/wheat-pest-detection-*.jar${NC}"
echo -e "${GREEN}前端: dist/frontend/${NC}"
echo -e "${GREEN}=========================================${NC}"
