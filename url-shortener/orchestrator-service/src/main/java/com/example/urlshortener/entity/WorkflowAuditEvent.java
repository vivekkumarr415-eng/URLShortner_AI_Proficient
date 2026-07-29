package com.example.urlshortener.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_audit_events", indexes = @Index(name = "idx_workflow_audit_workflow_occurred_at", columnList = "workflow_execution_id, occurred_at"))
public class WorkflowAuditEvent {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "workflow_execution_id") private WorkflowExecution workflowExecution;
    @Column(nullable = false, length = 80) private String action;
    @Column(length = 2000) private String details;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    protected WorkflowAuditEvent() { }
    public WorkflowAuditEvent(WorkflowExecution workflowExecution, String action, String details) { this.workflowExecution = workflowExecution; this.action = action; this.details = details; this.occurredAt = Instant.now(); }
}
