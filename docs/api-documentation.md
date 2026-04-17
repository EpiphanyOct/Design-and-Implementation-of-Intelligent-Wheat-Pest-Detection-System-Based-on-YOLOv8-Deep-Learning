# API 接口文档

## 概述

本文档描述了小麦害虫检测系统的所有API接口，包括请求参数、响应格式和错误码说明。

**基础URL**: `http://localhost:8080/api`

**内容类型**: `application/json`

---

## 认证接口

### 用户登录

**接口**: `POST /auth/login`

**描述**: 用户登录系统，获取访问令牌

**请求参数**:
```json
{
  "username": "string",    // 用户名，4-20位字母数字下划线
  "password": "string"     // 密码，至少8位，包含大小写字母、数字和特殊字符
}
```

**响应示例**:
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userInfo": {
    "username": "admin",
    "permission": "管理员"
  }
}
```

**错误码**:
- `400`: 请求参数错误
- `401`: 认证失败
- `500`: 服务器内部错误

---

## 害虫检测接口

### 单张图片害虫检测

**接口**: `POST /wheat-pest-detection/detect`

**描述**: 上传图片进行害虫种类识别

**请求格式**: `multipart/form-data`

**请求参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| image | File | 是 | 图片文件，支持JPG/PNG/BMP格式，最大5MB |

**响应示例**:
```json
{
  "success": true,
  "data": {
    "pestType": "红蜘蛛",
    "confidence": 0.92,
    "diagnosis": "寄生于小麦叶片，取食汁液导致叶片枯黄",
    "suggestions": "1. 定期检查叶片，发现早期虫害立即喷药\n2. 施用生物农药减少害虫数量",
    "imageUrl": "/uploads/detect_20240101_120000.jpg",
    "detectTime": "2024-01-01T12:00:00"
  }
}
```

### 批量害虫检测

**接口**: `POST /wheat-pest-detection/batch-detect`

**描述**: 批量上传图片进行害虫检测，最多支持20张

**请求格式**: `multipart/form-data`

**请求参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| images | File[] | 是 | 图片文件数组，1-20张 |

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "pestType": "红蜘蛛",
      "confidence": 0.92,
      "diagnosis": "...",
      "suggestions": "..."
    }
  ]
}
```

### 查询检测历史

**接口**: `GET /wheat-pest-detection/history`

**描述**: 查询历史检测记录，支持时间筛选

**请求参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| startTime | string | 否 | 开始时间，格式：yyyy-MM-dd HH:mm:ss |
| endTime | string | 否 | 结束时间，格式：yyyy-MM-dd HH:mm:ss |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
  "success": true,
  "data": {
    "records": [...],
    "total": 100,
    "current": 1,
    "pageSize": 10
  }
}
```

### 查询害虫信息

**接口**: `GET /wheat-pest-detection/pest-info`

**描述**: 根据害虫名称查询详细信息，支持模糊查询

**请求参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| pestName | string | 是 | 害虫名称，最大50字符 |

**响应示例**:
```json
{
  "success": true,
  "data": {
    "pestId": "P001",
    "pestName": "红蜘蛛",
    "description": "寄生于小麦叶片，取食汁液导致叶片枯黄",
    "commonSymptoms": "叶片出现黄白色斑点",
    "suggestedPrevention": "定期检查叶片，发现早期虫害立即喷药"
  }
}
```


## 附录

### 支持的害虫类型

| 害虫ID | 害虫名称 | 描述 |
|--------|----------|------|
| P001 | 红蜘蛛 | 寄生于小麦叶片，取食汁液 |
| P002 | 麦蚜 | 吸取小麦汁液，导致叶片卷曲 |
| P003 | 小麦螟虫 | 幼虫蛀食小麦茎秆 |

### 数据类型说明

| 数据类型 | 说明 |
|----------|------|
| MONITOR | 监测数据 |
| DETECTION | 检测数据 |
| ALERT | 预警数据 |
| REPORT | 报告数据 |
