package com.vibeflow.worklog.mapper;

import com.vibeflow.worklog.dto.WorklogDTO;
import com.vibeflow.worklog.entity.Worklog;
import org.springframework.stereotype.Component;

@Component
public class WorklogMapper {
    
    public WorklogDTO toDto(Worklog worklog) {
        if (worklog == null) {
            return null;
        }
        
        WorklogDTO dto = new WorklogDTO();
        dto.setId(worklog.getId());
        dto.setHours(worklog.getHours());
        dto.setCreatedAt(worklog.getCreatedAt());
        
        if (worklog.getTask() != null) {
            dto.setTaskId(worklog.getTask().getId());
        }
        
        if (worklog.getUser() != null) {
            dto.setUserId(worklog.getUser().getId());
            dto.setUserEmail(worklog.getUser().getEmail());
        }
        
        return dto;
    }
}
