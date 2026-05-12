package com.eximport.export.shared.excel;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
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

