package com.eximport.shared.exporting.excel.handler;

import com.eximport.shared.exporting.excel.annotation.BigDecimalFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalDeserializer extends JsonDeserializer<BigDecimal> implements ContextualDeserializer {
    private int scale;
    private RoundingMode roundingMode;

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        BigDecimal value = p.getDecimalValue();
        return value != null ? value.setScale(scale, roundingMode) : null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) {
        BigDecimalFormat annotation = property.getAnnotation(BigDecimalFormat.class);
        if (annotation != null) {
            this.scale = annotation.scale();
            this.roundingMode = annotation.roundingMode();
        }
        return this;
    }
}
