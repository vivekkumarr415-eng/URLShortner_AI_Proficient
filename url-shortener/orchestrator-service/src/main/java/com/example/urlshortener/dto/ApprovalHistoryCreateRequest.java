package com.example.urlshortener.dto;

import com.example.urlshortener.entity.ApprovalDecision;
import com.example.urlshortener.entity.WorkflowState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record ApprovalHistoryCreateRequest(
        @NotNull UUID workflowExecutionId,
        @NotNull WorkflowState previousState,
        @NotNull WorkflowState resultingState,
        @NotNull ApprovalDecision decision,
        @NotBlank @Size(max = 120) String approver,
        @Size(max = 2_000) String comments,
        Instant decidedAt
) {
}
