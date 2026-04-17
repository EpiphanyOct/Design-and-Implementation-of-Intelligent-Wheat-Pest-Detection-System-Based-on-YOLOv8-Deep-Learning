/**
 * 小麦害虫检测系统 - 用户身份验证入口
 * 功能描述: 确保系统安全访问，包含账号输入框验证，密码输入与确认，登录按钮操作，
 * 用户权限管理，身份验证流程，密码错误提示等功能。
 * 技术栈: React 18 + Ant Design 5 + Axios + React Router Dom
 */
import React, { useState, useEffect, useRef } from "react";
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
  Modal,
  Divider,
  Tooltip,
  theme,
} from "antd";
import {
  UserOutlined,
  LockOutlined,
  EyeInvisibleOutlined,
  EyeTwoTone,
  SafetyCertificateOutlined,
  LoginOutlined,
  InfoCircleOutlined,
  KeyOutlined,
} from "@ant-design/icons";
import axios from "axios";

const { Title, Text } = Typography;

/** 用户权限类型定义 */
const PERMISSIONS = {
  ADMIN: "管理员",
  INSPECTOR: "检测员",
  VIEWER: "访客",
};

/** 密码强度验证 */
const passwordStrengthRules = [
  { regex: /.{8,}/, message: "密码长度至少8位" },
  { regex: /[A-Z]/, message: "至少包含1个大写字母" },
  { regex: /[a-z]/, message: "至少包含1个小写字母" },
  { regex: /[0-9]/, message: "至少包含1个数字" },
  { regex: /[\W_]/, message: "至少包含1个特殊字符" },
];

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/api";

/**
 * 登录API调用
 * @param {string} username 用户名
 * @param {string} password 密码
 * @returns {Promise<Object>} {success:boolean,token:string,userInfo:object,error:string}
 */
async function loginAPI(username, password) {
  try {
    const resp = await axios.post(`${API_BASE_URL}/auth/login`, {
      username,
      password,
    });
    return resp.data;
  } catch (error) {
    if (error.response) {
      return { success: false, error: error.response.data.message || "登录失败" };
    } else {
      return { success: false, error: "网络或服务器异常" };
    }
  }
}

/**
 * 账号格式检验
 * @param {string} username
 * @returns {boolean}
 */
function validateUsername(username) {
  // 用户名由4-20位字母数字下划线组成，且不能全数字
  const reg = /^(?!\d+$)[\w]{4,20}$/;
  return reg.test(username);
}

/**
 * 验证密码强度
 * @param {string} password
 * @returns {Array} 不符合规则的提示信息数组，无则表示密码强度合格
 */
function validatePasswordStrength(password) {
  return passwordStrengthRules
    .filter((rule) => !rule.regex.test(password))
    .map((rule) => rule.message);
}

/** 主页面组件 */
export default function LoginForm({ onLoginSuccess }) {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [permission, setPermission] = useState(null);
  const [rememberMe, setRememberMe] = useState(false);
  const [passwordConfirmVisible, setPasswordConfirmVisible] = useState(false);
  const [passwordConfirmError, setPasswordConfirmError] = useState("");
  const [userInfo, setUserInfo] = useState(null);
  const [passwordStrengthTips, setPasswordStrengthTips] = useState([]);
  const [modalVisible, setModalVisible] = useState(false);
  const passwordRef = useRef("");

  // 页面主题色，基于antd theme
  const {
    token: { colorPrimary, colorError, colorTextBase, colorBgContainer },
  } = theme.useToken();

  /** 处理登录按钮点击 */
  const handleLogin = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      setLoginError("");
      const result = await loginAPI(values.username, values.password);
      if (result.success) {
        // 登录成功，保存用户信息
        setUserInfo(result.userInfo || { username: values.username, permission: PERMISSIONS.VIEWER });
        setPermission(result.userInfo?.permission || PERMISSIONS.VIEWER);
        message.success(`登录成功，欢迎您，${values.username}！`);
        if (rememberMe) {
          localStorage.setItem("wheatPestUser", JSON.stringify({ username: values.username, token: result.token }));
        } else {
          sessionStorage.setItem("wheatPestUser", JSON.stringify({ username: values.username, token: result.token }));
        }
        setModalVisible(true);
        if (onLoginSuccess) {
          onLoginSuccess(result.userInfo);
        }
      } else {
        setLoginError(result.error || "登录失败，请重试");
      }
    } catch (errorInfo) {
      // 表单校验失败
      setLoginError("请检查输入内容是否正确");
    } finally {
      setLoading(false);
    }
  };

  /** 密码输入事件，动态校验密码强度 */
  const onPasswordChange = (e) => {
    const pwd = e.target.value;
    passwordRef.current = pwd;
    const tips = validatePasswordStrength(pwd);
    setPasswordStrengthTips(tips);
    // 如果确认密码已输入，与密码同步校验
    if (passwordConfirmVisible) {
      const confirmValue = form.getFieldValue("confirmPassword");
      if (confirmValue && confirmValue !== pwd) {
        setPasswordConfirmError("两次输入的密码不一致");
      } else {
        setPasswordConfirmError("");
      }
    }
  };

  /** 确认密码输入事件 */
  const onConfirmPasswordChange = (e) => {
    const confirmVal = e.target.value;
    if (confirmVal !== passwordRef.current) {
      setPasswordConfirmError("两次输入的密码不一致");
    } else {
      setPasswordConfirmError("");
    }
  };

  /** 记住我切换事件 */
  const onRememberMeChange = (e) => {
    setRememberMe(e.target.checked);
  };

  /** 退出登录 */
  const handleLogout = () => {
    setUserInfo(null);
    setPermission(null);
    localStorage.removeItem("wheatPestUser");
    sessionStorage.removeItem("wheatPestUser");
    form.resetFields();
    setPasswordStrengthTips([]);
    setLoginError("");
    setPasswordConfirmError("");
    setModalVisible(false);
    message.info("您已退出登录");
  };

  /** 页面加载时，尝试自动登录 */
  useEffect(() => {
    const saved = localStorage.getItem("wheatPestUser") || sessionStorage.getItem("wheatPestUser");
    if (saved) {
      try {
        const user = JSON.parse(saved);
        if (user?.username && user?.token) {
          setUserInfo({ username: user.username });
          setPermission(PERMISSIONS.VIEWER);
          message.success(`欢迎回来，${user.username}`);
          if (onLoginSuccess) {
            onLoginSuccess(user);
          }
        }
      } catch {
        // 忽略解析错误
      }
    }
  }, [onLoginSuccess]);

  /** 密码框提示组件 */
  const PasswordStrengthTips = () =>
    passwordStrengthTips.length > 0 ? (
      <Alert
        type="warning"
        showIcon
        style={{ marginTop: 8 }}
        message={
          <>
            <Text strong>密码建议：</Text>
            {passwordStrengthTips.map((tip, i) => (
              <div key={i}>- {tip}</div>
            ))}
          </>
        }
      />
    ) : (
      <Alert
        type="success"
        showIcon
        style={{ marginTop: 8 }}
        message="密码强度合格"
      />
    );

  /** 登录表单组件 */
  const LoginFormComponent = () => (
    <Form
      form={form}
      name="login_form"
      layout="vertical"
      autoComplete="off"
      initialValues={{ remember: true }}
      onFinish={handleLogin}
      onFieldsChange={() => {
        setLoginError("");
      }}
      style={{ maxWidth: 420, margin: "auto" }}
    >
      {/* 用户名输入框 */}
      <Form.Item
        name="username"
        label={
          <Space>
            <UserOutlined />
            <Text strong>账号</Text>
            <Tooltip title="请输入4-20位字母、数字或下划线，且不能全数字">
              <InfoCircleOutlined />
            </Tooltip>
          </Space>
        }
        rules={[
          { required: true, message: "请输入账号" },
          {
            validator: (_, value) => {
              if (!value) return Promise.resolve();
              if (!validateUsername(value)) {
                return Promise.reject("账号格式不正确");
              }
              return Promise.resolve();
            },
          },
        ]}
        validateTrigger={["onBlur", "onChange"]}
        hasFeedback
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
            <Tooltip title="密码最少8位，包含大小写字母、数字、特殊字符">
              <InfoCircleOutlined />
            </Tooltip>
          </Space>
        }
        rules={[
          { required: true, message: "请输入密码" },
          () => ({
            validator(_, value) {
              if (!value) return Promise.resolve();
              const tips = validatePasswordStrength(value);
              if (tips.length > 0) {
                return Promise.reject(tips[0]);
              }
              return Promise.resolve();
            },
          }),
        ]}
        validateTrigger={["onBlur", "onChange"]}
        hasFeedback
      >
        <Input.Password
          placeholder="请输入密码"
          iconRender={(visible) =>
            visible ? <EyeTwoTone /> : <EyeInvisibleOutlined />
          }
          onChange={onPasswordChange}
          visibilityToggle={{ visible: passwordVisible, onVisibleChange: setPasswordVisible }}
          maxLength={32}
        />
      </Form.Item>

      {/* 密码强度提示 */}
      {form.getFieldValue("password") && <PasswordStrengthTips />}

      {/* 确认密码输入框，默认隐藏，登录页面不展示，但演示多输入验证 */}
      {passwordConfirmVisible && (
        <Form.Item
          name="confirmPassword"
          label={
            <Space>
              <KeyOutlined />
              <Text strong>确认密码</Text>
            </Space>
          }
          validateStatus={passwordConfirmError ? "error" : ""}
          help={passwordConfirmError || ""}
          rules={[{ required: true, message: "请再次输入密码确认" }]}
          hasFeedback
        >
          <Input.Password
            placeholder="请再次输入密码"
            onChange={onConfirmPasswordChange}
            maxLength={32}
          />
        </Form.Item>
      )}

      {/* 记住我复选框 */}
      <Form.Item name="remember" valuePropName="checked" initialValue={true}>
        <Checkbox checked={rememberMe} onChange={onRememberMeChange}>
          记住我
        </Checkbox>
      </Form.Item>

      {/* 登录错误提示 */}
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
          disabled={loading || passwordConfirmError}
        >
          登 录
        </Button>
      </Form.Item>
    </Form>
  );

  /** 登录后用户信息展示及操作 */
  const UserInfoCard = () => {
    const username = userInfo?.username || "未登录";
    return (
      <Card
        title={
          <Space>
            <SafetyCertificateOutlined style={{ color: colorPrimary }} />
            用户信息
          </Space>
        }
        bordered
        style={{ maxWidth: 500, margin: "auto" }}
      >
        <Space direction="vertical" style={{ width: "100%" }}>
          <Text>
            <strong>账号：</strong>
            {username}
          </Text>
          <Text>
            <strong>权限：</strong>
            {permission || "未知"}
          </Text>
          <Divider />
          <Button danger block onClick={handleLogout}>
            退出登录
          </Button>
        </Space>
      </Card>
    );
  };

  /** 登录成功后显示的欢迎提示弹窗 */
  const WelcomeModal = () => (
    <Modal
      title={`欢迎您，${userInfo?.username || ""}`}
      open={modalVisible}
      onOk={() => setModalVisible(false)}
      onCancel={() => setModalVisible(false)}
      okText="确定"
      cancelButtonProps={{ style: { display: "none" } }}
      centered
    >
      <Typography.Paragraph>
        您已成功登录小麦害虫检测系统，可以开始使用系统的各项功能。请根据您的权限访问相应的模块。
      </Typography.Paragraph>
      <Typography.Paragraph>
        <strong>当前权限：</strong> {permission}
      </Typography.Paragraph>
      <Typography.Paragraph>
        如果您忘记密码，请联系管理员重置。
      </Typography.Paragraph>
    </Modal>
  );

  return (
    <div
      style={{
        minHeight: "100vh",
        background:
          "linear-gradient(135deg, #8DC26F 0%, #6BAA6F 50%, #4E9148 100%)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        padding: "20px",
      }}
    >
      <Card
        title={
          <Title level={3} style={{ margin: 0, color: "#fff", userSelect: "none" }}>
            小麦害虫检测系统 - 用户身份验证入口
          </Title>
        }
        bordered={false}
        style={{
          width: 480,
          boxShadow:
            "0 4px 8px 0 rgba(0,0,0,0.2), 0 6px 20px 0 rgba(0,0,0,0.19)",
          backgroundColor: "rgba(255,255,255,0.95)",
          borderRadius: 12,
        }}
        headStyle={{
          background: "linear-gradient(135deg, #4E9148 0%, #6BAA6F 100%)",
          borderRadius: "12px 12px 0 0",
        }}
      >
        {!userInfo ? <LoginFormComponent /> : <UserInfoCard />}
      </Card>
      <WelcomeModal />
      {/* 辅助信息底部 */}
      <div
        style={{
          position: "fixed",
          bottom: 12,
          width: "100%",
          textAlign: "center",
          color: "#f0f0f0",
          userSelect: "none",
          fontSize: 12,
          zIndex: 1000,
        }}
      >
        © 2024 小麦害虫检测系统 &nbsp;&nbsp;|&nbsp;&nbsp; 专注于农业行业安全管理
      </div>
    </div>
  );
}
