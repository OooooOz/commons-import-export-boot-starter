package org.commons.infrastructure.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 导出请求判重锁管理器。
 * <p>
 * 在单个 core 服务实例内，基于请求指纹串行化同一请求的创建过程，避免并发重复创建任务。
 */
@Component
public class ExportTaskRequestDedupLockManager {
    private final ConcurrentMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<String, ReentrantLock>();

    public <T> T execute(String fingerprint, Supplier<T> supplier) {
        String key = fingerprint == null ? "" : fingerprint;
        ReentrantLock lock = lockMap.computeIfAbsent(key, value -> new ReentrantLock());
        lock.lock();
        try {
            return supplier.get();
        } finally {
            try {
                lock.unlock();
            } finally {
                if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                    lockMap.remove(key, lock);
                }
            }
        }
    }
}

