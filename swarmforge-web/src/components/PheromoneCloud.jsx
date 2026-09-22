import { useRef, useMemo, useEffect } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import { getTerrainHeight } from '../utils/terrainUtils'

/**
 * Creates a soft procedural Gaussian falloff glowing disc texture
 * to make pheromone deposits look like diffuse ethereal vapor/mist rather than hard square/circular points.
 */
function createGlowDiscTexture() {
    const canvas = document.createElement('canvas')
    canvas.width = 64
    canvas.height = 64
    const ctx = canvas.getContext('2d')

    const gradient = ctx.createRadialGradient(32, 32, 0, 32, 32, 32)
    gradient.addColorStop(0, 'rgba(255, 255, 255, 1.0)')
    gradient.addColorStop(0.3, 'rgba(255, 255, 255, 0.75)')
    gradient.addColorStop(0.7, 'rgba(255, 255, 255, 0.25)')
    gradient.addColorStop(1, 'rgba(255, 255, 255, 0.0)')

    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, 64, 64)

    const texture = new THREE.CanvasTexture(canvas)
    texture.wrapS = THREE.ClampToEdgeWrapping
    texture.wrapT = THREE.ClampToEdgeWrapping
    return texture
}

export default function PheromoneCloud() {
    const { pheromones, ants, terrainConfig, showPheromones } = useSimulationStore()
    const pointsRef = useRef()
    const glowTexture = useMemo(() => createGlowDiscTexture(), [])

    // If disabled in options, do not render pheromones
    if (!showPheromones) return null

    // Max capacity for cloud points
    const MAX_POINTS = 3000

    const { geometry, positions, colors, sizes } = useMemo(() => {
        const pos = new Float32Array(MAX_POINTS * 3)
        const col = new Float32Array(MAX_POINTS * 3)
        const sz = new Float32Array(MAX_POINTS)

        const geo = new THREE.BufferGeometry()
        geo.setAttribute('position', new THREE.BufferAttribute(pos, 3))
        geo.setAttribute('color', new THREE.BufferAttribute(col, 3))
        geo.setAttribute('size', new THREE.BufferAttribute(sz, 1))

        return { geometry: geo, positions: pos, colors: col, sizes: sz }
    }, [])

    useFrame(({ clock }) => {
        if (!pointsRef.current) return

        const elapsed = clock.getElapsedTime()
        let idx = 0

        // 1. Render active deposited pheromone points with soft trail dispersion
        if (pheromones && pheromones.length > 0) {
            for (let i = 0; i < pheromones.length && idx < MAX_POINTS; i++) {
                const p = pheromones[i]
                const x = p.x ?? 50
                const z = p.z !== undefined ? p.z : (p.y ?? 50)
                const groundY = getTerrainHeight(x, z, terrainConfig)

                // Add subtle floating shimmer drift
                const shimmer = Math.sin(elapsed * 2 + i) * 0.05
                let y = groundY + 0.15 + shimmer
                if (p.depth !== undefined && p.depth > 0) {
                    // Underground pheromone at depth
                    y = groundY - p.depth
                }

                positions[idx * 3] = x
                positions[idx * 3 + 1] = y
                positions[idx * 3 + 2] = z

                // Color based on 8 canonical pheromone types (1:1 with PheromoneType.java & PheromoneOverlay.java)
                const intensity = p.intensity || 1.0
                const type = (p.type || '').toUpperCase()

                if (type === 'FOOD' || type === 'FOOD_TRAIL') {
                    // FOOD - Green (Color.rgb(76, 175, 80))
                    colors[idx * 3] = 0.30 * intensity
                    colors[idx * 3 + 1] = 0.69 * intensity
                    colors[idx * 3 + 2] = 0.31 * intensity
                } else if (type === 'HOME' || type === 'HOME_TRAIL') {
                    // HOME - Blue (Color.rgb(33, 150, 243))
                    colors[idx * 3] = 0.13 * intensity
                    colors[idx * 3 + 1] = 0.59 * intensity
                    colors[idx * 3 + 2] = 0.95 * intensity
                } else if (type === 'ALARM') {
                    // ALARM - Red (Color.rgb(244, 67, 54))
                    colors[idx * 3] = 0.96 * intensity
                    colors[idx * 3 + 1] = 0.26 * intensity
                    colors[idx * 3 + 2] = 0.21 * intensity
                } else if (type === 'RECRUITMENT' || type === 'TRAIL') {
                    // RECRUITMENT/TRAIL - Yellow (Color.rgb(255, 193, 7))
                    colors[idx * 3] = 1.00 * intensity
                    colors[idx * 3 + 1] = 0.76 * intensity
                    colors[idx * 3 + 2] = 0.03 * intensity
                } else if (type === 'QUEEN' || type === 'QUEEN_SCENT') {
                    // QUEEN - Purple (Color.rgb(156, 39, 176))
                    colors[idx * 3] = 0.61 * intensity
                    colors[idx * 3 + 1] = 0.15 * intensity
                    colors[idx * 3 + 2] = 0.69 * intensity
                } else if (type === 'BROOD' || type === 'BROOD_SCENT') {
                    // BROOD - Orange (Color.rgb(255, 152, 0))
                    colors[idx * 3] = 1.00 * intensity
                    colors[idx * 3 + 1] = 0.60 * intensity
                    colors[idx * 3 + 2] = 0.00 * intensity
                } else if (type === 'DEATH' || type === 'DEATH_SCENT') {
                    // DEATH - Gray/Slate (Color.rgb(96, 125, 139))
                    colors[idx * 3] = 0.38 * intensity
                    colors[idx * 3 + 1] = 0.49 * intensity
                    colors[idx * 3 + 2] = 0.55 * intensity
                } else if (type === 'TERRITORY') {
                    // TERRITORY - Teal (Color.rgb(0, 150, 136))
                    colors[idx * 3] = 0.00 * intensity
                    colors[idx * 3 + 1] = 0.59 * intensity
                    colors[idx * 3 + 2] = 0.53 * intensity
                } else {
                    // Default Trail (Cyan / Azure)
                    colors[idx * 3] = 0.15 * intensity
                    colors[idx * 3 + 1] = 0.75 * intensity
                    colors[idx * 3 + 2] = 0.98 * intensity
                }

                sizes[idx] = (1.5 + Math.sin(elapsed + i * 0.1) * 0.3) * intensity
                idx++
            }
        }

        // 2. Render micro-halos directly attached to active moving ants
        if (ants && ants.length > 0) {
            for (let j = 0; j < ants.length && idx < MAX_POINTS; j++) {
                const ant = ants[j]
                const antX = ant.x ?? 50
                const antZ = ant.z !== undefined ? ant.z : (ant.y ?? 50)
                const groundY = getTerrainHeight(antX, antZ, terrainConfig)
                let antY = groundY + 0.12
                if (ant.isUnderground || (ant.y !== undefined && ant.y < 0)) {
                    antY = groundY + (ant.y || -1.2)
                }

                positions[idx * 3] = antX
                positions[idx * 3 + 1] = antY
                positions[idx * 3 + 2] = antZ

                if (ant.carriedItem && ant.carriedItem !== 'NONE') {
                    // Golden aura under foragers carrying food
                    colors[idx * 3] = 0.95
                    colors[idx * 3 + 1] = 0.70
                    colors[idx * 3 + 2] = 0.15
                } else if (ant.caste === 'QUEEN') {
                    // Royal pheromone violet aura
                    colors[idx * 3] = 0.85
                    colors[idx * 3 + 1] = 0.20
                    colors[idx * 3 + 2] = 0.95
                } else {
                    // Soft blue foraging scent
                    colors[idx * 3] = 0.20
                    colors[idx * 3 + 1] = 0.60
                    colors[idx * 3 + 2] = 0.90
                }

                sizes[idx] = 1.8
                idx++
            }
        }

        // Zero out unused points
        for (let k = idx; k < MAX_POINTS; k++) {
            positions[k * 3] = 0
            positions[k * 3 + 1] = -100
            positions[k * 3 + 2] = 0
            sizes[k] = 0
        }

        geometry.attributes.position.needsUpdate = true
        geometry.attributes.color.needsUpdate = true
        geometry.attributes.size.needsUpdate = true
        geometry.setDrawRange(0, idx)
    })

    return (
        <points ref={pointsRef} geometry={geometry}>
            <pointsMaterial
                map={glowTexture}
                size={1.6}
                vertexColors
                transparent
                opacity={0.65}
                blending={THREE.AdditiveBlending}
                sizeAttenuation={true}
                depthWrite={false}
            />
        </points>
    )
}
