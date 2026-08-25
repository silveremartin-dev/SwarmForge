import { useMemo } from 'react'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'

const casteColors = {
    WORKER: '#8b4513',
    SOLDIER: '#ff4444',
    QUEEN: '#ffd700',
    MALE: '#4444ff',
    NURSE: '#44aa44',
}

export default function AntMesh({ position, caste = 'WORKER', scale = 1, diseaseState = 'HEALTHY' }) {
    const { lookAndFeel } = useSimulationStore()
    const color = casteColors[caste] || casteColors.WORKER
    const isInfected = diseaseState === 'INFECTED' || diseaseState === 'CONTAGIOUS'

    // Realistically adapted infected chitin colors (dull decayed brown/gray mold spots)
    const antColor = useMemo(() => {
        if (!isInfected) return color
        if (lookAndFeel === 'REALISTIC') return '#4a3f35' // Decayed dull mold exoskeleton
        if (lookAndFeel === 'GAMING') return '#a3e635'    // Toxic neon green
        return '#65a30d'                                 // Scientific chart green
    }, [color, isInfected, lookAndFeel])

    return (
        <group position={position}>
            {/* SCIENTIFIC MODE: High-contrast data glowing aura */}
            {isInfected && lookAndFeel === 'SCIENTIFIC' && (
                <mesh position={[0, 0, 0]}>
                    <sphereGeometry args={[0.65 * scale, 12, 12]} />
                    <meshBasicMaterial color="#38bdf8" transparent opacity={0.6} wireframe />
                </mesh>
            )}

            {/* REALISTIC MODE: Fungal spore tendrils / hyphae sprouting on exoskeleton (no glowing scifi halo) */}
            {isInfected && lookAndFeel === 'REALISTIC' && (
                <group position={[0, 0.2 * scale, 0]}>
                    <mesh position={[0.08 * scale, 0.1 * scale, 0]}>
                        <cylinderGeometry args={[0.02 * scale, 0.04 * scale, 0.25 * scale, 6]} />
                        <meshStandardMaterial color="#d97706" roughness={0.9} />
                    </mesh>
                    <mesh position={[-0.08 * scale, 0.12 * scale, -0.1 * scale]}>
                        <cylinderGeometry args={[0.015 * scale, 0.03 * scale, 0.2 * scale, 6]} />
                        <meshStandardMaterial color="#d97706" roughness={0.9} />
                    </mesh>
                </group>
            )}

            {/* GAMING MODE: Retro status effect biohazard block indicator */}
            {isInfected && lookAndFeel === 'GAMING' && (
                <mesh position={[0, 0.8 * scale, 0]}>
                    <boxGeometry args={[0.25 * scale, 0.25 * scale, 0.25 * scale]} />
                    <meshBasicMaterial color="#a3e635" wireframe={false} />
                </mesh>
            )}

            {/* Head */}
            <mesh position={[0, 0, 0.3 * scale]}>
                <sphereGeometry args={[0.15 * scale, 8, 6]} />
                <meshStandardMaterial color={antColor} roughness={lookAndFeel === 'REALISTIC' ? 0.8 : 0.4} />
            </mesh>
            {/* Thorax */}
            <mesh>
                <sphereGeometry args={[0.12 * scale, 8, 6]} />
                <meshStandardMaterial color={antColor} roughness={lookAndFeel === 'REALISTIC' ? 0.8 : 0.4} />
            </mesh>
            {/* Abdomen */}
            <mesh position={[0, 0, -0.35 * scale]}>
                <sphereGeometry args={[0.2 * scale, 8, 6]} />
                <meshStandardMaterial color={antColor} roughness={lookAndFeel === 'REALISTIC' ? 0.8 : 0.4} />
            </mesh>
        </group>
    )
}
