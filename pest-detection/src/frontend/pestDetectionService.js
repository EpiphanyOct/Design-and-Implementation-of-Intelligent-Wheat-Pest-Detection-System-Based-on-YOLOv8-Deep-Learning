/**
 * 害虫检测服务
 * Feature: Pest Detection
 */
import axios from "axios";

const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/api";

/**
 * 单张图片检测
 * @param {File} file 图片文件
 * @returns {Promise<Object>} 检测结果
 */
async function detect(file) {
  const formData = new FormData();
  formData.append("image", file);

  try {
    const response = await axios.post(
      `${API_BASE_URL}/wheat-pest-detection/detect`,
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 60000,
      }
    );
    return response.data;
  } catch (error) {
    if (error.response) {
      return {
        success: false,
        message: error.response.data.message || "检测失败",
      };
    }
    return {
      success: false,
      message: "网络错误，请检查网络连接",
    };
  }
}

/**
 * 批量图片检测
 * @param {File[]} files 图片文件数组
 * @returns {Promise<Object>} 检测结果
 */
async function batchDetect(files) {
  const formData = new FormData();
  files.forEach((file) => {
    formData.append("images", file);
  });

  try {
    const response = await axios.post(
      `${API_BASE_URL}/wheat-pest-detection/batch-detect`,
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 120000,
      }
    );
    return response.data;
  } catch (error) {
    if (error.response) {
      return {
        success: false,
        message: error.response.data.message || "批量检测失败",
      };
    }
    return {
      success: false,
      message: "网络错误，请检查网络连接",
    };
  }
}

/**
 * 获取检测历史
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 历史记录
 */
async function getHistory(params) {
  try {
    const response = await axios.get(
      `${API_BASE_URL}/wheat-pest-detection/history`,
      { params }
    );
    return {
      success: true,
      data: response.data,
    };
  } catch (error) {
    if (error.response) {
      return {
        success: false,
        message: error.response.data.message || "获取历史记录失败",
      };
    }
    return {
      success: false,
      message: "网络错误，请检查网络连接",
    };
  }
}

/**
 * 查询害虫信息
 * @param {string} pestName 害虫名称
 * @returns {Promise<Object>} 害虫信息
 */
async function getPestInfo(pestName) {
  try {
    const response = await axios.get(
      `${API_BASE_URL}/wheat-pest-detection/pest-info`,
      { params: { pestName } }
    );
    return response.data;
  } catch (error) {
    if (error.response) {
      return {
        success: false,
        message: error.response.data.message || "查询失败",
      };
    }
    return {
      success: false,
      message: "网络错误，请检查网络连接",
    };
  }
}

export default {
  detect,
  batchDetect,
  getHistory,
  getPestInfo,
};
