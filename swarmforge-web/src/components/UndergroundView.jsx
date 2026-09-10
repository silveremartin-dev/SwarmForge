import React, { useMemo, useState } from 'react'
import * as THREE from 'three'
import { Html } from '@react-three/drei'
import { useSimulationStore } from '../store/simulationStore'
import { getTerrainHeight } from '../utils/terrainUtils'

const CHAMBER_COLORS = {
    QUEEN_QUARTERS: '#a855f7', // Royal Purple
    NURSERY: '#f43f5e',       // Soft Pink / Rose
    FOOD_STORAGE: '#eab308',  // Amber / Gold
    WASTE_DUMP: '#64748b',    // Slate / Dark
    ENTRANCE: '#38bdf8'       // Cyan / Yellow
}

const CHAMBER_ICONS = {
    QUEEN_QUARTERS: '👑',
    NURSERY: '🍼',
    FOOD_STORAGE: '🍯',
    WASTE_DUMP: '☣️',
    ENTRANCE: '🛡️'
}

function Tunnel({ start, end, terrainConfig }) {
    const { lookAndFeel } = useSimulationStore()
    const isGamified = lookAndFeel === 'GAMING'

    const { position, rotation, length } = useMemo(() => {
        const startGroundY = getTerrainHeight(start.x, start.z, terrainConfig)
        const endGroundY = getTerrainHeight(end.x, end.z, terrainConfig)
        const startVec = new THREE.Vector3(start.x, startGroundY + start.y, start.z)
        const endVec = new THREE.Vector3(end.x, endGroundY + end.y, end.z)

        const length = startVec.distanceTo(endVec)
        const position = startVec.clone().add(endVec).multiplyScalar(0.5)

        const direction = endVec.clone().sub(startVec).normalize()
        const quaternion = new THREE.Quaternion().setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction)
        const rotation = new THREE.Euler().setFromQuaternion(quaternion)

        return { position, rotation, length }
    }, [start, end, terrainConfig])

    return (
        <mesh position={position} rotation={rotation}>
            {isGamified ? (
                <boxGeometry args={[0.5, length, 0.5]} />
            ) : (
                <cylinderGeometry args={[0.3, 0.3, length, 10]} />
            )}
            <meshStandardMaterial color="#451a03" roughness={0.9} transparent opacity={0.85} />
        </mesh>
    )
}

function ChamberMesh({ chamber, isSelected, onClick, terrainConfig }) {
    const [hovered, setHovered] = useState(false)
    const { showChamberOverlay, lookAndFeel } = useSimulationStore()
    const isGamified = lookAndFeel === 'GAMING'
    const color = CHAMBER_COLORS[chamber.type] || '#38bdf8'
    const icon = CHAMBER_ICONS[chamber.type] || '🏛️'
    const radius = chamber.radius || 1.2

    const groundY = getTerrainHeight(chamber.position.x, chamber.position.z, terrainConfig)
    const worldY = groundY + chamber.position.y

    return (
        <group position={[chamber.position.x, worldY, chamber.position.z]}>
            {/* Main Chamber Shape (Voxel Cube in Gaming vs Sphere in Realistic) */}
            <mesh
                onClick={(e) => {
                    e.stopPropagation()
                    onClick(chamber)
                }}
                onPointerOver={(e) => {
                    e.stopPropagation()
                    setHovered(true)
                    document.body.style.cursor = 'pointer'
                }}
                onPointerOut={(e) => {
                    e.stopPropagation()
                    setHovered(false)
                    document.body.style.cursor = 'auto'
                }}
            >
                {isGamified ? (
                    <boxGeometry args={[radius * 1.6 * (hovered ? 1.08 : 1.0), radius * 1.4 * (hovered ? 1.08 : 1.0), radius * 1.6 * (hovered ? 1.08 : 1.0)]} />
                ) : (
                    <sphereGeometry args={[radius * (hovered ? 1.08 : 1.0), 16, 16]} />
                )}
                <meshStandardMaterial
                    color={color}
                    roughness={0.6}
                    metalness={0.2}
                    transparent
                    opacity={isSelected ? 0.95 : (hovered ? 0.85 : 0.70)}
                    emissive={isSelected ? color : (hovered ? '#ffffff' : '#000000')}
                    emissiveIntensity={isSelected ? 0.4 : (hovered ? 0.2 : 0)}
                />
            </mesh>

            {/* Selection Pulsing Ring */}
            {isSelected && (
                <mesh rotation={[-Math.PI / 2, 0, 0]}>
                    <ringGeometry args={[radius * 1.2, radius * 1.4, 24]} />
                    <meshBasicMaterial color="#38bdf8" side={THREE.DoubleSide} transparent opacity={0.85} />
                </mesh>
            )}

            {/* 3D Floating Chamber Tag */}
            {showChamberOverlay && (
                <Html
                    position={[0, radius + 0.6, 0]}
                    center
                    distanceFactor={45}
                    style={{ pointerEvents: 'none' }}
                >
                    <div
                        style={{
                            background: isSelected ? 'rgba(2, 132, 199, 0.95)' : 'rgba(15, 23, 42, 0.85)',
                            border: isSelected ? '1px solid #38bdf8' : '1px solid rgba(255,255,255,0.2)',
                            borderRadius: 6,
                            padding: '3px 8px',
                            color: '#fff',
                            fontSize: 11,
                            fontWeight: 700,
                            whiteSpace: 'nowrap',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 4,
                            boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
                            backdropFilter: 'blur(6px)',
                            transform: hovered ? 'scale(1.1)' : 'scale(1.0)',
                            transition: 'all 0.15s ease'
                        }}
                    >
                        <span>{icon}</span>
                        <span>{chamber.name.split('(')[0].trim()}</span>
                        <span style={{ fontSize: 9, opacity: 0.8, color: '#f59e0b' }}>
                            ({chamber.occupants || 0}/{chamber.capacity || 50})
                        </span>
                    </div>
                </Html>
            )}
        </group>
    )
}

export default function UndergroundView() {
    const { nests, showChamberOverlay, selectedChamber, setSelectedChamber, terrainConfig } = useSimulationStore()

    if (!showChamberOverlay || !nests || nests.length === 0) return null

    return (
        <group>
            {nests.map(nest => (
                <group key={nest.id}>
                    {/* Render Chambers */}
                    {nest.chambers && nest.chambers.map(chamber => (
                        <ChamberMesh
                            key={chamber.id}
                            chamber={chamber}
                            isSelected={selectedChamber?.id === chamber.id}
                            onClick={setSelectedChamber}
                            terrainConfig={terrainConfig}
                        />
                    ))}

                    {/* Render Tunnels */}
                    {nest.tunnels && nest.tunnels.map((tunnel, idx) => {
                        const startChamber = nest.chambers?.find(c => c.id === tunnel.startChamberId)
                        const endChamber = nest.chambers?.find(c => c.id === tunnel.endChamberId)

                        if (startChamber && endChamber) {
                            return (
                                <Tunnel
                                    key={`${nest.id}-tunnel-${idx}`}
                                    start={startChamber.position}
                                    end={endChamber.position}
                                    terrainConfig={terrainConfig}
                                />
                            )
                        }
                        return null
                    })}
                </group>
            ))}
        </group>
    )
}
