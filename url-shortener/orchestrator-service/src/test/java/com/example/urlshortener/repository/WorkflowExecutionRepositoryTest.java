package com.example.urlshortener.repository;

import com.example.urlshortener.entity.WorkflowExecution;
import com.example.urlshortener.entity.WorkflowState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WorkflowExecutionRepositoryTest {

    @Autowired
    private WorkflowExecutionRepository workflowExecutionRepository;

    @Test
    void findsWorkflowExecutionsByState() {
        WorkflowExecution pending = workflowExecutionRepository.saveAndFlush(
                new WorkflowExecution("database-entity-milestone", WorkflowState.PENDING)
        );
        workflowExecutionRepository.saveAndFlush(
                new WorkflowExecution("completed-milestone", WorkflowState.COMPLETED)
        );

        assertThat(workflowExecutionRepository.findByWorkflowStateOrderByUpdatedAtAsc(WorkflowState.PENDING))
                .extracting(WorkflowExecution::getId, WorkflowExecution::getWorkflowName)
                .containsExactly(tuple(pending.getId(), "database-entity-milestone"));
    }

    private static org.assertj.core.groups.Tuple tuple(Object... values) {
        return org.assertj.core.groups.Tuple.tuple(values);
    }
}
