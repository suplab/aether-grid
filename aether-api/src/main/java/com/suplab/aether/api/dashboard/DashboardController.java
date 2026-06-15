package com.suplab.aether.api.dashboard;

import com.suplab.aether.agents.registry.AgentRegistry;
import com.suplab.aether.policy.audit.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private static final int SSE_TIMEOUT_MS = 30_000;

    private final DashboardStatsService statsService;
    private final AuditLogService auditLogService;
    private final AgentRegistry agentRegistry;

    public DashboardController(DashboardStatsService statsService,
                               AuditLogService auditLogService,
                               AgentRegistry agentRegistry) {
        this.statsService = statsService;
        this.auditLogService = auditLogService;
        this.agentRegistry = agentRegistry;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(statsService.getSystemStats());
    }

    @GetMapping("/decisions")
    public ResponseEntity<List<Map<String, Object>>> recentDecisions(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(statsService.getRecentDecisions(Math.min(limit, 100)));
    }

    @GetMapping("/memory-breakdown")
    public ResponseEntity<List<Map<String, Object>>> memoryBreakdown() {
        return ResponseEntity.ok(statsService.getMemoryTypeBreakdown());
    }

    @GetMapping("/agent-breakdown")
    public ResponseEntity<List<Map<String, Object>>> agentBreakdown() {
        return ResponseEntity.ok(statsService.getAgentDecisionBreakdown());
    }

    @GetMapping("/agents")
    public ResponseEntity<List<String>> listAgents() {
        return ResponseEntity.ok(agentRegistry.registeredTypes());
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        var emitter = new SseEmitter((long) SSE_TIMEOUT_MS);
        try {
            var stats = statsService.getSystemStats();
            emitter.send(SseEmitter.event().name("stats").data(stats));
            emitter.complete();
        } catch (Exception e) {
            log.warn("Dashboard SSE stream error: {}", e.getMessage());
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
