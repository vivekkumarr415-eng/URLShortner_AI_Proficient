package com.example.urlshortener.workflow;

import com.example.urlshortener.entity.WorkflowStage;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class WorkflowDependencyGraphTest {
    private final WorkflowDependencyGraph graph = new WorkflowDependencyGraph();
    @Test void ordersDeliveryStagesByDependency() {
        assertThat(graph.dependenciesOf(WorkflowStage.IMPLEMENTATION)).containsExactly(WorkflowStage.ARCHITECTURE);
        assertThat(graph.next(WorkflowStage.REVIEW)).isEqualTo(WorkflowStage.APPROVAL);
    }
}
