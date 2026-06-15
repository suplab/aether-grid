package com.suplab.aether.core.domain;

public final class Tenant {

    private final TenantId id;
    private final String name;
    private final String apiKeyHash;
    private TenantStatus status;
    private boolean memoryOptOut;

    private Tenant(TenantId id, String name, String apiKeyHash) {
        this.id = id;
        this.name = name;
        this.apiKeyHash = apiKeyHash;
        this.status = TenantStatus.ACTIVE;
        this.memoryOptOut = false;
    }

    public static Tenant onboard(String name, String apiKeyHash) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Tenant name must not be blank");
        if (apiKeyHash == null || apiKeyHash.isBlank()) throw new IllegalArgumentException("apiKeyHash must not be blank");
        var tenant = new Tenant(TenantId.generate(), name, apiKeyHash);
        tenant.memoryOptOut = false;
        return tenant;
    }

    public static Tenant reconstitute(TenantId id, String name, String apiKeyHash, TenantStatus status) {
        var tenant = new Tenant(id, name, apiKeyHash);
        tenant.status = status;
        tenant.memoryOptOut = false;
        return tenant;
    }

    public static Tenant reconstitute(TenantId id, String name, String apiKeyHash, TenantStatus status, boolean memoryOptOut) {
        var tenant = new Tenant(id, name, apiKeyHash);
        tenant.status = status;
        tenant.memoryOptOut = memoryOptOut;
        return tenant;
    }

    public void suspend() {
        if (status == TenantStatus.DEPROVISIONED) throw new IllegalStateException("Cannot suspend a deprovisioned tenant");
        this.status = TenantStatus.SUSPENDED;
    }

    public void reactivate() {
        if (status == TenantStatus.DEPROVISIONED) throw new IllegalStateException("Cannot reactivate a deprovisioned tenant");
        this.status = TenantStatus.ACTIVE;
    }

    public void deprovision() {
        this.status = TenantStatus.DEPROVISIONED;
    }

    public void optOutOfMemory() {
        this.memoryOptOut = true;
    }

    public void optIntoMemory() {
        this.memoryOptOut = false;
    }

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public TenantId id() { return id; }
    public String name() { return name; }
    public String apiKeyHash() { return apiKeyHash; }
    public TenantStatus status() { return status; }
    public boolean memoryOptOut() { return memoryOptOut; }
}
