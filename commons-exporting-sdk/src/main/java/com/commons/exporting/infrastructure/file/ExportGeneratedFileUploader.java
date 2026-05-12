package com.commons.exporting.infrastructure.file;

import com.commons.exporting.domain.model.ExportTaskCreateRequest;

import java.io.File;

/**
 * 导出文件上传器。
 * <p>
 * SDK 在业务系统本地生成 Excel 临时文件后，会调用该扩展点完成最终文件上传与成功回写。
 * 默认可通过配置选择两种内置模式：
 * <ul>
 *     <li>上传到 core，由 core 统一存储；</li>
 *     <li>上传到业务系统自身 OSS / MinIO，再将最终文件地址回写给 core。</li>
 * </ul>
 * 业务系统也可直接覆盖该 Bean，自定义整段上传与回写流程。
 */
public interface ExportGeneratedFileUploader {
    /**
     * 处理导出成功后的文件上传与状态回写。
     *
     * @param taskId core 侧任务主键
     * @param request 导出任务请求上下文
     * @param file 本地生成的临时导出文件
     * @param fileName 最终导出文件名
     */
    void upload(Long taskId, ExportTaskCreateRequest request, File file, String fileName);
}

