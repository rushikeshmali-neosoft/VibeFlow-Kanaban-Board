package com.vibeflow.board.dto;

import com.vibeflow.common.enums.TaskStatus;
import com.vibeflow.task.dto.TaskDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private List<TaskStatus> columns;
    private List<TaskDTO> tasks;
    
    public static BoardDTO of(List<TaskStatus> columns, List<TaskDTO> tasks) {
        return new BoardDTO(columns, tasks);
    }
}
