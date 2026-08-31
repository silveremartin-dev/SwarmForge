package org.swarmforge.client.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccessoryConfigSnapshot(
    String name,
    String role,
    boolean enabled,
    int initialCount,
    String renewalStrategy
) {}
