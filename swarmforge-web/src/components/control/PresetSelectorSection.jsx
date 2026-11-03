import React, { useState } from 'react'
import { usePresetStore } from '../../store/presetStore'
import { useSimulationStore } from '../../store/simulationStore'
import { showToast } from '../../store/toastStore'
import {
    Globe, Bug, Home, ShieldAlert, Sun, Bookmark, Save, FolderOpen, Plus,
    RefreshCw, Dices, CheckCircle, MapPin
} from 'lucide-react'

export default function PresetSelectorSection({ styles }) {
    const {
        worldPresets,
        speciesPresets,
        nestPresets,
        preyPredatorPresets,
        weatherPresets,
        scenarioMetaPresets,
        selectedWorldId,
        selectedSpeciesId,
        selectedNestId,
        selectedPreyPredatorId,
        selectedWeatherId,
        selectedScenarioMetaId,
        masterSeed,
        hasPendingChanges,
        setMasterSeed,
        randomizeMasterSeed,
        setSelectedWorld,
        setSelectedSpecies,
        setSelectedNest,
        setSelectedPreyPredator,
        setSelectedWeather,
        selectScenarioMeta,
        applyPresetsToSimulation,
        saveScenarioMeta,
        exportPresetsJSON,
        importPresetsJSON,
    } = usePresetStore()

    const {
        running,
        pause,
        resetSimulation,
        weatherMode,
        setWeatherMode,
        realWeatherData,
        realWeatherLoading,
        fetchRealWorldWeather,
    } = useSimulationStore()

    const [newScenarioName, setNewScenarioName] = useState('')
    const [showSaveModal, setShowSaveModal] = useState(false)

    const handlePresetChange = (changeFn, val) => {
        changeFn(val)
    }

    const handleScenarioChange = (e) => {
        const scenarioId = e.target.value
        selectScenarioMeta(scenarioId)
        showToast("Scénario sélectionné", "info")
    }

    const handleApply = () => {
        if (running) {
            pause()
        }
        const session = applyPresetsToSimulation()
        if (resetSimulation) {
            resetSimulation(session)
        }
        showToast("⚡ Presets & Graine Aléatoire appliqués à la simulation avec succès !", "success")
    }

    const handleSaveScenario = () => {
        if (!newScenarioName.trim()) return
        saveScenarioMeta(newScenarioName.trim())
        setNewScenarioName('')
        setShowSaveModal(false)
        showToast("💾 Nouveau méta-preset enregistré avec succès !", "success")
    }

    const handleExport = () => {
        const jsonStr = exportPresetsJSON()
        const blob = new Blob([jsonStr], { type: 'application/json' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `swarmforge_presets_seed_${masterSeed}_${new Date().toISOString().slice(0, 10)}.json`
        a.click()
        showToast("📥 Presets & Seed exportés en JSON", "success")
    }

    const handleImport = (e) => {
        const file = e.target.files[0]
        if (!file) return
        const reader = new FileReader()
        reader.onload = (evt) => {
            const success = importPresetsJSON(evt.target.result)
            if (success) {
                showToast("Presets & Graine Aléatoire JSON importés avec succès !", "success")
            } else {
                showToast("Format de fichier JSON invalide.", "error")
            }
        }
        reader.readAsText(file)
    }

    return (
        <>
            {/* SECTION 1: PRESET & MASTER SEED SELECTORS */}
            <div style={styles.selectGroup}>
                <div style={styles.sectionHeader}>
                    <Bookmark size={14} className="text-sky-400" />
                    <span>Scénario (Méta-Preset)</span>
                </div>

                <select
                    value={selectedScenarioMetaId}
                    onChange={handleScenarioChange}
                    style={{ ...styles.select, background: '#0f172a', color: '#38bdf8', borderColor: '#334155' }}
                >
                    {scenarioMetaPresets.map(s => (
                        <option key={s.id} value={s.id}>
                            ★ {s.name}
                        </option>
                    ))}
                </select>

                {/* MASTER SEED */}
                <div>
                    <label style={{ ...styles.label, marginBottom: 4 }}>
                        <Dices size={13} style={{ color: '#f59e0b' }} />
                        <span>Graine Aléatoire (Master Seed Replay) :</span>
                    </label>
                    <div style={styles.seedBox}>
                        <input
                            type="number"
                            value={masterSeed}
                            onChange={(e) => setMasterSeed(e.target.value)}
                            style={styles.seedInput}
                            title="Graine aléatoire garantissant la reproductibilité exacte (replay) du monde"
                        />
                        <button
                            onClick={randomizeMasterSeed}
                            title="Générer une nouvelle graine aléatoire"
                            style={{ background: 'transparent', border: 'none', color: '#f59e0b', cursor: 'pointer', padding: 2 }}
                        >
                            <RefreshCw size={14} />
                        </button>
                    </div>
                </div>

                <div style={styles.btnRow}>
                    <button style={styles.iconBtn} onClick={() => setShowSaveModal(true)} title="Enregistrer la configuration actuelle comme nouveau méta-preset">
                        <Plus size={12} />
                        <span>Méta-Preset</span>
                    </button>
                    <button style={styles.iconBtn} onClick={handleExport} title="Exporter les presets & seed en JSON">
                        <Save size={12} />
                        <span>JSON</span>
                    </button>
                    <label style={{ ...styles.iconBtn, cursor: 'pointer' }} title="Importer un fichier JSON de presets">
                        <FolderOpen size={12} />
                        <span>Importer</span>
                        <input type="file" accept=".json" onChange={handleImport} style={{ display: 'none' }} />
                    </label>
                </div>
            </div>

            {/* DOMAIN PRESETS SELECTORS & REAL WEATHER TOGGLE */}
            <div style={styles.selectGroup}>
                <div style={styles.sectionHeader}>
                    <span>Sélecteurs de Presets d'Onglets</span>
                </div>

                {/* 1. World Preset */}
                <div>
                    <label style={styles.label}>
                        <Globe size={13} style={{ color: '#38bdf8' }} />
                        <span>Monde / Biome :</span>
                    </label>
                    <select
                        value={selectedWorldId}
                        onChange={(e) => handlePresetChange(setSelectedWorld, e.target.value)}
                        style={styles.select}
                    >
                        {worldPresets.map(w => (
                            <option key={w.id} value={w.id}>{w.name}</option>
                        ))}
                    </select>
                </div>

                {/* 2. Species Preset */}
                <div>
                    <label style={styles.label}>
                        <Bug size={13} style={{ color: '#f43f5e' }} />
                        <span>Espèce(s) :</span>
                    </label>
                    <select
                        value={selectedSpeciesId}
                        onChange={(e) => handlePresetChange(setSelectedSpecies, e.target.value)}
                        style={styles.select}
                    >
                        {speciesPresets.map(sp => (
                            <option key={sp.id} value={sp.id}>{sp.name}</option>
                        ))}
                    </select>
                </div>

                {/* 3. Nest Preset */}
                <div>
                    <label style={styles.label}>
                        <Home size={13} style={{ color: '#a855f7' }} />
                        <span>Nid (Correspondant) :</span>
                    </label>
                    <select
                        value={selectedNestId}
                        onChange={(e) => handlePresetChange(setSelectedNest, e.target.value)}
                        style={styles.select}
                    >
                        {nestPresets.map(n => (
                            <option key={n.id} value={n.id}>{n.name}</option>
                        ))}
                    </select>
                </div>

                {/* 4. Prey & Predator Preset */}
                <div>
                    <label style={styles.label}>
                        <ShieldAlert size={13} style={{ color: '#f59e0b' }} />
                        <span>Proies & Prédateurs :</span>
                    </label>
                    <select
                        value={selectedPreyPredatorId}
                        onChange={(e) => handlePresetChange(setSelectedPreyPredator, e.target.value)}
                        style={styles.select}
                    >
                        {preyPredatorPresets.map(pp => (
                            <option key={pp.id} value={pp.id}>{pp.name}</option>
                        ))}
                    </select>
                </div>

                {/* 5. Weather: Real-World vs Simulated Toggle */}
                <div style={{ background: 'rgba(0,0,0,0.25)', padding: 8, borderRadius: 6, border: '1px solid rgba(255,255,255,0.05)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                        <label style={styles.label}>
                            <Sun size={13} style={{ color: '#eab308' }} />
                            <span>Météo :</span>
                        </label>
                        <div style={{ display: 'inline-flex', borderRadius: 4, overflow: 'hidden', border: '1px solid #334155' }}>
                            <button
                                onClick={() => setWeatherMode('SIMULATED')}
                                style={{
                                    padding: '4px 9px',
                                    fontSize: 10,
                                    fontWeight: 700,
                                    border: 'none',
                                    background: weatherMode === 'SIMULATED' ? '#0284c7' : '#1e293b',
                                    color: weatherMode === 'SIMULATED' ? '#ffffff' : '#94a3b8',
                                    cursor: 'pointer',
                                    transition: 'all 0.15s ease'
                                }}
                                title="🔵 Météo Simulée : Cycle solaire et climatique 100% déterministe généré par le moteur interne (saisons, vent, précipitations paramétrables)."
                            >
                                🔵 Simulée
                            </button>
                            <button
                                onClick={() => setWeatherMode('REAL_WORLD')}
                                style={{
                                    padding: '4px 9px',
                                    fontSize: 10,
                                    fontWeight: 700,
                                    border: 'none',
                                    borderLeft: '1px solid #334155',
                                    background: weatherMode === 'REAL_WORLD' ? '#16a34a' : '#1e293b',
                                    color: weatherMode === 'REAL_WORLD' ? '#ffffff' : '#94a3b8',
                                    cursor: 'pointer',
                                    transition: 'all 0.15s ease'
                                }}
                                title="🟢 Météo Réelle : Synchronisation en direct avec les conditions météo terrestres réelles via l'API Open-Meteo."
                            >
                                🟢 Météo Réelle
                            </button>
                        </div>
                    </div>

                    {weatherMode === 'SIMULATED' ? (
                        <div>
                            <select
                                value={selectedWeatherId}
                                onChange={(e) => handlePresetChange(setSelectedWeather, e.target.value)}
                                style={styles.select}
                            >
                                {weatherPresets.map(w => (
                                    <option key={w.id} value={w.id}>{w.name}</option>
                                ))}
                            </select>
                            <div style={{ marginTop: 4, fontSize: 9.5, color: '#94a3b8', lineHeight: 1.3 }}>
                                ℹ️ <em>Simulation déterministe autonome (cycle circadien, température, humidité et vent synthétiques).</em>
                            </div>
                        </div>
                    ) : (
                        <div style={{ background: 'rgba(22, 163, 74, 0.1)', border: '1px solid rgba(34, 197, 94, 0.3)', borderRadius: 6, padding: 8, fontSize: 11 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontWeight: 700, color: '#4ade80', display: 'flex', alignItems: 'center', gap: 4 }}>
                                    <MapPin size={12} />
                                    {realWeatherData?.cityName || 'Position en direct'}
                                </span>
                                <button
                                    onClick={() => fetchRealWorldWeather(undefined, undefined, undefined, true)}
                                    disabled={realWeatherLoading}
                                    style={{ background: 'transparent', border: 'none', color: '#38bdf8', cursor: 'pointer', padding: 2 }}
                                    title="Rafraîchir les données météo Open-Meteo"
                                >
                                    <RefreshCw size={12} className={realWeatherLoading ? "animate-spin" : ""} />
                                </button>
                            </div>
                            {realWeatherData ? (
                                <div style={{ marginTop: 4, color: '#cbd5e1', lineHeight: 1.4 }}>
                                    <div><strong>{realWeatherData.temp}°C</strong> — {realWeatherData.condition} {realWeatherData.icon}</div>
                                    <div style={{ fontSize: 10, color: '#94a3b8' }}>
                                        Pluie: {realWeatherData.precipitation} mm | Vent: {realWeatherData.windSpeed} km/h | Humidité: {realWeatherData.humidity}%
                                    </div>
                                </div>
                            ) : (
                                <div style={{ color: '#94a3b8', fontStyle: 'italic', marginTop: 4 }}>
                                    {realWeatherLoading ? 'Chargement Open-Meteo...' : 'Cliquez pour récupérer la météo locale'}
                                </div>
                            )}
                            <div style={{ marginTop: 6, padding: '4px 6px', background: 'rgba(0,0,0,0.3)', borderRadius: 4, fontSize: 9.5, color: '#a7f3d0', lineHeight: 1.3 }}>
                                ℹ️ <em>Météo réelle via Open-Meteo API. Chaque mesure est archivée dans les snapshots pour garantir un replay déterministe.</em>
                            </div>
                        </div>
                    )}
                </div>

                {/* APPLY BUTTON */}
                <button
                    onClick={handleApply}
                    style={styles.applyBtn}
                    title="Appliquer l'ensemble des presets et la graine aléatoire à la simulation active"
                >
                    {hasPendingChanges ? <RefreshCw size={15} className="animate-spin" /> : <CheckCircle size={15} />}
                    <span>{hasPendingChanges ? '⚡ Appliquer les Presets & Seed' : '✓ Presets Appliqués'}</span>
                </button>
            </div>

            {/* Modal for saving new Meta-Preset */}
            {showSaveModal && (
                <div style={{
                    position: 'fixed',
                    top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0,0,0,0.7)',
                    zIndex: 2000,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}>
                    <div style={{
                        background: '#0f172a',
                        border: '1px solid #334155',
                        borderRadius: 12,
                        padding: 20,
                        width: 310,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 12
                    }}>
                        <div style={{ fontSize: 14, fontWeight: 700, color: '#38bdf8' }}>Nouveau Scénario (Méta-Preset)</div>
                        <input
                            type="text"
                            placeholder="Nom du Scénario (ex: Mon Terrarium N°1)"
                            value={newScenarioName}
                            onChange={(e) => setNewScenarioName(e.target.value)}
                            style={{ width: '100%', background: '#1e293b', border: '1px solid #475569', color: '#fff', padding: 8, borderRadius: 6, fontSize: 12 }}
                        />
                        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                            <button onClick={() => setShowSaveModal(false)} style={{ padding: '6px 12px', background: '#334155', border: 'none', color: '#fff', borderRadius: 6, fontSize: 12, cursor: 'pointer' }}>Annuler</button>
                            <button onClick={handleSaveScenario} style={{ padding: '6px 12px', background: '#0284c7', border: 'none', color: '#fff', borderRadius: 6, fontSize: 12, fontWeight: 700, cursor: 'pointer' }}>Enregistrer</button>
                        </div>
                    </div>
                </div>
            )}
        </>
    )
}
