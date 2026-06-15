package com.suplab.aether.api.controller;

import com.suplab.aether.api.dto.FeedbackRequest;
import com.suplab.aether.core.domain.AgentFeedback;
import com.suplab.aether.core.domain.DecisionOutcome;
import com.suplab.aether.core.domain.TenantId;
import com.suplab.aether.core.ports.AgentFeedbackPort;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/agents")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final AgentFeedbackPort feedbackPort;

    public AgentController(AgentFeedbackPort feedbackPort) {
        this.feedbackPort = feedbackPort;
    }

    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> recordFeedback(
            @PathVariable UUID tenantId,
            @RequestBody @Valid FeedbackRequest request) {
        var outcome = DecisionOutcome.valueOf(request.outcome().toUpperCase());
        var feedback = AgentFeedback.create(
                TenantId.of(tenantId.toString()),
                request.agentType(),
                request.decisionId(),
                request.originalDecision(),
                request.originalConfidence(),
                outcome,
                request.outcomeDetail()
        );
        feedbackPort.record(feedback);
        log.info("Recorded agent feedback id={} agentType={} outcome={} tenant={}",
                feedback.id(), feedback.agentType(), feedback.outcome(), tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("feedbackId", feedback.id().toString()));
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformance(@PathVariable UUID tenantId) {
        var stats = feedbackPort.getPerformanceStats(TenantId.of(tenantId.toString()));
        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId.toString(),
                "agents", stats
        ));
    }
}
