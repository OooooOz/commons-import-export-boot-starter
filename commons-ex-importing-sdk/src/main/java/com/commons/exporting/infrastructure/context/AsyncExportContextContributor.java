package com.commons.exporting.infrastructure.context;

/**
 * 异步导出上下文贡献器。
 * <p>
 * 业务系统可通过实现该接口，将租户、用户、数据权限、日志链路等基于 ThreadLocal 的上下文
 * 在导出任务提交时进行捕获，并在异步线程执行分页查询前恢复。
 */
public interface AsyncExportContextContributor {
    /**
     * 在任务提交线程中捕获当前上下文。
     *
     * @return 上下文快照；若当前贡献器无需处理上下文，可返回 {@link AsyncExportContextSnapshot#noop()}
     */
    AsyncExportContextSnapshot capture();
}

