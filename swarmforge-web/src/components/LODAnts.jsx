import { useRef, useMemo } from 'react'
import { useFrame, useThree } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import { getTerrainHeight } from '../utils/terrainUtils'

export default function LODAnts({ ants = [] }) {
    const meshRef = useRef()
    const hitMeshRef = useRef()
    const { setSelectedEntity, terrainConfig } = useSimulationStore()

    const tempObject = useMemo(() => new THREE.Object3D(), [])
    const tempColor = useMemo(() => new THREE.Color(), [])

    // Visual ant geometry
    const geometry = useMemo(() => new THREE.SphereGeometry(0.28, 8, 6), [])
    const material = useMemo(() => new THREE.MeshStandardMaterial({
        vertexColors: true,
        roughness: 0.5,
        metalness: 0.2,
        depthTest: true,
        depthWrite: true
    }), [])

    // Generous hitbox geometry for effortless 1-click ant selection
    const hitGeometry = useMemo(() => new THREE.SphereGeometry(1.5, 8, 6), [])
    const hitMaterial = useMemo(() => new THREE.MeshBasicMaterial({
        visible: false,
        depthWrite: false
    }), [])

    useFrame(() => {
        if (!ants || ants.length === 0) return

        for (let i = 0; i < ants.length; i++) {
            const ant = ants[i]
            if (!ant) continue
            const antX = ant.x ?? 50
            const antZ = ant.z !== undefined ? ant.z : (ant.y ?? 50)

            const groundY = getTerrainHeight(antX, antZ, terrainConfig)
            const isClimbing = Boolean(ant.isClimbingTree || ant.climbingTree || (ant.treeClimbHeight && ant.treeClimbHeight > 0))
            const treeOffset = isClimbing ? (ant.treeClimbHeight || 2.2) : 0
            const antY = groundY + 0.15 + treeOffset

            tempObject.position.set(antX, antY, antZ)
            const scale = ant.bodyLengthMm
                ? (ant.bodyLengthMm / 6.0)
                : (ant.caste === 'QUEEN' ? 2.4 : (ant.caste === 'SOLDIER' ? 1.5 : (ant.caste === 'MALE' ? 1.3 : 1.0)))

            tempObject.scale.setScalar(scale)

            if (ant.heading !== undefined) {
                tempObject.rotation.set(0, -ant.heading, 0)
            } else {
                tempObject.rotation.set(0, 0, 0)
            }

            tempObject.updateMatrix()
            if (meshRef.current) {
                meshRef.current.setMatrixAt(i, tempObject.matrix)
            }
            if (hitMeshRef.current) {
                hitMeshRef.current.setMatrixAt(i, tempObject.matrix)
            }

            // Castes & order coloring
            let colorStr = ant.caste === 'QUEEN' ? '#ffd700' :
                ant.caste === 'SOLDIER' ? '#ef4444' :
                    ant.caste === 'MALE' ? '#38bdf8' : (ant.color || '#8b4513')

            if (ant.diseaseState && ant.diseaseState !== 'HEALTHY') {
                colorStr = '#84cc16' // Infected sickly green
            }

            const orderStr = (ant.insectOrder || ant.insectType || ant.family || '').toUpperCase()
            if (orderStr.includes('TERMITE')) colorStr = ant.caste === 'SOLDIER' ? '#ea580c' : '#fef3c7'
            else if (orderStr.includes('BEE')) colorStr = '#fbbf24'
            else if (orderStr.includes('WASP')) colorStr = '#facc15'

            tempColor.set(colorStr)
            if (meshRef.current) {
                meshRef.current.setColorAt(i, tempColor)
            }
        }

        if (meshRef.current) {
            meshRef.current.instanceMatrix.needsUpdate = true
            if (meshRef.current.instanceColor) meshRef.current.instanceColor.needsUpdate = true
        }
        if (hitMeshRef.current) {
            hitMeshRef.current.instanceMatrix.needsUpdate = true
        }
    })

    // Interaction: Click to inspect and track ant
    const handleAntClick = (e) => {
        e.stopPropagation()
        const instanceId = e.instanceId
        if (instanceId !== undefined && ants[instanceId]) {
            setSelectedEntity(ants[instanceId])
        }
    }

    const handlePointerOver = (e) => {
        e.stopPropagation()
        document.body.style.cursor = 'pointer'
    }

    const handlePointerOut = () => {
        document.body.style.cursor = 'auto'
    }

    return (
        <group>
            {/* Visual instanced ants */}
            <instancedMesh
                ref={meshRef}
                args={[geometry, material, 10000]}
                count={ants.length}
                castShadow
                receiveShadow
            />
            {/* Generous hitbox instanced mesh for direct raycasting */}
            <instancedMesh
                ref={hitMeshRef}
                args={[hitGeometry, hitMaterial, 10000]}
                count={ants.length}
                onClick={handleAntClick}
                onPointerOver={handlePointerOver}
                onPointerOut={handlePointerOut}
            />
        </group>
    )
}
