import React, { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import { getTerrainHeight } from '../utils/terrainUtils'
import NestArchitectureMesh from './NestArchitectureMesh'

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
    const lookAndFeel = useSimulationStore(state => state.lookAndFeel)
    const isGamified = lookAndFeel === 'GAMING'

    // Pulse effect for Phantom / Ghost rendering
    useFrame((state) => {
        if ((nest?.isPhantom || isGhost) && phantomMeshRef.current) {
            const time = state.clock.getElapsedTime()
            phantomMeshRef.current.material.opacity = 0.35 + Math.sin(time * 3) * 0.25
        }
    })

    const scale = (nest?.scale || 1.0) * 0.75

    // Accurate normalized world coordinate mapper [0..100]m
    const toWorldCoord = (val) => {
        if (val === undefined || val === null) return 50
        return Math.max(2, Math.min(98, Number(val) || 50))
    }

    const posX = toWorldCoord(nest?.x)
    const posZ = toWorldCoord(nest?.z !== undefined ? nest?.z : nest?.y)
    const groundY = getTerrainHeight(posX, posZ, terrainConfig)

    const isPhantomMode = nest?.isPhantom || isGhost

    // Multi-exit surface portals for subterranean / mound nests (ensures all exits snap to ground altitude Y)
    const exitPortals = useMemo(() => {
        if (!nest) return []
        const offsets = nest?.exits || [
            { offsetX: 0, offsetZ: 0, isMain: true },
            { offsetX: 0.9 * scale, offsetZ: 0.6 * scale, isMain: false },
            { offsetX: -0.9 * scale, offsetZ: -0.7 * scale, isMain: false }
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

    if (!nest) return null

    const archType = (nest.type || nest.architecture || 'SUBTERRANEAN').toUpperCase()

    return (
        <group ref={groupRef} position={[posX, groundY, posZ]}>
            <NestArchitectureMesh
                archType={archType}
                scale={scale}
                isPhantomMode={isPhantomMode}
                isGamified={isGamified}
                exitPortals={exitPortals}
            />

            {/* Phantom Hologram Pulsing Ring Indicator if in Ghost / Phantom Mode */}
            {(isPhantomMode || isGhost) && (
                <mesh ref={phantomMeshRef} position={[0, 0.05, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                    <ringGeometry args={[1.0 * scale, 1.35 * scale, 32]} />
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
            {(nests || []).map((nest) => (
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
