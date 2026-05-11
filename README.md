# commons-import-export-boot-starter

## 异步导出接入方式

业务系统只需要引入 `commons-ex-importing-sdk`，实现 `AsyncExportHandler` 并配置 core 服务地址即可发起异步导出。

```yaml
common:
  export:
    core:
      core-url: http://localhost:8091
```

### 数据存储边界

- `common-ex-importing-core` 作为独立 core 服务部署，负责维护导出任务表、业务配置表以及导出文件存储。
- 业务系统不需要创建 `common-ex-importing-core` 中的任何表。
- 业务系统 starter 只在本地异步执行业务数据查询和 Excel 生成，任务创建、状态回写、文件上传都通过 HTTP 写入 core 服务。

### 调用流程

1. 业务系统注入 `Exporting`，调用 `createTask` 创建导出任务。
2. starter 调用 core 的 `/api/export/task/client/create`，core 只落库任务，不执行业务查询。
3. starter 在业务系统本地匹配 `AsyncExportHandler`，分页查询业务数据并生成 Excel。
4. starter 将处理中、成功/失败状态和导出文件回写到 core。
5. 前端继续通过 core 的导出任务查询、SSE 订阅和下载接口查看任务状态与文件。
