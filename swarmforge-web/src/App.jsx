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

// Placeholder for ErrorBoundary
const ErrorBoundary = ({ children }) => {
    return <>{children}</>
}

export default function App() {
    const { connected, connect, disconnect } = useSimulationStore()
    const { environment } = useSimulationStore()
    const [showUnderground, setShowUnderground] = useState(true)
    const [activeMode, setActiveMode] = useState('WORLD_EDITOR') // 'WORLD_EDITOR', 'CLIMATE_STUDIO', 'SIMULATION'

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

            <ErrorBoundary>
                {/* Status Indicator */}
                <div style={{ position: 'absolute', top: 60, right: 20, zIndex: 100, color: '#fff', background: 'rgba(0,0,0,0.5)', padding: '8px 12px', borderRadius: 8, backdropFilter: 'blur(8px)', fontSize: 12 }}>
                    <div>
                        Status: <span style={{ color: connected ? '#4ade80' : '#f87171', fontWeight: 'bold' }}>
                            {connected ? '● Connecté (Serveur)' : '○ Disconnecté (Hors ligne)'}
                        </span>
                    </div>
                    <div style={{ opacity: 0.7, marginTop: 2 }}>
                        Temps: {environment.timeOfDay} (Luminosité: {environment.lightLevel.toFixed(2)})
                    </div>
                    <div style={{ opacity: 0.7 }}>
                        {environment.season} | {environment.temperature?.toFixed(1)}°C | Pluie: {environment.rainIntensity?.toFixed(1)}mm
                    </div>
                    <button onClick={() => setShowUnderground(!showUnderground)} style={{ marginTop: 6, fontSize: 10, padding: '3px 8px', background: '#334155', color: '#fff', border: '1px solid #475569', borderRadius: 4, cursor: 'pointer' }}>
                        {showUnderground ? 'Masquer Nid Sous-terrain' : 'Afficher Nid Sous-terrain'}
                    </button>
                </div>

                <VRButton />
                <Canvas shadows camera={{ position: [20, 20, 20], fov: 50 }}>
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
            </ErrorBoundary>

            {/* Mode Specific Panels */}
            {activeMode === 'WORLD_EDITOR' && <WorldEditorPanel />}
            {activeMode === 'CLIMATE_STUDIO' && <ClimateStudioPanel />}
            {activeMode === 'SIMULATION' && (
                <>
                    <ControlPanel />
                    <InspectorPanel />
                </>
            )}
        </div>
    )
}

