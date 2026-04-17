/**
 * 登录表单组件
 * Feature: User Authentication
 */
import React, { useState, useEffect } from "react";
import {
  Form,
  Input,
  Button,
  Checkbox,
  Typography,
  message,
  Card,
  Space,
  Alert,
  theme,
} from "antd";
import {
  UserOutlined,
  LockOutlined,
  EyeInvisibleOutlined,
  EyeTwoTone,
  LoginOutlined,
} from "@ant-design/icons";
import authService from "./authService";

const { Title, Text } = Typography;

/**
 * 登录表单组件
 * Feature: User Authentication
 */
export default function LoginForm({ onLoginSuccess }) {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);

  const {
    token: { colorPrimary },
  } = theme.useToken();

  // 页面加载时检查是否有保存的账号
  useEffect(() => {
    const savedUser = localStorage.getItem("wheatPestRememberUser");
    if (savedUser) {
      try {
        const { username } = JSON.parse(savedUser);
        form.setFieldsValue({ username, remember: true });
        setRememberMe(true);
      } catch {
        // 忽略解析错误
      }
    }
  }, [form]);

  /**
   * 处理登录
   */
  const handleLogin = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      setLoginError("");

      const result = await authService.login(values.username, values.password);

      if (result.success) {
        // 保存Token
        authService.setToken(result.token);

        // 记住账号
        if (rememberMe) {
          localStorage.setItem(
            "wheatPestRememberUser",
            JSON.stringify({ username: values.username })
          );
        } else {
          localStorage.removeItem("wheatPestRememberUser");
        }

        message.success(`登录成功，欢迎您 ${result.userInfo.username}！`);

        if (onLoginSuccess) {
          onLoginSuccess(result.userInfo);
        }
      } else {
        setLoginError(result.error || "登录失败");
      }
    } catch (error) {
      setLoginError("登录异常，请稍后重试");
      console.error("Login error:", error);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 密码强度校验
   */
  const validatePassword = (_, value) => {
    if (!value) {
      return Promise.resolve();
    }
    if (value.length < 8) {
      return Promise.reject("密码长度至少8位");
    }
    if (!/[A-Z]/.test(value)) {
      return Promise.reject("密码必须包含大写字母");
    }
    if (!/[a-z]/.test(value)) {
      return Promise.reject("密码必须包含小写字母");
    }
    if (!/[0-9]/.test(value)) {
      return Promise.reject("密码必须包含数字");
    }
    return Promise.resolve();
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #8DC26F 0%, #6BAA6F 50%, #4E9148 100%)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        padding: "20px",
      }}
    >
      <Card
        title={
          <Title level={3} style={{ margin: 0, color: "#fff" }}>
            小麦害虫检测系统
          </Title>
        }
        bordered={false}
        style={{
          width: 420,
          boxShadow: "0 4px 8px 0 rgba(0,0,0,0.2), 0 6px 20px 0 rgba(0,0,0,0.19)",
          backgroundColor: "rgba(255,255,255,0.95)",
          borderRadius: 12,
        }}
        headStyle={{
          background: "linear-gradient(135deg, #4E9148 0%, #6BAA6F 100%)",
          borderRadius: "12px 12px 0 0",
          textAlign: "center",
        }}
      >
        <Form
          form={form}
          name="login_form"
          layout="vertical"
          autoComplete="off"
          onFinish={handleLogin}
          onFieldsChange={() => setLoginError("")}
        >
          {/* 用户名输入框 */}
          <Form.Item
            name="username"
            label={
              <Space>
                <UserOutlined />
                <Text strong>账号</Text>
              </Space>
            }
            rules={[
              { required: true, message: "请输入账号" },
              { min: 4, message: "账号长度至少4位" },
              { max: 20, message: "账号长度最多20位" },
            ]}
          >
            <Input placeholder="请输入账号" maxLength={20} allowClear />
          </Form.Item>

          {/* 密码输入框 */}
          <Form.Item
            name="password"
            label={
              <Space>
                <LockOutlined />
                <Text strong>密码</Text>
              </Space>
            }
            rules={[
              { required: true, message: "请输入密码" },
              { validator: validatePassword },
            ]}
          >
            <Input.Password
              placeholder="请输入密码"
              iconRender={(visible) =>
                visible ? <EyeTwoTone /> : <EyeInvisibleOutlined />
              }
              visibilityToggle={{
                visible: passwordVisible,
                onVisibleChange: setPasswordVisible,
              }}
              maxLength={32}
            />
          </Form.Item>

          {/* 记住我 */}
          <Form.Item name="remember" valuePropName="checked">
            <Checkbox
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
            >
              记住账号
            </Checkbox>
          </Form.Item>

          {/* 错误提示 */}
          {loginError && (
            <Alert
              type="error"
              showIcon
              message={loginError}
              style={{ marginBottom: 16 }}
            />
          )}

          {/* 登录按钮 */}
          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              icon={<LoginOutlined />}
              loading={loading}
              size="large"
            >
              登 录
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: "center", marginTop: 16 }}>
          <Text type="secondary">
            默认账号: admin / admin123
          </Text>
        </div>
      </Card>

      {/* 底部版权 */}
      <div
        style={{
          position: "fixed",
          bottom: 12,
          width: "100%",
          textAlign: "center",
          color: "#f0f0f0",
          fontSize: 12,
        }}
      >
        © 2024 小麦害虫检测系统
      </div>
    </div>
  );
}
