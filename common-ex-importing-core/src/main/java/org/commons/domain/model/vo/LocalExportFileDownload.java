package org.commons.domain.model.vo;

import lombok.Getter;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * 本地导出文件下载结果。
 */
@Getter
public class LocalExportFileDownload {
    private final File file;
    private final int statusCode;
    private final String message;

    private LocalExportFileDownload(File file, int statusCode, String message) {
        this.file = file;
        this.statusCode = statusCode;
        this.message = message;
    }

    public static LocalExportFileDownload success(File file) {
        return new LocalExportFileDownload(file, HttpServletResponse.SC_OK, null);
    }

    public static LocalExportFileDownload notFound(String message) {
        return new LocalExportFileDownload(null, HttpServletResponse.SC_NOT_FOUND, message);
    }

    public boolean downloadable() {
        return file != null;
    }

    public String getFileName() {
        return file == null ? null : file.getName();
    }
}

