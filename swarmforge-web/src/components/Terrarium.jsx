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

    // Ground Surface plane
    const groundGeometry = useMemo(() => new THREE.PlaneGeometry(100, 100), [])
    const groundMaterial = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#3d2817',
        roughness: 0.85,
        metalness: 0.1,
    }), [])

    // Geological Stratum 1: Topsoil / Humus (0 to -0.8m)
    const topsoilGeo = useMemo(() => new THREE.BoxGeometry(100.1, 0.8, 100.1), [])
    const topsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#4a321f',
        roughness: 0.9,
        metalness: 0.05,
    }), [])

    // Geological Stratum 2: Subsoil / Clay & Sand (-0.8m to -2.8m)
    const subsoilGeo = useMemo(() => new THREE.BoxGeometry(100.1, 2.0, 100.1), [])
    const subsoilMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#6b4c33',
        roughness: 0.95,
        metalness: 0.05,
    }), [])

    // Geological Stratum 3: Bedrock & Deep Stone (-2.8m to -5.0m)
    const bedrockGeo = useMemo(() => new THREE.BoxGeometry(100.1, 2.2, 100.1), [])
    const bedrockMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#2c2825',
        roughness: 0.98,
        metalness: 0.2,
    }), [])

    // Underground Water Table Line (Nappe phréatique)
    const waterTableGeo = useMemo(() => new THREE.BoxGeometry(100.2, 0.15, 100.2), [])
    const waterTableMat = useMemo(() => new THREE.MeshStandardMaterial({
        color: '#0284c7',
        transparent: true,
        opacity: 0.65,
        roughness: 0.1,
    }), [])

    return (
        <group ref={groupRef}>
            {/* Ground Surface */}
            <mesh
                geometry={groundGeometry}
                material={groundMaterial}
                rotation={[-Math.PI / 2, 0, 0]}
                position={[50, -0.01, 50]}
                receiveShadow
            />

            {/* Geological Skirt Stratum 1: Topsoil / Humus (Couche arable) */}
            <mesh
                geometry={topsoilGeo}
                material={topsoilMat}
                position={[50, -0.4, 50]}
                receiveShadow
            />

            {/* Geological Skirt Stratum 2: Clay & Sand Subsoil (Substrat d'Argile & Sable) */}
            <mesh
                geometry={subsoilGeo}
                material={subsoilMat}
                position={[50, -1.8, 50]}
                receiveShadow
            />

            {/* Geological Skirt Stratum 3: Bedrock & Deep Stone (Roche mère) */}
            <mesh
                geometry={bedrockGeo}
                material={bedrockMat}
                position={[50, -3.9, 50]}
                receiveShadow
            />

            {/* Subterranean Water Table Cutaway Horizon (Nappe Phréatique) */}
            <mesh
                geometry={waterTableGeo}
                material={waterTableMat}
                position={[50, -3.1, 50]}
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
