import { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'

export default function WeatherRenderer() {
    const { environment } = useSimulationStore()
    const pointsRef = useRef()
    const count = 2000 // Max particles

    // Determine precipitation type
    const isSnow = environment.temperature <= 0
    const intensity = environment.rainIntensity || 0

    // Do not render if clear weather
    if (intensity < 0.1) return null

    const particles = useMemo(() => {
        const positions = new Float32Array(count * 3)
        const velocities = new Float32Array(count * 3) // Store varied fall speeds

        for (let i = 0; i < count; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 100
            positions[i * 3 + 1] = Math.random() * 50
            positions[i * 3 + 2] = (Math.random() - 0.5) * 100

            velocities[i * 3] = 0 // x drift
            velocities[i * 3 + 1] = -0.5 - Math.random() * 0.5 // fall speed
            velocities[i * 3 + 2] = 0 // z drift
        }

        return { positions, velocities }
    }, [])

    useFrame((state, delta) => {
        if (!pointsRef.current) return

        const positions = pointsRef.current.geometry.attributes.position.array

        // Active particle count based on intensity (0-10mm)
        // 10mm rain = max particles
        const activeRatio = Math.min(1.0, intensity / 5.0)

        const windX = (environment.windSpeed || 0) * 0.1
        const fallSpeedMultiplier = isSnow ? 0.3 : 1.0 // Snow falls likely slower

        for (let i = 0; i < count; i++) {
            // Apply gravity and wind
            positions[i * 3] += windX * delta
            positions[i * 3 + 1] += particles.velocities[i * 3 + 1] * fallSpeedMultiplier * delta * 20

            // Reset if below ground
            if (positions[i * 3 + 1] < 0) {
                positions[i * 3 + 1] = 40 + Math.random() * 10 // Reset height
                positions[i * 3] = (Math.random() - 0.5) * 100 // Reset X
                positions[i * 3 + 2] = (Math.random() - 0.5) * 100 // Reset Z
            }

            // Wrap X/Z to keep cloud centered roughly
            if (positions[i * 3] > 50) positions[i * 3] -= 100
            if (positions[i * 3] < -50) positions[i * 3] += 100
            if (positions[i * 3 + 2] > 50) positions[i * 3 + 2] -= 100
            if (positions[i * 3 + 2] < -50) positions[i * 3 + 2] += 100
        }

        pointsRef.current.geometry.attributes.position.needsUpdate = true
    })

    return (
        <points ref={pointsRef}>
            <bufferGeometry>
                <bufferAttribute
                    attach="attributes-position"
                    count={count}
                    array={particles.positions}
                    itemSize={3}
                />
            </bufferGeometry>
            <pointsMaterial
                size={isSnow ? 0.4 : 0.2}
                color={isSnow ? '#ffffff' : '#88ccff'}
                transparent
                opacity={0.6}
                sizeAttenuation={true}
            />
        </points>
    )
}
