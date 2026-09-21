import { useRef, useMemo } from 'react'
import { useFrame, useThree } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import { getTerrainHeight } from '../utils/terrainUtils'

export default function LODAnts({ ants = [] }) {
    const meshRef = useRef()
    const { setSelectedEntity, terrainConfig, antTrackingEnabled } = useSimulationStore()

    const tempObject = useMemo(() => new THREE.Object3D(), [])
    const tempColor = useMemo(() => new THREE.Color(), [])

    // Optimized instanced ant geometry (capsule + head shape)
    const geometry = useMemo(() => new THREE.SphereGeometry(0.22, 8, 6), [])
    const material = useMemo(() => new THREE.MeshStandardMaterial({
        vertexColors: true,
        roughness: 0.6,
        metalness: 0.2,
        depthTest: true,
        depthWrite: true
    }), [])

    useFrame(() => {
        if (!meshRef.current || !ants || ants.length === 0) return

        for (let i = 0; i < ants.length; i++) {
            const ant = ants[i]
            if (!ant) continue
            const antX = ant.x ?? 50
            const antZ = ant.z !== undefined ? ant.z : (ant.y ?? 50)

            const groundY = getTerrainHeight(antX, antZ, terrainConfig)
            const isClimbing = Boolean(ant.isClimbingTree || ant.climbingTree || (ant.treeClimbHeight && ant.treeClimbHeight > 0))
            const treeOffset = isClimbing ? (ant.treeClimbHeight || 2.2) : 0
            const antY = groundY + 0.12 + treeOffset

            tempObject.position.set(antX, antY, antZ)
            const scale = ant.bodyLengthMm
                ? (ant.bodyLengthMm / 6.0)
                : (ant.caste === 'QUEEN' ? 2.2 : (ant.caste === 'SOLDIER' ? 1.4 : (ant.caste === 'MALE' ? 1.2 : 1.0)))

            tempObject.scale.setScalar(scale)

            if (ant.heading !== undefined) {
                tempObject.rotation.set(0, -ant.heading, 0)
            } else {
                tempObject.rotation.set(0, 0, 0)
            }

            tempObject.updateMatrix()
            meshRef.current.setMatrixAt(i, tempObject.matrix)

            // Castes & order coloring
            let colorStr = ant.caste === 'QUEEN' ? '#ffd700' :
                ant.caste === 'SOLDIER' ? '#ef4444' :
                    ant.caste === 'MALE' ? '#38bdf8' : '#8b4513'

            if (ant.diseaseState && ant.diseaseState !== 'HEALTHY') {
                colorStr = '#84cc16' // Infected sickly green
            }

            const orderStr = (ant.insectOrder || ant.insectType || ant.family || '').toUpperCase()
            if (orderStr.includes('TERMITE')) colorStr = ant.caste === 'SOLDIER' ? '#ea580c' : '#fef3c7'
            else if (orderStr.includes('BEE')) colorStr = '#fbbf24'
            else if (orderStr.includes('WASP')) colorStr = '#facc15'

            tempColor.set(colorStr)
            meshRef.current.setColorAt(i, tempColor)
        }

        meshRef.current.instanceMatrix.needsUpdate = true
        if (meshRef.current.instanceColor) meshRef.current.instanceColor.needsUpdate = true
    })

    // Interaction: Click to inspect and track ant
    const handleClick = (e) => {
        e.stopPropagation()
        const instanceId = e.instanceId
        if (instanceId !== undefined && ants[instanceId]) {
            setSelectedEntity(ants[instanceId])
        }
    }

    return (
        <group>
            <instancedMesh
                ref={meshRef}
                args={[geometry, material, 10000]}
                count={ants.length}
                onClick={handleClick}
                castShadow
                receiveShadow
            />
        </group>
    )
}
