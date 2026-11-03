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
    MacroAtmosphere,
    GamifiedVoxelParticles,
    VoxelTerrain,
    AntSelectionReticle3D,
    VoxelCursor3D
} from './TerrariumSubComponents'

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

    const { running, isPaused, speed } = useSimulationStore()
    const isSimActive = Boolean(running && !isPaused && (typeof speed === 'number' ? speed > 0 : true))
    const riverTimeRef = useRef(0)

    // Realtime spatial listener & river audio update based on 3D camera distance
    useFrame((state, delta) => {
        if (isSimActive) {
            riverTimeRef.current += delta * (speed || 1.0)
        }
        if (riverMeshRef.current) {
            riverMeshRef.current.position.y = 0.02 + Math.sin(riverTimeRef.current * 2) * 0.01
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
