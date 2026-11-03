/**
 * SwarmForge Web - Initial Entity Spawners
 * Generates initial colonies, food sources, and nest topologies from scenario species cards.
 */

export function generateInitialColonies(speciesCards) {
    if (!speciesCards || speciesCards.length === 0) return []
    return speciesCards.map(c => ({
        id: c.id,
        name: c.name,
        speciesId: c.speciesId,
        color: c.color,
        food: 0,
        water: 0,
        protein: 0,
        population: (c.initialQueens || 0) + (c.initialWorkers || 0) + (c.initialSoldiers || 0) + (c.initialMales || 0),
        queens: c.initialQueens || 0,
        workers: c.initialWorkers || 0,
        soldiers: c.initialSoldiers || 0,
        males: c.initialMales || 0
    }))
}

export function generateInitialFoodSources() {
    return []
}

export function generateInitialNests(speciesCards) {
    if (!speciesCards || speciesCards.length === 0) return []
    return speciesCards.map((c, idx) => ({
        id: `nest_${c.id}`,
        colonyId: c.id,
        name: `Nid de ${c.name}`,
        x: idx === 0 ? 35 : (35 + idx * 30),
        y: idx === 0 ? 35 : (35 + idx * 30),
        z: 0,
        nestType: c.nestType || 'DOME_AND_SUBTERRANEAN',
        scale: 1.0,
        color: c.color,
        chambers: [],
        tunnels: []
    }))
}
