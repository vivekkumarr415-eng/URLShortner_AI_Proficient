package com.example.urlshortener.dto;

import com.example.urlshortener.entity.ApprovalDecision;
import com.example.urlshortener.entity.WorkflowState;

import java.time.Instant;
import java.util.UUID;

public record ApprovalHistoryResponse(
        UUID id,
        UUID workflowExecutionId,
        WorkflowState previousState,
        WorkflowState resultingState,
        ApprovalDecision decision,
        String approver,
        String comments,
        Instant decidedAt
) {
}
