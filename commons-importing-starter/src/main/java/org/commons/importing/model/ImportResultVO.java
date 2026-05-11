package org.commons.importing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;

/**
 * 导入结果汇总对象。
 * <p>
 * 用于记录导入过程中的成功条数、失败条数以及全局或逐行提示信息。
 */
@Data
public class ImportResultVO {

    private final AtomicInteger failure = new AtomicInteger();

    private final AtomicInteger success = new AtomicInteger();

    private final List<String> msgList = new ArrayList<>();

    /**
     * 追加全局提示信息。
     *
     * @param msg 提示信息
     */
    public void addGlobalMsg(String msg) {
        if (msg != null && !msg.isEmpty()) {
            msgList.add(msg);
        }
    }

    /**
     * 记录一条失败结果。
     *
     * @param msg 失败原因
     */
    public void addFailure(String msg) {
        addFailureCount(1);
        if (msg != null && !msg.isEmpty()) {
            this.msgList.add(msg);
        }
    }

    /**
     * 记录一条成功结果并可附带提示信息。
     *
     * @param msg 成功提示信息
     */
    public void addSuccess(String msg) {
        addSuccessCount(1);
        if (msg != null && !msg.isEmpty()) {
            this.msgList.add(msg);
        }
    }

    /**
     * 增加失败计数。
     *
     * @param count 递增数量
     */
    public void addFailureCount(int count) {
        if (count > 0) {
            this.failure.addAndGet(count);
        }
    }

    /**
     * 增加成功计数。
     *
     * @param count 递增数量
     */
    public void addSuccessCount(int count) {
        if (count > 0) {
            this.success.addAndGet(count);
        }
    }

    /**
     * 批量追加提示信息。
     *
     * @param messages 提示信息列表
     */
    public void addMessages(List<String> messages) {
        if (messages != null && !messages.isEmpty()) {
            this.msgList.addAll(messages);
        }
    }

    /**
     * 合并另一份导入结果。
     *
     * @param other 另一份导入结果
     */
    public void merge(ImportResultVO other) {
        if (other == null) {
            return;
        }
        addFailureCount(other.getFailure().get());
        addSuccessCount(other.getSuccess().get());
        addMessages(other.getMsgList());
    }
}
