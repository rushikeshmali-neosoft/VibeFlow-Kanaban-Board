package com.vibeflow.task.service;

import com.vibeflow.auth.entity.User;
import com.vibeflow.auth.repository.UserRepository;
import com.vibeflow.common.enums.TaskStatus;
import com.vibeflow.common.exception.NotFoundException;
import com.vibeflow.common.exception.ValidationException;
import com.vibeflow.task.dto.CreateTaskRequest;
import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.task.dto.UpdateStatusRequest;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.event.TaskRealtimeEvent;
import com.vibeflow.task.mapper.TaskMapper;
import com.vibeflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    
    private static final List<TaskStatus> STATUS_ORDER = List.of(
            TaskStatus.BACKLOG,
            TaskStatus.TODO,
            TaskStatus.IN_PROGRESS,
            TaskStatus.IN_REVIEW,
            TaskStatus.TESTING,
            TaskStatus.DONE,
            TaskStatus.CANCELLED,
            TaskStatus.CLOSED
    );

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public TaskDTO createTask(CreateTaskRequest request, String creatorEmail) {
        validateTitle(request.getTitle());

        User creator = userRepository.findByEmailIgnoreCase(creatorEmail)
                .orElseThrow(() -> new NotFoundException("Creator not found"));

        Task task = new Task();
        task.setTitle(request.getTitle().trim());
        task.setDueDate(request.getDueDate());
        task.setCreatedBy(creator);
        task.setStatus(TaskStatus.BACKLOG);

        // Resolve and set assignee if provided at creation time
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new NotFoundException(
                            "Assignee not found with id: " + request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        int maxPosition = taskRepository.findMaxPositionByStatus(TaskStatus.BACKLOG);
        task.setPosition(maxPosition + 1);

        task = taskRepository.save(task);
        TaskDTO taskDTO = taskMapper.toDTO(task);
        eventPublisher.publishEvent(TaskRealtimeEvent.taskCreated(taskDTO));

        return taskDTO;
    }
    
    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        Map<TaskStatus, Integer> statusIndexes = buildStatusIndexes();
        return taskRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Task task) -> statusIndexes.getOrDefault(task.getStatus(), Integer.MAX_VALUE))
                        .thenComparing(Task::getPosition))
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
        return taskMapper.toDTO(task);
    }
    
    @Transactional
    public TaskDTO updateTaskStatus(Long taskId, UpdateStatusRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));
        
        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = request.getStatus();
        Integer newPosition = request.getPosition();

        if (oldStatus.equals(newStatus)) {
            return reorderTask(taskId, newPosition);
        }

        List<Task> sourceTasks = new ArrayList<>(taskRepository.findByStatusOrderByPositionAsc(oldStatus));
        sourceTasks.removeIf(current -> current.getId().equals(taskId));
        resequence(sourceTasks);
        taskRepository.saveAll(sourceTasks);

        List<Task> targetTasks = new ArrayList<>(taskRepository.findByStatusOrderByPositionAsc(newStatus));
        int insertIndex = Math.max(0, Math.min(newPosition - 1, targetTasks.size()));
        task.setStatus(newStatus);
        targetTasks.add(insertIndex, task);
        resequence(targetTasks);
        taskRepository.saveAll(targetTasks);

        TaskDTO taskDTO = taskMapper.toDTO(task);
        publishTaskUpdates(sourceTasks);
        publishTaskUpdates(targetTasks);
        
        return taskDTO;
    }
    
    @Transactional
    public TaskDTO reorderTask(Long taskId, Integer newPosition) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));
        
        if (task.getPosition().equals(newPosition)) {
            return taskMapper.toDTO(task);
        }

        List<Task> tasks = new ArrayList<>(taskRepository.findByStatusOrderByPositionAsc(task.getStatus()));
        tasks.removeIf(current -> current.getId().equals(taskId));
        int insertIndex = Math.max(0, Math.min(newPosition - 1, tasks.size()));
        tasks.add(insertIndex, task);
        resequence(tasks);
        taskRepository.saveAll(tasks);

        TaskDTO taskDTO = taskMapper.toDTO(task);
        publishTaskUpdates(tasks);
        
        return taskDTO;
    }
    
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }
        if (title.length() > 255) {
            throw new ValidationException("Permitted characters 255 only");
        }
    }

    private void resequence(List<Task> tasks) {
        for (int index = 0; index < tasks.size(); index++) {
            tasks.get(index).setPosition(index + 1);
        }
    }

    private Map<TaskStatus, Integer> buildStatusIndexes() {
        return STATUS_ORDER.stream()
                .collect(Collectors.toMap(status -> status, STATUS_ORDER::indexOf));
    }

    private void publishTaskUpdates(List<Task> tasks) {
        tasks.stream()
                .map(taskMapper::toDTO)
                .forEach(taskDTO -> eventPublisher.publishEvent(TaskRealtimeEvent.taskUpdated(taskDTO)));
    }
}
