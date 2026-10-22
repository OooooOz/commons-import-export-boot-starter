package com.commons.exporting.infrastructure.util;

import com.eximport.shared.exporting.exception.ExImportingBusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对象与 Map 互转工具类（基于 Jackson convertValue，支持深转换）。
 *
 * <p><b>实现</b>：共享单例 {@link ObjectMapper}（线程安全）完成对象与 Map 的双向转换，
 * 转换过程经过 Jackson 类型系统，与 JSON 序列化语义一致。</p>
 *
 * <p><b>深转换</b>：嵌套 POJO、List/数组、Map 等复杂结构均递归展开或还原
 * （对象 → 嵌套 {@code Map<String, Object>}，嵌套 Map → 还原为对应 POJO 字段），
 * 与浅转换工具（直接透传对象引用）语义不同。</p>
 *
 * <p><b>高效</b>：ObjectMapper 单例复用，Jackson 内部已对序列化器做充分缓存，
 * 避免每次转换重复构建解析器。</p>
 *
 * <p><b>安全</b>：</p>
 * <ul>
 *     <li>入参 null 安全：{@code toMap(null)} 返回空 Map，{@code toObject(null, ...)} 返回 null；</li>
 *     <li>反序列化关闭 {@code FAIL_ON_UNKNOWN_PROPERTIES}：Map 中多余 key 自动忽略，不抛异常；</li>
 *     <li>ObjectMapper 线程安全，静态单例可并发复用；</li>
 *     <li>循环引用等复杂结构由 Jackson 标准机制处理（数组/集合类型默认支持，
 *         纯 POJO 循环引用场景遵循与 JSON 序列化相同的约束，需业务侧 DTO 规避）。</li>
 * </ul>
 *
 * <p><b>注意</b>：转换语义遵循 Jackson 默认规则——基于 getter/setter/public 字段而非全部声明字段；
 * 时间类型（LocalDateTime 等 JDK8 时间）经 {@link JavaTimeModule} 支持并输出 ISO-8601 字符串，
 * 而非时间戳；如需自定义格式请对 DTO 字段加 Jackson 注解（如 {@code @JsonFormat}）。</p>
 *
 * @author commons-exporting
 */
@Slf4j
public final class ObjectMapConvertUtil {

    private ObjectMapConvertUtil() {}

    /**
     * 共享单例 ObjectMapper：线程安全，配置一次全局生效。
     * <ul>
     *     <li>注册 JavaTimeModule：支持 LocalDateTime / LocalDate / LocalTime / Instant 等 JDK8 时间类型；</li>
     *     <li>关闭 WRITE_DATES_AS_TIMESTAMPS：时间输出 ISO-8601 字符串（如 2026-10-21T10:30:00），可读且可无损还原；</li>
     *     <li>关闭 FAIL_ON_UNKNOWN_PROPERTIES：Map 中多余 key 自动忽略。</li>
     * </ul>
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // ==================== 对象转 Map（深转换） ====================

    /**
     * 对象转 Map（深转换）。
     *
     * <p>嵌套对象递归展开为 {@code Map<String, Object>}，集合/数组元素逐个转换，
     * 结果为字段名到值的 LinkedHashMap，顺序与 Jackson 输出属性顺序一致。</p>
     *
     * @param bean 源对象，允许为 null
     * @return 转换后的新 Map；入参为 null 时返回空 Map
     * @throws ExImportingBusinessException 转换失败（含不可序列化字段等）时抛出
     */
    public static Map<String, Object> toMap(Object bean) {
        if (bean == null) {
            return new LinkedHashMap<>(0);
        }
        try {
            return MAPPER.convertValue(bean, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (IllegalArgumentException e) {
            log.error("[ObjectMapConvertUtil#toMap]转换失败, class={}", bean.getClass().getName(), e);
            throw new ExImportingBusinessException("对象转Map失败: " + bean.getClass().getName(), e);
        }
    }

    // ==================== Map 转对象（深转换） ====================

    /**
     * Map 转对象（深转换）。
     *
     * <p>嵌套 Map 递归还原为对应 POJO 字段，集合元素同样逐个还原；
     * Map 中存在目标类型未定义的 key 时自动忽略（不抛异常）。</p>
     *
     * @param source 源 Map，允许为 null
     * @param clazz 目标类型
     * @param <T> 目标类型
     * @return 新建实例并回填；source 为 null 或 clazz 为 null 时返回 null
     * @throws ExImportingBusinessException 转换失败（类型不匹配且无法兼容）时抛出
     */
    public static <T> T toObject(Map<String, Object> source, Class<T> clazz) {
        if (source == null || clazz == null) {
            return null;
        }
        try {
            return MAPPER.convertValue(source, clazz);
        } catch (IllegalArgumentException e) {
            log.error("[ObjectMapConvertUtil#toObject]转换失败, target={}", clazz.getName(), e);
            throw new ExImportingBusinessException("Map转对象失败: " + clazz.getName(), e);
        }
    }

    /**
     * Map 转对象（深转换，支持泛型目标）。
     *
     * <p>用于带泛型的目标类型，如：
     * {@code toObject(map, new TypeReference<ExportTaskInfo&lt;OrderDTO&gt;&gt;() {})}。</p>
     *
     * @param source 源 Map，允许为 null
     * @param typeRef 目标泛型类型引用
     * @param <T> 目标类型
     * @return 新建实例并回填；source 为 null 或 typeRef 为 null 时返回 null
     * @throws ExImportingBusinessException 转换失败时抛出
     */
    public static <T> T toObject(Map<String, Object> source, TypeReference<T> typeRef) {
        if (source == null || typeRef == null) {
            return null;
        }
        try {
            return MAPPER.convertValue(source, typeRef);
        } catch (IllegalArgumentException e) {
            log.error("[ObjectMapConvertUtil#toObject]泛型转换失败", e);
            throw new ExImportingBusinessException("Map转对象失败(泛型)", e);
        }
    }
}
