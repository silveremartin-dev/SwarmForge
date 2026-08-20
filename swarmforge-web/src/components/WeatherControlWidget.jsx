import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'

/**
 * WeatherControlWidget Component
 * Provides a GUI overlay during Simulation Mode to toggle and display
 * atmospheric visual effects: Sun, Moon, Lightning, Clouds, Rain, Fog, Wind dust, Night Vision.
 */
export default function WeatherControlWidget() {
    const { environment, weatherToggles, setWeatherToggle, triggerLightning } = useSimulationStore()
    const [collapsed, setCollapsed] = useState(false)

    const {
        showSun = true,
        showLightning = true,
        showClouds = true,
        showPrecipitation = true,
        showFog = true,
        showWindDust = true,
        nightVision = false,
    } = weatherToggles || {}

    const styles = {
        container: {
            position: 'absolute',
            bottom: 20,
            right: 20,
            zIndex: 95,
            background: 'rgba(15, 23, 42, 0.88)',
            backdropFilter: 'blur(12px)',
            border: '1px solid rgba(255, 255, 255, 0.12)',
            borderRadius: 10,
            padding: collapsed ? '8px 12px' : '14px',
            color: '#f8fafc',
            width: collapsed ? 'auto' : 290,
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            transition: 'all 0.25s ease',
        },
        header: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: 13,
            fontWeight: 700,
            color: '#38bdf8',
            cursor: 'pointer',
        },
        grid: {
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 8,
            marginTop: 12,
        },
        toggleBtn: (active) => ({
            padding: '7px 10px',
            fontSize: 11,
            fontWeight: 600,
            border: '1px solid ' + (active ? '#0284c7' : '#334155'),
            borderRadius: 6,
            background: active ? 'rgba(2, 132, 199, 0.25)' : 'rgba(15, 23, 42, 0.6)',
            color: active ? '#38bdf8' : '#94a3b8',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            transition: 'all 0.15s ease',
        }),
        lightningBtn: {
            width: '100%',
            marginTop: 10,
            padding: '8px',
            fontSize: 11,
            fontWeight: 700,
            border: '1px solid #eab308',
            borderRadius: 6,
            background: 'linear-gradient(135deg, rgba(234, 179, 8, 0.2) 0%, rgba(202, 138, 4, 0.4) 100%)',
            color: '#fef08a',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 6,
            boxShadow: '0 0 10px rgba(234, 179, 8, 0.2)',
        },
        infoBadge: {
            marginTop: 10,
            fontSize: 10,
            color: '#94a3b8',
            background: 'rgba(0,0,0,0.2)',
            padding: '6px 8px',
            borderRadius: 4,
            display: 'flex',
            justifyContent: 'space-between',
        }
    }

    return (
        <div style={styles.container}>
            <div style={styles.header} onClick={() => setCollapsed(!collapsed)}>
                <span>🌦️ Affichage Météo & Effets</span>
                <span style={{ fontSize: 10, color: '#94a3b8' }}>{collapsed ? '▶ Déplier' : '▼ Réduire'}</span>
            </div>

            {!collapsed && (
                <>
                    <div style={styles.grid}>
                        <button
                            style={styles.toggleBtn(showSun)}
                            onClick={() => setWeatherToggle('showSun', !showSun)}
                        >
                            ☀️ Soleil / Lune
                        </button>
                        <button
                            style={styles.toggleBtn(showLightning)}
                            onClick={() => setWeatherToggle('showLightning', !showLightning)}
                        >
                            ⚡ Éclairs
                        </button>
                        <button
                            style={styles.toggleBtn(showClouds)}
                            onClick={() => setWeatherToggle('showClouds', !showClouds)}
                        >
                            ☁️ Nuages
                        </button>
                        <button
                            style={styles.toggleBtn(showPrecipitation)}
                            onClick={() => setWeatherToggle('showPrecipitation', !showPrecipitation)}
                        >
                            🌧️ Pluie / Neige
                        </button>
                        <button
                            style={styles.toggleBtn(showFog)}
                            onClick={() => setWeatherToggle('showFog', !showFog)}
                        >
                            🌫️ Brouillard
                        </button>
                        <button
                            style={styles.toggleBtn(showWindDust)}
                            onClick={() => setWeatherToggle('showWindDust', !showWindDust)}
                        >
                            💨 Vent & Poussière
                        </button>
                        <button
                            style={{ ...styles.toggleBtn(nightVision), gridColumn: 'span 2' }}
                            onClick={() => setWeatherToggle('nightVision', !nightVision)}
                        >
                            👁️ Vision Nocturne (Voir la nuit)
                        </button>
                    </div>

                    <button style={styles.lightningBtn} onClick={triggerLightning}>
                        ⚡ Déclencher un Éclair Instantané
                    </button>

                    <div style={styles.infoBadge}>
                        <span>État: <b style={{ color: '#38bdf8' }}>{environment.weatherState || 'CLEAR'}</b></span>
                        <span>Pluie: <b>{(environment.rainIntensity || 0).toFixed(1)} mm/h</b></span>
                    </div>
                </>
            )}
        </div>
    )
}
