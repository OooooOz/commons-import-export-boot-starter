package org.commons.infrastructure.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 导出任务号生成器。
 */
public final class ExportTaskNoGenerator {
    private ExportTaskNoGenerator() {
    }

    public static String generate() {
        return "EXP" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}

