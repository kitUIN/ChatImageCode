# ChatImageCode

[ChatImage](https://github.com/kitUIN/ChatImage) 的依赖库,包含多个版本共用的代码

> 这不是Minecraft 模组,这是抽象出来的共同代码部分

### 实现功能
- CICode 识别
- CQ码 识别
- 图片URI 识别
- Http请求图片下载
- 本地文件加载
- 图片分包/打包 (用于mc C/S之间的数据传输)
- 客户端配置文件
- 错误文本Tooltip
- 依赖Shadow避免撞车
- CICode json(反)序列化

### 安装
```gradle
maven {
    name "kituinMavenReleases"
    url "https://maven.kituin.fun/releases"
}
```

```gradle
modImplementation("io.github.kituin:ChatImageCode:${project.code_version}")
```

