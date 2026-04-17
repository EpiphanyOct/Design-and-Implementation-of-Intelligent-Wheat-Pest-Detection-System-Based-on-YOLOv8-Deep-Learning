/**
 * 小麦害虫检测系统 - 害虫识别组件
 * 功能描述: 实现图片上传、害虫种类智能识别、置信度显示、虫害诊断结果展示及建议防治措施
 * 具备查询时间筛选功能，支持历史检测结果分页查看
 * 技术栈: React 18 + Ant Design 5 + Axios
 */
import React, { useState, useEffect, useCallback } from "react";
import {
  Button,
  Upload,
  Modal,
  Table,
  Space,
  message,
  Progress,
  DatePicker,
  Card,
  Typography,
  Collapse,
  Spin,
} from "antd";
import { UploadOutlined, SearchOutlined, InfoCircleOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import axios from "axios";

const { Text, Title, Paragraph } = Typography;
const { Panel } = Collapse;
const { RangePicker } = DatePicker;

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/api";

// 上传前文件过滤，限制类型及大小，支持jpeg/png格式，最大5MB
function beforeUpload(file) {
  const isJpgOrPng = file.type === "image/jpeg" || file.type === "image/png";
  if (!isJpgOrPng) {
    message.error("只支持 JPG/PNG 格式的图片！");
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    message.error("图片大小不能超过 5MB！");
  }
  return isJpgOrPng && isLt5M;
}

// 自定义上传函数，防止Upload自动上传，改为手动
const dummyRequest = ({ onSuccess }) => {
  setTimeout(() => {
    onSuccess("ok");
  }, 0);
};

export default function PestSearch() {
  // 状态定义
  const [fileList, setFileList] = useState([]);
  const [detectResult, setDetectResult] = useState(null); // 当前检测结果
  const [loadingDetect, setLoadingDetect] = useState(false);
  const [historyData, setHistoryData] = useState([]); // 历史检测数据
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 5, total: 0 });
  const [dateRange, setDateRange] = useState([null, null]); // 查询时间筛选
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedHistoryDetail, setSelectedHistoryDetail] = useState(null);

  // 表格列定义
  const columns = [
    {
      title: "检测时间",
      dataIndex: "detectTime",
      key: "detectTime",
      width: 180,
      sorter: true,
      render: (text) => dayjs(text).format("YYYY-MM-DD HH:mm:ss"),
    },
    {
      title: "上传图片",
      dataIndex: "imageUrl",
      key: "imageUrl",
      width: 120,
      render: (url) => (
        <img
          src={url}
          alt="上传图片"
          style={{ width: 100, height: 60, objectFit: "cover", borderRadius: 4, border: "1px solid #f0f0f0" }}
        />
      ),
    },
    {
      title: "识别害虫种类",
      dataIndex: "pestType",
      key: "pestType",
      width: 140,
      render: (text) => <Text strong>{text || "暂无数据"}</Text>,
    },
    {
      title: "置信度评估",
      dataIndex: "confidence",
      key: "confidence",
      width: 140,
      render: (value) =>
        value != null ? (
          <Progress
            percent={Math.round(value * 100)}
            strokeColor={value > 0.8 ? "#52c41a" : value > 0.5 ? "#faad14" : "#f5222d"}
            status={value > 0.5 ? "normal" : "exception"}
            size="small"
          />
        ) : (
          <Text type="secondary">无</Text>
        ),
    },
    {
      title: "虫害诊断结果",
      dataIndex: "diagnosis",
      key: "diagnosis",
      ellipsis: true,
      render: (text) => text || "无",
    },
    {
      title: "建议防治措施",
      dataIndex: "suggestions",
      key: "suggestions",
      ellipsis: true,
      render: (text) => text || "无",
    },
    {
      title: "操作",
      key: "action",
      width: 100,
      fixed: "right",
      render: (_, record) => (
        <Button
          type="link"
          onClick={() => {
            setSelectedHistoryDetail(record);
            setModalVisible(true);
          }}
        >
          详情
        </Button>
      ),
    },
  ];

  // 文件上传列表变化回调
  const handleUploadChange = ({ fileList }) => {
    setFileList(fileList.slice(-1)); // 保留最后一个文件
  };

  const handleDetect = async () => {
    if (fileList.length === 0) {
      message.warning("请先上传图片");
      return;
    }
    const file = fileList[0].originFileObj;
    const formData = new FormData();
    formData.append("image", file);
    try {
      setLoadingDetect(true);
      setDetectResult(null);
      const res = await axios.post(`${API_BASE_URL}/wheat-pest-detection/detect`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 60000,
      });
      if (res.data && res.data.success) {
        setDetectResult(res.data.data);
        message.success("检测完成");
        fetchHistory(1, pagination.pageSize, dateRange); // 自动刷新历史记录
      } else {
        message.error(res.data.message || "检测失败，请重试");
      }
    } catch (err) {
      console.error(err);
      message.error("请求异常，请检查网络或稍后再试");
    } finally {
      setLoadingDetect(false);
    }
  };

  // 查询历史检测数据
  const fetchHistory = useCallback(
    async (currentPage, pageSize, dateRangeFiltered) => {
      try {
        setLoadingHistory(true);
        let params = {
          page: currentPage,
          pageSize,
        };
        if (dateRangeFiltered && dateRangeFiltered[0] && dateRangeFiltered[1]) {
          params.startTime = dayjs(dateRangeFiltered[0]).startOf("day").toISOString();
          params.endTime = dayjs(dateRangeFiltered[1]).endOf("day").toISOString();
        }
        const res = await axios.get(`${API_BASE_URL}/wheat-pest-detection/history`, { params });
        if (res.data && res.data.success) {
          setHistoryData(res.data.data.records);
          setPagination({
            current: res.data.data.current,
            pageSize: res.data.data.pageSize,
            total: res.data.data.total,
          });
        } else {
          message.error(res.data.message || "获取历史记录失败");
          setHistoryData([]);
          setPagination({ current: 1, pageSize: 5, total: 0 });
        }
      } catch (err) {
        console.error(err);
        message.error("请求异常，无法获取历史数据");
        setHistoryData([]);
        setPagination({ current: 1, pageSize: 5, total: 0 });
      } finally {
        setLoadingHistory(false);
      }
    },
    []
  );

  // 处理分页和排序等表格变化
  const handleTableChange = (pagination) => {
    fetchHistory(pagination.current, pagination.pageSize, dateRange);
  };

  // 时间筛选变化触发查询
  const onDateChange = (dates) => {
    setDateRange(dates);
  };

  // 点击查询历史按钮
  const handleSearchHistory = () => {
    fetchHistory(1, pagination.pageSize, dateRange);
  };

  // 关闭详情弹窗
  const handleModalClose = () => {
    setModalVisible(false);
    setSelectedHistoryDetail(null);
  };

  // 识别结果详情组件渲染
  const RenderDetectDetails = ({ data }) => {
    if (!data) return null;
    return (
      <Card
        bordered={true}
        style={{ marginTop: 16 }}
        title="检测结果详情"
        extra={
          <Button type="link" onClick={() => setDetectResult(null)}>
            清除结果
          </Button>
        }
      >
        <Space direction="vertical" style={{ width: "100%" }} size="large">
          <div style={{ textAlign: "center" }}>
            <img
              src={data.imageUrl}
              alt="检测图片"
              style={{ maxWidth: "100%", maxHeight: 300, borderRadius: 6, border: "1px solid #e8e8e8" }}
            />
          </div>
          <div>
            <Title level={5}>识别害虫种类</Title>
            <Text strong style={{ fontSize: 18, color: "#1890ff" }}>
              {data.pestType || "未识别"}
            </Text>
          </div>
          <div>
            <Title level={5}>置信度评估</Title>
            {data.confidence != null ? (
              <Progress
                percent={Math.round(data.confidence * 100)}
                status={data.confidence > 0.5 ? "normal" : "exception"}
                strokeColor={data.confidence > 0.8 ? "#52c41a" : data.confidence > 0.5 ? "#faad14" : "#f5222d"}
                showInfo
              />
            ) : (
              <Text type="secondary">无</Text>
            )}
          </div>
          <div>
            <Title level={5}>虫害诊断结果</Title>
            <Paragraph style={{ whiteSpace: "pre-wrap" }}>{data.diagnosis || "无"}</Paragraph>
          </div>
          <div>
            <Title level={5}>建议防治措施</Title>
            <Paragraph style={{ whiteSpace: "pre-wrap" }}>{data.suggestions || "无"}</Paragraph>
          </div>
        </Space>
      </Card>
    );
  };

  // 历史记录详情弹窗渲染
  const RenderHistoryModalContent = ({ record }) => {
    if (!record) return null;
    return (
      <Space direction="vertical" style={{ width: "100%" }} size="middle">
        <div style={{ textAlign: "center" }}>
          <img
            src={record.imageUrl}
            alt="历史上传图片"
            style={{ maxWidth: "100%", maxHeight: 280, borderRadius: 8, border: "1px solid #d9d9d9" }}
          />
        </div>
        <Collapse accordion ghost>
          <Panel header="害虫种类" key="pestType" forceRender>
            <Text strong style={{ fontSize: 16 }}>
              {record.pestType || "无数据"}
            </Text>
          </Panel>
          <Panel header="置信度" key="confidence" forceRender>
            {record.confidence != null ? (
              <Progress
                percent={Math.round(record.confidence * 100)}
                strokeColor={
                  record.confidence > 0.8 ? "#52c41a" : record.confidence > 0.5 ? "#faad14" : "#f5222d"
                }
                showInfo
              />
            ) : (
              <Text type="secondary">无</Text>
            )}
          </Panel>
          <Panel header="虫害诊断结果" key="diagnosis" forceRender>
            <Paragraph style={{ whiteSpace: "pre-wrap" }}>{record.diagnosis || "无"}</Paragraph>
          </Panel>
          <Panel header="建议防治措施" key="suggestions" forceRender>
            <Paragraph style={{ whiteSpace: "pre-wrap" }}>{record.suggestions || "无"}</Paragraph>
          </Panel>
          <Panel header="检测时间" key="time" forceRender>
            <Text>{dayjs(record.detectTime).format("YYYY年MM月DD日 HH:mm:ss")}</Text>
          </Panel>
        </Collapse>
      </Space>
    );
  };

  // 页面加载时获取历史数据
  useEffect(() => {
    fetchHistory(1, pagination.pageSize, dateRange);
  }, [fetchHistory]);

  // 页面布局及UI渲染
  return (
    <div style={{ maxWidth: 1200, margin: "20px auto 60px auto", padding: 16 }}>
      <Title level={2} style={{ textAlign: "center", marginBottom: 24 }}>
        小麦害虫检测系统
      </Title>

      {/* 上传与检测部分 */}
      <Card
        bordered={true}
        title={
          <Space>
            <InfoCircleOutlined style={{ color: "#1890ff" }} />
            智能分析上传图片，识别小麦害虫种类
          </Space>
        }
        style={{ marginBottom: 32 }}
      >
        <Space direction="vertical" size="large" style={{ width: "100%" }}>
          <Upload
            accept="image/jpeg,image/png"
            listType="picture-card"
            maxCount={1}
            fileList={fileList}
            onChange={handleUploadChange}
            beforeUpload={beforeUpload}
            customRequest={dummyRequest}
            showUploadList={{
              showPreviewIcon: false,
              showRemoveIcon: true,
            }}
          >
            {fileList.length >= 1 ? null : (
              <div>
                <UploadOutlined />
                <div style={{ marginTop: 8 }}>上传图片</div>
              </div>
            )}
          </Upload>
          <Button
            type="primary"
            size="large"
            onClick={handleDetect}
            disabled={fileList.length === 0}
            loading={loadingDetect}
            style={{ minWidth: 140 }}
          >
            开始检测
          </Button>
          {loadingDetect && <Text type="secondary">检测中，请稍候...</Text>}
          <RenderDetectDetails data={detectResult} />
        </Space>
      </Card>

      {/* 查询历史检测记录部分 */}
      <Card
        bordered={true}
        title={
          <Space>
            <SearchOutlined style={{ color: "#1890ff" }} />
            历史检测记录查询
          </Space>
        }
      >
        <Space align="center" style={{ marginBottom: 16, flexWrap: "wrap", gap: 12 }}>
          <RangePicker
            value={dateRange}
            onChange={onDateChange}
            allowEmpty={[true, true]}
            style={{ minWidth: 280 }}
            placeholder={["开始时间", "结束时间"]}
            format="YYYY-MM-DD"
            disabledDate={(current) => current && current > dayjs().endOf("day")}
          />
          <Button
            type="primary"
            onClick={handleSearchHistory}
            loading={loadingHistory}
            disabled={loadingHistory}
          >
            查询
          </Button>
          <Button
            onClick={() => {
              setDateRange([null, null]);
              fetchHistory(1, pagination.pageSize, [null, null]);
            }}
            disabled={loadingHistory}
          >
            重置
          </Button>
        </Space>
        <Table
          columns={columns}
          dataSource={historyData}
          rowKey={(record) => record.id || record.detectTime + Math.random()}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            pageSizeOptions: ["5", "10", "20", "50"],
            showTotal: (total) => `共 ${total} 条`,
          }}
          loading={loadingHistory}
          scroll={{ x: 1100 }}
          onChange={handleTableChange}
          bordered
          size="middle"
        />
      </Card>

      {/* 历史详情弹窗 */}
      <Modal
        title="检测详情"
        open={modalVisible}
        onCancel={handleModalClose}
        footer={[
          <Button key="close" onClick={handleModalClose}>
            关闭
          </Button>,
        ]}
        width={640}
        centered
      >
        {selectedHistoryDetail ? (
          <RenderHistoryModalContent record={selectedHistoryDetail} />
        ) : (
          <Spin tip="加载中..." />
        )}
      </Modal>

      {/* 底部版权信息 */}
      <div
        style={{
          textAlign: "center",
          marginTop: 40,
          padding: "20px 0",
          color: "#999",
          borderTop: "1px solid #eee",
        }}
      >
        © 2024 小麦害虫检测系统 - 农业行业解决方案
      </div>
    </div>
  );
}
