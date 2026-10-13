package com.commons.exporting.infrastructure.context;

/**
 * 异步导出上下文透传器。
 * <p>
 * 负责在任务提交线程中统一捕获上下文，并在异步线程执行时恢复与清理。
 */
public interface AsyncExportContextPropagator {
    /**
     * 捕获当前线程中的异步导出上下文。
     *
     * @return 上下文快照
     */
    AsyncExportContextSnapshot capture();
}

