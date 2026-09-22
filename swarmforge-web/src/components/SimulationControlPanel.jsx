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
        isMultiplayerOnly,
        setIsMultiplayerOnly,
        requiredPlayerCount,
        setRequiredPlayerCount,
        gridTilesX,
        setGridTilesX,
        gridTilesY,
        setGridTilesY,
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
        serverScenarios,
        lobbyStatus,
        lobbyPlayers,
        selectedServerScenarioId,
        isPlayerReady,
        togglePlayerReady,
        startServerMatch,
        selectServerScenario,
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
        showToast(t('scenarioAppliedBadge', '✓ Scénario Appliqué !'), 'success')
        setTimeout(() => setIsApplied(false), 2500)
    }

    const isJoin = serverRole === 'JOIN'
    const isScenarioDisabled = isJoin

    // Calculate equivalent duration text
    const getCalculatedDurationInfo = () => {
        if (durationUnit === '∞ Unlimited' || durationUnit === '∞ Illimité') {
            return `🔄 ${t('equivalentDurationLabel', 'Durée Équivalente :')} ∞`
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

        return `🔄 ${t('equivalentDurationLabel', 'Durée Équivalente :')} ${days}d ${hours}h ${mins}m ${secs}s (${steps.toLocaleString()} steps @ Δt = ${dt.toFixed(3)}s)`
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
                    {t('simControlsTitle', 'Configuration du Scénario & Écosystème Multi-Espèces')}
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
                    {isApplied
                        ? t('scenarioAppliedBadge', '✓ Scénario Appliqué !')
                        : (serverRole === 'HOST'
                            ? t('btnDeployScenarioServer', '🚀 Déployer le Scénario sur SwarmForge Server')
                            : t('btnJoinServerSimulation', '🌐 Rejoindre la Simulation Serveur'))}
                </button>
            </div>

            {/* 1. SwarmForge Remote Server gRPC Cluster Connection Panel */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 8,
                padding: '14px 16px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 12,
                    padding: '14px 16px',
                    borderRadius: 8,
                    background: isDark ? '#111827' : '#f0f9ff',
                    border: '1px solid rgba(2, 132, 199, 0.35)'
                }}>
                        {/* Row 1: Host, Port, Connect/Disconnect, Discover */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('hostLabel', 'Hôte :')}</span>
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
                                <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('portLabel', 'Port :')}</span>
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
                                    {t('btnDisconnect', '✕ Déconnecter')}
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
                                    {t('btnConnect', '🌐 Connecter')}
                                </button>
                            )}

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
                                {t('btnDiscover', '🔍 Détecter')}
                            </button>
                        </div>

                        {/* Row 2: Status */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 11 }}>
                            <span style={{ fontWeight: 600, color: textMuted }}>{t('grpcStatusLabel', 'Statut gRPC :')}</span>
                            {connected ? (
                                <span style={{ color: '#10b981', fontWeight: 700 }}>
                                    {t('grpcStatusConnected', '● Connecté')} ({serverHost}:{serverPort})
                                </span>
                            ) : (
                                <span style={{ color: '#ef4444', fontWeight: 700 }}>
                                    {t('grpcStatusOffline', 'Hors ligne')}
                                </span>
                            )}
                        </div>

                        {/* Row 3: Server Role */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <span style={{ fontSize: 12, fontWeight: 600, color: textMuted, minWidth: 100 }}>{t('serverRoleLabel', 'Rôle Serveur :')}</span>
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
                                <option value="JOIN">{t('roleJoinLabel', '● Rejoindre (Matchmaking / Mégaterrarium - Autorité Serveur)')}</option>
                                <option value="HOST">{t('roleHostLabel', '● Héberger / Déployer Scénario (Mode Chercheur - Autorité Client)')}</option>
                            </select>
                        </div>

                        {/* Row 4: Participant Tag & Species (Shown when JOIN is selected) */}
                        {isJoin && (
                            <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('participantTagLabel', 'Tag / Alias du Participant :')}</span>
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
                                    <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('speciesLabel', 'Espèce Affectée :')}</span>
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
                                    {t('joinModeExplainerTitle', '▶ Mode Rejoindre — Matchmaking & Mégaterrarium (Autorité Serveur)')}
                                </div>
                                <div>{t('joinModeExplainer1', '• Monde & Climat : Régis par l\'hôte serveur distant (paramètres locaux désactivés).')}</div>
                                <div>{t('joinModeExplainer2', '• Votre Colonie : Le serveur assigne une colonie équilibrée et l\'emplacement du nid.')}</div>
                                <div>{t('joinModeExplainer3', '• Espèce & Tag : Sélectionnez l\'espèce que vous apportez depuis votre bibliothèque (champ ci-dessus).')}</div>
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
                                    {t('hostModeExplainerTitle', '▶ Mode Hébergement — Contrôle Total du Chercheur (Autorité Client)')}
                                </div>
                                <div>{t('hostModeExplainer1', '• Scénario Maître : Tous vos paramètres ci-dessous (Monde, Climat, Espèces, Nids, Démographie) sont déployés sur le serveur.')}</div>
                                <div>{t('hostModeExplainer2', '• Rôle : Vous définissez l\'écosystème complet. D\'autres participants peuvent rejoindre votre simulation et piloter des colonies.')}</div>
                            </div>
                        {/* Row 6: Matchmaking Lobby & Scenario Status Banner (Visible when connected) */}
                        {connected && (
                            <div style={{
                                marginTop: 8,
                                padding: '12px 14px',
                                borderRadius: 8,
                                background: isDark ? '#0f172a' : '#f1f5f9',
                                border: `1px solid ${isDark ? '#334155' : '#cbd5e1'}`,
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 10
                            }}>
                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                        <Server size={16} color="#38bdf8" />
                                        <span style={{ fontSize: 12, fontWeight: 800, color: textMain }}>
                                            {t('serverScenariosTitle', 'Catalogue des Scénarios Serveur & Matchmaking')}
                                        </span>
                                    </div>
                                    <span style={{
                                        fontSize: 10,
                                        fontWeight: 800,
                                        padding: '3px 8px',
                                        borderRadius: 4,
                                        background: lobbyStatus === 'ACTIVE' ? '#059669' : (lobbyStatus === 'LOBBY_WAITING' ? '#d97706' : '#64748b'),
                                        color: '#ffffff',
                                        letterSpacing: '0.5px'
                                    }}>
                                        {lobbyStatus === 'ACTIVE' 
                                            ? t('lobbyStatusActive', '🟢 PARTIE EN COURS (ACTIVE)') 
                                            : (lobbyStatus === 'LOBBY_WAITING' 
                                                ? t('lobbyStatusWaiting', '⏳ EN ATTENTE DE DÉMARRAGE (LOBBY)') 
                                                : t('lobbyStatusInactive', '⚪ INACTIF'))}
                                    </span>
                                </div>

                                {/* Colony Slots & Connected Players */}
                                <div style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: 6,
                                    background: isDark ? '#1e293b' : '#ffffff',
                                    padding: '8px 10px',
                                    borderRadius: 6,
                                    border: `1px solid ${borderCol}`
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, fontWeight: 700, color: textMuted }}>
                                        <span>{t('lobbySlotsTitle', 'Slots de Colonies & Participants :')}</span>
                                        <span style={{ color: '#38bdf8' }}>
                                            {lobbyPlayers.length} {t('lobbySlotsCount', 'Joueur(s) Connecté(s)')}
                                        </span>
                                    </div>

                                    {lobbyPlayers.length === 0 ? (
                                        <div style={{ fontSize: 11, fontStyle: 'italic', color: textMuted }}>
                                            {t('slotWaitingPlayer', 'En attente d\'un joueur...')}
                                        </div>
                                    ) : (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                            {lobbyPlayers.map((p, idx) => (
                                                <div key={idx} style={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    justifyContent: 'space-between',
                                                    fontSize: 11,
                                                    padding: '4px 6px',
                                                    borderRadius: 4,
                                                    background: isDark ? 'rgba(255,255,255,0.03)' : 'rgba(0,0,0,0.02)'
                                                }}>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                                        <span style={{ fontWeight: 700, color: textMain }}>#{idx + 1} {p.tag || 'Participant'}</span>
                                                        <span style={{ color: textMuted }}>({p.species || 'Espèce'})</span>
                                                        <span style={{
                                                            fontSize: 9,
                                                            padding: '1px 4px',
                                                            borderRadius: 3,
                                                            background: p.role === 'HOST' ? '#0284c7' : '#64748b',
                                                            color: '#fff',
                                                            fontWeight: 600
                                                        }}>
                                                            {p.role || 'JOIN'}
                                                        </span>
                                                    </div>
                                                    <span style={{
                                                        fontSize: 10,
                                                        fontWeight: 700,
                                                        color: p.isReady ? '#10b981' : '#f59e0b'
                                                    }}>
                                                        {p.isReady ? '✓ Prêt' : '○ En attente'}
                                                    </span>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                {/* Lobby Action Bar (Ready toggle for Join, Start Match for Host) */}
                                <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                                    {isJoin ? (
                                        <button
                                            onClick={togglePlayerReady}
                                            style={{
                                                flex: 1,
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center',
                                                gap: 6,
                                                background: isPlayerReady ? '#059669' : '#d97706',
                                                color: '#ffffff',
                                                border: 'none',
                                                borderRadius: 6,
                                                padding: '8px 14px',
                                                fontSize: 12,
                                                fontWeight: 800,
                                                cursor: 'pointer'
                                            }}
                                        >
                                            {isPlayerReady ? t('btnToggleReady', '✓ Je suis Prêt') : t('btnToggleNotReady', '✕ Pas Prêt')}
                                        </button>
                                    ) : (
                                        <button
                                            onClick={startServerMatch}
                                            disabled={lobbyStatus === 'ACTIVE'}
                                            style={{
                                                flex: 1,
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center',
                                                gap: 6,
                                                background: lobbyStatus === 'ACTIVE' ? '#475569' : '#059669',
                                                color: '#ffffff',
                                                border: 'none',
                                                borderRadius: 6,
                                                padding: '8px 14px',
                                                fontSize: 12,
                                                fontWeight: 800,
                                                cursor: lobbyStatus === 'ACTIVE' ? 'not-allowed' : 'pointer',
                                                opacity: lobbyStatus === 'ACTIVE' ? 0.6 : 1.0
                                            }}
                                        >
                                            <Zap size={14} />
                                            {t('btnStartServerMatch', '🚀 Lancer la Partie (Hôte)')}
                                        </button>
                                    )}
                                </div>

                                {/* Server Known Scenarios List */}
                                {serverScenarios && serverScenarios.length > 0 && (
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 4 }}>
                                        <div style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>
                                            {t('serverScenariosTitle', 'Scénarios Disponibles sur le Serveur :')}
                                        </div>
                                        <div style={{
                                            display: 'flex',
                                            flexDirection: 'column',
                                            gap: 4,
                                            maxHeight: 180,
                                            overflowY: 'auto',
                                            paddingRight: 4
                                        }}>
                                            {serverScenarios.map(sc => {
                                                const isSelected = sc.id === selectedServerScenarioId
                                                return (
                                                    <div
                                                        key={sc.id}
                                                        style={{
                                                            display: 'flex',
                                                            alignItems: 'center',
                                                            justifyContent: 'space-between',
                                                            padding: '6px 8px',
                                                            borderRadius: 5,
                                                            background: isSelected 
                                                                ? (isDark ? 'rgba(56, 189, 248, 0.15)' : '#e0f2fe')
                                                                : (isDark ? '#1e293b' : '#ffffff'),
                                                            border: `1px solid ${isSelected ? '#38bdf8' : borderCol}`,
                                                            fontSize: 11
                                                        }}
                                                    >
                                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 2, flex: 1, minWidth: 0, paddingRight: 8 }}>
                                                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                                                <span style={{ fontWeight: 800, color: textMain, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                                    {sc.title}
                                                                </span>
                                                                <span style={{
                                                                    fontSize: 9,
                                                                    padding: '1px 5px',
                                                                    borderRadius: 3,
                                                                    background: isDark ? '#334155' : '#e2e8f0',
                                                                    color: textMuted,
                                                                    fontWeight: 600
                                                                }}>
                                                                    {sc.requiredPlayerCount || 1}P {sc.isMultiplayerOnly ? 'Multijoueur' : 'Solo/Multi'}
                                                                </span>
                                                            </div>
                                                            <span style={{ fontSize: 10, color: textMuted, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                                {sc.academicCategory} • Biome : {sc.biomeName}
                                                            </span>
                                                        </div>

                                                        {!isJoin && lobbyStatus !== 'ACTIVE' && (
                                                            <button
                                                                onClick={() => selectServerScenario(sc.id)}
                                                                disabled={isSelected}
                                                                style={{
                                                                    background: isSelected ? '#0284c7' : (isDark ? '#334155' : '#cbd5e1'),
                                                                    color: isSelected ? '#ffffff' : textMain,
                                                                    border: 'none',
                                                                    borderRadius: 4,
                                                                    padding: '4px 8px',
                                                                    fontSize: 10,
                                                                    fontWeight: 700,
                                                                    cursor: isSelected ? 'default' : 'pointer'
                                                                }}
                                                            >
                                                                {isSelected ? '✓ Actif' : t('btnSelectScenario', 'Sélectionner')}
                                                            </button>
                                                        )}
                                                    </div>
                                                )
                                            })}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
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
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('globalScenarioPresetLabel', 'Méta-Preset de Scénario Global :')}</span>
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
                        <Save size={13} /> {t('btnSave', 'Enregistrer')}
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
                        <Trash2 size={13} /> {t('btnDelete', 'Supprimer')}
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
                        <Download size={13} /> {t('btnExport', 'Exporter...')}
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
                        <Upload size={13} /> {t('btnImport', 'Importer...')}
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
                    <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>{t('scientificDescLabel', 'Description Scientifique du Scénario :')}</span>
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
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('worldPresetSectionLabel', '1. Biotope & Monde (World Preset) :')}</span>
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
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('weatherPresetSectionLabel', '2. Profil Météo & Climat :')}</span>

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
                            {t('weatherSimulated', 'Simulé')}
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
                            {t('weatherReal', 'Réel')}
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
                                {t('btnAlignWeather', '⛅ Aligner')}
                            </button>
                        </>
                    ) : (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1 }}>
                            <input
                                type="text"
                                value={realWeatherCity}
                                onChange={(e) => setRealWeatherCity(e.target.value)}
                                placeholder={t('realWeatherPlaceholder', 'Ville ou Lat, Lon...')}
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
                                {t('btnGetWeather', 'Obtenir')}
                            </button>
                            <span style={{ fontSize: 10, color: textMuted }}>{realWeatherStatus}</span>
                        </div>
                    )}
                </div>

                {/* 3. Start Date & Time */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('startDateTimeSectionLabel', '3. Date & Heure de Démarrage :')}</span>
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
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('masterSeedSectionLabel', '4. Graine Aléatoire Maîtresse (RNG Master Seed) :')}</span>
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
                        {t('btnNew', '🎲 Nouvelle')}
                    </button>
                </div>

                {/* 5. Physics Step (Integration Δt) */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('physicsStepSectionLabel', '5. Pas Physique (Intégration Δt) :')}</span>
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
                        {t('physicsStepNote', 'Note : Pour une graine et un pas Δt donnés, l\'exécution de la simulation est strictement déterministe et reproductible.')}
                    </div>
                </div>

                {/* 6. Maximum Simulation Duration */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('maxDurationSectionLabel', '6. Durée Maximale de Simulation :')}</span>
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
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }} title={t('minPopStopSectionTt', 'Arrête automatiquement la simulation si la population totale tombe sous ce seuil (0 = désactivé).')}>
                        {t('minPopStopSectionLabel', '7. Seuil d\'Arrêt Population Minimale :')}
                    </span>
                    <input
                        type="number"
                        min="0"
                        max="10000"
                        value={minPopStop}
                        onChange={(e) => setMinPopStop(parseInt(e.target.value))}
                        title={t('minPopStopInputTt', 'Nombre d\'individus minimum sous lequel la simulation se met automatiquement en pause.')}
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
                    <span style={{ fontSize: 11, color: textMuted }}>ind</span>
                </div>

                {/* 8. Megaterrarium & Multiplayer Topology Card (1:1 with SimulationControlPanel.java) */}
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 12,
                    background: isDark ? 'rgba(56, 189, 248, 0.04)' : '#f0f9ff',
                    border: `1px solid ${isDark ? '#0369a1' : '#bae6fd'}`,
                    borderRadius: 8,
                    padding: '14px 16px'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <Globe size={16} color="#0284c7" />
                        <span style={{ fontSize: 13, fontWeight: 800, color: textMain }} title={t('megaterrariumCardTt', 'Configuration de la topologie multi-nœuds (sharding spatial) et des paramètres multijoueur.')}>
                            {t('megaterrariumCardTitle', '🌐 Topologie Mégaterrarium & Multijoueur')}
                        </span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, fontWeight: 600, cursor: 'pointer' }} title={t('multiplayerOnlyTt', 'Si coché, ce scénario nécessite plusieurs participants et un serveur/cluster pour s\'exécuter.')}>
                            <input
                                type="checkbox"
                                checked={isMultiplayerOnly}
                                onChange={(e) => setIsMultiplayerOnly(e.target.checked)}
                            />
                            <span>{t('multiplayerOnlyChk', 'Scénario Exclusivement Multijoueur')}</span>
                        </label>

                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }} title={t('requiredPlayersTt', 'Nombre minimum de participants (slots de colonies) pour lancer la partie (1 à 16 joueurs).')}>
                                {t('requiredPlayersLabel', 'Nombre de Joueurs Requis :')}
                            </span>
                            <input
                                type="number"
                                min="1"
                                max="16"
                                value={requiredPlayerCount}
                                onChange={(e) => setRequiredPlayerCount(parseInt(e.target.value))}
                                title={t('requiredPlayersTt', 'Nombre minimum de participants (slots de colonies) pour lancer la partie (1 à 16 joueurs).')}
                                style={{
                                    width: 55,
                                    background: inputBg,
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 4,
                                    padding: '4px 6px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    textAlign: 'center'
                                }}
                            />
                            <span style={{ fontSize: 11, color: textMuted }}>joueur(s)</span>
                        </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }} title={t('megaterrariumTilesXTt', 'Nombre de sous-volumes shardés sur l\'axe X (1 à 8).')}>
                                {t('megaterrariumTilesXLabel', 'Tuiles X (Colonnes) :')}
                            </span>
                            <input
                                type="number"
                                min="1"
                                max="8"
                                value={gridTilesX}
                                onChange={(e) => setGridTilesX(parseInt(e.target.value))}
                                title={t('megaterrariumTilesXTt', 'Nombre de sous-volumes shardés sur l\'axe X (1 à 8).')}
                                style={{
                                    width: 50,
                                    background: inputBg,
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 4,
                                    padding: '4px 6px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    textAlign: 'center'
                                }}
                            />
                        </div>

                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }} title={t('megaterrariumTilesYTt', 'Nombre de sous-volumes shardés sur l\'axe Y (1 à 8).')}>
                                {t('megaterrariumTilesYLabel', 'Tuiles Y (Lignes) :')}
                            </span>
                            <input
                                type="number"
                                min="1"
                                max="8"
                                value={gridTilesY}
                                onChange={(e) => setGridTilesY(parseInt(e.target.value))}
                                title={t('megaterrariumTilesYTt', 'Nombre de sous-volumes shardés sur l\'axe Y (1 à 8).')}
                                style={{
                                    width: 50,
                                    background: inputBg,
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 4,
                                    padding: '4px 6px',
                                    fontSize: 11,
                                    fontWeight: 700,
                                    textAlign: 'center'
                                }}
                            />
                        </div>

                        {/* Reactive Megaterrarium Topology Badge */}
                        <div style={{
                            fontSize: 11,
                            fontWeight: 800,
                            padding: '4px 10px',
                            borderRadius: 6,
                            background: (gridTilesX > 1 || gridTilesY > 1) 
                                ? (isDark ? 'rgba(56, 189, 248, 0.2)' : '#e0f2fe')
                                : (isDark ? '#334155' : '#e2e8f0'),
                            color: (gridTilesX > 1 || gridTilesY > 1) ? '#0284c7' : textMuted
                        }}>
                            {(gridTilesX === 1 && gridTilesY === 1)
                                ? `🗺️ ${t('megaterrariumMonolithic', 'Monde Monolithique (1 Tile)')} | ${requiredPlayerCount} ${t('playersCountUnit', 'Joueur(s)')}`
                                : `🌐 ${t('megaterrariumSharded', 'Mégaterrarium Shardé')} : Grille ${gridTilesX} × ${gridTilesY} = ${gridTilesX * gridTilesY} ${t('subvolumesUnit', 'Sous-Volumes')} | ${requiredPlayerCount} ${t('playersCountUnit', 'Joueur(s)')}`
                            }
                        </div>
                    </div>

                    <div style={{ fontSize: 10, color: textMuted, fontStyle: 'italic' }}>
                        {(gridTilesX === 1 && gridTilesY === 1)
                            ? t('megaterrariumMonolithicDesc', 'Simulation standard non shardée. Volume unique calculé sur un seul worker ou thread.')
                            : t('megaterrariumShardedDesc', 'Topologie distribuée multi-nœuds : halo d\'échange de phéromones (3 cellules) et migration continue des entités entre sous-volumes shardés.')
                        }
                    </div>
                </div>

                {/* 9. Dynamic Multi-Species & Ecosystem Configuration Cards */}
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
                            {t('ecosystemColoniesTitle', 'Colonies & Castes IA de l\'Écosystème')} ({speciesCards.length} colonies)
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
                            <Plus size={13} /> {t('btnAddColony', '＋ Ajouter une Colonie')}
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
                                            onClick={() => {
                                                if (window.confirm(t('confirmDeleteColony', `Confirmer la suppression de la colonie "${card.name}" ?`))) {
                                                    removeSpeciesCard(card.id)
                                                }
                                            }}
                                            title={t('btnDeleteColonyTt', 'Supprimer cette colonie du scénario (confirmation requise)')}
                                            style={{ background: 'transparent', border: 'none', color: '#ef4444', cursor: 'pointer', padding: 2 }}
                                        >
                                            <Trash2 size={13} />
                                        </button>
                                    )}
                                </div>

                                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                    <span style={{ fontSize: 10, color: textMuted }}>{t('speciesCardLabel', 'Espèce :')}</span>
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
                                    <span style={{ fontSize: 10, color: textMuted }}>{t('nestTypeCardLabel', 'Type de Nid :')}</span>
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
                                        <span style={{ fontSize: 9, color: textMuted }}>{t('queensCardLabel', 'Reines')}</span>
                                        <input
                                            type="number"
                                            min="0"
                                            value={card.initialQueens ?? 1}
                                            onChange={(e) => updateSpeciesCard(card.id, { initialQueens: parseInt(e.target.value) || 0 })}
                                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                        <span style={{ fontSize: 9, color: textMuted }}>{t('workersCardLabel', 'Ouvrières')}</span>
                                        <input
                                            type="number"
                                            min="0"
                                            value={card.initialWorkers ?? 40}
                                            onChange={(e) => updateSpeciesCard(card.id, { initialWorkers: parseInt(e.target.value) || 0 })}
                                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                        <span style={{ fontSize: 9, color: textMuted }}>{t('soldiersCardLabel', 'Soldats')}</span>
                                        <input
                                            type="number"
                                            min="0"
                                            value={card.initialSoldiers ?? 10}
                                            onChange={(e) => updateSpeciesCard(card.id, { initialSoldiers: parseInt(e.target.value) || 0 })}
                                            style={{ background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '3px 4px', fontSize: 10, textAlign: 'center' }}
                                        />
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                        <span style={{ fontSize: 9, color: textMuted }}>{t('malesCardLabel', 'Mâles')}</span>
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
