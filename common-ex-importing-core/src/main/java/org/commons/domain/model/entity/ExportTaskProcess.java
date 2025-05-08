package org.commons.domain.model.entity;


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
    @TableId(type = IdType.ID_WORKER)
    private Long id;

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


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
