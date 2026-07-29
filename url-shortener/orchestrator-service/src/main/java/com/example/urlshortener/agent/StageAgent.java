package com.example.urlshortener.agent;
import com.example.urlshortener.entity.AgentType;
import com.example.urlshortener.entity.WorkflowExecution;
abstract class StageAgent implements WorkflowAgent { private final AgentType type; StageAgent(AgentType type) { this.type = type; } public AgentType type() { return type; } public void execute(WorkflowExecution workflow) { } }
