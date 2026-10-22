package com.eximport.shared.exporting.excel.handler;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.eximport.shared.exporting.model.CascadeAnnotateMeta;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnotateCascadeHandler implements SheetWriteHandler {

    private final List<CascadeAnnotateMeta> metaList;
    /**
     * key:dictKey -> 一级选项列表
     */
    private final Map<String, List<String>> parentDictMap;
    /**
     * key:dictKey -> key父名称，value子列表
     */
    private final Map<String, Map<String, List<String>>> cascadeDictMap;

    /**
     * 不创建隐藏sheet，最大下拉数据
     */
    private final int maxRow;

    public AnnotateCascadeHandler(List<CascadeAnnotateMeta> metaList,
                                  Map<String, List<String>> parentDictMap,
                                  Map<String, Map<String, List<String>>> cascadeDictMap,
                                  int maxRow) {
        this.metaList = metaList;
        this.parentDictMap = parentDictMap;
        this.cascadeDictMap = cascadeDictMap;
        this.maxRow = maxRow;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (CollectionUtils.isEmpty(metaList)) {
            return;
        }
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        Sheet mainSheet = writeSheetHolder.getSheet();

        // =========0. 解析真实列下标【关键修复：@ExcelProperty未显式指定index时，注解解析出的columnIndex为-1】=========
        Map<String, Integer> fieldColIndexMap = new HashMap<>();
        writeSheetHolder.getExcelWriteHeadProperty().getHeadMap().forEach((colIndex, head) -> fieldColIndexMap.put(head.getFieldName(), colIndex));

        List<CascadeAnnotateMeta> resolvedMetaList = new ArrayList<>(metaList.size());
        for (CascadeAnnotateMeta meta : metaList) {
            Integer realColIndex = StringUtils.isBlank(meta.getFieldName()) ? null : fieldColIndexMap.get(meta.getFieldName());
            int colIndex = realColIndex != null ? realColIndex : meta.getColumnIndex();
            if (colIndex < 0) {
                continue;
            }
            meta.setColumnIndex(colIndex);
            resolvedMetaList.add(meta);
        }
        if (CollectionUtils.isEmpty(resolvedMetaList)) {
            return;
        }

        Sheet dictSheet = workbook.createSheet("_Dict");
        workbook.setSheetHidden(workbook.getSheetIndex(dictSheet), true);
        DataValidationHelper dvHelper = mainSheet.getDataValidationHelper();

        // =========1. 按dictKey分组元数据【关键修复】=========
        Map<String, List<CascadeAnnotateMeta>> groupByDictKey = resolvedMetaList.stream().collect(Collectors.groupingBy(CascadeAnnotateMeta::getDictKey));

        // 每个dictKey只执行一次构建字典与名称管理器
        for (Map.Entry<String, List<CascadeAnnotateMeta>> entry : groupByDictKey.entrySet()) {
            String dictKey = entry.getKey();
            List<CascadeAnnotateMeta> groupMetaList = entry.getValue();

            Map<String, List<String>> parentChildMap = cascadeDictMap.get(dictKey);
            List<String> parentList = parentDictMap.get(dictKey);
            if (parentChildMap == null || parentList == null) {
                continue;
            }

            // ----只执行一次：写入隐藏dictSheet + 创建Name管理器，不会重复defineName----
            int dictCol = 0;
            for (Map.Entry<String, List<String>> item : parentChildMap.entrySet()) {
                String defineName = item.getKey().replace(" ", "_");
                List<String> children = item.getValue();
                for (int i = 0; i < children.size(); i++) {
                    Row row = dictSheet.getRow(i);
                    if (row == null) {
                        row = dictSheet.createRow(i);
                    }
                    row.createCell(dictCol).setCellValue(children.get(i));
                }
                Name name = workbook.createName();
                name.setNameName(defineName);
                CellRangeAddress range = new CellRangeAddress(0, children.size() - 1, dictCol, dictCol);
                name.setRefersToFormula(range.formatAsString(dictSheet.getSheetName(), true));
                dictCol++;
            }

            // ----同一dictKey下，遍历本组所有meta：给各个列设置下拉校验（父、子、三级）----
            // 校验区域首行为第2行（第1行是表头），公式用相对引用，逐行自动偏移到当前行的父级单元格
            int firstDataRow = 1;
            for (CascadeAnnotateMeta meta : groupMetaList) {
                int colIndex = meta.getColumnIndex();
                CellRangeAddressList rangeList = new CellRangeAddressList(firstDataRow, maxRow, colIndex, colIndex);
                DataValidationConstraint constraint;
                if (meta.isParent()) {
                    //父级普通下拉
                    constraint = dvHelper.createExplicitListConstraint(parentList.toArray(new String[0]));
                } else {
                    //子级联动：取dependOnCol，锚定校验区域首行的父级单元格（相对引用随行偏移，保证每行独立联动）
                    String refCell = getColLetter(meta.getDependCol()) + (firstDataRow + 1);
                    String formula = "INDIRECT(SUBSTITUTE(" + refCell + ",\" \",\"_\"))";
                    constraint = dvHelper.createFormulaListConstraint(formula);
                }
                DataValidation validation = dvHelper.createValidation(constraint, rangeList);
                validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
                // 处理Excel兼容性问题
                if (validation instanceof XSSFDataValidation) {
                    validation.setSuppressDropDownArrow(true);
                    validation.setShowErrorBox(true);
                } else {
                    validation.setSuppressDropDownArrow(false);
                }
                mainSheet.addValidationData(validation);
            }
        }
    }

    private String getColLetter(int index) {
        return org.apache.poi.ss.util.CellReference.convertNumToColString(index);
    }

}
