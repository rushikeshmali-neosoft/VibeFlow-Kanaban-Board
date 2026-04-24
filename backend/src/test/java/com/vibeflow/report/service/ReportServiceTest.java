package com.vibeflow.report.service;

import com.vibeflow.auth.entity.User;
import com.vibeflow.common.enums.TaskStatus;
import com.vibeflow.report.dto.TaskTimeReportDTO;
import com.vibeflow.report.dto.TimeReportDTO;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.repository.TaskRepository;
import com.vibeflow.worklog.repository.WorklogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private WorklogRepository worklogRepository;
    
    @InjectMocks
    private ReportService reportService;
    
    private Task task1;
    private Task task2;
    private User assignee;
    
    @BeforeEach
    void setUp() {
        assignee = new User();
        assignee.setId(2L);
        assignee.setEmail("assignee@example.com");
        
        task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task1.setAssignee(assignee);
        
        task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setStatus(TaskStatus.DONE);
        task2.setAssignee(null);
    }
    
    @Test
    void generateTimeReport_ReturnsCorrectReport() {
        // Arrange
        List<Task> tasks = Arrays.asList(task1, task2);
        
        when(taskRepository.findAll())
                .thenReturn(tasks);
        when(worklogRepository.sumHoursByTaskId(1L))
                .thenReturn(new BigDecimal("5.5"));
        when(worklogRepository.sumHoursByTaskId(2L))
                .thenReturn(new BigDecimal("3.0"));
        when(worklogRepository.sumTotalHours())
                .thenReturn(new BigDecimal("8.5"));
        
        // Act
        TimeReportDTO report = reportService.generateTimeReport();
        
        // Assert
        assertNotNull(report);
        assertNotNull(report.getTasks());
        assertEquals(2, report.getTasks().size());
        assertEquals(new BigDecimal("8.5"), report.getGrandTotal());
        
        // Verify task 1
        TaskTimeReportDTO task1Report = report.getTasks().stream()
                .filter(t -> t.getTaskId().equals(1L))
                .findFirst()
                .orElseThrow();
        assertEquals("Task 1", task1Report.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, task1Report.getStatus());
        assertEquals("assignee@example.com", task1Report.getAssignee());
        assertEquals(new BigDecimal("5.5"), task1Report.getTotalHours());
        
        // Verify task 2
        TaskTimeReportDTO task2Report = report.getTasks().stream()
                .filter(t -> t.getTaskId().equals(2L))
                .findFirst()
                .orElseThrow();
        assertEquals("Task 2", task2Report.getTitle());
        assertEquals(TaskStatus.DONE, task2Report.getStatus());
        assertNull(task2Report.getAssignee());
        assertEquals(new BigDecimal("3.0"), task2Report.getTotalHours());
        
        verify(taskRepository).findAll();
        verify(worklogRepository, times(2)).sumHoursByTaskId(anyLong());
        verify(worklogRepository).sumTotalHours();
    }
    
    @Test
    void generateTimeReport_NoTasks_ReturnsEmptyReport() {
        // Arrange
        when(taskRepository.findAll())
                .thenReturn(Arrays.asList());
        when(worklogRepository.sumTotalHours())
                .thenReturn(null);
        
        // Act
        TimeReportDTO report = reportService.generateTimeReport();
        
        // Assert
        assertNotNull(report);
        assertNotNull(report.getTasks());
        assertTrue(report.getTasks().isEmpty());
        assertEquals(BigDecimal.ZERO, report.getGrandTotal());
    }
    
    @Test
    void generateTimeReport_TaskWithNoWorklogs_ReturnsZeroHours() {
        // Arrange
        Task task = new Task();
        task.setId(3L);
        task.setTitle("Task 3");
        task.setStatus(TaskStatus.BACKLOG);
        task.setAssignee(null);
        
        when(taskRepository.findAll())
                .thenReturn(Arrays.asList(task));
        when(worklogRepository.sumHoursByTaskId(3L))
                .thenReturn(null);
        when(worklogRepository.sumTotalHours())
                .thenReturn(null);
        
        // Act
        TimeReportDTO report = reportService.generateTimeReport();
        
        // Assert
        assertNotNull(report);
        assertEquals(1, report.getTasks().size());
        assertEquals(BigDecimal.ZERO, report.getGrandTotal());
        
        TaskTimeReportDTO taskReport = report.getTasks().get(0);
        assertEquals(BigDecimal.ZERO, taskReport.getTotalHours());
    }
    
    @Test
    void generateTimeReport_MultipleTasks_CorrectGrandTotal() {
        // Arrange
        Task task3 = new Task();
        task3.setId(3L);
        task3.setTitle("Task 3");
        task3.setStatus(TaskStatus.TODO);
        
        List<Task> tasks = Arrays.asList(task1, task2, task3);
        
        when(taskRepository.findAll())
                .thenReturn(tasks);
        when(worklogRepository.sumHoursByTaskId(1L))
                .thenReturn(new BigDecimal("10.0"));
        when(worklogRepository.sumHoursByTaskId(2L))
                .thenReturn(new BigDecimal("5.5"));
        when(worklogRepository.sumHoursByTaskId(3L))
                .thenReturn(new BigDecimal("2.5"));
        when(worklogRepository.sumTotalHours())
                .thenReturn(new BigDecimal("18.0"));
        
        // Act
        TimeReportDTO report = reportService.generateTimeReport();
        
        // Assert
        assertNotNull(report);
        assertEquals(3, report.getTasks().size());
        assertEquals(new BigDecimal("18.0"), report.getGrandTotal());
        
        // Verify sum of individual task hours equals grand total
        BigDecimal sumOfTaskHours = report.getTasks().stream()
                .map(TaskTimeReportDTO::getTotalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("18.0"), sumOfTaskHours);
    }
}