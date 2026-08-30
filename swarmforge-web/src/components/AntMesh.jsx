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

const familyColors = {
    TERMITE: {
        QUEEN: '#fef08a',
        SOLDIER: '#ea580c',
        WORKER: '#fef3c7',
        MALE: '#e2e8f0'
    },
    BEE: {
        QUEEN: '#f59e0b',
        SOLDIER: '#d97706',
        WORKER: '#fbbf24',
        MALE: '#334155'
    },
    WASP: {
        QUEEN: '#facc15',
        SOLDIER: '#eab308',
        WORKER: '#fef08a',
        MALE: '#1e293b'
    },
    APHID: {
        QUEEN: '#a3e635',
        SOLDIER: '#65a30d',
        WORKER: '#84cc16',
        MALE: '#4d7c0f'
    },
    BEETLE: {
        QUEEN: '#78350f',
        SOLDIER: '#451a03',
        WORKER: '#581c87',
        MALE: '#1e1b4b'
    },
    THRIPS: {
        QUEEN: '#d97706',
        SOLDIER: '#b45309',
        WORKER: '#f59e0b',
        MALE: '#92400e'
    }
}

/**
 * Normalizes insect type / family strings to canonical InsectOrder enums.
 */
function resolveInsectOrder(insectOrder) {
    if (!insectOrder) return 'ANT'
    const str = String(insectOrder).toUpperCase()
    if (str.includes('BEE') || str.includes('APIDAE')) return 'BEE'
    if (str.includes('WASP') || str.includes('VESPIDAE')) return 'WASP'
    if (str.includes('TERMITE') || str.includes('ISOPTERA') || str.includes('TERMITOIDAE')) return 'TERMITE'
    if (str.includes('APHID') || str.includes('APHIDIDAE')) return 'APHID'
    if (str.includes('THRIPS') || str.includes('THYSANOPTERA')) return 'THRIPS'
    if (str.includes('BEETLE') || str.includes('COLEOPTERA')) return 'BEETLE'
    return 'ANT'
}

export default function AntMesh({ position, caste = 'WORKER', scale = 1, diseaseState = 'HEALTHY', insectOrder = 'ANT' }) {
    const { lookAndFeel } = useSimulationStore()
    const order = useMemo(() => resolveInsectOrder(insectOrder), [insectOrder])

    // Determine base color based on family and caste
    const baseColor = useMemo(() => {
        if (familyColors[order]) {
            return familyColors[order][caste] || familyColors[order].WORKER
        }
        return casteColors[caste] || casteColors.WORKER
    }, [order, caste])

    const isInfected = diseaseState === 'INFECTED' || diseaseState === 'CONTAGIOUS'

    // Realistically adapted infected chitin colors
    const antColor = useMemo(() => {
        if (!isInfected) return baseColor
        if (lookAndFeel === 'REALISTIC') return '#4a3f35' // Decayed dull mold exoskeleton
        if (lookAndFeel === 'GAMING') return '#a3e635'    // Toxic neon green
        return '#65a30d'                                 // Scientific chart green
    }, [baseColor, isInfected, lookAndFeel])

    return (
        <group position={position}>
            {/* SCIENTIFIC MODE: High-contrast data glowing aura */}
            {isInfected && lookAndFeel === 'SCIENTIFIC' && (
                <mesh position={[0, 0, 0]}>
                    <sphereGeometry args={[0.65 * scale, 12, 12]} />
                    <meshBasicMaterial color="#38bdf8" transparent opacity={0.6} wireframe />
                </mesh>
            )}

            {/* REALISTIC MODE: Fungal spore tendrils sprouting on exoskeleton */}
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

            {/* ── Dynamic Species Morphology rendering based on InsectOrder ── */}
            {order === 'TERMITE' ? (
                <group>
                    {/* Head (Soldiers get large orange head & mandibles) */}
                    <mesh position={[0, 0, 0.3 * scale]}>
                        <boxGeometry args={[0.25 * scale * (caste === 'SOLDIER' ? 1.4 : 1), 0.22 * scale, 0.26 * scale]} />
                        <meshStandardMaterial color={caste === 'SOLDIER' ? '#ea580c' : antColor} roughness={0.7} />
                    </mesh>
                    {caste === 'SOLDIER' && (
                        <group position={[0, 0, 0.48 * scale]}>
                            <mesh position={[0.08 * scale, 0, 0]}>
                                <boxGeometry args={[0.03 * scale, 0.03 * scale, 0.14 * scale]} />
                                <meshStandardMaterial color="#451a03" />
                            </mesh>
                            <mesh position={[-0.08 * scale, 0, 0]}>
                                <boxGeometry args={[0.03 * scale, 0.03 * scale, 0.14 * scale]} />
                                <meshStandardMaterial color="#451a03" />
                            </mesh>
                        </group>
                    )}
                    {/* Soft Thorax */}
                    <mesh position={[0, 0, 0]}>
                        <cylinderGeometry args={[0.12 * scale, 0.14 * scale, 0.25 * scale, 8]} rotation={[Math.PI / 2, 0, 0]} />
                        <meshStandardMaterial color={antColor} roughness={0.8} />
                    </mesh>
                    {/* Abdomen (Queens are physogastric enlarged) */}
                    <mesh position={[0, 0, -0.45 * scale * (caste === 'QUEEN' ? 1.8 : 1)]}>
                        <sphereGeometry args={[0.24 * scale * (caste === 'QUEEN' ? 1.5 : 1), 10, 8]} />
                        <meshStandardMaterial color={antColor} roughness={0.9} />
                    </mesh>
                </group>
            ) : order === 'BEE' ? (
                <group>
                    {/* Head */}
                    <mesh position={[0, 0, 0.28 * scale]}>
                        <sphereGeometry args={[0.16 * scale, 8, 8]} />
                        <meshStandardMaterial color="#1e293b" roughness={0.6} />
                    </mesh>
                    {/* Plump Fuzzy Thorax */}
                    <mesh position={[0, 0, 0]}>
                        <sphereGeometry args={[0.22 * scale, 10, 8]} />
                        <meshStandardMaterial color="#b45309" roughness={0.9} />
                    </mesh>
                    {/* Striped Ovate Abdomen */}
                    <mesh position={[0, 0, -0.38 * scale]}>
                        <cylinderGeometry args={[0.20 * scale, 0.24 * scale, 0.42 * scale, 10]} rotation={[Math.PI / 2, 0, 0]} />
                        <meshStandardMaterial color={antColor} roughness={0.5} />
                    </mesh>
                    {/* Dual Translucent Wings */}
                    <group position={[0, 0.2 * scale, -0.05 * scale]}>
                        <mesh position={[0.28 * scale, 0, 0]} rotation={[0, 0.2, 0.1]}>
                            <boxGeometry args={[0.35 * scale, 0.01, 0.16 * scale]} />
                            <meshStandardMaterial color="#e0f2fe" transparent opacity={0.6} />
                        </mesh>
                        <mesh position={[-0.28 * scale, 0, 0]} rotation={[0, -0.2, -0.1]}>
                            <boxGeometry args={[0.35 * scale, 0.01, 0.16 * scale]} />
                            <meshStandardMaterial color="#e0f2fe" transparent opacity={0.6} />
                        </mesh>
                    </group>
                </group>
            ) : order === 'WASP' ? (
                <group>
                    {/* Head */}
                    <mesh position={[0, 0, 0.3 * scale]}>
                        <sphereGeometry args={[0.15 * scale, 8, 8]} />
                        <meshStandardMaterial color="#0f172a" roughness={0.4} />
                    </mesh>
                    {/* Thorax */}
                    <mesh position={[0, 0, 0.05 * scale]}>
                        <sphereGeometry args={[0.18 * scale, 8, 8]} />
                        <meshStandardMaterial color={antColor} roughness={0.4} />
                    </mesh>
                    {/* Extremely Thin Petiole Waist */}
                    <mesh position={[0, 0, -0.18 * scale]}>
                        <cylinderGeometry args={[0.04 * scale, 0.04 * scale, 0.15 * scale, 6]} rotation={[Math.PI / 2, 0, 0]} />
                        <meshStandardMaterial color="#0f172a" />
                    </mesh>
                    {/* Pointed Striped Abdomen */}
                    <mesh position={[0, 0, -0.45 * scale]}>
                        <coneGeometry args={[0.20 * scale, 0.45 * scale, 8]} rotation={[-Math.PI / 2, 0, 0]} />
                        <meshStandardMaterial color={antColor} roughness={0.3} />
                    </mesh>
                    {/* Elongated Wings */}
                    <group position={[0, 0.18 * scale, -0.05 * scale]}>
                        <mesh position={[0.30 * scale, 0, 0]} rotation={[0, 0.1, 0.15]}>
                            <boxGeometry args={[0.40 * scale, 0.01, 0.12 * scale]} />
                            <meshStandardMaterial color="#f0f9ff" transparent opacity={0.65} />
                        </mesh>
                        <mesh position={[-0.30 * scale, 0, 0]} rotation={[0, -0.1, -0.15]}>
                            <boxGeometry args={[0.40 * scale, 0.01, 0.12 * scale]} />
                            <meshStandardMaterial color="#f0f9ff" transparent opacity={0.65} />
                        </mesh>
                    </group>
                </group>
            ) : order === 'APHID' ? (
                <group>
                    {/* Small Head */}
                    <mesh position={[0, 0, 0.22 * scale]}>
                        <sphereGeometry args={[0.10 * scale, 6, 6]} />
                        <meshStandardMaterial color={antColor} roughness={0.8} />
                    </mesh>
                    {/* Compact Thorax */}
                    <mesh position={[0, 0, 0.08 * scale]}>
                        <sphereGeometry args={[0.13 * scale, 6, 6]} />
                        <meshStandardMaterial color={antColor} roughness={0.8} />
                    </mesh>
                    {/* Pear-shaped Abdomen */}
                    <mesh position={[0, 0, -0.22 * scale]}>
                        <sphereGeometry args={[0.26 * scale, 10, 10]} />
                        <meshStandardMaterial color={antColor} roughness={0.7} />
                    </mesh>
                    {/* Rear Cornicles / Siphunculi tubes */}
                    <group position={[0, 0.2 * scale, -0.36 * scale]}>
                        <mesh position={[0.1 * scale, 0, 0]} rotation={[0.4, 0, 0]}>
                            <cylinderGeometry args={[0.02 * scale, 0.02 * scale, 0.15 * scale, 6]} />
                            <meshStandardMaterial color="#4d7c0f" />
                        </mesh>
                        <mesh position={[-0.1 * scale, 0, 0]} rotation={[0.4, 0, 0]}>
                            <cylinderGeometry args={[0.02 * scale, 0.02 * scale, 0.15 * scale, 6]} />
                            <meshStandardMaterial color="#4d7c0f" />
                        </mesh>
                    </group>
                </group>
            ) : order === 'BEETLE' ? (
                <group>
                    {/* Head */}
                    <mesh position={[0, 0, 0.25 * scale]}>
                        <boxGeometry args={[0.16 * scale, 0.12 * scale, 0.14 * scale]} />
                        <meshStandardMaterial color="#1e1b4b" roughness={0.3} />
                    </mesh>
                    {/* Broad Thoracic Pronotum Shield */}
                    <mesh position={[0, 0, 0.08 * scale]}>
                        <boxGeometry args={[0.26 * scale, 0.16 * scale, 0.18 * scale]} />
                        <meshStandardMaterial color={antColor} roughness={0.2} metalness={0.4} />
                    </mesh>
                    {/* Hard Chitin Elytra Wing Covers */}
                    <mesh position={[0, 0, -0.28 * scale]}>
                        <sphereGeometry args={[0.28 * scale, 10, 8]} />
                        <meshStandardMaterial color={antColor} roughness={0.15} metalness={0.5} />
                    </mesh>
                </group>
            ) : order === 'THRIPS' ? (
                <group>
                    {/* Small Head */}
                    <mesh position={[0, 0, 0.26 * scale]}>
                        <boxGeometry args={[0.08 * scale, 0.08 * scale, 0.10 * scale]} />
                        <meshStandardMaterial color={antColor} roughness={0.6} />
                    </mesh>
                    {/* Narrow Thorax */}
                    <mesh position={[0, 0, 0.12 * scale]}>
                        <boxGeometry args={[0.10 * scale, 0.08 * scale, 0.16 * scale]} />
                        <meshStandardMaterial color={antColor} roughness={0.6} />
                    </mesh>
                    {/* Elongated Narrow Abdomen */}
                    <mesh position={[0, 0, -0.28 * scale]}>
                        <cylinderGeometry args={[0.09 * scale, 0.09 * scale, 0.50 * scale, 6]} rotation={[Math.PI / 2, 0, 0]} />
                        <meshStandardMaterial color={antColor} roughness={0.6} />
                    </mesh>
                </group>
            ) : (
                /* Standard Ant / Formicidae morphology (Default Fallback) */
                <group>
                    {/* Head */}
                    <mesh position={[0, 0, 0.3 * scale]}>
                        <sphereGeometry args={[0.15 * scale, 8, 6]} />
                        <meshStandardMaterial color={antColor} roughness={lookAndFeel === 'REALISTIC' ? 0.8 : 0.4} />
                    </mesh>
                    {/* Thorax */}
                    <mesh position={[0, 0, 0]}>
                        <sphereGeometry args={[0.12 * scale, 8, 6]} />
                        <meshStandardMaterial color={antColor} roughness={lookAndFeel === 'REALISTIC' ? 0.8 : 0.4} />
                    </mesh>
                    {/* Abdomen */}
                    <mesh position={[0, 0, -0.35 * scale]}>
                        <sphereGeometry args={[0.2 * scale, 8, 6]} />
                        <meshStandardMaterial color={antColor} roughness={lookAndFeel === 'REALISTIC' ? 0.8 : 0.4} />
                    </mesh>
                </group>
            )}
        </group>
    )
}
