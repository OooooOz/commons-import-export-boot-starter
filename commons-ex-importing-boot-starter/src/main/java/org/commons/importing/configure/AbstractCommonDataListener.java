package org.commons.importing.configure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.commons.importing.model.ImportResultVO;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.util.ListUtils;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCommonDataListener<T> extends AnalysisEventListener<T> {
    /**
     * 表头索引映射
     */
    protected Map<Integer, String> headMap;
    /**
     * 批量保存数据量
     */
    private int batchSaveCount = 3000;
    /**
     * 是否批量保存
     */
    private boolean batchSave = true;
    /**
     * 最大导入行数
     */
    private Integer maxRows;
    /**
     * 数据集
     */
    private List<T> list = Lists.newArrayList();
    /**
     * 结果集
     */
    private ImportResultVO importResultVO = new ImportResultVO();

    public ImportResultVO getImportResultVO() {
        return importResultVO;
    }

    /**
     * 设置是否批量保存
     *
     * @param batchSave true-批量，false-全量
     */
    public void setBatchSave(boolean batchSave) {
        this.batchSave = batchSave;
    }

    public int getBatchSaveCount() {
        return batchSaveCount;
    }

    public void setBatchSaveCount(int batchSaveCount) {
        this.batchSaveCount = batchSaveCount;
    }

    /**
     * 是否跳过当前Sheet
     *
     * @param sheetName
     * @return
     */
    protected boolean needSkip(String sheetName) {
        return false;
    }

    /**
     * 保存数据
     */
    protected abstract void saveData();

    /**
     * 逐行校验数据
     *
     * @param data
     */
    protected void singleCheckData(T data) {};

    /**
     * 批量校验数据,与逐行校验数据最好不要一起用，会统计有问题
     *
     * @param list
     */
    protected ImportResultVO batchCheckData(List<T> list) {
        return new ImportResultVO();
    };

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = new HashMap<>(headMap);
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        String sheetName = context.readSheetHolder().getSheetName();
        if (this.needSkip(sheetName)) {
            log.info("[AbstractCommonDataListener#invoke]不处理Sheet:{}", sheetName);
            return;
        }

        // 逐行校验数据
        this.singleCheckData(data);
        list.add(data);
        if (!batchSave) {
            // 不分批保存，等最后一条数据解析后再统一保存
            return;
        }

        if (list.size() >= this.getBatchSaveCount()) {
            this.doBatchSave();
            // 存储完成清理 list
            list = ListUtils.newArrayListWithExpectedSize(batchSaveCount);
        }
    }

    private void doBatchSave() {
        ImportResultVO temp = this.batchCheckData(list);
        this.saveData();
        importResultVO.getFailure().addAndGet(temp.getFailure().get());
        if (CollectionUtils.isNotEmpty(temp.getMsgList())) {
            importResultVO.getMsgList().addAll(temp.getMsgList());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (maxRows != null && maxRows > list.size()) {
            importResultVO.addGlobalMsg(String.format("导入数据超过%s条", maxRows));
            return;
        }
        this.doBatchSave();
        importResultVO.getSuccess().addAndGet(list.size());
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        Integer rowIndex = context.readRowHolder().getRowIndex();
        log.error("[AbstractCommonDataListener#onException]第 {} 行 解析失败 {}", ++rowIndex, ExceptionUtil.stacktraceToString(exception));

        StringBuilder builder = new StringBuilder().append("第").append(rowIndex).append("行解析失败！");
        if (exception instanceof ExcelDataConvertException) {
            builder.append("错误原因：列【").append(this.headMap.get(((ExcelDataConvertException)exception).getColumnIndex())).append("】格式有误");
        } else {
            builder.append("错误原因：").append(exception.getMessage());
        }
        importResultVO.addFailure(builder.toString());
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }
}
