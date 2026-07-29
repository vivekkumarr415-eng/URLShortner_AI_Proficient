package com.example.urlshortener.service;

import com.example.urlshortener.agent.WorkflowAgent;
import com.example.urlshortener.dto.*;
import com.example.urlshortener.entity.*;
import com.example.urlshortener.exception.InvalidWorkflowStateException;
import com.example.urlshortener.exception.WorkflowNotFoundException;
import com.example.urlshortener.repository.*;
import com.example.urlshortener.workflow.WorkflowDependencyGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkflowEngine {
    private final WorkflowExecutionRepository workflows; private final WorkflowContextRepository context; private final ApprovalHistoryRepository approvals; private final DecisionHistoryRepository decisions; private final WorkflowAuditEventRepository audit; private final WorkflowDependencyGraph graph; private final Map<AgentType, WorkflowAgent> agents;
    public WorkflowEngine(WorkflowExecutionRepository workflows, WorkflowContextRepository context, ApprovalHistoryRepository approvals, DecisionHistoryRepository decisions, WorkflowAuditEventRepository audit, WorkflowDependencyGraph graph, List<WorkflowAgent> agents) {
        this.workflows=workflows; this.context=context; this.approvals=approvals; this.decisions=decisions; this.audit=audit; this.graph=graph; this.agents=agents.stream().collect(Collectors.toMap(WorkflowAgent::type, Function.identity()));
    }
    public WorkflowDetailsResponse start(WorkflowStartRequest request) {
        WorkflowExecution workflow=workflows.save(new WorkflowExecution(request.workflowName(), WorkflowState.RUNNING));
        if (request.context()!=null) request.context().forEach((key,value)->context.save(new WorkflowContextEntry(workflow,key,value)));
        record(workflow,"WORKFLOW_STARTED", "Initial plan created"); runUntilGate(workflow); return response(workflow);
    }
    @Transactional(readOnly=true) public WorkflowDetailsResponse get(UUID id) { WorkflowExecution workflow=find(id); return response(workflow); }
    public WorkflowDetailsResponse approve(UUID id, WorkflowApprovalRequest request) {
        WorkflowExecution workflow=find(id); require(workflow.getWorkflowState()==WorkflowState.AWAITING_APPROVAL,"Workflow is not awaiting approval");
        WorkflowState next=request.decision()==ApprovalDecision.APPROVED ? WorkflowState.RUNNING : request.decision()==ApprovalDecision.REWORK_REQUESTED ? WorkflowState.RUNNING : WorkflowState.REJECTED;
        approvals.save(new ApprovalHistory(workflow, WorkflowState.AWAITING_APPROVAL, next, request.decision(), request.approver(), request.comments(), null));
        if(request.decision()==ApprovalDecision.APPROVED){ workflow.advanceApprovalRound(); if(workflow.getApprovalRound()>=2){workflow.transitionTo(WorkflowState.COMPLETED,WorkflowStage.APPROVAL); record(workflow,"WORKFLOW_COMPLETED",request.comments());}else{workflow.transitionTo(WorkflowState.RUNNING,WorkflowStage.IMPLEMENTATION); runUntilGate(workflow);} }
        else if(request.decision()==ApprovalDecision.REWORK_REQUESTED){ workflow.replan(); record(workflow,"DYNAMIC_REPLAN",request.comments()); runUntilGate(workflow); }
        else { workflow.transitionTo(WorkflowState.SAFE_STOPPED,WorkflowStage.APPROVAL); record(workflow,"SAFE_STOP",request.comments()); }
        return response(workflow);
    }
    public WorkflowDetailsResponse retry(UUID id) { WorkflowExecution workflow=find(id); require(workflow.getWorkflowState()==WorkflowState.FAILED || workflow.getWorkflowState()==WorkflowState.SAFE_STOPPED,"Only failed or safely stopped workflows may be retried"); workflow.incrementRetryCount(); workflow.transitionTo(WorkflowState.RUNNING,workflow.getCurrentStage()); record(workflow,"RETRY", "Retry " + workflow.getRetryCount()); runUntilGate(workflow); return response(workflow); }
    public WorkflowDetailsResponse rollback(UUID id) { WorkflowExecution workflow=find(id); require(workflow.getWorkflowState()!=WorkflowState.COMPLETED,"Completed workflows cannot be rolled back"); workflow.transitionTo(WorkflowState.ROLLING_BACK,workflow.getCurrentStage()); record(workflow,"ROLLBACK_STARTED", "Rollback requested"); workflow.transitionTo(WorkflowState.SAFE_STOPPED,workflow.getCurrentStage()); record(workflow,"SAFE_STOP", "Rollback completed; workflow safely stopped"); return response(workflow); }
    private void runUntilGate(WorkflowExecution workflow) { while(workflow.getWorkflowState()==WorkflowState.RUNNING){ WorkflowStage stage=workflow.getCurrentStage(); if(stage==WorkflowStage.APPROVAL){ workflow.transitionTo(WorkflowState.AWAITING_APPROVAL,stage); record(workflow,"APPROVAL_GATE", "Human approval required"); return; } WorkflowAgent agent=agents.get(agentFor(stage)); if(agent==null) throw new IllegalStateException("No agent for " + stage); agent.execute(workflow); decisions.save(new DecisionHistory(workflow,stage,"COMPLETED","Dependencies satisfied: " + graph.dependenciesOf(stage))); record(workflow,"AGENT_COMPLETED",agent.type().name()); workflow.transitionTo(WorkflowState.RUNNING,graph.next(stage)); } }
    private AgentType agentFor(WorkflowStage stage) { return AgentType.valueOf(stage.name().equals("REQUIREMENTS") ? "REQUIREMENT" : stage.name().equals("ARCHITECTURE") ? "ARCHITECTURE" : stage.name()); }
    private WorkflowExecution find(UUID id){ return workflows.findById(id).orElseThrow(()->new WorkflowNotFoundException(id.toString())); }
    private void require(boolean condition,String message){ if(!condition) throw new InvalidWorkflowStateException(message); }
    private void record(WorkflowExecution workflow,String action,String details){ audit.save(new WorkflowAuditEvent(workflow,action,details)); }
    private WorkflowDetailsResponse response(WorkflowExecution workflow){ Map<String,String> values=context.findByWorkflowExecutionId(workflow.getId()).stream().collect(Collectors.toMap(WorkflowContextEntry::getKey,WorkflowContextEntry::getValue)); return new WorkflowDetailsResponse(workflow.getId(),workflow.getWorkflowName(),workflow.getWorkflowState(),workflow.getCurrentStage(),workflow.getPlanRevision(),workflow.getRetryCount(),workflow.getApprovalRound(),workflow.getCreatedAt(),workflow.getUpdatedAt(),values); }
}
