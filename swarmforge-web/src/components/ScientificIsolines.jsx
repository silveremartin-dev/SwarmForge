import React, { useMemo } from 'react'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import { getTerrainHeight } from '../utils/terrainUtils'

/**
 * Robust 2D Marching Squares segment generator.
 * Computes 3D line segment pairs on a regular 2D grid for given isovalues.
 */
function generateMarchingSquares(grid2D, xCoords, zCoords, isoLevels, heightFn, terrainConfig, yOffset = 0.05) {
    const nx = xCoords.length
    const nz = zCoords.length
    const positions = []
    const colors = []

    const interp = (pA, pB, vA, vB, C) => {
        const denom = vB - vA
        const t = Math.abs(denom) > 1e-6 ? (C - vA) / denom : 0.5
        const clampedT = Math.max(0, Math.min(1, t))
        return {
            x: pA.x + clampedT * (pB.x - pA.x),
            z: pA.z + clampedT * (pB.z - pA.z)
        }
    }

    const addSegment = (pt1, pt2, colorHex) => {
        const y1 = heightFn(pt1.x, pt1.z, terrainConfig) + yOffset
        const y2 = heightFn(pt2.x, pt2.z, terrainConfig) + yOffset
        
        positions.push(pt1.x, y1, pt1.z)
        positions.push(pt2.x, y2, pt2.z)

        const c = new THREE.Color(colorHex)
        colors.push(c.r, c.g, c.b)
        colors.push(c.r, c.g, c.b)
    }

    // Process each grid cell
    for (let i = 0; i < nx - 1; i++) {
        const x0 = xCoords[i]
        const x1 = xCoords[i + 1]

        for (let j = 0; j < nz - 1; j++) {
            const z0 = zCoords[j]
            const z1 = zCoords[j + 1]

            const p0 = { x: x0, z: z0 }
            const p1 = { x: x1, z: z0 }
            const p2 = { x: x1, z: z1 }
            const p3 = { x: x0, z: z1 }

            const v0 = grid2D[i][j]
            const v1 = grid2D[i + 1][j]
            const v2 = grid2D[i + 1][j + 1]
            const v3 = grid2D[i][j + 1]

            const minVal = Math.min(v0, v1, v2, v3)
            const maxVal = Math.max(v0, v1, v2, v3)

            for (const iso of isoLevels) {
                const C = iso.value
                if (C < minVal || C > maxVal) continue

                let code = 0
                if (v0 >= C) code |= 1
                if (v1 >= C) code |= 2
                if (v2 >= C) code |= 4
                if (v3 >= C) code |= 8

                if (code === 0 || code === 15) continue

                const e0 = interp(p0, p1, v0, v1, C)
                const e1 = interp(p1, p2, v1, v2, C)
                const e2 = interp(p3, p2, v3, v2, C)
                const e3 = interp(p0, p3, v0, v3, C)

                switch (code) {
                    case 1: addSegment(e3, e0, iso.color); break
                    case 2: addSegment(e0, e1, iso.color); break
                    case 3: addSegment(e3, e1, iso.color); break
                    case 4: addSegment(e1, e2, iso.color); break
                    case 5:
                        addSegment(e3, e2, iso.color)
                        addSegment(e0, e1, iso.color)
                        break
                    case 6: addSegment(e0, e2, iso.color); break
                    case 7: addSegment(e3, e2, iso.color); break
                    case 8: addSegment(e2, e3, iso.color); break
                    case 9: addSegment(e0, e2, iso.color); break
                    case 10:
                        addSegment(e0, e3, iso.color)
                        addSegment(e1, e2, iso.color)
                        break
                    case 11: addSegment(e1, e2, iso.color); break
                    case 12: addSegment(e3, e1, iso.color); break
                    case 13: addSegment(e0, e1, iso.color); break
                    case 14: addSegment(e3, e0, iso.color); break
                    default: break
                }
            }
        }
    }

    return { positions: new Float32Array(positions), colors: new Float32Array(colors) }
}

/**
 * 1. Topographic Isolines (IGN / Geological Elevation Contours)
 */
function TopoIsolines({ terrainConfig }) {
    const { geometry } = useMemo(() => {
        const N = 80
        const xCoords = []
        const zCoords = []
        for (let i = 0; i < N; i++) {
            xCoords.push(i * (100 / (N - 1)))
            zCoords.push(i * (100 / (N - 1)))
        }

        // Build elevation grid
        const grid = []
        let minH = Infinity
        let maxH = -Infinity

        for (let i = 0; i < N; i++) {
            const row = []
            for (let j = 0; j < N; j++) {
                const h = getTerrainHeight(xCoords[i], zCoords[j], terrainConfig)
                row.push(h)
                if (h < minH) minH = h
                if (h > maxH) maxH = h
            }
            grid.push(row)
        }

        // Elevation step calculation
        const step = 0.5
        const isoLevels = []
        const start = Math.floor(minH / step) * step
        const end = Math.ceil(maxH / step) * step

        for (let c = start; c <= end; c += step) {
            const isMajor = Math.abs(Math.round(c * 10) % 20) === 0 // Major contour every 2m
            isoLevels.push({
                value: c,
                color: isMajor ? '#fbbf24' : '#d97706', // Gold major, amber minor
                isMajor
            })
        }

        const { positions, colors } = generateMarchingSquares(grid, xCoords, zCoords, isoLevels, getTerrainHeight, terrainConfig, 0.05)
        const geo = new THREE.BufferGeometry()
        geo.setAttribute('position', new THREE.BufferAttribute(positions, 3))
        geo.setAttribute('color', new THREE.BufferAttribute(colors, 3))
        return { geometry: geo }
    }, [terrainConfig])

    return (
        <lineSegments geometry={geometry} frustumCulled={false}>
            <lineBasicMaterial vertexColors linewidth={2} transparent opacity={0.85} depthWrite={false} />
        </lineSegments>
    )
}

/**
 * 2. Pheromone Concentration Isolines (Chemical Equipotentials & Perception Boundaries)
 */
function PheromoneIsolines({ pheromones, ants, terrainConfig }) {
    const { geometry } = useMemo(() => {
        const N = 60
        const xCoords = []
        const zCoords = []
        for (let i = 0; i < N; i++) {
            xCoords.push(i * (100 / (N - 1)))
            zCoords.push(i * (100 / (N - 1)))
        }

        // Compute 2D Gaussian density kernel from pheromones and active ant trail centers
        const grid = []
        const activeSources = []

        if (pheromones && pheromones.length > 0) {
            for (const p of pheromones.slice(-100)) {
                activeSources.push({
                    x: p.x <= 5 ? p.x * 50 : p.x,
                    z: p.y <= 5 ? (p.y * 50) : (p.z || p.y || 50),
                    strength: p.intensity || 1.0
                })
            }
        }

        // If very few deposited pheromones, add ant trail contributions
        if (ants && ants.length > 0) {
            for (let i = 0; i < Math.min(ants.length, 30); i++) {
                const a = ants[i]
                if (a.carriedItem && a.carriedItem !== 'NONE') {
                    activeSources.push({
                        x: a.x <= 5 ? a.x * 50 : a.x,
                        z: a.y <= 5 ? (a.y * 50) : (a.z || a.y || 50),
                        strength: 1.5
                    })
                }
            }
        }

        const sigmaSq = 3.5 * 3.5

        for (let i = 0; i < N; i++) {
            const row = []
            for (let j = 0; j < N; j++) {
                const x = xCoords[i]
                const z = zCoords[j]
                let val = 0

                for (const src of activeSources) {
                    const dx = x - src.x
                    const dz = z - src.z
                    const distSq = dx * dx + dz * dz
                    if (distSq < 400) { // Radius of influence < 20m
                        val += src.strength * Math.exp(-distSq / (2 * sigmaSq))
                    }
                }
                row.push(val)
            }
            grid.push(row)
        }

        // Iso-concentration levels
        const isoLevels = [
            { value: 0.15, color: '#38bdf8' }, // Sensory detection boundary (Cyan)
            { value: 0.40, color: '#818cf8' }, // Active recruitment path (Indigo)
            { value: 0.75, color: '#c084fc' }, // Dense trunk corridor (Purple)
            { value: 1.20, color: '#f43f5e' }, // High-density chemical core (Rose)
        ]

        const { positions, colors } = generateMarchingSquares(grid, xCoords, zCoords, isoLevels, getTerrainHeight, terrainConfig, 0.08)
        const geo = new THREE.BufferGeometry()
        geo.setAttribute('position', new THREE.BufferAttribute(positions, 3))
        geo.setAttribute('color', new THREE.BufferAttribute(colors, 3))
        return { geometry: geo }
    }, [pheromones, ants, terrainConfig])

    return (
        <lineSegments geometry={geometry} frustumCulled={false}>
            <lineBasicMaterial vertexColors linewidth={2} transparent opacity={0.9} depthWrite={false} />
        </lineSegments>
    )
}

/**
 * 3. Micro-Climate Isolines (Surface Isotherms: Tree Shade, River Cooling, Solar Exposure)
 */
function MicroclimateIsolines({ environment, terrainConfig }) {
    const { geometry } = useMemo(() => {
        const N = 70
        const xCoords = []
        const zCoords = []
        for (let i = 0; i < N; i++) {
            xCoords.push(i * (100 / (N - 1)))
            zCoords.push(i * (100 / (N - 1)))
        }

        const baseTemp = environment?.temperature ?? 24.5
        const riverX = terrainConfig?.riverX ?? 25
        const hasRiver = terrainConfig?.hasRiver ?? true

        // Tree clusters providing cooling canopy shade
        const treeCanopies = [
            { x: 15, z: 30, radius: 8 },
            { x: 78, z: 25, radius: 9 },
            { x: 82, z: 75, radius: 7 },
            { x: 18, z: 80, radius: 8 },
            { x: 55, z: 15, radius: 6 },
            { x: 88, z: 88, radius: 7 },
        ]

        const grid = []
        let minT = Infinity
        let maxT = -Infinity

        for (let i = 0; i < N; i++) {
            const row = []
            for (let j = 0; j < N; j++) {
                const x = xCoords[i]
                const z = zCoords[j]
                const h = getTerrainHeight(x, z, terrainConfig)

                // 1. Altitude cooling (-0.5°C per meter elevation)
                let localT = baseTemp - h * 0.5

                // 2. River evaporative cooling (-2.5°C near water banks)
                if (hasRiver) {
                    const distToRiver = Math.abs(x - riverX)
                    if (distToRiver < 15) {
                        localT -= 2.5 * (1 - distToRiver / 15)
                    }
                }

                // 3. Tree canopy shade cooling (-3.2°C under shade)
                for (const tree of treeCanopies) {
                    const dist = Math.hypot(x - tree.x, z - tree.z)
                    if (dist < tree.radius) {
                        localT -= 3.2 * (1 - dist / tree.radius)
                    }
                }

                // 4. Open sunny clearings slight warming (+1.2°C)
                if (x > 40 && x < 70 && z > 40 && z < 70) {
                    localT += 1.2
                }

                if (localT < minT) minT = localT
                if (localT > maxT) maxT = localT
                row.push(localT)
            }
            grid.push(row)
        }

        // Generate isotherms every 0.8°C
        const step = 0.8
        const isoLevels = []
        const start = Math.floor(minT / step) * step
        const end = Math.ceil(maxT / step) * step

        for (let t = start; t <= end; t += step) {
            let color = '#38bdf8' // Cool Blue (< 21°C)
            if (t >= 24.5) color = '#f87171' // Hot Red
            else if (t >= 23.0) color = '#fbbf24' // Warm Amber
            else if (t >= 21.5) color = '#34d399' // Mild Mint Green

            isoLevels.push({ value: t, color })
        }

        const { positions, colors } = generateMarchingSquares(grid, xCoords, zCoords, isoLevels, getTerrainHeight, terrainConfig, 0.06)
        const geo = new THREE.BufferGeometry()
        geo.setAttribute('position', new THREE.BufferAttribute(positions, 3))
        geo.setAttribute('color', new THREE.BufferAttribute(colors, 3))
        return { geometry: geo }
    }, [environment, terrainConfig])

    return (
        <lineSegments geometry={geometry} frustumCulled={false}>
            <lineBasicMaterial vertexColors linewidth={2} transparent opacity={0.85} depthWrite={false} />
        </lineSegments>
    )
}

/**
 * Main Scientific Isolines Container
 */
export default function ScientificIsolines() {
    const {
        lookAndFeel,
        showScientificIsolinesTopo,
        showScientificIsolinesPheromones,
        showScientificIsolinesMicroclimate,
        terrainConfig,
        pheromones,
        ants,
        environment
    } = useSimulationStore()

    if (lookAndFeel !== 'SCIENTIFIC') return null

    return (
        <group frustumCulled={false}>
            {showScientificIsolinesTopo && (
                <TopoIsolines terrainConfig={terrainConfig} />
            )}
            {showScientificIsolinesPheromones && (
                <PheromoneIsolines pheromones={pheromones} ants={ants} terrainConfig={terrainConfig} />
            )}
            {showScientificIsolinesMicroclimate && (
                <MicroclimateIsolines environment={environment} terrainConfig={terrainConfig} />
            )}
        </group>
    )
}
