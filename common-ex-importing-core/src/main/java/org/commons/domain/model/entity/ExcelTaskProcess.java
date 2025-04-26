package org.commons.domain.model.entity

/main/java/org/commons.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 导入导出任务
 * @TableName excel_task_process
 */
@TableName(value ="excel_task_process")
@Data
public class ExcelTaskProcess implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类型：1-导入,2-导出
     */
    private Integer type;

    /**
     * 状态：0-初始,1-进行中,2-完成,3-失败
     */
    private Integer status;

    /**
     * 源文件
     */
    private String sourceFile;

    /**
     * 预估总记录数
     */
    private Long estimateCount;

    /**
     * 实际总记录数
     */
    private Long totalCount;

    /**
     * 成功记录数
     */
    private Long successCount;

    /**
     * 失败记录数
     */
    private Long failedCount;

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
     * chua
     */
    private String creator;

    /**
     * 业务编码
     */
    private String businessCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
