import React, { useRef, useMemo } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import LowPolyModel from './LowPolyModel'
import { getTerrainHeight, getEffectiveSeason } from '../utils/terrainUtils'
import {
    getPixelPoppyTexture,
    getPixelDandelionTexture,
    getPixelTallGrassTexture,
    getPixelOakLogSideTexture,
    getPixelOakLogTopTexture,
    getPixelOakLeavesTexture,
    getPixelBirchLogTexture,
    getPixelBirchLeavesTexture,
    getPixelPineLogTexture,
    getPixelPineLeavesTexture
} from '../utils/pixelTextures'

/**
 * Authentic Minecraft Voxel Tree Component (Gamified Mode)
 * Built out of crisp 16x16 pixel-textured cubic voxels snapped to a regular grid.
 * Strict Minecraft tree topologies:
 * - Oak: 1x1 or 2x2 trunk, 5x5 / 5x5 / 3x3 / cross leaf layers
 * - Birch: White bark with horizontal marks, lime green leaves
 * - Pine / Spruce: Dark log, contiguous tiered conical skirts
 */
function VoxelTree({ position, scale = 1.0, variant = 0, terrainConfig }) {
    const [posX, _, posZ] = position
    const gridX = Math.round(posX)
    const gridZ = Math.round(posZ)
    const groundY = getTerrainHeight(gridX, gridZ, terrainConfig)

    // Textures
    const oakLogSide = useMemo(() => getPixelOakLogSideTexture(), [])
    const oakLogTop = useMemo(() => getPixelOakLogTopTexture(), [])
    const oakLeavesTex = useMemo(() => getPixelOakLeavesTexture(), [])
    const birchLogTex = useMemo(() => getPixelBirchLogTexture(), [])
    const birchLeavesTex = useMemo(() => getPixelBirchLeavesTexture(), [])
    const pineLogTex = useMemo(() => getPixelPineLogTexture(), [])
    const pineLeavesTex = useMemo(() => getPixelPineLeavesTexture(), [])

    // Materials
    const { trunkSideMat, trunkTopMat, leavesMat } = useMemo(() => {
        if (variant % 3 === 1) {
            // Pine
            return {
                trunkSideMat: new THREE.MeshStandardMaterial({ map: pineLogTex, roughness: 0.9, metalness: 0.05 }),
                trunkTopMat: new THREE.MeshStandardMaterial({ map: oakLogTop, roughness: 0.9, metalness: 0.05 }),
                leavesMat: new THREE.MeshStandardMaterial({ map: pineLeavesTex, transparent: true, alphaTest: 0.25, roughness: 0.8, side: THREE.FrontSide })
            }
        }
        if (variant % 3 === 2) {
            // Birch
            return {
                trunkSideMat: new THREE.MeshStandardMaterial({ map: birchLogTex, roughness: 0.9, metalness: 0.05 }),
                trunkTopMat: new THREE.MeshStandardMaterial({ map: oakLogTop, roughness: 0.9, metalness: 0.05 }),
                leavesMat: new THREE.MeshStandardMaterial({ map: birchLeavesTex, transparent: true, alphaTest: 0.25, roughness: 0.8, side: THREE.FrontSide })
            }
        }
        // Oak
        return {
            trunkSideMat: new THREE.MeshStandardMaterial({ map: oakLogSide, roughness: 0.9, metalness: 0.05 }),
            trunkTopMat: new THREE.MeshStandardMaterial({ map: oakLogTop, roughness: 0.9, metalness: 0.05 }),
            leavesMat: new THREE.MeshStandardMaterial({ map: oakLeavesTex, transparent: true, alphaTest: 0.25, roughness: 0.8, side: THREE.FrontSide })
        }
    }, [variant, oakLogSide, oakLogTop, oakLeavesTex, birchLogTex, birchLeavesTex, pineLogTex, pineLeavesTex])

    const boxGeo = useMemo(() => new THREE.BoxGeometry(1, 1, 1), [])

    // Generate voxel blocks with exact Minecraft topologies (no floating gaps, no inverted layering)
    const { trunkVoxels, foliageVoxels } = useMemo(() => {
        const trunk = []
        const foliage = []
        const trunkH = Math.max(5, Math.round(6 * scale))

        // Variant 0: Standard Oak Tree (or Large Oak if scale > 1.2)
        if (variant % 3 === 0) {
            const isLarge = scale >= 1.25

            // 1. Trunk Column (Centered exactly on ground)
            for (let y = 0; y < trunkH; y++) {
                if (isLarge) {
                    trunk.push({ x: 0, y: y + 0.5, z: 0 })
                    trunk.push({ x: 1, y: y + 0.5, z: 0 })
                    trunk.push({ x: 0, y: y + 0.5, z: 1 })
                    trunk.push({ x: 1, y: y + 0.5, z: 1 })
                } else {
                    trunk.push({ x: 0, y: y + 0.5, z: 0 })
                }
            }

            // 2. Canopy Layers (Dense 5x5, 5x5, 3x3, cross)
            const baseLeafY = trunkH - 3
            // Layer 0: 5x5 (corner blocks omitted)
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (!isLarge && dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: baseLeafY + 0.5, z: dz })
                }
            }
            // Layer 1: 5x5 (corner blocks omitted)
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (!isLarge && dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: baseLeafY + 1.5, z: dz })
                }
            }
            // Layer 2: 3x3 square
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    foliage.push({ x: dx, y: baseLeafY + 2.5, z: dz })
                }
            }
            // Layer 3: 3x3 plus-cross at top crown
            const crossCoords = [[0, 0], [1, 0], [-1, 0], [0, 1], [0, -1]]
            crossCoords.forEach(([cx, cz]) => {
                foliage.push({ x: cx, y: baseLeafY + 3.5, z: cz })
            })

        // Variant 1: Pine / Spruce (Taïga Evergreen Conical Layers)
        } else if (variant % 3 === 1) {
            const pineH = Math.max(7, Math.round(8 * scale))

            for (let y = 0; y < pineH; y++) {
                trunk.push({ x: 0, y: y + 0.5, z: 0 })
            }

            const leafBase = pineH - 5
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: leafBase + 0.5, z: dz })
                }
            }
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) === 1 && Math.abs(dz) === 1) continue
                    foliage.push({ x: dx, y: leafBase + 1.5, z: dz })
                }
            }
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: leafBase + 2.5, z: dz })
                }
            }
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    foliage.push({ x: dx, y: leafBase + 3.5, z: dz })
                }
            }
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) === 1 && Math.abs(dz) === 1) continue
                    foliage.push({ x: dx, y: leafBase + 4.5, z: dz })
                }
            }
            foliage.push({ x: 0, y: leafBase + 5.5, z: 0 })

        // Variant 2: Birch Tree (White Bark + Bright Lime Leaves)
        } else {
            const birchH = Math.max(5, Math.round(6 * scale))

            for (let y = 0; y < birchH; y++) {
                trunk.push({ x: 0, y: y + 0.5, z: 0 })
            }

            const baseLeafY = birchH - 3
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: baseLeafY + 0.5, z: dz })
                }
            }
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: baseLeafY + 1.5, z: dz })
                }
            }
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    foliage.push({ x: dx, y: baseLeafY + 2.5, z: dz })
                }
            }
            foliage.push({ x: 0, y: baseLeafY + 3.5, z: 0 })
            foliage.push({ x: 1, y: baseLeafY + 3.5, z: 0 })
            foliage.push({ x: -1, y: baseLeafY + 3.5, z: 0 })
            foliage.push({ x: 0, y: baseLeafY + 3.5, z: 1 })
            foliage.push({ x: 0, y: baseLeafY + 3.5, z: -1 })
        }

        return { trunkVoxels: trunk, foliageVoxels: foliage }
    }, [scale, variant])

    return (
        <group position={[gridX, groundY, gridZ]} frustumCulled={false}>
            {trunkVoxels.map((v, i) => (
                <mesh
                    key={`trunk_${i}`}
                    geometry={boxGeo}
                    material={trunkSideMat}
                    position={[v.x, v.y, v.z]}
                    castShadow
                    receiveShadow
                    frustumCulled={false}
                />
            ))}

            {foliageVoxels.map((v, i) => (
                <mesh
                    key={`foliage_${i}`}
                    geometry={boxGeo}
                    material={leavesMat}
                    position={[v.x, v.y, v.z]}
                    castShadow
                    receiveShadow
                    frustumCulled={false}
                />
            ))}
        </group>
    )
}

/**
 * Single Realistic Tree Component
 */
function RealisticTree({ position, scale = 1.0, windSpeed = 2.4, season = 'SUMMER', url, objectName, terrainConfig }) {
    const [x, _, z] = position
    const groundY = getTerrainHeight(x, z, terrainConfig)

    return (
        <group position={[x, groundY, z]}>
            <LowPolyModel
                url={url || '/3d/nature_pack/Trees.glb'}
                objectName={objectName}
                targetHeight={9.5}
                scale={scale}
                sway={true}
                windSpeed={windSpeed}
                season={season}
                fallbackGeometry="dodecahedron"
                fallbackColor={season === 'AUTUMN' ? '#d97706' : season === 'WINTER' ? '#e2e8f0' : '#15803d'}
            />
        </group>
    )
}

/**
 * Dense Realistic Ground Flora & Understory
 */
function RealisticGroundFlora({ windSpeed = 2.4, season = 'SUMMER', terrainConfig }) {
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

    useFrame((state) => {
        if (groupRef.current) {
            const t = state.clock.getElapsedTime()
            groupRef.current.children.forEach((child, idx) => {
                if (child.userData?.type === 'GRASS' || child.userData?.type === 'FERN') {
                    child.rotation.z = Math.sin(t * 2 + idx) * 0.04
                }
            })
        }
    })

    return (
        <group ref={groupRef} frustumCulled={false}>
            {/* Natural Bushes & Shrubs from 3D pack */}
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

            {/* Mossy Rocks & Boulders from 3D pack */}
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

            {/* Clustered Flowers from 3D pack */}
            <LowPolyModel
                url="/3d/nature_pack/Flowers.glb"
                objectName="Flower_1_Clump"
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

            {floraItems.map((item) => {
                const mossColor = season === 'WINTER' ? '#e2e8f0' : season === 'AUTUMN' ? '#a16207' : '#3f6212'
                const fernColor = season === 'WINTER' ? '#cbd5e1' : season === 'AUTUMN' ? '#b45309' : '#15803d'
                const grassColor = season === 'WINTER' ? '#f1f5f9' : season === 'AUTUMN' ? '#ca8a04' : '#4d7c0f'

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

/**
 * Grand Ancient Hollow Tree Stump Component (Gamified Mode)
 */
function VoxelHollowStump({ position = [56, 0, 48], scale = 1.0, terrainConfig }) {
    const [posX, _, posZ] = position
    const gridX = Math.round(posX)
    const gridZ = Math.round(posZ)
    const groundY = getTerrainHeight(gridX, gridZ, terrainConfig)

    const { stumpVoxels, rootVoxels, fungiVoxels } = useMemo(() => {
        const trunk = []
        const roots = []
        const fungi = []
        const trunkRadius = Math.round(2.5 * scale)
        const trunkH = Math.round(5 * scale)

        for (let y = 0; y < trunkH; y++) {
            const isRim = y === trunkH - 1
            for (let dx = -trunkRadius; dx <= trunkRadius; dx++) {
                for (let dz = -trunkRadius; dz <= trunkRadius; dz++) {
                    const distSq = dx * dx + dz * dz
                    const isWall = distSq <= trunkRadius * trunkRadius && distSq >= (trunkRadius - 1.2) * (trunkRadius - 1.2)
                    if (isWall) {
                        if (isRim && (Math.abs(dx * dz) % 2 === 0)) continue
                        const barkColor = (y + dx + dz) % 2 === 0 ? '#451a03' : '#3b1402'
                        trunk.push({ x: dx, y: y + 0.5, z: dz, color: barkColor })
                    }
                }
            }
        }

        const flareAngles = [0, Math.PI / 2, Math.PI, (3 * Math.PI) / 2]
        flareAngles.forEach((angle, idx) => {
            const len = 2 + (idx % 2)
            for (let r = trunkRadius; r < trunkRadius + len; r++) {
                const rx = Math.round(Math.cos(angle) * r)
                const rz = Math.round(Math.sin(angle) * r)
                const rootH = Math.max(1, trunkH - (r - trunkRadius) * 2)
                for (let ry = 0; ry < rootH; ry++) {
                    roots.push({ x: rx, y: ry + 0.5, z: rz, color: '#3d2514' })
                }
            }
        })

        fungi.push({ x: trunkRadius, y: 2.5, z: 0, w: 1.5, d: 1.5, color: '#d97706' })
        fungi.push({ x: -trunkRadius, y: 3.5, z: 1, w: 1.5, d: 1.5, color: '#b45309' })

        return { stumpVoxels: trunk, rootVoxels: roots, fungiVoxels: fungi }
    }, [scale])

    return (
        <group position={[gridX, groundY, gridZ]} frustumCulled={false}>
            {stumpVoxels.map((v, i) => (
                <mesh key={`stump_${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow frustumCulled={false}>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color={v.color} roughness={0.9} metalness={0.05} depthWrite depthTest />
                </mesh>
            ))}
            {rootVoxels.map((v, i) => (
                <mesh key={`root_${i}`} position={[v.x, v.y, v.z]} castShadow receiveShadow frustumCulled={false}>
                    <boxGeometry args={[1, 1, 1]} />
                    <meshStandardMaterial color={v.color} roughness={0.92} metalness={0.05} depthWrite depthTest />
                </mesh>
            ))}
            {fungiVoxels.map((f, i) => (
                <mesh key={`fungi_${i}`} position={[f.x, f.y, f.z]} castShadow receiveShadow frustumCulled={false}>
                    <boxGeometry args={[f.w, 0.3, f.d]} />
                    <meshStandardMaterial color={f.color} roughness={0.7} metalness={0.05} depthWrite depthTest />
                </mesh>
            ))}
        </group>
    )
}

/**
 * 2.5D Cross-Billboard Mesh for Minecraft flowers and plants (X-shape)
 */
function PixelCrossMesh({ position, texture, scale = 1.0 }) {
    const geo = useMemo(() => new THREE.PlaneGeometry(0.85 * scale, 0.95 * scale), [scale])
    const mat = useMemo(() => new THREE.MeshStandardMaterial({
        map: texture,
        transparent: true,
        alphaTest: 0.25,
        roughness: 0.8,
        metalness: 0.05,
        side: THREE.DoubleSide,
    }), [texture])

    return (
        <group position={position} frustumCulled={false}>
            <mesh geometry={geo} material={mat} rotation={[0, Math.PI / 4, 0]} position={[0, 0.45 * scale, 0]} castShadow receiveShadow />
            <mesh geometry={geo} material={mat} rotation={[0, -Math.PI / 4, 0]} position={[0, 0.45 * scale, 0]} castShadow receiveShadow />
        </group>
    )
}

/**
 * Gamified Mode Voxel Flora Renderer
 */
function GamifiedVoxelFlora({ terrainConfig }) {
    const poppyTex = useMemo(() => getPixelPoppyTexture(), [])
    const dandelionTex = useMemo(() => getPixelDandelionTexture(), [])
    const tallGrassTex = useMemo(() => getPixelTallGrassTexture(), [])

    const voxelPlants = useMemo(() => {
        const items = []
        const rand = (seed) => {
            const x = Math.sin(seed * 14.123 + 45.67) * 43758.5453
            return x - Math.floor(x)
        }

        for (let i = 0; i < 70; i++) {
            const gridX = Math.round(rand(i * 1.7) * 90 + 5)
            const gridZ = Math.round(rand(i * 3.1) * 90 + 5)
            if (gridX >= 18 && gridX <= 32) continue // Skip river channel
            const groundY = getTerrainHeight(gridX, gridZ, terrainConfig)
            const type = Math.floor(rand(i * 4.3) * 6)

            items.push({ id: i, x: gridX, y: groundY, z: gridZ, type, scale: 0.8 + rand(i * 2.9) * 0.4 })
        }
        return items
    }, [terrainConfig])

    return (
        <group frustumCulled={false}>
            {voxelPlants.map((item) => {
                if (item.type === 3) {
                    return <PixelCrossMesh key={item.id} position={[item.x, item.y, item.z]} texture={poppyTex} scale={item.scale} />
                }
                if (item.type === 4) {
                    return <PixelCrossMesh key={item.id} position={[item.x, item.y, item.z]} texture={dandelionTex} scale={item.scale} />
                }
                if (item.type === 5) {
                    return <PixelCrossMesh key={item.id} position={[item.x, item.y, item.z]} texture={tallGrassTex} scale={item.scale} />
                }
                if (item.type === 0) {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} scale={[item.scale, item.scale, item.scale]} frustumCulled={false}>
                            <mesh position={[0, 0.4, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.3, 0.8, 0.3]} />
                                <meshStandardMaterial color="#f8fafc" roughness={0.8} depthWrite depthTest />
                            </mesh>
                            <mesh position={[0, 0.9, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[1.0, 0.25, 1.0]} />
                                <meshStandardMaterial color="#dc2626" roughness={0.6} depthWrite depthTest />
                            </mesh>
                            <mesh position={[0, 1.1, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.7, 0.2, 0.7]} />
                                <meshStandardMaterial color="#b91c1c" roughness={0.6} depthWrite depthTest />
                            </mesh>
                        </group>
                    )
                }
                if (item.type === 1) {
                    return (
                        <group key={item.id} position={[item.x, item.y, item.z]} scale={[item.scale, item.scale, item.scale]} frustumCulled={false}>
                            <mesh position={[0, 0.35, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[0.3, 0.7, 0.3]} />
                                <meshStandardMaterial color="#e2e8f0" roughness={0.85} depthWrite depthTest />
                            </mesh>
                            <mesh position={[0, 0.8, 0]} castShadow frustumCulled={false}>
                                <boxGeometry args={[1.1, 0.2, 1.1]} />
                                <meshStandardMaterial color="#92400e" roughness={0.7} depthWrite depthTest />
                            </mesh>
                        </group>
                    )
                }
                const flowerColor = item.id % 2 === 0 ? '#f43f5e' : '#eab308'
                return (
                    <group key={item.id} position={[item.x, item.y, item.z]} scale={[item.scale, item.scale, item.scale]} frustumCulled={false}>
                        <mesh position={[0, 0.35, 0]} castShadow frustumCulled={false}>
                            <boxGeometry args={[0.8, 0.7, 0.8]} />
                            <meshStandardMaterial color="#15803d" roughness={0.85} depthWrite depthTest />
                        </mesh>
                        <mesh position={[0.1, 0.75, 0.1]} castShadow frustumCulled={false}>
                            <boxGeometry args={[0.3, 0.3, 0.3]} />
                            <meshStandardMaterial color={flowerColor} roughness={0.6} depthWrite depthTest />
                        </mesh>
                    </group>
                )
            })}
        </group>
    )
}

/**
 * 3D Volumetric Tree Stump Component (Realistic Mode)
 */
function Stump3D({ position, scale = 1.0, terrainConfig }) {
    const [x, _, z] = position
    const groundY = getTerrainHeight(x, z, terrainConfig)
    const trunkHeight = 1.0 * scale
    const topRadius = 0.5 * scale
    const baseRadius = 0.65 * scale

    return (
        <group position={[x, groundY, z]} frustumCulled={false}>
            <mesh position={[0, trunkHeight / 2, 0]} castShadow receiveShadow frustumCulled={false}>
                <cylinderGeometry args={[topRadius, baseRadius, trunkHeight, 12]} />
                <meshStandardMaterial color="#5c3a21" roughness={0.9} metalness={0.05} depthTest={true} depthWrite={true} side={THREE.FrontSide} />
            </mesh>
            <mesh position={[0, trunkHeight + 0.01, 0]} rotation={[-Math.PI / 2, 0, 0]} receiveShadow frustumCulled={false}>
                <circleGeometry args={[topRadius * 0.95, 12]} />
                <meshStandardMaterial color="#d4a373" roughness={0.8} depthTest={true} depthWrite={true} side={THREE.FrontSide} />
            </mesh>
        </group>
    )
}

/**
 * Standardized Botanical Diagram Tree Component (Scientific Mode)
 * Calibré à l'échelle métrique réelle (hauteur 8-11m) avec anneau DBH à 1.3m
 */
function ScientificTree({ position, scale = 1.0, variant = 0, season = 'SUMMER', terrainConfig }) {
    const [x, _, z] = position
    const groundY = getTerrainHeight(x, z, terrainConfig)
    const trunkHeight = 6.0 * scale
    const crownColor = season === 'WINTER' ? '#cbd5e1' : season === 'AUTUMN' ? '#d97706' : '#15803d'

    return (
        <group position={[x, groundY, z]} frustumCulled={false}>
            {/* Projected Ground Root Zone Disk */}
            <mesh position={[0, 0.02, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                <ringGeometry args={[0.3 * scale, 3.0 * scale, 24]} />
                <meshBasicMaterial color="#0284c7" transparent opacity={0.15} side={THREE.DoubleSide} />
            </mesh>

            {/* Scientific Parametric Trunk (Charcoal / DBH Calibrated) */}
            <mesh position={[0, trunkHeight / 2, 0]} castShadow receiveShadow frustumCulled={false}>
                <cylinderGeometry args={[0.25 * scale, 0.45 * scale, trunkHeight, 12]} />
                <meshStandardMaterial color="#334155" roughness={0.7} metalness={0.15} side={THREE.FrontSide} depthWrite depthTest />
            </mesh>

            {/* DBH Metric Ring at Y=1.3m */}
            <mesh position={[0, 1.3, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                <torusGeometry args={[0.42 * scale, 0.025 * scale, 6, 16]} />
                <meshBasicMaterial color="#38bdf8" />
            </mesh>

            {/* LAI (Leaf Area Index) Light Interception Volumes */}
            {variant % 2 === 0 ? (
                // Conifer / Spruce Canopy
                <group position={[0, trunkHeight * 0.6, 0]} frustumCulled={false}>
                    <mesh position={[0, 0, 0]} castShadow receiveShadow frustumCulled={false}>
                        <coneGeometry args={[2.8 * scale, 3.2 * scale, 10]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[0, 1.8 * scale, 0]} castShadow receiveShadow frustumCulled={false}>
                        <coneGeometry args={[2.1 * scale, 2.6 * scale, 10]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[0, 3.4 * scale, 0]} castShadow receiveShadow frustumCulled={false}>
                        <coneGeometry args={[1.4 * scale, 2.0 * scale, 10]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                </group>
            ) : (
                // Deciduous / Broadleaf Canopy
                <group position={[0, trunkHeight + 1.2 * scale, 0]} frustumCulled={false}>
                    <mesh position={[0, 0, 0]} castShadow receiveShadow frustumCulled={false}>
                        <sphereGeometry args={[2.4 * scale, 12, 10]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[1.0 * scale, -0.5 * scale, 0.7 * scale]} castShadow receiveShadow frustumCulled={false}>
                        <sphereGeometry args={[1.4 * scale, 10, 8]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[-0.9 * scale, -0.4 * scale, -0.7 * scale]} castShadow receiveShadow frustumCulled={false}>
                        <sphereGeometry args={[1.5 * scale, 10, 8]} />
                        <meshStandardMaterial color={crownColor} roughness={0.65} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                </group>
            )}
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

    // Standardized tree positions & scale across all 3 modes
    const treePositions = useMemo(() => [
        { id: 1, pos: [15, 0, 30], scale: 1.0, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_1' },
        { id: 2, pos: [78, 0, 25], scale: 1.1, variant: 1, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_2' },
        { id: 3, pos: [82, 0, 75], scale: 1.05, variant: 2, url: '/3d/nature_pack/Birch Trees.glb', objectName: 'BirchTree_1' },
        { id: 4, pos: [18, 0, 80], scale: 1.1, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_3' },
        { id: 5, pos: [55, 0, 15], scale: 1.15, variant: 1, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_4' },
        { id: 6, pos: [88, 0, 88], scale: 1.0, variant: 2, url: '/3d/nature_pack/Birch Trees.glb', objectName: 'BirchTree_4' },
    ], [])

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
                <RealisticGroundFlora windSpeed={windSpeed} season={season} terrainConfig={terrainConfig} />
            )}
        </group>
    )
}




