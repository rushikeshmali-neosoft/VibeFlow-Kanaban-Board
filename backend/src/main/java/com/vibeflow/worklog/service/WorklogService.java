package com.vibeflow.worklog.service;

import com.vibeflow.auth.entity.User;
import com.vibeflow.auth.repository.UserRepository;
import com.vibeflow.common.exception.NotFoundException;
import com.vibeflow.common.exception.ValidationException;
import com.vibeflow.task.event.TaskRealtimeEvent;
import com.vibeflow.task.mapper.TaskMapper;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.repository.TaskRepository;
import com.vibeflow.worklog.dto.CreateWorklogRequest;
import com.vibeflow.worklog.dto.WorklogDTO;
import com.vibeflow.worklog.entity.Worklog;
import com.vibeflow.worklog.mapper.WorklogMapper;
import com.vibeflow.worklog.repository.WorklogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorklogService {

    private final WorklogRepository worklogRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorklogMapper worklogMapper;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public WorklogDTO createWorklog(Long taskId, CreateWorklogRequest request, String userEmail) {
        validateWorklog(request);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        Worklog worklog = new Worklog();
        worklog.setTask(task);
        worklog.setUser(user);
        worklog.setHours(request.getHours());

        Worklog savedWorklog = worklogRepository.save(worklog);
        eventPublisher.publishEvent(TaskRealtimeEvent.worklogAdded(taskMapper.toDTO(task)));
        return worklogMapper.toDto(savedWorklog);
    }

    @Transactional(readOnly = true)
    public List<WorklogDTO> getWorklogsByTaskId(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Task not found with id: " + taskId);
        }
        return worklogRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(worklogMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalHoursByTaskId(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new NotFoundException("Task not found with id: " + taskId);
        }
        BigDecimal total = worklogRepository.sumHoursByTaskId(taskId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getGrandTotalHours() {
        BigDecimal total = worklogRepository.sumTotalHours();
        return total != null ? total : BigDecimal.ZERO;
    }

    // ── Validation ────────────────────────────────────────────

    private void validateWorklog(CreateWorklogRequest request) {
        if (request.getHours() == null) {
            throw new ValidationException("Hours are required");
        }

        if (request.getHours().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Hours must be greater than 0");
        }

        // A single worklog entry cannot exceed 24 hours (one calendar day)
        if (request.getHours().compareTo(new BigDecimal("24")) > 0) {
            throw new ValidationException("Hours per entry cannot exceed 24");
        }

        // Enforce at most 2 decimal places (e.g. 1.25 is fine, 1.125 is not)
        if (request.getHours().stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Hours can have at most 2 decimal places (e.g. 1.50)");
        }
    }
}
