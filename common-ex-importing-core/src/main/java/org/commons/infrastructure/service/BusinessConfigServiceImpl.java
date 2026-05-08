package org.commons.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.commons.domain.mapper.BusinessConfigMapper;
import org.commons.domain.model.entity.BusinessConfig;
import org.commons.domain.service.BusinessConfigService;
import org.springframework.stereotype.Service;

/**
 * 业务配置 Service 实现。
 */
@Service
public class BusinessConfigServiceImpl extends ServiceImpl<BusinessConfigMapper, BusinessConfig> implements BusinessConfigService {
}

