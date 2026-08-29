import React, { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'

/**
 * Single Voxel Tree Component (Gamified Mode)
 * Built strictly out of stacked 3D cubes snapped to integer grid coords.
 */
function VoxelTree({ position, scale = 1.0 }) {
    const [posX, posY, posZ] = position
    const gridX = Math.round(posX)
    const gridZ = Math.round(posZ)

    // Generate voxel blocks for trunk and foliage
    const { trunkVoxels, foliageVoxels } = useMemo(() => {
        const trunk = []
        const foliage = []

        const trunkHeight = Math.max(4, Math.round(5 * scale))

        // 1. Trunk Cubes (Brown Voxel Column resting on ground at Y=0)
        for (let y = 0; y < trunkHeight; y++) {
            trunk.push({ x: 0, y: y + 0.5, z: 0 })
        }
        // Voxel Root Base blocks at ground level Y=0
        trunk.push({ x: 1, y: 0.5, z: 0 })
        trunk.push({ x: -1, y: 0.5, z: 0 })
        trunk.push({ x: 0, y: 0.5, z: 1 })
        trunk.push({ x: 0, y: 0.5, z: -1 })

        // 2. Foliage Cubes (Layered green voxel canopy)
        const canopyBaseY = trunkHeight - 2
        // Bottom wide tier (5x5 voxel layer)
        for (let dx = -2; dx <= 2; dx++) {
            for (let dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue // chamfer corners
                foliage.push({
                    x: dx,
                    y: canopyBaseY + 0.5,
                    z: dz,
                    color: (dx + dz) % 2 === 0 ? '#15803d' : '#166534'
                })
            }
        }

        // Middle tier (3x3 voxel layer)
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

        // Top cap tier (1x1 voxel layer)
        foliage.push({ x: 0, y: canopyBaseY + 2.5, z: 0, color: '#4ade80' })

        return { trunkVoxels: trunk, foliageVoxels: foliage }
    }, [scale])

    return (
        <group position={[gridX, posY, gridZ]}>
            {/* Trunk Voxel Cubes */}
            {trunkVoxels.map((v, i) => (
                <mesh key={`trunk-${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color="#5c3a21" roughness={0.9} metalness={0.05} />
                </mesh>
            ))}

            {/* Foliage Voxel Cubes */}
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
 * Features anchored root base at Y=0 (zero sliding/lifting) and wind sway applied to upper branches/foliage.
 */
function RealisticTree({ position, scale = 1.0, windSpeed = 2.4 }) {
    const swayGroupRef = useRef()

    useFrame((state) => {
        if (swayGroupRef.current) {
            const t = state.clock.getElapsedTime()
            // Sway rotation applied around origin pivot at base Y=0
            const swayX = Math.sin(t * (1.2 + windSpeed * 0.2)) * 0.035
            const swayZ = Math.cos(t * (0.9 + windSpeed * 0.15)) * 0.025
            swayGroupRef.current.rotation.x = swayX
            swayGroupRef.current.rotation.z = swayZ
        }
    })

    return (
        <group position={position}>
            {/* 1. Anchored Root Base at Y=0 (Fixed in Topsoil, non-moving) */}
            <group position={[0, 0, 0]}>
                {/* Flared organic root buttresses */}
                {[-0.4, 0.4].map((rx, idx) => (
                    <mesh key={idx} position={[rx * scale, 0.3 * scale, 0]} rotation={[0, 0, rx > 0 ? -0.4 : 0.4]} castShadow>
                        <cylinderGeometry args={[0.15 * scale, 0.45 * scale, 0.8 * scale, 8]} />
                        <meshStandardMaterial color="#2d1c0c" roughness={0.95} />
                    </mesh>
                ))}
            </group>

            {/* 2. Upper Trunk & Canopy Group (Sways gently with wind around Y=0 pivot) */}
            <group ref={swayGroupRef} position={[0, 0, 0]}>
                {/* Main Tapered Trunk */}
                <mesh position={[0, 3.0 * scale, 0]} castShadow receiveShadow>
                    <cylinderGeometry args={[0.35 * scale, 0.55 * scale, 6.0 * scale, 12]} />
                    <meshStandardMaterial color="#3d2817" roughness={0.9} />
                </mesh>

                {/* Major Branching Structure */}
                <mesh position={[0.8 * scale, 4.8 * scale, 0.4 * scale]} rotation={[0.4, 0.2, -0.5]} castShadow>
                    <cylinderGeometry args={[0.18 * scale, 0.28 * scale, 2.5 * scale, 8]} />
                    <meshStandardMaterial color="#3d2817" roughness={0.9} />
                </mesh>
                <mesh position={[-0.7 * scale, 5.0 * scale, -0.3 * scale]} rotation={[-0.3, -0.3, 0.6]} castShadow>
                    <cylinderGeometry args={[0.15 * scale, 0.25 * scale, 2.2 * scale, 8]} />
                    <meshStandardMaterial color="#3d2817" roughness={0.9} />
                </mesh>

                {/* Rich Layered Organic Leaf Canopies */}
                <mesh position={[0, 6.2 * scale, 0]} castShadow receiveShadow>
                    <dodecahedronGeometry args={[2.4 * scale, 2]} />
                    <meshStandardMaterial color="#1e5c2b" roughness={0.8} />
                </mesh>
                <mesh position={[1.2 * scale, 5.5 * scale, 0.6 * scale]} castShadow receiveShadow>
                    <dodecahedronGeometry args={[1.8 * scale, 1]} />
                    <meshStandardMaterial color="#15803d" roughness={0.8} />
                </mesh>
                <mesh position={[-1.1 * scale, 5.8 * scale, -0.5 * scale]} castShadow receiveShadow>
                    <dodecahedronGeometry args={[1.7 * scale, 1]} />
                    <meshStandardMaterial color="#22c55e" roughness={0.8} />
                </mesh>
                <mesh position={[0, 7.3 * scale, 0]} castShadow receiveShadow>
                    <dodecahedronGeometry args={[1.4 * scale, 1]} />
                    <meshStandardMaterial color="#4ade80" roughness={0.75} />
                </mesh>
            </group>
        </group>
    )
}

/**
 * Realistic Ground Vegetation Layer (Ferns, Moss, Grass Tufts, Wildflowers)
 * Displayed in Realistic Mode to populate the topsoil with rich flora.
 */
function RealisticGroundFlora({ windSpeed = 2.4 }) {
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

            // Avoid river channel (X between 18 and 32)
            if (x >= 18 && x <= 32) {
                x = x < 25 ? 15 : 35
            }

            const typeInt = Math.floor(rand(i * 3.7) * 4)
            const type = typeInt === 0 ? 'FERN' : typeInt === 1 ? 'MOSS' : typeInt === 2 ? 'GRASS' : 'FLOWER'
            const scale = 0.5 + rand(i * 5.1) * 0.8
            const rotY = rand(i * 7.9) * Math.PI * 2

            items.push({ id: i, x, z, type, scale, rotY })
        }
        return items
    }, [])

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
            {floraItems.map((item) => {
                if (item.type === 'MOSS') {
                    return (
                        <mesh key={item.id} position={[item.x, 0.08 * item.scale, item.z]} rotation={[0, item.rotY, 0]} receiveShadow>
                            <dodecahedronGeometry args={[0.6 * item.scale, 1]} />
                            <meshStandardMaterial color="#3f6212" roughness={0.95} />
                        </mesh>
                    )
                }

                if (item.type === 'FERN') {
                    return (
                        <group key={item.id} position={[item.x, 0, item.z]} rotation={[0, item.rotY, 0]} userData={{ type: 'FERN' }}>
                            {[0, 1.2, 2.4, 3.6, 4.8].map((angle, fIdx) => (
                                <mesh key={fIdx} position={[0, 0.25 * item.scale, 0]} rotation={[0.5, angle, 0]} castShadow>
                                    <coneGeometry args={[0.12 * item.scale, 0.9 * item.scale, 4]} />
                                    <meshStandardMaterial color="#15803d" roughness={0.8} />
                                </mesh>
                            ))}
                        </group>
                    )
                }

                if (item.type === 'GRASS') {
                    return (
                        <group key={item.id} position={[item.x, 0, item.z]} rotation={[0, item.rotY, 0]} userData={{ type: 'GRASS' }}>
                            {[-0.1, 0, 0.1].map((offset, gIdx) => (
                                <mesh key={gIdx} position={[offset, 0.3 * item.scale, offset]} rotation={[0.1 * gIdx, gIdx * 0.8, 0.1]} castShadow>
                                    <cylinderGeometry args={[0.02 * item.scale, 0.05 * item.scale, 0.7 * item.scale, 4]} />
                                    <meshStandardMaterial color="#65a30d" roughness={0.85} />
                                </mesh>
                            ))}
                        </group>
                    )
                }

                // Wildflower / Mushroom cluster
                return (
                    <group key={item.id} position={[item.x, 0, item.z]} rotation={[0, item.rotY, 0]}>
                        <mesh position={[0, 0.25 * item.scale, 0]} castShadow>
                            <cylinderGeometry args={[0.03 * item.scale, 0.04 * item.scale, 0.5 * item.scale, 6]} />
                            <meshStandardMaterial color="#e2e8f0" roughness={0.9} />
                        </mesh>
                        <mesh position={[0, 0.5 * item.scale, 0]} castShadow>
                            <sphereGeometry args={[0.18 * item.scale, 8, 8]} />
                            <meshStandardMaterial color={item.id % 2 === 0 ? '#f43f5e' : '#fbbf24'} roughness={0.6} />
                        </mesh>
                    </group>
                )
            })}
        </group>
    )
}

export default function VegetationRenderer() {
    const { lookAndFeel, climateEngine } = useSimulationStore()
    const isGamified = lookAndFeel === 'GAMING' || lookAndFeel === 'SCIENTIFIC'
    const windSpeed = climateEngine?.windSpeedMs ?? 2.4

    // Tree positions (Snapping to grid coords for voxel trees)
    const treePositions = useMemo(() => [
        { id: 1, pos: [15, 0, 30], scale: 1.2 },
        { id: 2, pos: [78, 0, 25], scale: 1.4 },
        { id: 3, pos: [82, 0, 75], scale: 1.1 },
        { id: 4, pos: [18, 0, 80], scale: 1.3 },
    ], [])

    return (
        <group>
            {/* Render Voxel Trees in Gamified Mode, or Realistic Anchored Trees in Realistic Mode */}
            {treePositions.map((tree) => (
                isGamified ? (
                    <VoxelTree key={tree.id} position={tree.pos} scale={tree.scale} />
                ) : (
                    <RealisticTree key={tree.id} position={tree.pos} scale={tree.scale} windSpeed={windSpeed} />
                )
            ))}

            {/* Dense Vegetation Floor in Realistic Mode */}
            {!isGamified && <RealisticGroundFlora windSpeed={windSpeed} />}
        </group>
    )
}
