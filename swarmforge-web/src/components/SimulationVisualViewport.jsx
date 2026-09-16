import React, { useRef, useState, useEffect } from 'react'
import { Canvas, useThree } from '@react-three/fiber'
import { OrbitControls, Grid } from '@react-three/drei'
import {
    RotateCcw,
    X,
    Crosshair,
    BookOpen
} from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import Terrarium from './Terrarium'
import PheromoneCloud from './PheromoneCloud'
import WeatherRenderer from './WeatherRenderer'
import UndergroundView from './UndergroundView'
import MinimapOverlay from './MinimapOverlay'
import MultiplayerScoreboardOverlay from './MultiplayerScoreboardOverlay'
import LegendGlossaryModal from './LegendGlossaryModal'
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
        trackedAntData,
        setTrackedAntId,
        followAntCamera,
        setFollowAntCamera
    } = useSimulationStore()

    const [resetCamTrigger, setResetCamTrigger] = useState(0)
    const [customTarget, setCustomTarget] = useState(null)
    const [showScoreboard, setShowScoreboard] = useState(true)
    const [showGlossaryModal, setShowGlossaryModal] = useState(false)
    const [isFlashing, setIsFlashing] = useState(false)
    const isDark = theme === 'dark'

    const handleFocusColony = (x, y, z) => {
        setFollowAntCamera(false)
        setCustomTarget([x, y, z])
        showToast(`📍 Caméra 3D centrée sur la colonie (${Math.round(x)}, ${Math.round(z)})`, 'info')
    }

    const currentTrackedAnt = ants.find(a => a.id === trackedAntId)
    const followPos = (followAntCamera && currentTrackedAnt) ? [currentTrackedAnt.x, currentTrackedAnt.y, currentTrackedAnt.z] : null

    return (
        <div style={{ width: '100%', height: '100%', position: 'relative', overflow: 'hidden' }}>
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

            {/* Multiplayer Scoreboard HUD Overlay (Top-Left) 1:1 with JavaFX */}
            <MultiplayerScoreboardOverlay
                isVisible={showScoreboard}
                onClose={() => setShowScoreboard(false)}
                onFocusColony={handleFocusColony}
            />

            {/* Tracked Ant HUD Overlay (Bottom-Left) 1:1 with JavaFX TrackedAntPane */}
            {trackedAntData && (
                <div style={{
                    position: 'absolute',
                    bottom: 20,
                    left: 20,
                    background: isDark ? 'rgba(15, 23, 42, 0.92)' : 'rgba(255, 255, 255, 0.95)',
                    border: isDark ? '1px solid rgba(56, 189, 248, 0.4)' : '1px solid rgba(56, 189, 248, 0.6)',
                    borderRadius: 10,
                    padding: '12px 16px',
                    color: isDark ? '#fff' : '#0f172a',
                    zIndex: 100,
                    backdropFilter: 'blur(10px)',
                    boxShadow: '0 8px 24px rgba(0,0,0,0.3)',
                    maxWidth: 320,
                    fontSize: 11
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8, borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: 6 }}>
                        <span style={{ fontWeight: 800, color: '#38bdf8', display: 'flex', alignItems: 'center', gap: 6 }}>
                            🐜 {trackedAntData.id}
                        </span>
                        <button
                            onClick={() => setTrackedAntId(null)}
                            style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: 2 }}
                        >
                            <X size={14} />
                        </button>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div><strong>Colonie :</strong> {trackedAntData.colonyName}</div>
                        <div><strong>Caste & Tâche :</strong> <span style={{ color: '#10b981' }}>{trackedAntData.caste} ({trackedAntData.task})</span></div>
                        <div><strong>Santé :</strong> {trackedAntData.health.toFixed(0)}% | <strong>Énergie :</strong> {trackedAntData.energy.toFixed(0)}%</div>
                        <div><strong>Position :</strong> ({trackedAntData.x.toFixed(1)}, {trackedAntData.z.toFixed(1)})</div>

                        <div style={{ marginTop: 6, display: 'flex', gap: 6 }}>
                            <button
                                onClick={() => setFollowAntCamera(!followAntCamera)}
                                style={{
                                    background: followAntCamera ? '#0284c7' : 'transparent',
                                    color: followAntCamera ? '#fff' : (isDark ? '#38bdf8' : '#0284c7'),
                                    border: '1px solid #0284c7',
                                    borderRadius: 5,
                                    padding: '4px 8px',
                                    fontSize: 10,
                                    fontWeight: 700,
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: 4
                                }}
                            >
                                <Crosshair size={12} /> {followAntCamera ? 'Caméra Fixée' : 'Suivre l\'Insecte'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Quick Viewport Floating Tools Bar (Bottom Center) */}
            <div style={{
                position: 'absolute',
                bottom: 16,
                left: '50%',
                transform: 'translateX(-50%)',
                background: isDark ? 'rgba(15, 23, 42, 0.88)' : 'rgba(255, 255, 255, 0.92)',
                border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
                borderRadius: 10,
                padding: '6px 14px',
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                backdropFilter: 'blur(10px)',
                boxShadow: '0 8px 24px rgba(0,0,0,0.3)',
                zIndex: 50
            }}>
                {/* Reset Camera */}
                <button
                    onClick={() => {
                        setFollowAntCamera(false)
                        setCustomTarget(null)
                        setResetCamTrigger(prev => prev + 1)
                    }}
                    title="Réinitialiser la caméra 3D"
                    style={{
                        background: 'transparent',
                        border: 'none',
                        color: isDark ? '#cbd5e1' : '#475569',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 5,
                        fontSize: 11,
                        fontWeight: 700
                    }}
                >
                    <RotateCcw size={14} color="#38bdf8" /> Vue Initiale
                </button>

                <div style={{ width: 1, height: 16, background: isDark ? '#334155' : '#cbd5e1' }} />

                {/* Open Legend / Glossary Guide */}
                <button
                    onClick={() => setShowGlossaryModal(true)}
                    title="Ouvrir le guide scientifique et la légende"
                    style={{
                        background: 'transparent',
                        border: 'none',
                        color: isDark ? '#cbd5e1' : '#475569',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 5,
                        fontSize: 11,
                        fontWeight: 700
                    }}
                >
                    <BookOpen size={14} color="#f59e0b" /> Guide & Lexique
                </button>
            </div>

            {/* Minimap Overlay (Top-Right) */}
            {showMinimap && <MinimapOverlay />}

            {/* Complete Right Sidebar: VCR, Speed, Media, Render Layers, Audio Mixer, Substrates */}
            <SimulationRightSidebar
                onTriggerFlash={() => {
                    setIsFlashing(true)
                    setTimeout(() => setIsFlashing(false), 350)
                }}
            />

            {/* Scientific Guide & Legend Modal */}
            <LegendGlossaryModal
                isOpen={showGlossaryModal}
                onClose={() => setShowGlossaryModal(false)}
            />
        </div>
    )
}

