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
        setTheme,
        language
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const navTabs = [
        { id: 'SIMULATION', label: t('tabSimulationManager', 'Gestionnaire de Simulation'), icon: Sliders },
        { id: 'VISUAL_3D', label: t('tabVisualView', 'Vue 3D'), icon: Eye },
        { id: 'GOD_MODE', label: t('tabGodMode', 'Mode Divin'), icon: Zap },
        { id: 'STATISTICS', label: t('tabStats', 'Statistiques'), icon: BarChart2 },
        { id: 'EVENT_LOG', label: t('tabLogs', 'Journal d\'événements'), icon: List },
        { id: 'SETTINGS', label: t('tabSettings', 'Paramètres'), icon: Settings }
    ]

    const popCount = ants?.length || 0

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
                        const isActive = activeTab === tab.id
                        return (
                            <button
                                key={tab.id}
                                onClick={() => setActiveTab(tab.id)}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: 6,
                                    padding: '6px 12px',
                                    fontSize: 12,
                                    fontWeight: 700,
                                    borderRadius: 6,
                                    border: 'none',
                                    cursor: 'pointer',
                                    background: isActive
                                        ? (isDark ? '#0284c7' : '#0284c7')
                                        : 'transparent',
                                    color: isActive
                                        ? '#ffffff'
                                        : (isDark ? '#94a3b8' : '#64748b'),
                                    transition: 'all 0.15s ease'
                                }}
                            >
                                <Icon size={14} />
                                <span>{tab.label}</span>
                            </button>
                        )
                    })}
                </nav>
            </div>

            {/* Right: Telemetry Banner & Theme Switcher */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                {/* Telemetry Status Banner (1:1 JavaFX Header) */}
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 10,
                    background: isDark ? 'rgba(30, 41, 59, 0.7)' : 'rgba(241, 245, 249, 0.9)',
                    border: isDark ? '1px solid rgba(56, 189, 248, 0.2)' : '1px solid rgba(56, 189, 248, 0.4)',
                    padding: '4px 12px',
                    borderRadius: 6,
                    fontSize: 11
                }}>
                    <span style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                        color: connected ? '#10b981' : '#38bdf8',
                        fontWeight: 700
                    }}>
                        <span style={{
                            width: 7,
                            height: 7,
                            borderRadius: '50%',
                            background: connected ? '#10b981' : '#38bdf8'
                        }} />
                        {connected ? t('statusConnected', 'Connecté') : t('statusStandalone', 'Mode Autonome')}
                    </span>

                    <span style={{ color: '#64748b' }}>|</span>

                    <span style={{ color: isDark ? '#38bdf8' : '#0284c7', fontWeight: 700, fontFamily: 'monospace' }}>
                        ⏱️ {simTimeFormatted}
                    </span>

                    <span style={{ color: isDark ? '#94a3b8' : '#64748b', fontSize: 10 }}>
                        ({speed}x)
                    </span>

                    <span style={{ color: '#64748b' }}>|</span>

                    <span style={{ color: '#f59e0b', fontWeight: 700 }} title="Ticks Par Seconde réels">
                        ⚡ {measuredTps || 20} TPS
                    </span>

                    <span style={{ color: '#64748b' }}>|</span>

                    <span style={{ color: '#a855f7', fontWeight: 700 }}>
                        🐜 Pop: {popCount}
                    </span>
                </div>

                {/* Theme Switcher */}
                <button
                    onClick={() => setTheme(isDark ? 'light' : 'dark')}
                    title={isDark ? 'Passer au mode clair' : 'Passer au mode sombre'}
                    style={{
                        background: 'transparent',
                        border: isDark ? '1px solid #334155' : '1px solid #cbd5e1',
                        borderRadius: 6,
                        padding: '6px 8px',
                        cursor: 'pointer',
                        color: isDark ? '#cbd5e1' : '#475569',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                    }}
                >
                    {isDark ? <Sun size={15} color="#f59e0b" /> : <Moon size={15} color="#6366f1" />}
                </button>
            </div>
        </header>
    )
}
