package com.commons.exporting.infrastructure.handle;

import java.util.List;

import org.apache.poi.ss.usermodel.*;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;

import cn.hutool.core.util.ObjectUtil;

public class CommentCellWriteHandler implements CellWriteHandler {

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head,
        Integer relativeRowIndex, Boolean isHead) {

        if (!isHead || cell == null || head == null) {
            return; // 跳过表头或空单元格
        }

        // 获取字段上的 @ExcelComment 注解
        ExcelComment commentAnnotation = head.getField().getAnnotation(ExcelComment.class);
        if (commentAnnotation == null) {
            return;
        }

        // 创建批注
        Sheet sheet = writeSheetHolder.getSheet();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper factory = sheet.getWorkbook().getCreationHelper();

        // 设置批注位置和大小
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + commentAnnotation.columnIndex());
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + commentAnnotation.rowIndex());

        // 添加批注
        Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(commentAnnotation.value()));
        cell.setCellComment(comment);

        // 可选：设置单元格字体颜色
        if (ObjectUtil.equals(Font.COLOR_NORMAL, commentAnnotation.fontColor())) {
            Font redFont = sheet.getWorkbook().createFont();
            redFont.setColor(commentAnnotation.fontColor());
            CellStyle style = sheet.getWorkbook().createCellStyle();
            style.setFont(redFont);
            cell.setCellStyle(style);
        }
    }

}
