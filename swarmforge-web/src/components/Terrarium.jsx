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
import { soundEngine } from '../utils/soundEngine'

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

export default function Terrarium() {
    const { ants, foodSources, predators, environment, lookAndFeel, environmentLighting, disasterState } = useSimulationStore()
    const groupRef = useRef()
    const riverMeshRef = useRef()
    const floodMeshRef = useRef()
    const { camera } = useThree()

    const isGamified = lookAndFeel === 'GAMING' || lookAndFeel === 'SCIENTIFIC'

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
        if (lookAndFeel === 'SCIENTIFIC') return '#090d16'
        return '#87ceeb'
    }, [environmentLighting?.isNight, lookAndFeel])

    // Procedural Splatting Ground Texture for Realistic Mode
    const splattingTexture = useMemo(() => {
        if (lookAndFeel === 'REALISTIC') {
            return createSplattingGroundTexture()
        }
        return null
    }, [lookAndFeel])

    // Ground Surface color adapted by Look & Feel theme
    const groundColor = useMemo(() => {
        if (lookAndFeel === 'SCIENTIFIC') return '#1e293b'
        if (lookAndFeel === 'REALISTIC') return '#ffffff' // texture tinted
        return '#3d2817'
    }, [lookAndFeel])

    // Ground Surface plane (100m x 100m, centered at [50, 0, 50])
    const groundGeometry = useMemo(() => new THREE.PlaneGeometry(100, 100), [])
    const groundMaterial = useMemo(() => {
        if (lookAndFeel === 'REALISTIC' && splattingTexture) {
            return new THREE.MeshStandardMaterial({
                map: splattingTexture,
                roughness: 0.88,
                metalness: 0.05,
                side: THREE.FrontSide,
            })
        }
        return new THREE.MeshStandardMaterial({
            color: groundColor,
            roughness: 0.85,
            metalness: lookAndFeel === 'SCIENTIFIC' ? 0.3 : 0.1,
            side: THREE.FrontSide,
        })
    }, [groundColor, lookAndFeel, splattingTexture])

    // Geological Stratum 1: Topsoil / Humus (Fully opaque solid BoxGeometry)
    const topsoilGeo = useMemo(() => new THREE.BoxGeometry(100, 0.8, 100), [])
    const topsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: lookAndFeel === 'REALISTIC' ? '#352417' : '#4a321f',
        roughness: 0.9,
        metalness: 0.05,
        side: THREE.FrontSide,
    }), [lookAndFeel])

    // Geological Stratum 2: Subsoil / Clay & Sand
    const subsoilGeo = useMemo(() => new THREE.BoxGeometry(100, 2.0, 100), [])
    const subsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#6b4c33',
        roughness: 0.95,
        metalness: 0.05,
        side: THREE.FrontSide,
    }), [])

    // Geological Stratum 3: Bedrock & Deep Stone
    const bedrockGeo = useMemo(() => new THREE.BoxGeometry(100, 2.2, 100), [])
    const bedrockMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#2c2825',
        roughness: 0.98,
        metalness: 0.2,
        side: THREE.FrontSide,
    }), [])

    // Subterranean Water Table Horizon
    const waterTableGeo = useMemo(() => new THREE.BoxGeometry(100, 0.15, 100), [])
    const waterTableMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#0284c7',
        transparent: true,
        opacity: 0.75,
        roughness: 0.1,
        side: THREE.DoubleSide,
    }), [])

    // ── 3D RIVER STREAM (Voxel Water Cubes in Gamified mode vs Liquid Plane in Realistic) ──
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

    // Array of aligned 3D River Voxel Cubes for Gamified Mode
    const voxelRiverCubes = useMemo(() => {
        const cubes = []
        for (let z = 0; z <= 100; z += 2) {
            for (let x = 20; x <= 28; x += 2) {
                cubes.push({ id: `r_${x}_${z}`, x, z })
            }
        }
        return cubes
    }, [])

    // ── CUBIC OUTER BORDER / VOXEL RIM (Bordure cubique en empilement de blocs) ──
    const voxelRimBlocks = useMemo(() => {
        const blocks = []
        const rimColor = lookAndFeel === 'SCIENTIFIC' ? '#2563eb' : (lookAndFeel === 'REALISTIC' ? '#84cc16' : '#0284c7')

        // Generate stepped cubic rim voxel blocks along all 4 outer edges of the 100m x 100m terrarium
        for (let i = 0; i <= 100; i += 2) {
            blocks.push({ id: `rim_n_${i}`, pos: [i, 0.1, 0], color: rimColor })
            blocks.push({ id: `rim_s_${i}`, pos: [i, 0.1, 100], color: rimColor })
            blocks.push({ id: `rim_w_${i}`, pos: [0, 0.1, i], color: rimColor })
            blocks.push({ id: `rim_e_${i}`, pos: [100, 0.1, i], color: rimColor })
        }
        return blocks
    }, [lookAndFeel])

    // Option B1: Volumetric Voxel Gravel & Quartz Pebble Inclusions on Cutaway Side Walls (Cubic in Gamified mode)
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

            {/* Ground Surface */}
            <mesh
                geometry={groundGeometry}
                material={groundMaterial}
                rotation={[-Math.PI / 2, 0, 0]}
                position={[50, 0, 50]}
                receiveShadow
            />

            {/* River Stream: Aligned Voxel Cubes in Gamified mode vs Liquid Surface in Realistic mode */}
            {isGamified ? (
                <group>
                    {voxelRiverCubes.map((c) => (
                        <mesh key={c.id} position={[c.x, 0.05, c.z]} receiveShadow>
                            <boxGeometry args={[1.9, 0.25, 1.9]} />
                            <meshStandardMaterial
                                color="#0284c7"
                                roughness={0.15}
                                metalness={0.7}
                                transparent
                                opacity={0.9}
                                emissive="#0369a1"
                                emissiveIntensity={0.25}
                                side={THREE.DoubleSide}
                            />
                        </mesh>
                    ))}
                </group>
            ) : (
                <mesh
                    ref={riverMeshRef}
                    geometry={riverGeometry}
                    material={riverMaterial}
                    rotation={[-Math.PI / 2, 0, 0]}
                    position={[25, 0.02, 50]}
                    receiveShadow
                />
            )}

            {/* Natural Disaster: Flash Flood Water Level Layer */}
            {disasterState?.activeDisaster === 'FLASH_FLOOD' && (
                <mesh ref={floodMeshRef} position={[50, 0.2, 50]}>
                    <boxGeometry args={[100, 0.4, 100]} />
                    <meshStandardMaterial color="#0284c7" transparent opacity={0.7} roughness={0.1} side={THREE.DoubleSide} />
                </mesh>
            )}

            {/* Geological Skirt Stratum 1: Topsoil / Humus (Y: [0, -0.8]) */}
            <mesh
                geometry={topsoilGeo}
                material={topsoilMat}
                position={[50, -0.4, 50]}
                receiveShadow
            />

            {/* Geological Skirt Stratum 2: Subsoil (Y: [-0.8, -2.8]) */}
            <mesh
                geometry={subsoilGeo}
                material={subsoilMat}
                position={[50, -1.8, 50]}
                receiveShadow
            />

            {/* Geological Skirt Stratum 3: Bedrock (Y: [-2.8, -5.0]) */}
            <mesh
                geometry={bedrockGeo}
                material={bedrockMat}
                position={[50, -3.9, 50]}
                receiveShadow
            />

            {/* Subterranean Water Table Horizon */}
            <mesh
                geometry={waterTableGeo}
                material={waterTableMat}
                position={[50, -3.1, 50]}
            />

            {/* CUBIC OUTER BORDER / VOXEL RIM (Bordure cubique en blocs 3D) */}
            <group>
                {voxelRimBlocks.map((b) => (
                    <mesh key={b.id} position={b.pos} castShadow receiveShadow>
                        <boxGeometry args={[2.05, 0.25, 2.05]} />
                        <meshStandardMaterial color={b.color} roughness={0.3} metalness={0.7} side={THREE.DoubleSide} />
                    </mesh>
                ))}
            </group>

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

            {/* Nests Renderer */}
            <NestRenderer />

            {/* Ants (LOD System) */}
            <LODAnts ants={ants} />

            {/* Food Sources */}
            {foodSources.map((food, i) => (
                <FoodSource
                    key={food.id || i}
                    position={[food.x, 0.5, food.y]}
                    quantity={food.quantity}
                    type={food.type}
                />
            ))}

            {/* Predators */}
            {predators.map((pred, i) => (
                <Predator
                    key={pred.id || i}
                    position={[pred.x, 0.5, pred.y]}
                    type={pred.type}
                    state={pred.state}
                />
            ))}
        </group>
    )
}
