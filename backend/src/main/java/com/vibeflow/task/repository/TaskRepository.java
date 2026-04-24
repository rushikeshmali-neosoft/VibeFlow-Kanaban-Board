package com.vibeflow.task.repository;

import com.vibeflow.common.enums.TaskStatus;
import com.vibeflow.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findAllByOrderByStatusAscPositionAsc();
    
    List<Task> findByStatusOrderByPositionAsc(TaskStatus status);
    
    Optional<Task> findFirstByStatusOrderByPositionDesc(TaskStatus status);
    
    @Query("SELECT COALESCE(MAX(t.position), 0) FROM Task t WHERE t.status = :status")
    Integer findMaxPositionByStatus(@Param("status") TaskStatus status);
    
    @Modifying
    @Query("UPDATE Task t SET t.position = t.position + 1 WHERE t.status = :status AND t.position >= :position")
    void incrementPositionsFrom(@Param("status") TaskStatus status, @Param("position") Integer position);
    
    @Modifying
    @Query("UPDATE Task t SET t.position = t.position - 1 WHERE t.status = :status AND t.position > :position")
    void decrementPositionsFrom(@Param("status") TaskStatus status, @Param("position") Integer position);
    
    @Modifying
    @Query("UPDATE Task t SET t.position = :newPosition WHERE t.id = :taskId")
    void updatePosition(@Param("taskId") Long taskId, @Param("newPosition") Integer newPosition);
    
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus, t.position = :newPosition WHERE t.id = :taskId")
    void updateStatusAndPosition(@Param("taskId") Long taskId, 
                                 @Param("newStatus") TaskStatus newStatus, 
                                 @Param("newPosition") Integer newPosition);
}