package com.eximport.shared.exporting.excel.handler;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.Map;

public class TextStyleWriteHandler implements SheetWriteHandler {

    /**
     * 未解析到表头时的默认覆盖列数（与销售价格变更模板列数一致）
     */
    private static final int DEFAULT_COLUMN_COUNT = 16;

    @Override
    public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        //
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        // 获取到当前的sheet
        Sheet sheet = writeSheetHolder.getSheet();
        // 按表头实际列数设置（@ExcelIgnore 字段不占列），未解析到表头时退回默认覆盖前16列
        Map<Integer, ?> headMap = writeSheetHolder.excelWriteHeadProperty().getHeadMap();
        int columnCount = MapUtils.isEmpty(headMap) ? DEFAULT_COLUMN_COUNT : headMap.size();
        CellStyle cellStyle = writeWorkbookHolder.getCachedWorkbook().createCellStyle();
        // 49为文本格式
        cellStyle.setDataFormat((short) 49);
        for (int i = 0; i < columnCount; i++) {
            // i为列，一整列设置为文本格式
            sheet.setDefaultColumnStyle(i, cellStyle);
        }
    }

}
