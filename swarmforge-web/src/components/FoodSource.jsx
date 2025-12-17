import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'

const foodColors = {
    SUGAR: '#ffffff',
    PROTEIN: '#ff6666',
    SEEDS: '#aa8844',
    FUNGUS: '#88aa88',
}

export default function FoodSource({ position, quantity = 100, type = 'SUGAR' }) {
    const meshRef = useRef()
    const color = foodColors[type] || foodColors.SUGAR
    const scale = Math.max(0.5, Math.min(3, quantity / 100))

    // Gentle pulsing animation
    useFrame((state) => {
        if (meshRef.current) {
            meshRef.current.scale.setScalar(scale + Math.sin(state.clock.elapsedTime * 2) * 0.1)
        }
    })

    return (
        <mesh ref={meshRef} position={position}>
            <dodecahedronGeometry args={[0.5, 0]} />
            <meshStandardMaterial
                color={color}
                emissive={color}
                emissiveIntensity={0.3}
                roughness={0.6}
            />
        </mesh>
    )
}
