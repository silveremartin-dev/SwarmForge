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

// Placeholder for ErrorBoundary, assuming it's defined elsewhere or will be added
const ErrorBoundary = ({ children }) => {
    return <>{children}</>
}

export default function App() {
    const { connected, connect, disconnect } = useSimulationStore()
    const { environment } = useSimulationStore()
    const [showUnderground, setShowUnderground] = useState(true)

    useEffect(() => {
        connect()
        return () => disconnect()
    }, [])

    // Derived values for lighting
    const sunX = Math.cos((environment.sunAngle - 0.25) * Math.PI * 2) * 50
    const sunY = Math.sin((environment.sunAngle - 0.25) * Math.PI * 2) * 50
    const sunZ = 20

    // Light Color Interpolation (Basic)
    const isNight = environment.lightLevel < 0.3
    const skyColor = isNight ? '#111122' : '#88ccff'
    const groundColor = isNight ? '#050510' : '#444422'
    const sunIntensity = Math.max(0.1, environment.lightLevel * 1.5)

    return (
        <div style={{ width: '100vw', height: '100vh', background: '#111' }}>
            <ErrorBoundary>
                <div style={{ position: 'absolute', top: 10, left: 10, zIndex: 100, color: '#fff' }}>
                    Status: <span style={{ color: connected ? '#4f4' : '#f44' }}>
                        {connected ? 'Connected' : 'Disconnected'}
                    </span>
                    <div style={{ fontSize: 12, opacity: 0.7 }}>
                        Time: {environment.timeOfDay} (Light: {environment.lightLevel.toFixed(2)})
                    </div>
                    <div style={{ fontSize: 12, opacity: 0.7 }}>
                        {environment.season} | {environment.temperature?.toFixed(1)}°C | Rain: {environment.rainIntensity?.toFixed(1)}mm
                    </div>
                    <button onClick={() => setShowUnderground(!showUnderground)} style={{ marginTop: 5, fontSize: 10, padding: '2px 5px', background: '#333', color: '#fff', border: '1px solid #555' }}>
                        {showUnderground ? 'Hide Nest' : 'Show Nest'}
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
        </div>
    )
}

{/* Control Panel */ }
            <ControlPanel />
            <InspectorPanel />
        </div >
    )
}
