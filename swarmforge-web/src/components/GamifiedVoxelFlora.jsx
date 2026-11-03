import React, { useMemo } from 'react'
import * as THREE from 'three'
import { getTerrainHeight } from '../utils/terrainUtils'
import {
    getPixelPoppyTexture,
    getPixelDandelionTexture,
    getPixelTallGrassTexture
} from '../utils/pixelTextures'

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
