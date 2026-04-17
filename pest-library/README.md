# Feature: Pest Library

## 分支说明

**分支名称**: `feature/pest-library`
**关联 User Story**: US-003 虫害信息库管理
**开发者**: [Developer Name]

## 功能描述

实现虫害信息库管理功能，包括：
- 害虫信息查询（按名称、分类）
- 害虫信息新增
- 害虫信息编辑
- 害虫信息删除
- 害虫图片管理

## 文件结构

```
feature/pest-library/
├── README.md
├── src/
│   ├── backend/
│   │   ├── PestInfoController.java
│   │   ├── PestInfoService.java
│   │   └── PestInfo.java
│   └── frontend/
│       ├── PestLibrary.jsx
│       └── pestLibraryService.js
├── tests/
│   ├── PestInfoControllerTest.java
│   ├── PestInfoServiceTest.java
│   └── PestLibrary.test.jsx
└── temp/
    └── pest-data-sample.json
```

## 合并前检查清单

- [ ] 所有单元测试通过
- [ ] 临时文件已清理
