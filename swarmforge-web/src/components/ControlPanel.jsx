import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { usePresetStore } from '../store/presetStore'
import { showToast } from '../store/toastStore'
import PopulationGraph from './PopulationGraph'
import { Globe, Bug, Home, ShieldAlert, Sun, Bookmark, Save, FolderOpen, Plus, Play, Pause, Dices, CheckCircle, RefreshCw } from 'lucide-react'

export default function ControlPanel() {
    const {
        tick,
        running,
        speed,
        stats,
        play,
        pause,
        setSpeed,
        resetSimulation,
        simulationParams,
        setSimulationParam,
    } = useSimulationStore()

    const [paramCategoryTab, setParamCategoryTab] = useState('pheromones')

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

    const [newScenarioName, setNewScenarioName] = useState('')
    const [showSaveModal, setShowSaveModal] = useState(false)

    // Helper: Preset selection prepares changes (marked as pending)
    const handlePresetChange = (changeFn, val) => {
        changeFn(val)
    }

    const handleScenarioChange = (e) => {
        const scenarioId = e.target.value
        selectScenarioMeta(scenarioId)
        showToast("Scénario sélectionné", "info")
    }

    // APPLY ACTION: Applies changes to active simulation and interrupts if running
    const handleApply = () => {
        if (running) {
            pause()
        }
        const session = applyPresetsToSimulation()
        if (resetSimulation) {
            resetSimulation(session)
        }
        showToast("⚡ Presets & Graine Aléatoire (Seed) appliqués à la simulation avec succès !", "success")
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
                showToast("Presets & Graine Aléatoire (Seed) JSON importés avec succès !", "success")
            } else {
                showToast("Format de fichier JSON invalide.", "error")
            }
        }
        reader.readAsText(file)
    }

    const styles = {
        panel: {
            position: inline ? 'relative' : 'absolute',
            top: inline ? 0 : 60,
            right: inline ? 'auto' : 20,
            left: inline ? 0 : 'auto',
            width: inline ? '100%' : 340,
            maxHeight: inline ? 'none' : 'calc(100vh - 80px)',
            overflowY: 'auto',
            background: 'rgba(15, 23, 42, 0.94)',
            backdropFilter: 'blur(16px)',
            border: '1px solid rgba(255, 255, 255, 0.12)',
            borderRadius: 12,
            padding: 16,
            display: 'flex',
            flexDirection: 'column',
            gap: 12,
            zIndex: 90,
            color: '#e2e8f0',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            boxShadow: '0 20px 40px rgba(0,0,0,0.5)',
        },
        title: {
            fontSize: 16,
            fontWeight: 800,
            color: '#38bdf8',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
        },
        sectionHeader: {
            fontSize: 11,
            fontWeight: 700,
            color: '#94a3b8',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
            marginBottom: 6,
            display: 'flex',
            alignItems: 'center',
            gap: 6,
        },
        selectGroup: {
            background: 'rgba(255,255,255,0.03)',
            border: '1px solid rgba(255,255,255,0.06)',
            borderRadius: 8,
            padding: 10,
            display: 'flex',
            flexDirection: 'column',
            gap: 8,
        },
        label: {
            fontSize: 11,
            fontWeight: 600,
            color: '#cbd5e1',
            display: 'flex',
            alignItems: 'center',
            gap: 6,
        },
        select: {
            width: '100%',
            background: '#0f172a',
            border: '1px solid #334155',
            color: '#38bdf8',
            padding: '6px 8px',
            borderRadius: 6,
            fontSize: 12,
            fontWeight: 600,
            outline: 'none',
        },
        seedBox: {
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            background: '#0f172a',
            border: '1px solid #334155',
            borderRadius: 6,
            padding: '4px 8px',
        },
        seedInput: {
            flex: 1,
            background: 'transparent',
            border: 'none',
            color: '#f59e0b',
            fontWeight: 700,
            fontSize: 12,
            outline: 'none',
        },
        btnRow: {
            display: 'flex',
            gap: 6,
            marginTop: 4,
        },
        iconBtn: {
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 4,
            padding: '5px 8px',
            fontSize: 11,
            fontWeight: 600,
            background: '#1e293b',
            border: '1px solid #334155',
            color: '#cbd5e1',
            borderRadius: 6,
            cursor: 'pointer',
            transition: 'all 0.2s',
        },
        applyBtn: {
            width: '100%',
            padding: '9px 12px',
            fontSize: 12,
            fontWeight: 800,
            border: 'none',
            borderRadius: 8,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            background: hasPendingChanges
                ? 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)'
                : 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)',
            color: '#fff',
            boxShadow: hasPendingChanges
                ? '0 4px 14px rgba(245, 158, 11, 0.45)'
                : '0 2px 8px rgba(2, 132, 199, 0.3)',
            animation: hasPendingChanges ? 'pulse 1.5s infinite' : 'none',
        },
        button: {
            width: '100%',
            padding: '10px',
            fontSize: 13,
            fontWeight: 700,
            border: 'none',
            borderRadius: 8,
            cursor: 'pointer',
            transition: 'all 0.2s',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
        },
        playBtn: {
            background: running ? 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' : 'linear-gradient(135deg, #22c55e 0%, #16a34a 100%)',
            color: '#fff',
            boxShadow: running ? '0 4px 12px rgba(239, 68, 68, 0.4)' : '0 4px 12px rgba(34, 197, 94, 0.4)',
        },
        slider: {
            width: '100%',
            accentColor: '#38bdf8',
            marginTop: 4,
        },
        statGrid: {
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 6,
        },
        statItem: {
            background: 'rgba(255,255,255,0.03)',
            border: '1px solid rgba(255,255,255,0.05)',
            borderRadius: 6,
            padding: 6,
            textAlign: 'center',
        },
        statValue: {
            fontSize: 16,
            fontWeight: 700,
            color: '#fff',
        },
        statLabel: {
            fontSize: 9,
            color: '#64748b',
            textTransform: 'uppercase',
        },
    }

    return (
        <div style={styles.panel}>
            <div style={styles.title}>
                <span>⚙️ Gestionnaire de Simulation</span>
                {running && <span style={{ fontSize: 10, background: '#16a34a', color: '#fff', padding: '2px 6px', borderRadius: 4 }}>EN COURS</span>}
            </div>

            {/* SECTION 1: PRESET & MASTER SEED SELECTORS (ABOVE SIMULATION CONTROLS) */}
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

                {/* MASTER SEED FOR DETERMINISTIC REPLAY INTEGRITY */}
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

            {/* DOMAIN PRESETS SELECTORS */}
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

                {/* 5. Weather & Climate Preset */}
                <div>
                    <label style={styles.label}>
                        <Sun size={13} style={{ color: '#eab308' }} />
                        <span>Météo & Climat :</span>
                    </label>
                    <select
                        value={selectedWeatherId}
                        onChange={(e) => handlePresetChange(setSelectedWeather, e.target.value)}
                        style={styles.select}
                    >
                        {weatherPresets.map(w => (
                            <option key={w.id} value={w.id}>{w.name}</option>
                        ))}
                    </select>
                </div>

                {/* APPLY BUTTON (PROMINENT BUTTON TO APPLY PRESETS & MASTER SEED TO SIMULATION) */}
                <button
                    onClick={handleApply}
                    style={styles.applyBtn}
                    title="Appliquer l'ensemble des presets et la graine aléatoire à la simulation active"
                >
                    {hasPendingChanges ? <RefreshCw size={15} className="animate-spin" /> : <CheckCircle size={15} />}
                    <span>{hasPendingChanges ? '⚡ Appliquer les Presets & Seed' : '✓ Presets Appliqués'}</span>
                </button>
            </div>

            {/* SECTION 2: CATEGORIZED SIMULATION PARAMETERS WITH EXPLICIT SCALES & UNITS */}
            <div style={styles.selectGroup}>
                <div style={styles.sectionHeader}>
                    <span>🧪 Paramètres & Échelles de Simulation</span>
                </div>

                {/* Parameter Sub-Category Tabs */}
                <div style={{ display: 'flex', gap: 2, background: 'rgba(0,0,0,0.3)', padding: 3, borderRadius: 6, marginBottom: 8 }}>
                    {[
                        { id: 'pheromones', label: '🧪 Phéromones' },
                        { id: 'resources', label: '🍎 Ressources' },
                        { id: 'species', label: '👥 Espèces' },
                        { id: 'economics', label: '💼 Économie' },
                        { id: 'glossary', label: '📖 Glossaire' },
                    ].map(tab => (
                        <button
                            key={tab.id}
                            onClick={() => setParamCategoryTab(tab.id)}
                            style={{
                                flex: 1,
                                padding: '4px 2px',
                                fontSize: 9,
                                fontWeight: 700,
                                border: paramCategoryTab === tab.id ? '1px solid #38bdf8' : '1px solid transparent',
                                borderRadius: 4,
                                cursor: 'pointer',
                                background: paramCategoryTab === tab.id ? '#1e293b' : 'transparent',
                                color: paramCategoryTab === tab.id ? '#38bdf8' : '#94a3b8',
                                transition: 'all 0.15s ease',
                            }}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>

                {/* Render Parameters for Selected Category */}
                {paramCategoryTab !== 'glossary' && simulationParams && simulationParams[paramCategoryTab] && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        {Object.entries(simulationParams[paramCategoryTab]).map(([key, param]) => (
                            <div key={key} style={{ background: 'rgba(255,255,255,0.02)', padding: 6, borderRadius: 6, border: '1px solid rgba(255,255,255,0.04)' }}>
                                <div style={{ fontSize: 11, fontWeight: 600, color: '#cbd5e1', display: 'flex', justifyContent: 'space-between', marginBottom: 2 }}>
                                    <span>{param.label}</span>
                                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                                        {param.value} {param.unit}
                                    </span>
                                </div>
                                <input
                                    type="range"
                                    min={param.min}
                                    max={param.max}
                                    step={param.min < 1 ? 0.01 : 1}
                                    value={param.value}
                                    onChange={(e) => setSimulationParam(paramCategoryTab, key, e.target.value)}
                                    style={styles.slider}
                                />
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 9, color: '#64748b', marginTop: 2 }}>
                                    <span>Min: {param.min} {param.unit}</span>
                                    <span>Max: {param.max} {param.unit}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {/* CONSOLIDATED SCIENTIFIC GLOSSARY (INLINE, ALPHABETICALLY SORTED A-Z) */}
                {paramCategoryTab === 'glossary' && (
                    <div style={{ maxHeight: 240, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 6, paddingRight: 4 }}>
                        <div style={{ fontSize: 11, fontWeight: 700, color: '#38bdf8', borderBottom: '1px solid rgba(56, 189, 248, 0.2)', paddingBottom: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span>📖 Glossaire Scientifique Unifié</span>
                            <span style={{ fontSize: 9, color: '#a78bfa', background: 'rgba(167, 139, 250, 0.15)', padding: '2px 6px', borderRadius: 4 }}>Ordre Alphabétique (A - Z)</span>
                        </div>

                        {/* A-Z Sorted List of Scientific & Simulation Terms */}
                        {[
                            {
                                term: "🏛️ Bio-Architecture & Terrarium (m, mm)",
                                color: "#38bdf8",
                                definition: "Géométrie des galeries sous-terraines et découpe des 3 couches géologiques du terrarium (couche arable, substrat d'argile, roche mère, nappe phréatique à -3.1m)."
                            },
                            {
                                term: "📐 Correspondance Métrique SI (s, m, mW)",
                                color: "#38bdf8",
                                definition: "1 Tick de simulation = 0.1s de temps réel. 1 Mètre linéaire = 1000 millimètres sous-millimétriques de résolution spatiale du terrain."
                            },
                            {
                                term: "👥 Démographie & Castes (œufs/jour, jours)",
                                color: "#fbbf24",
                                definition: "Taux de ponte réel de la reine fondatrice (œufs/jour) et espérance de vie naturelle des castes d'ouvrières et de soldats (en jours)."
                            },
                            {
                                term: "🧠 Moteur BDI (Beliefs-Desires-Intentions)",
                                color: "#a855f7",
                                definition: "Modèle d'agent cognitif autonome basé sur l'articulation dynamique des croyances sur l'environnement, des désirs de la colonie et des intentions de travail."
                            },
                            {
                                term: "💼 Métabolisme & Puissance (mW)",
                                color: "#f87171",
                                definition: "Puissance métabolique de repos consommée par individu (en milliwatts) pondérée par le rendement de récolte énergétique (%)."
                            },
                            {
                                term: "🧪 Phéromones & Diffusion (%/s, m)",
                                color: "#c084fc",
                                definition: "Décroissance temporelle (% par seconde) et rayon d'évaporation spatiale (mètres) des signaux d'attraction ou d'alarme de la colonie."
                            },
                            {
                                term: "🐜 Polyéthisme & Spécialisation",
                                color: "#fbbf24",
                                definition: "Division du travail au sein de la supercolonie selon l'âge (polyéthisme d'âge) ou la morphologie/caste (polyéthisme morphologique)."
                            },
                            {
                                term: "🍎 Ressources & Trophobiose (g/min, mg/h)",
                                color: "#4ade80",
                                definition: "Débit massique de création de nourriture (sucres, graines) et taux de sécrétion de miellat par les insectes trophobiontes (pucerons)."
                            },
                            {
                                term: "🌀 Stigmergie & Auto-Organisation",
                                color: "#38bdf8",
                                definition: "Mécanisme d'auto-organisation où les traces laissées dans l'environnement (dépôts phéromonaux, tunnels) guident et stimulent les actions ultérieures."
                            },
                            {
                                term: "📈 Vol de Lévy & Marche Brownienne",
                                color: "#f59e0b",
                                definition: "Modèles stochastiques de recherche de nourriture combinant petits pas exploratoires locaux et grands sauts d'exploration à longue distance."
                            }
                        ].map((item, idx) => (
                            <div key={idx} style={{ background: 'rgba(255,255,255,0.03)', padding: 6, borderRadius: 6, fontSize: 10 }}>
                                <span style={{ color: item.color, fontWeight: 700 }}>{item.term} :</span>
                                <div style={{ color: '#94a3b8', marginTop: 2, lineHeight: 1.35 }}>
                                    {item.definition}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* SECTION 2: SIMULATION EXECUTION CONTROLS (START / PAUSE / STOP) */}
            <div style={styles.selectGroup}>
                <div style={styles.sectionHeader}>
                    <span>Contrôles de Simulation</span>
                </div>
                <button
                    style={{ ...styles.button, ...styles.playBtn }}
                    onClick={() => running ? pause() : play()}
                >
                    {running ? <Pause size={16} /> : <Play size={16} />}
                    <span>{running ? 'PAUSE / INTERROMPRE' : 'LANCER SIMULATION'}</span>
                </button>

                <div style={{ marginTop: 4 }}>
                    <div style={{ fontSize: 11, color: '#94a3b8', display: 'flex', justifyContent: 'space-between' }}>
                        <span>Vitesse d'Exécution</span>
                        <span style={{ color: '#38bdf8', fontWeight: 700 }}>{speed.toFixed(1)}x</span>
                    </div>
                    <input
                        type="range"
                        min="0.1"
                        max="10"
                        step="0.1"
                        value={speed}
                        onChange={(e) => setSpeed(parseFloat(e.target.value))}
                        style={styles.slider}
                    />
                </div>
            </div>

            {/* Ticks & Real-Time Metrics */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(0,0,0,0.3)', padding: '6px 10px', borderRadius: 6 }}>
                <span style={{ fontSize: 11, color: '#94a3b8' }}>Ticks Exécutés</span>
                <span style={{ fontSize: 16, fontWeight: 700, color: '#38bdf8' }}>{tick.toLocaleString()}</span>
            </div>

            {/* Statistics */}
            <div style={styles.statGrid}>
                <div style={styles.statItem}>
                    <div style={styles.statValue}>{stats.totalPopulation}</div>
                    <div style={styles.statLabel}>Pop Total</div>
                </div>
                <div style={styles.statItem}>
                    <div style={{ ...styles.statValue, color: '#fbbf24' }}>{stats.totalWorkers}</div>
                    <div style={styles.statLabel}>Ouvrières</div>
                </div>
                <div style={styles.statItem}>
                    <div style={{ ...styles.statValue, color: '#f87171' }}>{stats.totalSoldiers}</div>
                    <div style={styles.statLabel}>Soldats</div>
                </div>
                <div style={styles.statItem}>
                    <div style={{ ...styles.statValue, color: '#4ade80' }}>{stats.totalFood.toFixed(0)}</div>
                    <div style={styles.statLabel}>Stock Nourriture</div>
                </div>
            </div>

            <PopulationGraph />

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
                        <div style={{ fontSize: 11, color: '#94a3b8' }}>
                            Sauvegarde la combinaison actuelle (Monde, Espèce, Nid, Proies/Prédateurs, Climat) avec la Master Seed <span style={{ color: '#f59e0b', fontWeight: 700 }}>#{masterSeed}</span>.
                        </div>
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
        </div>
    )
}
