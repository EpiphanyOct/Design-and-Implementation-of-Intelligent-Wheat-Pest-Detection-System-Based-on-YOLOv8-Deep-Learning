# Feature: User Authentication

## 分支说明

**分支名称**: `feature/user-authentication`
**关联 User Story**: US-001 用户登录认证

## 功能描述

实现小麦害虫检测系统的用户登录认证功能，包括：
- 用户登录（账号密码验证）
- 密码强度校验
- 记住账号功能
- 用户权限控制

## 文件结构

```
feature/user-authentication/
├── README.md                          # 本文件
├── src/                               # 功能源代码
│   ├── backend/                       # 后端代码
│   │   ├── AuthController.java        # 认证控制器
│   │   ├── UserService.java           # 用户服务
│   │   ├── JwtUtils.java              # JWT工具类
│   │   └── User.java                  # 用户实体
│   └── frontend/                      # 前端代码
│       ├── LoginForm.jsx              # 登录表单组件
│       └── authService.js             # 认证服务
├── tests/                             # 单元测试
│   ├── AuthControllerTest.java        # 控制器测试
│   ├── UserServiceTest.java           # 服务层测试
│   └── LoginForm.test.jsx             # 组件测试
└── temp/                              # 临时/调试文件
    ├── debug.log                      # 调试日志
    └── test-token.txt                 # 测试用Token
```


## 注意事项

1. 密码使用 BCrypt 加密存储
2. JWT Token 有效期设置为 24 小时
3. 登录失败 5 次后锁定账号 30 分钟

## 合并前检查清单

- [ ] 所有单元测试通过
- [ ] 代码符合规范
- [ ] 临时文件已清理
- [ ] 文档已更新
