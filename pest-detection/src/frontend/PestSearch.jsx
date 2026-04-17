/**
 * 害虫识别组件
 * Feature: Pest Detection
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
import {
  UploadOutlined,
  SearchOutlined,
  InfoCircleOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import pestDetectionService from "./pestDetectionService";

const { Text, Title, Paragraph } = Typography;
const { Panel } = Collapse;
const { RangePicker } = DatePicker;

/**
 * 害虫识别组件
 * Feature: Pest Detection
 */
export default function PestSearch() {
  const [fileList, setFileList] = useState([]);
  const [detectResult, setDetectResult] = useState(null);
  const [loadingDetect, setLoadingDetect] = useState(false);
  const [historyData, setHistoryData] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 5,
    total: 0,
  });
  const [dateRange, setDateRange] = useState([null, null]);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedHistoryDetail, setSelectedHistoryDetail] = useState(null);

  // 表格列定义
  const columns = [
    {
      title: "检测时间",
      dataIndex: "detectTime",
      key: "detectTime",
      width: 180,
      render: (text) => dayjs(text).format("YYYY-MM-DD HH:mm:ss"),
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
            strokeColor={
              value > 0.8 ? "#52c41a" : value > 0.5 ? "#faad14" : "#f5222d"
            }
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
    },
    {
      title: "建议防治措施",
      dataIndex: "suggestions",
      key: "suggestions",
      ellipsis: true,
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

  // 文件上传前校验
  const beforeUpload = (file) => {
    const isJpgOrPng =
      file.type === "image/jpeg" || file.type === "image/png";
    if (!isJpgOrPng) {
      message.error("只支持 JPG/PNG 格式的图片！");
    }
    const isLt5M = file.size / 1024 / 1024 < 5;
    if (!isLt5M) {
      message.error("图片大小不能超过 5MB！");
    }
    return isJpgOrPng && isLt5M;
  };

  // 上传列表变化
  const handleUploadChange = ({ fileList }) => {
    setFileList(fileList.slice(-1));
  };

  // 开始检测
  const handleDetect = async () => {
    if (fileList.length === 0) {
      message.warning("请先上传图片");
      return;
    }

    const file = fileList[0].originFileObj;

    try {
      setLoadingDetect(true);
      setDetectResult(null);

      const result = await pestDetectionService.detect(file);

      if (result.success) {
        setDetectResult(result);
        message.success("检测完成");
        fetchHistory(1, pagination.pageSize, dateRange);
      } else {
        message.error(result.message || "检测失败");
      }
    } catch (err) {
      console.error(err);
      message.error("请求异常，请检查网络或稍后再试");
    } finally {
      setLoadingDetect(false);
    }
  };

  // 查询历史记录
  const fetchHistory = useCallback(
    async (currentPage, pageSize, dateRangeFiltered) => {
      try {
        setLoadingHistory(true);

        const params = {
          page: currentPage,
          pageSize,
        };

        if (dateRangeFiltered && dateRangeFiltered[0] && dateRangeFiltered[1]) {
          params.startTime = dayjs(dateRangeFiltered[0]).format(
            "YYYY-MM-DD HH:mm:ss"
          );
          params.endTime = dayjs(dateRangeFiltered[1]).format(
            "YYYY-MM-DD HH:mm:ss"
          );
        }

        const result = await pestDetectionService.getHistory(params);

        if (result.success) {
          setHistoryData(result.data.records);
          setPagination({
            current: result.data.current,
            pageSize: result.data.pageSize,
            total: result.data.total,
          });
        } else {
          message.error(result.message || "获取历史记录失败");
        }
      } catch (err) {
        console.error(err);
        message.error("请求异常，无法获取历史数据");
      } finally {
        setLoadingHistory(false);
      }
    },
    []
  );

  // 表格分页变化
  const handleTableChange = (pagination) => {
    fetchHistory(pagination.current, pagination.pageSize, dateRange);
  };

  // 时间范围变化
  const onDateChange = (dates) => {
    setDateRange(dates);
  };

  // 查询历史
  const handleSearchHistory = () => {
    fetchHistory(1, pagination.pageSize, dateRange);
  };

  // 关闭详情弹窗
  const handleModalClose = () => {
    setModalVisible(false);
    setSelectedHistoryDetail(null);
  };

  // 检测结果详情
  const RenderDetectDetails = ({ data }) => {
    if (!data) return null;
    return (
      <Card
        bordered
        style={{ marginTop: 16 }}
        title="检测结果详情"
        extra={
          <Button type="link" onClick={() => setDetectResult(null)}>
            清除结果
          </Button>
        }
      >
        <Space direction="vertical" style={{ width: "100%" }} size="large">
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
                strokeColor={
                  data.confidence > 0.8
                    ? "#52c41a"
                    : data.confidence > 0.5
                    ? "#faad14"
                    : "#f5222d"
                }
              />
            ) : (
              <Text type="secondary">无</Text>
            )}
          </div>
          <div>
            <Title level={5}>虫害诊断结果</Title>
            <Paragraph>{data.diagnosis || "无"}</Paragraph>
          </div>
          <div>
            <Title level={5}>建议防治措施</Title>
            <Paragraph style={{ whiteSpace: "pre-wrap" }}>
              {data.suggestions || "无"}
            </Paragraph>
          </div>
        </Space>
      </Card>
    );
  };

  // 历史详情弹窗内容
  const RenderHistoryModalContent = ({ record }) => {
    if (!record) return null;
    return (
      <Space direction="vertical" style={{ width: "100%" }} size="middle">
        <Collapse accordion ghost>
          <Panel header="害虫种类" key="pestType">
            <Text strong style={{ fontSize: 16 }}>
              {record.pestType || "无数据"}
            </Text>
          </Panel>
          <Panel header="置信度" key="confidence">
            {record.confidence != null ? (
              <Progress
                percent={Math.round(record.confidence * 100)}
                strokeColor={
                  record.confidence > 0.8
                    ? "#52c41a"
                    : record.confidence > 0.5
                    ? "#faad14"
                    : "#f5222d"
                }
              />
            ) : (
              <Text type="secondary">无</Text>
            )}
          </Panel>
          <Panel header="虫害诊断结果" key="diagnosis">
            <Paragraph>{record.diagnosis || "无"}</Paragraph>
          </Panel>
          <Panel header="建议防治措施" key="suggestions">
            <Paragraph style={{ whiteSpace: "pre-wrap" }}>
              {record.suggestions || "无"}
            </Paragraph>
          </Panel>
          <Panel header="检测时间" key="time">
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

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>虫害识别</Title>

      {/* 上传与检测部分 */}
      <Card
        bordered
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
            customRequest={({ onSuccess }) => setTimeout(() => onSuccess("ok"), 0)}
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
          >
            开始检测
          </Button>

          {loadingDetect && <Text type="secondary">检测中，请稍候...</Text>}

          <RenderDetectDetails data={detectResult} />
        </Space>
      </Card>

      {/* 历史记录查询 */}
      <Card
        bordered
        title={
          <Space>
            <SearchOutlined style={{ color: "#1890ff" }} />
            历史检测记录查询
          </Space>
        }
      >
        <Space align="center" style={{ marginBottom: 16 }}>
          <RangePicker
            value={dateRange}
            onChange={onDateChange}
            format="YYYY-MM-DD"
          />
          <Button
            type="primary"
            onClick={handleSearchHistory}
            loading={loadingHistory}
          >
            查询
          </Button>
          <Button
            onClick={() => {
              setDateRange([null, null]);
              fetchHistory(1, pagination.pageSize, [null, null]);
            }}
          >
            重置
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={historyData}
          rowKey={(record) => record.id || record.detectTime}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            pageSizeOptions: ["5", "10", "20"],
            showTotal: (total) => `共 ${total} 条`,
          }}
          loading={loadingHistory}
          onChange={handleTableChange}
          bordered
          size="middle"
        />
      </Card>

      {/* 详情弹窗 */}
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
      >
        {selectedHistoryDetail ? (
          <RenderHistoryModalContent record={selectedHistoryDetail} />
        ) : (
          <Spin tip="加载中..." />
        )}
      </Modal>
    </div>
  );
}
