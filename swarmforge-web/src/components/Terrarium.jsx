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

    // Underground Water Table Line (Nappe phréatique at Y: -3.1m)
    const waterTableGeo = useMemo(() => new THREE.BoxGeometry(100, 0.15, 100), [])
    const waterTableMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#0284c7',
        transparent: true,
        opacity: 0.65,
        roughness: 0.1,
    }), [])

    // Outer Perimeter Skirt Frame (Encloses the 4 vertical sides cleanly at X=0,100 Z=0,100)
    const skirtBorderGeo = useMemo(() => new THREE.BoxGeometry(100.05, 5.0, 100.05), [])
    const skirtBorderMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#1c1917',
        wireframe: false,
        roughness: 0.95,
        side: THREE.BackSide,
    }), [])

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
