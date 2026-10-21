package com.commons.exporting.infrastructure.annotation;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

public class BigDecimalSerializer extends JsonSerializer<BigDecimal> implements ContextualSerializer {
    private int scale;
    private RoundingMode roundingMode;

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value != null) {
            BigDecimal scaled = value.setScale(scale, roundingMode);
            gen.writeNumber(scaled);
        } else {
            gen.writeNull();
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        BigDecimalFormat annotation = property.getAnnotation(BigDecimalFormat.class);
        if (annotation != null) {
            this.scale = annotation.scale();
            this.roundingMode = annotation.roundingMode();
        }
        return this;
    }
}
