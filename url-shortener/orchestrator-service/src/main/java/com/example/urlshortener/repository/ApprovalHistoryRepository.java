package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, UUID> {

    List<ApprovalHistory> findByWorkflowExecutionIdOrderByDecidedAtAsc(UUID workflowExecutionId);
}
