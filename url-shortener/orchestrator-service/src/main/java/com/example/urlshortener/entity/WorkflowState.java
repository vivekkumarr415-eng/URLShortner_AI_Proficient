package com.example.urlshortener.entity;

public enum WorkflowState {
    PENDING,
    RUNNING,
    AWAITING_APPROVAL,
    APPROVED,
    REJECTED,
    COMPLETED,
    FAILED,
    CANCELLED,
    ROLLING_BACK,
    SAFE_STOPPED
}
