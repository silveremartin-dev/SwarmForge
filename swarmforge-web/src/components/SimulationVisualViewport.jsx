import React, { useRef, useState, useEffect } from 'react'
import { Canvas, useThree } from '@react-three/fiber'
import { OrbitControls, Grid } from '@react-three/drei'
import { useSimulationStore } from '../store/simulationStore'
import Terrarium from './Terrarium'
import PheromoneCloud from './PheromoneCloud'
import WeatherRenderer from './WeatherRenderer'
import UndergroundView from './UndergroundView'
import MinimapOverlay from './MinimapOverlay'
import MultiplayerScoreboardOverlay from './MultiplayerScoreboardOverlay'
import VoxelMouseHoverHUD from './VoxelMouseHoverHUD'
import TrackedAntInspectorHUD from './TrackedAntInspectorHUD'
import ChamberInspectorHUD from './ChamberInspectorHUD'
import SimulationRightSidebar from './SimulationRightSidebar'
import { showToast } from '../store/toastStore'

function CameraController({ resetTrigger, followAntPosition, customTarget }) {
    const { camera } = useThree()
    const controlsRef = useRef()

    useEffect(() => {
        if (resetTrigger > 0) {
            camera.position.set(50, 65, 125)
            camera.lookAt(50, 0, 50)
            if (controlsRef.current) {
                controlsRef.current.target.set(50, 0, 50)
                controlsRef.current.update()
            }
        }
    }, [resetTrigger, camera])

    useEffect(() => {
        if (customTarget && controlsRef.current) {
            const [cx, cy, cz] = customTarget
            controlsRef.current.target.set(cx, cy, cz)
            camera.position.set(cx + 20, cy + 18, cz + 25)
            controlsRef.current.update()
        }
    }, [customTarget, camera])

    useEffect(() => {
        if (followAntPosition && controlsRef.current) {
            const [ax, ay, az] = followAntPosition
            controlsRef.current.target.set(ax, ay, az)
            camera.position.set(ax + 15, ay + 12, az + 20)
            controlsRef.current.update()
        }
    }, [followAntPosition, camera])

    return (
        <OrbitControls
            ref={controlsRef}
            enableDamping
            dampingFactor={0.05}
            target={[50, 0, 50]}
            maxPolarAngle={Math.PI / 2 - 0.02}
            minDistance={4}
            maxDistance={300}
        />
    )
}

function SunLighting({ environment, isDark }) {
    const sunAngle = 0.35
    const sunX = Math.cos((sunAngle - 0.25) * Math.PI * 2) * 60
    const sunY = Math.max(15, Math.sin((sunAngle - 0.25) * Math.PI * 2) * 70)
    const sunZ = 25
    const isNight = (environment?.lightLevel ?? 1.0) < 0.3

    return (
        <>
            <ambientLight intensity={isNight ? 0.2 : 0.5} />
            <hemisphereLight
                skyColor={isNight ? '#0f172a' : '#bfdbfe'}
                groundColor={isNight ? '#020617' : '#64748b'}
                intensity={0.4}
            />
            <directionalLight
                position={[50 + sunX, sunY + 30, 50 + sunZ]}
                intensity={isNight ? 0.1 : 1.2}
                castShadow
                shadow-mapSize={[2048, 2048]}
            />
        </>
    )
}

export default function SimulationVisualViewport() {
    const {
        environment,
        showGrid,
        showPheromones,
        showMinimap,
        showChambers,
        theme,
        ants,
        trackedAntId,
        followAntCamera,
        setFollowAntCamera
    } = useSimulationStore()

    const [resetCamTrigger, setResetCamTrigger] = useState(0)
    const [customTarget, setCustomTarget] = useState(null)
    const [showScoreboard, setShowScoreboard] = useState(true)
    const [isFlashing, setIsFlashing] = useState(false)
    const isDark = theme === 'dark'

    const handleFocusPosition = (x, y, z) => {
        setFollowAntCamera(false)
        setCustomTarget([x, y, z])
        showToast(`📍 Caméra 3D centrée sur (${Math.round(x)}, ${Math.round(z)})`, 'info')
    }

    const handleDoubleClickReset = () => {
        setFollowAntCamera(false)
        setCustomTarget(null)
        setResetCamTrigger(prev => prev + 1)
        showToast('🔄 Caméra 3D réinitialisée à la vue initiale', 'info')
    }

    const currentTrackedAnt = (ants || []).find(a => a.id === trackedAntId)
    const followPos = (followAntCamera && currentTrackedAnt) ? [currentTrackedAnt.x, currentTrackedAnt.y, currentTrackedAnt.z] : null

    return (
        <div
            onDoubleClick={handleDoubleClickReset}
            style={{ width: '100%', height: '100%', position: 'relative', overflow: 'hidden' }}
        >
            {/* Shutter Camera Flash Effect Overlay */}
            {isFlashing && (
                <div style={{
                    position: 'absolute',
                    inset: 0,
                    background: '#ffffff',
                    opacity: 0.85,
                    zIndex: 200,
                    pointerEvents: 'none',
                    animation: 'fadeout 0.35s ease-out forwards'
                }} />
            )}

            {/* 3D Canvas */}
            <Canvas
                shadows
                camera={{ position: [50, 65, 125], fov: 45 }}
                gl={{ preserveDrawingBuffer: true }}
                style={{ width: '100%', height: '100%', background: isDark ? '#0b0f19' : '#e2e8f0' }}
            >
                <SunLighting environment={environment} isDark={isDark} />

                <Terrarium />
                {showPheromones && <PheromoneCloud />}
                <WeatherRenderer />
                {showChambers && <UndergroundView />}

                {showGrid && (
                    <Grid
                        args={[100, 100]}
                        position={[50, -0.01, 50]}
                        cellSize={5}
                        cellThickness={0.5}
                        cellColor={isDark ? '#1e293b' : '#cbd5e1'}
                        sectionSize={10}
                        sectionThickness={1}
                        sectionColor={isDark ? '#334155' : '#94a3b8'}
                    />
                )}

                <CameraController
                    resetTrigger={resetCamTrigger}
                    followAntPosition={followPos}
                    customTarget={customTarget}
                />
            </Canvas>

            {/* 1. Multiplayer Scoreboard HUD Overlay (Top-Left) 1:1 with JavaFX */}
            <MultiplayerScoreboardOverlay
                isVisible={showScoreboard}
                onClose={() => setShowScoreboard(false)}
                onFocusColony={handleFocusPosition}
            />

            {/* 2. Floating Mouse-Hovered Voxel Inspector HUD */}
            <VoxelMouseHoverHUD />

            {/* 3. Collapsible Tracked Ant Inspector HUD (Bottom-Left) */}
            <TrackedAntInspectorHUD />

            {/* 4. Collapsible Subterranean Chamber Inspector HUD (Bottom-Left) */}
            <ChamberInspectorHUD onFocusChamber={handleFocusPosition} />

            {/* 5. Dual Minimap Overlay (Top-Right) */}
            {showMinimap && <MinimapOverlay />}

            {/* 6. Complete Right Sidebar: VCR, Speed, Media, Render Layers, Audio Mixer, Substrates */}
            <SimulationRightSidebar
                onTriggerFlash={() => {
                    setIsFlashing(true)
                    setTimeout(() => setIsFlashing(false), 350)
                }}
            />
        </div>
    )
}
