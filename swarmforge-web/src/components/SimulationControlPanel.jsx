import React, { useState } from 'react'
import {
    Plus,
    Trash2,
    CheckCircle,
    Globe,
    Sun,
    Calendar,
    Clock,
    Server,
    Sliders,
    Layers,
    Bookmark,
    Save,
    Sparkles,
    ShieldAlert,
    Cpu
} from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import {
    DEFAULT_WORLD_PRESETS,
    DEFAULT_SPECIES_PRESETS,
    DEFAULT_NEST_PRESETS,
    DEFAULT_PREY_PREDATOR_PRESETS,
    DEFAULT_WEATHER_PRESETS,
    DEFAULT_SCENARIO_META_PRESETS
} from '../store/presetStore'
import { getTranslation } from '../i18n/translations'
import { showToast } from '../store/toastStore'

export default function SimulationControlPanel() {
    const {
        stepSeconds,
        setStepSeconds,
        selectedScenarioPresetId,
        setScenarioPresetId,
        selectedWorldPresetId,
        setWorldPresetId,
        selectedWeatherPresetId,
        setWeatherPresetId,
        masterSeed,
        setMasterSeed,
        scenarioDescription,
        setScenarioDescription,
        startDateTime,
        setStartDateTime,
        maxDuration,
        setMaxDuration,
        durationUnit,
        setDurationUnit,
        minPopStop,
        setMinPopStop,
        speciesCards,
        addSpeciesCard,
        removeSpeciesCard,
        updateSpeciesCard,
        applyScenarioSetup,
        checkpoints,
        createCheckpoint,
        restoreCheckpoint,
        serverHost,
        setServerHost,
        serverPort,
        setServerPort,
        connected,
        serverStatusText,
        connect,
        disconnect,
        discover,
        theme,
        language,
        exportScenarioJson,
        importScenarioJson,
        isRealWeatherMode,
        realWeatherCity,
        realWeatherStatus,
        setRealWeatherMode,
        setRealWeatherCity,
        fetchRealWeather
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const [isApplied, setIsApplied] = useState(false)
    const [checkpointName, setCheckpointName] = useState('')

    const handleApply = () => {
        applyScenarioSetup()
        setIsApplied(true)
        showToast('✓ Scénario initialisé et appliqué avec succès !', 'success')
        setTimeout(() => setIsApplied(false), 2500)
    }

    const handleCreateCp = () => {
        const cp = createCheckpoint(checkpointName)
        setCheckpointName('')
        showToast(`✓ Point de contrôle "${cp.name}" créé au tick ${cp.tick} !`, 'info')
    }

    const cardBg = isDark ? '#1e293b' : '#ffffff'
    const borderCol = isDark ? '#334155' : '#e2e8f0'
    const inputBg = isDark ? '#0f172a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    return (
        <div style={{
            maxWidth: 1100,
            margin: '0 auto',
            padding: '20px 24px',
            display: 'flex',
            flexDirection: 'column',
            gap: 20
        }}>
            {/* Header Title & Primary Action */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10 }}>
                <div>
                    <h2 style={{ margin: 0, fontSize: 18, fontWeight: 800, color: '#38bdf8' }}>
                        ⚙️ {t('simControlsTitle', 'Gestionnaire & Contrôles de Simulation')}
                    </h2>
                    <p style={{ margin: '4px 0 0', fontSize: 12, color: textMuted }}>
                        Configuration stricte 1:1 : Scénario global, Biotope, Climat, Colonies et Castes IA.
                    </p>
                </div>

                <button
                    onClick={handleApply}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 8,
                        background: 'linear-gradient(135deg, #0284c7 0%, #2563eb 100%)',
                        color: '#ffffff',
                        border: 'none',
                        padding: '10px 22px',
                        borderRadius: 8,
                        fontSize: 13,
                        fontWeight: 800,
                        cursor: 'pointer',
                        boxShadow: '0 4px 14px rgba(2, 132, 199, 0.4)'
                    }}
                >
                    <CheckCircle size={16} />
                    {isApplied ? 'Scénario Appliqué !' : '🚀 APPLIQUER & INITIALISER LE SCÉNARIO'}
                </button>
            </div>

            {/* Top Meta-Scenario Preset Card */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1, minWidth: 280 }}>
                    <Sparkles size={20} color="#f59e0b" />
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4, width: '100%' }}>
                        <span style={{ fontSize: 12, fontWeight: 700, color: textMuted }}>
                            Modèle Pré-configuré de Scénario Global :
                        </span>
                        <select
                            value={selectedScenarioPresetId}
                            onChange={(e) => setScenarioPresetId(e.target.value)}
                            style={{
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 6,
                                padding: '6px 10px',
                                fontSize: 13,
                                fontWeight: 700
                            }}
                        >
                            {DEFAULT_SCENARIO_META_PRESETS.map(s => (
                                <option key={s.id} value={s.id}>{s.name} ({s.academicCategory})</option>
                            ))}
                        </select>
                    </div>
                </div>

                {/* Scenario Actions (Save, Export, Import) */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
                    <button
                        onClick={exportScenarioJson}
                        title="Exporter la configuration du scénario au format JSON"
                        style={{
                            background: isDark ? '#334155' : '#e2e8f0',
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '6px 10px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 4
                        }}
                    >
                        <Save size={12} /> Exporter JSON
                    </button>

                    <label
                        title="Importer un fichier JSON de scénario"
                        style={{
                            background: isDark ? '#334155' : '#e2e8f0',
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '6px 10px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 4
                        }}
                    >
                        <Sparkles size={12} /> Importer JSON
                        <input
                            type="file"
                            accept=".json"
                            style={{ display: 'none' }}
                            onChange={(e) => {
                                const file = e.target.files?.[0]
                                if (file) {
                                    const reader = new FileReader()
                                    reader.onload = (ev) => {
                                        const ok = importScenarioJson(ev.target.result)
                                        if (ok) showToast('✓ Scénario JSON importé avec succès !', 'success')
                                        else showToast('Erreur lors de l\'import du JSON', 'error')
                                    }
                                    reader.readAsText(file)
                                }
                            }}
                        />
                    </label>

                    <div style={{ width: 1, height: 16, background: borderCol, margin: '0 4px' }} />

                    <Bookmark size={15} color="#38bdf8" />
                    <input
                        type="text"
                        placeholder="Nom du CP..."
                        value={checkpointName}
                        onChange={(e) => setCheckpointName(e.target.value)}
                        style={{ width: 120, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '5px 8px', fontSize: 11 }}
                    />
                    <button
                        onClick={handleCreateCp}
                        style={{
                            background: isDark ? '#334155' : '#e2e8f0',
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '5px 8px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Save size={11} /> Créer CP
                    </button>

                    {checkpoints.length > 0 && (
                        <select
                            onChange={(e) => restoreCheckpoint(e.target.value)}
                            defaultValue=""
                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '5px 8px', fontSize: 11 }}
                        >
                            <option value="" disabled>Restaurer CP ({checkpoints.length})...</option>
                            {checkpoints.map(cp => (
                                <option key={cp.id} value={cp.id}>{cp.name} (T={cp.tick})</option>
                            ))}
                        </select>
                    )}
                </div>
            </div>

            {/* 1. World & Weather Preset Selection */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
                gap: 16
            }}>
                {/* World Preset Card */}
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 10, padding: '16px', display: 'flex', flexDirection: 'column', gap: 10 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#38bdf8' }}>
                        <Globe size={18} />
                        <span style={{ fontSize: 14, fontWeight: 800 }}>1. Preset de Monde (Biotope)</span>
                    </div>

                    <select
                        value={selectedWorldPresetId}
                        onChange={(e) => setWorldPresetId(e.target.value)}
                        style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '8px 10px', fontSize: 13, fontWeight: 600 }}
                    >
                        {DEFAULT_WORLD_PRESETS.map(w => (
                            <option key={w.id} value={w.id}>{w.name}</option>
                        ))}
                    </select>

                    <p style={{ margin: 0, fontSize: 11, color: textMuted, lineHeight: 1.4 }}>
                        {DEFAULT_WORLD_PRESETS.find(w => w.id === selectedWorldPresetId)?.description}
                    </p>
                </div>

                {/* Weather Preset Card (Simulated vs Real Open-Meteo) */}
                <div style={{ background: cardBg, border: `1px solid ${borderCol}`, borderRadius: 10, padding: '16px', display: 'flex', flexDirection: 'column', gap: 10 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#f59e0b' }}>
                            <Sun size={18} />
                            <span style={{ fontSize: 14, fontWeight: 800 }}>2. Profil Météo & Climat</span>
                        </div>

                        {/* Mode Switcher: Simulée vs Réelle */}
                        <div style={{ display: 'flex', background: inputBg, border: `1px solid ${borderCol}`, borderRadius: 6, padding: 2 }}>
                            <button
                                onClick={() => setRealWeatherMode(false)}
                                style={{
                                    background: !isRealWeatherMode ? '#0284c7' : 'transparent',
                                    color: !isRealWeatherMode ? '#fff' : textMuted,
                                    border: 'none',
                                    borderRadius: 4,
                                    padding: '3px 8px',
                                    fontSize: 10,
                                    fontWeight: 700,
                                    cursor: 'pointer'
                                }}
                            >
                                Simulée
                            </button>
                            <button
                                onClick={() => {
                                    setRealWeatherMode(true)
                                    fetchRealWeather(realWeatherCity)
                                }}
                                style={{
                                    background: isRealWeatherMode ? '#0284c7' : 'transparent',
                                    color: isRealWeatherMode ? '#fff' : textMuted,
                                    border: 'none',
                                    borderRadius: 4,
                                    padding: '3px 8px',
                                    fontSize: 10,
                                    fontWeight: 700,
                                    cursor: 'pointer'
                                }}
                            >
                                Réelle
                            </button>
                        </div>
                    </div>

                    {!isRealWeatherMode ? (
                        <>
                            <select
                                value={selectedWeatherPresetId}
                                onChange={(e) => setWeatherPresetId(e.target.value)}
                                style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '8px 10px', fontSize: 13, fontWeight: 600 }}
                            >
                                {DEFAULT_WEATHER_PRESETS.map(w => (
                                    <option key={w.id} value={w.id}>{w.name}</option>
                                ))}
                            </select>

                            <p style={{ margin: 0, fontSize: 11, color: textMuted, lineHeight: 1.4 }}>
                                {DEFAULT_WEATHER_PRESETS.find(w => w.id === selectedWeatherPresetId)?.description}
                            </p>
                        </>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                            <div style={{ display: 'flex', gap: 6 }}>
                                <input
                                    type="text"
                                    placeholder="Ville (ex: Paris, Manaus, Sydney)..."
                                    value={realWeatherCity}
                                    onChange={(e) => setRealWeatherCity(e.target.value)}
                                    onKeyDown={(e) => { if (e.key === 'Enter') fetchRealWeather(realWeatherCity) }}
                                    style={{ flex: 1, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '6px 8px', fontSize: 12 }}
                                />
                                <button
                                    onClick={() => fetchRealWeather(realWeatherCity)}
                                    style={{
                                        background: '#0284c7',
                                        color: '#fff',
                                        border: 'none',
                                        borderRadius: 6,
                                        padding: '6px 12px',
                                        fontSize: 11,
                                        fontWeight: 700,
                                        cursor: 'pointer'
                                    }}
                                >
                                    Obtenir
                                </button>
                            </div>
                            <div style={{ fontSize: 11, color: '#38bdf8', fontStyle: 'italic' }}>
                                {realWeatherStatus}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* 2. Scenario Settings & Deterministic Seed */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px',
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                gap: 16
            }}>
                <div>
                    <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                        🎲 Graine Aléatoire (Master Seed) :
                    </label>
                    <input
                        type="number"
                        value={masterSeed}
                        onChange={(e) => setMasterSeed(e.target.value)}
                        style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '8px 10px', fontSize: 13 }}
                    />
                </div>

                <div>
                    <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                        📅 Date & Heure de Départ :
                    </label>
                    <input
                        type="datetime-local"
                        value={startDateTime.slice(0, 16)}
                        onChange={(e) => setStartDateTime(e.target.value ? `${e.target.value}:00` : '2026-03-20T08:00:00')}
                        style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '7px 10px', fontSize: 13 }}
                    />
                </div>

                <div>
                    <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                        ⏱ Durée Maximale :
                    </label>
                    <div style={{ display: 'flex', gap: 6 }}>
                        <input
                            type="number"
                            min="1"
                            value={maxDuration}
                            onChange={(e) => setMaxDuration(parseFloat(e.target.value))}
                            style={{ width: '60%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '8px 10px', fontSize: 13 }}
                        />
                        <select
                            value={durationUnit}
                            onChange={(e) => setDurationUnit(e.target.value)}
                            style={{ width: '40%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '8px 6px', fontSize: 12 }}
                        >
                            <option value="Seconds">Secondes</option>
                            <option value="Minutes">Minutes</option>
                            <option value="Hours">Heures</option>
                            <option value="Days">Jours</option>
                        </select>
                    </div>
                </div>

                <div>
                    <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                        🛑 Arrêt Pop. Minimale :
                    </label>
                    <input
                        type="number"
                        min="0"
                        value={minPopStop}
                        onChange={(e) => setMinPopStop(parseInt(e.target.value))}
                        placeholder="0 = Pas de limite"
                        style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 6, padding: '8px 10px', fontSize: 13 }}
                    />
                </div>
            </div>

            {/* 3. Multi-Species & Colony Scenario Setup Cards */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px',
                display: 'flex',
                flexDirection: 'column',
                gap: 16
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#10b981' }}>
                        <Layers size={18} />
                        <span style={{ fontSize: 14, fontWeight: 800 }}>3. Configuration des Espèces & Colonies ({speciesCards.length})</span>
                    </div>

                    <button
                        onClick={() => addSpeciesCard()}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 6,
                            background: isDark ? '#047857' : '#10b981',
                            color: '#fff',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: 6,
                            fontSize: 12,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Plus size={14} /> Ajouter une Colonie
                    </button>
                </div>

                {/* Cards List */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                    {speciesCards.map((card) => (
                        <div
                            key={card.id}
                            style={{
                                background: inputBg,
                                border: `1px solid ${borderCol}`,
                                borderLeft: `5px solid ${card.color || '#38bdf8'}`,
                                borderRadius: 8,
                                padding: '14px 16px',
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 12
                            }}
                        >
                            {/* Card Header */}
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                    <input
                                        type="color"
                                        value={card.color || '#38bdf8'}
                                        onChange={(e) => updateSpeciesCard(card.id, { color: e.target.value })}
                                        style={{ width: 26, height: 26, border: 'none', background: 'transparent', cursor: 'pointer' }}
                                    />
                                    <input
                                        type="text"
                                        value={card.name}
                                        onChange={(e) => updateSpeciesCard(card.id, { name: e.target.value })}
                                        style={{ fontSize: 13, fontWeight: 700, background: 'transparent', border: 'none', borderBottom: `1px solid ${borderCol}`, color: textMain, padding: '2px 4px' }}
                                    />
                                </div>

                                {speciesCards.length > 1 && (
                                    <button
                                        onClick={() => removeSpeciesCard(card.id)}
                                        title="Supprimer cette colonie"
                                        style={{ background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', padding: 4 }}
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                )}
                            </div>

                            {/* Card Grid Inputs */}
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 12 }}>
                                <div>
                                    <label style={{ fontSize: 11, color: textMuted, display: 'block', marginBottom: 4 }}>Espèce :</label>
                                    <select
                                        value={card.speciesId}
                                        onChange={(e) => updateSpeciesCard(card.id, { speciesId: e.target.value })}
                                        style={{ width: '100%', background: cardBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 6px', fontSize: 12 }}
                                    >
                                        {DEFAULT_SPECIES_PRESETS.map(s => (
                                            <option key={s.id} value={s.id}>{s.name}</option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label style={{ fontSize: 11, color: textMuted, display: 'block', marginBottom: 4 }}>Architecture du Nid :</label>
                                    <select
                                        value={card.nestType}
                                        onChange={(e) => updateSpeciesCard(card.id, { nestType: e.target.value })}
                                        style={{ width: '100%', background: cardBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 6px', fontSize: 12 }}
                                    >
                                        {DEFAULT_NEST_PRESETS.map(n => (
                                            <option key={n.id} value={n.nestType}>{n.name}</option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label style={{ fontSize: 11, color: textMuted, display: 'block', marginBottom: 4 }}>Moteur Cognitif IA :</label>
                                    <select
                                        value={card.aiArchitecture || 'BEHAVIOR_TREE'}
                                        onChange={(e) => updateSpeciesCard(card.id, { aiArchitecture: e.target.value })}
                                        style={{ width: '100%', background: cardBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 6px', fontSize: 12 }}
                                    >
                                        <option value="BEHAVIOR_TREE">Arbres de Comportement (BT)</option>
                                        <option value="GOAP">Planification de Buts (GOAP)</option>
                                        <option value="UTILITY_AI">Courbes d'Utilité (Utility)</option>
                                        <option value="HYBRID_RULE_UTILITY">Hybride Règles / Utilité</option>
                                    </select>
                                </div>

                                <div>
                                    <label style={{ fontSize: 11, color: textMuted, display: 'block', marginBottom: 4 }}>Écosystème Proies/Prédateurs :</label>
                                    <select
                                        value={card.preyPredatorPresetId || DEFAULT_PREY_PREDATOR_PRESETS[0].id}
                                        onChange={(e) => updateSpeciesCard(card.id, { preyPredatorPresetId: e.target.value })}
                                        style={{ width: '100%', background: cardBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 6px', fontSize: 12 }}
                                    >
                                        {DEFAULT_PREY_PREDATOR_PRESETS.map(p => (
                                            <option key={p.id} value={p.id}>{p.name}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>

                            {/* Caste Counts */}
                            <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', background: cardBg, padding: '8px 12px', borderRadius: 6, border: `1px solid ${borderCol}` }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <span style={{ fontSize: 11, fontWeight: 700 }}>👑 Reines :</span>
                                    <input
                                        type="number"
                                        min="0"
                                        value={card.initialQueens ?? 1}
                                        onChange={(e) => updateSpeciesCard(card.id, { initialQueens: parseInt(e.target.value) || 0 })}
                                        style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 5px', fontSize: 11 }}
                                    />
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <span style={{ fontSize: 11, fontWeight: 700 }}>🐜 Ouvrières :</span>
                                    <input
                                        type="number"
                                        min="0"
                                        value={card.initialWorkers ?? 40}
                                        onChange={(e) => updateSpeciesCard(card.id, { initialWorkers: parseInt(e.target.value) || 0 })}
                                        style={{ width: 55, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 5px', fontSize: 11 }}
                                    />
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <span style={{ fontSize: 11, fontWeight: 700 }}>🛡️ Soldats :</span>
                                    <input
                                        type="number"
                                        min="0"
                                        value={card.initialSoldiers ?? 10}
                                        onChange={(e) => updateSpeciesCard(card.id, { initialSoldiers: parseInt(e.target.value) || 0 })}
                                        style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 5px', fontSize: 11 }}
                                    />
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <span style={{ fontSize: 11, fontWeight: 700 }}>🐝 Mâles :</span>
                                    <input
                                        type="number"
                                        min="0"
                                        value={card.initialMales ?? 0}
                                        onChange={(e) => updateSpeciesCard(card.id, { initialMales: parseInt(e.target.value) || 0 })}
                                        style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 5px', fontSize: 11 }}
                                    />
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* 4. Engine Configuration (Default dt) */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#38bdf8' }}>
                    <Cpu size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>Paramètres du Moteur Physique & Pas de Temps (dt)</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{ fontSize: 12, color: textMuted }}>Pas temporel standard (\(\Delta t\)) :</span>
                        <select
                            value={stepSeconds}
                            onChange={(e) => setStepSeconds(parseFloat(e.target.value))}
                            style={{
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 6,
                                padding: '6px 12px',
                                fontSize: 13,
                                fontWeight: 700
                            }}
                        >
                            <option value="0.01">0.01 s (100 ticks/s — Ultra Haute Résolution)</option>
                            <option value="0.05">0.05 s (20 ticks/s — Standard SwarmForge)</option>
                            <option value="0.1">0.10 s (10 ticks/s — Économie CPU)</option>
                            <option value="0.5">0.50 s (2 ticks/s — Vue Macro)</option>
                            <option value="1.0">1.00 s (1 tick/s — Vue Longue Durée)</option>
                        </select>
                    </div>
                    <span style={{ fontSize: 11, color: textMuted }}>
                        Cadence cible standard : 20 TPS. Le pas de temps détermine la précision de l'intégration comportementale des insectes.
                    </span>
                </div>
            </div>

            {/* 5. Server Connection Box */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#38bdf8' }}>
                    <Server size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>Connexion Serveur Distant gRPC / WebSocket</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, color: textMuted }}>Hôte :</span>
                        <input
                            type="text"
                            value={serverHost}
                            onChange={(e) => setServerHost(e.target.value)}
                            style={{ width: 120, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 8px', fontSize: 12 }}
                        />
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, color: textMuted }}>Port :</span>
                        <input
                            type="number"
                            value={serverPort}
                            onChange={(e) => setServerPort(e.target.value)}
                            style={{ width: 75, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 8px', fontSize: 12 }}
                        />
                    </div>

                    {connected ? (
                        <button
                            onClick={disconnect}
                            style={{ background: '#ef4444', color: '#fff', border: 'none', padding: '6px 14px', borderRadius: 6, fontSize: 12, fontWeight: 700, cursor: 'pointer' }}
                        >
                            Déconnecter
                        </button>
                    ) : (
                        <button
                            onClick={connect}
                            style={{ background: '#0284c7', color: '#fff', border: 'none', padding: '6px 14px', borderRadius: 6, fontSize: 12, fontWeight: 700, cursor: 'pointer' }}
                        >
                            🌐 Connecter
                        </button>
                    )}

                    <button
                        onClick={discover}
                        style={{ background: isDark ? '#334155' : '#e2e8f0', color: textMain, border: 'none', padding: '6px 14px', borderRadius: 6, fontSize: 12, fontWeight: 700, cursor: 'pointer' }}
                    >
                        🔍 Détecter
                    </button>

                    <span style={{ fontSize: 12, fontWeight: 600, color: connected ? '#10b981' : (isDark ? '#94a3b8' : '#64748b'), marginLeft: 'auto' }}>
                        {serverStatusText}
                    </span>
                </div>
            </div>
        </div>
    )
}
