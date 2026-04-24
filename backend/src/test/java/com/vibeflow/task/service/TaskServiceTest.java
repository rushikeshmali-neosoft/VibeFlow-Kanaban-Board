package com.vibeflow.task.service;

import com.vibeflow.auth.entity.User;
import com.vibeflow.auth.repository.UserRepository;
import com.vibeflow.common.enums.TaskStatus;
import com.vibeflow.common.exception.NotFoundException;
import com.vibeflow.common.exception.ValidationException;
import com.vibeflow.task.dto.CreateTaskRequest;
import com.vibeflow.task.dto.ReorderTaskRequest;
import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.task.dto.UpdateStatusRequest;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.event.TaskRealtimeEvent;
import com.vibeflow.task.mapper.TaskMapper;
import com.vibeflow.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService TDD - Full Coverage")
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

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

    // ── CREATE TASK ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createTask()")
    class CreateTaskTests {

        @Test
        @DisplayName("Given valid request, when createTask, then returns TaskDTO with event published")
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
        @DisplayName("Given title > 255 chars, when createTask, then throws ValidationException")
        void createTask_TitleTooLong_ThrowsValidationException() {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("a".repeat(256));

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> taskService.createTask(request, "creator@example.com"));
            assertEquals("Permitted characters 255 only", ex.getMessage());
        }

        @Test
        @DisplayName("Given blank title, when createTask, then throws ValidationException")
        void createTask_BlankTitle_ThrowsValidationException() {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("   ");

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> taskService.createTask(request, "creator@example.com"));
            assertEquals("Title is required", ex.getMessage());
        }

        @Test
        @DisplayName("Given null title, when createTask, then throws ValidationException")
        void createTask_NullTitle_ThrowsValidationException() {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle(null);

            assertThrows(ValidationException.class,
                    () -> taskService.createTask(request, "creator@example.com"));
        }

        @Test
        @DisplayName("Given exactly 255 chars title, when createTask, then succeeds")
        void createTask_TitleExactly255Chars_Succeeds() {
            CreateTaskRequest request = new CreateTaskRequest();
            request.setTitle("a".repeat(255));

            when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(testUser));
            when(taskRepository.findMaxPositionByStatus(TaskStatus.BACKLOG)).thenReturn(0);
            when(taskRepository.save(any(Task.class))).thenReturn(backlogTask);
            when(taskMapper.toDTO(backlogTask)).thenReturn(taskDTO);

            assertDoesNotThrow(() -> taskService.createTask(request, "creator@example.com"));
        }
    }

    // ── GET TASK ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTaskById()")
    class GetTaskByIdTests {

        @Test
        @DisplayName("Given existing task ID, when getTaskById, then returns correct TaskDTO")
        void getTaskById_TaskExists_ReturnsTaskDTO() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(backlogTask));
            when(taskMapper.toDTO(backlogTask)).thenReturn(taskDTO);

            TaskDTO result = taskService.getTaskById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Test Task", result.getTitle());
        }

        @Test
        @DisplayName("Given non-existent task ID, when getTaskById, then throws NotFoundException")
        void getTaskById_NotFound_ThrowsNotFoundException() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> taskService.getTaskById(999L));
            assertNotNull(ex.getMessage());
        }
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateTaskStatus()")
    class UpdateTaskStatusTests {

        @Test
        @DisplayName("Given cross-column move, when updateTaskStatus, then resequences both columns and fires events")
        void updateTaskStatus_CrossColumn_ResequencesAndPublishesEvent() {
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
    }

    // ── REORDER ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("reorderTask()")
    class ReorderTaskTests {

        @Test
        @DisplayName("Given valid reorder within column, when reorderTask, then resequences and publishes events")
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

        @Test
        @DisplayName("Given non-existent task ID, when reorderTask, then throws NotFoundException")
        void reorderTask_TaskNotFound_ThrowsNotFoundException() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> taskService.reorderTask(999L, 1));
        }
    }
}
