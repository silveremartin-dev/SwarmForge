/**
 * SwarmForge Terrain & Climate Utils
 * 
 * Provides:
 * 1. `getTerrainHeight(x, z, terrainConfig)`: Calculates realistic organic 3D terrain elevation Y
 *    with multi-octave relief, hills, river valley channel, sandbanks, and rocky knolls.
 * 2. `getSubstrateAt(x, y, z, terrainConfig)`: Returns the geological substrate layer name & color at (x, y, z).
 * 3. `getEffectiveSeason(season, hemisphere)`: Inverts seasons when hemisphere is 'SOUTHERN'.
 */

/**
 * Computes exact ground surface altitude Y at coordinates (x, z).
 * 
 * @param {number} x - X coordinate in meters (0 to 100)
 * @param {number} z - Z coordinate in meters (0 to 100)
 * @param {object} terrainConfig - Elevation, roughness, and river parameters
 * @returns {number} Ground altitude Y in meters
 */
export function getTerrainHeight(x, z, terrainConfig = {}) {
    const {
        baseElevation = 0,
        slopeX = 0,
        slopeZ = 0,
        roughness = 0,
        hasRiver = true,
        riverX = 25,
        riverWidth = 12,
        riverDepth = 0.8
    } = terrainConfig

    // 1. Base Altitude + Sloped Elevation
    let y = baseElevation + (x - 50) * slopeX + (z - 50) * slopeZ

    // 2. Multi-octave natural rolling hills and relief
    const r = typeof roughness === 'number' ? roughness : 0.5
    if (r > 0) {
        // Macro hills (Low frequency, high amplitude)
        y += (Math.sin(x * 0.05 + 0.3) * Math.cos(z * 0.05 + 0.7) * 2.2 + 
              Math.sin(x * 0.08 - z * 0.06) * 1.2) * r

        // Meso knolls and ridges (Medium frequency)
        y += (Math.sin(x * 0.15 + z * 0.12) * 0.5 + Math.cos(x * 0.22) * 0.3) * r
    }

    // 3. Parabolic River Bed Trough with natural river valley smoothing
    if (hasRiver) {
        const distToRiver = Math.abs(x - riverX)
        if (distToRiver < riverWidth / 2) {
            const factor = 1 - Math.pow(distToRiver / (riverWidth / 2), 2)
            y -= riverDepth * factor
        } else if (distToRiver < riverWidth) {
            // River valley depression
            const valleyFactor = (1 - (distToRiver - riverWidth / 2) / (riverWidth / 2)) * 0.3
            y -= valleyFactor
        }
    }

    return y
}

/**
 * Determines geological soil substrate layer at given world coordinates.
 */
export function getSubstrateAt(x, y, z, terrainConfig = {}) {
    const surfaceY = getTerrainHeight(x, z, terrainConfig)
    const depth = surfaceY - y // Positive underground

    const hasRiver = terrainConfig?.hasRiver ?? true
    const riverX = terrainConfig?.riverX ?? 25
    const riverWidth = terrainConfig?.riverWidth ?? 12
    const distToRiver = Math.abs(x - riverX)

    if (hasRiver && distToRiver < riverWidth / 2 && y <= 0.05) {
        return { name: 'Eau Fluviale', color: '#0284c7', type: 'WATER', desc: 'Courant alluvial dynamique' }
    }
    if (hasRiver && distToRiver >= riverWidth / 2 && distToRiver < (riverWidth / 2 + 3.0) && depth < 0.3) {
        return { name: 'Sable Fin Alluvial', color: '#ca8a04', type: 'SAND', desc: 'Berge perméable et meuble' }
    }

    if (depth <= 0.3) {
        return { name: 'Humus Organique', color: '#452b18', type: 'HUMUS', desc: 'Litière forestière superficielle' }
    } else if (depth <= 1.8) {
        return { name: 'Terre & Argile Compacte', color: '#854d0e', type: 'CLAY', desc: 'Strate propice aux galeries et chambres' }
    } else if (depth <= 3.2) {
        return { name: 'Limon & Nappe Phréatique', color: '#0284c7', type: 'LIMON', desc: 'Substrat humide drainant' }
    } else {
        return { name: 'Roche-Mère (Socle)', color: '#334155', type: 'BEDROCK', desc: 'Socle granitique infranchissable' }
    }
}

/**
 * Calculates effective season considering Northern vs Southern Hemisphere.
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
