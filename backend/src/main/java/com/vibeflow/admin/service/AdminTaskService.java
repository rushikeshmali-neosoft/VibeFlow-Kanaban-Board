package com.vibeflow.admin.service;

import com.vibeflow.task.entity.Task;
import com.vibeflow.task.repository.TaskRepository;
import com.vibeflow.task.dto.TaskDTO;
import com.vibeflow.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTask(Long taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
        } else {
            throw new RuntimeException("Task not found");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTaskStats() {
        List<Task> tasks = taskRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", tasks.size());
        
        Map<String, Long> statusCounts = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));
        stats.put("tasksPerStatus", statusCounts);
        
        return stats;
    }
}
