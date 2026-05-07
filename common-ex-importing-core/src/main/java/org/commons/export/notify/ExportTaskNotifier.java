package org.commons.export.notify;

import lombok.extern.slf4j.Slf4j;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 基于 SSE 的导出任务通知。前端也可不用 SSE，直接轮询任务详情接口。
 */
@Slf4j
@Component
public class ExportTaskNotifier {
    private static final long DEFAULT_TIMEOUT = 30L * 60L * 1000L;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String creator) {
        String key = key(creator);
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        CopyOnWriteArrayList<SseEmitter> list = emitters.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);
        emitter.onCompletion(() -> remove(key, emitter));
        emitter.onTimeout(() -> remove(key, emitter));
        emitter.onError(e -> remove(key, emitter));
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            remove(key, emitter);
        }
        return emitter;
    }

    public void notify(ExportTaskProcess task) {
        if (task == null) {
            return;
        }
        notifyByKey(key(task.getCreator()), task);
        notifyByKey("all", task);
    }

    private void notifyByKey(String key, ExportTaskProcess task) {
        List<SseEmitter> list = emitters.get(key);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("export-task").data(task));
            } catch (Exception e) {
                log.warn("发送导出任务SSE通知失败，taskId={}", task.getId(), e);
                remove(key, emitter);
            }
        }
    }

    private String key(String creator) {
        return StringUtils.hasText(creator) ? creator : "anonymous";
    }

    private void remove(String key, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(key);
        if (list != null) {
            list.remove(emitter);
        }
    }
}

