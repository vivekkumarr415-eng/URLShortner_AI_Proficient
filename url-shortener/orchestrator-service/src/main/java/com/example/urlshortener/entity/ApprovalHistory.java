package com.example.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "approval_history",
        indexes = {
                @Index(name = "idx_approval_history_workflow_decided_at", columnList = "workflow_execution_id, decided_at"),
                @Index(name = "idx_approval_history_approver_decided_at", columnList = "approver, decided_at")
        }
)
public class ApprovalHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_execution_id", nullable = false)
    private WorkflowExecution workflowExecution;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_state", nullable = false, length = 32)
    private WorkflowState previousState;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resulting_state", nullable = false, length = 32)
    private WorkflowState resultingState;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApprovalDecision decision;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String approver;

    @Size(max = 2_000)
    @Column(length = 2_000)
    private String comments;

    @NotNull
    @Column(name = "decided_at", nullable = false, updatable = false)
    private Instant decidedAt;

    protected ApprovalHistory() {
    }

    public ApprovalHistory(WorkflowExecution workflowExecution, WorkflowState previousState, WorkflowState resultingState,
                           ApprovalDecision decision, String approver, String comments, Instant decidedAt) {
        this.workflowExecution = workflowExecution;
        this.previousState = previousState;
        this.resultingState = resultingState;
        this.decision = decision;
        this.approver = approver;
        this.comments = comments;
        this.decidedAt = decidedAt;
    }

    @PrePersist
    void initializeDecidedAt() {
        if (decidedAt == null) {
            decidedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public WorkflowExecution getWorkflowExecution() {
        return workflowExecution;
    }

    public WorkflowState getPreviousState() {
        return previousState;
    }

    public WorkflowState getResultingState() {
        return resultingState;
    }

    public ApprovalDecision getDecision() {
        return decision;
    }

    public String getApprover() {
        return approver;
    }

    public String getComments() {
        return comments;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
