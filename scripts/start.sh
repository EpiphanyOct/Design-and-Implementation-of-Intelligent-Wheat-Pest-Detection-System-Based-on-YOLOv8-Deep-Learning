# =============================================
# 小麦害虫检测系统 - 启动脚本
# =============================================

set -e

echo "========================================="
echo "小麦害虫检测系统启动脚本"
echo "========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: 未找到Java环境，请先安装Java 11或更高版本${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo -e "${GREEN}Java版本: ${JAVA_VERSION}${NC}"

# 查找JAR文件
JAR_FILE=$(find "${PROJECT_ROOT}/src/backend/target" -name "wheat-pest-detection-*.jar" 2>/dev/null | head -1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${YELLOW}未找到JAR文件，开始构建...${NC}"
    cd "${PROJECT_ROOT}/src/backend"
    mvn clean package -DskipTests
    JAR_FILE=$(find "${PROJECT_ROOT}/src/backend/target" -name "wheat-pest-detection-*.jar" | head -1)
fi

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}错误: 构建失败，无法找到JAR文件${NC}"
    exit 1
fi

echo -e "${GREEN}JAR文件: ${JAR_FILE}${NC}"

# 日志目录
LOG_DIR="${PROJECT_ROOT}/logs"
mkdir -p "${LOG_DIR}"

# 启动后端
echo -e "${YELLOW}启动后端服务...${NC}"
nohup java -jar "${JAR_FILE}" \
    --server.port=8080 \
    --logging.file.name="${LOG_DIR}/wheat-pest-detection.log" \
    > "${LOG_DIR}/startup.log" 2>&1 &

BACKEND_PID=$!
echo $BACKEND_PID > "${PROJECT_ROOT}/backend.pid"
echo -e "${GREEN}后端服务已启动，PID: ${BACKEND_PID}${NC}"

# 等待后端启动
echo -e "${YELLOW}等待后端服务启动...${NC}"
sleep 5

# 检查后端是否启动成功
if curl -s http://localhost:8080/api/actuator/health > /dev/null; then
    echo -e "${GREEN}后端服务启动成功！${NC}"
else
    echo -e "${YELLOW}后端服务可能还在启动中，请稍后检查${NC}"
fi

# 启动前端（如果安装了serve）
if command -v serve &> /dev/null; then
    echo -e "${YELLOW}启动前端服务...${NC}"
    cd "${PROJECT_ROOT}/src/frontend"
    if [ -d "build" ]; then
        nohup serve -s build -l 3000 > "${LOG_DIR}/frontend.log" 2>&1 &
        FRONTEND_PID=$!
        echo $FRONTEND_PID > "${PROJECT_ROOT}/frontend.pid"
        echo -e "${GREEN}前端服务已启动，PID: ${FRONTEND_PID}${NC}"
        echo -e "${GREEN}前端访问地址: http://localhost:3000${NC}"
    else
        echo -e "${YELLOW}前端未构建，请先运行 npm run build${NC}"
    fi
else
    echo -e "${YELLOW}未安装serve，跳过前端启动${NC}"
    echo -e "${YELLOW}可以使用 'npm install -g serve' 安装${NC}"
fi

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}服务启动完成！${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "访问地址:"
echo "  后端API: http://localhost:8080/api"
echo "  前端页面: http://localhost:3000"
echo ""
echo "日志文件:"
echo "  后端日志: ${LOG_DIR}/wheat-pest-detection.log"
echo "  启动日志: ${LOG_DIR}/startup.log"
echo ""
echo "管理命令:"
echo "  停止服务: ./scripts/stop.sh"
echo "  查看状态: ./scripts/status.sh"
