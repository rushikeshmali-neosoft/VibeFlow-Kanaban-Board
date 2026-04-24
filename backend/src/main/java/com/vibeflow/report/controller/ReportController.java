package com.vibeflow.report.controller;

import com.vibeflow.common.dto.ApiResponse;
import com.vibeflow.report.dto.TimeReportDTO;
import com.vibeflow.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report", description = "Reporting APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/time")
    @Operation(summary = "Get time report with task totals and grand total")
    public ResponseEntity<ApiResponse<TimeReportDTO>> getTimeReport() {
        TimeReportDTO report = reportService.generateTimeReport();
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}