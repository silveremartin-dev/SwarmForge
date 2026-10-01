import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import { useSimulationStore } from '../store/simulationStore'

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
    const { lookAndFeel, running, isPaused, speed } = useSimulationStore()
    const isGamified = lookAndFeel === 'GAMING'
    const color = predatorColors[type] || predatorColors.SPIDER
    const scale = predatorScales[type] || 1.5
    const isSimActive = Boolean(running && !isPaused && (typeof speed === 'number' ? speed > 0 : true))
    const animTimeRef = useRef(0)

    // Animation based on state (frozen when simulation is paused or stopped)
    useFrame((_, delta) => {
        if (!meshRef.current) return

        if (isSimActive) {
            animTimeRef.current += delta * (speed || 1.0)
        }
        const t = animTimeRef.current

        if (state === 'CHASING') {
            // Bobbing animation
            meshRef.current.position.y = position[1] + Math.sin(t * 10) * 0.2
        } else if (state === 'ATTACKING') {
            // Shake animation
            meshRef.current.rotation.z = Math.sin(t * 20) * 0.1
        }
    })

    return (
        <mesh ref={meshRef} position={position} scale={scale} castShadow>
            {isGamified ? (
                <boxGeometry args={[0.7, 0.4, 0.9]} />
            ) : type === 'SPIDER' ? (
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
