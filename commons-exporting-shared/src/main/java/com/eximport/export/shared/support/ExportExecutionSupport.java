package com.eximport.export.shared.support;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 导出执行公共支持类。
 */
public final class ExportExecutionSupport {
    private static final String DEFAULT_FAILURE_MESSAGE = "导出失败";
    private static final int DEFAULT_MAX_MESSAGE_LENGTH = 250;

    private ExportExecutionSupport() {
    }

    public static String normalizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        }
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            fileName = fileName + ".xlsx";
        }
        return fileName;
    }

    public static String limitMessage(String message) {
        return limitMessage(message, DEFAULT_FAILURE_MESSAGE, DEFAULT_MAX_MESSAGE_LENGTH);
    }

    public static String limitMessage(String message, String defaultMessage, int maxLength) {
        String fallback = (defaultMessage == null || defaultMessage.trim().isEmpty()) ? DEFAULT_FAILURE_MESSAGE : defaultMessage;
        if (message == null || message.trim().isEmpty()) {
            return fallback;
        }
        if (maxLength <= 0 || message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength);
    }

    public static RejectedExecutionHandler resolveRejectedExecutionHandler(String rejectionPolicy) {
        String policy = normalizeRejectionPolicy(rejectionPolicy);
        if ("abort".equals(policy)) {
            return new ThreadPoolExecutor.AbortPolicy();
        }
        if ("caller-runs".equals(policy)) {
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }
        throw new IllegalArgumentException("不支持的导出线程池拒绝策略: " + rejectionPolicy + "，仅支持 abort 或 caller-runs");
    }

    public static String normalizeRejectionPolicy(String rejectionPolicy) {
        if (rejectionPolicy == null || rejectionPolicy.trim().isEmpty()) {
            return "abort";
        }
        return rejectionPolicy.trim().toLowerCase(Locale.ROOT);
    }
}

