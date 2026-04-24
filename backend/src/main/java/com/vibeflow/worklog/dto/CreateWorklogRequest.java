package com.vibeflow.worklog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateWorklogRequest {
    
    @NotNull(message = "Hours are required")
    @DecimalMin(value = "0.01", message = "Hours must be greater than 0")
    @Digits(integer = 3, fraction = 2, message = "Hours must have up to 3 integer digits and 2 decimal places")
    private BigDecimal hours;
}
