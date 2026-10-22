package com.commons.importing.configure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.util.ListUtils;
import com.commons.importing.model.ImportResultVO;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCommonDataListener<T> extends AnalysisEventListener<T> {
    /**
     * 表头索引映射
     */
    protected Map<Integer, String> headMap;
    /**
     * 当前 Sheet 是否跳过（每个 Sheet 在表头解析完时触发判定一次，避免 invoke 逐行重复判断与重复日志）
     */
    private boolean currentSheetSkipped;
    /**
     * 批量保存数据量
     */
    @Getter
    @Setter
    private int batchSaveCount = 3000;
    /**
     * 是否批量保存
     */
    @Setter
    private boolean batchSave = true;
    /**
     * 最大导入行数
     */
    @Getter
    private Integer maxRows;
    /**
     * 数据集
     */
    @Setter
    private List<T> list = Lists.newArrayList();
    /**
     * 结果集
     */
    @Getter
    private final ImportResultVO importResultVO = new ImportResultVO();

    /**
     * 使用默认的结果集对象
     */
    @Setter
    private boolean defaultImportResultVO = true;

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
        // 每个 Sheet 表头解析完回调一次：此处判定是否跳过当前 Sheet，
        // invoke 中只读状态不再逐行重复判断，也不会重复输出跳过日志
        String sheetName = context.readSheetHolder().getSheetName();
        this.currentSheetSkipped = this.needSkip(sheetName);
        if (this.currentSheetSkipped) {
            log.info("[AbstractCommonDataListener#invokeHeadMap]不处理Sheet:{}", sheetName);
        }
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        if (this.currentSheetSkipped) {
            // 当前 Sheet 已判定跳过，静默丢弃补数据行，不重复打印
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
        if (!defaultImportResultVO) {
            importResultVO.getFailure().addAndGet(temp.getFailure().get());
            if (CollectionUtils.isNotEmpty(temp.getMsgList())) {
                importResultVO.getMsgList().addAll(temp.getMsgList());
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 空 Sheet 不会触发 invokeHeadMap，此处现场判定一次，避免上个 Sheet 的跳过状态泄漏到空 Sheet
        if (this.needSkip(context.readSheetHolder().getSheetName())) {
            return;
        }
        if (maxRows != null && maxRows < list.size()) {
            importResultVO.addGlobalMsg(String.format("导入数据超过%s条", maxRows));
            return;
        }
        this.doBatchSave();
        importResultVO.addSuccessCount(list.size());
        list = ListUtils.newArrayListWithExpectedSize(batchSaveCount);
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
