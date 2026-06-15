package com.suplab.aether.agents.orchestrator;

import com.suplab.aether.agents.registry.AgentRegistry;
import com.suplab.aether.agents.spi.AgentCapability;
import com.suplab.aether.agents.spi.AgentDecision;
import com.suplab.aether.agents.spi.AgentInput;
import com.suplab.aether.agents.spi.AgentOutput;
import com.suplab.aether.core.exception.AgentException;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);
    private static final int MAX_ITERATIONS = 5;

    private final AgentRegistry registry;
    private final MeterRegistry meterRegistry;
    private final ExecutorService parallelPool;

    public AgentOrchestrator(AgentRegistry registry, MeterRegistry meterRegistry) {
        this.registry = registry;
        this.meterRegistry = meterRegistry;
        this.parallelPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    public OrchestrationResult orchestrate(AgentInput input) {
        var agents = registry.findByCapability(input.capability());
        if (agents.isEmpty()) {
            log.warn("No agents registered for capability={}", input.capability());
            return OrchestrationResult.noAgents(input.callId(), input.capability());
        }

        var outputs = new ArrayList<AgentOutput>();
        int iterations = 0;

        for (var agent : agents) {
            if (iterations >= MAX_ITERATIONS) {
                log.warn("Max iteration limit ({}) reached for capability={}", MAX_ITERATIONS, input.capability());
                break;
            }
            try {
                long startMs = System.currentTimeMillis();
                var output = agent.execute(input);
                long durationMs = System.currentTimeMillis() - startMs;

                outputs.add(output);
                log.info("Agent {} produced decision={} confidence={} autoEnforced={}",
                        agent.agentType(), output.decision(), output.confidence(), output.autoEnforced());

                var agentType = agent.agentType();
                var decision = output.decision().name();
                meterRegistry.counter("aether.agent.executions", "agent", agentType, "decision", decision).increment();
                meterRegistry.timer("aether.agent.latency", "agent", agentType).record(durationMs, TimeUnit.MILLISECONDS);

                iterations++;
            } catch (Exception e) {
                log.error("Agent {} threw exception: {}", agent.agentType(), e.getMessage());
                throw new AgentException(agent.agentType(), "execution failed", e);
            }
        }

        return OrchestrationResult.of(input.callId(), input.capability(), outputs);
    }

    public List<AgentOutput> orchestrateParallel(List<AgentInput> inputs) {
        var futures = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> orchestrate(input), parallelPool))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .map(OrchestrationResult::outputs)
                .flatMap(List::stream)
                .toList();
    }
}
