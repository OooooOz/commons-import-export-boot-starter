package com.eximport.export.shared.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 基于字符串键的进程内串行锁支持类。
 */
public class KeyedLockSupport {
    private final ConcurrentMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public <T> T execute(String key, Supplier<T> supplier) {
        String actualKey = key == null ? "" : key;
        ReentrantLock lock = lockMap.computeIfAbsent(actualKey, value -> new ReentrantLock());
        lock.lock();
        try {
            return supplier.get();
        } finally {
            try {
                lock.unlock();
            } finally {
                if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                    lockMap.remove(actualKey, lock);
                }
            }
        }
    }
}

