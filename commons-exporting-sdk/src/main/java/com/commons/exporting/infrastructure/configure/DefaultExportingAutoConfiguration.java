package com.commons.exporting.infrastructure.configure;


import com.commons.exporting.configure.ExportAsyncProperties;
import com.commons.exporting.configure.ExportCoreProperties;
import com.commons.exporting.domain.service.Exporting;
import com.commons.exporting.infrastructure.client.ExportTaskCoreFeignClient;
import com.commons.exporting.infrastructure.client.RemoteExportTaskClient;
import com.commons.exporting.infrastructure.context.AsyncExportContextContributor;
import com.commons.exporting.infrastructure.context.AsyncExportContextPropagator;
import com.commons.exporting.infrastructure.context.CompositeAsyncExportContextPropagator;
import com.commons.exporting.infrastructure.context.RequestHeaderAsyncExportContextContributor;
import com.commons.exporting.infrastructure.handle.AsyncExportHandler;
import com.commons.exporting.infrastructure.service.DefaultExporting;
import com.commons.exporting.infrastructure.service.StarterAsyncExportExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Request;
import feign.Util;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 导出 SDK 自动配置。
 * <p>
 * 负责注册访问 core 服务的轻量 HTTP 客户端、异步执行器以及业务系统可直接注入的
 * {@link Exporting} 门面实现。
 */
@AutoConfiguration
@EnableConfigurationProperties({ExportAsyncProperties.class, ExportCoreProperties.class})
public class DefaultExportingAutoConfiguration {
    public DefaultExportingAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean
    public ExportTaskCoreFeignClient exportTaskCoreFeignClient(ExportCoreProperties exportCoreProperties,
                                                               ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return Feign.builder()
                .encoder(new FormEncoder(jsonEncoder(objectMapper)))
                .decoder(jsonDecoder(objectMapper))
                .options(new Request.Options(
                        exportCoreProperties.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS,
                        exportCoreProperties.getReadTimeoutMillis(), TimeUnit.MILLISECONDS,
                        true))
                .target(ExportTaskCoreFeignClient.class, trimTrailingSlash(exportCoreProperties.getCoreUrl()));
    }

    @Bean
    @ConditionalOnMissingBean
    public RemoteExportTaskClient remoteExportTaskClient(ExportTaskCoreFeignClient exportTaskCoreFeignClient) {
        return new RemoteExportTaskClient(exportTaskCoreFeignClient);
    }

    /**
     * 注册异步导出上下文透传器。
     * <p>
     * 业务系统可声明一个或多个 {@link AsyncExportContextContributor} Bean，用于捕获并恢复租户、用户、
     * 数据权限等 ThreadLocal 上下文。未声明时默认使用空实现。
     */
    @Bean
    @ConditionalOnClass(name = {
            "org.springframework.web.context.request.RequestContextHolder",
            "org.springframework.web.context.request.ServletRequestAttributes",
            "javax.servlet.http.HttpServletRequest"
    })
    public AsyncExportContextContributor requestHeaderAsyncExportContextContributor() {
        return new RequestHeaderAsyncExportContextContributor();
    }

    @Bean
    @ConditionalOnMissingBean
    public AsyncExportContextPropagator asyncExportContextPropagator(ObjectProvider<AsyncExportContextContributor> contributorsProvider) {
        return new CompositeAsyncExportContextPropagator(contributorsProvider.orderedStream().collect(Collectors.toList()));
    }

    /**
     * 注册本地异步导出执行器，并收集业务系统实现的全部导出处理器。
     */
    @Bean
    @ConditionalOnMissingBean
    public StarterAsyncExportExecutor starterAsyncExportExecutor(RemoteExportTaskClient remoteExportTaskClient,
                                                                 ExportAsyncProperties exportAsyncProperties,
                                                                 AsyncExportContextPropagator asyncExportContextPropagator,
                                                                 ObjectProvider<AsyncExportHandler<?>> handlersProvider) {
        List<AsyncExportHandler<?>> handlers = handlersProvider.orderedStream().collect(Collectors.toList());
        return new StarterAsyncExportExecutor(remoteExportTaskClient, exportAsyncProperties, asyncExportContextPropagator, handlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public Exporting exporting(RemoteExportTaskClient remoteExportTaskClient,
                               StarterAsyncExportExecutor starterAsyncExportExecutor) {
        return new DefaultExporting(remoteExportTaskClient, starterAsyncExportExecutor);
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) return "http://10.39.11.156:8091";
        String result = value.trim();
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }

    private Encoder jsonEncoder(ObjectMapper objectMapper) {
        return (object, bodyType, requestTemplate) -> {
            if (object == null) return;
            try {
                requestTemplate.body(objectMapper.writeValueAsBytes(object), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new EncodeException("JSON请求体序列化失败", e);
            }
        };
    }

    private Decoder jsonDecoder(ObjectMapper objectMapper) {
        return (response, type) -> {
            if (response.body() == null) return Util.emptyValueOf(type);
            return readResponseBody(objectMapper, response.body().asInputStream(), type);
        };
    }

    private Object readResponseBody(ObjectMapper objectMapper, java.io.InputStream inputStream, Type type) throws IOException {
        try {
            return objectMapper.readValue(inputStream, objectMapper.getTypeFactory().constructType(type));
        } finally {
            inputStream.close();
        }
    }
}
