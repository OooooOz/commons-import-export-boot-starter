package com.commons.exporting.infrastructure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside  // 关键注解，允许组合Jackson注解
@JsonSerialize(using = BigDecimalSerializer.class)  // 直接关联序列化器
@JsonDeserialize(using = BigDecimalDeserializer.class) // 直接关联反序列化器
public @interface BigDecimalFormat {
    int scale() default 2;

    RoundingMode roundingMode() default RoundingMode.HALF_UP;
}
