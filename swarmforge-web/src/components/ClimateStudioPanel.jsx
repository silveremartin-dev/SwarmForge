import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { showToast } from '../store/toastStore'
import { getEffectiveSeason } from '../utils/terrainUtils'

export default function ClimateStudioPanel() {
    const { climateEngine, updateClimateEngine } = useSimulationStore()
    
    const [climateType, setClimateType] = useState(climateEngine?.climateType || 'OCEANIC')
    const [hemisphere, setHemisphere] = useState(climateEngine?.hemisphere || 'NORTHERN')
    const [season, setSeason] = useState(climateEngine?.season || 'SUMMER')
    const [latitudeDeg, setLatitudeDeg] = useState(climateEngine?.latitudeDeg || 45.0)
    const [windSpeedMs, setWindSpeedMs] = useState(climateEngine?.windSpeedMs || 2.4)
    const [photoperiodHours, setPhotoperiodHours] = useState(14)

    const effectiveSeason = getEffectiveSeason(season, hemisphere)

    const styles = {
        container: {
            position: 'absolute',
            top: 60,
            left: 20,
            width: 400,
            maxHeight: 'calc(100vh - 80px)',
            overflowY: 'auto',
            background: 'rgba(20, 24, 36, 0.94)',
            backdropFilter: 'blur(14px)',
            border: '1px solid rgba(255, 255, 255, 0.14)',
            borderRadius: 12,
            padding: 20,
            color: '#e2e8f0',
            boxShadow: '0 20px 40px rgba(0,0,0,0.5)',
            zIndex: 90,
            fontFamily: 'system-ui, -apple-system, sans-serif',
        },
        header: {
            fontSize: 18,
            fontWeight: 700,
            color: '#f59e0b',
            marginBottom: 4,
        },
        subtitle: {
            fontSize: 12,
            color: '#94a3b8',
            marginBottom: 16,
        },
        section: {
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 255, 255, 0.05)',
            borderRadius: 8,
            padding: 14,
            marginBottom: 14,
        },
        label: {
            fontSize: 12,
            fontWeight: 600,
            color: '#cbd5e1',
            marginBottom: 6,
            display: 'flex',
            justifyContent: 'space-between',
        },
        slider: {
            width: '100%',
            accentColor: '#f59e0b',
            cursor: 'pointer',
        },
        applyBtn: {
            width: '100%',
            padding: '12px',
            background: 'linear-gradient(135deg, #d97706 0%, #b45309 100%)',
            color: '#fff',
            fontWeight: 700,
            fontSize: 13,
            border: 'none',
            borderRadius: 8,
            cursor: 'pointer',
            marginTop: 10,
        }
    }

    const biomes = [
        { id: 'OCEANIC', name: '🌿 Océanique / Tempéré', desc: 'Chênes, bouleaux, pins et sol vert' },
        { id: 'CONTINENTAL', name: '🌲 Continental / Taïga', desc: 'Érables, bouleaux et conifères mixtes' },
        { id: 'MEDITERRANEAN', name: '🫒 Méditerranéen', desc: 'Érables, pins maritimes et garrigue' },
        { id: 'SAVANNA', name: '🏜️ Aride / Savane', desc: 'Cactus, arbres morts, palmiers et terre ocre' },
        { id: 'TROPICAL', name: '🌴 Tropical / Jungle', desc: 'Palmiers, bambous, fleurs exotiques et humus' },
        { id: 'ALPINE', name: '🏔️ Alpin / Montagne', desc: 'Pins d’altitude, roches granitiques et éboulis' },
    ]

    const handleApply = () => {
        if (updateClimateEngine) {
            updateClimateEngine({
                climateType,
                hemisphere,
                season,
                latitudeDeg,
                windSpeedMs,
            })
        }
        showToast(`⛅ Climat synchronisé : ${climateType} (${hemisphere === 'SOUTHERN' ? 'Hémisphère Sud' : 'Hémisphère Nord'}) - Saison active : ${effectiveSeason}`, 'success')
    }

    return (
        <div style={styles.container}>
            <div style={styles.header}>⛅ Studio Climat & Biomes</div>
            <div style={styles.subtitle}>
                Configuration des écosystèmes, biomes 3D, cycles saisonniers et astronomie solaire.
            </div>

            {/* Biome Selector */}
            <div style={styles.section}>
                <div style={styles.label}>
                    <span>Biome / Type de Climat</span>
                    <span style={{ color: '#f59e0b', fontWeight: 700 }}>{climateType}</span>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: 6, marginTop: 8 }}>
                    {biomes.map(b => (
                        <button
                            key={b.id}
                            style={{
                                padding: '8px 10px',
                                fontSize: 12,
                                borderRadius: 6,
                                border: '1px solid ' + (climateType === b.id ? '#f59e0b' : '#334155'),
                                background: climateType === b.id ? 'rgba(217, 119, 6, 0.25)' : '#1e293b',
                                color: climateType === b.id ? '#fef08a' : '#cbd5e1',
                                cursor: 'pointer',
                                textAlign: 'left',
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 2,
                            }}
                            onClick={() => setClimateType(b.id)}
                        >
                            <span style={{ fontWeight: 600 }}>{b.name}</span>
                            <span style={{ fontSize: 10, color: '#94a3b8' }}>{b.desc}</span>
                        </button>
                    ))}
                </div>
            </div>

            {/* Hemisphere & Latitude */}
            <div style={styles.section}>
                <div style={styles.label}>
                    <span>Hémisphère Terrestre</span>
                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>{hemisphere === 'NORTHERN' ? 'Nord 🌐' : 'Sud 🌐 (Saisons Inversées)'}</span>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6, marginTop: 8 }}>
                    <button
                        style={{
                            padding: '8px',
                            fontSize: 11,
                            borderRadius: 6,
                            border: '1px solid #475569',
                            background: hemisphere === 'NORTHERN' ? '#0284c7' : '#1e293b',
                            color: '#fff',
                            cursor: 'pointer',
                            fontWeight: hemisphere === 'NORTHERN' ? 700 : 400,
                        }}
                        onClick={() => setHemisphere('NORTHERN')}
                    >
                        🌐 Hémisphère Nord
                    </button>
                    <button
                        style={{
                            padding: '8px',
                            fontSize: 11,
                            borderRadius: 6,
                            border: '1px solid #475569',
                            background: hemisphere === 'SOUTHERN' ? '#0284c7' : '#1e293b',
                            color: '#fff',
                            cursor: 'pointer',
                            fontWeight: hemisphere === 'SOUTHERN' ? 700 : 400,
                        }}
                        onClick={() => setHemisphere('SOUTHERN')}
                    >
                        🌐 Hémisphère Sud
                    </button>
                </div>

                <div style={{ ...styles.label, marginTop: 12 }}>
                    <span>Latitude</span>
                    <span>{latitudeDeg}° {hemisphere === 'NORTHERN' ? 'N' : 'S'}</span>
                </div>
                <input
                    type="range"
                    min="0"
                    max="80"
                    value={latitudeDeg}
                    onChange={(e) => setLatitudeDeg(parseFloat(e.target.value))}
                    style={styles.slider}
                />
            </div>

            {/* Seasons with Inversion Indicator */}
            <div style={styles.section}>
                <div style={styles.label}>
                    <span>Saison Calendaire</span>
                    <span style={{ color: '#f59e0b', fontWeight: 700 }}>{season}</span>
                </div>
                {hemisphere === 'SOUTHERN' && (
                    <div style={{ fontSize: 11, color: '#38bdf8', marginBottom: 8, background: 'rgba(2, 132, 199, 0.15)', padding: '4px 8px', borderRadius: 4 }}>
                        ℹ️ Hémisphère Sud : Saison effective = <strong>{effectiveSeason}</strong>
                    </div>
                )}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
                    {['SPRING', 'SUMMER', 'AUTUMN', 'WINTER'].map(s => (
                        <button
                            key={s}
                            style={{
                                padding: '6px',
                                fontSize: 11,
                                borderRadius: 6,
                                border: '1px solid #475569',
                                background: season === s ? '#d97706' : '#1e293b',
                                color: '#fff',
                                cursor: 'pointer',
                            }}
                            onClick={() => setSeason(s)}
                        >
                            {s === 'SPRING' && '🌸 Printemps'}
                            {s === 'SUMMER' && '☀️ Été'}
                            {s === 'AUTUMN' && '🍂 Automne'}
                            {s === 'WINTER' && '❄️ Hiver'}
                        </button>
                    ))}
                </div>
            </div>

            {/* Wind & Photoperiod */}
            <div style={styles.section}>
                <div style={styles.label}>
                    <span>Vitesse du Vent (Oscillation Végétale)</span>
                    <span>{windSpeedMs} m/s</span>
                </div>
                <input
                    type="range"
                    min="0.5"
                    max="15"
                    step="0.5"
                    value={windSpeedMs}
                    onChange={(e) => setWindSpeedMs(parseFloat(e.target.value))}
                    style={styles.slider}
                />

                <div style={{ ...styles.label, marginTop: 10 }}>
                    <span>Photopériode (Durée du Jour)</span>
                    <span>{photoperiodHours}h de Soleil / jour</span>
                </div>
                <input
                    type="range"
                    min="6"
                    max="20"
                    value={photoperiodHours}
                    onChange={(e) => setPhotoperiodHours(parseInt(e.target.value))}
                    style={styles.slider}
                />
            </div>

            <button style={styles.applyBtn} onClick={handleApply}>
                ⛅ Synchroniser le Profil & Biome Climatique
            </button>
        </div>
    )
}
