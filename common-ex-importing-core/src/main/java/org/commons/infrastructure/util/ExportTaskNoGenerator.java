package org.commons.infrastructure.util;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 导出任务号生成器。
 */
public final class ExportTaskNoGenerator {
    private ExportTaskNoGenerator() {
    }

    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Generates a unique serial number.
     *
     * @return A unique serial number as a String.
     */
    public static String generateSerialNumber() {
        // Get current timestamp in the format yyyyMMddHHmmss
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);

        // Generate a random 6-digit number
        int randomNumber = 100000 + random.nextInt(900000);

        // Combine timestamp and random number to form the serial number
        return timestamp + String.format("%06d", randomNumber);
    }

    public static String generateUniqueCostCode() {
        // 获取当前时间戳并格式化为 YYYYMMDDHHMMSS
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());

        // 生成6位随机数
        int randomNumber = 100000 + random.nextInt(900000); // 生成范围在 100000 到 999999 之间的随机数

        // 组合时间戳和随机数
        return timestamp + String.valueOf(randomNumber);
    }
}

