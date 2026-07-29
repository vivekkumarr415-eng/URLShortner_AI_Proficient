package com.example.urlshortener.dto;

import com.example.urlshortener.entity.WorkflowState;

import java.time.Instant;
import java.util.UUID;

public record WorkflowExecutionResponse(
        UUID id,
        String workflowName,
        WorkflowState workflowState,
        Instant createdAt,
        Instant updatedAt
) {
}
