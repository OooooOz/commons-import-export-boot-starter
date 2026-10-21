package com.commons.exporting.infrastructure.handle;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Row;

/**
 * 流式导出分组合并单元格处理器（附加表头行高）：在{@link GroupMergeWriteHandler}合并能力基础上，
 * 将指定相对索引的表头行固定为指定行高（磅）；其余表头行不设固定行高，开启换行的超长表头仍可随文本自动扩展行高。
 */
public class GroupMergeHeadRowHeightWriteHandler extends GroupMergeWriteHandler implements RowWriteHandler {

    /**
     * 需要固定行高的表头行相对索引（0开始，第0行为一级分组表头）
     */
    private final int headRowRelativeIndex;

    /**
     * 表头行固定行高（磅）
     */
    private final float headRowHeight;

    public GroupMergeHeadRowHeightWriteHandler(int groupColumn, int mergeEndColumn, int headRowRelativeIndex, float headRowHeight) {
        super(groupColumn, mergeEndColumn);
        this.headRowRelativeIndex = headRowRelativeIndex;
        this.headRowHeight = headRowHeight;
    }

    @Override
    public void afterRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                               Integer relativeRowIndex, Boolean isHead) {
        if (Boolean.TRUE.equals(isHead) && relativeRowIndex != null && relativeRowIndex == headRowRelativeIndex) {
            row.setHeightInPoints(headRowHeight);
        }
    }
}
