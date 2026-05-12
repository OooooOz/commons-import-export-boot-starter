package org.commons.infrastructure.util;

import com.eximport.export.shared.support.KeyedLockSupport;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 导出请求判重锁管理器。
 * <p>
 * 在单个 core 服务实例内，基于请求指纹串行化同一请求的创建过程，避免并发重复创建任务。
 */
@Component
public class ExportTaskRequestDedupLockManager {
    private final KeyedLockSupport keyedLockSupport = new KeyedLockSupport();

    public <T> T execute(String fingerprint, Supplier<T> supplier) {
        return keyedLockSupport.execute(fingerprint, supplier);
    }
}

