package org.commons.domain.mapper;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.commons.domain.model.dto.ExportTaskPageQuery;
import org.commons.domain.model.entity.ExportTaskProcess;

import java.util.Collection;

/**
 * @author
 * @description 针对表【export_task_process(导出任务)】的数据库操作Mapper
 * @createDate 2025-04-26 20:25:49
 * @Entity src/main/java/org/commons.domain.ExcelTaskProcess
 */
public interface ExportTaskProcessMapper extends BaseMapper<ExportTaskProcess> {

	/**
	 * XML 分页查询导出任务。
	 */
	IPage<ExportTaskProcess> selectTaskPage(Page<ExportTaskProcess> page, @Param("query") ExportTaskPageQuery query);

	/**
	 * 根据请求指纹查询最近一个可复用任务。
	 */
	ExportTaskProcess selectLatestReusableTask(@Param("requestFingerprint") String requestFingerprint,
	                                         @Param("statuses") Collection<Integer> statuses);
}




