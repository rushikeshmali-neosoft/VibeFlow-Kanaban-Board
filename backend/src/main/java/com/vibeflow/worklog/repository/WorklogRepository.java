package com.vibeflow.worklog.repository;

import com.vibeflow.task.entity.Task;
import com.vibeflow.worklog.entity.Worklog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WorklogRepository extends JpaRepository<Worklog, Long> {
    
    List<Worklog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    List<Worklog> findByTask(Task task);
    
    @Query("SELECT SUM(w.hours) FROM Worklog w WHERE w.task.id = :taskId")
    BigDecimal sumHoursByTaskId(@Param("taskId") Long taskId);
    
    @Query("SELECT SUM(w.hours) FROM Worklog w")
    BigDecimal sumTotalHours();
    
    boolean existsByTaskIdAndUserId(Long taskId, Long userId);
}