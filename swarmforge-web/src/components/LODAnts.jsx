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

    // Staggered update to avoid checking 10k ants every frame for LOD switch
    // But matrix updates must be every frame for smooth movement
    useFrame(({ clock }) => {
        if (!meshRef.current) return

        const camPos = camera.position
        const nearby = []
        let count = 0

        // Update instances
        ants.forEach((ant, i) => {
            const dx = ant.x - camPos.x
            const dz = ant.y - camPos.z // Ants are on X/Z (y is up)
            // Note: camera y affects distance but X/Z is dominant for map view

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
                const scale = ant.caste === 'QUEEN' ? 2 : ant.caste === 'SOLDIER' ? 1.3 : 1
                tempObject.scale.setScalar(scale)
                // Rotate based on heading if available (ant.heading)
                if (ant.heading !== undefined) {
                    tempObject.rotation.y = -ant.heading // Adjust depending on coords
                } else {
                    tempObject.rotation.y = 0
                }
            }

            tempObject.updateMatrix()
            meshRef.current.setMatrixAt(i, tempObject.matrix)

            // Color
            const color = ant.caste === 'QUEEN' ? '#ffd700' :
                ant.caste === 'SOLDIER' ? '#ff4444' :
                    ant.caste === 'MALE' ? '#4444ff' : '#8b4513'
            tempColor.set(color)
            meshRef.current.setColorAt(i, tempColor)
        })

        meshRef.current.instanceMatrix.needsUpdate = true
        if (meshRef.current.instanceColor) meshRef.current.instanceColor.needsUpdate = true

        // Update state mainly for React rendering of High Res
        // Optimization: Only set state if significantly different to avoid re-renders?
        // React's diffing handles it, but creating new array every frame is meh.
        // Actually, setting state inside useFrame triggers re-render every frame -> BAD.

        // FIX: We should perhaps manage High Res Rendering purely effectively or throttle updates.
        // For now, let's throttle the SetState
        if (clock.getElapsedTime() % 0.5 < 0.05) { // Update LOD list every 0.5s
            setHighResIndices(nearby)
        }
    })

    // Interaction
    const handleClick = (e) => {
        e.stopPropagation()
        const instanceId = e.instanceId
        if (instanceId !== undefined && ants[instanceId]) {
            // If hidden (high res), click might pass through or still hit invisible instance?
            // Raycaster hits invisible instances usually? No.
            // If scale is 0, it won't hit.
            // So we depend on AntMesh for clicks on high res.
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
                return (
                    <AntMesh
                        key={ant.id}
                        position={[ant.x, 0.3, ant.y]}
                        caste={ant.caste}
                        scale={ant.caste === 'QUEEN' ? 2 : ant.caste === 'SOLDIER' ? 1.3 : 1}
                    // Pass heading if AntMesh supports it
                    />
                )
            })}
        </group>
    )
}
