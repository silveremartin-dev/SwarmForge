import React, { useState } from 'react'
import { Sliders, Zap, BarChart2, Terminal, Volume2, Globe } from 'lucide-react'
import ControlPanel from './ControlPanel'
import GodModePanel from './GodModePanel'
import StatisticsDashboardPanel from './StatisticsDashboardPanel'
import EventLogPanel from './EventLogPanel'
import AudioMixerWidget from './AudioMixerWidget'
import SettingsPanel from './SettingsPanel'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'

export default function SimulationLeftSidebar() {
    const [activeTab, setActiveTab] = useState('controls') // 'controls' | 'godmode' | 'stats' | 'events' | 'audio' | 'settings'
    const { language, theme } = useSimulationStore()
    const isDark = theme === 'dark'

    const t = (key, fallback) => getTranslation(language, key, fallback)

    const tabs = [
        { id: 'controls', emoji: '⚙️', label: t('tabControls', 'Contrôles'), icon: Sliders },
        { id: 'godmode', emoji: '👑', label: t('tabGodMode', 'Mode Divin'), icon: Zap },
        { id: 'stats', emoji: '📊', label: t('tabStats', 'Statistiques'), icon: BarChart2 },
        { id: 'events', emoji: '📜', label: t('tabLogs', 'Événements'), icon: Terminal },
        { id: 'audio', emoji: '🔊', label: t('tabAudio', 'Audio'), icon: Volume2 },
        { id: 'settings', emoji: '🌐', label: t('tabSettings', 'Paramètres'), icon: Globe },
    ]

    const styles = {
        container: {
            position: 'absolute',
            top: 60,
            left: 20,
            width: 390,
            maxHeight: 'calc(100vh - 75px)',
            zIndex: 90,
            display: 'flex',
            flexDirection: 'column',
            gap: 8,
            pointerEvents: 'auto',
        },
        tabBar: {
            display: 'flex',
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.94)',
            backdropFilter: 'blur(16px)',
            border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
            borderRadius: 10,
            padding: 4,
            gap: 3,
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
        },
        tabBtn: (active) => ({
            flex: 1,
            padding: '7px 3px',
            fontSize: 10,
            fontWeight: 700,
            border: active
                ? '1px solid #38bdf8'
                : '1px solid transparent',
            borderRadius: 6,
            background: active
                ? (isDark ? '#0284c7' : '#38bdf8')
                : 'transparent',
            color: active
                ? '#ffffff'
                : (isDark ? '#94a3b8' : '#64748b'),
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 2,
            transition: 'all 0.15s ease',
            whiteSpace: 'nowrap'
        }),
        contentArea: {
            maxHeight: 'calc(100vh - 135px)',
            overflowY: 'auto',
            borderRadius: 12,
        },
        settingsCard: {
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.94)',
            backdropFilter: 'blur(16px)',
            border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
            borderRadius: 12,
            padding: 16,
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
        }
    }

    return (
        <div style={styles.container}>
            {/* Top Navigation Tabs for Left Simulation Sidebar */}
            <div style={styles.tabBar}>
                {tabs.map(tab => {
                    const Icon = tab.icon
                    const active = activeTab === tab.id
                    return (
                        <button
                            key={tab.id}
                            style={styles.tabBtn(active)}
                            onClick={() => setActiveTab(tab.id)}
                            title={tab.label}
                        >
                            <Icon size={12} />
                            <span>{tab.label}</span>
                        </button>
                    )
                })}
            </div>

            {/* Active Panel View */}
            <div style={styles.contentArea}>
                {activeTab === 'controls' && <ControlPanel inline={true} />}
                {activeTab === 'godmode' && <GodModePanel inline={true} />}
                {activeTab === 'stats' && <StatisticsDashboardPanel />}
                {activeTab === 'events' && <EventLogPanel />}
                {activeTab === 'audio' && <AudioMixerWidget />}
                {activeTab === 'settings' && (
                    <div style={styles.settingsCard}>
                        <SettingsPanel />
                    </div>
                )}
            </div>
        </div>
    )
}
