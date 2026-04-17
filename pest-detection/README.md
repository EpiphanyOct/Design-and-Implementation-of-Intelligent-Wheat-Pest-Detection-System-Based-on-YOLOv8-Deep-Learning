# Feature: Pest Detection

## 分支说明

**分支名称**: `feature/pest-detection`
**关联 User Story**: US-002 害虫图片识别
**开发者**: [Developer Name]

## 功能描述

实现小麦害虫图片智能识别功能，包括：
- 单张图片害虫检测
- 批量图片害虫检测
- 害虫种类识别与置信度评估
- 虫害诊断结果生成
- 防治建议推荐
- 检测历史记录查询

## 文件结构

```
feature/pest-detection/
├── README.md                          # 本文件
├── src/                               # 功能源代码
│   ├── backend/                       # 后端代码
│   │   ├── PestDetectionController.java
│   │   ├── PestDetectionService.java
│   │   ├── ImageUtils.java
│   │   └── PestDetectionRecord.java
│   └── frontend/                      # 前端代码
│       ├── PestSearch.jsx
│       └── pestDetectionService.js
├── tests/                             # 单元测试
│   ├── PestDetectionControllerTest.java
│   ├── PestDetectionServiceTest.java
│   └── PestSearch.test.jsx
└── temp/                              # 临时/调试文件
    ├── sample-images/                 # 测试图片
    └── detection-test.log
```


## 注意事项

1. 支持图片格式: JPG, PNG, BMP
2. 单张图片最大 5MB
3. 批量检测最多支持 20 张图片
4. 识别结果置信度阈值: 0.5

## 合并前检查清单

- [ ] 所有单元测试通过
- [ ] 图片上传功能测试通过
- [ ] 临时文件已清理
- [ ] 文档已更新
