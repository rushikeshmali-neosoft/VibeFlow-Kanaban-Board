package com.vibeflow.admin.controller;

import com.vibeflow.admin.service.AdminUserService;
import com.vibeflow.common.dto.ApiResponse;
import com.vibeflow.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getAllUsers()));
    }

    @PostMapping("/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<UserDTO>> toggleUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.toggleUserStatus(userId)));
    }
}
