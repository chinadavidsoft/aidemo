# aidemo

`aidemo` 是一个基于 Java 11 的命令行对话工具，支持在同一次进程里连续多轮对话（REPL）并使用流式输出。

## 功能

- `aidemo chat` 进入交互式连续对话
- 会话上下文在进程内持续保留
- `/clear` 手动清空上下文
- `/exit` 或 `Ctrl+D` 退出
- 真实 SSE 流式输出（边接收边打印）

## 环境要求

- JDK 11+
- Maven 3.9+
- DeepSeek API Key（放在项目根目录 `deepseek.key` 文件里，单行文本）

## 快速开始

1. 在项目根目录创建 `deepseek.key`：

```bash
echo 'your_api_key_here' > deepseek.key
```

2. 打包：

```bash
mvn -DskipTests package
```

3. 启动：

```bash
java -jar target/aidemo-1.0-SNAPSHOT.jar chat
```

进入后会看到提示符：

```text
you>
```

## 交互命令

- `/clear`：清空当前会话历史
- `/exit`：退出程序
- 空行：忽略，不发送请求

## 在 IDEA 直接启动

Run Configuration 建议如下：

- Main class: `org.example.App`
- Program arguments: `chat`
- Use classpath of module: `aidemo`
- Working directory: 项目根目录（`$ProjectFileDir$`）

## 项目结构

```text
src/main/java/org/example
├── App.java                      # 启动入口
├── cli
│   ├── CliApplication.java       # 应用装配与启动流程
│   ├── CliArgs.java              # 参数解析与 usage
│   └── ConsoleChatRunner.java    # REPL 交互循环
├── client
│   ├── DeepSeekChatClient.java   # DeepSeek HTTP + SSE 客户端
│   ├── StreamingChatClient.java  # 客户端接口
│   └── TokenSink.java            # 流式 token 回调
├── config
│   ├── ApiKeyProvider.java       # API Key 读取
│   └── ChatConfig.java           # 配置对象
├── model
│   └── ChatMessage.java          # 消息模型
└── service
    └── ChatSession.java          # 会话历史管理
```

## 测试

```bash
mvn test
```

当前测试覆盖：

- CLI 参数与 usage
- 流式请求 payload 组装
- SSE delta 内容解析（含错误分支）
