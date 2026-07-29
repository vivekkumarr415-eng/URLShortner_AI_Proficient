package com.example.urlshortener.workflow;
import com.example.urlshortener.entity.WorkflowStage;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
@Component
public class WorkflowDependencyGraph {
    private final Map<WorkflowStage, List<WorkflowStage>> dependencies = Map.of(
            WorkflowStage.REQUIREMENTS, List.of(), WorkflowStage.PLANNING, List.of(WorkflowStage.REQUIREMENTS),
            WorkflowStage.ARCHITECTURE, List.of(WorkflowStage.PLANNING), WorkflowStage.IMPLEMENTATION, List.of(WorkflowStage.ARCHITECTURE),
            WorkflowStage.TESTING, List.of(WorkflowStage.IMPLEMENTATION), WorkflowStage.DOCUMENTATION, List.of(WorkflowStage.TESTING),
            WorkflowStage.REVIEW, List.of(WorkflowStage.DOCUMENTATION), WorkflowStage.APPROVAL, List.of());
    public List<WorkflowStage> dependenciesOf(WorkflowStage stage) { return dependencies.get(stage); }
    public WorkflowStage next(WorkflowStage stage) { return switch (stage) { case REQUIREMENTS -> WorkflowStage.PLANNING; case PLANNING -> WorkflowStage.ARCHITECTURE; case ARCHITECTURE -> WorkflowStage.APPROVAL; case IMPLEMENTATION -> WorkflowStage.TESTING; case TESTING -> WorkflowStage.DOCUMENTATION; case DOCUMENTATION -> WorkflowStage.REVIEW; case REVIEW -> WorkflowStage.APPROVAL; case APPROVAL -> WorkflowStage.IMPLEMENTATION; }; }
}
