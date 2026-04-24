package com.vibeflow.worklog.service;

import com.vibeflow.auth.entity.User;
import com.vibeflow.auth.repository.UserRepository;
import com.vibeflow.common.exception.NotFoundException;
import com.vibeflow.common.exception.ValidationException;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.repository.TaskRepository;
import com.vibeflow.worklog.dto.CreateWorklogRequest;
import com.vibeflow.worklog.dto.WorklogDTO;
import com.vibeflow.worklog.entity.Worklog;
import com.vibeflow.worklog.mapper.WorklogMapper;
import com.vibeflow.worklog.repository.WorklogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorklogServiceTest {

    @Mock
    private WorklogRepository worklogRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorklogMapper worklogMapper;

    @Mock
    private com.vibeflow.task.mapper.TaskMapper taskMapper;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WorklogService worklogService;

    private Task testTask;
    private User testUser;
    private Worklog testWorklog;
    private WorklogDTO testWorklogDTO;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");

        testWorklog = new Worklog();
        testWorklog.setId(1L);
        testWorklog.setTask(testTask);
        testWorklog.setUser(testUser);
        testWorklog.setHours(new BigDecimal("2.5"));
        testWorklog.setCreatedAt(LocalDateTime.now());

        testWorklogDTO = new WorklogDTO();
        testWorklogDTO.setId(1L);
        testWorklogDTO.setTaskId(1L);
        testWorklogDTO.setUserId(1L);
        testWorklogDTO.setUserEmail("user@example.com");
        testWorklogDTO.setHours(new BigDecimal("2.5"));
        testWorklogDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createWorklog_ValidRequest_ReturnsWorklogDTO() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setHours(new BigDecimal("2.5"));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(testUser));
        when(worklogRepository.save(any(Worklog.class))).thenReturn(testWorklog);
        when(worklogMapper.toDto(any(Worklog.class))).thenReturn(testWorklogDTO);
        when(taskMapper.toDTO(any(com.vibeflow.task.entity.Task.class))).thenReturn(new com.vibeflow.task.dto.TaskDTO());

        WorklogDTO result = worklogService.createWorklog(1L, request, "test@example.com");

        assertNotNull(result);
        assertEquals(new BigDecimal("2.5"), result.getHours());
        verify(worklogRepository).save(any(Worklog.class));
        verify(eventPublisher).publishEvent(any(com.vibeflow.task.event.TaskRealtimeEvent.class));
    }

    @Test
    void createWorklog_TaskNotFound_ThrowsNotFoundException() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setHours(new BigDecimal("2.5"));

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> worklogService.createWorklog(999L, request, "test@example.com"));
    }

    @Test
    void createWorklog_UserNotFound_ThrowsNotFoundException() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setHours(new BigDecimal("2.5"));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> worklogService.createWorklog(1L, request, "nonexistent@example.com"));
    }

    @Test
    void createWorklog_HoursZero_ThrowsValidationException() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setHours(BigDecimal.ZERO);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> worklogService.createWorklog(1L, request, "test@example.com"));
        assertEquals("Hours must be greater than 0", exception.getMessage());
    }

    @Test
    void createWorklog_HoursNegative_ThrowsValidationException() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setHours(new BigDecimal("-1.5"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> worklogService.createWorklog(1L, request, "test@example.com"));
        assertEquals("Hours must be greater than 0", exception.getMessage());
    }

    @Test
    void createWorklog_HoursTooLarge_ThrowsValidationException() {
        CreateWorklogRequest request = new CreateWorklogRequest();
        request.setHours(new BigDecimal("1000.00"));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> worklogService.createWorklog(1L, request, "test@example.com"));
        assertEquals("Hours cannot exceed 999.99", exception.getMessage());
    }

    @Test
    void getWorklogsByTaskId_TaskExists_ReturnsWorklogList() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(worklogRepository.findByTaskIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(testWorklog));
        when(worklogMapper.toDto(testWorklog)).thenReturn(testWorklogDTO);

        List<WorklogDTO> result = worklogService.getWorklogsByTaskId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getTaskId());
    }

    @Test
    void getWorklogsByTaskId_TaskNotFound_ThrowsNotFoundException() {
        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> worklogService.getWorklogsByTaskId(999L));
    }

    @Test
    void getTotalHoursByTaskId_ReturnsCorrectTotal() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(worklogRepository.sumHoursByTaskId(1L)).thenReturn(new BigDecimal("7.5"));

        BigDecimal result = worklogService.getTotalHoursByTaskId(1L);

        assertEquals(new BigDecimal("7.5"), result);
    }

    @Test
    void getTotalHoursByTaskId_NoWorklogs_ReturnsZero() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(worklogRepository.sumHoursByTaskId(1L)).thenReturn(null);

        BigDecimal result = worklogService.getTotalHoursByTaskId(1L);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getGrandTotalHours_ReturnsCorrectTotal() {
        when(worklogRepository.sumTotalHours()).thenReturn(new BigDecimal("25.5"));

        BigDecimal result = worklogService.getGrandTotalHours();

        assertEquals(new BigDecimal("25.5"), result);
    }
}
