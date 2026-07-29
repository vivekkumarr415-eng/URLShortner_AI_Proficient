package com.example.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "workflow_executions",
        indexes = {
                @Index(name = "idx_workflow_executions_state_updated_at", columnList = "workflow_state, updated_at"),
                @Index(name = "idx_workflow_executions_created_at", columnList = "created_at")
        }
)
public class WorkflowExecution {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "workflow_name", nullable = false, length = 120)
    private String workflowName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_state", nullable = false, length = 32)
    private WorkflowState workflowState;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 32)
    private WorkflowStage currentStage;

    @Column(name = "plan_revision", nullable = false)
    private int planRevision;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "approval_round", nullable = false)
    private int approvalRound;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "workflowExecution")
    private List<ApprovalHistory> approvalHistory = new ArrayList<>();

    protected WorkflowExecution() {
    }

    public WorkflowExecution(String workflowName, WorkflowState workflowState) {
        this.workflowName = workflowName;
        this.workflowState = workflowState;
        this.currentStage = WorkflowStage.REQUIREMENTS;
        this.planRevision = 1;
    }

    @PrePersist
    void initializeTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public WorkflowState getWorkflowState() {
        return workflowState;
    }

    public WorkflowStage getCurrentStage() { return currentStage; }
    public int getPlanRevision() { return planRevision; }
    public int getRetryCount() { return retryCount; }
    public int getApprovalRound() { return approvalRound; }

    public void transitionTo(WorkflowState state, WorkflowStage stage) {
        this.workflowState = state;
        this.currentStage = stage;
    }

    public void incrementRetryCount() { retryCount++; }
    public void replan() { planRevision++; approvalRound = 0; currentStage = WorkflowStage.PLANNING; workflowState = WorkflowState.RUNNING; }
    public void advanceApprovalRound() { approvalRound++; }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<ApprovalHistory> getApprovalHistory() {
        return List.copyOf(approvalHistory);
    }
}
