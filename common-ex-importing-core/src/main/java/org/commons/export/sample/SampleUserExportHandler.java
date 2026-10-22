package org.commons.export.sample;

import com.eximport.shared.exporting.model.ExportTaskCreateRequest;
import com.eximport.shared.exporting.excel.handler.AsyncExportHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 示例业务导出处理器。真实业务请复制该类，替换为数据库分页查询。
 */
@Component
@ConditionalOnProperty(prefix = "common.export.sample", name = "enabled", havingValue = "true")
public class SampleUserExportHandler implements AsyncExportHandler<SampleUserExportRow> {
    public static final String BUSINESS_SYSTEM = "手动DEMO";
    public static final String BUSINESS_TYPE = "测试";

    @Override
    public String businessSystem() {
        return BUSINESS_SYSTEM;
    }

    @Override
    public String businessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public Class<SampleUserExportRow> headClass() {
        return SampleUserExportRow.class;
    }

    @Override
    public String sheetName(ExportTaskCreateRequest dto) {
        return "用户数据";
    }

    @Override
    public String fileName(ExportTaskCreateRequest dto) {
        return "用户导出.xlsx";
    }

    @Override
    public List<SampleUserExportRow> queryPage(ExportTaskCreateRequest dto, long pageNo, int pageSize) {
        long total = getLong(dto.getExtMap(), "total", 10000L);
        long start = (pageNo - 1L) * pageSize + 1L;
        if (start > total) {
            return new ArrayList<>();
        }
        long end = Math.min(start + pageSize - 1L, total);
        List<SampleUserExportRow> rows = new ArrayList<>((int) (end - start + 1L));
        for (long i = start; i <= end; i++) {
            rows.add(new SampleUserExportRow(i, "用户" + i, "1380000" + String.format("%04d", i % 10000), i % 2 == 0 ? "启用" : "禁用"));
        }
        return rows;
    }

    private long getLong(Map<String, Object> map, String key, long defaultValue) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

