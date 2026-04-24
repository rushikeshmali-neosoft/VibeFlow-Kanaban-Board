package com.vibeflow.websocket.service;

import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.websocket.dto.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void broadcast(String type, TaskDTO task) {
        WebSocketEvent<TaskDTO> event = WebSocketEvent.of(type, task);
        messagingTemplate.convertAndSend("/topic/tasks", event);
    }
}
