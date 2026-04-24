package com.vibeflow.task.dto;

import com.vibeflow.common.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private TaskStatus status;
    private Integer position;
    private Long assigneeId;
    private String assigneeEmail;
    private Long createdById;
    private String createdByEmail;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}