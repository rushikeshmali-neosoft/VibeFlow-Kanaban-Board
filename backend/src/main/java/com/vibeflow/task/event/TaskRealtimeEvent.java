package com.vibeflow.task.event;

import com.vibeflow.task.dto.TaskDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskRealtimeEvent {
    private String type;
    private TaskDTO task;

    public static TaskRealtimeEvent taskCreated(TaskDTO task) {
        return new TaskRealtimeEvent("TASK_CREATED", task);
    }

    public static TaskRealtimeEvent taskUpdated(TaskDTO task) {
        return new TaskRealtimeEvent("TASK_UPDATED", task);
    }

    public static TaskRealtimeEvent assignmentUpdated(TaskDTO task) {
        return new TaskRealtimeEvent("ASSIGNMENT_UPDATED", task);
    }

    public static TaskRealtimeEvent worklogAdded(TaskDTO task) {
        return new TaskRealtimeEvent("WORKLOG_ADDED", task);
    }
}
