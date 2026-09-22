import React, { useRef, useState, useEffect, useMemo, useCallback } from 'react'
import * as THREE from 'three'
import { Canvas, useThree, useFrame } from '@react-three/fiber'
import { OrbitControls, Grid } from '@react-three/drei'
import { useSimulationStore } from '../store/simulationStore'
import { getTerrainHeight } from '../utils/terrainUtils'
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
    const { cameraFollowMode, selectedEntity, trackedAntData, customCameraTarget, setCustomCameraTarget } = useSimulationStore()

    const ant = trackedAntData || selectedEntity

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
        const target = customTarget || customCameraTarget
        if (target && controlsRef.current) {
            const [cx, cy, cz] = target
            controlsRef.current.target.set(cx, cy, cz)
            camera.position.set(cx + 20, cy + 18, cz + 25)
            controlsRef.current.update()
            if (customCameraTarget) {
                setCustomCameraTarget(null)
            }
        }
    }, [customTarget, customCameraTarget, camera, setCustomCameraTarget])

    useFrame(() => {
        if (!ant || !cameraFollowMode) return

        const ax = ant.x ?? 50
        const az = ant.z !== undefined ? ant.z : (ant.y ?? 50)
        const ay = (ant.y !== undefined && ant.z !== undefined ? ant.y : 0.15)
        const heading = ant.heading !== undefined ? ant.heading : 0

        if (cameraFollowMode === 'TPS') {
            const camDist = 12
            const camHeight = 7
            const targetX = ax - Math.sin(heading) * camDist
            const targetZ = az - Math.cos(heading) * camDist
            const targetY = ay + camHeight

            camera.position.lerp(new THREE.Vector3(targetX, targetY, targetZ), 0.08)
            if (controlsRef.current) {
                controlsRef.current.target.lerp(new THREE.Vector3(ax, ay + 0.5, az), 0.1)
                controlsRef.current.update()
            }
        } else if (cameraFollowMode === 'FPS') {
            camera.position.set(ax, ay + 0.35, az)
            const lookX = ax + Math.sin(heading) * 10
            const lookZ = az + Math.cos(heading) * 10
            if (controlsRef.current) {
                controlsRef.current.target.set(lookX, ay + 0.35, lookZ)
                controlsRef.current.update()
            }
        }
    })

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

/**
 * 2D Screen-space pixel picking handler (1:1 with heavy client WorldEditorPane.java).
 * Projects ant/chamber 3D coordinates to screen pixels and matches clicks within 25-30px.
 */
function ScreenSpaceInteractionHandler() {
    const { camera, size, gl } = useThree()
    const {
        ants,
        nests,
        terrainConfig,
        setSelectedEntity,
        setSelectedChamber
    } = useSimulationStore()

    const tempVec = useMemo(() => new THREE.Vector3(), [])

    useEffect(() => {
        const dom = gl.domElement
        if (!dom) return

        const handlePointerMove = (e) => {
            const rect = dom.getBoundingClientRect()
            const mouseX = e.clientX - rect.left
            const mouseY = e.clientY - rect.top

            // 1. Check Ants (Screen-Space threshold: 25px, 1:1 with heavy client)
            let closestAnt = null
            let minAntDistSq = 25 * 25
            if (ants && ants.length > 0) {
                for (let i = 0; i < ants.length; i++) {
                    const a = ants[i]
                    const ax = a.x ?? 50
                    const az = a.z !== undefined ? a.z : (a.y ?? 50)
                    const ay = getTerrainHeight(ax, az, terrainConfig) + 0.15

                    tempVec.set(ax, ay, az).project(camera)
                    if (tempVec.z < 1.0) {
                        const sx = ((tempVec.x + 1) * size.width) / 2
                        const sy = ((-tempVec.y + 1) * size.height) / 2
                        const dSq = (sx - mouseX) * (sx - mouseX) + (sy - mouseY) * (sy - mouseY)
                        if (dSq < minAntDistSq) {
                            minAntDistSq = dSq
                            closestAnt = a
                        }
                    }
                }
            }

            if (closestAnt) {
                dom.style.cursor = 'pointer'
                return
            }

            // 2. Check Chambers (Screen-Space threshold: 30px, 1:1 with heavy client)
            let closestChamber = null
            let minChamberDistSq = 30 * 30
            if (nests && nests.length > 0) {
                for (const nest of nests) {
                    if (nest.chambers) {
                        for (const ch of nest.chambers) {
                            const cx = ch.x ?? nest.x ?? 50
                            const cy = ch.y ?? -1.2
                            const cz = ch.z ?? nest.z ?? 50

                            tempVec.set(cx, cy, cz).project(camera)
                            if (tempVec.z < 1.0) {
                                const sx = ((tempVec.x + 1) * size.width) / 2
                                const sy = ((-tempVec.y + 1) * size.height) / 2
                                const dSq = (sx - mouseX) * (sx - mouseX) + (sy - mouseY) * (sy - mouseY)
                                if (dSq < minChamberDistSq) {
                                    minChamberDistSq = dSq
                                    closestChamber = ch
                                }
                            }
                        }
                    }
                }
            }

            if (closestChamber) {
                dom.style.cursor = 'pointer'
                return
            }

            dom.style.cursor = 'default'
        }

        const handlePointerDown = (e) => {
            if (e.button !== 0) return
            const rect = dom.getBoundingClientRect()
            const mouseX = e.clientX - rect.left
            const mouseY = e.clientY - rect.top

            // 1. Check Ants (Screen-Space 25px threshold)
            let closestAnt = null
            let minAntDistSq = 25 * 25
            if (ants && ants.length > 0) {
                for (let i = 0; i < ants.length; i++) {
                    const a = ants[i]
                    const ax = a.x ?? 50
                    const az = a.z !== undefined ? a.z : (a.y ?? 50)
                    const ay = getTerrainHeight(ax, az, terrainConfig) + 0.15

                    tempVec.set(ax, ay, az).project(camera)
                    if (tempVec.z < 1.0) {
                        const sx = ((tempVec.x + 1) * size.width) / 2
                        const sy = ((-tempVec.y + 1) * size.height) / 2
                        const dSq = (sx - mouseX) * (sx - mouseX) + (sy - mouseY) * (sy - mouseY)
                        if (dSq < minAntDistSq) {
                            minAntDistSq = dSq
                            closestAnt = a
                        }
                    }
                }
            }

            if (closestAnt) {
                setSelectedEntity(closestAnt)
                return
            }

            // 2. Check Chambers (Screen-Space 30px threshold)
            let closestChamber = null
            let minChamberDistSq = 30 * 30
            if (nests && nests.length > 0) {
                for (const nest of nests) {
                    if (nest.chambers) {
                        for (const ch of nest.chambers) {
                            const cx = ch.x ?? nest.x ?? 50
                            const cy = ch.y ?? -1.2
                            const cz = ch.z ?? nest.z ?? 50

                            tempVec.set(cx, cy, cz).project(camera)
                            if (tempVec.z < 1.0) {
                                const sx = ((tempVec.x + 1) * size.width) / 2
                                const sy = ((-tempVec.y + 1) * size.height) / 2
                                const dSq = (sx - mouseX) * (sx - mouseX) + (sy - mouseY) * (sy - mouseY)
                                if (dSq < minChamberDistSq) {
                                    minChamberDistSq = dSq
                                    closestChamber = { ...ch, nestName: nest.name, colonyId: nest.colonyId }
                                }
                            }
                        }
                    }
                }
            }

            if (closestChamber) {
                setSelectedChamber(closestChamber)
                return
            }
        }

        dom.addEventListener('pointermove', handlePointerMove)
        dom.addEventListener('pointerdown', handlePointerDown)

        return () => {
            dom.removeEventListener('pointermove', handlePointerMove)
            dom.removeEventListener('pointerdown', handlePointerDown)
        }
    }, [ants, nests, camera, size, gl, terrainConfig, setSelectedEntity, setSelectedChamber])

    return null
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

                <ScreenSpaceInteractionHandler />

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

            {/* 2. Floating Mouse-Hovered Voxel Inspector HUD (1:1 with WorldEditorPane.java) */}
            <VoxelMouseHoverHUD />

            {/* 3. Tracked Ant Inspector HUD (Bottom-Left 1:1 with TrackedAntPane.java) */}
            <TrackedAntInspectorHUD onFocusAnt={handleFocusPosition} />

            {/* 4. Subterranean Chamber Inspector HUD (Bottom-Left 1:1 with ChamberInfoPane.java) */}
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
