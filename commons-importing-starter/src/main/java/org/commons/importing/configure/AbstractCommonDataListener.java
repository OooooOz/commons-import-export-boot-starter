package org.commons.importing.configure;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.commons.importing.model.ImportResultVO;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.util.ListUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * 通用导入监听器基类。
 * <p>
 * 提供批量缓存、行级异常处理、批量校验与导入结果汇总等通用能力。
 * 业务系统只需继承该类并实现 {@link #saveData()}，必要时覆写校验钩子即可。
 *
 * @param <T> 导入实体类型
 */
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
    private List<T> list = new ArrayList<>();
    /**
     * 结果集
     */
    private final ImportResultVO importResultVO = new ImportResultVO();

    /**
     * 获取当前监听器累计的导入结果。
     *
     * @return 导入结果
     */
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
     * 判断是否跳过当前 sheet。
     *
     * @param sheetName 当前读取到的 sheet 名称
     * @return {@code true} 表示跳过当前 sheet，{@code false} 表示继续处理
     */
    protected boolean needSkip(String sheetName) {
        return false;
    }

    /**
     * 执行当前缓存批次的数据保存逻辑。
     */
    protected abstract void saveData();

    /**
     * 逐行校验数据。
     *
     * @param data 当前读取到的一行数据
     */
    protected void singleCheckData(T data) {}

    /**
     * 批量校验当前缓存的数据。
     * <p>
     * 若已使用逐行校验，建议谨慎叠加使用该方法，以免成功/失败统计语义混淆。
     *
     * @param list 当前待入库的数据批次
     * @return 当前批次校验结果
     */
    protected ImportResultVO batchCheckData(List<T> list) {
        return new ImportResultVO();
    }

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
        importResultVO.merge(temp);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (maxRows != null && maxRows > list.size()) {
            importResultVO.addGlobalMsg(String.format("导入数据超过%s条", maxRows));
            return;
        }
        this.doBatchSave();
        importResultVO.addSuccessCount(list.size());
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        Integer rowIndex = context.readRowHolder().getRowIndex();
        log.error("[AbstractCommonDataListener#onException]第 {} 行 解析失败 {}", ++rowIndex, stackTraceToString(exception));

        StringBuilder builder = new StringBuilder().append("第").append(rowIndex).append("行解析失败！");
        if (exception instanceof ExcelDataConvertException) {
            builder.append("错误原因：列【").append(this.headMap.get(((ExcelDataConvertException)exception).getColumnIndex())).append("】格式有误");
        } else {
            builder.append("错误原因：").append(exception.getMessage());
        }
        importResultVO.addFailure(builder.toString());
    }

    /**
     * 设置最大导入行数限制。
     *
     * @param maxRows 最大行数限制
     */
    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }

    private String stackTraceToString(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            exception.printStackTrace(printWriter);
            printWriter.flush();
            return stringWriter.toString();
        }
    }
}
