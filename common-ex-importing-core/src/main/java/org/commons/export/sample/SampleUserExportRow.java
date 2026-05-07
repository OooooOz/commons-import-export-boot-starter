package org.commons.export.sample;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 示例导出行。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleUserExportRow {
    @ExcelProperty("用户ID")
    private Long userId;

    @ExcelProperty("用户名")
    private String userName;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("状态")
    private String status;
}

