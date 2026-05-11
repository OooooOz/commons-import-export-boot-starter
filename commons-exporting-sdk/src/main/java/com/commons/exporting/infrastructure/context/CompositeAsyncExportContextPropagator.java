package com.commons.exporting.infrastructure.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 组合型异步导出上下文透传器。
 * <p>
 * 将多个上下文贡献器组合在一起统一捕获，并按注册顺序恢复、按逆序清理。
 */
public class CompositeAsyncExportContextPropagator implements AsyncExportContextPropagator {
    private final List<AsyncExportContextContributor> contributors;

    public CompositeAsyncExportContextPropagator(List<AsyncExportContextContributor> contributors) {
        this.contributors = contributors == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(contributors));
    }

    @Override
    public AsyncExportContextSnapshot capture() {
        if (contributors.isEmpty()) {
            return AsyncExportContextSnapshot.noop();
        }
        List<AsyncExportContextSnapshot> snapshots = new ArrayList<>(contributors.size());
        for (AsyncExportContextContributor contributor : contributors) {
            AsyncExportContextSnapshot snapshot = contributor == null ? AsyncExportContextSnapshot.noop() : contributor.capture();
            snapshots.add(snapshot == null ? AsyncExportContextSnapshot.noop() : snapshot);
        }
        return new CompositeAsyncExportContextSnapshot(snapshots);
    }

    private static class CompositeAsyncExportContextSnapshot implements AsyncExportContextSnapshot {
        private final List<AsyncExportContextSnapshot> snapshots;

        private CompositeAsyncExportContextSnapshot(List<AsyncExportContextSnapshot> snapshots) {
            this.snapshots = snapshots;
        }

        @Override
        public void restore() {
            for (AsyncExportContextSnapshot snapshot : snapshots) {
                snapshot.restore();
            }
        }

        @Override
        public void clear() {
            for (int i = snapshots.size() - 1; i >= 0; i--) {
                snapshots.get(i).clear();
            }
        }
    }
}

