package com.vibeflow.report.dto;

import com.vibeflow.common.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TaskTimeReportDTO {
    private Long taskId;
    private String title;
    private TaskStatus status;
    private String assignee;
    private BigDecimal totalHours;
}