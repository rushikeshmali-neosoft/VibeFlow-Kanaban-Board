package com.vibeflow.admin.controller;

import com.vibeflow.admin.service.AdminTaskService;
import com.vibeflow.common.dto.ApiResponse;
import com.vibeflow.task.dto.TaskDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tasks")
@RequiredArgsConstructor
public class AdminTaskController {

    private final AdminTaskService adminTaskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getAllTasks() {
        return ResponseEntity.ok(ApiResponse.success(adminTaskService.getAllTasks()));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        adminTaskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaskStats() {
        return ResponseEntity.ok(ApiResponse.success(adminTaskService.getTaskStats()));
    }
}
