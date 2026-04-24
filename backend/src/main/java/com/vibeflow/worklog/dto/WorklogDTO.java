package com.vibeflow.worklog.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class WorklogDTO {
    
    private Long id;
    
    private Long taskId;
    
    private Long userId;
    
    private String userEmail;
    
    private BigDecimal hours;

    private LocalDateTime createdAt;
}
