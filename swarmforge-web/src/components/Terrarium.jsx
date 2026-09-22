import { useRef, useMemo, useEffect } from 'react'
import { useFrame, useThree } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import AntMesh from './AntMesh'
import LODAnts from './LODAnts'
import FoodSource from './FoodSource'
import Predator from './Predator'
import NestRenderer from './NestRenderer'
import VegetationRenderer from './VegetationRenderer'
import ScientificIsolines from './ScientificIsolines'
import { soundEngine } from '../utils/soundEngine'
import { getTerrainHeight, getSubstrateAt } from '../utils/terrainUtils'
import {
    getPixelGrassTopTexture,
    getPixelGrassSideTexture,
    getPixelCobbleTexture,
    getPixelSandTexture,
    getPixelWaterTexture
} from '../utils/pixelTextures'

/**
 * Realistic Mode Atmospheric Macro Pollen & Night Fireflies
 */
function MacroAtmosphere({ isNight, terrainConfig }) {
    const groupRef = useRef()
    const particles = useMemo(() => {
        const pts = []
        for (let i = 0; i < 50; i++) {
            pts.push({
                x: Math.random() * 90 + 5,
                y: Math.random() * 6 + 0.5,
                z: Math.random() * 90 + 5,
                speed: 0.2 + Math.random() * 0.5,
                phase: Math.random() * Math.PI * 2,
                size: isNight ? 0.22 : 0.10
            })
        }
        return pts
    }, [isNight])

    useFrame((state) => {
        if (groupRef.current) {
            const t = state.clock.getElapsedTime()
            groupRef.current.children.forEach((mesh, idx) => {
                const p = particles[idx]
                mesh.position.y = p.y + Math.sin(t * p.speed + p.phase) * 0.35
                mesh.position.x = (mesh.position.x + Math.sin(t * 0.2 + idx) * 0.02)
            })
        }
    })

    return (
        <group ref={groupRef} frustumCulled={false}>
            {particles.map((p, i) => (
                <mesh key={i} position={[p.x, p.y, p.z]}>
                    <sphereGeometry args={[p.size, 6, 6]} />
                    <meshBasicMaterial
                        color={isNight ? '#a3e635' : '#fef08a'}
                        transparent
                        opacity={isNight ? 0.85 : 0.50}
                    />
                </mesh>
            ))}
        </group>
    )
}

/**
 * Gamified Mode Floating Voxel Pheromone Particles
 */
function GamifiedVoxelParticles({ terrainConfig }) {
    const groupRef = useRef()
    const voxels = useMemo(() => {
        const v = []
        for (let i = 0; i < 35; i++) {
            const x = Math.random() * 90 + 5
            const z = Math.random() * 90 + 5
            const groundY = getTerrainHeight(x, z, terrainConfig)
            v.push({
                x,
                baseY: groundY + 0.2 + Math.random() * 0.8,
                z,
                color: i % 3 === 0 ? '#38bdf8' : (i % 3 === 1 ? '#f59e0b' : '#f43f5e'),
                speed: 0.8 + Math.random() * 1.2
            })
        }
        return v
    }, [terrainConfig])

    useFrame((state) => {
        if (groupRef.current) {
            const t = state.clock.getElapsedTime()
            groupRef.current.children.forEach((mesh, idx) => {
                const v = voxels[idx]
                mesh.position.y = v.baseY + Math.sin(t * v.speed + idx) * 0.25
                mesh.rotation.y = t * 0.8 + idx
            })
        }
    })

    return (
        <group ref={groupRef} frustumCulled={false}>
            {voxels.map((v, i) => (
                <mesh key={i} position={[v.x, v.baseY, v.z]}>
                    <boxGeometry args={[0.22, 0.22, 0.22]} />
                    <meshStandardMaterial
                        color={v.color}
                        emissive={v.color}
                        emissiveIntensity={0.6}
                        roughness={0.3}
                    />
                </mesh>
            ))}
        </group>
    )
}

/**
 * Stepped Voxel Terrain for Gamified Mode
 */
function VoxelTerrain({ terrainConfig, slicePlaneRatio = 1.0, show3DSkirt = true }) {
    const Y_BASE = -4.0
    const STEP = 2.0

    const grassTopTex = useMemo(() => getPixelGrassTopTexture(), [])
    const cobbleTex = useMemo(() => getPixelCobbleTexture(), [])
    const sandTex = useMemo(() => getPixelSandTexture(), [])
    const waterTex = useMemo(() => getPixelWaterTexture(), [])

    const maxZ = 100 * slicePlaneRatio

    const { terrainBlocks, cutawayBlocks } = useMemo(() => {
        const blocks = []
        const skirt = []
        const hasRiver = terrainConfig?.hasRiver ?? true
        const riverX = terrainConfig?.riverX ?? 25
        const riverWidth = terrainConfig?.riverWidth ?? 12

        const hash = (x, z) => {
            const val = Math.sin(x * 12.9898 + z * 78.233) * 43758.5453
            return val - Math.floor(val)
        }

        for (let x = STEP / 2; x <= 100; x += STEP) {
            for (let z = STEP / 2; z <= maxZ; z += STEP) {
                const heightY = getTerrainHeight(x, z, terrainConfig)
                const distToRiver = Math.abs(x - riverX)
                const isRiver = hasRiver && distToRiver < riverWidth / 2
                const isBeach = hasRiver && distToRiver >= riverWidth / 2 && distToRiver < (riverWidth / 2 + 3.0)
                const isRock = (heightY > 1.8 || (heightY > 1.0 && hash(x, z) > 0.65)) && !isRiver && !isBeach

                const surfaceY = isRiver ? -0.05 : heightY
                const blockHeight = Math.max(0.4, surfaceY - Y_BASE)
                const posY = Y_BASE + blockHeight / 2

                let topColor = '#4d7c0f'
                if (isRiver) topColor = '#0284c7'
                else if (isBeach) topColor = '#ca8a04'
                else if (isRock) topColor = '#64748b'
                else if (hash(x * 2.1, z * 3.7) > 0.6) topColor = '#3f6212'

                blocks.push({
                    id: `v_${Math.round(x)}_${Math.round(z)}`,
                    x,
                    y: posY,
                    z,
                    width: STEP - 0.05,
                    height: blockHeight,
                    isRiver,
                    isRock,
                    isBeach,
                    topColor
                })
            }
        }

        if (show3DSkirt) {
            const createWallSlice = (side, count) => {
                for (let i = 0; i < count; i++) {
                    const coord = (i + 0.5) * (100 / count)
                    let x = 0, z = 0
                    if (side === 'N') { x = coord; z = 0.5 }
                    else if (side === 'S') { x = coord; z = Math.min(99.5, maxZ - 0.5) }
                    else if (side === 'W') { x = 0.5; z = (i + 0.5) * (maxZ / count) }
                    else { x = 99.5; z = (i + 0.5) * (maxZ / count) }

                    if (z > maxZ) continue

                    const surfaceY = getTerrainHeight(x, z, terrainConfig)
                    const topsoilH = Math.max(0.2, surfaceY - (-0.8))

                    // Humus Stratum
                    skirt.push({
                        id: `sk_top_${side}_${i}`,
                        x, y: -0.8 + topsoilH / 2, z,
                        w: (100 / count) - 0.05, h: topsoilH,
                        color: '#452b18'
                    })
                    // Clay Subsoil
                    skirt.push({
                        id: `sk_mid_${side}_${i}`,
                        x, y: -1.8, z,
                        w: (100 / count) - 0.05, h: 2.0,
                        color: '#854d0e'
                    })
                    // Bedrock
                    skirt.push({
                        id: `sk_bed_${side}_${i}`,
                        x, y: -3.4, z,
                        w: (100 / count) - 0.05, h: 1.2,
                        color: '#334155'
                    })
                }
            }
            createWallSlice('N', 25)
            createWallSlice('S', 25)
            createWallSlice('W', 25)
            createWallSlice('E', 25)
        }

        return { terrainBlocks: blocks, cutawayBlocks: skirt }
    }, [terrainConfig, maxZ, show3DSkirt])

    return (
        <group frustumCulled={false}>
            {terrainBlocks.map((b) => (
                <mesh key={b.id} position={[b.x, b.y, b.z]} receiveShadow castShadow frustumCulled={false}>
                    <boxGeometry args={[b.width, b.height, b.width]} />
                    <meshStandardMaterial
                        color={b.topColor}
                        map={b.isRiver ? waterTex : (b.isRock ? cobbleTex : (b.isBeach ? sandTex : grassTopTex))}
                        roughness={b.isRiver ? 0.2 : 0.85}
                        metalness={b.isRiver ? 0.6 : 0.05}
                        transparent={b.isRiver}
                        opacity={b.isRiver ? 0.85 : 1.0}
                    />
                </mesh>
            ))}

            {show3DSkirt && cutawayBlocks.map((s) => (
                <mesh key={s.id} position={[s.x, s.y, s.z]} receiveShadow castShadow frustumCulled={false}>
                    <boxGeometry args={[s.w, s.h, s.w]} />
                    <meshStandardMaterial color={s.color} roughness={0.9} metalness={0.05} />
                </mesh>
            ))}
        </group>
    )
}

/**
 * 3D Ant Selection Target Spotlight Reticle (Neon Cyan + Amber Rotating Double Ring)
 */
function AntSelectionReticle3D({ ant, terrainConfig }) {
    const groupRef = useRef()

    useFrame((state, delta) => {
        if (groupRef.current) {
            groupRef.current.rotation.y += delta * 2.2
            const t = state.clock.getElapsedTime()
            const pulse = 1.0 + Math.sin(t * 5.0) * 0.12
            groupRef.current.scale.set(pulse, pulse, pulse)
        }
    })

    if (!ant) return null

    const antX = ant.x ?? 50
    const antZ = ant.z ?? (ant.y ?? 50)
    const antY = getTerrainHeight(antX, antZ, terrainConfig) + 0.15

    return (
        <group ref={groupRef} position={[antX, antY, antZ]}>
            {/* Outer Cyan Glowing Ring */}
            <mesh rotation={[-Math.PI / 2, 0, 0]}>
                <torusGeometry args={[0.55, 0.03, 16, 32]} />
                <meshBasicMaterial color="#38bdf8" transparent opacity={0.85} />
            </mesh>
            {/* Inner Amber Glowing Ring */}
            <mesh rotation={[-Math.PI / 2, 0, 0]}>
                <torusGeometry args={[0.32, 0.02, 16, 32]} />
                <meshBasicMaterial color="#f59e0b" transparent opacity={0.90} />
            </mesh>
            {/* 4 Crosshairs */}
            {[0, Math.PI / 2, Math.PI, (3 * Math.PI) / 2].map((angle, idx) => (
                <mesh key={idx} position={[Math.cos(angle) * 0.55, 0, Math.sin(angle) * 0.55]} rotation={[0, -angle, 0]}>
                    <boxGeometry args={[0.02, 0.02, 0.12]} />
                    <meshBasicMaterial color="#38bdf8" />
                </mesh>
            ))}
        </group>
    )
}

/**
 * 3D Voxel Cursor Highlight (Wireframe box + translucent planar indicator)
 */
function VoxelCursor3D({ hoveredVoxel, terrainConfig }) {
    if (!hoveredVoxel || !hoveredVoxel.isHovering) return null
    const vx = Math.round(hoveredVoxel.x ?? 50)
    const vz = Math.round(hoveredVoxel.z ?? 50)
    const vy = getTerrainHeight(vx, vz, terrainConfig) + 0.05

    return (
        <group position={[vx, vy, vz]}>
            {/* Flat neon voxel tile indicator */}
            <mesh rotation={[-Math.PI / 2, 0, 0]}>
                <planeGeometry args={[1.0, 1.0]} />
                <meshBasicMaterial color="#38bdf8" transparent opacity={0.4} depthWrite={false} side={THREE.DoubleSide} />
            </mesh>
            {/* Wireframe box border */}
            <lineSegments>
                <edgesGeometry args={[new THREE.BoxGeometry(1.0, 0.2, 1.0)]} />
                <lineBasicMaterial color="#38bdf8" linewidth={2} />
            </lineSegments>
        </group>
    )
}

export default function Terrarium() {
    const {
        ants,
        pheromones,
        foodSources,
        predators,
        environment,
        terrainConfig,
        lookAndFeel,
        showTerrain,
        show3DSkirt,
        showVegetation,
        showAnts,
        slicePlaneRatio,
        selectedEntity,
        setSelectedEntity,
        hoveredVoxel,
        setHoveredVoxel,
        isUVVisionMode
    } = useSimulationStore()

    const groupRef = useRef()
    const riverMeshRef = useRef()
    const { camera, gl } = useThree()

    // Enable local clipping on WebGL renderer for axial cross-sections
    useEffect(() => {
        if (gl) {
            gl.localClippingEnabled = true
        }
    }, [gl])

    const isGamified = lookAndFeel === 'GAMING' || lookAndFeel === 'GAMIFIED'

    // Clipping plane slicing along Z axis (0 to 100m)
    const clippingPlanes = useMemo(() => {
        const ratio = typeof slicePlaneRatio === 'number' && !isNaN(slicePlaneRatio) ? slicePlaneRatio : 1.0
        if (ratio >= 0.999) return []
        const sliceZ = 100 * Math.max(0.01, Math.min(1.0, ratio))
        return [new THREE.Plane(new THREE.Vector3(0, 0, -1), sliceZ)]
    }, [slicePlaneRatio])

    // Realtime spatial listener & river audio update based on 3D camera distance
    useFrame((state) => {
        if (camera) {
            soundEngine.updateSpatialListener(camera.position.x, camera.position.y, camera.position.z)
            soundEngine.updateRiverSound(camera.position, { x: 25, y: 0, z: 50 })
        }
        if (riverMeshRef.current) {
            riverMeshRef.current.position.y = 0.02 + Math.sin(state.clock.getElapsedTime() * 2) * 0.01
        }
    })

    // Ground Surface geometry with multi-biome vertex colors (Grass, Sand, Rocks, Earth)
    const groundGeometry = useMemo(() => {
        const geo = new THREE.PlaneGeometry(100, 100, 100, 100)
        geo.rotateX(-Math.PI / 2)
        const pos = geo.attributes.position
        const count = pos.count
        const colors = new Float32Array(count * 3)

        const hasRiver = terrainConfig?.hasRiver ?? true
        const riverX = terrainConfig?.riverX ?? 25
        const riverWidth = terrainConfig?.riverWidth ?? 12

        for (let i = 0; i < count; i++) {
            const worldX = pos.getX(i) + 50
            const worldZ = pos.getZ(i) + 50
            const y = getTerrainHeight(worldX, worldZ, terrainConfig)
            pos.setY(i, y)

            const distToRiver = Math.abs(worldX - riverX)
            const isRiver = hasRiver && distToRiver < riverWidth / 2
            const isSand = hasRiver && distToRiver >= riverWidth / 2 && distToRiver < (riverWidth / 2 + 3.0)
            const isRock = (y > 1.8 || (y > 1.0 && Math.sin(worldX * 0.4 + worldZ * 0.4) > 0.45)) && !isRiver && !isSand

            let col = new THREE.Color('#3f6212') // Lush green grass
            if (isRiver) {
                col = new THREE.Color('#0369a1') // River bed
            } else if (isSand) {
                col = new THREE.Color('#ca8a04') // Golden sand bank
            } else if (isRock) {
                col = new THREE.Color('#64748b') // Granite rock
            } else if (y < 0.6) {
                col = new THREE.Color('#4d7c0f') // Low meadow grass
            } else if (y > 1.4) {
                col = new THREE.Color('#365314') // Dark alpine grass
            }

            if (lookAndFeel === 'SCIENTIFIC') {
                const normY = Math.max(0, Math.min(1, y / 3.0))
                col = new THREE.Color().setHSL(0.35 - normY * 0.25, 0.6, 0.35 + normY * 0.2)
            }

            if (isUVVisionMode) {
                col = new THREE.Color('#7c3aed')
            }

            colors[i * 3] = col.r
            colors[i * 3 + 1] = col.g
            colors[i * 3 + 2] = col.b
        }

        geo.setAttribute('color', new THREE.BufferAttribute(colors, 3))
        geo.computeVertexNormals()
        geo.computeBoundingBox()
        geo.computeBoundingSphere()
        return geo
    }, [terrainConfig, lookAndFeel, isUVVisionMode])

    const groundMaterial = useMemo(() => {
        return new THREE.MeshStandardMaterial({
            vertexColors: true,
            roughness: 0.8,
            metalness: 0.05,
            clippingPlanes: clippingPlanes,
            clipShadows: true,
            side: THREE.DoubleSide
        })
    }, [clippingPlanes])

    // River Geometry & Material
    const riverGeometry = useMemo(() => new THREE.PlaneGeometry(12, 100), [])
    const riverMaterial = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#0284c7',
        roughness: 0.15,
        metalness: 0.6,
        transparent: true,
        opacity: 0.85,
        emissive: '#0369a1',
        emissiveIntensity: 0.2,
        clippingPlanes: clippingPlanes,
        side: THREE.DoubleSide,
    }), [clippingPlanes])

    // Geological Skirt Materials (Humus, Subsoil, Bedrock)
    const topsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#452b18',
        roughness: 0.9,
        metalness: 0.05,
        clippingPlanes: clippingPlanes
    }), [clippingPlanes])

    const subsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#854d0e',
        roughness: 0.92,
        metalness: 0.05,
        clippingPlanes: clippingPlanes
    }), [clippingPlanes])

    const bedrockMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#334155',
        roughness: 0.95,
        metalness: 0.15,
        clippingPlanes: clippingPlanes
    }), [clippingPlanes])

    const calculateLocalPheromone = (wx, wz) => {
        if (!pheromones || pheromones.length === 0) return 0.0
        let total = 0
        for (let i = 0; i < pheromones.length; i++) {
            const p = pheromones[i]
            const px = p.x ?? 50
            const pz = p.z !== undefined ? p.z : (p.y ?? 50)
            const d2 = (px - wx) * (px - wx) + (pz - wz) * (pz - wz)
            if (d2 < 25) { // within 5 meters
                total += (p.intensity || 1.0) * (1.0 - Math.sqrt(d2) / 5.0)
            }
        }
        return total
    }

    const handleGroundPointerMove = (e) => {
        if (e.point) {
            const wx = e.point.x
            const wy = e.point.y
            const wz = e.point.z
            const substrate = getSubstrateAt(wx, wy, wz, terrainConfig)
            const clientX = e.nativeEvent?.clientX ?? e.clientX ?? 200
            const clientY = e.nativeEvent?.clientY ?? e.clientY ?? 200
            setHoveredVoxel({
                x: wx,
                y: wy,
                z: wz,
                screenX: clientX,
                screenY: clientY,
                substrate,
                temp: (environment?.temperature ?? 22.0) - wy * 0.4,
                humidity: environment?.humidity ?? 65.0,
                phero: calculateLocalPheromone(wx, wz),
                isHovering: true
            })
        }
    }

    const handleGroundClick = (e) => {
        if (!e.point) return
        const wx = e.point.x
        const wz = e.point.z

        // 1. Proximity check for nearby ants (within 2.2m)
        if (ants && ants.length > 0) {
            let closestAnt = null
            let minDistSq = 2.2 * 2.2
            for (let i = 0; i < ants.length; i++) {
                const a = ants[i]
                const ax = a.x ?? 50
                const az = a.z !== undefined ? a.z : (a.y ?? 50)
                const distSq = (ax - wx) * (ax - wx) + (az - wz) * (az - wz)
                if (distSq < minDistSq) {
                    minDistSq = distSq
                    closestAnt = a
                }
            }
            if (closestAnt) {
                setSelectedEntity(closestAnt)
                return
            }
        }

        // 2. Otherwise pin/select the clicked voxel
        const wy = e.point.y
        const substrate = getSubstrateAt(wx, wy, wz, terrainConfig)
        const clientX = e.nativeEvent?.clientX ?? e.clientX ?? 200
        const clientY = e.nativeEvent?.clientY ?? e.clientY ?? 200
        setHoveredVoxel({
            x: wx,
            y: wy,
            z: wz,
            screenX: clientX,
            screenY: clientY,
            substrate,
            temp: (environment?.temperature ?? 22.0) - wy * 0.4,
            humidity: environment?.humidity ?? 65.0,
            phero: calculateLocalPheromone(wx, wz),
            isHovering: true,
            isPinned: true
        })
    }

    const handleGroundPointerOut = () => {
        if (hoveredVoxel && !hoveredVoxel.isPinned) {
            setHoveredVoxel(null)
        }
    }

    return (
        <group ref={groupRef}>
            {/* Ground Surface */}
            {showTerrain && (
                isGamified ? (
                    <VoxelTerrain
                        terrainConfig={terrainConfig}
                        slicePlaneRatio={slicePlaneRatio}
                        show3DSkirt={show3DSkirt}
                    />
                ) : (
                    <group>
                        <mesh
                            geometry={groundGeometry}
                            material={groundMaterial}
                            position={[50, 0, 50]}
                            receiveShadow
                            onPointerMove={handleGroundPointerMove}
                            onClick={handleGroundClick}
                            onPointerOut={handleGroundPointerOut}
                        />
                        <mesh
                            ref={riverMeshRef}
                            geometry={riverGeometry}
                            material={riverMaterial}
                            rotation={[-Math.PI / 2, 0, 0]}
                            position={[25, 0.02, 50]}
                            receiveShadow
                            onPointerMove={handleGroundPointerMove}
                            onClick={handleGroundClick}
                            onPointerOut={handleGroundPointerOut}
                        />
                    </group>
                )
            )}

            {/* Scientific Isolines: 3D Topographic, Pheromones & Micro-climate Isolines */}
            <ScientificIsolines />

            {/* Geological Skirt Strata (Realistic & Scientific Modes) */}
            {show3DSkirt && !isGamified && (
                <group>
                    {/* Topsoil Layer (Y: -0.4, Height: 0.8) */}
                    <mesh
                        position={[50, -0.4, 50]}
                        material={topsoilMat}
                        receiveShadow
                        onPointerMove={handleGroundPointerMove}
                        onClick={handleGroundClick}
                    >
                        <boxGeometry args={[100, 0.8, 100]} />
                    </mesh>
                    {/* Subsoil Clay Layer (Y: -1.8, Height: 2.0) */}
                    <mesh
                        position={[50, -1.8, 50]}
                        material={subsoilMat}
                        receiveShadow
                        onPointerMove={handleGroundPointerMove}
                        onClick={handleGroundClick}
                    >
                        <boxGeometry args={[100, 2.0, 100]} />
                    </mesh>
                    {/* Bedrock Deep Stone Layer (Y: -3.8, Height: 2.0) */}
                    <mesh
                        position={[50, -3.8, 50]}
                        material={bedrockMat}
                        receiveShadow
                        onPointerMove={handleGroundPointerMove}
                        onClick={handleGroundClick}
                    >
                        <boxGeometry args={[100, 2.0, 100]} />
                    </mesh>
                </group>
            )}

            {/* Trees & Ground Flora Renderer */}
            {showVegetation && <VegetationRenderer />}

            {/* Gamified Mode Voxel Floating Pheromone Particles */}
            {isGamified && (
                <GamifiedVoxelParticles terrainConfig={terrainConfig} />
            )}

            {/* Nests Renderer */}
            <NestRenderer />

            {/* 3D Voxel Cursor Highlight Marker */}
            <VoxelCursor3D hoveredVoxel={hoveredVoxel} terrainConfig={terrainConfig} />

            {/* Ants (LOD System) & Selection Target Reticle 3D */}
            {showAnts && <LODAnts ants={ants} />}
            <AntSelectionReticle3D ant={selectedEntity} terrainConfig={terrainConfig} />

            {/* Food Sources (Aligned to Terrain Height) */}
            {(foodSources || []).map((food, i) => {
                if (!food) return null
                const foodX = food.x ?? 50
                const foodZ = food.z !== undefined ? food.z : (food.y ?? 50)
                const foodY = getTerrainHeight(foodX, foodZ, terrainConfig) + 0.3
                return (
                    <FoodSource
                        key={food.id || i}
                        position={[foodX, foodY, foodZ]}
                        quantity={food.quantity || food.amount || 100}
                        type={food.type}
                    />
                )
            })}

            {/* Predators (Aligned to Terrain Height) */}
            {(predators || []).map((pred, i) => {
                if (!pred) return null
                const predX = pred.x ?? 50
                const predZ = pred.z !== undefined ? pred.z : (pred.y ?? 50)
                const predY = getTerrainHeight(predX, predZ, terrainConfig) + 0.3
                return (
                    <Predator
                        key={pred.id || i}
                        position={[predX, predY, predZ]}
                        type={pred.type}
                        state={pred.state}
                    />
                )
            })}
        </group>
    )
}
