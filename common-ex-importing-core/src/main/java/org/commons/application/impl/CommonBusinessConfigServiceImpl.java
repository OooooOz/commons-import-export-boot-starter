package org.commons.application.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.commons.adapter.dto.BusinessConfigPageParamDTO;
import org.commons.adapter.dto.BusinessConfigSaveDTO;
import org.commons.application.CommonBusinessConfigService;
import org.commons.domain.model.entity.BusinessConfig;
import org.commons.domain.model.vo.BusinessConfigVO;
import org.commons.domain.service.BusinessConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务配置应用服务实现。
 */
@Service
public class CommonBusinessConfigServiceImpl implements CommonBusinessConfigService {

    private static final String CONFIG_TYPE_IMPORT = "IMPORT";
    private static final String CONFIG_TYPE_EXPORT = "EXPORT";

    private final BusinessConfigService businessConfigService;

    public CommonBusinessConfigServiceImpl(BusinessConfigService businessConfigService) {
        this.businessConfigService = businessConfigService;
    }

    @Override
    public IPage<BusinessConfigVO> page(BusinessConfigPageParamDTO query) {
        Page<BusinessConfig> page = new Page<>(query.getPage(), query.getSize());
        IPage<BusinessConfig> pageData = businessConfigService.page(page, buildQueryWrapper(query));
        Page<BusinessConfigVO> result = new Page<>(pageData.getCurrent(), pageData.getSize(), pageData.getTotal());
        result.setRecords(pageData.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    @Override
    public BusinessConfigVO getById(Long id) {
        return toVO(businessConfigService.getById(id));
    }

    @Override
    public BusinessConfigVO create(BusinessConfigSaveDTO dto) {
        String configType = normalizeConfigType(dto.getConfigType());
        String businessSystem = normalize(dto.getBusinessSystem());
        String businessType = normalize(dto.getBusinessType());
        validateUnique(configType, businessSystem, businessType, null);
        Date now = new Date();
        BusinessConfig entity = new BusinessConfig();
        entity.setConfigType(configType);
        entity.setBusinessSystem(businessSystem);
        entity.setBusinessType(businessType);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        businessConfigService.save(entity);
        return toVO(entity);
    }

    @Override
    public BusinessConfigVO update(BusinessConfigSaveDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        BusinessConfig entity = businessConfigService.getById(dto.getId());
        if (entity == null) {
            throw new IllegalArgumentException("业务配置不存在");
        }
        String configType = normalizeConfigType(dto.getConfigType());
        String businessSystem = normalize(dto.getBusinessSystem());
        String businessType = normalize(dto.getBusinessType());
        validateUnique(configType, businessSystem, businessType, dto.getId());
        entity.setConfigType(configType);
        entity.setBusinessSystem(businessSystem);
        entity.setBusinessType(businessType);
        entity.setUpdateTime(new Date());
        businessConfigService.updateById(entity);
        return toVO(entity);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        return businessConfigService.removeById(id);
    }

    @Override
    public List<BusinessConfigVO> listOptions() {
        return listOptions(null);
    }

    @Override
    public List<BusinessConfigVO> listOptions(String configType) {
        String normalizedConfigType = StringUtils.hasText(configType) ? normalizeConfigType(configType) : null;
        LambdaQueryWrapper<BusinessConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(normalizedConfigType), BusinessConfig::getConfigType, normalizedConfigType)
                .orderByAsc(BusinessConfig::getBusinessSystem)
                .orderByAsc(BusinessConfig::getBusinessType)
                .orderByAsc(BusinessConfig::getId);
        return businessConfigService.list(queryWrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<BusinessConfig> buildQueryWrapper(BusinessConfigPageParamDTO query) {
        String normalizedConfigType = StringUtils.hasText(query.getConfigType()) ? normalizeConfigType(query.getConfigType()) : null;
        LambdaQueryWrapper<BusinessConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(normalizedConfigType), BusinessConfig::getConfigType, normalizedConfigType)
                .like(StringUtils.hasText(query.getBusinessSystem()), BusinessConfig::getBusinessSystem, normalize(query.getBusinessSystem()))
                .like(StringUtils.hasText(query.getBusinessType()), BusinessConfig::getBusinessType, normalize(query.getBusinessType()))
                .orderByDesc(BusinessConfig::getId);
        return queryWrapper;
    }

    private void validateUnique(String configType, String businessSystem, String businessType, Long excludeId) {
        LambdaQueryWrapper<BusinessConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BusinessConfig::getConfigType, configType)
                .eq(BusinessConfig::getBusinessSystem, businessSystem)
                .eq(BusinessConfig::getBusinessType, businessType)
                .ne(excludeId != null, BusinessConfig::getId, excludeId);
        if (businessConfigService.count(queryWrapper) > 0) {
            throw new IllegalArgumentException("业务系统和业务类型组合已存在");
        }
    }

    private BusinessConfigVO toVO(BusinessConfig entity) {
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, BusinessConfigVO.class);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeConfigType(String configType) {
        String normalized = normalize(configType);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("配置类型不能为空");
        }
        String upper = normalized.toUpperCase();
        if (!CONFIG_TYPE_IMPORT.equals(upper) && !CONFIG_TYPE_EXPORT.equals(upper)) {
            throw new IllegalArgumentException("配置类型只支持IMPORT或EXPORT");
        }
        return upper;
    }
}

