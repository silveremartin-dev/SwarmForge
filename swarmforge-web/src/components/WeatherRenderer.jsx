import { useRef, useMemo, useState, useEffect } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'
import { soundEngine } from '../utils/soundEngine'

/**
 * WeatherRenderer Component
 * Renders interactive 3D environmental weather effects & solar/lunar mechanics:
 * - Soleil (Sun mesh, solar trajectory East -> South -> West, facade insolation North vs South)
 * - Lune (3D Moon mesh & pale blue lunar crepuscular lighting during night)
 * - Éclairs (Instant visual lightning bolt flash + distance-delayed thunder sound propagation)
 * - Nuages (3D drifting volumetric cloud deck reacting to wind)
 * - Pluie / Neige / Grêle (Precipitation particle engine)
 * - Brouillard & Vent (Volumetric fog & atmospheric dust stream)
 * - Vision Nocturne (Night vision soft ambient illumination toggle)
 */
export default function WeatherRenderer() {
    const { environment, weatherToggles, environmentLighting, climateEngine } = useSimulationStore()

    // Environment parameters with fallbacks
    const temp = environmentLighting?.currentCalculatedTempC ?? environment.temperature ?? 20
    const humidity = climateEngine?.cloudCover ? Math.floor(climateEngine.cloudCover * 100) : (environment.humidity ?? 50)
    const intensity = climateEngine?.precipitationMm ?? environment.rainIntensity ?? 0
    const windSpeed = climateEngine?.windSpeedMs ?? environment.windSpeed ?? 5
    const weatherState = environment.weatherState || (intensity > 15 ? 'TEMPEST' : intensity > 5 ? 'THUNDERSTORM' : intensity > 0.5 ? 'RAIN' : temp <= 0 ? 'SNOW' : 'CLEAR')
    const isNight = environmentLighting?.isNight ?? (environment.lightLevel !== undefined ? environment.lightLevel < 0.3 : false)

    // Display Toggles
    const {
        showSun = true,
        showLightning = true,
        showClouds = true,
        showPrecipitation = true,
        showFog = true,
        showWindDust = true,
        nightVision = false,
        lightningTrigger = 0,
    } = weatherToggles || {}

    // ── 1. SOLAR & LUNAR CELESTIAL DYNAMICS (Driven by Real Astronomical Math) ─────
    const elevationRad = ((environmentLighting?.sunElevationDeg ?? 42) * Math.PI) / 180.0
    const azimuthRad = ((environmentLighting?.sunAzimuthDeg ?? 145) * Math.PI) / 180.0

    // Sun 3D Trajectory (Centered on map [50, 0, 50], radius 70m)
    const sunX = 50 + Math.cos(elevationRad) * Math.sin(azimuthRad) * 75
    const sunY = Math.sin(elevationRad) * 65
    const sunZ = 50 + Math.cos(elevationRad) * Math.cos(azimuthRad) * 75

    // Moon 3D Trajectory (Opposite position on celestial sphere)
    const moonX = 100 - sunX
    const moonY = -sunY
    const moonZ = 100 - sunZ

    // ── 2. PRECIPITATION ENGINE (Rain, Snow, Hail) ─────────────────────────────
    const precipRef = useRef()
    const maxParticles = 3000
    const isSnow = temp <= 0 || weatherState === 'SNOW' || weatherState === 'BLIZZARD'
    const isHail = weatherState === 'HAIL'

    const particles = useMemo(() => {
        const positions = new Float32Array(maxParticles * 3)
        const velocities = new Float32Array(maxParticles * 3)

        for (let i = 0; i < maxParticles; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 120
            positions[i * 3 + 1] = Math.random() * 60
            positions[i * 3 + 2] = (Math.random() - 0.5) * 120

            velocities[i * 3] = (Math.random() - 0.5) * 0.2
            velocities[i * 3 + 1] = -0.6 - Math.random() * 0.8
            velocities[i * 3 + 2] = (Math.random() - 0.5) * 0.2
        }
        return { positions, velocities }
    }, [])

    useFrame((state, delta) => {
        if (!precipRef.current || !showPrecipitation || (intensity < 0.1 && !isSnow && !isHail)) return

        const positions = precipRef.current.geometry.attributes.position.array
        const windX = windSpeed * 0.15
        const fallSpeedMult = isSnow ? 0.25 : isHail ? 1.8 : 1.0

        for (let i = 0; i < maxParticles; i++) {
            positions[i * 3] += (windX + particles.velocities[i * 3]) * delta * 10
            positions[i * 3 + 1] += particles.velocities[i * 3 + 1] * fallSpeedMult * delta * 25

            if (positions[i * 3 + 1] < 0) {
                positions[i * 3 + 1] = 50 + Math.random() * 10
                positions[i * 3] = (Math.random() - 0.5) * 120
                positions[i * 3 + 2] = (Math.random() - 0.5) * 120
            }

            if (positions[i * 3] > 60) positions[i * 3] -= 120
            if (positions[i * 3] < -60) positions[i * 3] += 120
            if (positions[i * 3 + 2] > 60) positions[i * 3 + 2] -= 120
            if (positions[i * 3 + 2] < -60) positions[i * 3 + 2] += 120
        }

        precipRef.current.geometry.attributes.position.needsUpdate = true
    })

    // ── 3. LIGHTNING & SPEED OF SOUND PROPAGATION DELAY ───────────────────────
    const [lightningActive, setLightningActive] = useState(false)
    const [lightningMesh, setLightningMesh] = useState(null)
    const prevTriggerRef = useRef(lightningTrigger)

    useEffect(() => {
        const isStorm = weatherState === 'THUNDERSTORM' || weatherState === 'TEMPEST' || weatherState === 'HAIL'
        const manualTriggered = lightningTrigger !== prevTriggerRef.current
        prevTriggerRef.current = lightningTrigger

        if ((isStorm || manualTriggered) && showLightning) {
            flashLightning()
        }
    }, [lightningTrigger, weatherState, showLightning])

    // Periodic auto-lightning in storms
    useEffect(() => {
        const isStorm = weatherState === 'THUNDERSTORM' || weatherState === 'TEMPEST'
        if (!isStorm || !showLightning) return

        const interval = setInterval(() => {
            if (Math.random() > 0.4) {
                flashLightning()
            }
        }, 4500)

        return () => clearInterval(interval)
    }, [weatherState, showLightning])

    const flashLightning = () => {
        // 1. Instant visual lightning flash (t = 0)
        setLightningActive(true)

        const startX = (Math.random() - 0.5) * 70
        const startZ = (Math.random() - 0.5) * 70
        const points = []
        let currentPos = new THREE.Vector3(startX, 55, startZ)
        points.push(currentPos.clone())

        while (currentPos.y > 0) {
            currentPos.y -= 3 + Math.random() * 4
            currentPos.x += (Math.random() - 0.5) * 7
            currentPos.z += (Math.random() - 0.5) * 7
            points.push(currentPos.clone())
        }

        const geometry = new THREE.BufferGeometry().setFromPoints(points)
        setLightningMesh(geometry)

        setTimeout(() => {
            setLightningActive(false)
        }, 180)

        // 2. Physical Speed of Sound Audio Propagation Delay (v = 343 m/s)
        const distFromCenter = Math.sqrt(startX * startX + startZ * startZ)
        const soundDelayMs = 350 + Math.floor((distFromCenter / 50) * 2000)

        setTimeout(() => {
            soundEngine.ensureContext()
            soundEngine.triggerThunder()
        }, soundDelayMs)
    }

    // ── 4. CLOUDS DECK (3D Volumetric Drifting Clouds) ─────────────────────────
    const cloudGroupRef = useRef()
    const cloudCount = 14

    const cloudPuffs = useMemo(() => {
        const clouds = []
        for (let c = 0; c < cloudCount; c++) {
            const puffs = []
            const cx = (Math.random() - 0.5) * 110
            const cy = 35 + Math.random() * 15
            const cz = (Math.random() - 0.5) * 110
            const puffCount = 5 + Math.floor(Math.random() * 6)

            for (let p = 0; p < puffCount; p++) {
                puffs.push({
                    pos: [
                        cx + (Math.random() - 0.5) * 12,
                        cy + (Math.random() - 0.5) * 4,
                        cz + (Math.random() - 0.5) * 12,
                    ],
                    scale: 4 + Math.random() * 6,
                })
            }
            clouds.push(puffs)
        }
        return clouds
    }, [])

    useFrame((state, delta) => {
        if (!cloudGroupRef.current || !showClouds) return
        cloudGroupRef.current.rotation.y += windSpeed * 0.0005 * delta
    })

    const cloudColor = (weatherState === 'THUNDERSTORM' || weatherState === 'TEMPEST')
        ? '#1e2430'
        : (weatherState === 'CLOUDY' || weatherState === 'RAIN' || weatherState === 'SNOW')
            ? '#64748b'
            : isNight ? '#1e2029' : '#ffffff'

    // ── 5. WIND DUST & POLLEN PARTICLES ───────────────────────────────────────
    const dustRef = useRef()
    const dustCount = 800

    const dustPositions = useMemo(() => {
        const pos = new Float32Array(dustCount * 3)
        for (let i = 0; i < dustCount; i++) {
            pos[i * 3] = (Math.random() - 0.5) * 100
            pos[i * 3 + 1] = Math.random() * 30
            pos[i * 3 + 2] = (Math.random() - 0.5) * 100
        }
        return pos
    }, [])

    return (
        <group>
            {/* 👁️ Night Vision Soft Ambient Illumination */}
            {nightVision && (
                <ambientLight intensity={0.95} color="#cbd5e1" />
            )}

            {/* ☀️ 1. Sun Mesh & Direct Facade Insolation */}
            {showSun && sunY > -5 && (
                <group>
                    <mesh position={[sunX, sunY, sunZ]}>
                        <sphereGeometry args={[3, 16, 16]} />
                        <meshBasicMaterial color={sunY < 15 ? '#ff7733' : '#fff5cc'} />
                    </mesh>
                    <directionalLight
                        position={[sunX, sunY, sunZ]}
                        intensity={Math.max(0.2, (sunY / 55) * 1.5)}
                        color={sunY < 15 ? '#ff8844' : '#ffffff'}
                        castShadow
                        shadow-mapSize={[2048, 2048]}
                    />
                </group>
            )}

            {/* 🌙 2. 3D Moon Mesh & Lunar Crepuscular Light */}
            {isNight && moonY > -10 && (
                <group>
                    <mesh position={[moonX, Math.max(10, moonY), moonZ]}>
                        <sphereGeometry args={[2.5, 16, 16]} />
                        <meshStandardMaterial color="#94a3b8" emissive="#38bdf8" emissiveIntensity={0.25} roughness={0.8} />
                    </mesh>
                    {/* Soft Pale Blue Lunar Crepuscular Light */}
                    <directionalLight
                        position={[moonX, Math.max(15, moonY), moonZ]}
                        intensity={nightVision ? 0.6 : 0.25}
                        color="#7dd3fc"
                    />
                </group>
            )}

            {/* ☁️ 3. Drifting 3D Clouds Deck */}
            {showClouds && (
                <group ref={cloudGroupRef}>
                    {cloudPuffs.map((cloud, cIdx) => (
                        <group key={cIdx}>
                            {cloud.map((puff, pIdx) => (
                                <mesh key={pIdx} position={puff.pos} scale={[puff.scale, puff.scale * 0.6, puff.scale]}>
                                    <sphereGeometry args={[1, 12, 12]} />
                                    <meshStandardMaterial
                                        color={cloudColor}
                                        transparent
                                        opacity={0.75}
                                        roughness={0.9}
                                    />
                                </mesh>
                            ))}
                        </group>
                    ))}
                </group>
            )}

            {/* ⚡ 4. Lightning Bolt & Strobe Flash */}
            {showLightning && lightningActive && (
                <group>
                    <ambientLight intensity={4.5} color="#b0c4de" />
                    <pointLight position={[0, 45, 0]} intensity={25.0} color="#e0ffff" distance={150} />

                    {lightningMesh && (
                        <line geometry={lightningMesh}>
                            <lineBasicMaterial color="#ffffff" linewidth={4} />
                        </line>
                    )}
                </group>
            )}

            {/* 🌧️ 5. Rain, Snow & Hail Particle System */}
            {showPrecipitation && (intensity > 0.1 || isSnow || isHail) && (
                <points ref={precipRef}>
                    <bufferGeometry>
                        <bufferAttribute
                            attach="attributes-position"
                            count={maxParticles}
                            array={particles.positions}
                            itemSize={3}
                        />
                    </bufferGeometry>
                    <pointsMaterial
                        size={isSnow ? 0.45 : isHail ? 0.35 : 0.22}
                        color={isSnow ? '#ffffff' : isHail ? '#e2e8f0' : '#70b5ff'}
                        transparent
                        opacity={isSnow ? 0.85 : 0.65}
                        sizeAttenuation={true}
                    />
                </points>
            )}

            {/* 💨 6. Wind Dust & Pollen Stream */}
            {showWindDust && windSpeed > 2 && (
                <points ref={dustRef}>
                    <bufferGeometry>
                        <bufferAttribute
                            attach="attributes-position"
                            count={dustCount}
                            array={dustPositions}
                            itemSize={3}
                        />
                    </bufferGeometry>
                    <pointsMaterial
                        size={0.15}
                        color="#d4d4d8"
                        transparent
                        opacity={0.4}
                        sizeAttenuation={true}
                    />
                </points>
            )}
        </group>
    )
}
