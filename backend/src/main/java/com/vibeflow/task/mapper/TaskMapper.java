package com.vibeflow.task.mapper;

import com.vibeflow.auth.entity.User;
import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.task.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    
    public TaskDTO toDTO(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus());
        dto.setPosition(task.getPosition());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        
        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
            dto.setAssigneeEmail(task.getAssignee().getEmail());
        }
        
        if (task.getCreatedBy() != null) {
            dto.setCreatedById(task.getCreatedBy().getId());
            dto.setCreatedByEmail(task.getCreatedBy().getEmail());
        }
        
        return dto;
    }
    
    public Task toEntity(TaskDTO dto, User createdBy, User assignee) {
        if (dto == null) {
            return null;
        }
        
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setStatus(dto.getStatus());
        task.setPosition(dto.getPosition());
        task.setDueDate(dto.getDueDate());
        task.setCreatedBy(createdBy);
        task.setAssignee(assignee);
        
        return task;
    }
}