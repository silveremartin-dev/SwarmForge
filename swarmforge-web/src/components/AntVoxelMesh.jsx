import React from 'react'

/**
 * Voxel / Gamified Minecraft-style Anatomy for AntMesh.
 */
export default function AntVoxelMesh({ scale, antColor, environmentLighting, carriedItem }) {
    return (
        <group>
            {/* Voxel Head */}
            <mesh position={[0, 0, 0.28 * scale]} castShadow>
                <boxGeometry args={[0.24 * scale, 0.22 * scale, 0.24 * scale]} />
                <meshStandardMaterial color={antColor} roughness={0.85} />
            </mesh>
            {/* Pixel Eyes (Minecraft Spider/Mob Glowing Eyes) */}
            <mesh position={[0.08 * scale, 0.05 * scale, 0.41 * scale]}>
                <boxGeometry args={[0.05 * scale, 0.05 * scale, 0.02 * scale]} />
                <meshBasicMaterial color={environmentLighting?.isNight ? '#ef4444' : '#18181b'} />
            </mesh>
            <mesh position={[-0.08 * scale, 0.05 * scale, 0.41 * scale]}>
                <boxGeometry args={[0.05 * scale, 0.05 * scale, 0.02 * scale]} />
                <meshBasicMaterial color={environmentLighting?.isNight ? '#ef4444' : '#18181b'} />
            </mesh>
            {/* Voxel Mandibles */}
            <mesh position={[0.08 * scale, -0.05 * scale, 0.42 * scale]}>
                <boxGeometry args={[0.06 * scale, 0.05 * scale, 0.12 * scale]} />
                <meshStandardMaterial color="#1e1b4b" roughness={0.7} />
            </mesh>
            <mesh position={[-0.08 * scale, -0.05 * scale, 0.42 * scale]}>
                <boxGeometry args={[0.06 * scale, 0.05 * scale, 0.12 * scale]} />
                <meshStandardMaterial color="#1e1b4b" roughness={0.7} />
            </mesh>
            {/* Voxel Thorax */}
            <mesh position={[0, 0, 0]} castShadow>
                <boxGeometry args={[0.20 * scale, 0.18 * scale, 0.26 * scale]} />
                <meshStandardMaterial color={antColor} roughness={0.85} />
            </mesh>
            {/* Voxel Petiole Waist */}
            <mesh position={[0, 0.02 * scale, -0.16 * scale]}>
                <boxGeometry args={[0.10 * scale, 0.12 * scale, 0.10 * scale]} />
                <meshStandardMaterial color="#3b1d11" roughness={0.9} />
            </mesh>
            {/* Voxel Gaster / Abdomen */}
            <mesh position={[0, 0, -0.38 * scale]} castShadow>
                <boxGeometry args={[0.30 * scale, 0.26 * scale, 0.42 * scale]} />
                <meshStandardMaterial color={antColor} roughness={0.75} />
            </mesh>
            {/* 6 Blocky Stick Legs */}
            <group position={[0, -0.05 * scale, 0]}>
                {/* Left Legs */}
                <mesh position={[0.18 * scale, -0.08 * scale, 0.10 * scale]} rotation={[0, 0, -0.4]}>
                    <boxGeometry args={[0.04 * scale, 0.18 * scale, 0.04 * scale]} />
                    <meshStandardMaterial color="#2d1c0c" roughness={0.9} />
                </mesh>
                <mesh position={[0.20 * scale, -0.08 * scale, 0]} rotation={[0, 0, -0.4]}>
                    <boxGeometry args={[0.04 * scale, 0.18 * scale, 0.04 * scale]} />
                    <meshStandardMaterial color="#2d1c0c" roughness={0.9} />
                </mesh>
                <mesh position={[0.18 * scale, -0.08 * scale, -0.10 * scale]} rotation={[0, 0, -0.4]}>
                    <boxGeometry args={[0.04 * scale, 0.18 * scale, 0.04 * scale]} />
                    <meshStandardMaterial color="#2d1c0c" roughness={0.9} />
                </mesh>
                {/* Right Legs */}
                <mesh position={[-0.18 * scale, -0.08 * scale, 0.10 * scale]} rotation={[0, 0, 0.4]}>
                    <boxGeometry args={[0.04 * scale, 0.18 * scale, 0.04 * scale]} />
                    <meshStandardMaterial color="#2d1c0c" roughness={0.9} />
                </mesh>
                <mesh position={[-0.20 * scale, -0.08 * scale, 0]} rotation={[0, 0, 0.4]}>
                    <boxGeometry args={[0.04 * scale, 0.18 * scale, 0.04 * scale]} />
                    <meshStandardMaterial color="#2d1c0c" roughness={0.9} />
                </mesh>
                <mesh position={[-0.18 * scale, -0.08 * scale, -0.10 * scale]} rotation={[0, 0, 0.4]}>
                    <boxGeometry args={[0.04 * scale, 0.18 * scale, 0.04 * scale]} />
                    <meshStandardMaterial color="#2d1c0c" roughness={0.9} />
                </mesh>
            </group>

            {/* Minecraft Carried Voxel Item */}
            {carriedItem === 'SUGAR_NECTAR' && (
                <mesh position={[0, 0.28 * scale, 0.38 * scale]}>
                    <boxGeometry args={[0.16 * scale, 0.16 * scale, 0.16 * scale]} />
                    <meshStandardMaterial color="#facc15" emissive="#eab308" emissiveIntensity={0.5} roughness={0.2} />
                </mesh>
            )}
            {carriedItem === 'SEEDS' && (
                <mesh position={[0, 0.28 * scale, 0.38 * scale]}>
                    <boxGeometry args={[0.14 * scale, 0.14 * scale, 0.18 * scale]} />
                    <meshStandardMaterial color="#92400e" roughness={0.7} />
                </mesh>
            )}
        </group>
    )
}
