package org.commons.domain.model.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.commons.domain.model.enums.ResponseEnum;

/**
 * @author chenzw36
 * @Description
 * @since 2021/4/19 17:29
 */
@Getter
@Setter
@NoArgsConstructor
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -6842004487143726249L;

    private String errCode;

    private String errMsg;

    public BusinessException(String errMsg) {
        super(errMsg);
        errCode = ResponseEnum.FAILURE.getCode();
        this.errMsg = errMsg;
    }

    public BusinessException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
