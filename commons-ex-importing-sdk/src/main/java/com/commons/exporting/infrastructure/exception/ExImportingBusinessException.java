package com.commons.exporting.infrastructure.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 全局业务异常
 * 供 ExcelUtil 等 SDK 内部使用。
 */
@Getter
@Setter
@NoArgsConstructor
public class ExImportingBusinessException extends RuntimeException {

    private static final long serialVersionUID = -6842004487143726249L;

    /**
     * 失败状态码
     */
    public static final Integer FAIL_CODE = 500;

    private Integer errCode;

    private String errMsg;

    private Object data;

    public ExImportingBusinessException(String errMsg, Throwable cause) {
        super(errMsg, cause);
        this.errCode = FAIL_CODE;
        this.errMsg = errMsg;
    }

    public ExImportingBusinessException(String errMsg) {
        super(errMsg);
        this.errCode = FAIL_CODE;
        this.errMsg = errMsg;
    }

    public ExImportingBusinessException(Integer errCode, String errMsg, Object data) {
        super(errMsg);
        this.errCode = errCode == null ? FAIL_CODE : errCode;
        this.errMsg = errMsg;
        this.data = data;
    }

    public ExImportingBusinessException(String errMsg, Object data) {
        super(errMsg);
        this.errCode = FAIL_CODE;
        this.errMsg = errMsg;
        this.data = data;
    }

    public ExImportingBusinessException(Integer errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode == null ? FAIL_CODE : errCode;
        this.errMsg = errMsg;
    }
}
