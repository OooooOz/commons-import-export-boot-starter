package org.commons.importing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

@Data
public class ImportResultVO {

    private AtomicInteger failure = new AtomicInteger();

    private AtomicInteger success = new AtomicInteger();

    private List<String> msgList = new ArrayList<>();

    public void addGlobalMsg(String msg) {
        msgList.add(msg);
    }

    public void addFailure(String msg) {
        this.failure.incrementAndGet();
        if (ObjectUtil.isNotEmpty(msg)) {
            this.msgList.add(msg);
        }
    }

    public void addSuccess(String msg) {
        this.success.incrementAndGet();
        if (ObjectUtil.isNotEmpty(msg)) {
            this.msgList.add(msg);
        }
    }
}
