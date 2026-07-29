package com.example.urlshortener.repository;

import com.example.urlshortener.entity.WorkflowExecution;
import com.example.urlshortener.entity.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {

    List<WorkflowExecution> findByWorkflowStateOrderByUpdatedAtAsc(WorkflowState workflowState);
}
