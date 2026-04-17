/**
 * 登录表单组件测试
 * Feature: User Authentication
 */
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import LoginForm from "../src/frontend/LoginForm";
import authService from "../src/frontend/authService";

// Mock authService
jest.mock("../src/frontend/authService");

describe("LoginForm Component", () => {
  const mockOnLoginSuccess = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  test("renders login form correctly", () => {
    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    expect(screen.getByText("小麦害虫检测系统")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("请输入账号")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("请输入密码")).toBeInTheDocument();
    expect(screen.getByText("登 录")).toBeInTheDocument();
    expect(screen.getByText("记住账号")).toBeInTheDocument();
  });

  test("shows validation error for empty username", async () => {
    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    const loginButton = screen.getByText("登 录");
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText("请输入账号")).toBeInTheDocument();
    });
  });

  test("shows validation error for empty password", async () => {
    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    const usernameInput = screen.getByPlaceholderText("请输入账号");
    await userEvent.type(usernameInput, "admin");

    const loginButton = screen.getByText("登 录");
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText("请输入密码")).toBeInTheDocument();
    });
  });

  test("shows validation error for short password", async () => {
    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    const usernameInput = screen.getByPlaceholderText("请输入账号");
    const passwordInput = screen.getByPlaceholderText("请输入密码");

    await userEvent.type(usernameInput, "admin");
    await userEvent.type(passwordInput, "123");

    const loginButton = screen.getByText("登 录");
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText("密码长度至少8位")).toBeInTheDocument();
    });
  });

  test("shows validation error for password without uppercase", async () => {
    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    const usernameInput = screen.getByPlaceholderText("请输入账号");
    const passwordInput = screen.getByPlaceholderText("请输入密码");

    await userEvent.type(usernameInput, "admin");
    await userEvent.type(passwordInput, "password123!");

    const loginButton = screen.getByText("登 录");
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText("密码必须包含大写字母")).toBeInTheDocument();
    });
  });

  test("successful login calls onLoginSuccess", async () => {
    authService.login.mockResolvedValue({
      success: true,
      token: "mock-token",
      userInfo: { username: "admin", role: "ADMIN" },
    });

    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    const usernameInput = screen.getByPlaceholderText("请输入账号");
    const passwordInput = screen.getByPlaceholderText("请输入密码");

    await userEvent.type(usernameInput, "admin");
    await userEvent.type(passwordInput, "Password123!");

    const loginButton = screen.getByText("登 录");
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith("admin", "Password123!");
      expect(authService.setToken).toHaveBeenCalledWith("mock-token");
      expect(mockOnLoginSuccess).toHaveBeenCalledWith({
        username: "admin",
        role: "ADMIN",
      });
    });
  });

  test("failed login shows error message", async () => {
    authService.login.mockResolvedValue({
      success: false,
      error: "用户名或密码错误",
    });

    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    const usernameInput = screen.getByPlaceholderText("请输入账号");
    const passwordInput = screen.getByPlaceholderText("请输入密码");

    await userEvent.type(usernameInput, "admin");
    await userEvent.type(passwordInput, "Password123!");

    const loginButton = screen.getByText("登 录");
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText("用户名或密码错误")).toBeInTheDocument();
    });
  });

  test("loads saved username when remember me is checked", () => {
    localStorage.setItem(
      "wheatPestRememberUser",
      JSON.stringify({ username: "saveduser" })
    );

    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    const usernameInput = screen.getByPlaceholderText("请输入账号");
    expect(usernameInput.value).toBe("saveduser");
  });

  test("toggles password visibility", async () => {
    render(<LoginForm onLoginSuccess={mockOnLoginSuccess} />);

    const passwordInput = screen.getByPlaceholderText("请输入密码");
    
    // Initially password should be hidden
    expect(passwordInput).toHaveAttribute("type", "password");

    // Find and click the visibility toggle
    const visibilityToggle = screen.getByRole("img", { hidden: true });
    fireEvent.click(visibilityToggle);

    // Password should now be visible
    expect(passwordInput).toHaveAttribute("type", "text");
  });
});
