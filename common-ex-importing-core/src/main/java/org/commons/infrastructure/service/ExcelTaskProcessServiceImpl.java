package org.commons.infrastructure.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.commons.domain.mapper.ExcelTaskProcessMapper;
import org.commons.domain.model.entity.ExcelTaskProcess;
import org.commons.domain.service.ExcelTaskProcessService;
import org.springframework.stereotype.Service;

/**
* @author c-zhongwh01
* @description 针对表【excel_task_process(导入导出任务)】的数据库操作Service实现
* @createDate 2025-04-26 20:25:49
*/
@Service
public class ExcelTaskProcessServiceImpl extends ServiceImpl<ExcelTaskProcessMapper, ExcelTaskProcess> implements ExcelTaskProcessService {

}




