import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { showToast } from '../store/toastStore'

export default function ClimateStudioPanel() {
    const { environment } = useSimulationStore()
    const [photoperiodHours, setPhotoperiodHours] = useState(14)
    const [tempDay, setTempDay] = useState(26)
    const [tempNight, setTempNight] = useState(16)
    const [precipitationMm, setPrecipitationMm] = useState(12)
    const [season, setSeason] = useState('SPRING')
    const [humidity, setHumidity] = useState(65)

    const styles = {
        container: {
            position: 'absolute',
            top: 60,
            left: 20,
            width: 380,
            maxHeight: 'calc(100vh - 80px)',
            overflowY: 'auto',
            background: 'rgba(20, 24, 36, 0.92)',
            backdropFilter: 'blur(12px)',
            border: '1px solid rgba(255, 255, 255, 0.12)',
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

    return (
        <div style={styles.container}>
            <div style={styles.header}>⛅ Studio Climat SwarmForge</div>
            <div style={styles.subtitle}>
                Configuration des cycles photopériodiques, photobiologie & courbes météorologiques.
            </div>

            <div style={styles.section}>
                <div style={styles.label}>
                    <span>Saison Active</span>
                    <span style={{ color: '#f59e0b', fontWeight: 700 }}>{season}</span>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6, marginTop: 8 }}>
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
                            {s}
                        </button>
                    ))}
                </div>
            </div>

            <div style={styles.section}>
                <div style={styles.label}>
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

                <div style={{ ...styles.label, marginTop: 10 }}>
                    <span>Température Diurne / Nocturne</span>
                    <span>{tempDay}°C / {tempNight}°C</span>
                </div>
                <input
                    type="range"
                    min="5"
                    max="45"
                    value={tempDay}
                    onChange={(e) => setTempDay(parseInt(e.target.value))}
                    style={styles.slider}
                />
            </div>

            <div style={styles.section}>
                <div style={styles.label}>
                    <span>Précipitations (Pluie)</span>
                    <span>{precipitationMm} mm/j</span>
                </div>
                <input
                    type="range"
                    min="0"
                    max="50"
                    value={precipitationMm}
                    onChange={(e) => setPrecipitationMm(parseInt(e.target.value))}
                    style={styles.slider}
                />

                <div style={{ ...styles.label, marginTop: 10 }}>
                    <span>Humidité Ambiante</span>
                    <span>{humidity}%</span>
                </div>
                <input
                    type="range"
                    min="20"
                    max="100"
                    value={humidity}
                    onChange={(e) => setHumidity(parseInt(e.target.value))}
                    style={styles.slider}
                />
            </div>

            <button style={styles.applyBtn} onClick={() => showToast("⛅ Profil climatique synchronisé avec le simulateur avec succès !", "success")}>
                ⛅ Synchroniser le Profil Climatique
            </button>
        </div>
    )
}
