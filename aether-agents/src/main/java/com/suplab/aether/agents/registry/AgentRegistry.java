package com.suplab.aether.agents.registry;

import com.suplab.aether.agents.spi.Agent;
import com.suplab.aether.agents.spi.AgentCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AgentRegistry {

    private static final Logger log = LoggerFactory.getLogger(AgentRegistry.class);

    private final List<Agent> agents;
    private final Set<String> disabledAgents = ConcurrentHashMap.newKeySet();

    public AgentRegistry(List<Agent> agents) {
        this.agents = List.copyOf(agents);
        log.info("AgentRegistry initialised with {} agent(s): {}",
                agents.size(),
                agents.stream().map(Agent::agentType).toList());
    }

    public List<Agent> findByCapability(AgentCapability capability) {
        return agents.stream()
                .filter(Agent::isEnabled)
                .filter(a -> !disabledAgents.contains(a.agentType()))
                .filter(a -> a.supports(capability))
                .toList();
    }

    public Optional<Agent> findByType(String agentType) {
        return agents.stream()
                .filter(a -> a.agentType().equals(agentType))
                .findFirst();
    }

    public Map<String, Set<AgentCapability>> agentCapabilityMap() {
        return agents.stream()
                .collect(Collectors.toMap(Agent::agentType, Agent::capabilities));
    }

    public void disableAgent(String agentType) {
        disabledAgents.add(agentType);
        log.warn("Agent kill-switch activated: {} is now disabled", agentType);
    }

    public void enableAgent(String agentType) {
        disabledAgents.remove(agentType);
        log.info("Agent {} re-enabled", agentType);
    }

    public boolean isEnabled(String agentType) {
        return !disabledAgents.contains(agentType);
    }
}
