package com.example.urlshortener.repository;
import com.example.urlshortener.entity.WorkflowAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface WorkflowAuditEventRepository extends JpaRepository<WorkflowAuditEvent, UUID> { }
