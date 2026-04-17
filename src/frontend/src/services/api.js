/**
 * API 服务层
 * 封装所有后端接口调用
 */
import axios from "axios";

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/api";

// 创建axios实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    const user = localStorage.getItem("wheatPestUser") || sessionStorage.getItem("wheatPestUser");
    if (user) {
      const { token } = JSON.parse(user);
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 未授权，清除登录状态
      localStorage.removeItem("wheatPestUser");
      sessionStorage.removeItem("wheatPestUser");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

// 认证相关API
export const authAPI = {
  login: (username, password) =>
    apiClient.post("/auth/login", { username, password }),
  logout: () => apiClient.post("/auth/logout"),
  getCurrentUser: () => apiClient.get("/auth/current-user"),
};

// 害虫检测相关API
export const pestDetectionAPI = {
  detect: (formData) =>
    apiClient.post("/wheat-pest-detection/detect", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  batchDetect: (formData) =>
    apiClient.post("/wheat-pest-detection/batch-detect", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  getHistory: (params) =>
    apiClient.get("/wheat-pest-detection/history", { params }),
  getPestInfo: (pestName) =>
    apiClient.get("/wheat-pest-detection/pest-info", { params: { pestName } }),
};

// 实时监测相关API
export const monitoringAPI = {
  uploadEnvironmentData: (data) =>
    apiClient.post("/wheat-pest/environment-data", data),
  getGrowthStatus: (params) =>
    apiClient.get("/wheat-pest/growth-status", { params }),
  getPestWarnings: (params) =>
    apiClient.get("/wheat-pest/pest-warnings", { params }),
  queryPlots: (filter) =>
    apiClient.post("/wheat-pest/plots/query", filter),
};

// 虫害信息库相关API
export const pestInfoAPI = {
  getList: (params) => apiClient.get("/pest-info/list", { params }),
  getDetail: (id) => apiClient.get(`/pest-info/detail/${id}`),
  add: (data) => apiClient.post("/pest-info/add", data),
  update: (id, data) => apiClient.put(`/pest-info/update/${id}`, data),
  delete: (id) => apiClient.delete(`/pest-info/delete/${id}`),
};

// 数据管理相关API
export const dataManagementAPI = {
  getList: (params) => apiClient.get("/data-management/list", { params }),
  export: (data) => apiClient.post("/data-management/export", data),
  backup: () => apiClient.post("/data-management/backup"),
  restore: (data) => apiClient.post("/data-management/restore", data),
};

// 预警设置相关API
export const alertSettingsAPI = {
  getList: (params) => apiClient.get("/alert-settings/list", { params }),
  add: (data) => apiClient.post("/alert-settings/add", data),
  update: (id, data) => apiClient.put(`/alert-settings/update/${id}`, data),
  delete: (id) => apiClient.delete(`/alert-settings/delete/${id}`),
};

// 报告生成相关API
export const reportsAPI = {
  getList: (params) => apiClient.get("/reports/list", { params }),
  generate: (data) => apiClient.post("/reports/generate", data),
  download: (id) => apiClient.get(`/reports/download/${id}`),
  delete: (id) => apiClient.delete(`/reports/delete/${id}`),
};

// 设备控制相关API
export const devicesAPI = {
  getList: (params) => apiClient.get("/devices/list", { params }),
  control: (data) => apiClient.post("/devices/control", data),
  add: (data) => apiClient.post("/devices/add", data),
  update: (id, data) => apiClient.put(`/devices/update/${id}`, data),
  delete: (id) => apiClient.delete(`/devices/delete/${id}`),
};

export default apiClient;
