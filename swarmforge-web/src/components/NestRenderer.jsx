import React, { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'

// Individual Nest 3D Mesh Component supporting 6 types and Phantom/Ghost overlay
function SingleNest({ nest, isGhost = false }) {
    const groupRef = useRef()
    const phantomMeshRef = useRef()

    // Pulse effect for Phantom / Ghost rendering
    useFrame((state) => {
        if ((nest.isPhantom || isGhost) && phantomMeshRef.current) {
            const time = state.clock.getElapsedTime()
            phantomMeshRef.current.material.opacity = 0.35 + Math.sin(time * 3) * 0.25
        }
    })

    const scale = nest.scale || 1.0
    const posX = nest.x || 50
    const posY = nest.z || 0 // 3D Y coordinate offset
    const posZ = nest.y || 50

    const isPhantomMode = nest.isPhantom || isGhost

    // Render mesh based on Nest Type
    const renderNestGeometry = () => {
        switch (nest.type) {
            case 'PINE_NEEDLES':
                // Dôme d'épines de pin (Pine needle mound nest)
                return (
                    <group>
                        {/* Main cone mound */}
                        <mesh position={[0, 0.75 * scale, 0]} castShadow receiveShadow>
                            <coneGeometry args={[2.2 * scale, 1.5 * scale, 16]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#4a3319'}
                                roughness={0.9}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                                emissive={isPhantomMode ? '#0284c7' : '#000000'}
                                emissiveIntensity={isPhantomMode ? 0.6 : 0}
                            />
                        </mesh>
                        {/* Internal Chamber Cutaway Visualisation (Visible in Phantom/Wireframe mode) */}
                        {isPhantomMode && (
                            <group position={[0, 0.5 * scale, 0]}>
                                {/* Queen Chamber */}
                                <mesh position={[0, -0.2 * scale, 0]}>
                                    <sphereGeometry args={[0.5 * scale, 12, 12]} />
                                    <meshBasicMaterial color="#ffd700" transparent opacity={0.8} wireframe />
                                </mesh>
                                {/* Nursery Chambers */}
                                <mesh position={[0.6 * scale, 0.2 * scale, 0.4 * scale]}>
                                    <sphereGeometry args={[0.35 * scale, 8, 8]} />
                                    <meshBasicMaterial color="#4ade80" transparent opacity={0.8} wireframe />
                                </mesh>
                                {/* Food Granary */}
                                <mesh position={[-0.6 * scale, 0.1 * scale, -0.3 * scale]}>
                                    <sphereGeometry args={[0.4 * scale, 8, 8]} />
                                    <meshBasicMaterial color="#fbbf24" transparent opacity={0.8} wireframe />
                                </mesh>
                            </group>
                        )}
                        {/* Pine cone details / twig debris */}
                        {!isPhantomMode && (
                            <>
                                <mesh position={[0.6 * scale, 0.4 * scale, 0.8 * scale]} rotation={[0.4, 0.2, 0.5]}>
                                    <coneGeometry args={[0.3 * scale, 0.6 * scale, 8]} />
                                    <meshStandardMaterial color="#2d1d0f" roughness={0.95} />
                                </mesh>
                                <mesh position={[-0.8 * scale, 0.3 * scale, -0.6 * scale]} rotation={[-0.3, 0.5, 0.2]}>
                                    <coneGeometry args={[0.25 * scale, 0.5 * scale, 8]} />
                                    <meshStandardMaterial color="#3a2717" roughness={0.95} />
                                </mesh>
                            </>
                        )}
                    </group>
                )

            case 'TERMITE_MOUND':
                // Termitière verticale (Vertical cathedral spire)
                return (
                    <group>
                        {/* Tall ribbed cathedral spire */}
                        <mesh position={[0, 2.5 * scale, 0]} castShadow receiveShadow>
                            <cylinderGeometry args={[0.6 * scale, 1.8 * scale, 5.0 * scale, 12]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#9a5323'}
                                roughness={0.95}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                                emissive={isPhantomMode ? '#0284c7' : '#000000'}
                                emissiveIntensity={isPhantomMode ? 0.6 : 0}
                            />
                        </mesh>
                        {/* Internal Ventilation Shafts & Royal Cell (Phantom Cutaway View) */}
                        {isPhantomMode && (
                            <group>
                                <mesh position={[0, 1.2 * scale, 0]}>
                                    <cylinderGeometry args={[0.2 * scale, 0.3 * scale, 2.8 * scale, 8]} />
                                    <meshBasicMaterial color="#f97316" transparent opacity={0.8} wireframe />
                                </mesh>
                                <mesh position={[0, 0.6 * scale, 0]}>
                                    <boxGeometry args={[0.8 * scale, 0.4 * scale, 0.8 * scale]} />
                                    <meshBasicMaterial color="#ffd700" transparent opacity={0.85} wireframe />
                                </mesh>
                            </group>
                        )}
                        {/* Top ventilation chimney spires */}
                        <mesh position={[0.4 * scale, 4.5 * scale, 0.2 * scale]} castShadow>
                            <cylinderGeometry args={[0.25 * scale, 0.4 * scale, 1.5 * scale, 8]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#7c3a17'} wireframe={isPhantomMode} />
                        </mesh>
                        <mesh position={[-0.3 * scale, 4.2 * scale, -0.3 * scale]} castShadow>
                            <cylinderGeometry args={[0.2 * scale, 0.35 * scale, 1.2 * scale, 8]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#7c3a17'} wireframe={isPhantomMode} />
                        </mesh>
                    </group>
                )

            case 'WASP_BRANCH':
                // Guêpier suspendu sur branche d'arbre (Wasp nest on tree branch)
                return (
                    <group>
                        {/* Tree Branch extending horizontally */}
                        <mesh position={[0, 4.5 * scale, 0]} rotation={[0, 0, 0.1]} castShadow>
                            <cylinderGeometry args={[0.25 * scale, 0.35 * scale, 6.0 * scale, 8]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#3f2b1d'} roughness={0.9} wireframe={isPhantomMode} />
                        </mesh>
                        {/* Leaves foliage on branch */}
                        {!isPhantomMode && (
                            <mesh position={[1.8 * scale, 4.7 * scale, 0.3 * scale]}>
                                <sphereGeometry args={[0.9 * scale, 8, 8]} />
                                <meshStandardMaterial color="#15803d" roughness={0.7} />
                            </mesh>
                        )}
                        {/* Hanging Paper Wasp Hexagonal Nest */}
                        <mesh position={[0, 3.2 * scale, 0]} castShadow>
                            <dodecahedronGeometry args={[1.1 * scale, 1]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#a89f91'}
                                roughness={0.8}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                                emissive={isPhantomMode ? '#0284c7' : '#000000'}
                                emissiveIntensity={isPhantomMode ? 0.6 : 0}
                            />
                        </mesh>
                        {/* Internal Comb Tiers (Phantom Cutaway View) */}
                        {isPhantomMode && (
                            <group position={[0, 3.2 * scale, 0]}>
                                <mesh position={[0, 0.3 * scale, 0]}>
                                    <cylinderGeometry args={[0.7 * scale, 0.7 * scale, 0.15 * scale, 6]} />
                                    <meshBasicMaterial color="#facc15" transparent opacity={0.8} wireframe />
                                </mesh>
                                <mesh position={[0, -0.1 * scale, 0]}>
                                    <cylinderGeometry args={[0.6 * scale, 0.6 * scale, 0.15 * scale, 6]} />
                                    <meshBasicMaterial color="#facc15" transparent opacity={0.8} wireframe />
                                </mesh>
                            </group>
                        )}
                    </group>
                )

            case 'WOODEN_BEEHIVE':
                // Ruche en bois traditionnelle (Wooden Beehive box on posts)
                return (
                    <group>
                        {/* Support legs/posts */}
                        <mesh position={[-0.8 * scale, 0.6 * scale, -0.8 * scale]} castShadow>
                            <boxGeometry args={[0.15 * scale, 1.2 * scale, 0.15 * scale]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#451a03'} wireframe={isPhantomMode} />
                        </mesh>
                        <mesh position={[0.8 * scale, 0.6 * scale, -0.8 * scale]} castShadow>
                            <boxGeometry args={[0.15 * scale, 1.2 * scale, 0.15 * scale]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#451a03'} wireframe={isPhantomMode} />
                        </mesh>
                        <mesh position={[-0.8 * scale, 0.6 * scale, 0.8 * scale]} castShadow>
                            <boxGeometry args={[0.15 * scale, 1.2 * scale, 0.15 * scale]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#451a03'} wireframe={isPhantomMode} />
                        </mesh>
                        <mesh position={[0.8 * scale, 0.6 * scale, 0.8 * scale]} castShadow>
                            <boxGeometry args={[0.15 * scale, 1.2 * scale, 0.15 * scale]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#451a03'} wireframe={isPhantomMode} />
                        </mesh>
                        {/* Wooden Box Body */}
                        <mesh position={[0, 2.0 * scale, 0]} castShadow receiveShadow>
                            <boxGeometry args={[2.0 * scale, 1.8 * scale, 2.0 * scale]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#d97706'}
                                roughness={0.7}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                                emissive={isPhantomMode ? '#0284c7' : '#000000'}
                                emissiveIntensity={isPhantomMode ? 0.6 : 0}
                            />
                        </mesh>
                        {/* Internal Wooden Honeycomb Frames (Phantom Cutaway View) */}
                        {isPhantomMode && (
                            <group position={[0, 2.0 * scale, 0]}>
                                {[-0.6, -0.2, 0.2, 0.6].map((xOffset, i) => (
                                    <mesh key={i} position={[xOffset * scale, 0, 0]}>
                                        <boxGeometry args={[0.08 * scale, 1.4 * scale, 1.6 * scale]} />
                                        <meshBasicMaterial color="#fbbf24" transparent opacity={0.8} wireframe />
                                    </mesh>
                                ))}
                            </group>
                        )}
                        {/* Pitched Wooden Roof */}
                        <mesh position={[0, 3.2 * scale, 0]} rotation={[0, Math.PI / 4, 0]} castShadow>
                            <coneGeometry args={[1.8 * scale, 0.9 * scale, 4]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#78350f'} roughness={0.8} wireframe={isPhantomMode} />
                        </mesh>
                    </group>
                )

            case 'TREE_TRUNK':
                // Cavité dans Tronc d'Arbre (Hollow Tree Log nest)
                return (
                    <group>
                        {/* Hollow log trunk */}
                        <mesh position={[0, 1.8 * scale, 0]} castShadow receiveShadow>
                            <cylinderGeometry args={[1.4 * scale, 1.6 * scale, 3.6 * scale, 12]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#29180c'}
                                roughness={0.95}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                                emissive={isPhantomMode ? '#0284c7' : '#000000'}
                                emissiveIntensity={isPhantomMode ? 0.6 : 0}
                            />
                        </mesh>
                        {/* Entrance cavity slot */}
                        {!isPhantomMode && (
                            <mesh position={[0, 1.8 * scale, 1.35 * scale]}>
                                <boxGeometry args={[0.8 * scale, 1.4 * scale, 0.2 * scale]} />
                                <meshStandardMaterial color="#000000" roughness={1.0} />
                            </mesh>
                        )}
                    </group>
                )

            case 'EARTH_MOUND':
            default:
                // Fourmilière terrestre sous-terraine
                return (
                    <mesh position={[0, 0.5 * scale, 0]} castShadow receiveShadow>
                        <sphereGeometry args={[1.8 * scale, 16, 12, 0, Math.PI * 2, 0, Math.PI / 2]} />
                        <meshStandardMaterial
                            color={isPhantomMode ? '#38bdf8' : '#5c3a21'}
                            roughness={0.9}
                            wireframe={isPhantomMode}
                            transparent={isPhantomMode}
                            opacity={isPhantomMode ? 0.45 : 1.0}
                            emissive={isPhantomMode ? '#0284c7' : '#000000'}
                            emissiveIntensity={isPhantomMode ? 0.6 : 0}
                        />
                    </mesh>
                )
        }
    }

    return (
        <group ref={groupRef} position={[posX, posY, posZ]}>
            {renderNestGeometry()}

            {/* Phantom Hologram Pulsing Ring Indicator if in Ghost / Phantom Mode */}
            {(isPhantomMode || isGhost) && (
                <mesh ref={phantomMeshRef} position={[0, 0.05, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                    <ringGeometry args={[1.8 * scale, 2.5 * scale, 32]} />
                    <meshBasicMaterial color="#38bdf8" transparent opacity={0.5} side={THREE.DoubleSide} />
                </mesh>
            )}
        </group>
    )
}

export default function NestRenderer() {
    const { nests, phantomNestsVisible, ghostNest } = useSimulationStore()

    return (
        <group>
            {/* Active Nests in Simulation */}
            {nests.map((nest) => (
                <SingleNest
                    key={nest.id}
                    nest={{
                        ...nest,
                        isPhantom: nest.isPhantom || phantomNestsVisible
                    }}
                />
            ))}

            {/* Ghost Nest Preview during Placement Mode */}
            {ghostNest && ghostNest.active && (
                <SingleNest nest={ghostNest} isGhost={true} />
            )}
        </group>
    )
}
