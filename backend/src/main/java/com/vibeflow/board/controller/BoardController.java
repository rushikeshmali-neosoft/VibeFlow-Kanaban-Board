package com.vibeflow.board.controller;

import com.vibeflow.board.dto.BoardDTO;
import com.vibeflow.board.service.BoardService;
import com.vibeflow.common.dto.ApiResponse;
import com.vibeflow.common.enums.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
@Tag(name = "Board", description = "Board management APIs")
public class BoardController {
    
    private final BoardService boardService;
    
    @GetMapping
    @Operation(summary = "Get full board data (columns + tasks)")
    public ResponseEntity<ApiResponse<BoardDTO>> getBoard() {
        BoardDTO board = boardService.getBoard();
        return ResponseEntity.ok(ApiResponse.success(board));
    }
    
    @GetMapping("/columns")
    @Operation(summary = "Get board columns")
    public ResponseEntity<ApiResponse<List<TaskStatus>>> getColumns() {
        List<TaskStatus> columns = boardService.getColumns();
        return ResponseEntity.ok(ApiResponse.success(columns));
    }
}