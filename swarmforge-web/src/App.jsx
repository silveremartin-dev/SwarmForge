import { Canvas, useThree } from '@react-three/fiber'
import { OrbitControls, Stats, Grid } from '@react-three/drei'
import { useState, useEffect, useRef } from 'react'
import { VRButton, XR, Controllers, Hands } from '@react-three/xr'
import Terrarium from './components/Terrarium'
import ControlPanel from './components/ControlPanel'
import InspectorPanel from './components/InspectorPanel'
import { useSimulationStore } from './store/simulationStore'
import ImmersiveControls from './components/ImmersiveControls'
import PheromoneCloud from './components/PheromoneCloud'
import WeatherRenderer from './components/WeatherRenderer'
import UndergroundView from './components/UndergroundView'
import Navbar from './components/Navbar'
import WorldEditorPanel from './components/WorldEditorPanel'
import ClimateStudioPanel from './components/ClimateStudioPanel'
import ViewportToolbar from './components/ViewportToolbar'
import WeatherControlWidget from './components/WeatherControlWidget'
import SimulationLeftSidebar from './components/SimulationLeftSidebar'
import SimulationRightSidebar from './components/SimulationRightSidebar'
import MinimapOverlay from './components/MinimapOverlay'
import ToastContainer from './components/ToastContainer'
import { getTranslation } from './i18n/translations'
import { soundEngine } from './utils/soundEngine'

import GodModePanel from './components/GodModePanel'

// Placeholder for ErrorBoundary
const ErrorBoundary = ({ children }) => {
    return <>{children}</>
}

function ResponsiveOrbitControls() {
    const { cameraResetTrigger } = useSimulationStore()
    const { camera } = useThree()
    const controlsRef = useRef()

    useEffect(() => {
        if (cameraResetTrigger > 0) {
            camera.position.set(50, 65, 125)
            camera.lookAt(50, 0, 50)
            if (controlsRef.current) {
                controlsRef.current.target.set(50, 0, 50)
                controlsRef.current.update()
            }
        }
    }, [cameraResetTrigger, camera])

    return (
        <OrbitControls
            ref={controlsRef}
            enableDamping
            dampingFactor={0.05}
            target={[50, 0, 50]}
            maxPolarAngle={Math.PI / 2 - 0.02}
            minDistance={5}
            maxDistance={250}
        />
    )
}

function SunLighting({ sunPosition, intensity, isNight, timeOfDay }) {
    const lightRef = useRef()
    const targetRef = useRef()

    useEffect(() => {
        if (lightRef.current && targetRef.current) {
            lightRef.current.target = targetRef.current
            lightRef.current.target.updateMatrixWorld()
        }
    }, [])

    return (
        <>
            <object3D ref={targetRef} position={[50, 0, 50]} />
            <directionalLight
                ref={lightRef}
                position={sunPosition}
                intensity={isNight ? 0.05 : intensity}
                castShadow
                shadow-mapSize={[2048, 2048]}
                shadow-camera-left={-65}
                shadow-camera-right={65}
                shadow-camera-top={65}
                shadow-camera-bottom={-65}
                shadow-camera-near={1}
                shadow-camera-far={250}
                shadow-bias={-0.0004}
                color={timeOfDay === 'DAWN' ? '#ff9966' : timeOfDay === 'DUSK' ? '#ff7744' : '#ffffff'}
            />
        </>
    )
}

export default function App() {
    const {
        connected,
        connect,
        disconnect,
        running,
        tick,
        simTimeFormatted,
        speed,
        environment,
        showChamberOverlay,
        lookAndFeel,
        measuredTps,
        stats,
        ants,
        language,
        theme,
        showGrid
    } = useSimulationStore()
    const [activeMode, setActiveMode] = useState('SIMULATION')
    const isDark = theme === 'dark'

    const t = (key, fallback) => getTranslation(language, key, fallback)

    useEffect(() => {
        connect()
        return () => disconnect()
    }, [])

    // Global WebAudio Context Auto-Unlock on first user interaction
    useEffect(() => {
        const unlockAudio = () => {
            soundEngine.ensureContext()
            window.removeEventListener('click', unlockAudio)
            window.removeEventListener('keydown', unlockAudio)
            window.removeEventListener('touchstart', unlockAudio)
        }
        window.addEventListener('click', unlockAudio)
        window.addEventListener('keydown', unlockAudio)
        window.addEventListener('touchstart', unlockAudio)
        return () => {
            window.removeEventListener('click', unlockAudio)
            window.removeEventListener('keydown', unlockAudio)
            window.removeEventListener('touchstart', unlockAudio)
        }
    }, [])

    // Derived values for directional sun & sky lighting (centered on [50, 0, 50])
    const sunAngle = environment?.sunAngle ?? 0.35
    const sunX = Math.cos((sunAngle - 0.25) * Math.PI * 2) * 60
    const sunY = Math.max(15, Math.sin((sunAngle - 0.25) * Math.PI * 2) * 70)
    const sunZ = 25

    // Light Color Interpolation
    const isNight = (environment?.lightLevel ?? 1.0) < 0.3
    const skyColor = isNight ? '#0f172a' : (isDark ? '#93c5fd' : '#bfdbfe')
    const groundColor = isNight ? '#020617' : (isDark ? '#334155' : '#64748b')
    const sunIntensity = Math.max(0.15, (environment?.lightLevel ?? 1.0) * 1.4)

    const antCount = ants?.length ?? stats?.totalPopulation ?? 0

    return (
        <div style={{ width: '100vw', height: '100vh', background: isDark ? '#0b0f19' : '#f1f5f9', overflow: 'hidden', position: 'relative' }}>
            {/* Top Navigation Bar */}
            <Navbar activeMode={activeMode} setActiveMode={setActiveMode} />

            {/* Global Non-Intrusive Toast Notification System */}
            <ToastContainer />

            <ErrorBoundary>
                {/* Status Indicator with Synchronized Real-Time Simulation Clock & TPS / Performance Stats */}
                <div style={{
                    position: 'absolute',
                    top: 60,
                    left: activeMode === 'SIMULATION' || activeMode === 'WORLD_EDITOR' ? 420 : 20,
                    zIndex: 100,
                    color: isDark ? '#fff' : '#0f172a',
                    background: isDark ? 'rgba(15, 23, 42, 0.88)' : 'rgba(255, 255, 255, 0.92)',
                    padding: '8px 14px',
                    borderRadius: 8,
                    backdropFilter: 'blur(8px)',
                    border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
                    fontSize: 11,
                    transition: 'all 0.2s ease',
                    boxShadow: '0 4px 14px rgba(0,0,0,0.3)'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
                        <span>{t('statusLabel', 'Statut :')}</span>
                        <span style={{ color: connected ? '#10b981' : '#38bdf8', fontWeight: 'bold' }}>
                            {connected ? `● ${t('statusConnected', 'Connecté (Serveur)')}` : `● ${t('statusStandalone', 'Mode 1:1 Autonome')}`}
                        </span>
                        <span style={{ color: '#64748b' }}>•</span>
                        <span style={{ color: isDark ? '#38bdf8' : '#0284c7', fontWeight: 800 }}>
                            ⏱️ {simTimeFormatted || 'J+0 00:00:00'}
                        </span>
                        <span style={{ color: isDark ? '#94a3b8' : '#64748b', fontSize: 10 }}>
                            ({speed.toFixed(1)}x)
                        </span>
                        <span style={{ color: '#64748b' }}>•</span>
                        <span style={{ color: '#f59e0b', fontWeight: 700 }} title="Ticks Par Seconde réels exécutés par le moteur de simulation">
                            ⚡ TPS: {measuredTps ?? 0} {t('tickUnit', 'ticks/s')}
                        </span>
                        <span style={{ color: '#64748b' }}>•</span>
                        <span style={{ color: '#a855f7', fontWeight: 700 }}>
                            🐜 {t('popLabel', 'Pop')}: {antCount}
                        </span>
                    </div>
                    <div style={{ opacity: 0.9, marginTop: 3, color: isDark ? '#cbd5e1' : '#475569' }}>
                        Ciel: {environment.timeOfDay} | Lum: {environment.lightLevel.toFixed(2)} | Temp: {environment.temperature?.toFixed(1)}°C ({environment.weatherState || 'Dégagé'})
                    </div>
                </div>

                {!running && tick === 0 && (
                    <div style={{ position: 'absolute', top: '40%', left: '55%', transform: 'translate(-50%, -50%)', zIndex: 80, background: isDark ? 'rgba(15, 23, 42, 0.92)' : 'rgba(255, 255, 255, 0.95)', border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)', borderRadius: 12, padding: '24px 32px', textAlign: 'center', color: isDark ? '#fff' : '#0f172a', boxShadow: '0 20px 40px rgba(0,0,0,0.4)', backdropFilter: 'blur(12px)', maxWidth: 480 }}>
                        <h3 style={{ fontSize: 16, fontWeight: 800, color: isDark ? '#38bdf8' : '#0284c7', marginBottom: 8, marginTop: 0 }}>🎬 Vue 3D Prête (Horloge 1:1)</h3>
                        <p style={{ fontSize: 12, color: isDark ? '#94a3b8' : '#475569', lineHeight: 1.5, margin: 0 }}>
                            La simulation et le monde sont initialisés.<br/>
                            Cliquez sur <strong style={{ color: '#10b981' }}>"▶ LANCER SIMULATION"</strong> dans le Panneau de Contrôle pour démarrer,<br/>
                            ou <strong>maintenez le clic</strong> pour le mode <em>Pas-à-Pas (Step-by-Step)</em>.
                        </p>
                    </div>
                )}

                <VRButton />
                <Canvas shadows camera={{ position: [50, 65, 125], fov: 45 }} gl={{ preserveDrawingBuffer: true }}>
                    <XR>
                        <Controllers />
                        <Hands />
                        <ImmersiveControls />

                        {/* Ambient & Sky Illumination */}
                        <ambientLight intensity={Math.max(0.3, (environment?.lightLevel ?? 1.0) * 0.45)} />
                        <hemisphereLight skyColor={skyColor} groundColor={groundColor} intensity={Math.max(0.2, (environment?.lightLevel ?? 1.0) * 0.5)} />

                        {/* Sun Directional Light with calibrated parallel shadow projection centered on [50, 0, 50] */}
                        <SunLighting
                            sunPosition={[50 + sunX, sunY + 30, 50 + sunZ]}
                            intensity={sunIntensity}
                            isNight={isNight}
                            timeOfDay={environment.timeOfDay}
                        />

                        {/* Night Moon Light */}
                        {isNight && (
                            <directionalLight
                                position={[30, 60, 40]}
                                intensity={0.25}
                                color="#818cf8"
                                castShadow
                                shadow-mapSize={[1024, 1024]}
                                shadow-camera-left={-60}
                                shadow-camera-right={60}
                                shadow-camera-top={60}
                                shadow-camera-bottom={-60}
                            />
                        )}

                        {/* Visual Sun Sphere (Non-Gamified modes) */}
                        {lookAndFeel !== 'GAMING' && (
                            <mesh position={[50 + sunX, sunY + 30, 50 + sunZ]}>
                                <sphereGeometry args={[2.5, 16, 16]} />
                                <meshBasicMaterial color={environment.timeOfDay === 'DAWN' || environment.timeOfDay === 'DUSK' ? '#ff4400' : '#ffffaa'} />
                            </mesh>
                        )}

                        <Terrarium />
                        <PheromoneCloud />
                        <WeatherRenderer />
                        {showChamberOverlay && <UndergroundView />}

                        {(showGrid || activeMode === 'WORLD_EDITOR') && (
                            <Grid
                                args={[100, 100]}
                                position={[50, -0.01, 50]}
                                cellSize={5}
                                cellThickness={0.5}
                                cellColor={isDark ? '#1a1a2e' : '#cbd5e1'}
                                sectionSize={10}
                                sectionThickness={1}
                                sectionColor={isDark ? '#2a2a4e' : '#94a3b8'}
                            />
                        )}

                        <ResponsiveOrbitControls />
                        <Stats />
                    </XR>
                </Canvas>

                {/* 3D Visual View Media Capture Toolbar */}
                <ViewportToolbar />
            </ErrorBoundary>

            {/* Mode Specific Panels */}
            {activeMode === 'WORLD_EDITOR' && <WorldEditorPanel />}
            {activeMode === 'CLIMATE_STUDIO' && <ClimateStudioPanel />}
            {activeMode === 'SIMULATION' && (
                <>
                    <SimulationLeftSidebar />
                    <SimulationRightSidebar />
                    <InspectorPanel />
                    <WeatherControlWidget />
                    <MinimapOverlay />
                </>
            )}
        </div>
    )
}
