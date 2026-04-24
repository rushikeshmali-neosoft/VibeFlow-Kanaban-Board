package com.vibeflow.board.service;

import com.vibeflow.board.dto.BoardDTO;
import com.vibeflow.common.enums.TaskStatus;
import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    
    private final TaskService taskService;
    
    private static final List<TaskStatus> COLUMNS = Arrays.asList(
        TaskStatus.BACKLOG,
        TaskStatus.TODO,
        TaskStatus.IN_PROGRESS,
        TaskStatus.IN_REVIEW,
        TaskStatus.TESTING,
        TaskStatus.DONE,
        TaskStatus.CANCELLED,
        TaskStatus.CLOSED
    );
    
    @Transactional(readOnly = true)
    public BoardDTO getBoard() {
        return BoardDTO.of(COLUMNS, taskService.getAllTasks());
    }
    
    @Transactional(readOnly = true)
    public List<TaskStatus> getColumns() {
        return COLUMNS;
    }
}
