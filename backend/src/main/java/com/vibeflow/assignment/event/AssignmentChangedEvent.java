package com.vibeflow.assignment.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentChangedEvent {
    private Long taskId;
    private Long oldAssigneeId;
    private Long newAssigneeId;
    private Long changedById;
}
