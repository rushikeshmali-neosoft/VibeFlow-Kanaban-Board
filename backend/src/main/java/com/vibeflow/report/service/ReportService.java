package com.vibeflow.report.service;

import com.vibeflow.report.dto.TaskTimeReportDTO;
import com.vibeflow.report.dto.TimeReportDTO;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.repository.TaskRepository;
import com.vibeflow.worklog.repository.WorklogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final TaskRepository taskRepository;
    private final WorklogRepository worklogRepository;
    
    @Transactional(readOnly = true)
    public TimeReportDTO generateTimeReport() {
        List<Task> allTasks = taskRepository.findAll();
        
        List<TaskTimeReportDTO> taskReports = allTasks.stream()
                .map(this::convertToTaskTimeReport)
                .collect(Collectors.toList());
        
        BigDecimal grandTotal = worklogRepository.sumTotalHours();
        if (grandTotal == null) {
            grandTotal = BigDecimal.ZERO;
        }
        
        TimeReportDTO report = new TimeReportDTO();
        report.setTasks(taskReports);
        report.setGrandTotal(grandTotal);
        
        return report;
    }
    
    private TaskTimeReportDTO convertToTaskTimeReport(Task task) {
        TaskTimeReportDTO dto = new TaskTimeReportDTO();
        dto.setTaskId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus());
        
        if (task.getAssignee() != null) {
            dto.setAssignee(task.getAssignee().getEmail());
        }
        
        BigDecimal totalHours = worklogRepository.sumHoursByTaskId(task.getId());
        dto.setTotalHours(totalHours != null ? totalHours : BigDecimal.ZERO);
        
        return dto;
    }
}