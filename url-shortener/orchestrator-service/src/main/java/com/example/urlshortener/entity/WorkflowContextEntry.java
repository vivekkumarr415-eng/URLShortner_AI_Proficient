package com.example.urlshortener.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_context", uniqueConstraints = @UniqueConstraint(name = "uk_workflow_context_key", columnNames = {"workflow_execution_id", "context_key"}))
public class WorkflowContextEntry {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "workflow_execution_id") private WorkflowExecution workflowExecution;
    @Column(name = "context_key", nullable = false, length = 120) private String key;
    @Column(name = "context_value", nullable = false, length = 4000) private String value;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected WorkflowContextEntry() { }
    public WorkflowContextEntry(WorkflowExecution workflowExecution, String key, String value) { this.workflowExecution = workflowExecution; this.key = key; this.value = value; this.updatedAt = Instant.now(); }
    public String getKey() { return key; }
    public String getValue() { return value; }
}
