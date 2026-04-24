package com.vibeflow.assignment.repository;

import com.vibeflow.assignment.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {
    
    List<AssignmentHistory> findByTaskIdOrderByChangedAtDesc(Long taskId);
}