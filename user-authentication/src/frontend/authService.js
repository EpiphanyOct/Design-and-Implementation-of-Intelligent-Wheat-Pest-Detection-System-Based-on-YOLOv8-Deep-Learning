/**
 * 认证服务
 * Feature: User Authentication
 */
import axios from "axios";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/api";

// Token 存储键名
const TOKEN_KEY = "wheatPestToken";
const USER_INFO_KEY = "wheatPestUserInfo";

/**
 * 登录
 * @param {string} username 用户名
 * @param {string} password 密码
 * @returns {Promise<Object>} 登录结果
 */
async function login(username, password) {
  try {
    const response = await axios.post(`${API_BASE_URL}/auth/login`, {
      username,
      password,
    });
    return response.data;
  } catch (error) {
    if (error.response) {
      return {
        success: false,
        error: error.response.data.error || "登录失败",
      };
    }
    return {
      success: false,
      error: "网络错误，请检查网络连接",
    };
  }
}

/**
 * 登出
 */
async function logout() {
  const token = getToken();
  if (token) {
    try {
      await axios.post(
        `${API_BASE_URL}/auth/logout`,
        {},
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
    } catch (error) {
      console.error("Logout error:", error);
    }
  }
  clearToken();
}

/**
 * 获取当前用户信息
 */
async function getCurrentUser() {
  const token = getToken();
  if (!token) {
    return null;
  }

  try {
    const response = await axios.get(`${API_BASE_URL}/auth/current-user`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  } catch (error) {
    console.error("Get current user error:", error);
    return null;
  }
}

/**
 * 设置Token
 */
function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * 获取Token
 */
function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * 清除Token
 */
function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_INFO_KEY);
}

/**
 * 检查是否已登录
 */
function isAuthenticated() {
  return !!getToken();
}

/**
 * 设置请求拦截器
 */
function setupAxiosInterceptors() {
  // 请求拦截器 - 添加Token
  axios.interceptors.request.use(
    (config) => {
      const token = getToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // 响应拦截器 - 处理401错误
  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        clearToken();
        window.location.href = "/login";
      }
      return Promise.reject(error);
    }
  );
}

// 初始化拦截器
setupAxiosInterceptors();

export default {
  login,
  logout,
  getCurrentUser,
  setToken,
  getToken,
  clearToken,
  isAuthenticated,
};
