import { describe, it, expect } from 'vitest'
import { getTerrainHeight, getEffectiveSeason } from './terrainUtils'

describe('terrainUtils', () => {
    it('calculates ground elevation with slopes and base altitude', () => {
        const flatHeight = getTerrainHeight(50, 50, { baseElevation: 0, slopeX: 0, slopeZ: 0, hasRiver: false })
        expect(flatHeight).toBe(0)

        const elevatedHeight = getTerrainHeight(50, 50, { baseElevation: 10, slopeX: 0, slopeZ: 0, hasRiver: false })
        expect(elevatedHeight).toBe(10)

        const slopedHeight = getTerrainHeight(70, 50, { baseElevation: 0, slopeX: 0.1, slopeZ: 0, hasRiver: false })
        expect(slopedHeight).toBeCloseTo(2.0, 5)
    })

    it('inverts seasons properly for Southern Hemisphere', () => {
        expect(getEffectiveSeason('SUMMER', 'NORTHERN')).toBe('SUMMER')
        expect(getEffectiveSeason('WINTER', 'NORTHERN')).toBe('WINTER')
        expect(getEffectiveSeason('SUMMER', 'SOUTHERN')).toBe('WINTER')
        expect(getEffectiveSeason('WINTER', 'SOUTHERN')).toBe('SUMMER')
        expect(getEffectiveSeason('SPRING', 'SOUTHERN')).toBe('AUTUMN')
        expect(getEffectiveSeason('AUTUMN', 'SOUTHERN')).toBe('SPRING')
    })
})
