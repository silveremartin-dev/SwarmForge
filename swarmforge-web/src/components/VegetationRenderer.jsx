import React, { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import LowPolyModel from './LowPolyModel'
import { getTerrainHeight, getEffectiveSeason } from '../utils/terrainUtils'
import { getPixelPoppyTexture, getPixelDandelionTexture, getPixelTallGrassTexture } from '../utils/pixelTextures'

/**
 * Single Voxel Tree Component (Gamified Mode)
 * Built strictly out of stacked 3D cubes snapped to integer grid coords.
 * Supports multiple species variants (Oak, Pine, Birch) with dense multi-tiered voxel foliage.
 */
function VoxelTree({ position, scale = 1.0, variant = 0, terrainConfig }) {
    const [posX, _, posZ] = position
    const gridX = Math.round(posX)
    const gridZ = Math.round(posZ)
    const posY = getTerrainHeight(gridX, gridZ, terrainConfig)

    // Generate dense voxel blocks for trunk and foliage
    const { trunkVoxels, foliageVoxels } = useMemo(() => {
        const trunk = []
        const foliage = []

        const trunkHeight = Math.max(6, Math.round(7 * scale))

        // Variant 0: Voxel Oak Tree (Massive dense canopy with 80+ voxel cubes)
        if (variant % 3 === 0) {
            // 1. Trunk Cubes (2x2 Column resting on ground + root flares)
            for (let y = 0; y < trunkHeight; y++) {
                trunk.push({ x: 0, y: y + 0.5, z: 0, color: '#5c3a21' })
                if (y < trunkHeight - 2) {
                    trunk.push({ x: 1, y: y + 0.5, z: 0, color: '#4a2e19' })
                }
            }
            // Root flares spreading on ground
            trunk.push({ x: 2, y: 0.5, z: 0, color: '#3d2514' })
            trunk.push({ x: -1, y: 0.5, z: 0, color: '#3d2514' })
            trunk.push({ x: 0, y: 0.5, z: 1, color: '#3d2514' })
            trunk.push({ x: 0, y: 0.5, z: -1, color: '#3d2514' })
            trunk.push({ x: 1, y: 0.5, z: 1, color: '#3d2514' })

            // 2. Foliage Canopy (Multi-tiered 7x7, 5x5, 3x3 layers)
            const canopyBaseY = trunkHeight - 2

            // Tier 1: 7x7 wide base foliage layer
            for (let dx = -3; dx <= 3; dx++) {
                for (let dz = -3; dz <= 3; dz++) {
                    if (Math.abs(dx) === 3 && Math.abs(dz) === 3) continue
                    if (Math.abs(dx) + Math.abs(dz) > 5) continue
                    foliage.push({
                        x: dx,
                        y: canopyBaseY + 0.5,
                        z: dz,
                        color: (dx + dz) % 2 === 0 ? '#14532d' : '#15803d'
                    })
                }
            }

            // Tier 2: 5x5 mid-level foliage layer
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    foliage.push({
                        x: dx,
                        y: canopyBaseY + 1.5,
                        z: dz,
                        color: (dx + dz) % 2 === 0 ? '#166534' : '#22c55e'
                    })
                }
            }

            // Tier 3: 3x3 upper foliage layer
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    foliage.push({
                        x: dx,
                        y: canopyBaseY + 2.5,
                        z: dz,
                        color: '#4ade80'
                    })
                }
            }

            // Tier 4: Crown peak voxels
            foliage.push({ x: 0, y: canopyBaseY + 3.5, z: 0, color: '#86efac' })
            foliage.push({ x: 1, y: canopyBaseY + 2.5, z: 0, color: '#86efac' })

        // Variant 1: Voxel Pine / Spruce Tree (Tall conical pyramid structure)
        } else if (variant % 3 === 1) {
            // Slender tall trunk
            for (let y = 0; y < trunkHeight + 2; y++) {
                trunk.push({ x: 0, y: y + 0.5, z: 0, color: '#451a03' })
            }
            trunk.push({ x: 1, y: 0.5, z: 0, color: '#3b1402' })
            trunk.push({ x: -1, y: 0.5, z: 0, color: '#3b1402' })

            // Conical foliage skirts
            const pHeight = trunkHeight + 1
            // Skirt 1 (Bottom 5x5)
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    foliage.push({ x: dx, y: pHeight - 4 + 0.5, z: dz, color: '#064e3b' })
                }
            }
            // Skirt 2 (Mid 3x3)
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    foliage.push({ x: dx, y: pHeight - 2 + 0.5, z: dz, color: '#047857' })
                }
            }
            // Skirt 3 (Top 1x1 peak)
            foliage.push({ x: 0, y: pHeight + 0.5, z: 0, color: '#10b981' })
            foliage.push({ x: 0, y: pHeight + 1.5, z: 0, color: '#34d399' })

        // Variant 2: Voxel Birch Tree (White/gray bark + bright lime canopy)
        } else {
            for (let y = 0; y < trunkHeight; y++) {
                trunk.push({
                    x: 0,
                    y: y + 0.5,
                    z: 0,
                    color: y % 2 === 0 ? '#f8fafc' : '#cbd5e1'
                })
            }
            const bY = trunkHeight - 2
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    foliage.push({ x: dx, y: bY + 0.5, z: dz, color: '#4d7c0f' })
                }
            }
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    foliage.push({ x: dx, y: bY + 1.5, z: dz, color: '#65a30d' })
                }
            }
            foliage.push({ x: 0, y: bY + 2.5, z: 0, color: '#84cc16' })
        }

        return { trunkVoxels: trunk, foliageVoxels: foliage }
    }, [scale, variant])

    return (
        <group position={[gridX, posY, gridZ]} frustumCulled={false}>
            {trunkVoxels.map((v, i) => (
                <mesh key={`trunk-${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow frustumCulled={false}>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color={v.color} roughness={0.9} metalness={0.05} side={THREE.FrontSide} depthWrite depthTest />
                </mesh>
            ))}

            {foliageVoxels.map((v, i) => (
                <mesh key={`foliage-${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow frustumCulled={false}>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color={v.color} roughness={0.8} metalness={0.05} side={THREE.FrontSide} depthWrite depthTest />
                </mesh>
            ))}
        </group>
    )
}

/**
 * Single Realistic / Scientific Tree Component
 * Systematically uses downloaded 3D .obj models (/3d/tree.obj, /3d/LOW_POLY_set.obj, /3d/forest_nature_set_all_in.obj)
 * anchored to ground elevation Y = getTerrainHeight(x, z).
 */
function RealisticTree({ position, scale = 1.0, windSpeed = 2.4, season = 'SUMMER', modelUrl = '/3d/tree.obj', terrainConfig }) {
    const [x, _, z] = position
    const groundY = getTerrainHeight(x, z, terrainConfig)

    return (
        <group position={[x, groundY, z]}>
            <LowPolyModel
                url={modelUrl}
                textureUrl="/3d/texture_gradient.png"
                position={[0, 0, 0]}
                scale={scale * 0.8}
                sway={true}
                windSpeed={windSpeed}
                season={season}
                fallbackGeometry="dodecahedron"
                fallbackColor={season === 'AUTUMN' ? '#d97706' : season === 'WINTER' ? '#e2e8f0' : '#15803d'}
            />
        </group>
    )
}

/**
 * Ground Vegetation & Low Poly Plant Sets systematically using 3D .obj models
 */
function RealisticGroundFlora({ windSpeed = 2.4, season = 'SUMMER', terrainConfig }) {
    const floraItems = useMemo(() => {
        const items = []
        const rand = (seed) => {
            const x = Math.sin(seed * 12.9898 + 78.233) * 43758.5453
            return x - Math.floor(x)
        }

        // Generate 60 scattered flora patches
        for (let i = 0; i < 60; i++) {
            let x = rand(i * 1.1) * 90 + 5
            let z = rand(i * 2.3) * 90 + 5

            if (x >= 18 && x <= 32) {
                x = x < 25 ? 15 : 35
            }

            const typeInt = Math.floor(rand(i * 3.7) * 4)
            const type = typeInt === 0 ? 'FERN' : typeInt === 1 ? 'MOSS' : typeInt === 2 ? 'GRASS' : 'FLOWER'
            const scale = 0.5 + rand(i * 5.1) * 0.8
            const rotY = rand(i * 7.9) * Math.PI * 2
            const groundY = getTerrainHeight(x, z, terrainConfig)

            items.push({ id: i, x, y: groundY, z, type, scale, rotY })
        }
        return items
    }, [terrainConfig])

    const groupRef = useRef()

    useFrame((state) => {
        if (groupRef.current) {
            const t = state.clock.getElapsedTime()
            groupRef.current.children.forEach((child, idx) => {
                if (child.userData?.type === 'GRASS' || child.userData?.type === 'FERN') {
                    child.rotation.z = Math.sin(t * 2 + idx) * 0.05
                }
            })
        }
    })

    return (
        <group ref={groupRef} frustumCulled={false}>
            {/* Systematically loaded 3D .obj Bamboo Clusters */}
            <LowPolyModel
                url="/3d/bamboo_set.obj"
                position={[65, getTerrainHeight(65, 85, terrainConfig), 85]}
                scale={0.7}
                sway={true}
                windSpeed={windSpeed}
                season={season}
                fallbackGeometry="cylinder"
                fallbackColor="#65a30d"
            />
            <LowPolyModel
                url="/3d/bamboo_set.obj"
                position={[12, getTerrainHeight(12, 15, terrainConfig), 15]}
                scale={0.6}
                rotation={[0, 1.2, 0]}
                sway={true}
                windSpeed={windSpeed}
                season={season}
                fallbackGeometry="cylinder"
                fallbackColor="#4ade80"
            />

            {/* Systematically loaded 3D .obj Cacti */}
            <LowPolyModel
                url="/3d/cactus.obj"
                position={[88, getTerrainHeight(88, 18, terrainConfig), 18]}
                scale={0.5}
                rotation={[0, 0.5, 0]}
                season={season}
                fallbackGeometry="cylinder"
                fallbackColor="#15803d"
            />

            {/* Systematically loaded 3D .obj Tropical Plant Sets */}
            <LowPolyModel
                url="/3d/tropical_plants.obj"
                position={[45, getTerrainHeight(45, 82, terrainConfig), 82]}
                scale={0.6}
                sway={true}
                windSpeed={windSpeed}
                season={season}
                fallbackGeometry="dodecahedron"
                fallbackColor="#166534"
            />
            <LowPolyModel
                url="/3d/tropical_plants.obj"
                position={[28, getTerrainHeight(28, 22, terrainConfig), 22]}
                scale={0.5}
                rotation={[0, 2.1, 0]}
                sway={true}
                windSpeed={windSpeed}
                season={season}
                fallbackGeometry="dodecahedron"
                fallbackColor="#15803d"
            />

            {/* Systematically loaded 3D .obj Forest Nature Set */}
            <LowPolyModel
                url="/3d/forest_nature_set_all_in.obj"
                textureUrl="/3d/texture_gradient.png"
                position={[75, getTerrainHeight(75, 45, terrainConfig), 45]}
                scale={0.4}
                sway={true}
                windSpeed={windSpeed}
                season={season}
                fallbackGeometry="dodecahedron"
                fallbackColor="#15803d"
            />

            {floraItems.map((item) => {
                const mossColor = season === 'WINTER' ? '#e2e8f0' : season === 'AUTUMN' ? '#a16207' : '#3f6212'
                const fernColor = season === 'WINTER' ? '#cbd5e1' : season === 'AUTUMN' ? '#b45309' : '#15803d'
                const grassColor = season === 'WINTER' ? '#f1f5f9' : season === 'AUTUMN' ? '#ca8a04' : '#65a30d'

                if (item.type === 'MOSS') {
                    return (
                        <mesh key={item.id} position={[item.x, item.y + 0.08 * item.scale, item.z]} rotation={[0, item.rotY, 0]} receiveShadow frustumCulled={false}>
                            <dodecahedronGeometry args={[0.6 * item.scale, 1]} />
                            <meshStandardMaterial color={mossColor} roughness={0.95} side={THREE.FrontSide} depthWrite depthTest />
                        </mesh>
                    )
                }

                if (item.type === 'FERN') {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]} userData={{ type: 'FERN' }} frustumCulled={false}>
                            {[0, 1.05, 2.1, 3.15, 4.2, 5.25].map((angle, fIdx) => (
                                <mesh key={fIdx} position={[0, 0.3 * item.scale, 0]} rotation={[0.4, angle, 0.1]} castShadow frustumCulled={false}>
                                    <boxGeometry args={[0.35 * item.scale, 0.05 * item.scale, 1.1 * item.scale]} />
                                    <meshStandardMaterial color={fernColor} roughness={0.85} side={THREE.FrontSide} depthWrite depthTest />
                                </mesh>
                            ))}
                        </group>
                    )
                }

                if (item.type === 'GRASS') {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]} userData={{ type: 'GRASS' }} frustumCulled={false}>
                            {[-0.15, 0, 0.15].map((offset, gIdx) => (
                                <mesh key={gIdx} position={[offset, 0.4 * item.scale, offset]} rotation={[0.15 * gIdx, gIdx * 0.8, 0.1]} castShadow frustumCulled={false}>
                                    <boxGeometry args={[0.08 * item.scale, 0.8 * item.scale, 0.15 * item.scale]} />
                                    <meshStandardMaterial color={grassColor} roughness={0.85} side={THREE.FrontSide} depthWrite depthTest />
                                </mesh>
                            ))}
                        </group>
                    )
                }

                // Wildflower / Mushroom cluster
                return (
                    <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]} frustumCulled={false}>
                        <mesh position={[0, 0.3 * item.scale, 0]} castShadow frustumCulled={false}>
                            <cylinderGeometry args={[0.04 * item.scale, 0.06 * item.scale, 0.6 * item.scale, 8]} />
                            <meshStandardMaterial color="#f8fafc" roughness={0.9} side={THREE.FrontSide} depthWrite depthTest />
                        </mesh>
                        <mesh position={[0, 0.6 * item.scale, 0]} castShadow frustumCulled={false}>
                            <sphereGeometry args={[0.22 * item.scale, 12, 12]} />
                            <meshStandardMaterial color={item.id % 2 === 0 ? '#f43f5e' : '#fbbf24'} roughness={0.5} side={THREE.FrontSide} depthWrite depthTest />
                        </mesh>
                    </group>
                )
            })}
        </group>
    )
}

/**
 * Grand Ancient Hollow Tree Stump Component (Gamified Mode)
 * Inspired by the prominent hollow tree stump in the reference diorama.
 * Features a hollow interior cavity, broken jagged rim, flared root arches, and bracket fungi.
 */
function VoxelHollowStump({ position = [52, 0, 48], scale = 1.0, terrainConfig }) {
    const [posX, _, posZ] = position
    const gridX = Math.round(posX)
    const gridZ = Math.round(posZ)
    const groundY = getTerrainHeight(gridX, gridZ, terrainConfig)

    const { stumpVoxels, rootVoxels, fungiVoxels } = useMemo(() => {
        const trunk = []
        const roots = []
        const fungi = []
        const trunkRadius = Math.round(3 * scale)
        const trunkH = Math.round(7 * scale)

        // 1. Hollow cylindrical trunk wall
        for (let y = 0; y < trunkH; y++) {
            const isRim = y === trunkH - 1
            for (let dx = -trunkRadius; dx <= trunkRadius; dx++) {
                for (let dz = -trunkRadius; dz <= trunkRadius; dz++) {
                    const distSq = dx * dx + dz * dz
                    const isWall = distSq <= trunkRadius * trunkRadius && distSq >= (trunkRadius - 1.2) * (trunkRadius - 1.2)
                    if (isWall) {
                        // Jagged broken top rim
                        if (isRim && (Math.abs(dx * dz) % 2 === 0)) continue
                        const barkColor = (y + dx + dz) % 2 === 0 ? '#451a03' : '#3b1402'
                        trunk.push({ x: dx, y: y + 0.5, z: dz, color: barkColor })
                    }
                }
            }
        }

        // 2. Root buttresses flaring outwards and downwards
        const flareAngles = [0, Math.PI / 2, Math.PI, (3 * Math.PI) / 2, Math.PI / 4, (5 * Math.PI) / 4]
        flareAngles.forEach((angle, idx) => {
            const len = 3 + (idx % 2) * 2
            for (let r = trunkRadius; r < trunkRadius + len; r++) {
                const rx = Math.round(Math.cos(angle) * r)
                const rz = Math.round(Math.sin(angle) * r)
                const rootH = Math.max(1, trunkH - (r - trunkRadius) * 2)
                for (let ry = 0; ry < rootH; ry++) {
                    roots.push({ x: rx, y: ry + 0.5, z: rz, color: '#3d2514' })
                }
            }
        })

        // 3. Bracket fungi / shelf mushrooms growing on the trunk
        fungi.push({ x: trunkRadius, y: 3.5, z: 0, w: 2, d: 2, color: '#d97706' })
        fungi.push({ x: trunkRadius + 1, y: 3.5, z: 0, w: 1, d: 1, color: '#fef3c7' })
        fungi.push({ x: -trunkRadius, y: 4.5, z: 1, w: 2, d: 2, color: '#b45309' })
        fungi.push({ x: 0, y: 2.5, z: trunkRadius, w: 2, d: 2, color: '#d97706' })

        return { stumpVoxels: trunk, rootVoxels: roots, fungiVoxels: fungi }
    }, [scale])

    return (
        <group position={[gridX, groundY, gridZ]} frustumCulled={false}>
            {/* Trunk Wall Voxels */}
            {stumpVoxels.map((v, i) => (
                <mesh key={`stump_${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow frustumCulled={false}>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color={v.color} roughness={0.9} metalness={0.05} depthWrite depthTest />
                </mesh>
            ))}

            {/* Root Flares */}
            {rootVoxels.map((v, i) => (
                <mesh key={`root_${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow frustumCulled={false}>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color={v.color} roughness={0.92} metalness={0.05} depthWrite depthTest />
                </mesh>
            ))}

            {/* Bracket Shelf Fungi */}
            {fungiVoxels.map((f, i) => (
                <mesh key={`fungi_${i}`} position={[f.x, f.y, f.z]} castShadow receiveShadow frustumCulled={false}>
                    <boxGeometry args={[f.w, 0.4, f.d]} />
                    <meshStandardMaterial color={f.color} roughness={0.7} metalness={0.05} depthWrite depthTest />
                </mesh>
            ))}
        </group>
    )
}

/**
 * 2.5D Cross-Billboard Mesh for Minecraft flowers and plants (X-shape)
 */
function PixelCrossMesh({ position, texture, scale = 1.0 }) {
    const geo = useMemo(() => new THREE.PlaneGeometry(0.85 * scale, 0.95 * scale), [scale])
    const mat = useMemo(() => new THREE.MeshStandardMaterial({
        map: texture,
        transparent: true,
        alphaTest: 0.25,
        roughness: 0.8,
        metalness: 0.05,
        side: THREE.DoubleSide,
    }), [texture])

    return (
        <group position={position} frustumCulled={false}>
            <mesh geometry={geo} material={mat} rotation={[0, Math.PI / 4, 0]} position={[0, 0.45 * scale, 0]} castShadow receiveShadow />
            <mesh geometry={geo} material={mat} rotation={[0, -Math.PI / 4, 0]} position={[0, 0.45 * scale, 0]} castShadow receiveShadow />
        </group>
    )
}

/**
 * Gamified Mode Voxel Flora Renderer (High Fidelity)
 * Renders authentic 2.5D cross-billboard pixel flowers (Poppies, Dandelions, Tall Grass),
 * voxel mushrooms (Amanites with white spots, brown bolets), and coral/yellow flower bushes.
 */
function GamifiedVoxelFlora({ terrainConfig }) {
    const poppyTex = useMemo(() => getPixelPoppyTexture(), [])
    const dandelionTex = useMemo(() => getPixelDandelionTexture(), [])
    const tallGrassTex = useMemo(() => getPixelTallGrassTexture(), [])

    const voxelPlants = useMemo(() => {
        const items = []
        const rand = (seed) => {
            const x = Math.sin(seed * 14.123 + 45.67) * 43758.5453
            return x - Math.floor(x)
        }

        // Generate 80 rich voxel flora instances
        for (let i = 0; i < 80; i++) {
            const gridX = Math.round(rand(i * 1.7) * 90 + 5)
            const gridZ = Math.round(rand(i * 3.1) * 90 + 5)
            if (gridX >= 18 && gridX <= 32) continue // Skip river channel
            const groundY = getTerrainHeight(gridX, gridZ, terrainConfig)
            // 0: Red Mushroom, 1: Brown Mushroom, 2: Flower Bush, 3: Poppy Cross, 4: Dandelion Cross, 5: Tall Grass Cross
            const type = Math.floor(rand(i * 4.3) * 6)

            items.push({ id: i, x: gridX, y: groundY, z: gridZ, type, scale: 0.75 + rand(i * 2.9) * 0.5 })
        }
        return items
    }, [terrainConfig])

    return (
        <group frustumCulled={false}>
            {voxelPlants.map((item) => {
                // Type 3: 2.5D Cross Poppy
                if (item.type === 3) {
                    return <PixelCrossMesh key={item.id} position={[item.x, item.y, item.z]} texture={poppyTex} scale={item.scale} />
                }
                // Type 4: 2.5D Cross Dandelion
                if (item.type === 4) {
                    return <PixelCrossMesh key={item.id} position={[item.x, item.y, item.z]} texture={dandelionTex} scale={item.scale} />
                }
                // Type 5: 2.5D Cross Tall Grass
                if (item.type === 5) {
                    return <PixelCrossMesh key={item.id} position={[item.x, item.y, item.z]} texture={tallGrassTex} scale={item.scale} />
                }

                // Type 0: Red Fly Agaric Mushroom (White stem + Crimson cap with white speckles)
                if (item.type === 0) {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} scale={[item.scale, item.scale, item.scale]} frustumCulled={false}>
                            {/* Stem */}
                            <mesh position={[0, 0.5, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.4, 1.0, 0.4]} />
                                <meshStandardMaterial color="#f8fafc" roughness={0.8} depthWrite depthTest />
                            </mesh>
                            {/* Crimson Tier 1 Cap */}
                            <mesh position={[0, 1.1, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[1.2, 0.3, 1.2]} />
                                <meshStandardMaterial color="#dc2626" roughness={0.6} depthWrite depthTest />
                            </mesh>
                            {/* Crimson Tier 2 Dome */}
                            <mesh position={[0, 1.35, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.8, 0.25, 0.8]} />
                                <meshStandardMaterial color="#b91c1c" roughness={0.6} depthWrite depthTest />
                            </mesh>
                            {/* White Spot Voxels */}
                            <mesh position={[0.3, 1.3, 0.3]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.2, 0.2, 0.2]} />
                                <meshStandardMaterial color="#ffffff" roughness={0.8} depthWrite depthTest />
                            </mesh>
                            <mesh position={[-0.3, 1.3, -0.3]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.2, 0.2, 0.2]} />
                                <meshStandardMaterial color="#ffffff" roughness={0.8} depthWrite depthTest />
                            </mesh>
                        </group>
                    )
                }

                // Type 1: Brown Stepped Bolet Mushroom
                if (item.type === 1) {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} scale={[item.scale, item.scale, item.scale]} frustumCulled={false}>
                            <mesh position={[0, 0.4, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.35, 0.8, 0.35]} />
                                <meshStandardMaterial color="#e2e8f0" roughness={0.85} depthWrite depthTest />
                            </mesh>
                            <mesh position={[0, 0.9, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[1.4, 0.25, 1.4]} />
                                <meshStandardMaterial color="#92400e" roughness={0.7} depthWrite depthTest />
                            </mesh>
                            <mesh position={[0, 1.1, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.9, 0.25, 0.9]} />
                                <meshStandardMaterial color="#78350f" roughness={0.7} depthWrite depthTest />
                            </mesh>
                        </group>
                    )
                }

                // Type 2: Colorful Voxel Flower Bush (Coral / Rose / Yellow)
                const flowerColor = item.id % 2 === 0 ? '#f43f5e' : '#eab308'
                return (
                    <group key={item.id} position={[item.x, item.y, item.z]} scale={[item.scale, item.scale, item.scale]} frustumCulled={false}>
                        {/* Base Leaves */}
                        <mesh position={[0, 0.4, 0]} castShadow frustumCulled={false}>
                            <boxGeometry args={[1.0, 0.8, 1.0]} />
                            <meshStandardMaterial color="#15803d" roughness={0.85} depthWrite depthTest />
                        </mesh>
                        {/* Top Flower Blooms */}
                        <mesh position={[0.2, 0.9, 0.2]} castShadow frustumCulled={false}>
                            <boxGeometry args={[0.4, 0.4, 0.4]} />
                            <meshStandardMaterial color={flowerColor} roughness={0.6} depthWrite depthTest />
                        </mesh>
                        <mesh position={[-0.2, 0.9, -0.2]} castShadow frustumCulled={false}>
                            <boxGeometry args={[0.35, 0.35, 0.35]} />
                            <meshStandardMaterial color={flowerColor} roughness={0.6} depthWrite depthTest />
                        </mesh>
                        <mesh position={[0.1, 1.0, -0.2]} castShadow frustumCulled={false}>
                            <boxGeometry args={[0.3, 0.3, 0.3]} />
                            <meshStandardMaterial color="#fbbf24" roughness={0.6} depthWrite depthTest />
                        </mesh>
                    </group>
                )
            })}
        </group>
    )
}

/**
 * 3D Volumetric Tree Stump Component
 * Renders an authentic 3D tree stump with bark cylinder, top growth ring disk, and root flares.
 */
function Stump3D({ position, scale = 1.0, terrainConfig }) {
    const [x, _, z] = position
    const groundY = getTerrainHeight(x, z, terrainConfig)
    const trunkHeight = 0.9 * scale
    const topRadius = 0.5 * scale
    const baseRadius = 0.65 * scale

    return (
        <group position={[x, groundY, z]} frustumCulled={false}>
            {/* Main 3D Stump Trunk */}
            <mesh position={[0, trunkHeight / 2, 0]} castShadow receiveShadow frustumCulled={false}>
                <cylinderGeometry args={[topRadius, baseRadius, trunkHeight, 12]} />
                <meshStandardMaterial color="#5c3a21" roughness={0.9} metalness={0.05} depthTest={true} depthWrite={true} side={THREE.FrontSide} />
            </mesh>
            {/* Top Cut Wood Disk with Growth Ring Tone */}
            <mesh position={[0, trunkHeight + 0.01, 0]} rotation={[-Math.PI / 2, 0, 0]} receiveShadow frustumCulled={false}>
                <circleGeometry args={[topRadius * 0.95, 12]} />
                <meshStandardMaterial color="#d4a373" roughness={0.8} depthTest={true} depthWrite={true} side={THREE.FrontSide} />
            </mesh>
            {/* 3D Root Buttresses Flare at Base */}
            {[0, 1.2, 2.4, 3.6, 4.8].map((angle, idx) => (
                <mesh
                    key={`root-${idx}`}
                    position={[Math.cos(angle) * baseRadius * 0.7, trunkHeight * 0.25, Math.sin(angle) * baseRadius * 0.7]}
                    rotation={[0.3, -angle, 0]}
                    castShadow
                    receiveShadow
                    frustumCulled={false}
                >
                    <boxGeometry args={[0.25 * scale, trunkHeight * 0.6, 0.6 * scale]} />
                    <meshStandardMaterial color="#4a2e19" roughness={0.95} depthTest={true} depthWrite={true} side={THREE.FrontSide} />
                </mesh>
            ))}
        </group>
    )
}

/**
 * Standardized Botanical Diagram Tree Component (Scientific Mode)
 * Represents an academic forestry diagram calibrated to physical biometrics:
 * - Measured trunk DBH (Diameter at Breast Height) at Y=1.3m
 * - LAI (Leaf Area Index) light interception crown volume
 * - Projected ground root zone radius (R_root)
 */
function ScientificTree({ position, scale = 1.0, variant = 0, season = 'SUMMER', terrainConfig }) {
    const [x, _, z] = position
    const groundY = getTerrainHeight(x, z, terrainConfig)
    const trunkHeight = 4.0 * scale
    const crownColor = season === 'WINTER' ? '#cbd5e1' : season === 'AUTUMN' ? '#d97706' : '#15803d'
    const dbhMm = Math.round(280 * scale) // Diameter at Breast Height in mm

    return (
        <group position={[x, groundY, z]} frustumCulled={false}>
            {/* Projected Ground Root Zone Disk */}
            <mesh position={[0, 0.02, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                <ringGeometry args={[0.2 * scale, 2.4 * scale, 24]} />
                <meshBasicMaterial color="#0284c7" transparent opacity={0.15} side={THREE.DoubleSide} />
            </mesh>

            {/* Scientific Parametric Trunk (Charcoal / DBH Calibrated) */}
            <mesh position={[0, trunkHeight / 2, 0]} castShadow receiveShadow frustumCulled={false}>
                <cylinderGeometry args={[0.20 * scale, 0.38 * scale, trunkHeight, 12]} />
                <meshStandardMaterial color="#334155" roughness={0.7} metalness={0.15} side={THREE.FrontSide} depthWrite depthTest />
            </mesh>

            {/* DBH Metric Ring at Y=1.3m */}
            <mesh position={[0, 1.3 * scale, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                <torusGeometry args={[0.32 * scale, 0.02 * scale, 6, 16]} />
                <meshBasicMaterial color="#38bdf8" />
            </mesh>

            {/* LAI (Leaf Area Index) Light Interception Volumes */}
            {variant % 2 === 0 ? (
                // Conifer / Pinaceae Canopy
                <group position={[0, trunkHeight * 0.65, 0]} frustumCulled={false}>
                    <mesh position={[0, 0, 0]} castShadow receiveShadow frustumCulled={false}>
                        <coneGeometry args={[2.0 * scale, 2.4 * scale, 10]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[0, 1.3 * scale, 0]} castShadow receiveShadow frustumCulled={false}>
                        <coneGeometry args={[1.5 * scale, 1.9 * scale, 10]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[0, 2.4 * scale, 0]} castShadow receiveShadow frustumCulled={false}>
                        <coneGeometry args={[1.0 * scale, 1.5 * scale, 10]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    {/* LAI Light Interception Wireframe Envelope */}
                    <mesh position={[0, 1.2 * scale, 0]}>
                        <coneGeometry args={[2.2 * scale, 4.0 * scale, 8]} />
                        <meshBasicMaterial color="#38bdf8" wireframe transparent opacity={0.2} />
                    </mesh>
                </group>
            ) : (
                // Deciduous / Broadleaf Canopy
                <group position={[0, trunkHeight + 0.8 * scale, 0]} frustumCulled={false}>
                    <mesh position={[0, 0, 0]} castShadow receiveShadow frustumCulled={false}>
                        <sphereGeometry args={[1.7 * scale, 12, 10]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[0.7 * scale, -0.4 * scale, 0.5 * scale]} castShadow receiveShadow frustumCulled={false}>
                        <sphereGeometry args={[1.0 * scale, 10, 8]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[-0.6 * scale, -0.3 * scale, -0.5 * scale]} castShadow receiveShadow frustumCulled={false}>
                        <sphereGeometry args={[1.1 * scale, 10, 8]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    {/* LAI Interception Ring */}
                    <mesh position={[0, 0, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                        <ringGeometry args={[1.8 * scale, 1.9 * scale, 24]} />
                        <meshBasicMaterial color="#22c55e" transparent opacity={0.4} side={THREE.DoubleSide} />
                    </mesh>
                </group>
            )}
        </group>
    )
}

export default function VegetationRenderer() {
    const { lookAndFeel, climateEngine, terrainConfig } = useSimulationStore()
    const isGamified = lookAndFeel === 'GAMING'
    const isScientific = lookAndFeel === 'SCIENTIFIC'
    const windSpeed = climateEngine?.windSpeedMs ?? 2.4
    
    // Effective season flipped for Southern Hemisphere
    const rawSeason = climateEngine?.currentSeason || climateEngine?.season || 'SUMMER'
    const season = getEffectiveSeason(rawSeason, climateEngine?.hemisphere || 'NORTHERN')

    // Tree positions systematically across terrarium
    const treePositions = useMemo(() => [
        { id: 1, pos: [15, getTerrainHeight(15, 30, terrainConfig), 30], scale: 1.3, variant: 0, modelUrl: '/3d/LOW_POLY_set.obj' },
        { id: 2, pos: [78, getTerrainHeight(78, 25, terrainConfig), 25], scale: 1.5, variant: 1, modelUrl: '/3d/tree.obj' },
        { id: 3, pos: [82, getTerrainHeight(82, 75, terrainConfig), 75], scale: 1.2, variant: 2, modelUrl: '/3d/forest_nature_set_all_in.obj' },
        { id: 4, pos: [18, getTerrainHeight(18, 80, terrainConfig), 80], scale: 1.4, variant: 0, modelUrl: '/3d/tree.obj' },
        { id: 5, pos: [55, getTerrainHeight(55, 15, terrainConfig), 15], scale: 1.1, variant: 1, modelUrl: '/3d/LOW_POLY_set.obj' },
        { id: 6, pos: [88, getTerrainHeight(88, 88, terrainConfig), 88], scale: 1.3, variant: 2, modelUrl: '/3d/forest_nature_set_all_in.obj' },
    ], [terrainConfig])

    // Volumetric 3D Tree Stump positions across terrarium
    const stumpPositions = useMemo(() => [
        { id: 'stump_1', pos: [32, getTerrainHeight(32, 60, terrainConfig), 60], scale: 1.2 },
        { id: 'stump_2', pos: [68, getTerrainHeight(68, 42, terrainConfig), 42], scale: 1.4 },
        { id: 'stump_3', pos: [22, getTerrainHeight(22, 18, terrainConfig), 18], scale: 1.0 },
    ], [terrainConfig])

    return (
        <group frustumCulled={false}>
            {/* Render Trees according to visual mode: Voxel (Gaming), Parametric (Scientific), or High-poly OBJ (Realistic) */}
            {treePositions.map((tree) => {
                if (isGamified) {
                    return <VoxelTree key={tree.id} position={tree.pos} scale={tree.scale} variant={tree.variant} terrainConfig={terrainConfig} />
                }
                if (isScientific) {
                    return <ScientificTree key={tree.id} position={tree.pos} scale={tree.scale} variant={tree.variant} season={season} terrainConfig={terrainConfig} />
                }
                return <RealisticTree key={tree.id} position={tree.pos} scale={tree.scale} windSpeed={windSpeed} season={season} modelUrl={tree.modelUrl} terrainConfig={terrainConfig} />
            })}

            {/* Render Grand Ancient Hollow Stump in Gamified mode, or Realistic 3D Stumps in Realistic mode */}
            {isGamified ? (
                <VoxelHollowStump position={[56, 0, 48]} scale={1.2} terrainConfig={terrainConfig} />
            ) : (
                stumpPositions.map((stump) => (
                    <Stump3D key={stump.id} position={stump.pos} scale={stump.scale} terrainConfig={terrainConfig} />
                ))
            )}

            {/* Flora Floor: Voxel Plants in Gamified mode vs Systematic Flora Floor in Realistic/Scientific mode */}
            {isGamified ? (
                <GamifiedVoxelFlora terrainConfig={terrainConfig} />
            ) : (
                <RealisticGroundFlora windSpeed={windSpeed} season={season} terrainConfig={terrainConfig} />
            )}
        </group>
    )
}



