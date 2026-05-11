package com.commons.exporting.domain.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 业务系统提交给导出 SDK 的任务创建模型。
 * <p>
 * 该对象仅描述导出任务的业务属性，不暴露 {@code taskNo}。
 * 任务号由 core 服务在落库时统一生成并保证唯一。
 */
@Data
public class ExportTaskCreateRequest {
    /**
     * 任务名称，用于前端列表展示。
     */
    private String taskName;

    /**
     * 业务类型标识，用于匹配具体的导出处理器。
     */
    private String businessType;

    /**
     * 业务系统标识，用于区分不同业务接入方。
     */
    private String businessSystem;

    /**
     * 导出文件名，可为空；为空时由处理器或默认逻辑生成。
     */
    private String fileName;

    /**
     * sheet 名称，可为空；为空时使用处理器默认值。
     */
    private String sheetName;

    /**
     * 文件地址预留字段，通常由 core 在上传成功后回填最终下载地址。
     */
    private String fileUrl;

    /**
     * 业务侧期望记录的开始时间，可作为查询条件或展示字段。
     */
    private Date startTime;

    /**
     * 业务侧期望记录的结束时间，可作为查询条件或展示字段。
     */
    private Date endTime;

    /**
     * 导出任务发起人。
     */
    private String creator;

    /**
     * 扩展业务参数，将原样传递给导出处理器进行分页查询。
     * <p>
     * 建议将显式查询条件（如状态、时间区间、关键字）通过该字段传入；
     * 若业务依赖 ThreadLocal 中的租户、用户、数据权限等隐式上下文，请结合
     * {@code AsyncExportContextContributor} 进行异步线程上下文透传。
     */
    private Map<String, Object> extMap;
}

