package com.example.urlshortener.dto;

import com.example.urlshortener.entity.WorkflowState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkflowExecutionCreateRequest(
        @NotBlank @Size(max = 120) String workflowName,
        @NotNull WorkflowState workflowState
) {
}
