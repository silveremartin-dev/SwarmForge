import { useRef, useMemo, useState } from 'react'
import { useFrame, useThree } from '@react-three/fiber'
import * as THREE from 'three'
import AntMesh from './AntMesh'
import { useSimulationStore } from '../store/simulationStore'

const LOD_DISTANCE_SQ = 15 * 15 // Square distance for LOD switch
const MAX_HIGH_RES = 50 // Limit high res to nearest 50 to prevent freezing

export default function LODAnts({ ants }) {
    const meshRef = useRef()
    const { camera } = useThree()
    const { setSelectedEntity } = useSimulationStore()

    // Track which ants are high-res
    const [highResIndices, setHighResIndices] = useState([])

    const tempObject = useMemo(() => new THREE.Object3D(), [])
    const tempColor = useMemo(() => new THREE.Color(), [])

    // Reuse geometry/materials
    const geometry = useMemo(() => new THREE.SphereGeometry(0.3, 8, 6), [])
    const material = useMemo(() => new THREE.MeshStandardMaterial({ vertexColors: true }), [])

    useFrame(({ clock }) => {
        if (!meshRef.current) return

        const camPos = camera.position
        const nearby = []
        let count = 0

        // Update instances
        ants.forEach((ant, i) => {
            const dx = ant.x - camPos.x
            const dz = ant.y - camPos.z // Ants are on X/Z (y is up)

            const distSq = dx * dx + dz * dz
            const isHighRes = distSq < LOD_DISTANCE_SQ && count < MAX_HIGH_RES

            if (isHighRes) {
                nearby.push(i)
                count++
                // Hide instance
                tempObject.scale.setScalar(0)
            } else {
                // Show instance
                tempObject.position.set(ant.x, 0.3, ant.y)
                const scale = ant.bodyLengthMm ? (ant.bodyLengthMm / 6.0) : (ant.caste === 'QUEEN' ? 2.2 : ant.caste === 'SOLDIER' ? 1.4 : ant.caste === 'MALE' ? 1.2 : 1.0)
                tempObject.scale.setScalar(scale)
                if (ant.heading !== undefined) {
                    tempObject.rotation.y = -ant.heading
                } else {
                    tempObject.rotation.y = 0
                }
            }

            tempObject.updateMatrix()
            meshRef.current.setMatrixAt(i, tempObject.matrix)

            // Color
            let colorStr = ant.caste === 'QUEEN' ? '#ffd700' :
                ant.caste === 'SOLDIER' ? '#ff4444' :
                    ant.caste === 'MALE' ? '#4444ff' : '#8b4513'
            const orderStr = (ant.insectOrder || ant.insectType || ant.family || '').toUpperCase()
            if (orderStr.includes('TERMITE')) colorStr = ant.caste === 'SOLDIER' ? '#ea580c' : '#fef3c7'
            else if (orderStr.includes('BEE')) colorStr = '#fbbf24'
            else if (orderStr.includes('WASP')) colorStr = '#facc15'
            else if (orderStr.includes('APHID')) colorStr = '#84cc16'
            else if (orderStr.includes('BEETLE')) colorStr = '#451a03'
            else if (orderStr.includes('THRIPS')) colorStr = '#d97706'
            tempColor.set(colorStr)
            meshRef.current.setColorAt(i, tempColor)
        })

        meshRef.current.instanceMatrix.needsUpdate = true
        if (meshRef.current.instanceColor) meshRef.current.instanceColor.needsUpdate = true

        if (clock.getElapsedTime() % 0.5 < 0.05) { // Update LOD list every 0.5s
            setHighResIndices(nearby)
        }
    })

    // Interaction
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
                args={[geometry, material, 10000]} // Max 10k capacity
                count={ants.length}
                onClick={handleClick}
            />

            {highResIndices.map(i => {
                const ant = ants[i]
                if (!ant) return null
                const antScale = ant.bodyLengthMm ? (ant.bodyLengthMm / 6.0) : (ant.caste === 'QUEEN' ? 2.2 : ant.caste === 'SOLDIER' ? 1.4 : ant.caste === 'MALE' ? 1.2 : 1.0)
                return (
                    <AntMesh
                        key={ant.id}
                        position={[ant.x, 0.3, ant.y]}
                        caste={ant.caste}
                        scale={antScale}
                        diseaseState={ant.diseaseState || 'HEALTHY'}
                        insectOrder={ant.insectOrder || ant.insectType || ant.family || 'ANT'}
                    />
                )
            })}
        </group>
    )
}
