package com.eximport.export.shared.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class BaseResponseJacksonTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldDeserializeLegacyIsSuccessField() throws Exception {
        BaseResponse<String> response = objectMapper.readValue(
                "{\"code\":\"0\",\"message\":\"ok\",\"isSuccess\":true,\"data\":\"A\"}",
                objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, String.class));

        Assert.assertTrue(response.isSuccess());
        Assert.assertEquals("A", response.getData());
    }

    @Test
    public void shouldSerializeSuccessFieldName() throws Exception {
        BaseResponse<String> response = new BaseResponse<>();
        response.setCode("0");
        response.setMessage("ok");
        response.setSuccess(true);
        response.setData("A");

        String json = objectMapper.writeValueAsString(response);
        Assert.assertTrue(json.contains("\"success\":true"));
        Assert.assertFalse(json.contains("\"isSuccess\""));
    }
}

