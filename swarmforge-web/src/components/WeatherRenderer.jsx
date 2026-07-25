import { useRef, useMemo, useState, useEffect } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'

/**
 * WeatherRenderer Component
 * Renders interactive 3D environmental weather effects in Simulation Mode:
 * - Soleil (Sun mesh, solar halo, directional rays)
 * - Éclairs (Lightning bolt geometry & screen flash lights during storms or triggers)
 * - Nuages (3D drifting cloud clusters reacting to wind)
 * - Pluie / Neige / Grêle (Precipitation particle engine for rain, snow, hail)
 * - Brouillard (Volumetric atmospheric fog adapting to humidity & climate)
 * - Vent / Poussière (Wind vector dust & pollen particles)
 */
export default function WeatherRenderer() {
    const { environment, weatherToggles } = useSimulationStore()

    // Environment parameters with fallbacks
    const temp = environment.temperature ?? 20
    const humidity = environment.humidity ?? 50
    const intensity = environment.rainIntensity ?? 0
    const windSpeed = environment.windSpeed ?? 5
    const weatherState = environment.weatherState || (intensity > 15 ? 'TEMPEST' : intensity > 5 ? 'THUNDERSTORM' : intensity > 0.5 ? 'RAIN' : temp <= 0 ? 'SNOW' : 'CLEAR')
    const timeOfDay = environment.timeOfDay || 'DAY'
    const isNight = environment.lightLevel !== undefined ? environment.lightLevel < 0.3 : (timeOfDay === 'NIGHT')

    // Toggles
    const {
        showSun = true,
        showLightning = true,
        showClouds = true,
        showPrecipitation = true,
        showFog = true,
        showWindDust = true,
        lightningTrigger = 0,
    } = weatherToggles || {}

    // ── 1. PRECIPITATION ENGINE (Rain, Snow, Hail) ─────────────────────────────
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

    // ── 2. LIGHTNING / ÉCLAIRS SYSTEM ─────────────────────────────────────────
    const [lightningActive, setLightningActive] = useState(false)
    const [lightningMesh, setLightningMesh] = useState(null)
    const prevTriggerRef = useRef(lightningTrigger)

    // Trigger lightning bolt on manual click or during storms
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
        }, 4000)

        return () => clearInterval(interval)
    }, [weatherState, showLightning])

    const flashLightning = () => {
        setLightningActive(true)

        // Generate jagged electric bolt points
        const startX = (Math.random() - 0.5) * 60
        const startZ = (Math.random() - 0.5) * 60
        const points = []
        let currentPos = new THREE.Vector3(startX, 50, startZ)
        points.push(currentPos.clone())

        while (currentPos.y > 0) {
            currentPos.y -= 3 + Math.random() * 4
            currentPos.x += (Math.random() - 0.5) * 6
            currentPos.z += (Math.random() - 0.5) * 6
            points.push(currentPos.clone())
        }

        const geometry = new THREE.BufferGeometry().setFromPoints(points)
        setLightningMesh(geometry)

        setTimeout(() => {
            setLightningActive(false)
        }, 180)
    }

    // ── 3. CLOUDS DECK (3D Volumetric Drifting Clouds) ─────────────────────────
    const cloudGroupRef = useRef()
    const cloudCount = 12

    const cloudPuffs = useMemo(() => {
        const clouds = []
        for (let c = 0; c < cloudCount; c++) {
            const puffs = []
            const cx = (Math.random() - 0.5) * 100
            const cy = 35 + Math.random() * 15
            const cz = (Math.random() - 0.5) * 100
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

    // Cloud color shifts based on weather
    const cloudColor = (weatherState === 'THUNDERSTORM' || weatherState === 'TEMPEST')
        ? '#222831'
        : (weatherState === 'CLOUDY' || weatherState === 'RAIN' || weatherState === 'SNOW')
            ? '#707793'
            : isNight ? '#1e2029' : '#ffffff'

    // ── 4. SUN MESH & SOLAR RAYS ───────────────────────────────────────────────
    const sunAngle = environment.sunAngle ?? 0.5
    const sunX = Math.cos((sunAngle - 0.25) * Math.PI * 2) * 55
    const sunY = Math.sin((sunAngle - 0.25) * Math.PI * 2) * 55
    const sunZ = 15

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

    useFrame((state, delta) => {
        if (!dustRef.current || !showWindDust) return
        const pos = dustRef.current.geometry.attributes.position.array
        const speed = windSpeed * 0.3 * delta

        for (let i = 0; i < dustCount; i++) {
            pos[i * 3] += speed
            if (pos[i * 3] > 50) pos[i * 3] = -50
        }
        dustRef.current.geometry.attributes.position.needsUpdate = true
    })

    // ── 6. FOG DENSITY & COLOR ────────────────────────────────────────────────
    const fogDensity = (humidity > 80 || weatherState === 'FOG' || weatherState === 'TEMPEST')
        ? 0.025
        : (weatherState === 'RAIN' || weatherState === 'SNOW')
            ? 0.012
            : 0.004

    const fogColor = isNight ? '#0b0f19' : (weatherState === 'THUNDERSTORM' || weatherState === 'TEMPEST') ? '#1e2430' : '#88aaff'

    return (
        <group>
            {/* 🌫️ 1. Volumetric Fog Layer */}
            {showFog && <fogExp2 attach="fog" args={[fogColor, fogDensity]} />}

            {/* ☀️ 2. Sun & Solar Flares */}
            {showSun && !isNight && (
                <group position={[sunX, sunY, sunZ]}>
                    {/* Glowing Sun Core */}
                    <mesh>
                        <sphereGeometry args={[3.5, 32, 32]} />
                        <meshBasicMaterial color={sunY < 15 ? '#ff7733' : '#ffffaa'} />
                    </mesh>

                    {/* Solar Corona Glow */}
                    <mesh scale={[1.4, 1.4, 1.4]}>
                        <sphereGeometry args={[3.5, 16, 16]} />
                        <meshBasicMaterial
                            color={sunY < 15 ? '#ffaa00' : '#ffee77'}
                            transparent
                            opacity={0.35}
                        />
                    </mesh>

                    {/* Direct Light Source */}
                    <directionalLight
                        intensity={Math.max(0.2, environment.lightLevel * 1.8)}
                        color={sunY < 15 ? '#ff8844' : '#ffffff'}
                        castShadow
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

            {/* ⚡ 4. Lightning Bolt & Flash Light */}
            {showLightning && lightningActive && (
                <group>
                    {/* Ambient Strobe Flash */}
                    <ambientLight intensity={4.5} color="#b0c4de" />
                    <pointLight position={[0, 45, 0]} intensity={25.0} color="#e0ffff" distance={150} />

                    {/* Jagged Bolt Line */}
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
