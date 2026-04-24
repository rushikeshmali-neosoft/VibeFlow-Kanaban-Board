package com.vibeflow.websocket.listener;

import com.vibeflow.task.event.TaskRealtimeEvent;
import com.vibeflow.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TaskWebSocketEventListener {

    private final WebSocketService webSocketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskRealtimeEvent(TaskRealtimeEvent event) {
        webSocketService.broadcast(event.getType(), event.getTask());
    }
}
