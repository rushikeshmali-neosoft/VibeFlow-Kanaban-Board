package com.vibeflow.task.controller;

import com.vibeflow.common.dto.ApiResponse;
import com.vibeflow.task.dto.CreateTaskRequest;
import com.vibeflow.task.dto.ReorderTaskRequest;
import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.task.dto.UpdateStatusRequest;
import com.vibeflow.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management APIs")
public class TaskController {
    
    private final TaskService taskService;
    
    @PostMapping
    @Operation(summary = "Create new task")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TaskDTO task = taskService.createTask(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", task));
    }
    
    @GetMapping
    @Operation(summary = "Get all tasks")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskDTO>> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status (drag & drop between columns)")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        TaskDTO task = taskService.updateTaskStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task status updated", task));
    }
    
    @PatchMapping("/{id}/reorder")
    @Operation(summary = "Reorder task within same column")
    public ResponseEntity<ApiResponse<TaskDTO>> reorderTask(
            @PathVariable Long id,
            @Valid @RequestBody ReorderTaskRequest request) {
        TaskDTO task = taskService.reorderTask(id, request.getPosition());
        return ResponseEntity.ok(ApiResponse.success("Task reordered successfully", task));
    }
}
