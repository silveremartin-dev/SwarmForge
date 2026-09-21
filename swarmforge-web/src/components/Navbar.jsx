import React from 'react'
import {
    Sliders,
    Eye,
    Zap,
    BarChart2,
    List,
    Settings,
    Sun,
    Moon
} from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'

export default function Navbar() {
    const {
        activeTab,
        setActiveTab,
        connected,
        serverStatusText,
        simTimeFormatted,
        speed,
        measuredTps,
        ants,
        theme,
        language,
        isScenarioApplied,
        running
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const isSimReady = Boolean(isScenarioApplied || connected)

    const navTabs = [
        { id: 'SIMULATION', label: t('tabSimulationManager', 'Gestionnaire de Simulation'), icon: Sliders, requiresReady: false },
        { id: 'VISUAL_3D', label: t('tabVisualView', 'Vue 3D'), icon: Eye, requiresReady: true },
        { id: 'GOD_MODE', label: t('tabGodMode', 'Mode Divin'), icon: Zap, requiresReady: true },
        { id: 'STATISTICS', label: t('tabStats', 'Statistiques'), icon: BarChart2, requiresReady: true },
        { id: 'EVENT_LOG', label: t('tabLogs', 'Journal d\'événements'), icon: List, requiresReady: true },
        { id: 'SETTINGS', label: t('tabSettings', 'Paramètres'), icon: Settings, requiresReady: false }
    ]

    const popCount = isSimReady ? (ants?.length || 0) : '--'

    return (
        <header style={{
            height: 52,
            background: isDark ? '#0f172a' : '#ffffff',
            borderBottom: isDark ? '1px solid #1e293b' : '1px solid #e2e8f0',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 16px',
            zIndex: 1000,
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            flexShrink: 0
        }}>
            {/* Left: Brand + All Main Tabs (1:1 JavaFX top tabs) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 7, marginRight: 4 }}>
                    <span style={{ fontSize: 20 }}>🐜</span>
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <span style={{
                            fontSize: 15,
                            fontWeight: 800,
                            letterSpacing: '0.3px',
                            background: 'linear-gradient(135deg, #38bdf8 0%, #818cf8 100%)',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent'
                        }}>
                            SwarmForge
                        </span>
                        <span style={{ fontSize: 9, color: isDark ? '#64748b' : '#94a3b8', fontWeight: 600, marginTop: -2 }}>
                            Visualiseur de Simulation
                        </span>
                    </div>
                </div>

                {/* Direct Main Tabs Bar */}
                <nav style={{
                    display: 'flex',
                    background: isDark ? '#1e293b' : '#f1f5f9',
                    padding: 3,
                    borderRadius: 8,
                    gap: 3
                }}>
                    {navTabs.map(tab => {
                        const Icon = tab.icon
                        const isActive = activeTab === tab.id || (tab.id === 'SIMULATION' && !['VISUAL_3D', 'GOD_MODE', 'STATISTICS', 'EVENT_LOG', 'SETTINGS'].includes(activeTab))
                        const isLocked = tab.requiresReady && !isSimReady

                        return (
                            <button
                                key={tab.id}
                                disabled={isLocked}
                                onClick={() => {
                                    if (!isLocked) setActiveTab(tab.id)
                                }}
                                title={isLocked ? 'Initialisez ou appliquez le scénario pour accéder à cette vue' : tab.label}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: 6,
                                    padding: '6px 12px',
                                    fontSize: 12,
                                    fontWeight: 700,
                                    borderRadius: 6,
                                    border: 'none',
                                    cursor: isLocked ? 'not-allowed' : 'pointer',
                                    opacity: isLocked ? 0.45 : 1.0,
                                    background: isActive
                                        ? '#0284c7'
                                        : 'transparent',
                                    color: isActive
                                        ? '#ffffff'
                                        : (isDark ? '#94a3b8' : '#64748b'),
                                    transition: 'all 0.15s ease'
                                }}
                            >
                                <Icon size={14} />
                                <span>{tab.label}</span>
                                {isLocked && <span style={{ fontSize: 10 }}>🔒</span>}
                            </button>
                        )
                    })}
                </nav>
            </div>

            {/* Right: Telemetry Banner (Strictly Synchronized with Simulation State) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                {/* Telemetry Status Banner (1:1 JavaFX Header) */}
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 10,
                    background: isDark ? 'rgba(30, 41, 59, 0.7)' : 'rgba(241, 245, 249, 0.9)',
                    border: isDark ? '1px solid rgba(56, 189, 248, 0.2)' : '1px solid rgba(56, 189, 248, 0.4)',
                    padding: '5px 14px',
                    borderRadius: 6,
                    fontSize: 11
                }}>
                    <span style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                        color: connected ? '#10b981' : (isSimReady ? '#38bdf8' : '#94a3b8'),
                        fontWeight: 700
                    }}>
                        <span style={{
                            width: 7,
                            height: 7,
                            borderRadius: '50%',
                            background: connected ? '#10b981' : (isSimReady ? '#38bdf8' : '#94a3b8')
                        }} />
                        {connected ? t('statusConnected', 'Connecté Serveur') : (isSimReady ? t('statusStandalone', 'Prêt / Local') : 'En attente d\'initialisation')}
                    </span>

                    <span style={{ color: '#64748b' }}>|</span>

                    <span style={{ color: isSimReady ? (isDark ? '#38bdf8' : '#0284c7') : '#64748b', fontWeight: 700, fontFamily: 'monospace' }}>
                        ⏱️ {isSimReady ? simTimeFormatted : '--:--:--'}
                    </span>

                    <span style={{ color: isDark ? '#94a3b8' : '#64748b', fontSize: 10 }}>
                        ({isSimReady ? `${speed}x` : '1x'})
                    </span>

                    <span style={{ color: '#64748b' }}>|</span>

                    <span style={{ color: isSimReady ? '#f59e0b' : '#64748b', fontWeight: 700 }} title="Nombre de pas de calcul par seconde réels">
                        ⚡ {isSimReady ? `${measuredTps || (running ? 20 : 0)} pas/s` : '-- pas/s'}
                    </span>

                    <span style={{ color: '#64748b' }}>|</span>

                    <span style={{ color: isSimReady ? '#a855f7' : '#64748b', fontWeight: 700 }}>
                        🐜 Pop: {popCount}
                    </span>
                </div>
            </div>
        </header>
    )
}
