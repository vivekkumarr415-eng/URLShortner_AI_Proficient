package com.example.urlshortener.agent;

import com.example.urlshortener.entity.AgentType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class WorkflowAgentsTest {
    @Test void exposesDedicatedAgentTypes() {
        assertThat(new RequirementAgent().type()).isEqualTo(AgentType.REQUIREMENT);
        assertThat(new ReviewerAgent().type()).isEqualTo(AgentType.REVIEW);
        assertThat(new ApprovalAgent().type()).isEqualTo(AgentType.APPROVAL);
    }
}
