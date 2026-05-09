package org.commons.exporting.infrastructure.handle;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.export.handler.ExportTaskHandler;
import org.commons.exporting.domain.model.ExportTaskCreateRequest;

import java.util.List;

/**
 * starter 暴露给业务系统的异步导出处理器。
 * 业务系统实现该接口即可接入异步导出，无需直接感知 core 模块接口。
 *
 * @param <T> Excel 行模型
 */
public interface AsyncExportHandler<T> extends ExportTaskHandler<T> {

    /**
     * sheet 名称。
     */
    default String sheetName(ExportTaskCreateRequest request) {
        return "数据";
    }

    /**
     * 默认文件名，不含路径。
     */
    default String fileName(ExportTaskCreateRequest request) {
        return request.getBusinessType() + ".xlsx";
    }

    /**
     * 自定义 EasyExcel 写入构建器。
     */
    default void customizeWriter(ExportTaskCreateRequest request, ExcelWriterBuilder writerBuilder) {
        // 默认无扩展
    }

    /**
     * 分页查询业务数据。
     */
    List<T> queryPage(ExportTaskCreateRequest request, long pageNo, int pageSize);

    @Override
    default String sheetName(ExportTaskDTO dto) {
        return sheetName(toRequest(dto));
    }

    @Override
    default String fileName(ExportTaskDTO dto) {
        return fileName(toRequest(dto));
    }

    @Override
    default void customizeWriter(ExportTaskDTO dto, ExcelWriterBuilder writerBuilder) {
        customizeWriter(toRequest(dto), writerBuilder);
    }

    @Override
    default List<T> queryPage(ExportTaskDTO dto, long pageNo, int pageSize) {
        return queryPage(toRequest(dto), pageNo, pageSize);
    }

    static ExportTaskCreateRequest toRequest(ExportTaskDTO dto) {
        ExportTaskCreateRequest request = new ExportTaskCreateRequest();
        if (dto == null) {
            return request;
        }
        request.setTaskNo(dto.getTaskNo());
        request.setTaskName(dto.getTaskName());
        request.setBusinessType(dto.getBusinessType());
        request.setBusinessSystem(dto.getBusinessSystem());
        request.setFileName(dto.getFileName());
        request.setSheetName(dto.getSheetName());
        request.setFileUrl(dto.getFileUrl());
        request.setStartTime(dto.getStartTime());
        request.setEndTime(dto.getEndTime());
        request.setCreator(dto.getCreator());
        request.setExtMap(dto.getExtMap());
        return request;
    }
}

