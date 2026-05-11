package com.commons.exporting.infrastructure.context;

/**
 * 异步导出上下文快照。
 * <p>
 * 用于承载在任务提交线程中捕获的上下文，并在异步线程中执行恢复与清理。
 */
public interface AsyncExportContextSnapshot {
    /**
     * 恢复当前快照对应的上下文到异步执行线程。
     */
    void restore();

    /**
     * 清理当前快照恢复到异步线程中的上下文，避免线程复用导致污染。
     */
    void clear();

    /**
     * 返回一个空操作快照。
     *
     * @return 无需恢复和清理的上下文快照
     */
    static AsyncExportContextSnapshot noop() {
        return new AsyncExportContextSnapshot() {
            @Override
            public void restore() {
                // no-op
            }

            @Override
            public void clear() {
                // no-op
            }
        };
    }
}

