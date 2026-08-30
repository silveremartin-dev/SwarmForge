import React, { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import { getTerrainHeight } from '../utils/terrainUtils'

/**
 * Complete 3D Nest Renderer supporting ALL 13 SwarmForge Nest Architectures:
 * 1. ARBOREAL_CARTON (Nid cartonné arboricole sur branche)
 * 2. ARBOREAL_SILK (Nid en soie arboricole tissé dans les feuilles)
 * 3. BAMBOO (Nid dans tige de bambou ancrée au sol)
 * 4. BIVOUAC (Bivouac vivant de fourmis légionnaires)
 * 5. CATHEDRAL / TERMITE_MOUND (Termitière cathédrale)
 * 6. HANGING_PAPER / WASP_BRANCH (Guêpier suspendu sur branche)
 * 7. WAX_COMB (Rayons de cire suspendus)
 * 8. HOLLOW_TRUNK / TREE_TRUNK (Tronc creux ancré au sol)
 * 9. SUBTERRANEAN (Nid souterrain à entrées multiples)
 * 10. FUNGI_VAULT (Voûte à champignons Atta sous-terraine)
 * 11. SURFACE_DOME / PINE_NEEDLES (Dôme d'épines de surface à sorties multiples)
 * 12. WAX_POTS (Pots de cire de bourdon sous pierre/sol)
 * 13. WOODEN_BEEHIVE (Ruche en bois sur pieds ancrés au sol)
 */
function SingleNest({ nest, isGhost = false }) {
    const groupRef = useRef()
    const phantomMeshRef = useRef()
    const terrainConfig = useSimulationStore(state => state.terrainConfig)

    // Pulse effect for Phantom / Ghost rendering
    useFrame((state) => {
        if ((nest.isPhantom || isGhost) && phantomMeshRef.current) {
            const time = state.clock.getElapsedTime()
            phantomMeshRef.current.material.opacity = 0.35 + Math.sin(time * 3) * 0.25
        }
    })

    const scale = nest.scale || 1.0
    const posX = nest.x || 50
    const posZ = nest.y || 50
    const groundY = getTerrainHeight(posX, posZ, terrainConfig)

    const isPhantomMode = nest.isPhantom || isGhost

    // Multi-exit surface portals for subterranean / mound nests (ensures all exits snap to ground altitude Y)
    const exitPortals = useMemo(() => {
        const offsets = nest.exits || [
            { offsetX: 0, offsetZ: 0, isMain: true },
            { offsetX: 2.2 * scale, offsetZ: 1.5 * scale, isMain: false },
            { offsetX: -2.5 * scale, offsetZ: -1.8 * scale, isMain: false }
        ]

        return offsets.map((exit, idx) => {
            const worldX = posX + exit.offsetX
            const worldZ = posZ + exit.offsetZ
            const exitY = getTerrainHeight(worldX, worldZ, terrainConfig)
            const relY = exitY - groundY

            return {
                id: idx,
                relX: exit.offsetX,
                relY,
                relZ: exit.offsetZ,
                isMain: exit.isMain
            }
        })
    }, [posX, posZ, groundY, scale, nest.exits, terrainConfig])

    // Render mesh based on Nest Architecture (13 architectures + legacy aliases)
    const renderNestGeometry = () => {
        const archType = (nest.type || nest.architecture || 'SUBTERRANEAN').toUpperCase()

        switch (archType) {
            case 'ARBOREAL_CARTON':
                // 1. Nid cartonné arboricole (Crematogaster) sur branche ancrée au sol
                return (
                    <group>
                        <mesh position={[0, 2.5 * scale, 0]} castShadow receiveShadow>
                            <cylinderGeometry args={[0.4 * scale, 0.6 * scale, 5.0 * scale, 10]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#29180c'} roughness={0.95} wireframe={isPhantomMode} />
                        </mesh>
                        <mesh position={[1.2 * scale, 4.2 * scale, 0]} rotation={[0, 0, -0.2]} castShadow>
                            <cylinderGeometry args={[0.22 * scale, 0.3 * scale, 3.5 * scale, 8]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#3f2b1d'} roughness={0.9} wireframe={isPhantomMode} />
                        </mesh>
                        {/* Spherical carton bulb attached around the branch */}
                        <mesh position={[1.5 * scale, 4.0 * scale, 0]} castShadow>
                            <sphereGeometry args={[1.2 * scale, 12, 12]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#452b19'}
                                roughness={0.95}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                            />
                        </mesh>
                    </group>
                )

            case 'ARBOREAL_SILK':
                // 2. Nid en soie arboricole (Oecophylla weaver ant) tissé dans le feuillage
                return (
                    <group>
                        <mesh position={[0, 2.8 * scale, 0]} castShadow receiveShadow>
                            <cylinderGeometry args={[0.35 * scale, 0.55 * scale, 5.6 * scale, 10]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#1f1308'} roughness={0.95} wireframe={isPhantomMode} />
                        </mesh>
                        {/* Silk leaf bundle in canopy */}
                        <group position={[0.8 * scale, 5.0 * scale, 0]}>
                            <mesh castShadow>
                                <dodecahedronGeometry args={[1.4 * scale, 1]} />
                                <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#166534'} roughness={0.6} wireframe={isPhantomMode} />
                            </mesh>
                            {/* White silk thread weaving wrapping the leaves */}
                            <mesh scale={1.05}>
                                <dodecahedronGeometry args={[1.4 * scale, 1]} />
                                <meshStandardMaterial color="#f8fafc" roughness={0.3} wireframe transparent opacity={0.7} />
                            </mesh>
                        </group>
                    </group>
                )

            case 'BAMBOO':
                // 3. Nid dans tige de bambou ancrée au sol (Temnothorax)
                return (
                    <group>
                        <mesh position={[0, 2.2 * scale, 0]} castShadow receiveShadow>
                            <cylinderGeometry args={[0.25 * scale, 0.35 * scale, 4.4 * scale, 12]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#65a30d'}
                                roughness={0.4}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                            />
                        </mesh>
                        {/* Bamboo nodes / segments */}
                        {[1.0, 2.2, 3.4].map((nodeY, idx) => (
                            <mesh key={`node-${idx}`} position={[0, nodeY * scale, 0]}>
                                <torusGeometry args={[0.3 * scale, 0.05 * scale, 8, 16]} />
                                <meshStandardMaterial color="#4d7c0f" roughness={0.3} />
                            </mesh>
                        ))}
                    </group>
                )

            case 'BIVOUAC':
                // 4. Bivouac vivant de fourmis légionnaires (Eciton)
                return (
                    <group>
                        {/* Hanging cluster teardrop shape */}
                        <mesh position={[0, 1.8 * scale, 0]} rotation={[0, 0, Math.PI]} castShadow>
                            <coneGeometry args={[1.4 * scale, 3.2 * scale, 12]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#3b0764'}
                                roughness={0.95}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                                emissive={isPhantomMode ? '#0284c7' : '#000000'}
                            />
                        </mesh>
                    </group>
                )

            case 'CATHEDRAL':
            case 'TERMITE_MOUND':
                // 5. Termitière verticale cathédrale ancrée au sol
                return (
                    <group>
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
                        {exitPortals.filter(p => !p.isMain).map((portal) => (
                            <group key={`termite-exit-${portal.id}`} position={[portal.relX, portal.relY, portal.relZ]}>
                                <mesh position={[0, 0.2 * scale, 0]} castShadow>
                                    <coneGeometry args={[0.4 * scale, 0.5 * scale, 8]} />
                                    <meshStandardMaterial color="#7c3a17" roughness={0.9} />
                                </mesh>
                            </group>
                        ))}
                    </group>
                )

            case 'HANGING_PAPER':
            case 'WASP_BRANCH':
                // 6. Guêpier suspendu sur branche d'arbre ancrée au sol
                return (
                    <group>
                        <mesh position={[0, 2.5 * scale, 0]} castShadow receiveShadow>
                            <cylinderGeometry args={[0.4 * scale, 0.6 * scale, 5.0 * scale, 10]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#29180c'} roughness={0.95} wireframe={isPhantomMode} />
                        </mesh>
                        <mesh position={[1.5 * scale, 4.5 * scale, 0]} rotation={[0, 0, -0.15]} castShadow>
                            <cylinderGeometry args={[0.2 * scale, 0.3 * scale, 4.0 * scale, 8]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#3f2b1d'} roughness={0.9} wireframe={isPhantomMode} />
                        </mesh>
                        {!isPhantomMode && (
                            <group position={[2.5 * scale, 5.2 * scale, 0]}>
                                <mesh castShadow>
                                    <dodecahedronGeometry args={[1.5 * scale, 1]} />
                                    <meshStandardMaterial color="#15803d" roughness={0.7} />
                                </mesh>
                            </group>
                        )}
                        <mesh position={[1.8 * scale, 3.4 * scale, 0]} castShadow>
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
                    </group>
                )

            case 'WAX_COMB':
                // 7. Rayons de cire d'abeille sauvages suspendus
                return (
                    <group>
                        {[-0.4, 0, 0.4].map((offsetZ, i) => (
                            <mesh key={`comb-${i}`} position={[0, 2.5 * scale, offsetZ * scale]} castShadow>
                                <boxGeometry args={[1.8 * scale, 1.6 * scale, 0.15 * scale]} />
                                <meshStandardMaterial
                                    color={isPhantomMode ? '#38bdf8' : '#fbbf24'}
                                    roughness={0.5}
                                    wireframe={isPhantomMode}
                                />
                            </mesh>
                        ))}
                    </group>
                )

            case 'HOLLOW_TRUNK':
            case 'TREE_TRUNK':
                // 8. Tronc d'arbre creux ancré au sol
                return (
                    <group>
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
                        {!isPhantomMode && (
                            <mesh position={[0, 1.8 * scale, 1.35 * scale]}>
                                <boxGeometry args={[0.8 * scale, 1.4 * scale, 0.2 * scale]} />
                                <meshStandardMaterial color="#000000" roughness={1.0} />
                            </mesh>
                        )}
                    </group>
                )

            case 'FUNGI_VAULT':
                // 10. Voûte à champignons (Atta leafcutter) sous-terraine avec cratères de surface
                return (
                    <group>
                        {/* Multiple craterets in a foraging cluster */}
                        {exitPortals.map((portal) => (
                            <group key={`fungi-exit-${portal.id}`} position={[portal.relX, portal.relY, portal.relZ]}>
                                <mesh position={[0, 0.2 * scale, 0]} castShadow receiveShadow>
                                    <coneGeometry args={[0.8 * scale, 0.4 * scale, 12]} />
                                    <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#854d0e'} roughness={0.9} wireframe={isPhantomMode} />
                                </mesh>
                            </group>
                        ))}
                        {/* Fungus chamber cutaway (glowing emerald green in phantom mode) */}
                        {isPhantomMode && (
                            <mesh position={[0, -1.2 * scale, 0]}>
                                <sphereGeometry args={[1.1 * scale, 16, 16]} />
                                <meshBasicMaterial color="#10b981" transparent opacity={0.85} wireframe />
                            </mesh>
                        )}
                    </group>
                )

            case 'SURFACE_DOME':
            case 'PINE_NEEDLES':
                // 11. Dôme d'épines de surface à sorties multiples
                return (
                    <group>
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
                        {exitPortals.map((portal) => (
                            <group key={`exit-${portal.id}`} position={[portal.relX, portal.relY, portal.relZ]}>
                                <mesh position={[0, 0.05, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                                    <ringGeometry args={[0.25 * scale, 0.45 * scale, 16]} />
                                    <meshStandardMaterial color="#2d1d0f" roughness={1.0} side={THREE.DoubleSide} />
                                </mesh>
                            </group>
                        ))}
                    </group>
                )

            case 'WAX_POTS':
                // 12. Pots de cire de bourdon (Bombus) sous pierre/sol
                return (
                    <group>
                        {[-0.5, 0, 0.5].map((px, i) =>
                            [-0.4, 0.4].map((pz, j) => (
                                <mesh key={`pot-${i}-${j}`} position={[px * scale, 0.4 * scale, pz * scale]} castShadow>
                                    <sphereGeometry args={[0.4 * scale, 10, 10]} />
                                    <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#ca8a04'} roughness={0.7} wireframe={isPhantomMode} />
                                </mesh>
                            ))
                        )}
                    </group>
                )

            case 'WOODEN_BEEHIVE':
                // 13. Ruche en bois sur 4 pieds ancrés au sol
                return (
                    <group>
                        {[-0.8, 0.8].map((lx) =>
                            [-0.8, 0.8].map((lz) => (
                                <mesh key={`leg-${lx}-${lz}`} position={[lx * scale, 0.6 * scale, lz * scale]} castShadow>
                                    <boxGeometry args={[0.15 * scale, 1.2 * scale, 0.15 * scale]} />
                                    <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#451a03'} wireframe={isPhantomMode} />
                                </mesh>
                            ))
                        )}
                        <mesh position={[0, 2.0 * scale, 0]} castShadow receiveShadow>
                            <boxGeometry args={[2.0 * scale, 1.8 * scale, 2.0 * scale]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#d97706'}
                                roughness={0.7}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                            />
                        </mesh>
                        <mesh position={[0, 3.2 * scale, 0]} rotation={[0, Math.PI / 4, 0]} castShadow>
                            <coneGeometry args={[1.8 * scale, 0.9 * scale, 4]} />
                            <meshStandardMaterial color={isPhantomMode ? '#38bdf8' : '#78350f'} roughness={0.8} wireframe={isPhantomMode} />
                        </mesh>
                    </group>
                )

            case 'STONE_CREVICE':
                // Nid sous rocher / crevasse rocheuse posée au sol
                return (
                    <group>
                        <mesh position={[0, 0.6 * scale, 0]} rotation={[0.1, 0.4, -0.05]} castShadow receiveShadow>
                            <dodecahedronGeometry args={[1.8 * scale, 1]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#475569'}
                                roughness={0.9}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                            />
                        </mesh>
                        <mesh position={[0.6 * scale, 0.15 * scale, 0.8 * scale]} rotation={[0, 0.5, 0]}>
                            <boxGeometry args={[1.2 * scale, 0.3 * scale, 0.4 * scale]} />
                            <meshStandardMaterial color="#0f172a" roughness={1.0} />
                        </mesh>
                    </group>
                )

            case 'ROTTEN_LOG':
                // Nid sous souche / bois mort couché horizontalement sur le sol
                return (
                    <group>
                        <mesh position={[0, 0.5 * scale, 0]} rotation={[0, 0.8, Math.PI / 2]} castShadow receiveShadow>
                            <cylinderGeometry args={[0.7 * scale, 0.8 * scale, 3.8 * scale, 10]} />
                            <meshStandardMaterial
                                color={isPhantomMode ? '#38bdf8' : '#331e11'}
                                roughness={0.95}
                                wireframe={isPhantomMode}
                                transparent={isPhantomMode}
                                opacity={isPhantomMode ? 0.45 : 1.0}
                            />
                        </mesh>
                    </group>
                )

            case 'SUBTERRANEAN':
            default:
                // 9. Nid souterrain (Fourmilière terrestre classique à dôme & entrées multiples)
                return (
                    <group>
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
                        {exitPortals.map((portal) => (
                            <group key={`earth-exit-${portal.id}`} position={[portal.relX, portal.relY, portal.relZ]}>
                                <mesh position={[0, 0.04, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                                    <ringGeometry args={[0.3 * scale, 0.55 * scale, 16]} />
                                    <meshStandardMaterial color="#3a2312" roughness={0.95} side={THREE.DoubleSide} />
                                </mesh>
                            </group>
                        ))}
                    </group>
                )
        }
    }

    return (
        <group ref={groupRef} position={[posX, groundY, posZ]}>
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
