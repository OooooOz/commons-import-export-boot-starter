package org.commons.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 业务配置。
 */
@Data
@TableName("export_business_config")
public class BusinessConfig implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置类型：IMPORT/EXPORT。
     */
    private String configType;

    /**
     * 业务系统。
     */
    private String businessSystem;

    /**
     * 业务类型。
     */
    private String businessType;

    /**
     * 创建时间。
     */
    private Date createTime;

    /**
     * 更新时间。
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

