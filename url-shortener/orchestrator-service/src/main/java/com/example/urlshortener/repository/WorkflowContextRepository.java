package com.example.urlshortener.repository;
import com.example.urlshortener.entity.WorkflowContextEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface WorkflowContextRepository extends JpaRepository<WorkflowContextEntry, UUID> { List<WorkflowContextEntry> findByWorkflowExecutionId(UUID workflowId); }
