package com.eximport.shared.exporting.excel;

import com.eximport.shared.exporting.excel.handler.ExcelPageExportHandler;
import com.eximport.shared.exporting.support.PagedExcelWriteSupport;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PagedExcelWriteSupportTest {
    private final PagedExcelWriteSupport support = new PagedExcelWriteSupport();

    @Test
    public void shouldWriteExcelUsingSharedHandler() throws Exception {
        File tempFile = File.createTempFile("shared-export-", ".xlsx");
        try {
            support.writeExcel(tempFile, "request", new ExcelPageExportHandler<String, ExportRow>() {
                @Override
                public Class<ExportRow> headClass() {
                    return ExportRow.class;
                }

                @Override
                public String sheetName(String request) {
                    return "测试数据";
                }

                @Override
                public List<ExportRow> queryPage(String request, long pageNo, int pageSize) {
                    if (pageNo == 1L) {
                        return Arrays.asList(new ExportRow(1L, "A"), new ExportRow(2L, "B"));
                    }
                    if (pageNo == 2L) {
                        return Collections.singletonList(new ExportRow(3L, "C"));
                    }
                    return Collections.emptyList();
                }
            }, 2, 1048575L, 10L);

            Assert.assertTrue(tempFile.exists());
            Assert.assertTrue(tempFile.length() > 0L);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test
    public void shouldRejectPageDataLargerThanPageSize() throws Exception {
        File tempFile = File.createTempFile("shared-export-overflow-", ".xlsx");
        try {
            try {
                support.writeExcel(tempFile, "request", new ExcelPageExportHandler<String, ExportRow>() {
                    @Override
                    public Class<ExportRow> headClass() {
                        return ExportRow.class;
                    }

                    @Override
                    public List<ExportRow> queryPage(String request, long pageNo, int pageSize) {
                        return Arrays.asList(new ExportRow(1L, "A"), new ExportRow(2L, "B"), new ExportRow(3L, "C"));
                    }
                }, 2, 1048575L, 10L);
                Assert.fail("Expected IllegalStateException");
            } catch (IllegalStateException ex) {
                Assert.assertTrue(ex.getMessage().contains("pageSize"));
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test
    public void shouldUseHandlerPageSizeOverConfigured() throws Exception {
        File tempFile = File.createTempFile("shared-export-handler-pagesize-", ".xlsx");
        try {
            List<Integer> receivedPageSizes = new ArrayList<>();
            List<ExportRow> pageOne = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                pageOne.add(new ExportRow((long) (i + 1), "row" + i));
            }
            support.writeExcel(tempFile, "request", new ExcelPageExportHandler<String, ExportRow>() {
                @Override
                public Class<ExportRow> headClass() {
                    return ExportRow.class;
                }

                @Override
                public List<ExportRow> queryPage(String request, long pageNo, int pageSize) {
                    receivedPageSizes.add(pageSize);
                    if (pageNo == 1L) {
                        return pageOne;
                    }
                    return Collections.emptyList();
                }

                @Override
                public int pageSize() {
                    // 模拟业务内部固定分页大小，覆盖调用方传入的配置值
                    return 3;
                }
            }, 5000, 1048575L, 10L);
            Assert.assertTrue(tempFile.length() > 0L);
            // 第一页满页（3=pageSize）继续查第二页，第二页为空结束，共两次均收到 handler 声明的 pageSize
            Assert.assertEquals(Arrays.asList(3, 3), receivedPageSizes);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test
    public void shouldFallbackToConfiguredPageSizeWhenHandlerNotDeclared() throws Exception {
        File tempFile = File.createTempFile("shared-export-config-pagesize-", ".xlsx");
        try {
            List<Integer> receivedPageSizes = new ArrayList<>();
            support.writeExcel(tempFile, "request", new ExcelPageExportHandler<String, ExportRow>() {
                @Override
                public Class<ExportRow> headClass() {
                    return ExportRow.class;
                }

                @Override
                public List<ExportRow> queryPage(String request, long pageNo, int pageSize) {
                    receivedPageSizes.add(pageSize);
                    if (pageNo == 1L) {
                        return Arrays.asList(new ExportRow(1L, "A"), new ExportRow(2L, "B"));
                    }
                    return Collections.emptyList();
                }
            }, 2, 1048575L, 10L);
            Assert.assertTrue(tempFile.length() > 0L);
            // 第一页满页（2=pageSize）继续查第二页，第二页为空结束，共两次均收到配置 pageSize
            Assert.assertEquals(Arrays.asList(2, 2), receivedPageSizes);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public static class ExportRow {
        private Long id;
        private String name;

        public ExportRow() {
        }

        public ExportRow(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

