/**
 * 首页组件
 * 展示系统整体运行状态与关键数据概览
 */
import React, { useState, useEffect } from "react";
import { Card, Row, Col, Statistic, Table, DatePicker, Space, Typography } from "antd";
import {
  BugOutlined,
  AlertOutlined,
  FileTextOutlined,
  DashboardOutlined,
} from "@ant-design/icons";
import { Pie, Line } from "@ant-design/charts";
import dayjs from "dayjs";

const { Title } = Typography;
const { RangePicker } = DatePicker;

// 模拟数据
const mockStats = {
  totalPests: 1256,
  aphidCount: 423,
  sawflyCount: 189,
  otherPests: 644,
};

const mockPieData = [
  { type: "蚜虫", value: 423 },
  { type: "吸浆虫", value: 189 },
  { type: "红蜘蛛", value: 312 },
  { type: "螟虫", value: 198 },
  { type: "其他", value: 134 },
];

const mockLineData = [
  { date: "2024-01-01", value: 120 },
  { date: "2024-01-02", value: 132 },
  { date: "2024-01-03", value: 101 },
  { date: "2024-01-04", value: 134 },
  { date: "2024-01-05", value: 90 },
  { date: "2024-01-06", value: 230 },
  { date: "2024-01-07", value: 210 },
];

const mockTableData = [
  { id: 1, pestType: "蚜虫", count: 45, detectTime: "2024-01-07 10:30:00" },
  { id: 2, pestType: "红蜘蛛", count: 32, detectTime: "2024-01-07 09:15:00" },
  { id: 3, pestType: "吸浆虫", count: 18, detectTime: "2024-01-06 16:45:00" },
  { id: 4, pestType: "螟虫", count: 27, detectTime: "2024-01-06 14:20:00" },
  { id: 5, pestType: "其他", count: 12, detectTime: "2024-01-05 11:00:00" },
];

const columns = [
  { title: "害虫类型", dataIndex: "pestType", key: "pestType" },
  { title: "数量", dataIndex: "count", key: "count" },
  { title: "检测时间", dataIndex: "detectTime", key: "detectTime" },
];

const pieConfig = {
  data: mockPieData,
  angleField: "value",
  colorField: "type",
  radius: 0.8,
  label: {
    type: "outer",
    content: "{name} {percentage}",
  },
  interactions: [{ type: "element-active" }],
};

const lineConfig = {
  data: mockLineData,
  xField: "date",
  yField: "value",
  point: {
    size: 5,
    shape: "diamond",
  },
  label: {
    style: {
      fill: "#aaa",
    },
  },
};

export default function Home() {
  const [dateRange, setDateRange] = useState([dayjs().subtract(7, "day"), dayjs()]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // 这里可以调用API获取真实数据
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 500);
  }, [dateRange]);

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>
        <DashboardOutlined /> 系统首页
      </Title>

      {/* 数据筛选 */}
      <Card style={{ marginBottom: 24 }}>
        <Space>
          <span>时间范围：</span>
          <RangePicker
            value={dateRange}
            onChange={setDateRange}
            allowClear={false}
          />
        </Space>
      </Card>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="害虫总数"
              value={mockStats.totalPests}
              prefix={<BugOutlined />}
              valueStyle={{ color: "#cf1322" }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="蚜虫数量"
              value={mockStats.aphidCount}
              valueStyle={{ color: "#1890ff" }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="吸浆虫数量"
              value={mockStats.sawflyCount}
              valueStyle={{ color: "#52c41a" }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={loading}>
            <Statistic
              title="其他害虫"
              value={mockStats.otherPests}
              valueStyle={{ color: "#faad14" }}
            />
          </Card>
        </Col>
      </Row>

      {/* 图表区域 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card title="害虫类型分布" loading={loading}>
            <Pie {...pieConfig} />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="害虫数量趋势" loading={loading}>
            <Line {...lineConfig} />
          </Card>
        </Col>
      </Row>

      {/* 数据表格 */}
      <Card title="近期检测记录" loading={loading}>
        <Table
          dataSource={mockTableData}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 5 }}
        />
      </Card>
    </div>
  );
}
