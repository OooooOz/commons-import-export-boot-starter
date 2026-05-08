package org.commons.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.commons.adapter.dto.BusinessConfigPageParamDTO;
import org.commons.adapter.dto.BusinessConfigSaveDTO;
import org.commons.domain.model.vo.BusinessConfigVO;

import java.util.List;

/**
 * 业务配置应用服务。
 */
public interface CommonBusinessConfigService {
    IPage<BusinessConfigVO> page(BusinessConfigPageParamDTO query);

    BusinessConfigVO getById(Long id);

    BusinessConfigVO create(BusinessConfigSaveDTO dto);

    BusinessConfigVO update(BusinessConfigSaveDTO dto);

    boolean delete(Long id);

    List<BusinessConfigVO> listOptions();

    List<BusinessConfigVO> listOptions(String configType);
}

