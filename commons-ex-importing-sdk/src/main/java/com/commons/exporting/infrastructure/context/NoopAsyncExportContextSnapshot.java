package com.commons.exporting.infrastructure.context;

/**
 * 空操作上下文快照。
 */
final class NoopAsyncExportContextSnapshot implements AsyncExportContextSnapshot {
    static final NoopAsyncExportContextSnapshot INSTANCE = new NoopAsyncExportContextSnapshot();

    private NoopAsyncExportContextSnapshot() {
    }

    @Override
    public void restore() {
        // no-op
    }

    @Override
    public void clear() {
        // no-op
    }
}

