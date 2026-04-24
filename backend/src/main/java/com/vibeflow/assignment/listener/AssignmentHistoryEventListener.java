package com.vibeflow.assignment.listener;

import com.vibeflow.assignment.entity.AssignmentHistory;
import com.vibeflow.assignment.event.AssignmentChangedEvent;
import com.vibeflow.assignment.repository.AssignmentHistoryRepository;
import com.vibeflow.auth.repository.UserRepository;
import com.vibeflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentHistoryEventListener {
    
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAssignmentChangedEvent(AssignmentChangedEvent event) {
        try {
            log.info("Processing assignment change event for task: {}", event.getTaskId());
            
            AssignmentHistory history = new AssignmentHistory();
            history.setTask(taskRepository.getReferenceById(event.getTaskId()));
            
            if (event.getOldAssigneeId() != null) {
                history.setOldAssignee(userRepository.getReferenceById(event.getOldAssigneeId()));
            }
            
            if (event.getNewAssigneeId() != null) {
                history.setNewAssignee(userRepository.getReferenceById(event.getNewAssigneeId()));
            }
            history.setChangedBy(userRepository.getReferenceById(event.getChangedById()));
            
            assignmentHistoryRepository.save(history);
            
            log.info("Assignment history saved for task: {}", event.getTaskId());
        } catch (Exception e) {
            log.error("Failed to save assignment history for event: {}", event, e);
        }
    }
}
