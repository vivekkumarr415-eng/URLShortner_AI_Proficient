package com.example.urlshortener.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "decision_history", indexes = @Index(name = "idx_decision_history_workflow_decided_at", columnList = "workflow_execution_id, decided_at"))
public class DecisionHistory {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "workflow_execution_id") private WorkflowExecution workflowExecution;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private WorkflowStage stage;
    @Column(nullable = false, length = 120) private String decision;
    @Column(length = 2000) private String rationale;
    @Column(name = "decided_at", nullable = false) private Instant decidedAt;
    protected DecisionHistory() { }
    public DecisionHistory(WorkflowExecution workflowExecution, WorkflowStage stage, String decision, String rationale) { this.workflowExecution = workflowExecution; this.stage = stage; this.decision = decision; this.rationale = rationale; this.decidedAt = Instant.now(); }
}
