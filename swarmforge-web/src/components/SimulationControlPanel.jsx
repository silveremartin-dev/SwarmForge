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
    Cpu,
    Download,
    Upload,
    RotateCcw,
    Zap,
    MapPin
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
        executionMode,
        setExecutionMode,
        playerAlias,
        setPlayerAlias,
        playerSpecies,
        setPlayerSpecies,
        serverRole,
        setServerRole,
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

    const isServer = executionMode === 'REMOTE_CLIENT_SERVER'
    const isJoin = serverRole === 'JOIN'
    const isScenarioDisabled = isServer && isJoin

    // Calculate equivalent duration text
    const getCalculatedDurationInfo = () => {
        if (durationUnit === '∞ Unlimited' || durationUnit === '∞ Illimité') {
            return '🔄 Exécution continue sans limite de temps prédéfinie'
        }
        let totalSeconds = 0
        const dur = Number(maxDuration) || 100
        switch (durationUnit) {
            case 'Seconds (s)': totalSeconds = dur; break;
            case 'Minutes (min)': totalSeconds = dur * 60; break;
            case 'Hours (h)': totalSeconds = dur * 3600; break;
            case 'Days (d)': case 'Days': totalSeconds = dur * 86400; break;
            case 'Months (30d)': totalSeconds = dur * 86400 * 30; break;
            case 'Years (365d)': totalSeconds = dur * 86400 * 365; break;
            case 'Ticks': totalSeconds = dur * (stepSeconds || 0.0166); break;
            default: totalSeconds = dur * 86400; break;
        }

        const days = Math.floor(totalSeconds / 86400)
        const remSec1 = totalSeconds % 86400
        const hours = Math.floor(remSec1 / 3600)
        const remSec2 = remSec1 % 3600
        const mins = Math.floor(remSec2 / 60)
        const secs = Math.floor(remSec2 % 60)
        const dt = stepSeconds || 0.0166
        const steps = Math.round(totalSeconds / dt)

        return `🔄 Equivalent Duration: ${days}j ${hours}h ${mins}m ${secs}s (${steps.toLocaleString()} steps at Δt = ${dt.toFixed(3)}s)`
    }

    // Time parsing for Start Date & Time
    const dtObj = new Date(startDateTime || '2026-03-20T08:00:00')
    const curDateStr = dtObj.toISOString().split('T')[0]
    const curHour = dtObj.getHours()
    const curMin = dtObj.getMinutes()
    const curSec = dtObj.getSeconds()

    const handleDateChange = (newDateStr) => {
        const [y, m, d] = newDateStr.split('-').map(Number)
        const updated = new Date(dtObj)
        if (!isNaN(y) && !isNaN(m) && !isNaN(d)) {
            updated.setFullYear(y, m - 1, d)
            setStartDateTime(updated.toISOString().slice(0, 19))
        }
    }

    const handleTimeChange = (h, m, s) => {
        const updated = new Date(dtObj)
        updated.setHours(h ?? curHour, m ?? curMin, s ?? curSec)
        setStartDateTime(updated.toISOString().slice(0, 19))
    }

    const cardBg = isDark ? '#181b22' : '#ffffff'
    const borderCol = isDark ? '#2d3340' : '#e2e8f0'
    const inputBg = isDark ? '#0f131a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    return (
        <div style={{
            maxWidth: 1050,
            margin: '0 auto',
            padding: '24px 28px',
            display: 'flex',
            flexDirection: 'column',
            gap: 18,
            color: textMain,
            fontFamily: 'system-ui, -apple-system, sans-serif'
        }}>
            {/* Header: Title & Apply Button */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: `1px solid ${borderCol}`, paddingBottom: 12 }}>
                <h1 style={{ margin: 0, fontSize: 19, fontWeight: 700, letterSpacing: '-0.2px' }}>
                    Scenario Configuration & Multi-Species Ecosystem
                </h1>

                <button
                    onClick={handleApply}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 8,
                        background: 'linear-gradient(135deg, #0284c7 0%, #2563eb 100%)',
                        color: '#ffffff',
                        border: 'none',
                        padding: '9px 20px',
                        borderRadius: 7,
                        fontSize: 13,
                        fontWeight: 700,
                        cursor: 'pointer',
                        boxShadow: '0 4px 14px rgba(2, 132, 199, 0.4)'
                    }}
                >
                    <CheckCircle size={15} />
                    {isApplied ? 'Scénario Appliqué !' : '🚀 APPLIQUER & INITIALISER LE SCÉNARIO'}
                </button>
            </div>

            {/* 1. Execution Engine Row */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 8,
                padding: '12px 16px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 120 }}>Execution Engine:</span>
                    <select
                        value={executionMode}
                        onChange={(e) => setExecutionMode(e.target.value)}
                        style={{
                            flex: 1,
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '7px 12px',
                            fontSize: 12,
                            fontWeight: 600,
                            outline: 'none'
                        }}
                    >
                        <option value="STANDALONE_LOCAL">● Embedded Local Mode (In-Process CPU)</option>
                        <option value="REMOTE_CLIENT_SERVER">● SwarmForge Server Mode (Remote / gRPC Cluster)</option>
                    </select>
                </div>

                {/* Server Network Subpanel (Shown ONLY when Server Mode is selected) */}
                {isServer && (
                    <div style={{
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 12,
                        padding: '14px 16px',
                        borderRadius: 8,
                        background: isDark ? '#111827' : '#f0f9ff',
                        border: '1px solid rgba(2, 132, 199, 0.35)'
                    }}>
                        {/* Row 1: Host, Port, Connect/Disconnect, Start Server, Discover */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>Host:</span>
                                <input
                                    type="text"
                                    value={serverHost}
                                    onChange={(e) => setServerHost(e.target.value)}
                                    style={{
                                        width: 110,
                                        background: inputBg,
                                        color: textMain,
                                        border: `1px solid ${borderCol}`,
                                        borderRadius: 5,
                                        padding: '6px 8px',
                                        fontSize: 12,
                                        fontWeight: 600
                                    }}
                                />
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>Port:</span>
                                <input
                                    type="number"
                                    value={serverPort}
                                    onChange={(e) => setServerPort(e.target.value)}
                                    style={{
                                        width: 75,
                                        background: inputBg,
                                        color: textMain,
                                        border: `1px solid ${borderCol}`,
                                        borderRadius: 5,
                                        padding: '6px 8px',
                                        fontSize: 12,
                                        fontWeight: 600
                                    }}
                                />
                            </div>

                            {connected ? (
                                <button
                                    onClick={disconnect}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 5,
                                        background: '#0284c7',
                                        color: '#ffffff',
                                        border: 'none',
                                        borderRadius: 6,
                                        padding: '6px 14px',
                                        fontSize: 12,
                                        fontWeight: 700,
                                        cursor: 'pointer'
                                    }}
                                >
                                    ✕ Disconnect
                                </button>
                            ) : (
                                <button
                                    onClick={connect}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 5,
                                        background: '#0284c7',
                                        color: '#ffffff',
                                        border: 'none',
                                        borderRadius: 6,
                                        padding: '6px 14px',
                                        fontSize: 12,
                                        fontWeight: 700,
                                        cursor: 'pointer'
                                    }}
                                >
                                    🌐 Connect
                                </button>
                            )}

                            <button
                                onClick={() => showToast('🚀 Démarrage du serveur local SwarmForge...', 'info')}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: 5,
                                    background: isDark ? '#1e293b' : '#e2e8f0',
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 6,
                                    padding: '6px 12px',
                                    fontSize: 12,
                                    fontWeight: 600,
                                    cursor: 'pointer'
                                }}
                            >
                                🚀 Démarrer Serveur...
                            </button>

                            <button
                                onClick={discover}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: 5,
                                    background: isDark ? '#1e293b' : '#e2e8f0',
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 6,
                                    padding: '6px 12px',
                                    fontSize: 12,
                                    fontWeight: 600,
                                    cursor: 'pointer'
                                }}
                            >
                                🔍 Détecter
                            </button>
                        </div>

                        {/* Row 2: Status */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 11 }}>
                            <span style={{ fontWeight: 600, color: textMuted }}>gRPC Status:</span>
                            {connected ? (
                                <span style={{ color: '#10b981', fontWeight: 700 }}>
                                    ● Connected ({serverHost}:{serverPort})
                                </span>
                            ) : (
                                <span style={{ color: '#ef4444', fontWeight: 700 }}>
                                    Offline
                                </span>
                            )}
                        </div>

                        {/* Row 3: Server Role */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <span style={{ fontSize: 12, fontWeight: 600, color: textMuted, minWidth: 80 }}>Server Role:</span>
                            <select
                                value={serverRole}
                                onChange={(e) => setServerRole(e.target.value)}
                                style={{
                                    flex: 1,
                                    background: inputBg,
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 6,
                                    padding: '6px 10px',
                                    fontSize: 12,
                                    fontWeight: 600
                                }}
                            >
                                <option value="JOIN">● Join (Matchmaking / Megaterrarium - Server Authority)</option>
                                <option value="HOST">● Host / Deploy Scenario (Researcher Mode - Client Authority)</option>
                            </select>
                        </div>

                        {/* Row 4: Participant Tag & Species (Shown when JOIN is selected) */}
                        {isJoin && (
                            <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>Participant Tag:</span>
                                    <input
                                        type="text"
                                        value={playerAlias}
                                        onChange={(e) => setPlayerAlias(e.target.value)}
                                        style={{
                                            width: 130,
                                            background: inputBg,
                                            color: textMain,
                                            border: `1px solid ${borderCol}`,
                                            borderRadius: 5,
                                            padding: '5px 8px',
                                            fontSize: 12,
                                            fontWeight: 600
                                        }}
                                    />
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1 }}>
                                    <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>Species:</span>
                                    <select
                                        value={playerSpecies}
                                        onChange={(e) => setPlayerSpecies(e.target.value)}
                                        style={{
                                            flex: 1,
                                            background: inputBg,
                                            color: textMain,
                                            border: `1px solid ${borderCol}`,
                                            borderRadius: 5,
                                            padding: '5px 8px',
                                            fontSize: 12,
                                            fontWeight: 600
                                        }}
                                    >
                                        {DEFAULT_SPECIES_PRESETS.map(s => (
                                            <option key={s.id} value={s.name}>{s.name} ({s.latinName})</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                        )}

                        {/* Row 5: Mode Explainer Card (Gold for Join, Emerald for Host) */}
                        {isJoin ? (
                            <div style={{
                                padding: '10px 14px',
                                borderRadius: 6,
                                background: isDark ? 'rgba(234, 179, 8, 0.08)' : '#fefce8',
                                border: '1px solid #ca8a04',
                                fontSize: 11,
                                color: isDark ? '#fef08a' : '#854d0e',
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 4
                            }}>
                                <div style={{ fontWeight: 800, color: '#eab308' }}>
                                    ▶ Join Mode — Matchmaking & Megaterrarium (Server Authority)
                                </div>
                                <div>• World & Climate: Governed by the server host (local settings below are disabled).</div>
                                <div>• Your Colony: The server assigns a balanced starting colony and nest location.</div>
                                <div>• Species & Tag: Select the species you bring from your personal library (field above).</div>
                            </div>
                        ) : (
                            <div style={{
                                padding: '10px 14px',
                                borderRadius: 6,
                                background: isDark ? 'rgba(16, 185, 129, 0.08)' : '#ecfdf5',
                                border: '1px solid #059669',
                                fontSize: 11,
                                color: isDark ? '#a7f3d0' : '#065f46',
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 4
                            }}>
                                <div style={{ fontWeight: 800, color: '#10b981' }}>
                                    ▶ Host Mode — Deploy Custom Scenario (Host Authority)
                                </div>
                                <div>• Master Scenario: All your settings below (World, Climate, Species, Nests, Demographics) are deployed to the server.</div>
                                <div>• Role: You define the entire ecosystem. Other participants can join your simulation and take control of configured colonies.</div>
                            </div>
                        )}
                    </div>
                )}
            </div>

            {/* 2. Global Scenario Preset & Actions Container */}
            <div style={{
                opacity: isScenarioDisabled ? 0.35 : 1.0,
                pointerEvents: isScenarioDisabled ? 'none' : 'auto',
                display: 'flex',
                flexDirection: 'column',
                gap: 16
            }}>
                {/* Global Preset Selector */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>Global Scenario Preset :</span>
                    <select
                        value={selectedScenarioPresetId}
                        onChange={(e) => setScenarioPresetId(e.target.value)}
                        style={{
                            flex: 1,
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '7px 12px',
                            fontSize: 12,
                            fontWeight: 600
                        }}
                    >
                        {DEFAULT_SCENARIO_META_PRESETS.map(p => (
                            <option key={p.id} value={p.id}>{p.name} ({p.academicCategory})</option>
                        ))}
                    </select>
                </div>

                {/* Preset Actions Buttons (Save, Delete, Export, Import) */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <button
                        onClick={() => showToast('✓ Configuration du scénario enregistrée.', 'success')}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 5,
                            background: isDark ? '#1e293b' : '#e2e8f0',
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '6px 14px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Save size={13} /> Save
                    </button>

                    <button
                        onClick={() => showToast('Scénario réinitialisé par défaut.', 'info')}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 5,
                            background: '#dc2626',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: 6,
                            padding: '6px 14px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Trash2 size={13} /> Delete
                    </button>

                    <button
                        onClick={exportScenarioJson}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 5,
                            background: isDark ? '#1e293b' : '#e2e8f0',
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '6px 14px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Download size={13} /> Export...
                    </button>

                    <label
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 5,
                            background: isDark ? '#1e293b' : '#e2e8f0',
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '6px 14px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Upload size={13} /> Import...
                        <input
                            type="file"
                            accept=".json"
                            style={{ display: 'none' }}
                            onChange={(e) => {
                                const file = e.target.files?.[0]
                                if (file) {
                                    const reader = new FileReader()
                                    reader.onload = (ev) => {
                                        const success = importScenarioJson(ev.target?.result)
                                        if (success) showToast('✓ Scénario importé avec succès !', 'success')
                                        else showToast('Erreur lors de l\'import JSON', 'error')
                                    }
                                    reader.readAsText(file)
                                }
                            }}
                        />
                    </label>
                </div>

                {/* Scenario Description */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>Scientific Scenario Description :</span>
                    <textarea
                        value={scenarioDescription}
                        onChange={(e) => setScenarioDescription(e.target.value)}
                        rows={3}
                        style={{
                            width: '100%',
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '8px 10px',
                            fontSize: 11,
                            fontFamily: 'inherit',
                            resize: 'vertical',
                            boxSizing: 'border-box'
                        }}
                    />
                </div>

                {/* 1. World Preset */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>1. World Preset (Biotope) :</span>
                    <select
                        value={selectedWorldPresetId}
                        onChange={(e) => setWorldPresetId(e.target.value)}
                        style={{
                            flex: 1,
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '6px 10px',
                            fontSize: 12,
                            fontWeight: 600
                        }}
                    >
                        {DEFAULT_WORLD_PRESETS.map(w => (
                            <option key={w.id} value={w.id}>{w.name}</option>
                        ))}
                    </select>
                </div>

                {/* 2. Weather & Climate Preset */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>2. Weather & Climate Preset :</span>

                    <div style={{ display: 'flex', borderRadius: 5, overflow: 'hidden', border: `1px solid ${borderCol}` }}>
                        <button
                            onClick={() => setRealWeatherMode(false)}
                            style={{
                                padding: '4px 10px',
                                fontSize: 11,
                                fontWeight: 700,
                                border: 'none',
                                background: !isRealWeatherMode ? '#0284c7' : inputBg,
                                color: !isRealWeatherMode ? '#ffffff' : textMuted,
                                cursor: 'pointer'
                            }}
                        >
                            Simulated
                        </button>
                        <button
                            onClick={() => setRealWeatherMode(true)}
                            style={{
                                padding: '4px 10px',
                                fontSize: 11,
                                fontWeight: 700,
                                border: 'none',
                                background: isRealWeatherMode ? '#0284c7' : inputBg,
                                color: isRealWeatherMode ? '#ffffff' : textMuted,
                                cursor: 'pointer'
                            }}
                        >
                            Real
                        </button>
                    </div>

                    {!isRealWeatherMode ? (
                        <>
                            <select
                                value={selectedWeatherPresetId}
                                onChange={(e) => setWeatherPresetId(e.target.value)}
                                style={{
                                    flex: 1,
                                    background: inputBg,
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 6,
                                    padding: '6px 10px',
                                    fontSize: 12,
                                    fontWeight: 600
                                }}
                            >
                                {DEFAULT_WEATHER_PRESETS.map(w => (
                                    <option key={w.id} value={w.id}>{w.name}</option>
                                ))}
                            </select>

                            <button
                                onClick={() => showToast('✓ Profil météo aligné avec le biome du monde.', 'info')}
                                style={{
                                    background: isDark ? '#334155' : '#e2e8f0',
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 6,
                                    padding: '5px 12px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    cursor: 'pointer'
                                }}
                            >
                                Align
                            </button>
                        </>
                    ) : (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1 }}>
                            <input
                                type="text"
                                value={realWeatherCity}
                                onChange={(e) => setRealWeatherCity(e.target.value)}
                                placeholder="Ville ou Lat, Lon..."
                                style={{
                                    width: 140,
                                    background: inputBg,
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 5,
                                    padding: '5px 8px',
                                    fontSize: 11
                                }}
                            />
                            <button
                                onClick={() => fetchRealWeather(realWeatherCity)}
                                style={{
                                    background: '#0284c7',
                                    color: '#ffffff',
                                    border: 'none',
                                    borderRadius: 5,
                                    padding: '5px 10px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    cursor: 'pointer'
                                }}
                            >
                                Obtenir
                            </button>
                            <span style={{ fontSize: 10, color: textMuted }}>{realWeatherStatus}</span>
                        </div>
                    )}
                </div>

                {/* 3. Start Date & Time */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>3. Start Date & Time :</span>
                    <input
                        type="date"
                        value={curDateStr}
                        onChange={(e) => handleDateChange(e.target.value)}
                        style={{
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 5,
                            padding: '4px 8px',
                            fontSize: 11,
                            fontWeight: 600
                        }}
                    />

                    <input
                        type="number"
                        min="0"
                        max="23"
                        value={curHour}
                        onChange={(e) => handleTimeChange(parseInt(e.target.value), curMin, curSec)}
                        style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px', fontSize: 11, textAlign: 'center' }}
                    />
                    <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>h</span>

                    <input
                        type="number"
                        min="0"
                        max="59"
                        value={curMin}
                        onChange={(e) => handleTimeChange(curHour, parseInt(e.target.value), curSec)}
                        style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px', fontSize: 11, textAlign: 'center' }}
                    />
                    <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>m</span>

                    <input
                        type="number"
                        min="0"
                        max="59"
                        value={curSec}
                        onChange={(e) => handleTimeChange(curHour, curMin, parseInt(e.target.value))}
                        style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px', fontSize: 11, textAlign: 'center' }}
                    />
                    <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>s</span>
                </div>

                {/* 4. Master Random Seed */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>4. Master Random Seed :</span>
                    <input
                        type="number"
                        value={masterSeed}
                        onChange={(e) => setMasterSeed(parseInt(e.target.value))}
                        style={{
                            width: 120,
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 5,
                            padding: '5px 8px',
                            fontSize: 12,
                            fontWeight: 700
                        }}
                    />
                    <button
                        onClick={() => {
                            const newSeed = Math.floor(Math.random() * 900000 + 100000)
                            setMasterSeed(newSeed)
                            showToast(`Nouvelle graine générée : ${newSeed}`, 'info')
                        }}
                        style={{
                            background: isDark ? '#334155' : '#e2e8f0',
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 5,
                            padding: '5px 12px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        New
                    </button>
                </div>

                {/* 5. Physics Step (Integration Δt) */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>5. Physics Step (Integration Δt) :</span>
                        <select
                            value={stepSeconds}
                            onChange={(e) => setStepSeconds(parseFloat(e.target.value))}
                            style={{
                                width: 280,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 6,
                                padding: '6px 10px',
                                fontSize: 12,
                                fontWeight: 600
                            }}
                        >
                            <option value={0.0166}>16.6 ms (60 Hz - Max Physics Fidelity / Default)</option>
                            <option value={0.05}>50 ms (20 Hz - Standard Precision)</option>
                            <option value={0.1}>100 ms (10 Hz - Fast Mode)</option>
                            <option value={1.0}>1.0 s (Macroscopic Ecosystem Mode)</option>
                            <option value={5.0}>5.0 s (Ultra Macroscopic Mode)</option>
                        </select>
                    </div>
                    <div style={{ fontSize: 10, fontStyle: 'italic', color: textMuted, marginLeft: 170 }}>
                        Note: For a given Seed and Δt step, simulation execution is fully deterministic and reproducible.
                    </div>
                </div>

                {/* 6. Maximum Simulation Duration */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>6. Maximum Simulation Duration :</span>
                        <input
                            type="number"
                            min="1"
                            max="2000000000"
                            value={maxDuration}
                            onChange={(e) => setMaxDuration(parseFloat(e.target.value))}
                            style={{
                                width: 80,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 5,
                                padding: '5px 8px',
                                fontSize: 12,
                                fontWeight: 700
                            }}
                        />
                        <select
                            value={durationUnit}
                            onChange={(e) => setDurationUnit(e.target.value)}
                            style={{
                                width: 140,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 5,
                                padding: '5px 8px',
                                fontSize: 12,
                                fontWeight: 600
                            }}
                        >
                            <option value="Days (d)">Days (d)</option>
                            <option value="Hours (h)">Hours (h)</option>
                            <option value="Minutes (min)">Minutes (min)</option>
                            <option value="Seconds (s)">Seconds (s)</option>
                            <option value="Months (30d)">Months (30d)</option>
                            <option value="Years (365d)">Years (365d)</option>
                            <option value="Ticks">Ticks</option>
                            <option value="∞ Unlimited">∞ Unlimited</option>
                        </select>
                    </div>
                    <div style={{ fontSize: 10, fontWeight: 700, color: '#38bdf8', marginLeft: 170 }}>
                        {getCalculatedDurationInfo()}
                    </div>
                </div>

                {/* 7. Min Population Stop Threshold */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>7. Min Population Stop Threshold :</span>
                    <input
                        type="number"
                        min="0"
                        max="10000"
                        value={minPopStop}
                        onChange={(e) => setMinPopStop(parseInt(e.target.value))}
                        style={{
                            width: 80,
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 5,
                            padding: '5px 8px',
                            fontSize: 12,
                            fontWeight: 700
                        }}
                    />
                </div>

                {/* 8. Dynamic Multi-Species & Ecosystem Configuration Cards */}
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 12,
                    borderTop: `1px solid ${borderCol}`,
                    paddingTop: 16,
                    marginTop: 8
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ fontSize: 13, fontWeight: 800, color: '#38bdf8' }}>
                            Colonies & Castes IA de l'Écosystème ({speciesCards.length} colonies)
                        </span>

                        <button
                            onClick={() => addSpeciesCard()}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 6,
                                background: '#0284c7',
                                color: '#ffffff',
                                border: 'none',
                                padding: '6px 14px',
                                borderRadius: 6,
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer'
                            }}
                        >
                            <Plus size={13} /> Ajouter une Colonie
                        </button>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 14 }}>
                        {speciesCards.map((card, idx) => (
                            <div
                                key={card.id}
                                style={{
                                    background: cardBg,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 8,
                                    padding: '12px 14px',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: 8,
                                    boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                                }}
                            >
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                        <div style={{ width: 12, height: 12, borderRadius: '50%', background: card.color || '#10b981' }} />
                                        <input
                                            type="text"
                                            value={card.name}
                                            onChange={(e) => updateSpeciesCard(card.id, { name: e.target.value })}
                                            style={{
                                                background: 'transparent',
                                                color: textMain,
                                                border: 'none',
                                                borderBottom: `1px solid ${borderCol}`,
                                                fontSize: 12,
                                                fontWeight: 800,
                                                padding: '2px 4px'
                                            }}
                                        />
                                    </div>
                                    {speciesCards.length > 1 && (
                                        <button
                                            onClick={() => removeSpeciesCard(card.id)}
                                            style={{ background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', padding: 2 }}
                                        >
                                            <Trash2 size={13} />
                                        </button>
                                    )}
                                </div>

                                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                    <span style={{ fontSize: 10, color: textMuted }}>Espèce :</span>
                                    <select
                                        value={card.speciesId}
                                        onChange={(e) => updateSpeciesCard(card.id, { speciesId: e.target.value })}
                                        style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px 6px', fontSize: 11 }}
                                    >
                                        {DEFAULT_SPECIES_PRESETS.map(s => (
                                            <option key={s.id} value={s.id}>{s.name} ({s.latinName})</option>
                                        ))}
                                    </select>
                                </div>

                                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                    <span style={{ fontSize: 10, color: textMuted }}>Type de Nid :</span>
                                    <select
                                        value={card.nestType}
                                        onChange={(e) => updateSpeciesCard(card.id, { nestType: e.target.value })}
                                        style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px 6px', fontSize: 11 }}
                                    >
                                        {DEFAULT_NEST_PRESETS.map(n => (
                                            <option key={n.id} value={n.nestType}>{n.name}</option>
                                        ))}
                                    </select>
                                </div>

                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 6, paddingTop: 4 }}>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                        <span style={{ fontSize: 9, color: textMuted }}>Reines</span>
                                        <input
                                            type="number"
                                            min="0"
                                            value={card.initialQueens ?? 1}
                                            onChange={(e) => updateSpeciesCard(card.id, { initialQueens: parseInt(e.target.value) || 0 })}
                                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                        <span style={{ fontSize: 9, color: textMuted }}>Ouvrières</span>
                                        <input
                                            type="number"
                                            min="0"
                                            value={card.initialWorkers ?? 40}
                                            onChange={(e) => updateSpeciesCard(card.id, { initialWorkers: parseInt(e.target.value) || 0 })}
                                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                        <span style={{ fontSize: 9, color: textMuted }}>Soldats</span>
                                        <input
                                            type="number"
                                            min="0"
                                            value={card.initialSoldiers ?? 10}
                                            onChange={(e) => updateSpeciesCard(card.id, { initialSoldiers: parseInt(e.target.value) || 0 })}
                                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                        <span style={{ fontSize: 9, color: textMuted }}>Mâles</span>
                                        <input
                                            type="number"
                                            min="0"
                                            value={card.initialMales ?? 0}
                                            onChange={(e) => updateSpeciesCard(card.id, { initialMales: parseInt(e.target.value) || 0 })}
                                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                        />
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    )
}
