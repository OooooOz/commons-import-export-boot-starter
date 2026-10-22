package com.eximport.shared.exporting.excel.handler;

import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.util.List;

/**
 * 流式导出分组合并单元格处理器：以 {@code groupColumn} 列为分组依据，
 * 将连续且分组值相同的多行，在 0~{@code mergeEndColumn} 列纵向合并；分组值为空的行不与任何行合并。
 *
 * <p>用于逐批写出的流式导出场景：按sheet中的绝对行号跟踪当前分组，
 * 组内每新增一行即把合并区域顺延到当前行，跨批次被拆分的分组以及数据末尾的最后一个分组也能正确合并，
 * 不依赖数据一次性加载完成。</p>
 */
public class GroupMergeWriteHandler implements CellWriteHandler {

    /**
     * 分组列索引（0开始）
     */
    private final int groupColumn;

    /**
     * 合并结束列索引（0开始），合并0~mergeEndColumn列；小于0时不合并
     */
    private final int mergeEndColumn;

    /**
     * 当前分组起始数据行号（表头占第0行，数据从第1行开始）
     */
    private int groupStartRow = -1;

    /**
     * 当前分组值（统一转字符串便于比较）
     */
    private String currentGroupValue;

    public GroupMergeWriteHandler(int groupColumn, int mergeEndColumn) {
        this.groupColumn = groupColumn;
        this.mergeEndColumn = mergeEndColumn;
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                 Integer relativeRowIndex, Boolean isHead) {
        if (Boolean.TRUE.equals(isHead) || cell == null || mergeEndColumn < 0 || cell.getColumnIndex() != groupColumn) {
            return;
        }
        int rowIndex = cell.getRowIndex();
        String groupValue = groupValue(cellDataList);
        if (groupStartRow < 0 || currentGroupValue == null || groupValue == null || !currentGroupValue.equals(groupValue)) {
            groupStartRow = rowIndex;
            currentGroupValue = groupValue;
            return;
        }
        extendMerge(writeSheetHolder.getSheet(), rowIndex);
    }

    private void extendMerge(Sheet sheet, int rowIndex) {
        int mergeColumns = mergeEndColumn + 1;
        int mergedRegionCount = sheet.getNumMergedRegions();
        // 当前分组已建立的合并区域总是sheet末尾连续的mergeColumns个；校验命中后逆序移除旧区域再顺延，避免区域重叠
        boolean active = mergedRegionCount >= mergeColumns
                && sheet.getMergedRegion(mergedRegionCount - mergeColumns).getFirstRow() == groupStartRow
                && sheet.getMergedRegion(mergedRegionCount - 1).getLastRow() == rowIndex - 1;
        if (active) {
            for (int col = mergeEndColumn; col >= 0; col--) {
                sheet.removeMergedRegion(mergedRegionCount - mergeColumns + col);
            }
        }
        for (int col = 0; col <= mergeEndColumn; col++) {
            sheet.addMergedRegion(new CellRangeAddress(groupStartRow, rowIndex, col, col));
        }
    }

    private String groupValue(List<WriteCellData<?>> cellDataList) {
        if (cellDataList == null || cellDataList.isEmpty()) {
            return null;
        }
        WriteCellData<?> cellData = cellDataList.get(0);
        CellDataTypeEnum type = cellData.getType();
        if (type == null) {
            return null;
        }
        switch (type) {
            case STRING:
                return cellData.getStringValue();
            case NUMBER:
                BigDecimal numberValue = cellData.getNumberValue();
                return numberValue == null ? null : numberValue.stripTrailingZeros().toPlainString();
            case BOOLEAN:
                return String.valueOf(cellData.getBooleanValue());
            default:
                return null;
        }
    }
}
