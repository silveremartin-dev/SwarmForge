package org.swarmforge.client.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Immutable snapshot of an accessory species configuration (aphids, parasites, fungal cultivars).
 */
public record AccessoryConfigSnapshot(
    String name,
    String role,
    boolean enabled,
    int initialCount,
    String renewalStrategy
) {}
