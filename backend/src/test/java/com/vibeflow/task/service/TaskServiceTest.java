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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task backlogTask;
    private Task progressTask;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        backlogTask = new Task();
        backlogTask.setId(1L);
        backlogTask.setTitle("Test Task");
        backlogTask.setStatus(TaskStatus.BACKLOG);
        backlogTask.setPosition(1);
        backlogTask.setCreatedBy(testUser);

        progressTask = new Task();
        progressTask.setId(2L);
        progressTask.setTitle("Progress Task");
        progressTask.setStatus(TaskStatus.IN_PROGRESS);
        progressTask.setPosition(1);
        progressTask.setCreatedBy(testUser);

        taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        taskDTO.setTitle("Test Task");
        taskDTO.setStatus(TaskStatus.BACKLOG);
        taskDTO.setPosition(1);
        taskDTO.setCreatedByEmail("test@example.com");
    }

    @Test
    void createTask_ValidRequest_ReturnsTaskDTO() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDueDate(LocalDate.now().plusDays(7));

        when(userRepository.findByEmailIgnoreCase("creator@example.com")).thenReturn(Optional.of(testUser));
        when(taskRepository.findMaxPositionByStatus(TaskStatus.BACKLOG)).thenReturn(0);
        when(taskRepository.save(any(Task.class))).thenReturn(backlogTask);
        when(taskMapper.toDTO(backlogTask)).thenReturn(taskDTO);

        TaskDTO result = taskService.createTask(request, "creator@example.com");

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher, times(1)).publishEvent(any(TaskRealtimeEvent.class));
    }

    @Test
    void createTask_TitleTooLong_ThrowsValidationException() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("a".repeat(256));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> taskService.createTask(request, "creator@example.com"));
        assertEquals("Permitted characters 255 only", exception.getMessage());
    }

    @Test
    void createTask_TitleEmpty_ThrowsValidationException() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(" ");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> taskService.createTask(request, "creator@example.com"));
        assertEquals("Title is required", exception.getMessage());
    }

    @Test
    void getTaskById_TaskExists_ReturnsTaskDTO() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(backlogTask));
        when(taskMapper.toDTO(backlogTask)).thenReturn(taskDTO);

        TaskDTO result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getTaskById_TaskNotFound_ThrowsNotFoundException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.getTaskById(999L));
    }

    @Test
    void updateTaskStatus_CrossColumnMove_ResequencesAndPublishesEvent() {
        UpdateStatusRequest request = new UpdateStatusRequest(TaskStatus.IN_PROGRESS, 2);

        Task anotherBacklogTask = new Task();
        anotherBacklogTask.setId(3L);
        anotherBacklogTask.setStatus(TaskStatus.BACKLOG);
        anotherBacklogTask.setPosition(2);

        Task existingProgressTask = new Task();
        existingProgressTask.setId(4L);
        existingProgressTask.setStatus(TaskStatus.IN_PROGRESS);
        existingProgressTask.setPosition(1);

        Task movedTask = new Task();
        movedTask.setId(1L);
        movedTask.setTitle("Test Task");
        movedTask.setStatus(TaskStatus.IN_PROGRESS);
        movedTask.setPosition(2);

        TaskDTO movedTaskDTO = new TaskDTO();
        movedTaskDTO.setId(1L);
        movedTaskDTO.setStatus(TaskStatus.IN_PROGRESS);
        movedTaskDTO.setPosition(2);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(backlogTask));
        when(taskRepository.findByStatusOrderByPositionAsc(TaskStatus.BACKLOG))
                .thenReturn(List.of(backlogTask, anotherBacklogTask));
        when(taskRepository.findByStatusOrderByPositionAsc(TaskStatus.IN_PROGRESS))
                .thenReturn(List.of(existingProgressTask));
        when(taskRepository.saveAll(any())).thenReturn(List.of());
        when(taskMapper.toDTO(any(Task.class))).thenReturn(movedTaskDTO);

        TaskDTO result = taskService.updateTaskStatus(1L, request);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(2, result.getPosition());
        verify(taskRepository, times(2)).saveAll(any());
        verify(eventPublisher, times(3)).publishEvent(any(TaskRealtimeEvent.class));
    }

    @Test
    void reorderTask_WithinColumn_ResequencesAndPublishesEvent() {
        Task secondTask = new Task();
        secondTask.setId(2L);
        secondTask.setStatus(TaskStatus.BACKLOG);
        secondTask.setPosition(2);

        Task thirdTask = new Task();
        thirdTask.setId(3L);
        thirdTask.setStatus(TaskStatus.BACKLOG);
        thirdTask.setPosition(3);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(backlogTask));
        when(taskRepository.findByStatusOrderByPositionAsc(TaskStatus.BACKLOG))
                .thenReturn(List.of(backlogTask, secondTask, thirdTask));
        when(taskMapper.toDTO(backlogTask)).thenReturn(taskDTO);

        TaskDTO result = taskService.reorderTask(1L, 3);

        assertNotNull(result);
        verify(taskRepository).saveAll(any());

        ArgumentCaptor<TaskRealtimeEvent> eventCaptor = ArgumentCaptor.forClass(TaskRealtimeEvent.class);
        verify(eventPublisher, times(3)).publishEvent(eventCaptor.capture());
        assertEquals("TASK_UPDATED", eventCaptor.getAllValues().get(0).getType());
    }
}
