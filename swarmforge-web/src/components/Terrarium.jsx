import { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import AntMesh from './AntMesh'
import LODAnts from './LODAnts'
import FoodSource from './FoodSource'
import Predator from './Predator'

export default function Terrarium() {
    const { ants, foodSources, predators, environment } = useSimulationStore()
    const groupRef = useRef()

    // Ground Surface plane (100m x 100m, centered at [50, 0, 50])
    const groundGeometry = useMemo(() => new THREE.PlaneGeometry(100, 100), [])
    const groundMaterial = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#3d2817',
        roughness: 0.85,
        metalness: 0.1,
    }), [])

    // Geological Stratum 1: Topsoil / Humus (Y: 0.0m to -0.8m, height 0.8m)
    const topsoilGeo = useMemo(() => new THREE.BoxGeometry(100, 0.8, 100), [])
    const topsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#4a321f',
        roughness: 0.9,
        metalness: 0.05,
    }), [])

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

    // Option A2: Chamfered Bezel Glass Enclosure Rim (Cadre d'observation d'aquarium/terrarium à Y: 0)
    const bezelGeo = useMemo(() => new THREE.BoxGeometry(100.2, 0.12, 100.2), [])
    const bezelMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#38bdf8',
        metalness: 0.8,
        roughness: 0.2,
        transparent: true,
        opacity: 0.9,
    }), [])

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
            {/* Ground Surface - Exactly flush at Y=0 */}
            <mesh
                geometry={groundGeometry}
                material={groundMaterial}
                rotation={[-Math.PI / 2, 0, 0]}
                position={[50, 0, 50]}
                receiveShadow
            />

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
