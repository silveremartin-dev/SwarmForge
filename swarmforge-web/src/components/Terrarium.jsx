import { useRef, useMemo } from 'react'
import { useFrame, useThree } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import AntMesh from './AntMesh'
import LODAnts from './LODAnts'
import FoodSource from './FoodSource'
import Predator from './Predator'
import NestRenderer from './NestRenderer'
import { soundEngine } from '../utils/soundEngine'

export default function Terrarium() {
    const { ants, foodSources, predators, environment, lookAndFeel, environmentLighting, disasterState } = useSimulationStore()
    const groupRef = useRef()
    const riverMeshRef = useRef()
    const floodMeshRef = useRef()
    const { camera } = useThree()

    // Realtime spatial listener & river audio update based on 3D camera distance
    useFrame((state) => {
        if (camera) {
            soundEngine.updateSpatialListener(camera.position.x, camera.position.y, camera.position.z)
            soundEngine.updateRiverSound(camera.position, { x: 25, y: 0, z: 50 })
        }
        if (riverMeshRef.current) {
            // Animate subtle water surface ripples
            riverMeshRef.current.position.y = 0.02 + Math.sin(state.clock.getElapsedTime() * 2) * 0.015
        }
        if (floodMeshRef.current && disasterState?.activeDisaster === 'FLASH_FLOOD') {
            const floodLevel = 0.2 + (disasterState.intensity / 100) * 1.5
            floodMeshRef.current.position.y = Math.min(1.5, floodLevel)
        }
    })

    // Dynamic Fog Color based on Day/Night & Climate
    const fogColor = useMemo(() => {
        if (environmentLighting?.isNight) return '#0f172a' // Midnight blue fog
        if (lookAndFeel === 'SCIENTIFIC') return '#090d16'
        return '#87ceeb' // Sky blue daytime fog
    }, [environmentLighting?.isNight, lookAndFeel])

    // Ground Surface color adapted by Look & Feel theme
    const groundColor = useMemo(() => {
        if (lookAndFeel === 'SCIENTIFIC') return '#1e293b' // High-contrast slate laboratory floor
        if (lookAndFeel === 'REALISTIC') return '#2d3a24'  // Mossy forest topsoil
        return '#3d2817'                                  // Gaming earth brown
    }, [lookAndFeel])

    // Ground Surface plane (100m x 100m, centered at [50, 0, 50])
    const groundGeometry = useMemo(() => new THREE.PlaneGeometry(100, 100), [])
    const groundMaterial = useMemo(() => new THREE.MeshStandardMaterial({
        color: groundColor,
        roughness: 0.85,
        metalness: lookAndFeel === 'SCIENTIFIC' ? 0.3 : 0.1,
    }), [groundColor, lookAndFeel])

    // Geological Stratum 1: Topsoil / Humus (Y: 0.0m to -0.8m, height 0.8m)
    const topsoilGeo = useMemo(() => new THREE.BoxGeometry(100, 0.8, 100), [])
    const topsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: lookAndFeel === 'REALISTIC' ? '#352417' : '#4a321f',
        roughness: 0.9,
        metalness: 0.05,
    }), [lookAndFeel])

    // Geological Stratum 2: Subsoil / Clay & Sand (Y: -0.8m to -2.8m, height 2.0m)
    const subsoilGeo = useMemo(() => new THREE.BoxGeometry(100, 2.0, 100), [])
    const subsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#6b4c33',
        roughness: 0.95,
        metalness: 0.05,
    }), [])

    // Geological Stratum 3: Bedrock & Deep Stone (Y: -2.8m to -5.0m, height 2.2m)
    const bedrockGeo = useMemo(() => new THREE.BoxGeometry(100, 2.2, 100), [])
    const bedrockMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#2c2825',
        roughness: 0.98,
        metalness: 0.2,
    }), [])

    // Subterranean Water Table Cutaway Horizon (Nappe Phréatique Y: -3.1m)
    const waterTableGeo = useMemo(() => new THREE.BoxGeometry(100, 0.15, 100), [])
    const waterTableMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#0284c7',
        transparent: true,
        opacity: 0.65,
        roughness: 0.1,
    }), [])

    // 3D Surface River Stream Mesh (Running from Z=0 to Z=100 at X=25)
    const riverGeometry = useMemo(() => new THREE.PlaneGeometry(12, 100), [])
    const riverMaterial = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#0284c7',
        roughness: 0.1,
        metalness: 0.8,
        transparent: true,
        opacity: 0.85,
        emissive: '#0369a1',
        emissiveIntensity: 0.2,
    }), [])

    // Option A2: Chamfered Bezel Glass Enclosure Rim (Cadre d'observation d'aquarium/terrarium à Y: 0)
    const bezelGeo = useMemo(() => new THREE.BoxGeometry(100.2, 0.12, 100.2), [])
    const bezelMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: lookAndFeel === 'SCIENTIFIC' ? '#2563eb' : (lookAndFeel === 'REALISTIC' ? '#84cc16' : '#38bdf8'),
        metalness: 0.8,
        roughness: 0.2,
        transparent: true,
        opacity: 0.9,
    }), [lookAndFeel])

    // Outer Perimeter Skirt Frame (Encloses the 4 vertical sides cleanly at X=0,100 Z=0,100)
    const skirtBorderGeo = useMemo(() => new THREE.BoxGeometry(100.05, 5.0, 100.05), [])
    const skirtBorderMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#1c1917',
        wireframe: false,
        roughness: 0.95,
        side: THREE.BackSide,
    }), [])

    // Option B1: Volumetric Gravel & Pebble Inclusions along the cutaway walls
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
            const scale = 0.15 + rand(i * 7) * 0.35
            const color = i % 3 === 0 ? '#e2e8f0' : (i % 3 === 1 ? '#7c2d12' : '#fef08a')
            items.push({ id: i, pos: [x, y, z], scale, color })
        }
        return items
    }, [])

    return (
        <group ref={groupRef}>
            {/* Atmospheric Fog Effect */}
            <fogExp2 attach="fog" args={[fogColor, 0.008]} />

            {/* Ground Surface - Exactly flush at Y=0 */}
            <mesh
                geometry={groundGeometry}
                material={groundMaterial}
                rotation={[-Math.PI / 2, 0, 0]}
                position={[50, 0, 50]}
                receiveShadow
            />

            {/* 3D River Stream flowing on the terrarium surface at X=25 */}
            <mesh
                ref={riverMeshRef}
                geometry={riverGeometry}
                material={riverMaterial}
                rotation={[-Math.PI / 2, 0, 0]}
                position={[25, 0.02, 50]}
                receiveShadow
            />

            {/* Natural Disaster: Flash Flood Water Level Layer */}
            {disasterState?.activeDisaster === 'FLASH_FLOOD' && (
                <mesh ref={floodMeshRef} position={[50, 0.2, 50]}>
                    <boxGeometry args={[100, 0.4, 100]} />
                    <meshStandardMaterial color="#0284c7" transparent opacity={0.7} roughness={0.1} />
                </mesh>
            )}

            {/* Geological Skirt Stratum 1: Topsoil / Humus (Couche arable Y: [0, -0.8]) */}
            <mesh
                geometry={topsoilGeo}
                material={topsoilMat}
                position={[50, -0.4, 50]}
                receiveShadow
            />

            {/* Geological Skirt Stratum 2: Clay & Sand Subsoil (Substrat d'Argile & Sable Y: [-0.8, -2.8]) */}
            <mesh
                geometry={subsoilGeo}
                material={subsoilMat}
                position={[50, -1.8, 50]}
                receiveShadow
            />

            {/* Geological Skirt Stratum 3: Bedrock & Deep Stone (Roche mère Y: [-2.8, -5.0]) */}
            <mesh
                geometry={bedrockGeo}
                material={bedrockMat}
                position={[50, -3.9, 50]}
                receiveShadow
            />

            {/* Subterranean Water Table Cutaway Horizon (Nappe Phréatique Y: -3.1) */}
            <mesh
                geometry={waterTableGeo}
                material={waterTableMat}
                position={[50, -3.1, 50]}
            />

            {/* Option A2: Chamfered Glass Bezel Rim Frame (Seams sealing top edge) */}
            <mesh
                geometry={bezelGeo}
                material={bezelMat}
                position={[50, 0, 50]}
            />

            {/* Option B1: Volumetric Gravel & Quartz Pebble Inclusions on Cutaway Side Walls */}
            {gravelInclusions.map((g) => (
                <mesh key={g.id} position={g.pos}>
                    <sphereGeometry args={[g.scale, 6, 6]} />
                    <meshStandardMaterial color={g.color} roughness={0.8} />
                </mesh>
            ))}

            {/* Perimeter Skirt Backing Box (Smooth clean dark edges) */}
            <mesh
                geometry={skirtBorderGeo}
                material={skirtBorderMat}
                position={[50, -2.5, 50]}
            />

            {/* Nests Renderer (Épine de pin, Termitière, Guêpier + branche, Ruche en bois...) */}
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
