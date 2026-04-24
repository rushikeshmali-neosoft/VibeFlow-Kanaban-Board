package com.vibeflow.worklog.controller;

import com.vibeflow.common.dto.ApiResponse;
import com.vibeflow.worklog.dto.CreateWorklogRequest;
import com.vibeflow.worklog.dto.WorklogDTO;
import com.vibeflow.worklog.service.WorklogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/worklogs")
@RequiredArgsConstructor
@Tag(name = "Worklog", description = "Worklog management APIs")
public class WorklogController {
    
    private final WorklogService worklogService;
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Log time for a task", description = "Add a worklog entry for a specific task")
    public ResponseEntity<ApiResponse<WorklogDTO>> createWorklog(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateWorklogRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        WorklogDTO worklog = worklogService.createWorklog(taskId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Worklog added successfully", worklog));
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get worklogs for a task", description = "Retrieve all worklog entries for a specific task")
    public ResponseEntity<ApiResponse<List<WorklogDTO>>> getWorklogsByTaskId(@PathVariable Long taskId) {
        
        List<WorklogDTO> worklogs = worklogService.getWorklogsByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success("Worklogs retrieved successfully", worklogs));
    }
    
    @GetMapping("/total")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get total hours for a task", description = "Calculate total logged hours for a specific task")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalHoursByTaskId(@PathVariable Long taskId) {
        
        BigDecimal totalHours = worklogService.getTotalHoursByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success("Total hours retrieved successfully", totalHours));
    }
}