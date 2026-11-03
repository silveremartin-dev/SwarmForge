import React, { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import LowPolyModel from './LowPolyModel'
import { getTerrainHeight, getEffectiveSeason } from '../utils/terrainUtils'
import {
    VoxelTree,
    RealisticTree,
    VoxelHollowStump,
    GamifiedVoxelFlora,
    Stump3D,
    ScientificTree,
    getBiomeTreePositions
} from './VegetationSubComponents'

/**
 * Dense Realistic Ground Flora & Understory with Biome Adaption
 */
function RealisticGroundFlora({ windSpeed = 2.4, season = 'SUMMER', climateType = 'OCEANIC', terrainConfig }) {
    const floraItems = useMemo(() => {
        const items = []
        const rand = (seed) => {
            const x = Math.sin(seed * 12.9898 + 78.233) * 43758.5453
            return x - Math.floor(x)
        }

        // 80 patches of dense natural flora
        for (let i = 0; i < 80; i++) {
            let x = rand(i * 1.1) * 90 + 5
            let z = rand(i * 2.3) * 90 + 5

            if (x >= 18 && x <= 32) {
                x = x < 25 ? 14 : 36
            }

            const typeInt = Math.floor(rand(i * 3.7) * 4)
            const type = typeInt === 0 ? 'FERN' : typeInt === 1 ? 'MOSS' : typeInt === 2 ? 'GRASS' : 'FLOWER'
            const scale = 0.6 + rand(i * 5.1) * 0.7
            const rotY = rand(i * 7.9) * Math.PI * 2
            const groundY = getTerrainHeight(x, z, terrainConfig)

            items.push({ id: i, x, y: groundY, z, type, scale, rotY })
        }
        return items
    }, [terrainConfig])

    const groupRef = useRef()
    const { running, isPaused, speed } = useSimulationStore()
    const isSimActive = Boolean(running && !isPaused && (typeof speed === 'number' ? speed > 0 : true))
    const floraTimeRef = useRef(0)

    useFrame((state, delta) => {
        if (groupRef.current) {
            if (isSimActive) {
                floraTimeRef.current += delta * (speed || 1.0)
            }
            const t = floraTimeRef.current
            groupRef.current.children.forEach((child, idx) => {
                if (child.userData?.type === 'GRASS' || child.userData?.type === 'FERN') {
                    child.rotation.z = Math.sin(t * 2 + idx) * 0.04
                }
            })
        }
    })

    const isArid = climateType === 'ARID' || climateType === 'SAVANNA' || climateType === 'DESERT'
    const isTropical = climateType === 'TROPICAL'

    return (
        <group ref={groupRef} frustumCulled={false}>
            {/* Biome-specific Bushes / Cacti / Tropical Shrubs */}
            {isArid ? (
                <>
                    <LowPolyModel
                        url="/3d/cactus.obj"
                        objectName="2_lowpoly_Circle.047"
                        targetHeight={1.6}
                        position={[45, getTerrainHeight(45, 82, terrainConfig), 82]}
                        scale={0.95}
                    />
                    <LowPolyModel
                        url="/3d/cactus.obj"
                        objectName="3_lowpoly_Circle.005"
                        targetHeight={1.4}
                        position={[28, getTerrainHeight(28, 22, terrainConfig), 22]}
                        scale={0.9}
                        rotation={[0, 2.1, 0]}
                    />
                    <LowPolyModel
                        url="/3d/nature_pack/Bushes.glb"
                        objectName="Plant_1"
                        targetHeight={1.1}
                        position={[65, getTerrainHeight(65, 85, terrainConfig), 85]}
                        scale={0.85}
                        rotation={[0, 0.8, 0]}
                        season={season}
                    />
                    <LowPolyModel
                        url="/3d/cactus.obj"
                        objectName="4_lowpoly_Circle.007"
                        targetHeight={1.3}
                        position={[14, getTerrainHeight(14, 48, terrainConfig), 48]}
                        scale={0.85}
                        rotation={[0, 1.2, 0]}
                    />
                </>
            ) : isTropical ? (
                <>
                    <LowPolyModel
                        url="/3d/nature_pack/Flower Bushes.glb"
                        objectName="Plant_Flowers"
                        targetHeight={1.6}
                        position={[45, getTerrainHeight(45, 82, terrainConfig), 82]}
                        scale={0.95}
                        season={season}
                    />
                    <LowPolyModel
                        url="/3d/bamboo_set.obj"
                        objectName="bamboo_shrub_1"
                        targetHeight={1.5}
                        position={[28, getTerrainHeight(28, 22, terrainConfig), 22]}
                        scale={0.9}
                        rotation={[0, 2.1, 0]}
                        season={season}
                    />
                    <LowPolyModel
                        url="/3d/nature_pack/Flower Bushes.glb"
                        objectName="Plant_2"
                        targetHeight={1.4}
                        position={[65, getTerrainHeight(65, 85, terrainConfig), 85]}
                        scale={0.9}
                        rotation={[0, 0.8, 0]}
                        season={season}
                    />
                    <LowPolyModel
                        url="/3d/bamboo_set.obj"
                        objectName="bamboo_shrub_2"
                        targetHeight={1.4}
                        position={[14, getTerrainHeight(14, 48, terrainConfig), 48]}
                        scale={0.85}
                        rotation={[0, 1.2, 0]}
                        season={season}
                    />
                </>
            ) : (
                <>
                    <LowPolyModel
                        url="/3d/nature_pack/Bushes.glb"
                        objectName="Bush"
                        targetHeight={1.4}
                        position={[45, getTerrainHeight(45, 82, terrainConfig), 82]}
                        scale={0.95}
                        sway={true}
                        windSpeed={windSpeed}
                        season={season}
                    />
                    <LowPolyModel
                        url="/3d/nature_pack/Bushes.glb"
                        objectName="Plant_1"
                        targetHeight={1.2}
                        position={[28, getTerrainHeight(28, 22, terrainConfig), 22]}
                        scale={0.9}
                        rotation={[0, 2.1, 0]}
                        sway={true}
                        windSpeed={windSpeed}
                        season={season}
                    />
                    <LowPolyModel
                        url="/3d/nature_pack/Bushes.glb"
                        objectName="Bush_Flowers"
                        targetHeight={1.5}
                        position={[65, getTerrainHeight(65, 85, terrainConfig), 85]}
                        scale={0.9}
                        rotation={[0, 0.8, 0]}
                        season={season}
                    />
                    <LowPolyModel
                        url="/3d/nature_pack/Bushes.glb"
                        objectName="Bush"
                        targetHeight={1.3}
                        position={[14, getTerrainHeight(14, 48, terrainConfig), 48]}
                        scale={0.85}
                        rotation={[0, 1.2, 0]}
                        sway={true}
                        windSpeed={windSpeed}
                        season={season}
                    />
                </>
            )}

            {/* Mossy / Arid Rocks & Boulders from 3D pack */}
            <LowPolyModel
                url="/3d/nature_pack/Rocks.glb"
                objectName="Rock_1"
                targetHeight={1.8}
                position={[72, getTerrainHeight(72, 35, terrainConfig), 35]}
                scale={0.9}
            />
            <LowPolyModel
                url="/3d/nature_pack/Rocks.glb"
                objectName="Rock_3"
                targetHeight={1.4}
                position={[18, getTerrainHeight(18, 55, terrainConfig), 55]}
                scale={0.85}
                rotation={[0, 1.4, 0]}
            />
            <LowPolyModel
                url="/3d/nature_pack/Rocks.glb"
                objectName="Rock_2"
                targetHeight={1.2}
                position={[40, getTerrainHeight(40, 18, terrainConfig), 18]}
                scale={0.8}
                rotation={[0, 2.4, 0]}
            />
            <LowPolyModel
                url="/3d/nature_pack/Rocks.glb"
                objectName="Rock_5"
                targetHeight={1.5}
                position={[85, getTerrainHeight(85, 60, terrainConfig), 60]}
                scale={0.9}
                rotation={[0, 0.5, 0]}
            />

            {/* Clustered Flowers / Tropical blooms */}
            <LowPolyModel
                url="/3d/nature_pack/Flowers.glb"
                objectName={isTropical ? 'Flower_5_Clump' : 'Flower_1_Clump'}
                targetHeight={0.8}
                position={[35, getTerrainHeight(35, 40, terrainConfig), 40]}
                scale={0.9}
            />
            <LowPolyModel
                url="/3d/nature_pack/Flowers.glb"
                objectName="Flower_3_Clump"
                targetHeight={0.8}
                position={[60, getTerrainHeight(60, 70, terrainConfig), 70]}
                scale={0.9}
            />
            <LowPolyModel
                url="/3d/nature_pack/Flowers.glb"
                objectName="Flower_4_Clump"
                targetHeight={0.8}
                position={[22, getTerrainHeight(22, 72, terrainConfig), 72]}
                scale={0.85}
            />
            <LowPolyModel
                url="/3d/nature_pack/Flowers.glb"
                objectName="Flower_2_Clump"
                targetHeight={0.8}
                position={[80, getTerrainHeight(80, 38, terrainConfig), 38]}
                scale={0.85}
            />

            {/* Ground Grass / Moss / Fern Sprites */}
            {floraItems.map((item) => {
                const mossColor = isArid
                    ? '#a16207'
                    : season === 'WINTER' ? '#e2e8f0' : season === 'AUTUMN' ? '#a16207' : '#3f6212'
                const fernColor = isArid
                    ? '#b45309'
                    : season === 'WINTER' ? '#cbd5e1' : season === 'AUTUMN' ? '#b45309' : '#15803d'
                const grassColor = isArid
                    ? '#d4b483'
                    : season === 'WINTER' ? '#f1f5f9' : season === 'AUTUMN' ? '#ca8a04' : '#4d7c0f'

                if (item.type === 'MOSS') {
                    return (
                        <mesh key={item.id} position={[item.x, item.y + 0.08 * item.scale, item.z]} rotation={[0, item.rotY, 0]} receiveShadow frustumCulled={false}>
                            <dodecahedronGeometry args={[0.6 * item.scale, 1]} />
                            <meshStandardMaterial color={mossColor} roughness={0.95} side={THREE.FrontSide} depthWrite depthTest />
                        </mesh>
                    )
                }

                if (item.type === 'FERN') {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]} userData={{ type: 'FERN' }} frustumCulled={false}>
                            {[0, 1.05, 2.1, 3.15, 4.2, 5.25].map((angle, fIdx) => (
                                <mesh key={fIdx} position={[0, 0.3 * item.scale, 0]} rotation={[0.4, angle, 0.1]} castShadow frustumCulled={false}>
                                    <boxGeometry args={[0.35 * item.scale, 0.05 * item.scale, 1.1 * item.scale]} />
                                    <meshStandardMaterial color={fernColor} roughness={0.85} side={THREE.FrontSide} depthWrite depthTest />
                                </mesh>
                            ))}
                        </group>
                    )
                }

                if (item.type === 'GRASS') {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]} userData={{ type: 'GRASS' }} frustumCulled={false}>
                            {[-0.15, 0, 0.15].map((offset, gIdx) => (
                                <mesh key={gIdx} position={[offset, 0.4 * item.scale, offset]} rotation={[0.15 * gIdx, gIdx * 0.8, 0.1]} castShadow frustumCulled={false}>
                                    <boxGeometry args={[0.08 * item.scale, 0.8 * item.scale, 0.15 * item.scale]} />
                                    <meshStandardMaterial color={grassColor} roughness={0.85} side={THREE.FrontSide} depthWrite depthTest />
                                </mesh>
                            ))}
                        </group>
                    )
                }

                // Wildflower / Forest Mushroom cluster
                return (
                    <group key={item.id} position={[item.x, item.y, item.z]} rotation={[0, item.rotY, 0]} frustumCulled={false}>
                        <mesh position={[0, 0.3 * item.scale, 0]} castShadow frustumCulled={false}>
                            <cylinderGeometry args={[0.04 * item.scale, 0.06 * item.scale, 0.6 * item.scale, 8]} />
                            <meshStandardMaterial color="#f8fafc" roughness={0.9} side={THREE.FrontSide} depthWrite depthTest />
                        </mesh>
                        <mesh position={[0, 0.6 * item.scale, 0]} castShadow frustumCulled={false}>
                            <sphereGeometry args={[0.22 * item.scale, 12, 12]} />
                            <meshStandardMaterial color={item.id % 2 === 0 ? '#f43f5e' : '#fbbf24'} roughness={0.5} side={THREE.FrontSide} depthWrite depthTest />
                        </mesh>
                    </group>
                )
            })}
        </group>
    )
}

export default function VegetationRenderer() {
    const { lookAndFeel, climateEngine, terrainConfig } = useSimulationStore()
    const isGamified = lookAndFeel === 'GAMING'
    const isScientific = lookAndFeel === 'SCIENTIFIC'
    const windSpeed = climateEngine?.windSpeedMs ?? 2.4
    
    const rawSeason = climateEngine?.currentSeason || climateEngine?.season || 'SUMMER'
    const season = getEffectiveSeason(rawSeason, climateEngine?.hemisphere || 'NORTHERN')

    const climateType = climateEngine?.climateType || 'OCEANIC'

    // Standardized tree positions & biome-adapted models across all 3 modes
    const treePositions = useMemo(() => getBiomeTreePositions(climateType), [climateType])

    // Volumetric 3D Tree Stump positions
    const stumpPositions = useMemo(() => [
        { id: 'stump_1', pos: [32, 0, 60], scale: 1.2 },
        { id: 'stump_2', pos: [68, 0, 42], scale: 1.4 },
        { id: 'stump_3', pos: [22, 0, 18], scale: 1.0 },
    ], [])

    return (
        <group frustumCulled={false}>
            {/* Render Trees according to visual mode */}
            {treePositions.map((tree) => {
                if (isGamified) {
                    return <VoxelTree key={tree.id} position={tree.pos} scale={tree.scale} variant={tree.variant} terrainConfig={terrainConfig} />
                }
                if (isScientific) {
                    return <ScientificTree key={tree.id} position={tree.pos} scale={tree.scale} variant={tree.variant} season={season} terrainConfig={terrainConfig} />
                }
                return (
                    <RealisticTree
                        key={tree.id}
                        position={tree.pos}
                        scale={tree.scale}
                        targetHeight={tree.targetHeight}
                        windSpeed={windSpeed}
                        season={season}
                        url={tree.url}
                        objectName={tree.objectName}
                        terrainConfig={terrainConfig}
                    />
                )
            })}

            {/* Grand Ancient Hollow Stump in Gamified mode, or 3D Stumps in Realistic mode */}
            {isGamified ? (
                <VoxelHollowStump position={[56, 0, 48]} scale={1.2} terrainConfig={terrainConfig} />
            ) : (
                stumpPositions.map((stump) => (
                    <Stump3D key={stump.id} position={stump.pos} scale={stump.scale} terrainConfig={terrainConfig} />
                ))
            )}

            {/* Flora Floor: Voxel Plants in Gamified mode vs Realistic Flora in Realistic/Scientific mode */}
            {isGamified ? (
                <GamifiedVoxelFlora terrainConfig={terrainConfig} />
            ) : (
                <RealisticGroundFlora windSpeed={windSpeed} season={season} climateType={climateType} terrainConfig={terrainConfig} />
            )}
        </group>
    )
}




