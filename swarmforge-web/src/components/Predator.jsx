import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'

const predatorColors = {
    SPIDER: '#333333',
    ANTLION: '#aa8855',
    BEETLE: '#224422',
    BIRD: '#4466aa',
    LIZARD: '#668844',
}

const predatorScales = {
    SPIDER: 1.5,
    ANTLION: 1.2,
    BEETLE: 1.8,
    BIRD: 3,
    LIZARD: 4,
}

export default function Predator({ position, type = 'SPIDER', state = 'IDLE' }) {
    const meshRef = useRef()
    const color = predatorColors[type] || predatorColors.SPIDER
    const scale = predatorScales[type] || 1.5

    // Animation based on state
    useFrame((clock) => {
        if (!meshRef.current) return

        if (state === 'CHASING') {
            // Bobbing animation
            meshRef.current.position.y = position[1] + Math.sin(clock.clock.elapsedTime * 10) * 0.2
        } else if (state === 'ATTACKING') {
            // Shake animation
            meshRef.current.rotation.z = Math.sin(clock.clock.elapsedTime * 20) * 0.1
        }
    })

    return (
        <mesh ref={meshRef} position={position} scale={scale}>
            {type === 'SPIDER' ? (
                <octahedronGeometry args={[0.5, 0]} />
            ) : type === 'BIRD' ? (
                <coneGeometry args={[0.3, 1, 4]} />
            ) : (
                <boxGeometry args={[0.8, 0.4, 1.2]} />
            )}
            <meshStandardMaterial
                color={color}
                emissive={state === 'ATTACKING' ? '#ff0000' : '#000000'}
                emissiveIntensity={state === 'ATTACKING' ? 0.5 : 0}
            />
        </mesh>
    )
}
