package com.vibeflow.assignment.controller;

import com.vibeflow.assignment.dto.AssignmentHistoryDTO;
import com.vibeflow.assignment.dto.UpdateAssigneeRequest;
import com.vibeflow.common.dto.ApiResponse;
import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.assignment.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}")
@RequiredArgsConstructor
@Tag(name = "Assignment", description = "Task assignment management APIs")
public class AssignmentController {
    
    private final AssignmentService assignmentService;
    
    @PatchMapping("/assignee")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign or unassign task", description = "Update assignee for a task. Set assigneeId to null to unassign.")
    public ResponseEntity<ApiResponse<TaskDTO>> updateAssignee(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateAssigneeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        TaskDTO task = assignmentService.updateAssignee(taskId, request.getAssigneeId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Assignee updated successfully", task));
    }
    
    @GetMapping({"/history", "/assignment-history"})
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get assignment history", description = "Retrieve assignment history for a specific task")
    public ResponseEntity<ApiResponse<List<AssignmentHistoryDTO>>> getAssignmentHistory(@PathVariable Long taskId) {
        
        List<AssignmentHistoryDTO> history = assignmentService.getAssignmentHistory(taskId);
        return ResponseEntity.ok(ApiResponse.success("Assignment history retrieved successfully", history));
    }
}
