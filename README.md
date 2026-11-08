# 魔姬增强模块 (Mora Magic Module)

魔姬助手 LSPosed 增强模块，为魔姬助手添加菜单注入、消息改写、文件附件等功能。

## 功能

### ✅ 已实现
- **菜单注入**：在魔姬助手右上角三点菜单注入「魔姬增强设置」入口
- **消息改写**：Hook OkHttp `newCall` 拦截请求，自动改写消息内容
- **分级日志系统**：详细记录 Hook 过程、网络请求、请求体内容
- **静态作用域声明**：自动指定作用域为 `com.zte.aiassistant`

### 🚧 开发中
- **文件附件**：按原生 OSS 附件方式发送文件
- **系统提示词修改**：自定义魔姬的系统提示词
- **设置界面**：模块独立设置页面

## 仓库结构

```
mora-magic-module/
├── app/
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml          # 模块清单（LSPosed 配置）
│           ├── assets/
│           │   └── xposed_init               # Xposed 入口类列表
│           ├── dex/
│           │   └── classes.dex              # 预编译 dex（稳定 Hook 版本）
│           ├── java/
│           │   └── com/mora/magicmodule/
│           │       ├── MoraHooker.java       # 主 Hook 类（菜单注入、OkHttp 拦截）
│           │       ├── MoraHooker2.java      # 第二 Hook 类（文件上传）
│           │       ├── FileUploadHelper.java # 文件上传工具类
│           │       └── MainActivity.java     # 模块设置界面
│           └── res/
│               └── values/
│                   └── arrays.xml            # Xposed 作用域配置
├── stub/
│   └── de/robv/android/xposed/              # Xposed API stub 类
│       ├── IXposedHookLoadPackage.java
│       ├── XC_LoadPackage.java
│       ├── XC_MethodHook.java
│       ├── XposedHelpers.java
│       └── AndroidAppHelper.java
├── .github/
│   └── workflows/
│       └── build.yml                        # GitHub Actions 编译流程
└── README.md                                # 本文件
```

## 技术方案

### Multidex 架构
采用 multidex 方案解决预编译 dex 无法修改的问题：

- **classes.dex**：预编译的稳定版本，保证 Hook 正常运行
- **classes2.dex**：新功能代码，从源码编译

### 双 Hook 入口
`xposed_init` 文件支持多个入口类：
```
com.mora.magicmodule.MoraHooker   # 主 Hook（菜单注入、OkHttp 拦截）
com.mora.magicmodule.MoraHooker2  # 第二 Hook（文件上传）
```

## 编译方式

使用 GitHub Actions 云端编译：
1. 推送代码到 main 分支
2. GitHub Actions 自动编译 APK
3. 在 Actions 页面下载编译好的 APK

## 目标应用

- **包名**：`com.zte.aiassistant`
- **应用名**：魔姬助手
- **服务器**：`https://aimoe.ztems.com`

## 接口说明

### 聊天接口
```
POST https://aimoe.ztems.com/aimoe/service/api/v1/chat
```

### OSS 上传
```
POST https://aigc.ztems.com/aigcapi/oss/file/presignedurl
PUT  https://zaigc-oss.oss-cn-shenzhen.aliyuncs.com/tempfiles/1249941/
```

## 版本历史

- **v12.4.x**：multidex 方案，预编译 dex + classes2.dex
- **v12.3.x**：单 dex 从源码编译
- **v12.0.x**：初版，菜单注入 + OkHttp 拦截

## 注意事项

1. 需要 LSPosed 环境（支持 API 102+）
2. 需要在 LSPosed 中启用模块并作用于魔姬助手
3. 重启魔姬助手后生效
