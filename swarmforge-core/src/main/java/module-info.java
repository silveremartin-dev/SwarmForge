/*
 * SwarmForge Core Module
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
module org.swarmforge.core {
    requires java.base;
    requires java.logging;

    exports org.swarmforge.core.domain;
    exports org.swarmforge.core.simulation;
    exports org.swarmforge.core.spatial;
    exports org.swarmforge.core.species;
    exports org.swarmforge.core.gpu;
    exports org.swarmforge.core.world;
}
