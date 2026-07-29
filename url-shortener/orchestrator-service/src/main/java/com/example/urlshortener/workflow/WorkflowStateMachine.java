package com.example.urlshortener.workflow;
import com.example.urlshortener.entity.WorkflowState;
import java.util.Set;
import org.springframework.stereotype.Component;
@Component public class WorkflowStateMachine {
    public boolean canTransition(WorkflowState from, WorkflowState to) {
        if (from == to) return true;
        return switch (from) { case PENDING -> Set.of(WorkflowState.RUNNING, WorkflowState.CANCELLED).contains(to); case RUNNING -> Set.of(WorkflowState.AWAITING_APPROVAL, WorkflowState.FAILED, WorkflowState.ROLLING_BACK).contains(to); case AWAITING_APPROVAL -> Set.of(WorkflowState.RUNNING, WorkflowState.COMPLETED, WorkflowState.SAFE_STOPPED).contains(to); case FAILED, SAFE_STOPPED -> to == WorkflowState.RUNNING || to == WorkflowState.ROLLING_BACK; case ROLLING_BACK -> to == WorkflowState.SAFE_STOPPED; default -> false; };
    }
}
