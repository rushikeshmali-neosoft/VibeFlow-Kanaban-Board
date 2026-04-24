package com.vibeflow.assignment.service;

import com.vibeflow.assignment.dto.AssignmentHistoryDTO;
import com.vibeflow.assignment.entity.AssignmentHistory;
import com.vibeflow.assignment.event.AssignmentChangedEvent;
import com.vibeflow.assignment.repository.AssignmentHistoryRepository;
import com.vibeflow.auth.entity.User;
import com.vibeflow.auth.repository.UserRepository;
import com.vibeflow.common.exception.NotFoundException;
import com.vibeflow.common.exception.ValidationException;
import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.event.TaskRealtimeEvent;
import com.vibeflow.task.mapper.TaskMapper;
import com.vibeflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {
    
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public TaskDTO updateAssignee(Long taskId, Long assigneeId, String changedByEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));
        
        User changedBy = userRepository.findByEmailIgnoreCase(changedByEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + changedByEmail));
        
        User oldAssignee = task.getAssignee();
        User newAssignee = null;
        
        if (assigneeId != null) {
            newAssignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + assigneeId));
            
            if (newAssignee.equals(oldAssignee)) {
                throw new ValidationException("Task is already assigned to this user");
            }
        }
        
        task.setAssignee(newAssignee);
        Task savedTask = taskRepository.save(task);
        
        // Publish assignment changed event
        eventPublisher.publishEvent(new AssignmentChangedEvent(
                taskId,
                oldAssignee != null ? oldAssignee.getId() : null,
                newAssignee != null ? newAssignee.getId() : null,
                changedBy.getId()
        ));
        
        log.info("Task {} assignee updated from {} to {} by {}",
                taskId,
                oldAssignee != null ? oldAssignee.getEmail() : "null",
                newAssignee != null ? newAssignee.getEmail() : "null",
                changedByEmail);
        
        TaskDTO taskDTO = taskMapper.toDTO(savedTask);
        eventPublisher.publishEvent(TaskRealtimeEvent.assignmentUpdated(taskDTO));
        
        return taskDTO;
    }
    
    @Transactional(readOnly = true)
    public List<AssignmentHistoryDTO> getAssignmentHistory(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));
        
        List<AssignmentHistory> history = assignmentHistoryRepository.findByTaskIdOrderByChangedAtDesc(task.getId());
        
        return history.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private AssignmentHistoryDTO toDto(AssignmentHistory history) {
        AssignmentHistoryDTO dto = new AssignmentHistoryDTO();
        dto.setId(history.getId());
        dto.setTaskId(history.getTask().getId());
        
        if (history.getOldAssignee() != null) {
            dto.setOldAssigneeId(history.getOldAssignee().getId());
            dto.setOldAssigneeEmail(history.getOldAssignee().getEmail());
        }
        
        if (history.getNewAssignee() != null) {
            dto.setNewAssigneeId(history.getNewAssignee().getId());
            dto.setNewAssigneeEmail(history.getNewAssignee().getEmail());
        }
        
        dto.setChangedById(history.getChangedBy().getId());
        dto.setChangedByEmail(history.getChangedBy().getEmail());
        dto.setChangedAt(history.getChangedAt());
        
        return dto;
    }
}
