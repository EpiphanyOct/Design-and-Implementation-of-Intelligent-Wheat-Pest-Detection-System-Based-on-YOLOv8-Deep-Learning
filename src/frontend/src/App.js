/**
 * 小麦害虫检测系统 - 主应用组件
 */
import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { Layout, Menu, theme } from "antd";
import {
  HomeOutlined,
  SearchOutlined,
  DatabaseOutlined,
  MonitorOutlined,
  SettingOutlined,
  FileTextOutlined,
  ControlOutlined,
  UserOutlined,
} from "@ant-design/icons";
import LoginForm from "./components/LoginForm";
import Home from "./pages/Home";
import PestSearch from "./components/PestSearch";
import "./App.css";

const { Header, Sider, Content } = Layout;

// 主布局组件
const MainLayout = ({ user, onLogout }) => {
  const [collapsed, setCollapsed] = useState(false);
  const {
    token: { colorBgContainer },
  } = theme.useToken();

  const menuItems = [
    { key: "/", icon: <HomeOutlined />, label: "首页" },
    { key: "/pest-search", icon: <SearchOutlined />, label: "虫害识别" },
    { key: "/pest-library", icon: <DatabaseOutlined />, label: "虫害信息库" },
    { key: "/monitoring", icon: <MonitorOutlined />, label: "实时监测" },
    { key: "/data-management", icon: <DatabaseOutlined />, label: "数据管理" },
    { key: "/alert-settings", icon: <SettingOutlined />, label: "预警设置" },
    { key: "/reports", icon: <FileTextOutlined />, label: "报告生成" },
    { key: "/device-control", icon: <ControlOutlined />, label: "设备控制" },
  ];

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div
          style={{
            height: 64,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: "#fff",
            fontSize: collapsed ? 14 : 18,
            fontWeight: "bold",
          }}
        >
          {collapsed ? "害虫" : "小麦害虫检测"}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={["/"]}
          items={menuItems}
          onClick={({ key }) => {
            window.location.href = key;
          }}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: "0 24px",
            background: colorBgContainer,
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <span style={{ fontSize: 18, fontWeight: "bold" }}>
            小麦害虫检测系统 V1.0
          </span>
          <span>
            <UserOutlined /> {user?.username || "用户"}
          </span>
        </Header>
        <Content
          style={{
            margin: "24px 16px",
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
          }}
        >
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/pest-search" element={<PestSearch />} />
            <Route path="*" element={<div>功能开发中...</div>} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
};

function App() {
  const [user, setUser] = useState(null);

  const handleLoginSuccess = (userInfo) => {
    setUser(userInfo);
  };

  const handleLogout = () => {
    setUser(null);
  };

  return (
    <Router>
      <Routes>
        <Route
          path="/login"
          element={<LoginForm onLoginSuccess={handleLoginSuccess} />}
        />
        <Route
          path="/*"
          element={
            user ? (
              <MainLayout user={user} onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" replace />
            )
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
