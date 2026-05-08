package org.commons.domain.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 业务配置返回对象。
 */
@Data
public class BusinessConfigVO {
    private Long id;
    private String configType;
    private String businessSystem;
    private String businessType;
    private Date createTime;
    private Date updateTime;
}

