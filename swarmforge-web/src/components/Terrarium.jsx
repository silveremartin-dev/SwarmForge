import { useRef, useMemo } from 'react'
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
import { getTerrainHeight } from '../utils/terrainUtils'
import {
    getPixelGrassTopTexture,
    getPixelGrassSideTexture,
    getPixelCobbleTexture,
    getPixelSandTexture,
    getPixelLilyPadTexture,
    getPixelSugarCaneTexture,
    getPixelWaterTexture
} from '../utils/pixelTextures'

/**
 * Realistic Mode Atmospheric Macro Pollen & Night Fireflies
 */
function MacroAtmosphere({ isNight, terrainConfig }) {
    const groupRef = useRef()
    const particles = useMemo(() => {
        const pts = []
        for (let i = 0; i < 60; i++) {
            pts.push({
                x: Math.random() * 90 + 5,
                y: Math.random() * 6 + 0.5,
                z: Math.random() * 90 + 5,
                speed: 0.2 + Math.random() * 0.5,
                phase: Math.random() * Math.PI * 2,
                size: isNight ? 0.25 : 0.12
            })
        }
        return pts
    }, [isNight])

    useFrame((state) => {
        if (groupRef.current) {
            const t = state.clock.getElapsedTime()
            groupRef.current.children.forEach((mesh, idx) => {
                const p = particles[idx]
                mesh.position.y = p.y + Math.sin(t * p.speed + p.phase) * 0.4
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
                        opacity={isNight ? 0.85 : 0.55}
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
        for (let i = 0; i < 40; i++) {
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
                    <boxGeometry args={[0.25, 0.25, 0.25]} />
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
 * Creates a procedural Gaussian-splatted ground texture for Realistic Mode.
 * Blends patches of forest soil, red clay, golden sand, slate pebbles, and vibrant moss.
 */
function createSplattingGroundTexture() {
    const canvas = document.createElement('canvas')
    canvas.width = 512
    canvas.height = 512
    const ctx = canvas.getContext('2d')

    // Base soil fill
    ctx.fillStyle = '#2d1c0c'
    ctx.fillRect(0, 0, 512, 512)

    // Gaussian Splatting color patches
    const colors = [
        'rgba(45, 92, 36, 0.50)',   // Moss Green
        'rgba(139, 69, 19, 0.55)',   // Clay Red-Brown
        'rgba(210, 180, 140, 0.45)', // Sand Tan
        'rgba(112, 128, 144, 0.40)', // Pebble Slate Gray
        'rgba(65, 42, 22, 0.65)',    // Organic Humus Dark
    ]

    for (let i = 0; i < 450; i++) {
        const x = Math.random() * 512
        const y = Math.random() * 512
        const radius = 12 + Math.random() * 50
        const color = colors[Math.floor(Math.random() * colors.length)]

        const gradient = ctx.createRadialGradient(x, y, 0, x, y, radius)
        gradient.addColorStop(0, color)
        gradient.addColorStop(1, 'transparent')

        ctx.fillStyle = gradient
        ctx.beginPath()
        ctx.arc(x, y, radius, 0, Math.PI * 2)
        ctx.fill()
    }

    const texture = new THREE.CanvasTexture(canvas)
    texture.wrapS = THREE.RepeatWrapping
    texture.wrapT = THREE.RepeatWrapping
    texture.repeat.set(6, 6)
    return texture
}

/**
 * Minecraft Cubic Sky with Drifting Clouds, Cubic Sun & Moon
 */
function VoxelSky({ isNight }) {
    const cloudsRef = useRef()
    useFrame((state) => {
        if (cloudsRef.current) {
            const t = state.clock.getElapsedTime()
            cloudsRef.current.position.x = ((t * 0.4) % 120) - 20
        }
    })

    return (
        <group frustumCulled={false}>
            {/* Minecraft Cubic Sun / Moon */}
            {isNight ? (
                <mesh position={[85, 55, 15]}>
                    <boxGeometry args={[8, 8, 8]} />
                    <meshBasicMaterial color="#f1f5f9" />
                </mesh>
            ) : (
                <mesh position={[20, 60, 20]}>
                    <boxGeometry args={[10, 10, 10]} />
                    <meshBasicMaterial color="#fef08a" />
                </mesh>
            )}

            {/* Drifting Layered Cubic Clouds */}
            <group ref={cloudsRef} position={[0, 42, 50]}>
                {[
                    { x: 10, z: -20, w: 25, d: 15 },
                    { x: 45, z: 10, w: 30, d: 18 },
                    { x: 85, z: -10, w: 22, d: 14 },
                    { x: -30, z: 25, w: 28, d: 16 },
                ].map((c, i) => (
                    <mesh key={i} position={[c.x, 0, c.z]}>
                        <boxGeometry args={[c.w, 1.8, c.d]} />
                        <meshBasicMaterial color="#ffffff" transparent opacity={0.6} />
                    </mesh>
                ))}
            </group>
        </group>
    )
}

/**
 * Minecraft Cubic Torch with Flickering Cubic Flame & Rising Smoke
 */
function VoxelTorch({ position }) {
    const flameRef = useRef()
    const smokeRef = useRef()

    useFrame((state) => {
        const t = state.clock.getElapsedTime()
        if (flameRef.current) {
            flameRef.current.scale.y = 0.8 + Math.sin(t * 12) * 0.25
        }
        if (smokeRef.current) {
            smokeRef.current.position.y = 0.65 + ((t * 0.6) % 0.8)
            smokeRef.current.scale.setScalar(0.5 + ((t * 0.6) % 0.8) * 0.6)
        }
    })

    return (
        <group position={position} frustumCulled={false}>
            {/* Wooden post */}
            <mesh position={[0, 0.35, 0]} castShadow>
                <boxGeometry args={[0.12, 0.7, 0.12]} />
                <meshStandardMaterial color="#78350f" roughness={0.9} />
            </mesh>
            {/* Coal Head */}
            <mesh position={[0, 0.72, 0]}>
                <boxGeometry args={[0.14, 0.14, 0.14]} />
                <meshStandardMaterial color="#18181b" roughness={0.9} />
            </mesh>
            {/* Flickering Cubic Flame */}
            <mesh ref={flameRef} position={[0, 0.85, 0]}>
                <boxGeometry args={[0.16, 0.16, 0.16]} />
                <meshStandardMaterial color="#f59e0b" emissive="#fbbf24" emissiveIntensity={1.0} />
            </mesh>
            {/* Rising smoke voxel */}
            <mesh ref={smokeRef} position={[0, 0.95, 0]}>
                <boxGeometry args={[0.1, 0.1, 0.1]} />
                <meshBasicMaterial color="#71717a" transparent opacity={0.4} />
            </mesh>
        </group>
    )
}

/**
 * Minecraft River Flora: Water Lily Pads on River & Sugar Cane along Banks
 */
function VoxelRiverFlora({ terrainConfig }) {
    const lilyTex = useMemo(() => getPixelLilyPadTexture(), [])
    const caneTex = useMemo(() => getPixelSugarCaneTexture(), [])

    const { lilyPads, sugarCanes } = useMemo(() => {
        const lilies = [
            { x: 23, z: 18, s: 1.4 },
            { x: 26, z: 34, s: 1.6 },
            { x: 24, z: 52, s: 1.5 },
            { x: 27, z: 68, s: 1.3 },
            { x: 23, z: 84, s: 1.7 },
        ]
        const canes = [
            { x: 19.5, z: 22, h: 2.2 },
            { x: 19.2, z: 42, h: 2.5 },
            { x: 30.5, z: 58, h: 2.0 },
            { x: 30.8, z: 76, h: 2.4 },
        ]
        return { lilyPads: lilies, sugarCanes: canes }
    }, [])

    return (
        <group frustumCulled={false}>
            {/* Lily Pads on River */}
            {lilyPads.map((l, idx) => (
                <mesh key={`lily_${idx}`} position={[l.x, 0.04, l.z]} rotation={[-Math.PI / 2, 0, idx * 1.2]}>
                    <planeGeometry args={[l.s, l.s]} />
                    <meshStandardMaterial map={lilyTex} transparent alphaTest={0.3} side={THREE.DoubleSide} />
                </mesh>
            ))}
            {/* Sugar Cane Reeds along Banks */}
            {sugarCanes.map((c, idx) => {
                const groundY = getTerrainHeight(c.x, c.z, terrainConfig)
                return (
                    <group key={`cane_${idx}`} position={[c.x, groundY, c.z]}>
                        <mesh position={[0, c.h / 2, 0]} rotation={[0, Math.PI / 4, 0]}>
                            <planeGeometry args={[0.9, c.h]} />
                            <meshStandardMaterial map={caneTex} transparent alphaTest={0.3} side={THREE.DoubleSide} />
                        </mesh>
                        <mesh position={[0, c.h / 2, 0]} rotation={[0, -Math.PI / 4, 0]}>
                            <planeGeometry args={[0.9, c.h]} />
                            <meshStandardMaterial map={caneTex} transparent alphaTest={0.3} side={THREE.DoubleSide} />
                        </mesh>
                    </group>
                )
            })}
        </group>
    )
}

/**
 * Realistic Mode: Low-Lying River Mist Floating over River
 */
function RiverMist() {
    const mistRef = useRef()
    useFrame((state) => {
        if (mistRef.current) {
            const t = state.clock.getElapsedTime()
            mistRef.current.position.y = 0.28 + Math.sin(t * 0.8) * 0.05
            mistRef.current.material.opacity = 0.35 + Math.sin(t * 0.5) * 0.10
        }
    })

    return (
        <mesh ref={mistRef} position={[25, 0.3, 50]} rotation={[-Math.PI / 2, 0, 0]}>
            <planeGeometry args={[14, 100]} />
            <meshStandardMaterial color="#e0f2fe" transparent opacity={0.35} depthWrite={false} roughness={0.1} />
        </mesh>
    )
}

/**
 * Realistic Mode: Forest Floor Leaf Litter, Twigs & Dewdrop Reflections
 */
function ForestLitterLayer({ terrainConfig }) {
    const litter = useMemo(() => {
        const items = []
        const rand = (seed) => {
            const x = Math.sin(seed * 19.123 + 88.23) * 43758.5453
            return x - Math.floor(x)
        }
        const leafColors = ['#78350f', '#92400e', '#a16207', '#b45309', '#d97706']

        for (let i = 0; i < 90; i++) {
            const x = rand(i * 1.3) * 90 + 5
            const z = rand(i * 2.7) * 90 + 5
            if (x >= 18 && x <= 32) continue // skip river
            const groundY = getTerrainHeight(x, z, terrainConfig)
            const isTwig = i % 3 === 0
            const rotY = rand(i * 4.1) * Math.PI * 2
            const col = leafColors[i % leafColors.length]
            const scale = 0.25 + rand(i * 5.9) * 0.3

            items.push({ id: i, x, y: groundY + 0.02, z, isTwig, rotY, col, scale })
        }
        return items
    }, [terrainConfig])

    return (
        <group frustumCulled={false}>
            {litter.map((item) => (
                <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]}>
                    {item.isTwig ? (
                        <mesh position={[0, 0.015, 0]} rotation={[0, 0, Math.PI / 2]}>
                            <cylinderGeometry args={[0.015 * item.scale, 0.012 * item.scale, 0.6 * item.scale, 5]} />
                            <meshStandardMaterial color="#451a03" roughness={0.95} />
                        </mesh>
                    ) : (
                        <mesh position={[0, 0.01, 0]} rotation={[-Math.PI / 2, 0.2, 0]}>
                            <planeGeometry args={[0.4 * item.scale, 0.3 * item.scale]} />
                            <meshStandardMaterial color={item.col} roughness={0.9} side={THREE.DoubleSide} />
                        </mesh>
                    )}
                </group>
            ))}
        </group>
    )
}

/**
 * High-Fidelity Minecraft Diorama Voxel Terrain Generator (Gamified Mode)
 * Creates fine-subdivided stepped voxel blocks with organic biomes (Grass, Dirt Paths, Rock Cliffs, Sand Shores, Water)
 * textured with crisp 16x16 pixel-art textures and rich vertical geological cutaways.
 */
function VoxelTerrain({ terrainConfig }) {
    const Y_BASE = -5.0
    const STEP = 1.25 // Fine voxel subdivision (80x80 grid across 100m)

    const grassTopTex = useMemo(() => getPixelGrassTopTexture(), [])
    const cobbleTex = useMemo(() => getPixelCobbleTexture(), [])
    const sandTex = useMemo(() => getPixelSandTexture(), [])
    const waterTex = useMemo(() => getPixelWaterTexture(), [])

    const { terrainBlocks, cutawayBlocks } = useMemo(() => {
        const blocks = []
        const skirt = []
        const hasRiver = terrainConfig?.hasRiver ?? true
        const riverX = terrainConfig?.riverX ?? 25
        const riverWidth = terrainConfig?.riverWidth ?? 11

        // Hash-based pseudo random for deterministic terrain shading
        const hash = (x, z) => {
            const val = Math.sin(x * 12.9898 + z * 78.233) * 43758.5453
            return val - Math.floor(val)
        }

        // 1. Generate Surface Terrain Voxel Columns
        for (let x = STEP / 2; x <= 100; x += STEP) {
            for (let z = STEP / 2; z <= 100; z += STEP) {
                const heightY = getTerrainHeight(x, z, terrainConfig)
                const distToRiver = Math.abs(x - riverX)
                const isRiver = hasRiver && distToRiver < riverWidth / 2
                const isBeach = hasRiver && distToRiver >= riverWidth / 2 && distToRiver < (riverWidth / 2 + 3.5)
                
                // Path curve through terrain center
                const distToPath = Math.abs(z - (50 + Math.sin(x * 0.08) * 12))
                const isPath = distToPath < 3.2 && !isRiver

                // Rock cliffs on elevated or rough terrain
                const isRock = (heightY > 1.6 || (heightY > 0.8 && hash(x, z) > 0.65)) && !isRiver && !isBeach

                // Determine block height from floor to surface
                const surfaceY = isRiver ? -0.1 : heightY
                const blockHeight = Math.max(0.4, surfaceY - Y_BASE)
                const posY = Y_BASE + blockHeight / 2

                // Natural, desaturated Minecraft color palettes
                let topColor = '#4d7c0f' // Default lush grass
                let sideColor = '#593d25' // Rich loam dirt

                const rnd = hash(x * 2.1, z * 3.7)

                if (isRiver) {
                    topColor = rnd > 0.5 ? '#0284c7' : '#0369a1'
                    sideColor = '#0369a1'
                } else if (isBeach) {
                    topColor = rnd > 0.5 ? '#d4b483' : '#c2a675'
                    sideColor = '#a8895b'
                } else if (isRock) {
                    topColor = rnd > 0.6 ? '#64748b' : (rnd > 0.3 ? '#57534e' : '#78716c')
                    sideColor = rnd > 0.5 ? '#475569' : '#3f3f46'
                } else if (isPath) {
                    topColor = rnd > 0.5 ? '#714d2e' : '#8c6747'
                    sideColor = '#57391e'
                } else {
                    // Natural Grass Variations (Lush forest, meadow, dry patches)
                    if (rnd > 0.75) topColor = '#5b8c32'
                    else if (rnd > 0.45) topColor = '#4d7c0f'
                    else if (rnd > 0.2) topColor = '#3f6212'
                    else topColor = '#65a30d' // Bright clover accent
                }

                blocks.push({
                    id: `v_${Math.round(x)}_${Math.round(z)}`,
                    x,
                    y: posY,
                    z,
                    width: STEP - 0.04,
                    height: blockHeight,
                    isRiver,
                    isRock,
                    isBeach,
                    topColor,
                    sideColor
                })
            }
        }

        // 2. Generate Cutaway Side Walls with Sliced Subterranean Strata & Hollow Cavities
        const createWallSlice = (side, count) => {
            for (let i = 0; i < count; i++) {
                const coord = (i + 0.5) * (100 / count)
                let x = 0, z = 0
                if (side === 'N') { x = coord; z = 0.5 }
                else if (side === 'S') { x = coord; z = 99.5 }
                else if (side === 'W') { x = 0.5; z = coord }
                else { x = 99.5; z = coord }

                const surfaceY = getTerrainHeight(x, z, terrainConfig)

                // Sliced Stratum 1: Humus & Topsoil (Y: surfaceY down to -0.8m)
                const topsoilH = Math.max(0.2, surfaceY - (-0.8))
                skirt.push({
                    id: `sk_top_${side}_${i}`,
                    x,
                    y: -0.8 + topsoilH / 2,
                    z,
                    w: (100 / count) - 0.05,
                    h: topsoilH,
                    color: i % 2 === 0 ? '#452b18' : '#382010'
                })

                // Sliced Stratum 2: Subsoil / Ochre Clay (Y: -0.8m down to -2.8m)
                // Procedural hollow pocket in clay (representing subterranean chamber cutout)
                const isHollow = (i >= 8 && i <= 11) || (i >= 22 && i <= 24)
                if (!isHollow) {
                    skirt.push({
                        id: `sk_mid_${side}_${i}`,
                        x,
                        y: -1.8,
                        z,
                        w: (100 / count) - 0.05,
                        h: 2.0,
                        color: i % 2 === 0 ? '#92400e' : '#a16207'
                    })
                } else {
                    // Dark interior background of carved cavity
                    skirt.push({
                        id: `sk_cav_${side}_${i}`,
                        x,
                        y: -1.8,
                        z,
                        w: (100 / count) - 0.05,
                        h: 2.0,
                        color: '#1c1917'
                    })
                }

                // Sliced Stratum 3: Bedrock & Cobble Foundation (Y: -2.8m down to -5.0m)
                skirt.push({
                    id: `sk_bed_${side}_${i}`,
                    x,
                    y: -3.9,
                    z,
                    w: (100 / count) - 0.05,
                    h: 2.2,
                    color: i % 3 === 0 ? '#475569' : (i % 3 === 1 ? '#334155' : '#1e293b')
                })
            }
        }

        createWallSlice('N', 32)
        createWallSlice('S', 32)
        createWallSlice('W', 32)
        createWallSlice('E', 32)

        return { terrainBlocks: blocks, cutawayBlocks: skirt }
    }, [terrainConfig])

    return (
        <group frustumCulled={false}>
            {/* Voxel Surface Columns */}
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
                        side={THREE.FrontSide}
                        depthWrite={true}
                        depthTest={true}
                    />
                </mesh>
            ))}

            {/* Cutaway Geological Skirt Faces with Sliced Strata & Hollow Cavities */}
            {cutawayBlocks.map((s) => (
                <mesh key={s.id} position={[s.x, s.y, s.z]} receiveShadow castShadow frustumCulled={false}>
                    <boxGeometry args={[s.w, s.h, s.w]} />
                    <meshStandardMaterial color={s.color} roughness={0.92} metalness={0.05} depthWrite depthTest />
                </mesh>
            ))}
        </group>
    )
}


export default function Terrarium() {
    const { ants, foodSources, predators, environment, terrainConfig, lookAndFeel, environmentLighting, disasterState, climateEngine } = useSimulationStore()
    const groupRef = useRef()
    const riverMeshRef = useRef()
    const floodMeshRef = useRef()
    const { camera } = useThree()

    // Gamified mode is strictly 'GAMING'. Scientific mode uses standard realistic 3D mesh rendering.
    const isGamified = lookAndFeel === 'GAMING'

    // Realtime spatial listener & river audio update based on 3D camera distance
    useFrame((state) => {
        if (camera) {
            soundEngine.updateSpatialListener(camera.position.x, camera.position.y, camera.position.z)
            soundEngine.updateRiverSound(camera.position, { x: 25, y: 0, z: 50 })
        }
        if (riverMeshRef.current) {
            riverMeshRef.current.position.y = 0.02 + Math.sin(state.clock.getElapsedTime() * 2) * 0.015
        }
        if (floodMeshRef.current && disasterState?.activeDisaster === 'FLASH_FLOOD') {
            const floodLevel = 0.2 + (disasterState.intensity / 100) * 1.5
            floodMeshRef.current.position.y = Math.min(1.5, floodLevel)
        }
    })

    // Dynamic Fog Color based on Day/Night & Climate
    const fogColor = useMemo(() => {
        if (environmentLighting?.isNight) return '#0f172a'
        if (lookAndFeel === 'SCIENTIFIC') return '#64748b'
        return '#87ceeb'
    }, [environmentLighting?.isNight, lookAndFeel])

    // Real Textures loaded for Mode 3D Naturel (REALISTIC mode) with dynamic Biome / Climate adaption
    const realisticTextures = useMemo(() => {
        if (lookAndFeel !== 'REALISTIC') return null
        const loader = new THREE.TextureLoader()
        const loadTex = (url, rx = 8, ry = 8) => {
            const t = loader.load(url)
            t.wrapS = THREE.RepeatWrapping
            t.wrapT = THREE.RepeatWrapping
            t.repeat.set(rx, ry)
            return t
        }

        const climateType = climateEngine?.climateType || 'OCEANIC'

        // Arid / Savanna / Desert landscapes
        if (climateType === 'ARID' || climateType === 'SAVANNA') {
            return {
                groundMap: loadTex('/3d/textures/pbr/Ground086/Ground086_1K-JPG_Color.jpg', 12, 12),
                groundNormalMap: loadTex('/3d/textures/pbr/Ground086/Ground086_1K-JPG_NormalGL.jpg', 12, 12),
                groundRoughnessMap: loadTex('/3d/textures/pbr/Ground086/Ground086_1K-JPG_Roughness.jpg', 12, 12),
                topsoilMap: loadTex('/3d/textures/pbr/Ground093C/Ground093C_1K-JPG_Color.jpg', 8, 8),
                topsoilNormalMap: loadTex('/3d/textures/pbr/Ground093C/Ground093C_1K-JPG_NormalGL.jpg', 8, 8),
                topsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground093C/Ground093C_1K-JPG_Roughness.jpg', 8, 8),
                subsoilMap: loadTex('/3d/textures/pbr/Ground061/Ground061_1K-JPG_Color.jpg', 6, 6),
                subsoilNormalMap: loadTex('/3d/textures/pbr/Ground061/Ground061_1K-JPG_NormalGL.jpg', 6, 6),
                subsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground061/Ground061_1K-JPG_Roughness.jpg', 6, 6),
                bedrockMap: loadTex('/3d/textures/pbr/Ground089/Ground089_1K-JPG_Color.jpg', 6, 6),
                bedrockNormalMap: loadTex('/3d/textures/pbr/Ground089/Ground089_1K-JPG_NormalGL.jpg', 6, 6),
                bedrockRoughnessMap: loadTex('/3d/textures/pbr/Ground089/Ground089_1K-JPG_Roughness.jpg', 6, 6),
                riverCobbleMap: loadTex('/3d/textures/3td_RiverCobble_01.png', 4, 12),
                riverCobbleNormalMap: loadTex('/3d/textures/3td_RiverCobble_01_NRM.png', 4, 12),
            }
        }

        // Mediterranean / Stony Dry Scrubland
        if (climateType === 'MEDITERRANEAN') {
            return {
                groundMap: loadTex('/3d/textures/pbr/Ground097/Ground097_1K-JPG_Color.jpg', 12, 12),
                groundNormalMap: loadTex('/3d/textures/pbr/Ground097/Ground097_1K-JPG_NormalGL.jpg', 12, 12),
                groundRoughnessMap: loadTex('/3d/textures/pbr/Ground097/Ground097_1K-JPG_Roughness.jpg', 12, 12),
                topsoilMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_Color.jpg', 8, 8),
                topsoilNormalMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_NormalGL.jpg', 8, 8),
                topsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_Roughness.jpg', 8, 8),
                subsoilMap: loadTex('/3d/textures/pbr/Ground049A/Ground049A_1K-JPG_Color.jpg', 6, 6),
                subsoilNormalMap: loadTex('/3d/textures/pbr/Ground049A/Ground049A_1K-JPG_NormalGL.jpg', 6, 6),
                subsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground049A/Ground049A_1K-JPG_Roughness.jpg', 6, 6),
                bedrockMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_Color.jpg', 6, 6),
                bedrockNormalMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_NormalGL.jpg', 6, 6),
                bedrockRoughnessMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_Roughness.jpg', 6, 6),
                riverCobbleMap: loadTex('/3d/textures/3td_RiverCobble_01.png', 4, 12),
                riverCobbleNormalMap: loadTex('/3d/textures/3td_RiverCobble_01_NRM.png', 4, 12),
            }
        }

        // Alpine / Mountain
        if (climateType === 'ALPINE') {
            return {
                groundMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_Color.jpg', 12, 12),
                groundNormalMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_NormalGL.jpg', 12, 12),
                groundRoughnessMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_Roughness.jpg', 12, 12),
                topsoilMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_Color.jpg', 8, 8),
                topsoilNormalMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_NormalGL.jpg', 8, 8),
                topsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_Roughness.jpg', 8, 8),
                subsoilMap: loadTex('/3d/textures/pbr/Ground108/Ground108_1K-JPG_Color.jpg', 6, 6),
                subsoilNormalMap: loadTex('/3d/textures/pbr/Ground108/Ground108_1K-JPG_NormalGL.jpg', 6, 6),
                subsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground108/Ground108_1K-JPG_Roughness.jpg', 6, 6),
                bedrockMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_Color.jpg', 6, 6),
                bedrockNormalMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_NormalGL.jpg', 6, 6),
                bedrockRoughnessMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_Roughness.jpg', 6, 6),
                riverCobbleMap: loadTex('/3d/textures/3td_RiverCobble_01.png', 4, 12),
                riverCobbleNormalMap: loadTex('/3d/textures/3td_RiverCobble_01_NRM.png', 4, 12),
            }
        }

        // Tropical Jungle
        if (climateType === 'TROPICAL') {
            return {
                groundMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_Color.jpg', 12, 12),
                groundNormalMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_NormalGL.jpg', 12, 12),
                groundRoughnessMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_Roughness.jpg', 12, 12),
                topsoilMap: loadTex('/3d/textures/pbr/Ground052/Ground052_1K-JPG_Color.jpg', 8, 8),
                topsoilNormalMap: loadTex('/3d/textures/pbr/Ground052/Ground052_1K-JPG_NormalGL.jpg', 8, 8),
                topsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground052/Ground052_1K-JPG_Roughness.jpg', 8, 8),
                subsoilMap: loadTex('/3d/textures/pbr/Ground049B/Ground049B_1K-JPG_Color.jpg', 6, 6),
                subsoilNormalMap: loadTex('/3d/textures/pbr/Ground049B/Ground049B_1K-JPG_NormalGL.jpg', 6, 6),
                subsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground049B/Ground049B_1K-JPG_Roughness.jpg', 6, 6),
                bedrockMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_Color.jpg', 6, 6),
                bedrockNormalMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_NormalGL.jpg', 6, 6),
                bedrockRoughnessMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_Roughness.jpg', 6, 6),
                riverCobbleMap: loadTex('/3d/textures/3td_RiverCobble_01.png', 4, 12),
                riverCobbleNormalMap: loadTex('/3d/textures/3td_RiverCobble_01_NRM.png', 4, 12),
            }
        }

        // Default Oceanic / Continental Temperate
        return {
            groundMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_Color.jpg', 12, 12),
            groundNormalMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_NormalGL.jpg', 12, 12),
            groundRoughnessMap: loadTex('/3d/textures/pbr/Ground037/Ground037_1K-JPG_Roughness.jpg', 12, 12),
            topsoilMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_Color.jpg', 8, 8),
            topsoilNormalMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_NormalGL.jpg', 8, 8),
            topsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground003/Ground003_1K-JPG_Roughness.jpg', 8, 8),
            subsoilMap: loadTex('/3d/textures/pbr/Ground049A/Ground049A_1K-JPG_Color.jpg', 6, 6),
            subsoilNormalMap: loadTex('/3d/textures/pbr/Ground049A/Ground049A_1K-JPG_NormalGL.jpg', 6, 6),
            subsoilRoughnessMap: loadTex('/3d/textures/pbr/Ground049A/Ground049A_1K-JPG_Roughness.jpg', 6, 6),
            bedrockMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_Color.jpg', 6, 6),
            bedrockNormalMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_NormalGL.jpg', 6, 6),
            bedrockRoughnessMap: loadTex('/3d/textures/pbr/Ground025/Ground025_1K-JPG_Roughness.jpg', 6, 6),
            riverCobbleMap: loadTex('/3d/textures/3td_RiverCobble_01.png', 4, 12),
            riverCobbleNormalMap: loadTex('/3d/textures/3td_RiverCobble_01_NRM.png', 4, 12),
        }
    }, [lookAndFeel, climateEngine?.climateType])

    // Procedural Splatting Ground Texture fallback for Realistic Mode
    const splattingTexture = useMemo(() => {
        if (lookAndFeel === 'REALISTIC' && !realisticTextures?.groundMap) {
            return createSplattingGroundTexture()
        }
        return null
    }, [lookAndFeel, realisticTextures])

    // Ground Surface color adapted by Look & Feel theme
    const groundColor = useMemo(() => {
        if (lookAndFeel === 'SCIENTIFIC') return '#2e4a23'
        if (lookAndFeel === 'REALISTIC') return '#ffffff'
        return '#3d2817'
    }, [lookAndFeel])

    // Displaced Ground Surface mesh matching terrain elevation relief (Aligned to terrarium center [50, 0, 50])
    const groundGeometry = useMemo(() => {
        const geo = new THREE.PlaneGeometry(100, 100, 80, 80)
        geo.rotateX(-Math.PI / 2)
        const pos = geo.attributes.position
        for (let i = 0; i < pos.count; i++) {
            const worldX = pos.getX(i) + 50
            const worldZ = pos.getZ(i) + 50
            const y = getTerrainHeight(worldX, worldZ, terrainConfig)
            pos.setY(i, y)
        }
        geo.computeVertexNormals()
        geo.computeBoundingBox()
        geo.computeBoundingSphere()
        return geo
    }, [terrainConfig])

    const groundMaterial = useMemo(() => {
        if (lookAndFeel === 'REALISTIC') {
            return new THREE.MeshStandardMaterial({
                map: realisticTextures?.groundMap || splattingTexture,
                normalMap: realisticTextures?.groundNormalMap || null,
                roughnessMap: realisticTextures?.groundRoughnessMap || null,
                roughness: 0.85,
                metalness: 0.05,
                side: THREE.FrontSide,
            })
        }
        return new THREE.MeshStandardMaterial({
            color: groundColor,
            roughness: 0.85,
            metalness: 0.05,
            side: THREE.FrontSide,
        })
    }, [groundColor, lookAndFeel, realisticTextures, splattingTexture])

    // Geological Strata: Carved around river when hasRiver is true
    const hasRiver = terrainConfig?.hasRiver ?? true
    const riverX = terrainConfig?.riverX ?? 25
    const riverWidth = terrainConfig?.riverWidth ?? 12
    const riverLeft = Math.max(0, riverX - riverWidth / 2)
    const riverRight = Math.min(100, riverX + riverWidth / 2)
    const leftWidth = Math.max(0.1, riverLeft)
    const rightWidth = Math.max(0.1, 100 - riverRight)
    const leftCenterX = leftWidth / 2
    const rightCenterX = riverRight + rightWidth / 2

    // Topsoil Left & Right Bank Geometries
    const topsoilLeftGeo = useMemo(() => new THREE.BoxGeometry(leftWidth, 0.8, 100), [leftWidth])
    const topsoilRightGeo = useMemo(() => new THREE.BoxGeometry(rightWidth, 0.8, 100), [rightWidth])

    // Geological Stratum 1: Topsoil / Humus (Fully opaque solid BoxGeometry)
    const topsoilGeo = useMemo(() => {
        const geo = new THREE.BoxGeometry(100, 0.8, 100)
        geo.computeBoundingBox()
        geo.computeBoundingSphere()
        return geo
    }, [])
    const topsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: lookAndFeel === 'REALISTIC' ? '#ffffff' : (lookAndFeel === 'SCIENTIFIC' ? '#334155' : '#4a321f'),
        map: lookAndFeel === 'REALISTIC' ? realisticTextures?.topsoilMap : null,
        normalMap: lookAndFeel === 'REALISTIC' ? realisticTextures?.topsoilNormalMap : null,
        roughnessMap: lookAndFeel === 'REALISTIC' ? realisticTextures?.topsoilRoughnessMap : null,
        roughness: 0.9,
        metalness: 0.05,
        side: THREE.FrontSide,
    }), [lookAndFeel, realisticTextures])

    // Geological Stratum 2: Subsoil / Clay & Sand
    const subsoilGeo = useMemo(() => {
        const geo = new THREE.BoxGeometry(100, 2.0, 100)
        geo.computeBoundingBox()
        geo.computeBoundingSphere()
        return geo
    }, [])
    const subsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: lookAndFeel === 'REALISTIC' ? '#ffffff' : (lookAndFeel === 'SCIENTIFIC' ? '#1e293b' : '#6b4c33'),
        map: lookAndFeel === 'REALISTIC' ? realisticTextures?.subsoilMap : null,
        normalMap: lookAndFeel === 'REALISTIC' ? realisticTextures?.subsoilNormalMap : null,
        roughnessMap: lookAndFeel === 'REALISTIC' ? realisticTextures?.subsoilRoughnessMap : null,
        roughness: 0.95,
        metalness: 0.05,
        side: THREE.FrontSide,
    }), [lookAndFeel, realisticTextures])

    // Geological Stratum 3: Bedrock & Deep Stone
    const bedrockGeo = useMemo(() => {
        const geo = new THREE.BoxGeometry(100, 2.2, 100)
        geo.computeBoundingBox()
        geo.computeBoundingSphere()
        return geo
    }, [])
    const bedrockMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: lookAndFeel === 'REALISTIC' ? '#ffffff' : (lookAndFeel === 'SCIENTIFIC' ? '#0f172a' : '#2c2825'),
        map: lookAndFeel === 'REALISTIC' ? realisticTextures?.bedrockMap : null,
        normalMap: lookAndFeel === 'REALISTIC' ? realisticTextures?.bedrockNormalMap : null,
        roughnessMap: lookAndFeel === 'REALISTIC' ? realisticTextures?.bedrockRoughnessMap : null,
        roughness: 0.98,
        metalness: 0.2,
        side: THREE.FrontSide,
    }), [lookAndFeel, realisticTextures])

    // Subterranean Water Table Horizon
    const waterTableGeo = useMemo(() => new THREE.BoxGeometry(100, 0.15, 100), [])
    const waterTableMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#0284c7',
        transparent: true,
        opacity: 0.75,
        roughness: 0.1,
        side: THREE.DoubleSide,
    }), [])

    // ── 3D RIVER STREAM (Liquid Plane in Realistic and Scientific modes) ──
    const riverGeometry = useMemo(() => new THREE.PlaneGeometry(12, 100), [])
    const riverMaterial = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#0284c7',
        roughness: 0.1,
        metalness: 0.8,
        transparent: true,
        opacity: 0.85,
        emissive: '#0369a1',
        emissiveIntensity: 0.2,
        side: THREE.DoubleSide,
    }), [])

    // ── CUBIC OUTER BORDER / VOXEL RIM (Bordure cubique en empilement de blocs) ──
    const voxelRimBlocks = useMemo(() => {
        const blocks = []
        const rimColor = lookAndFeel === 'SCIENTIFIC' ? '#475569' : (lookAndFeel === 'REALISTIC' ? '#84cc16' : '#0284c7')

        for (let i = 0; i <= 100; i += 2) {
            blocks.push({ id: `rim_n_${i}`, pos: [i, 0.1, 0], color: rimColor })
            blocks.push({ id: `rim_s_${i}`, pos: [i, 0.1, 100], color: rimColor })
            blocks.push({ id: `rim_w_${i}`, pos: [0, 0.1, i], color: rimColor })
            blocks.push({ id: `rim_e_${i}`, pos: [100, 0.1, i], color: rimColor })
        }
        return blocks
    }, [lookAndFeel])

    // Volumetric Voxel Gravel & Quartz Pebble Inclusions on Cutaway Side Walls
    const gravelInclusions = useMemo(() => {
        const items = []
        const rand = (seed) => Math.sin(seed * 9999) - Math.floor(Math.sin(seed * 9999))
        for (let i = 0; i < 40; i++) {
            const side = i % 4
            let x = 0, z = 0
            if (side === 0) { x = rand(i) * 100; z = 0.1 }
            else if (side === 1) { x = 99.9; z = rand(i) * 100 }
            else if (side === 2) { x = rand(i) * 100; z = 99.9 }
            else { x = 0.1; z = rand(i) * 100 }
            const y = -0.5 - rand(i * 3) * 3.8
            const scale = 0.2 + rand(i * 7) * 0.4
            const color = i % 3 === 0 ? '#e2e8f0' : (i % 3 === 1 ? '#7c2d12' : '#fef08a')
            items.push({ id: i, pos: [x, y, z], scale, color })
        }
        return items
    }, [])

    return (
        <group ref={groupRef}>
            {/* Atmospheric Fog Effect */}
            <fogExp2 attach="fog" args={[fogColor, 0.008]} />

            {/* Ground Surface: Solid Voxel Block Grid in Gamified mode vs Smooth Displaced Mesh in Realistic mode */}
            {isGamified ? (
                <VoxelTerrain terrainConfig={terrainConfig} />
            ) : (
                <group>
                    <mesh
                        geometry={groundGeometry}
                        material={groundMaterial}
                        position={[50, 0, 50]}
                        receiveShadow
                    />
                    {/* River Pebble & Cobble Bed underneath water */}
                    {realisticTextures?.riverCobbleMap && (
                        <mesh
                            rotation={[-Math.PI / 2, 0, 0]}
                            position={[25, -0.1, 50]}
                            receiveShadow
                        >
                            <planeGeometry args={[12, 100]} />
                            <meshStandardMaterial
                                map={realisticTextures.riverCobbleMap}
                                normalMap={realisticTextures.riverCobbleNormalMap}
                                roughness={0.9}
                            />
                        </mesh>
                    )}
                    <mesh
                        ref={riverMeshRef}
                        geometry={riverGeometry}
                        material={riverMaterial}
                        rotation={[-Math.PI / 2, 0, 0]}
                        position={[25, 0.02, 50]}
                        receiveShadow
                    />
                </group>
            )}

            {/* Scientific Mode: 3D Topographic, Pheromones & Micro-climate Isolines */}
            {lookAndFeel === 'SCIENTIFIC' && (
                <ScientificIsolines />
            )}

            {/* Natural Disaster: Flash Flood Water Level Layer */}
            {disasterState?.activeDisaster === 'FLASH_FLOOD' && (
                <mesh ref={floodMeshRef} position={[50, 0.2, 50]}>
                    <boxGeometry args={[100, 0.4, 100]} />
                    <meshStandardMaterial color="#0284c7" transparent opacity={0.7} roughness={0.1} side={THREE.DoubleSide} />
                </mesh>
            )}

            {/* Geological Skirt Strata (Realistic & Scientific Modes only) */}
            {!isGamified && (
                <group>
                    {hasRiver ? (
                        <>
                            {/* Topsoil Left Bank & Right Bank */}
                            <mesh geometry={topsoilLeftGeo} material={topsoilMat} position={[leftCenterX, -0.4, 50]} receiveShadow />
                            <mesh geometry={topsoilRightGeo} material={topsoilMat} position={[rightCenterX, -0.4, 50]} receiveShadow />

                            {/* Subsoil Left Bank & Right Bank */}
                            <mesh position={[leftCenterX, -1.8, 50]} receiveShadow>
                                <boxGeometry args={[leftWidth, 2.0, 100]} />
                                <primitive object={subsoilMat} attach="material" />
                            </mesh>
                            <mesh position={[rightCenterX, -1.8, 50]} receiveShadow>
                                <boxGeometry args={[rightWidth, 2.0, 100]} />
                                <primitive object={subsoilMat} attach="material" />
                            </mesh>
                        </>
                    ) : (
                        <>
                            <mesh geometry={topsoilGeo} material={topsoilMat} position={[50, -0.4, 50]} receiveShadow />
                            <mesh geometry={subsoilGeo} material={subsoilMat} position={[50, -1.8, 50]} receiveShadow />
                        </>
                    )}

                    {/* Geological Stratum 3: Bedrock (Y: [-2.8, -5.0]) */}
                    <mesh geometry={bedrockGeo} material={bedrockMat} position={[50, -3.9, 50]} receiveShadow />

                    {/* Subterranean Water Table Horizon */}
                    <mesh geometry={waterTableGeo} material={waterTableMat} position={[50, -3.1, 50]} />
                </group>
            )}

            {/* CUBIC OUTER BORDER / VOXEL RIM (Bordure cubique en blocs 3D - Gamified Mode Only) */}
            {isGamified && (
                <group>
                    {voxelRimBlocks.map((b) => (
                        <mesh key={b.id} position={b.pos} castShadow receiveShadow>
                            <boxGeometry args={[2.05, 0.25, 2.05]} />
                            <meshStandardMaterial color={b.color} roughness={0.3} metalness={0.7} side={THREE.DoubleSide} />
                        </mesh>
                    ))}
                </group>
            )}

            {/* Volumetric Voxel Gravel & Quartz Pebble Inclusions on Cutaway Side Walls (Cubic Voxels) */}
            {gravelInclusions.map((g) => (
                <mesh key={g.id} position={g.pos}>
                    {isGamified ? (
                        <boxGeometry args={[g.scale * 1.5, g.scale * 1.5, g.scale * 1.5]} />
                    ) : (
                        <sphereGeometry args={[g.scale, 6, 6]} />
                    )}
                    <meshStandardMaterial color={g.color} roughness={0.8} side={THREE.DoubleSide} />
                </mesh>
            ))}

            {/* Trees & Ground Flora Renderer (Voxel Trees for Gamified vs Realistic Anchored Trees & Flora for Realistic) */}
            <VegetationRenderer />

            {/* Gamified Mode Voxel Floating Pheromone Particles */}
            {isGamified && (
                <GamifiedVoxelParticles terrainConfig={terrainConfig} />
            )}

            {/* Gamified Mode: Minecraft Cubic Sky with Drifting Clouds & Sun/Moon */}
            {isGamified && (
                <VoxelSky isNight={environmentLighting?.isNight} />
            )}

            {/* Gamified Mode: Water Lilies on River & Sugar Cane Reeds on Banks */}
            {isGamified && (
                <VoxelRiverFlora terrainConfig={terrainConfig} />
            )}

            {/* Gamified Mode: Torches with Flickering Cubic Flame & Smoke at Landmark Locations */}
            {isGamified && (
                <group>
                    <VoxelTorch position={[30, getTerrainHeight(30, 30, terrainConfig), 30]} />
                    <VoxelTorch position={[72, getTerrainHeight(72, 72, terrainConfig), 72]} />
                    <VoxelTorch position={[52, getTerrainHeight(52, 42, terrainConfig), 42]} />
                </group>
            )}

            {/* Realistic Mode: Low-Lying River Mist & Forest Floor Leaf Litter Layer */}
            {lookAndFeel === 'REALISTIC' && (
                <group>
                    <RiverMist />
                    <ForestLitterLayer terrainConfig={terrainConfig} />
                    <MacroAtmosphere isNight={environmentLighting?.isNight} terrainConfig={terrainConfig} />
                </group>
            )}

            {/* Nests Renderer */}
            <NestRenderer />

            {/* Ants (LOD System) */}
            <LODAnts ants={ants} />

            {/* Food Sources (Aligned to Terrain Height) */}
            {foodSources.map((food, i) => {
                const foodX = food.x <= 5 ? food.x * 50 : food.x
                const foodZ = food.y <= 5 ? food.y * 50 : food.y
                const foodY = getTerrainHeight(foodX, foodZ, terrainConfig) + 0.3
                return (
                    <FoodSource
                        key={food.id || i}
                        position={[foodX, foodY, foodZ]}
                        quantity={food.quantity}
                        type={food.type}
                    />
                )
            })}

            {/* Predators (Aligned to Terrain Height) */}
            {predators.map((pred, i) => {
                const predX = pred.x <= 5 ? pred.x * 50 : pred.x
                const predZ = pred.y <= 5 ? pred.y * 50 : pred.y
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
