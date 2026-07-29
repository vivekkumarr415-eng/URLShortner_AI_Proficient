package com.example.urlshortener.repository;

import com.example.urlshortener.entity.ApprovalDecision;
import com.example.urlshortener.entity.ApprovalHistory;
import com.example.urlshortener.entity.WorkflowExecution;
import com.example.urlshortener.entity.WorkflowState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ApprovalHistoryRepositoryTest {

    @Autowired
    private WorkflowExecutionRepository workflowExecutionRepository;

    @Autowired
    private ApprovalHistoryRepository approvalHistoryRepository;

    @Test
    void findsApprovalHistoryForWorkflowInDecisionOrder() {
        WorkflowExecution execution = workflowExecutionRepository.saveAndFlush(
                new WorkflowExecution("database-entity-milestone", WorkflowState.AWAITING_APPROVAL)
        );
        ApprovalHistory history = approvalHistoryRepository.saveAndFlush(new ApprovalHistory(
                execution,
                WorkflowState.RUNNING,
                WorkflowState.AWAITING_APPROVAL,
                ApprovalDecision.REWORK_REQUESTED,
                "principal-architect",
                "Add repository tests before approval.",
                Instant.now().minusSeconds(10)
        ));

        assertThat(approvalHistoryRepository.findByWorkflowExecutionIdOrderByDecidedAtAsc(execution.getId()))
                .extracting(ApprovalHistory::getId, ApprovalHistory::getApprover, ApprovalHistory::getDecision)
                .containsExactly(tuple(history.getId(), "principal-architect", ApprovalDecision.REWORK_REQUESTED));
    }

    private static org.assertj.core.groups.Tuple tuple(Object... values) {
        return org.assertj.core.groups.Tuple.tuple(values);
    }
}
