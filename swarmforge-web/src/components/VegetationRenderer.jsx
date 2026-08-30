import React, { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import LowPolyModel from './LowPolyModel'
import { getTerrainHeight, getEffectiveSeason } from '../utils/terrainUtils'

/**
 * Single Voxel Tree Component (Gamified Mode)
 * Built strictly out of stacked 3D cubes snapped to integer grid coords.
 */
function VoxelTree({ position, scale = 1.0, terrainConfig }) {
    const [posX, _, posZ] = position
    const gridX = Math.round(posX)
    const gridZ = Math.round(posZ)
    const posY = getTerrainHeight(gridX, gridZ, terrainConfig)

    // Generate voxel blocks for trunk and foliage
    const { trunkVoxels, foliageVoxels } = useMemo(() => {
        const trunk = []
        const foliage = []

        const trunkHeight = Math.max(4, Math.round(5 * scale))

        // 1. Trunk Cubes (Brown Voxel Column resting on ground)
        for (let y = 0; y < trunkHeight; y++) {
            trunk.push({ x: 0, y: y + 0.5, z: 0 })
        }
        // Voxel Root Base blocks at ground level
        trunk.push({ x: 1, y: 0.5, z: 0 })
        trunk.push({ x: -1, y: 0.5, z: 0 })
        trunk.push({ x: 0, y: 0.5, z: 1 })
        trunk.push({ x: 0, y: 0.5, z: -1 })

        // 2. Foliage Cubes (Layered green voxel canopy)
        const canopyBaseY = trunkHeight - 2
        for (let dx = -2; dx <= 2; dx++) {
            for (let dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                foliage.push({
                    x: dx,
                    y: canopyBaseY + 0.5,
                    z: dz,
                    color: (dx + dz) % 2 === 0 ? '#15803d' : '#166534'
                })
            }
        }

        for (let dx = -1; dx <= 1; dx++) {
            for (let dz = -1; dz <= 1; dz++) {
                foliage.push({
                    x: dx,
                    y: canopyBaseY + 1.5,
                    z: dz,
                    color: '#22c55e'
                })
            }
        }

        foliage.push({ x: 0, y: canopyBaseY + 2.5, z: 0, color: '#4ade80' })

        return { trunkVoxels: trunk, foliageVoxels: foliage }
    }, [scale])

    return (
        <group position={[gridX, posY, gridZ]}>
            {trunkVoxels.map((v, i) => (
                <mesh key={`trunk-${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color="#5c3a21" roughness={0.9} metalness={0.05} />
                </mesh>
            ))}

            {foliageVoxels.map((v, i) => (
                <mesh key={`foliage-${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color={v.color} roughness={0.85} metalness={0.05} />
                </mesh>
            ))}
        </group>
    )
}

/**
 * Single Realistic Tree Component (Realistic Mode)
 * Uses downloaded Low Poly 3D models (/3d/LOW_POLY_set.glb & /3d/tree.obj)
 * anchored to ground elevation Y = getTerrainHeight(x, z).
 */
function RealisticTree({ position, scale = 1.0, windSpeed = 2.4, season = 'SUMMER', modelUrl = '/3d/LOW_POLY_set.glb', terrainConfig }) {
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
 * Realistic Ground Vegetation & Low Poly Plant Sets (Ferns, Bamboo, Cactus, Tropical Flora)
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
                if (child.userData.type === 'GRASS' || child.userData.type === 'FERN') {
                    child.rotation.z = Math.sin(t * 2 + idx) * 0.05
                }
            })
        }
    })

    return (
        <group ref={groupRef}>
            {/* Downloaded Low Poly Bamboo Clusters */}
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

            {/* Downloaded Low Poly Cacti */}
            <LowPolyModel
                url="/3d/cactus.obj"
                position={[88, getTerrainHeight(88, 18, terrainConfig), 18]}
                scale={0.5}
                rotation={[0, 0.5, 0]}
                season={season}
                fallbackGeometry="cylinder"
                fallbackColor="#15803d"
            />

            {/* Downloaded Low Poly Tropical Plant Sets & FBX Pack */}
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
                url="/3d/stylized_tropical_pack.fbx"
                position={[28, getTerrainHeight(28, 22, terrainConfig), 22]}
                scale={0.015}
                rotation={[0, 2.1, 0]}
                sway={true}
                windSpeed={windSpeed}
                season={season}
                fallbackGeometry="dodecahedron"
                fallbackColor="#15803d"
            />

            {/* Downloaded Low Poly Forest Nature Set */}
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
                        <mesh key={item.id} position={[item.x, item.y + 0.08 * item.scale, item.z]} rotation={[0, item.rotY, 0]} receiveShadow>
                            <dodecahedronGeometry args={[0.6 * item.scale, 1]} />
                            <meshStandardMaterial color={mossColor} roughness={0.95} />
                        </mesh>
                    )
                }

                if (item.type === 'FERN') {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]} userData={{ type: 'FERN' }}>
                            {[0, 1.05, 2.1, 3.15, 4.2, 5.25].map((angle, fIdx) => (
                                <mesh key={fIdx} position={[0, 0.3 * item.scale, 0]} rotation={[0.4, angle, 0.1]} castShadow>
                                    <boxGeometry args={[0.35 * item.scale, 0.05 * item.scale, 1.1 * item.scale]} />
                                    <meshStandardMaterial color={fernColor} roughness={0.85} />
                                </mesh>
                            ))}
                        </group>
                    )
                }

                if (item.type === 'GRASS') {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]} userData={{ type: 'GRASS' }}>
                            {[-0.15, 0, 0.15].map((offset, gIdx) => (
                                <mesh key={gIdx} position={[offset, 0.4 * item.scale, offset]} rotation={[0.15 * gIdx, gIdx * 0.8, 0.1]} castShadow>
                                    <boxGeometry args={[0.08 * item.scale, 0.8 * item.scale, 0.15 * item.scale]} />
                                    <meshStandardMaterial color={grassColor} roughness={0.85} />
                                </mesh>
                            ))}
                        </group>
                    )
                }

                // Wildflower / Mushroom cluster
                return (
                    <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]}>
                        <mesh position={[0, 0.3 * item.scale, 0]} castShadow>
                            <cylinderGeometry args={[0.04 * item.scale, 0.06 * item.scale, 0.6 * item.scale, 8]} />
                            <meshStandardMaterial color="#f8fafc" roughness={0.9} />
                        </mesh>
                        <mesh position={[0, 0.6 * item.scale, 0]} castShadow>
                            <sphereGeometry args={[0.22 * item.scale, 12, 12]} />
                            <meshStandardMaterial color={item.id % 2 === 0 ? '#f43f5e' : '#fbbf24'} roughness={0.5} />
                        </mesh>
                    </group>
                )
            })}
        </group>
    )
}

/**
 * Gamified Mode Voxel Flora Renderer
 * Renders blocky 3D voxel shrubs, flowers, and cacti across the terrain.
 */
function GamifiedVoxelFlora({ terrainConfig }) {
    const voxelPlants = useMemo(() => {
        const items = []
        const rand = (seed) => {
            const x = Math.sin(seed * 14.123 + 45.67) * 43758.5453
            return x - Math.floor(x)
        }
        for (let i = 0; i < 40; i++) {
            const gridX = Math.round(rand(i * 1.5) * 90 + 5)
            const gridZ = Math.round(rand(i * 2.8) * 90 + 5)
            if (gridX >= 18 && gridX <= 32) continue
            const groundY = getTerrainHeight(gridX, gridZ, terrainConfig)
            const typeInt = Math.floor(rand(i * 3.3) * 3)
            items.push({ id: i, x: gridX, y: groundY, z: gridZ, type: typeInt })
        }
        return items
    }, [terrainConfig])

    return (
        <group>
            {voxelPlants.map((item) => (
                <group key={item.id} position={[item.x, item.y, item.z]}>
                    {item.type === 0 ? (
                        <mesh position={[0, 0.5, 0]} castShadow frustumCulled={false}>
                            <boxGeometry args={[1, 1, 1]} />
                            <meshStandardMaterial color="#166534" roughness={0.9} />
                        </mesh>
                    ) : item.type === 1 ? (
                        <group>
                            <mesh position={[0, 0.5, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.3, 1, 0.3]} />
                                <meshStandardMaterial color="#15803d" roughness={0.9} />
                            </mesh>
                            <mesh position={[0, 1.1, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.5, 0.5, 0.5]} />
                                <meshStandardMaterial color={item.id % 2 === 0 ? '#ef4444' : '#eab308'} roughness={0.6} />
                            </mesh>
                        </group>
                    ) : (
                        <group>
                            <mesh position={[0, 1.0, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.8, 2.0, 0.8]} />
                                <meshStandardMaterial color="#4d7c0f" roughness={0.8} />
                            </mesh>
                        </group>
                    )}
                </group>
            ))}
        </group>
    )
}

export default function VegetationRenderer() {
    const { lookAndFeel, climateEngine, terrainConfig } = useSimulationStore()
    const isGamified = lookAndFeel === 'GAMING' || lookAndFeel === 'SCIENTIFIC'
    const windSpeed = climateEngine?.windSpeedMs ?? 2.4
    
    // Effective season flipped for Southern Hemisphere
    const rawSeason = climateEngine?.currentSeason || climateEngine?.season || 'SUMMER'
    const season = getEffectiveSeason(rawSeason, climateEngine?.hemisphere || 'NORTHERN')

    // Tree positions (Snapping to grid coords for voxel trees)
    const treePositions = useMemo(() => [
        { id: 1, pos: [15, 0, 30], scale: 1.2, modelUrl: '/3d/LOW_POLY_set.glb' },
        { id: 2, pos: [78, 0, 25], scale: 1.4, modelUrl: '/3d/tree.obj' },
        { id: 3, pos: [82, 0, 75], scale: 1.1, modelUrl: '/3d/forest_nature_set_all_in.obj' },
        { id: 4, pos: [18, 0, 80], scale: 1.3, modelUrl: '/3d/LOW_POLY_set.glb' },
    ], [])

    return (
        <group>
            {/* Render Voxel Trees in Gamified Mode, or Low Poly 3D Trees in Realistic/Naturel Mode */}
            {treePositions.map((tree) => (
                isGamified ? (
                    <VoxelTree key={tree.id} position={tree.pos} scale={tree.scale} terrainConfig={terrainConfig} />
                ) : (
                    <RealisticTree key={tree.id} position={tree.pos} scale={tree.scale} windSpeed={windSpeed} season={season} modelUrl={tree.modelUrl} terrainConfig={terrainConfig} />
                )
            ))}

            {/* Flora Floor: Voxel Plants in Gamified mode vs Low Poly Vegetation Floor in Realistic mode */}
            {isGamified ? (
                <GamifiedVoxelFlora terrainConfig={terrainConfig} />
            ) : (
                <RealisticGroundFlora windSpeed={windSpeed} season={season} terrainConfig={terrainConfig} />
            )}
        </group>
    )
}


