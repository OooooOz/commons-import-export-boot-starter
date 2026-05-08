package org.commons.adapter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.commons.adapter.dto.BusinessConfigPageParamDTO;
import org.commons.adapter.dto.BusinessConfigSaveDTO;
import org.commons.application.CommonBusinessConfigService;
import org.commons.domain.model.vo.BaseResponse;
import org.commons.domain.model.vo.BusinessConfigVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 业务配置控制器。
 */
@RestController
@RequestMapping("/api/business/config")
public class BusinessConfigController {

    private final CommonBusinessConfigService commonBusinessConfigService;

    public BusinessConfigController(CommonBusinessConfigService commonBusinessConfigService) {
        this.commonBusinessConfigService = commonBusinessConfigService;
    }

    @GetMapping("/page")
    public BaseResponse<IPage<BusinessConfigVO>> page(@Validated BusinessConfigPageParamDTO query) {
        return BaseResponse.SUCCESS(commonBusinessConfigService.page(query));
    }

    @GetMapping("/options")
    public BaseResponse<List<BusinessConfigVO>> options(@RequestParam(value = "configType", required = false) String configType) {
        return BaseResponse.SUCCESS(commonBusinessConfigService.listOptions(configType));
    }

    @GetMapping("/{id}")
    public BaseResponse<BusinessConfigVO> getById(@PathVariable("id") Long id) {
        return BaseResponse.SUCCESS(commonBusinessConfigService.getById(id));
    }

    @PostMapping("/create")
    public BaseResponse<BusinessConfigVO> create(@RequestBody @Validated BusinessConfigSaveDTO dto) {
        try {
            return BaseResponse.SUCCESS(commonBusinessConfigService.create(dto));
        } catch (IllegalArgumentException e) {
            return BaseResponse.FAILURE(e.getMessage());
        }
    }

    @PostMapping("/update")
    public BaseResponse<BusinessConfigVO> update(@RequestBody @Validated BusinessConfigSaveDTO dto) {
        try {
            return BaseResponse.SUCCESS(commonBusinessConfigService.update(dto));
        } catch (IllegalArgumentException e) {
            return BaseResponse.FAILURE(e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    public BaseResponse<Boolean> delete(@PathVariable("id") Long id) {
        try {
            return BaseResponse.SUCCESS(commonBusinessConfigService.delete(id));
        } catch (IllegalArgumentException e) {
            return BaseResponse.FAILURE(e.getMessage());
        }
    }
}

