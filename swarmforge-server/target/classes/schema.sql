-- SwarmForge Database Schema
-- Copyright (c) 2022-2025 Silvère Martin-Michiellot
-- AI Assistant: Gemini (Google DeepMind)
-- MIT License

-- Worlds/Terrariums
CREATE TABLE IF NOT EXISTS worlds (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    depth INT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    altitude DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Colonies
CREATE TABLE IF NOT EXISTS colonies (
    id UUID PRIMARY KEY,
    world_id UUID REFERENCES worlds(id) ON DELETE CASCADE,
    species VARCHAR(255) NOT NULL,
    nest_x REAL NOT NULL,
    nest_y REAL NOT NULL,
    nest_z REAL NOT NULL,
    food_stored REAL DEFAULT 0,
    water_stored REAL DEFAULT 0,
    total_born INT DEFAULT 0,
    total_died INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Simulation Checkpoints (for save/load)
CREATE TABLE IF NOT EXISTS checkpoints (
    id UUID PRIMARY KEY,
    world_id UUID REFERENCES worlds(id) ON DELETE CASCADE,
    tick BIGINT NOT NULL,
    name VARCHAR(255),
    cells_data BYTEA,           -- Compressed cell data
    colonies_data BYTEA,        -- Compressed colony data
    individuals_data BYTEA,     -- Compressed individual data
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Species Definitions
CREATE TABLE IF NOT EXISTS species (
    id UUID PRIMARY KEY,
    scientific_name VARCHAR(255) UNIQUE NOT NULL,
    common_name VARCHAR(255),
    worker_lifespan INT,
    queen_lifespan INT,
    worker_speed REAL,
    view_distance REAL,
    typical_colony_size INT,
    forms_mega_colonies BOOLEAN DEFAULT FALSE,
    config_json JSONB
);

-- Simulation Statistics
CREATE TABLE IF NOT EXISTS statistics (
    id SERIAL PRIMARY KEY,
    world_id UUID REFERENCES worlds(id) ON DELETE CASCADE,
    tick BIGINT NOT NULL,
    total_population INT,
    total_colonies INT,
    total_food_collected REAL,
    average_colony_size REAL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_checkpoints_world ON checkpoints(world_id);
CREATE INDEX idx_colonies_world ON colonies(world_id);
CREATE INDEX idx_statistics_world_tick ON statistics(world_id, tick);
