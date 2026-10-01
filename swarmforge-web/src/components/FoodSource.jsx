import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'

const foodColors = {
    SUGAR: '#ffffff',
    PROTEIN: '#ff6666',
    SEEDS: '#aa8844',
    FUNGUS: '#88aa88',
}

export default function FoodSource({ position, quantity = 100, type = 'SUGAR' }) {
    const meshRef = useRef()
    const { lookAndFeel, running, isPaused, speed } = useSimulationStore()
    const isGamified = lookAndFeel === 'GAMING'
    const color = foodColors[type] || foodColors.SUGAR
    const scale = Math.max(0.5, Math.min(3, quantity / 100))
    const isSimActive = Boolean(running && !isPaused && (typeof speed === 'number' ? speed > 0 : true))
    const pulseTimeRef = useRef(0)

    // Gentle pulsing animation (frozen when simulation is paused or stopped)
    useFrame((state, delta) => {
        if (meshRef.current) {
            if (isSimActive) {
                pulseTimeRef.current += delta * (speed || 1.0)
            }
            meshRef.current.scale.setScalar(scale + Math.sin(pulseTimeRef.current * 2) * 0.1)
        }
    })

    return (
        <mesh ref={meshRef} position={position} castShadow>
            {isGamified ? (
                <boxGeometry args={[0.7, 0.7, 0.7]} />
            ) : (
                <dodecahedronGeometry args={[0.5, 0]} />
            )}
            <meshStandardMaterial
                color={color}
                emissive={color}
                emissiveIntensity={0.3}
                roughness={0.6}
            />
        </mesh>
    )
}
