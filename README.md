# commons-import-export-boot-starter

`commons-import-export-boot-starter` 提供两类能力：

- `commons-exmporting-sdk`：业务系统接入异步导出
- `commons-importing-starter`：业务系统接入 Excel 导入
- `common-ex-importing-core`：独立部署的导出任务 core 服务

## 模块说明

### `common-ex-importing-core`

独立部署的 core 服务，负责：

- 导出任务元数据落库
- 导出任务状态维护
- 导出文件上传与存储
- 前端查询、订阅、下载等能力

### `commons-exmporting-sdk`

业务系统异步导出 SDK，负责：

- 调用 core 创建导出任务
- 在业务系统本地匹配 `AsyncExportHandler`
- 分页查询业务数据并生成 Excel
- 将处理状态和文件上传结果回写到 core

### `commons-importing-starter`

业务系统导入 starter，负责：

- 构建导入器
- 读取 Excel 文件
- 委托监听器进行校验、落库、结果汇总

## 异步导出接入

### 1. 引入依赖

> 当前导出 SDK 的实际发布坐标为 `commons-exmporting-sdk`。

```xml

<dependency>
    <groupId>com.ex-import</groupId>
    <artifactId>commons-exmporting-sdk</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 2. 配置 core 服务地址与异步参数

```yaml
common:
  export:
    core:
      core-url: http://localhost:8091
      connect-timeout-millis: 5000
      read-timeout-millis: 60000
      generated-file-upload-mode: core
    async:
      core-pool-size: 4
      max-pool-size: 8
      queue-capacity: 200
      rejection-policy: abort
      page-size: 5000
      max-rows-per-sheet: 1000000
      max-query-pages: 10000
```

并发调优说明：

- `rejection-policy=abort`：默认推荐值。线程池和队列满时直接拒绝任务，并快速回写失败状态，避免拖垮业务请求线程。
- `rejection-policy=caller-runs`：回退到调用线程执行，不建议高并发在线接口使用，仅适合低并发或离线触发场景。
- `queue-capacity` 过大虽然能减少拒绝，但也会放大任务堆积和执行延迟。
- `max-pool-size` 建议结合数据库、文件存储、下游接口承载能力调整，而不是盲目增大。
- `max-query-pages` 用于防止业务分页实现异常时无限循环查询。

### 3. 数据存储边界

- `common-ex-importing-core` 作为独立 core 服务部署，负责维护导出任务表、业务配置表以及导出文件存储。
- 业务系统不需要创建 `common-ex-importing-core` 中的任何表。
- `commons-exmporting-sdk` 不依赖 `common-ex-importing-core` 的 Java 类，业务系统与 core 之间仅通过 HTTP 接口交互。
- 业务系统 starter 只在本地异步执行业务数据查询和 Excel 生成，任务创建、状态回写、文件上传都通过 HTTP 写入 core 服务。

### 4. 实现 `AsyncExportHandler`

业务系统只需要实现一个处理器，声明：

- 当前处理器所属业务系统 `businessSystem()`
- 当前处理器对应业务类型 `businessType()`
- Excel 表头模型 `headClass()`
- 分页查询逻辑 `queryPage(...)`

```java

@Component
public class OrderExportHandler implements AsyncExportHandler<OrderExportRow> {

    private final OrderQueryService orderQueryService;

    public OrderExportHandler(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @Override
    public String businessSystem() {
        return "order-service";
    }

    @Override
    public String businessType() {
        return "order-list";
    }

    @Override
    public Class<OrderExportRow> headClass() {
        return OrderExportRow.class;
    }

    @Override
    public String sheetName(ExportTaskCreateRequest request) {
        return "订单数据";
    }

    @Override
    public String fileName(ExportTaskCreateRequest request) {
        return "订单导出.xlsx";
    }

    @Override
    public List<OrderExportRow> queryPage(ExportTaskCreateRequest request, long pageNo, int pageSize) {
        return orderQueryService.queryExportRows(request.getExtMap(), pageNo, pageSize);
    }
}
```

### 5. 发起异步导出任务

```java

@RestController
@RequestMapping("/orders")
public class OrderExportController {

    private final Exporting exporting;

    public OrderExportController(Exporting exporting) {
        this.exporting = exporting;
    }

    @PostMapping("/export")
    public ExportTaskInfo export(@RequestBody OrderExportQuery query) {
        ExportTaskCreateRequest request = new ExportTaskCreateRequest();
        request.setTaskName("订单列表导出");
        request.setBusinessSystem("order-service");
        request.setBusinessType("order-list");
        request.setCreator(query.getOperator());
        request.setExtMap(new HashMap<String, Object>() {{
            put("status", query.getStatus());
            put("startTime", query.getStartTime());
            put("endTime", query.getEndTime());
        }});
        return exporting.createTask(request);
    }
}
```

返回的 `ExportTaskInfo` 中会包含：

- `id`：core 侧任务主键
- `taskNo`：core 自动生成的唯一任务号
- `status`：当前任务状态
- `fileName` / `fileUrl`：导出完成后可用

判重说明：

- core 会基于业务侧可见请求字段生成请求指纹：`businessSystem`、`businessType`、`taskName`、`fileName`、`creator`、`extMap`
- 当这些字段完全相同时，会直接复用已有任务，不会重复创建、重复执行导出
- 当前默认复用状态为：`INIT`、`PROCESSING`
- 若历史任务为 `FAIL`、`SUCCESS`，则允许再次创建并重新执行

### 6. 异步线程中的业务上下文透传

如果业务分页查询依赖这些基于 `ThreadLocal` 的上下文：

- 登录用户
- 租户信息
- 数据权限
- 业务链路号 / MDC
- 当前请求头（例如 `orgCode`、`Authorization`、租户头）

那么在线程池异步执行时，这些上下文默认不会自动带到导出线程中。

建议区分两类参数：

1. **显式查询条件**：通过 `ExportTaskCreateRequest.extMap` 传递
2. **隐式 ThreadLocal 上下文**：通过 `AsyncExportContextContributor` 透传

如果业务系统本身是 Web 应用，并且分页查询代码里仍然通过 `HttpServletRequest` / `RequestContextHolder`
读取请求头，那么 SDK 会自动捕获当前请求头并在异步导出线程中恢复一个轻量级请求对象。

也就是说，类似下面这种已有逻辑，在 `AsyncExportHandler.queryPage(...)` 中可以继续直接使用：

```java
public class UserContextHandler {
    public static String getCurrentOrgCode() {
        HttpServletRequest request = HttpContextUtils.getRequest();
        return request == null ? "" : Optional.ofNullable(request.getHeader("orgCode")).orElse("");
    }
}
```

适用场景：

- `HttpContextUtils.getRequest()` 内部基于 `RequestContextHolder`
- 只需要读取请求头、请求路径等轻量信息
- 不建议在异步导出线程中继续依赖原始请求体输入流或会话写操作

示例：透传业务系统自定义的租户和用户上下文

```java
@Component
public class BusinessAsyncExportContextContributor implements AsyncExportContextContributor {

    @Override
    public AsyncExportContextSnapshot capture() {
        final String tenantId = TenantContextHolder.getTenantId();
        final LoginUser loginUser = UserContextHolder.getCurrentUser();

        return new AsyncExportContextSnapshot() {
            @Override
            public void restore() {
                TenantContextHolder.setTenantId(tenantId);
                UserContextHolder.setCurrentUser(loginUser);
            }

            @Override
            public void clear() {
                TenantContextHolder.clear();
                UserContextHolder.clear();
            }
        };
    }
}
```

有了该 Bean 之后，`AsyncExportHandler.queryPage(...)` 在异步线程里也能拿到对应上下文。

### 7. 调用流程

1. 业务系统注入 `Exporting`，调用 `createTask` 创建导出任务。
2. starter 调用 core 的 `/api/export/task/client/create`，core 只落库任务，不执行业务查询。
3. starter 在业务系统本地匹配 `AsyncExportHandler`，分页查询业务数据并生成 Excel。
4. starter 将处理中、成功/失败状态和导出文件回写到 core。
5. 前端继续通过 core 的导出任务查询、SSE 订阅和下载接口查看任务状态与文件。

### 8. 业务系统上传到自身 OSS

SDK 现在支持两种内置控制方式：

- `common.export.core.generated-file-upload-mode=core`：默认模式，先把本地生成的 Excel 临时文件上传回 core，再由 core 按自身配置决定落到本地存储或 core
  自己的 OSS。
- `common.export.core.generated-file-upload-mode=business`：由业务系统把文件上传到自己的 OSS / MinIO，再把最终 `fileUrl` 回写给 core。

如果你选择 `business` 模式，则需要在业务系统实现 `BusinessExportFileUploader`：

```java

@Component
public class BusinessOssExportFileUploader implements BusinessExportFileUploader {

  private final OssUploadService ossUploadService;

  public BusinessOssExportFileUploader(OssUploadService ossUploadService) {
    this.ossUploadService = ossUploadService;
  }

  @Override
  public String upload(ExportTaskCreateRequest request, File file, String fileName) {
    return ossUploadService.upload(file, "exports/" + fileName);
  }
}
```

此时 SDK 会自动：

1. 在业务系统本地生成 Excel
2. 调用你实现的 `BusinessExportFileUploader`
3. 拿到业务 OSS 返回的 `fileUrl`
4. 调用 core 的 `/api/export/task/client/{id}/success` 回写成功状态和文件地址

如果你希望进一步按业务自定义整个上传回写流程，也可以直接覆盖 `ExportGeneratedFileUploader` Bean：

```java

@Component
public class BusinessOssExportGeneratedFileUploader implements ExportGeneratedFileUploader {

  private final RemoteExportTaskClient remoteExportTaskClient;
  private final OssUploadService ossUploadService;

  public BusinessOssExportGeneratedFileUploader(RemoteExportTaskClient remoteExportTaskClient,
                                                OssUploadService ossUploadService) {
    this.remoteExportTaskClient = remoteExportTaskClient;
    this.ossUploadService = ossUploadService;
  }

  @Override
  public void upload(Long taskId, ExportTaskCreateRequest request, File file, String fileName) {
    String fileUrl = ossUploadService.upload(file, "exports/" + fileName);
    remoteExportTaskClient.reportSuccess(taskId, fileName, fileUrl);
  }
}
```

这样处理后：

- Excel 仍然由业务系统本地生成
- 文件上传到业务系统自己的 OSS
- core 只保存任务状态、文件名和 `fileUrl`
- 前端仍然直接使用 `fileUrl` 下载，无需改造页面

优先级说明：

1. **自定义 `ExportGeneratedFileUploader` Bean**：优先级最高，完全由业务系统接管
2. `generated-file-upload-mode=business`：使用内置“业务侧上传 + core 回写 URL”模式
3. `generated-file-upload-mode=core`：使用内置“上传到 core，由 core 存储”模式

对应地，core 新增了一个仅回写文件地址的接口：

- `POST /api/export/task/client/{id}/success`

请求体示例：

```json
{
  "fileName": "订单导出.xlsx",
  "fileUrl": "https://your-business-oss.example.com/exports/order-20260511.xlsx",
  "message": "导出完成"
}
```

## Excel 导入接入

### 1. 引入依赖

```xml

<dependency>
    <groupId>com.ex-import</groupId>
    <artifactId>commons-importing-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 定义导入实体

```java
public class OrderImportRow {
    @ExcelProperty("订单号")
    private String orderNo;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("金额")
    private BigDecimal amount;
}
```

### 3. 实现导入监听器

```java
public class OrderImportListener extends AbstractCommonDataListener<OrderImportRow> {

    private final List<OrderImportRow> cachedRows = new ArrayList<>();
    private final OrderImportService orderImportService;

    public OrderImportListener(OrderImportService orderImportService) {
        this.orderImportService = orderImportService;
    }

    @Override
    protected void singleCheckData(OrderImportRow data) {
        if (data.getOrderNo() == null || data.getOrderNo().trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        cachedRows.add(data);
    }

    @Override
    protected void saveData() {
        orderImportService.batchSave(cachedRows);
        cachedRows.clear();
    }
}
```

### 4. 执行导入

```java

@RestController
@RequestMapping("/orders/import")
public class OrderImportController {

    private final Importing importing;
    private final OrderImportService orderImportService;

    public OrderImportController(Importing importing, OrderImportService orderImportService) {
        this.importing = importing;
        this.orderImportService = orderImportService;
    }

    @PostMapping
    public ImportResultVO importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        Importer<OrderImportRow> importer = importing
                .getImporterFactoryBuilder()
                .entityClass(OrderImportRow.class)
                .maxRows(5000)
                .build()
                .createImporter();

        importer.file(file.getInputStream());
        importer.startImport(new OrderImportListener(orderImportService));
        return importer.getImportResultVO();
    }
}
```

### 5. 导入结果说明

`ImportResultVO` 会返回：

- `success`：成功条数
- `failure`：失败条数
- `msgList`：全局提示或逐行错误信息

## 建议

- 导出场景下，`businessSystem + businessType` 组合必须唯一，否则 starter 无法正确匹配处理器。
- 导入场景下，建议在 `singleCheckData(...)` 中做字段级校验，在 `saveData()` 中做批量入库。
- 若需要前端展示统一的导出任务列表、SSE 状态变化、文件下载能力，建议全部走 core 服务提供的接口。

