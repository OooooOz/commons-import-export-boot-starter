package com.eximport.export.shared.support;

import com.eximport.export.shared.model.ExportTaskFields;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ExportSupportTest {
    @Test
    public void shouldBuildHandlerMapAndKey() {
        Map<String, DemoHandler> mappings = ExportHandlerRegistrySupport.buildHandlerMap(Arrays.asList(
                new DemoHandler("sysA", "typeA"),
                new DemoHandler("sysB", "typeB")));

        Assert.assertEquals(2, mappings.size());
        Assert.assertTrue(mappings.containsKey("sysA_typeA"));
        Assert.assertEquals("sysB_typeB", ExportHandlerRegistrySupport.buildHandlerKey(" sysB ", " typeB "));
    }

    @Test
    public void shouldRejectDuplicateHandlerRegistration() {
        try {
            ExportHandlerRegistrySupport.buildHandlerMap(Arrays.asList(
                    new DemoHandler("sysA", "typeA"),
                    new DemoHandler("sysA", "typeA")));
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("导出处理器重复注册"));
        }
    }

    @Test
    public void shouldNormalizeFileNameAndLimitMessage() {
        String normalized = ExportExecutionSupport.normalizeFileName("ab:c?.xls");
        Assert.assertEquals("ab_c_.xls.xlsx", normalized);
        Assert.assertEquals("导出失败", ExportExecutionSupport.limitMessage(null));
        Assert.assertEquals("12345", ExportExecutionSupport.limitMessage("123456", "失败", 5));
    }

    @Test
    public void shouldResolveRejectedExecutionHandler() {
        RejectedExecutionHandler abort = ExportExecutionSupport.resolveRejectedExecutionHandler(null);
        RejectedExecutionHandler callerRuns = ExportExecutionSupport.resolveRejectedExecutionHandler(" caller-runs ");

        Assert.assertTrue(abort instanceof ThreadPoolExecutor.AbortPolicy);
        Assert.assertTrue(callerRuns instanceof ThreadPoolExecutor.CallerRunsPolicy);
        Assert.assertEquals("abort", ExportExecutionSupport.normalizeRejectionPolicy("  "));
    }

    @Test
    public void shouldBuildStorageObjectName() {
        String objectName = ExportStorageSupport.buildObjectName("/exports/", "TASK001", "demo.xlsx", new Date(0L));

        Assert.assertEquals("exports/19700101/TASK001-demo.xlsx", objectName);
        Assert.assertEquals("abc", ExportStorageSupport.trimSlash("/abc/"));
    }

    @Test
    public void shouldBuildStableFingerprintFromTaskFields() {
        ExportTaskFields first = new ExportTaskFields();
        first.setBusinessSystem(" sys ");
        first.setBusinessType(" type ");
        first.setTaskName(" demo ");
        first.setFileName(" file.xlsx ");
        first.setCreator(" user ");
        Map<String, Object> firstExt = new LinkedHashMap<>();
        firstExt.put("b", 2);
        firstExt.put("a", Arrays.asList("x", "y"));
        first.setExtMap(firstExt);

        ExportTaskFields second = new ExportTaskFields();
        second.setBusinessSystem("sys");
        second.setBusinessType("type");
        second.setTaskName("demo");
        second.setFileName("file.xlsx");
        second.setCreator("user");
        Map<String, Object> secondExt = new LinkedHashMap<>();
        secondExt.put("a", Arrays.asList("x", "y"));
        secondExt.put("b", 2);
        second.setExtMap(secondExt);

        Assert.assertEquals(
                ExportTaskFingerprintSupport.build(first),
                ExportTaskFingerprintSupport.build(second));
    }

    @Test
    public void shouldExecuteWithKeyedLockSupport() {
        KeyedLockSupport keyedLockSupport = new KeyedLockSupport();

        String result = keyedLockSupport.execute("fingerprint", () -> "ok");
        Integer nullKeyResult = keyedLockSupport.execute(null, () -> 1);

        Assert.assertEquals("ok", result);
        Assert.assertEquals(Integer.valueOf(1), nullKeyResult);
    }

    @Test
    public void shouldDeepCopyNestedPayload() {
        Date now = new Date();
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("numbers", new ArrayList<>(Arrays.asList(1, 2, 3)));
        nested.put("when", now);
        nested.put("array", new String[]{"A", "B"});
        nested.put("set", new java.util.LinkedHashSet<>(Arrays.asList("X", "Y")));

        LinkedHashMap<String, Object> copy = ExportPayloadCopySupport.copyMap(new LinkedHashMap<String, Object>() {{
            put("nested", nested);
        }});

        Map<String, Object> nestedCopy = castMap(copy.get("nested"));
        Assert.assertNotSame(nested, nestedCopy);
        Assert.assertNotSame(now, nestedCopy.get("when"));
        Assert.assertArrayEquals(new String[]{"A", "B"}, (String[]) nestedCopy.get("array"));

        addInteger(nestedCopy.get("numbers"));
        Assert.assertEquals(3, ((java.util.List<?>) nested.get("numbers")).size());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private void addInteger(Object value) {
        ((java.util.List<Object>) value).add(4);
    }

    private static class DemoHandler implements ExportHandlerDescriptor {
        private final String businessSystem;
        private final String businessType;

        private DemoHandler(String businessSystem, String businessType) {
            this.businessSystem = businessSystem;
            this.businessType = businessType;
        }

        @Override
        public String businessSystem() {
            return businessSystem;
        }

        @Override
        public String businessType() {
            return businessType;
        }
    }
}

