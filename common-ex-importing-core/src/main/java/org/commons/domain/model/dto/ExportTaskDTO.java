package org.commons.domain.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.Map;

@Data
public class ExportTaskDTO {
    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /**
     * 业务系统
     */
    @NotBlank(message = "业务系统不能为空")
    private String businessSystem;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String fileUrl;

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
     * 额外业务参数
     */
    private Map<String, Object> extMap;

}
