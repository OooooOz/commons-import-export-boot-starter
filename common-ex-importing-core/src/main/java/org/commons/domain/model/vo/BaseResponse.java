package org.commons.domain.model.vo;

import org.commons.domain.model.enums.ResponseEnum;

/**
 * 返回基类
 */
public class BaseResponse<T> extends com.eximport.export.shared.model.BaseResponse<T> {

    public BaseResponse() {
        setMessage(ResponseEnum.SUCCESS.getMsg());
        setCode(ResponseEnum.SUCCESS.getCode());
        setSuccess(true);
    }

    public static <T> BaseResponse<T> UNKNOWN() {
        return response(ResponseEnum.UNKNOWN_ERROR, null, false);
    }

    public static <T> BaseResponse<T> SUCCESS() {
        return response(ResponseEnum.SUCCESS, null, true);
    }

    public static <T> BaseResponse<T> SUCCESS(T t) {
        return response(ResponseEnum.SUCCESS, t, true);
    }

    public static <T> BaseResponse<T> FAILURE(ResponseEnum responseEnum) {
        return response(responseEnum, null, false);
    }

    public static <T> BaseResponse<T> FAILURE(String msg) {
        BaseResponse<T> resp = response(ResponseEnum.FAILURE, null, false);
        resp.setMessage(msg);
        return resp;
    }

    private static <T> BaseResponse<T> response(ResponseEnum responseEnum, T data, boolean success) {
        BaseResponse<T> resp = new BaseResponse<>();
        resp.setMessage(responseEnum.getMsg());
        resp.setCode(responseEnum.getCode());
        resp.setSuccess(success);
        resp.setData(data);
        return resp;
    }
}
