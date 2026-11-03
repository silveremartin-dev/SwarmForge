import React, { useMemo } from 'react'
import * as THREE from 'three'
import { getTerrainHeight } from '../utils/terrainUtils'
import LowPolyModel from './LowPolyModel'
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
 */
export function VoxelTree({ position, scale = 1.0, variant = 0, terrainConfig }) {
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
            return {
                trunkSideMat: new THREE.MeshStandardMaterial({ map: pineLogTex, roughness: 0.9, metalness: 0.05 }),
                trunkTopMat: new THREE.MeshStandardMaterial({ map: oakLogTop, roughness: 0.9, metalness: 0.05 }),
                leavesMat: new THREE.MeshStandardMaterial({ map: pineLeavesTex, transparent: true, alphaTest: 0.25, roughness: 0.8, side: THREE.FrontSide })
            }
        }
        if (variant % 3 === 2) {
            return {
                trunkSideMat: new THREE.MeshStandardMaterial({ map: birchLogTex, roughness: 0.9, metalness: 0.05 }),
                trunkTopMat: new THREE.MeshStandardMaterial({ map: oakLogTop, roughness: 0.9, metalness: 0.05 }),
                leavesMat: new THREE.MeshStandardMaterial({ map: birchLeavesTex, transparent: true, alphaTest: 0.25, roughness: 0.8, side: THREE.FrontSide })
            }
        }
        return {
            trunkSideMat: new THREE.MeshStandardMaterial({ map: oakLogSide, roughness: 0.9, metalness: 0.05 }),
            trunkTopMat: new THREE.MeshStandardMaterial({ map: oakLogTop, roughness: 0.9, metalness: 0.05 }),
            leavesMat: new THREE.MeshStandardMaterial({ map: oakLeavesTex, transparent: true, alphaTest: 0.25, roughness: 0.8, side: THREE.FrontSide })
        }
    }, [variant, oakLogSide, oakLogTop, oakLeavesTex, birchLogTex, birchLeavesTex, pineLogTex, pineLeavesTex])

    const boxGeo = useMemo(() => new THREE.BoxGeometry(1, 1, 1), [])

    const { trunkVoxels, foliageVoxels } = useMemo(() => {
        const trunk = []
        const foliage = []
        const trunkH = Math.max(5, Math.round(6 * scale))

        if (variant % 3 === 0) {
            const isLarge = scale >= 1.25
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

            const baseLeafY = trunkH - 3
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (!isLarge && dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: baseLeafY + 0.5, z: dz })
                }
            }
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (!isLarge && dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: baseLeafY + 1.5, z: dz })
                }
            }
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    if (!isLarge && dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: baseLeafY + 2.5, z: dz })
                }
            }
            foliage.push({ x: 0, y: baseLeafY + 3.5, z: 0 })
            foliage.push({ x: 1, y: baseLeafY + 3.5, z: 0 })
            foliage.push({ x: -1, y: baseLeafY + 3.5, z: 0 })
            foliage.push({ x: 0, y: baseLeafY + 3.5, z: 1 })
            foliage.push({ x: 0, y: baseLeafY + 3.5, z: -1 })
        } else if (variant % 3 === 1) {
            const pineH = Math.max(6, Math.round(7 * scale))
            for (let y = 0; y < pineH; y++) {
                trunk.push({ x: 0, y: y + 0.5, z: 0 })
            }

            const leafBase = Math.max(2, pineH - 5)
            for (let dx = -2; dx <= 2; dx++) {
                for (let dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) === 2 && Math.abs(dz) === 2) continue
                    if (dx === 0 && dz === 0) continue
                    foliage.push({ x: dx, y: leafBase + 0.5, z: dz })
                }
            }
            for (let dx = -1; dx <= 1; dx++) {
                for (let dz = -1; dz <= 1; dz++) {
                    if (dx === 0 && dz === 0) continue
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
export function RealisticTree({ position, scale = 1.0, targetHeight = 9.5, windSpeed = 2.4, season = 'SUMMER', url, objectName, terrainConfig }) {
    const [x, _, z] = position
    const groundY = getTerrainHeight(x, z, terrainConfig)

    return (
        <group position={[x, groundY, z]}>
            <LowPolyModel
                url={url || '/3d/nature_pack/Trees.glb'}
                objectName={objectName}
                targetHeight={targetHeight}
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
 * Grand Ancient Hollow Tree Stump Component (Gamified Mode)
 */
export function VoxelHollowStump({ position = [56, 0, 48], scale = 1.0, terrainConfig }) {
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
        <group position={[gridX, groundY, gridZ]} scale={[0.25, 0.25, 0.25]} frustumCulled={false}>
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
export function PixelCrossMesh({ position, texture, scale = 1.0 }) {
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
export function GamifiedVoxelFlora({ terrainConfig }) {
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
            if (gridX >= 18 && gridX <= 32) continue
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
export function Stump3D({ position, scale = 1.0, terrainConfig }) {
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
 */
export function ScientificTree({ position, scale = 1.0, variant = 0, season = 'SUMMER', terrainConfig }) {
    const [x, _, z] = position
    const groundY = getTerrainHeight(x, z, terrainConfig)
    const trunkHeight = 6.0 * scale
    const crownColor = season === 'WINTER' ? '#cbd5e1' : season === 'AUTUMN' ? '#d97706' : '#15803d'

    return (
        <group position={[x, groundY, z]} frustumCulled={false}>
            <mesh position={[0, 0.02, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                <ringGeometry args={[0.3 * scale, 3.0 * scale, 24]} />
                <meshBasicMaterial color="#0284c7" transparent opacity={0.15} side={THREE.DoubleSide} />
            </mesh>

            <mesh position={[0, trunkHeight / 2, 0]} castShadow receiveShadow frustumCulled={false}>
                <cylinderGeometry args={[0.25 * scale, 0.45 * scale, trunkHeight, 12]} />
                <meshStandardMaterial color="#334155" roughness={0.7} metalness={0.15} side={THREE.FrontSide} depthWrite depthTest />
            </mesh>

            <mesh position={[0, 1.3, 0]} rotation={[-Math.PI / 2, 0, 0]}>
                <torusGeometry args={[0.42 * scale, 0.025 * scale, 6, 16]} />
                <meshBasicMaterial color="#38bdf8" />
            </mesh>

            {variant % 2 === 0 ? (
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

export function getBiomeTreePositions(climateType) {
    if (climateType === 'ARID' || climateType === 'SAVANNA' || climateType === 'DESERT') {
        return [
            { id: 1, pos: [15, 0, 30], scale: 1.0, targetHeight: 8.5, variant: 0, url: '/3d/cactus.obj', objectName: '0_lowpoly_Circle.034' },
            { id: 2, pos: [78, 0, 25], scale: 1.1, targetHeight: 13.5, variant: 1, url: '/3d/nature_pack/Dead Trees.glb', objectName: 'DeadTree_1' },
            { id: 3, pos: [82, 0, 75], scale: 1.0, targetHeight: 9.0, variant: 2, url: '/3d/cactus.obj', objectName: '1_lowpoly_Circle.035' },
            { id: 4, pos: [18, 0, 80], scale: 1.1, targetHeight: 14.5, variant: 0, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_1' },
            { id: 5, pos: [55, 0, 15], scale: 1.15, targetHeight: 13.5, variant: 1, url: '/3d/nature_pack/Dead Trees.glb', objectName: 'DeadTree_3' },
            { id: 6, pos: [88, 0, 88], scale: 1.0, targetHeight: 15.0, variant: 2, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_2' },
        ]
    }
    if (climateType === 'TROPICAL') {
        return [
            { id: 1, pos: [15, 0, 30], scale: 1.05, targetHeight: 15.0, variant: 0, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_1' },
            { id: 2, pos: [78, 0, 25], scale: 1.1, targetHeight: 13.0, variant: 1, url: '/3d/bamboo_set.obj', objectName: 'bamboo_tree_1_Cylinder' },
            { id: 3, pos: [82, 0, 75], scale: 1.0, targetHeight: 15.5, variant: 2, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_3' },
            { id: 4, pos: [18, 0, 80], scale: 1.1, targetHeight: 16.0, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_1' },
            { id: 5, pos: [55, 0, 15], scale: 1.15, targetHeight: 13.5, variant: 1, url: '/3d/bamboo_set.obj', objectName: 'bamboo_tree_2_Cylinder.001' },
            { id: 6, pos: [88, 0, 88], scale: 1.0, targetHeight: 15.5, variant: 2, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_5' },
        ]
    }
    if (climateType === 'MEDITERRANEAN') {
        return [
            { id: 1, pos: [15, 0, 30], scale: 1.0, targetHeight: 14.0, variant: 0, url: '/3d/nature_pack/Maple Trees.glb', objectName: 'MapleTree_1' },
            { id: 2, pos: [78, 0, 25], scale: 1.1, targetHeight: 15.0, variant: 1, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_1' },
            { id: 3, pos: [82, 0, 75], scale: 1.05, targetHeight: 13.5, variant: 2, url: '/3d/nature_pack/Maple Trees.glb', objectName: 'MapleTree_2' },
            { id: 4, pos: [18, 0, 80], scale: 1.1, targetHeight: 14.5, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_4' },
            { id: 5, pos: [55, 0, 15], scale: 1.15, targetHeight: 13.0, variant: 1, url: '/3d/nature_pack/Dead Trees.glb', objectName: 'DeadTree_2' },
            { id: 6, pos: [88, 0, 88], scale: 1.0, targetHeight: 15.5, variant: 2, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_3' },
        ]
    }
    if (climateType === 'ALPINE' || climateType === 'CONTINENTAL') {
        return [
            { id: 1, pos: [15, 0, 30], scale: 1.05, targetHeight: 16.0, variant: 1, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_2' },
            { id: 2, pos: [78, 0, 25], scale: 1.1, targetHeight: 14.0, variant: 2, url: '/3d/nature_pack/Birch Trees.glb', objectName: 'BirchTree_1' },
            { id: 3, pos: [82, 0, 75], scale: 1.05, targetHeight: 15.5, variant: 1, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_4' },
            { id: 4, pos: [18, 0, 80], scale: 1.1, targetHeight: 14.5, variant: 0, url: '/3d/nature_pack/Maple Trees.glb', objectName: 'MapleTree_3' },
            { id: 5, pos: [55, 0, 15], scale: 1.15, targetHeight: 16.5, variant: 1, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_5' },
            { id: 6, pos: [88, 0, 88], scale: 1.0, targetHeight: 14.0, variant: 2, url: '/3d/nature_pack/Birch Trees.glb', objectName: 'BirchTree_3' },
        ]
    }
    return [
        { id: 1, pos: [15, 0, 30], scale: 1.0, targetHeight: 15.0, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_1' },
        { id: 2, pos: [78, 0, 25], scale: 1.1, targetHeight: 16.0, variant: 1, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_2' },
        { id: 3, pos: [82, 0, 75], scale: 1.05, targetHeight: 14.0, variant: 2, url: '/3d/nature_pack/Birch Trees.glb', objectName: 'BirchTree_1' },
        { id: 4, pos: [18, 0, 80], scale: 1.1, targetHeight: 15.0, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_3' },
        { id: 5, pos: [55, 0, 15], scale: 1.15, targetHeight: 16.0, variant: 1, url: '/3d/nature_pack/Pine Trees.glb', objectName: 'PineTree_4' },
        { id: 6, pos: [88, 0, 88], scale: 1.0, targetHeight: 14.5, variant: 2, url: '/3d/nature_pack/Birch Trees.glb', objectName: 'BirchTree_4' },
    ]
}
