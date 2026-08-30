/**
 * SwarmForge Terrain & Climate Utils
 * 
 * Provides:
 * 1. `getTerrainHeight(x, z, terrainConfig)`: Calculates exact ground surface altitude Y
 *    for any (x, z) coordinates, supporting sloped ground (incline along X/Z), base elevation offsets
 *    (e.g., Y=0, Y=10, Y=-5), and river depressions.
 * 2. `getEffectiveSeason(season, hemisphere)`: Inverts seasons when hemisphere is 'SOUTHERN'.
 */

/**
 * Computes exact ground surface altitude Y at coordinates (x, z).
 * 
 * @param {number} x - X coordinate in meters (0 to 100)
 * @param {number} z - Z coordinate in meters (0 to 100)
 * @param {object} terrainConfig - Elevation, slope incline, and river parameters
 * @returns {number} Ground altitude Y in meters
 */
export function getTerrainHeight(x, z, terrainConfig = {}) {
    const {
        baseElevation = 0,  // Base ground altitude (Y=0, Y=10, Y=-5)
        slopeX = 0,         // Ground slope incline along X axis (m/m)
        slopeZ = 0,         // Ground slope incline along Z axis (m/m)
        roughness = 0,      // Micro-relief roughness height
        hasRiver = true,    // River depression channel
        riverX = 25,        // River X center
        riverWidth = 10,    // River width in meters
        riverDepth = 0.6    // River bed depth
    } = terrainConfig

    // 1. Base Altitude + Sloped Elevation Incline
    let y = baseElevation + (x - 50) * slopeX + (z - 50) * slopeZ

    // 2. Micro-relief roughness elevation ripple
    if (roughness > 0) {
        y += Math.sin(x * 0.12) * Math.cos(z * 0.12) * roughness * 1.2
    }

    // 3. Parabolic River Bed Trough
    if (hasRiver) {
        const distToRiver = Math.abs(x - riverX)
        if (distToRiver < riverWidth / 2) {
            const factor = 1 - Math.pow(distToRiver / (riverWidth / 2), 2)
            y -= riverDepth * factor
        }
    }

    return y
}

/**
 * Calculates effective season considering Northern vs Southern Hemisphere.
 * In the Southern Hemisphere, seasons invert (Winter <-> Summer, Spring <-> Autumn).
 * 
 * @param {string} season - Nominal season ('SPRING' | 'SUMMER' | 'AUTUMN' | 'WINTER')
 * @param {string} hemisphere - Hemisphere ('NORTHERN' | 'SOUTHERN')
 * @returns {string} Effective seasonal phase for rendering and climate physics
 */
export function getEffectiveSeason(season = 'SUMMER', hemisphere = 'NORTHERN') {
    if (hemisphere === 'SOUTHERN') {
        switch (season) {
            case 'WINTER': return 'SUMMER'
            case 'SUMMER': return 'WINTER'
            case 'SPRING': return 'AUTUMN'
            case 'AUTUMN': return 'SPRING'
            default: return season
        }
    }
    return season
}
