package com.vibeflow.report.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TimeReportDTO {
    private List<TaskTimeReportDTO> tasks;
    private BigDecimal grandTotal;
}