import { Canvas } from '@react-three/fiber'
import { OrbitControls, Stats, Grid } from '@react-three/drei'
import { useState, useEffect } from 'react'
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
import ToastContainer from './components/ToastContainer'

import GodModePanel from './components/GodModePanel'

// Placeholder for ErrorBoundary
const ErrorBoundary = ({ children }) => {
    return <>{children}</>
}

export default function App() {
    const { connected, connect, disconnect } = useSimulationStore()
    const { environment } = useSimulationStore()
    const [showUnderground, setShowUnderground] = useState(true)
    const [activeMode, setActiveMode] = useState('SIMULATION') // Default to 'SIMULATION' mode for quick access to Simulation Manager

    useEffect(() => {
        connect()
        return () => disconnect()
    }, [])

    // Derived values for lighting
    const sunX = Math.cos((environment.sunAngle - 0.25) * Math.PI * 2) * 50
    const sunY = Math.sin((environment.sunAngle - 0.25) * Math.PI * 2) * 50
    const sunZ = 20

    // Light Color Interpolation
    const isNight = environment.lightLevel < 0.3
    const skyColor = isNight ? '#111122' : '#88ccff'
    const groundColor = isNight ? '#050510' : '#444422'
    const sunIntensity = Math.max(0.1, environment.lightLevel * 1.5)

    return (
        <div style={{ width: '100vw', height: '100vh', background: '#0b0f19', overflow: 'hidden', position: 'relative' }}>
            {/* Top Navigation Bar */}
            <Navbar activeMode={activeMode} setActiveMode={setActiveMode} />

            {/* Global Non-Intrusive Toast Notification System */}
            <ToastContainer />

            <ErrorBoundary>
                {/* Status Indicator (Cleaned without useless button) */}
                <div style={{ position: 'absolute', top: 60, left: 20, zIndex: 100, color: '#fff', background: 'rgba(15, 23, 42, 0.85)', padding: '8px 14px', borderRadius: 8, backdropFilter: 'blur(8px)', border: '1px solid rgba(255,255,255,0.1)', fontSize: 11 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span>Statut:</span>
                        <span style={{ color: connected ? '#4ade80' : '#38bdf8', fontWeight: 'bold' }}>
                            {connected ? '● Connecté (Serveur)' : '● Mode Local'}
                        </span>
                    </div>
                    <div style={{ opacity: 0.85, marginTop: 3 }}>
                        Temps: {environment.timeOfDay} | Lum: {environment.lightLevel.toFixed(2)} | {environment.season} ({environment.temperature?.toFixed(1)}°C)
                    </div>
                </div>

                {!running && tick === 0 && (
                    <div style={{ position: 'absolute', top: '40%', left: '50%', transform: 'translate(-50%, -50%)', zIndex: 120, background: 'rgba(15, 23, 42, 0.92)', border: '1px solid rgba(56, 189, 248, 0.3)', borderRadius: 12, padding: '24px 32px', textAlign: 'center', color: '#fff', boxShadow: '0 20px 40px rgba(0,0,0,0.7)', backdropFilter: 'blur(12px)', maxWidth: 480 }}>
                        <h3 style={{ fontSize: 16, fontWeight: 800, color: '#38bdf8', marginBottom: 8, marginTop: 0 }}>🎬 Vue 3D en Attente de Simulation</h3>
                        <p style={{ fontSize: 12, color: '#94a3b8', lineHeight: 1.5, margin: 0 }}>
                            La vue 3D s'active automatiquement dès le démarrage de la simulation.<br/>
                            Veuillez configurer / peupler un monde puis cliquer sur <strong style={{ color: '#10b981' }}>"▶ LANCER SIMULATION"</strong> dans le Gestionnaire de Simulation.
                        </p>
                    </div>
                )}

                <VRButton />
                <Canvas shadows camera={{ position: [20, 20, 20], fov: 50 }} gl={{ preserveDrawingBuffer: true }}>
                    <XR>
                        <Controllers />
                        <Hands />
                        <ImmersiveControls />

                        {/* Dynamic Environment Lighting */}
                        <ambientLight intensity={Math.max(0.2, environment.lightLevel * 0.4)} />
                        <pointLight
                            position={[sunX, sunY, sunZ]}
                            intensity={sunIntensity}
                            castShadow
                            shadow-mapSize={[2048, 2048]}
                            color={environment.timeOfDay === 'DAWN' ? '#ff9966' : environment.timeOfDay === 'DUSK' ? '#ff7744' : '#ffffff'}
                        />
                        <hemisphereLight skyColor={skyColor} groundColor={groundColor} intensity={Math.max(0.1, environment.lightLevel * 0.6)} />

                        {/* Visual Sun */}
                        <mesh position={[sunX, sunY, sunZ]}>
                            <sphereGeometry args={[2, 16, 16]} />
                            <meshBasicMaterial color={environment.timeOfDay === 'DAWN' || environment.timeOfDay === 'DUSK' ? '#ff4400' : '#ffffaa'} />
                        </mesh>

                        <Terrarium />
                        <PheromoneCloud />
                        <WeatherRenderer />
                        {showUnderground && <UndergroundView />}

                        <Grid
                            args={[100, 100]}
                            position={[50, 0, 50]}
                            cellSize={5}
                            cellThickness={0.5}
                            cellColor="#1a1a2e"
                            sectionSize={10}
                            sectionThickness={1}
                            sectionColor="#2a2a4e"
                        />

                        <OrbitControls
                            enableDamping
                            dampingFactor={0.05}
                            target={[50, 0, 50]}
                        />
                        <Stats />
                    </XR>
                </Canvas>

                {/* 3D Visual View Media Capture Toolbar (Photo HD & Video Recorder with MP4 export) */}
                <ViewportToolbar />
            </ErrorBoundary>

            {/* Mode Specific Panels */}
            {activeMode === 'WORLD_EDITOR' && <WorldEditorPanel />}
            {activeMode === 'CLIMATE_STUDIO' && <ClimateStudioPanel />}
            {activeMode === 'SIMULATION' && (
                <>
                    <ControlPanel />
                    <InspectorPanel />
                    <WeatherControlWidget />
                    <div style={{ position: 'absolute', top: 125, left: 20, width: 330, zIndex: 90, maxHeight: 'calc(100vh - 200px)', overflowY: 'auto' }}>
                        <GodModePanel />
                    </div>
                </>
            )}
        </div>
    )
}
