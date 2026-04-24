package com.vibeflow.assignment.listener;

import com.vibeflow.assignment.entity.AssignmentHistory;
import com.vibeflow.assignment.event.AssignmentChangedEvent;
import com.vibeflow.assignment.repository.AssignmentHistoryRepository;
import com.vibeflow.auth.entity.User;
import com.vibeflow.auth.repository.UserRepository;
import com.vibeflow.task.entity.Task;
import com.vibeflow.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentHistoryEventListenerTest {

    @Mock
    private AssignmentHistoryRepository assignmentHistoryRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssignmentHistoryEventListener assignmentHistoryEventListener;

    @Test
    void handleAssignmentChangedEvent_CreatesHistoryRecord() {
        Task task = new Task();
        task.setId(10L);

        User oldAssignee = new User();
        oldAssignee.setId(1L);
        oldAssignee.setEmail("old@example.com");

        User newAssignee = new User();
        newAssignee.setId(2L);
        newAssignee.setEmail("new@example.com");

        User changedBy = new User();
        changedBy.setId(3L);
        changedBy.setEmail("changer@example.com");

        when(taskRepository.getReferenceById(10L)).thenReturn(task);
        when(userRepository.getReferenceById(1L)).thenReturn(oldAssignee);
        when(userRepository.getReferenceById(2L)).thenReturn(newAssignee);
        when(userRepository.getReferenceById(3L)).thenReturn(changedBy);

        assignmentHistoryEventListener.handleAssignmentChangedEvent(
                new AssignmentChangedEvent(10L, 1L, 2L, 3L)
        );

        ArgumentCaptor<AssignmentHistory> historyCaptor = ArgumentCaptor.forClass(AssignmentHistory.class);
        verify(assignmentHistoryRepository).save(historyCaptor.capture());

        AssignmentHistory savedHistory = historyCaptor.getValue();
        assertNotNull(savedHistory);
        assertEquals(10L, savedHistory.getTask().getId());
        assertEquals(1L, savedHistory.getOldAssignee().getId());
        assertEquals(2L, savedHistory.getNewAssignee().getId());
        assertEquals(3L, savedHistory.getChangedBy().getId());
    }
}
