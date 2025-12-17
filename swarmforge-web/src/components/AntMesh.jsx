import { useMemo } from 'react'
import * as THREE from 'three'

const casteColors = {
    WORKER: '#8b4513',
    SOLDIER: '#ff4444',
    QUEEN: '#ffd700',
    MALE: '#4444ff',
    NURSE: '#44aa44',
}

export default function AntMesh({ position, caste = 'WORKER', scale = 1 }) {
    const color = casteColors[caste] || casteColors.WORKER

    const geometry = useMemo(() => {
        // Simple ant shape: 3 spheres
        const group = new THREE.Group()

        // Head
        const headGeo = new THREE.SphereGeometry(0.15 * scale, 8, 6)
        const head = new THREE.Mesh(headGeo)
        head.position.z = 0.3 * scale

        // Thorax
        const thoraxGeo = new THREE.SphereGeometry(0.12 * scale, 8, 6)
        const thorax = new THREE.Mesh(thoraxGeo)

        // Abdomen
        const abdomenGeo = new THREE.SphereGeometry(0.2 * scale, 8, 6)
        const abdomen = new THREE.Mesh(abdomenGeo)
        abdomen.position.z = -0.35 * scale

        return headGeo // Simplified for now
    }, [scale])

    return (
        <group position={position}>
            {/* Head */}
            <mesh position={[0, 0, 0.3 * scale]}>
                <sphereGeometry args={[0.15 * scale, 8, 6]} />
                <meshStandardMaterial color={color} />
            </mesh>
            {/* Thorax */}
            <mesh>
                <sphereGeometry args={[0.12 * scale, 8, 6]} />
                <meshStandardMaterial color={color} />
            </mesh>
            {/* Abdomen */}
            <mesh position={[0, 0, -0.35 * scale]}>
                <sphereGeometry args={[0.2 * scale, 8, 6]} />
                <meshStandardMaterial color={color} />
            </mesh>
        </group>
    )
}
