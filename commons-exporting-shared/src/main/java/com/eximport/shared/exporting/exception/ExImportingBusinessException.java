package com.eximport.shared.exporting.exception;

/**
 * 全局业务异常（导出/导入 SDK 公共异常）。
 * <p>
 * 供 core、sdk 及业务系统统一处理：catch 本类型即可覆盖公共组件抛出的业务错误。
 */
public class ExImportingBusinessException extends RuntimeException {

    private static final long serialVersionUID = -6842004487143726249L;

    /**
     * 失败状态码
     */
    public static final Integer FAIL_CODE = 500;

    private Integer errCode;

    private String errMsg;

    private Object data;

    public ExImportingBusinessException() {
        this.errCode = FAIL_CODE;
    }

    public ExImportingBusinessException(String errMsg) {
        super(errMsg);
        this.errCode = FAIL_CODE;
        this.errMsg = errMsg;
    }

    public ExImportingBusinessException(String errMsg, Throwable cause) {
        super(errMsg, cause);
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

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
