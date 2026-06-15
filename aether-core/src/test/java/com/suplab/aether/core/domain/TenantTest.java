package com.suplab.aether.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantTest {

    @Test
    void onboard_createsActiveTenant() {
        var tenant = Tenant.onboard("Acme Corp", "sha256hash");

        assertThat(tenant.id()).isNotNull();
        assertThat(tenant.name()).isEqualTo("Acme Corp");
        assertThat(tenant.isActive()).isTrue();
        assertThat(tenant.status()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void suspend_changesStatusToSuspended() {
        var tenant = Tenant.onboard("Acme Corp", "sha256hash");
        tenant.suspend();

        assertThat(tenant.isActive()).isFalse();
        assertThat(tenant.status()).isEqualTo(TenantStatus.SUSPENDED);
    }

    @Test
    void reactivate_restoresActiveStatus() {
        var tenant = Tenant.onboard("Acme Corp", "sha256hash");
        tenant.suspend();
        tenant.reactivate();

        assertThat(tenant.isActive()).isTrue();
    }

    @Test
    void deprovision_preventsSubsequentStateChanges() {
        var tenant = Tenant.onboard("Acme Corp", "sha256hash");
        tenant.deprovision();

        assertThatThrownBy(tenant::suspend)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deprovisioned");

        assertThatThrownBy(tenant::reactivate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deprovisioned");
    }

    @Test
    void onboard_rejectsBlankName() {
        assertThatThrownBy(() -> Tenant.onboard("  ", "hash"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }
}
