package com.eximport.shared.exporting.excel.handler;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.Sheet;

public class SheetProtectHandler implements SheetWriteHandler {
    private final String password;

    public SheetProtectHandler(String password) {
        this.password = password;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        // 开启工作表保护，密码传null就是无密码保护
        sheet.protectSheet(password);
    }
}
