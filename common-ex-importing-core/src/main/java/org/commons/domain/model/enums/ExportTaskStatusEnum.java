package org.commons.domain.model.enums;

import lombok.Getter;

/**
 * 导出任务状态。
 */
@Getter
public enum ExportTaskStatusEnum {
    INIT(0, "初始"),
    PROCESSING(1, "进行中"),
    SUCCESS(2, "完成"),
    FAIL(3, "失败");

    private final int code;
    private final String desc;

    ExportTaskStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

