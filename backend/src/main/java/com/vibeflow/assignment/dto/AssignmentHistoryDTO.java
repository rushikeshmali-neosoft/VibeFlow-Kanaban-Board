package com.vibeflow.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentHistoryDTO {
    private Long id;
    private Long taskId;
    private Long oldAssigneeId;
    private String oldAssigneeEmail;
    private Long newAssigneeId;
    private String newAssigneeEmail;
    private Long changedById;
    private String changedByEmail;
    private LocalDateTime changedAt;
}