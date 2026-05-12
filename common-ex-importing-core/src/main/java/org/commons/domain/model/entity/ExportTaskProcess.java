package org.commons.domain.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 导出任务
 * @TableName export_task_process
 */
@TableName(value ="export_task_process")
@Data
public class ExportTaskProcess implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务唯一编号
     */
    private String taskNo;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务系统
     */
    private String businessSystem;

    /**
     * 状态：0-初始,1-进行中,2-完成,3-失败
     */
    private Integer status;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String fileUrl;

    /**
     * 反馈消息
     */
    private String message;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 操作人
     */
    private String creator;

    /**
     * 请求指纹，用于判重同一导出请求。
     */
    @JsonIgnore
    private String requestFingerprint;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
