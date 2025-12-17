import { useRef, useMemo, useEffect } from 'react'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'

export default function PheromoneCloud() {
    const { tick } = useSimulationStore()
    // In a real implementation, we would subscribe to a 'cells' update or similar
    // For now, we simulate visualisation or assume store has a 'pheromones' array
    // Since simulationStore currently only has 'colonies', 'ants', 'food',
    // we might need to mock this or adding a way to receive grid data.

    // Placeholder: Let's visualize where ants are as a "trail" if we don't have grid data yet,
    // or just render a dummy cloud to show the feature.
    // Ideally, the server sends `CellDelta` which contains pheromones.

    // Let's assume we want to visualize a simple static cloud for "Home" pheromones near center 
    // to demonstrate the UI capability.

    const particles = useMemo(() => {
        // Mocking trails for visualization until server data is hooked up
        const count = 2000
        const positions = new Float32Array(count * 3)
        const colors = new Float32Array(count * 3)

        for (let i = 0; i < count; i++) {
            // Home Trail (Blue) - Radial lines from center
            const isHomeTrail = i < count / 2

            const angle = Math.random() * Math.PI * 2
            const dist = Math.random() * 40
            const x = 50 + Math.cos(angle) * dist
            const z = 50 + Math.sin(angle) * dist
            const y = 0.5

            positions[i * 3] = x
            positions[i * 3 + 1] = y
            positions[i * 3 + 2] = z

            if (isHomeTrail) {
                // Blue
                colors[i * 3] = 0.1
                colors[i * 3 + 1] = 0.2
                colors[i * 3 + 2] = 1.0
            } else {
                // Red (Food) - Scattered
                colors[i * 3] = 1.0
                colors[i * 3 + 1] = 0.1
                colors[i * 3 + 2] = 0.1
            }
        }

        return { positions, colors }
    }, [])

    return (
        <points ref={pointsRef}>
            <bufferGeometry>
                <bufferAttribute
                    attach="attributes-position"
                    count={count}
                    array={particles.positions}
                    itemSize={3}
                />
                <bufferAttribute
                    attach="attributes-color"
                    count={count}
                    array={particles.colors}
                    itemSize={3}
                />
            </bufferGeometry>
            <pointsMaterial
                size={0.2}
                vertexColors
                transparent
                opacity={0.6}
                sizeAttenuation={true}
                depthWrite={false}
            />
        </points>
    )
}
