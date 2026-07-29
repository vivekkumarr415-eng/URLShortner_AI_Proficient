package com.example.urlshortener.agent;
import com.example.urlshortener.entity.AgentType;
import com.example.urlshortener.entity.WorkflowExecution;
public interface WorkflowAgent { AgentType type(); void execute(WorkflowExecution workflow); }
