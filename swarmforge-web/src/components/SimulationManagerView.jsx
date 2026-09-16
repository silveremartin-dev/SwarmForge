import React from 'react'
import { Sliders, Eye, Zap, BarChart2, List } from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'

import SimulationControlPanel from './SimulationControlPanel'
import SimulationVisualViewport from './SimulationVisualViewport'
import GodModePanel from './GodModePanel'
import StatisticsDashboardPanel from './StatisticsDashboardPanel'
import EventLogPanel from './EventLogPanel'

export default function SimulationManagerView() {
    const { activeSubTab, setActiveSubTab, theme, language } = useSimulationStore()
    const isDark = theme === 'dark'

    const t = (key, fallback) => getTranslation(language, key, fallback)

    const subTabs = [
        { id: 'CONTROLS', label: t('tabControls', 'Contrôle & Scénario'), icon: Sliders },
        { id: 'VISUAL_3D', label: t('tabVisualView', 'Vue 3D'), icon: Eye },
        { id: 'GOD_MODE', label: t('tabGodMode', 'God Mode'), icon: Zap },
        { id: 'STATISTICS', label: t('tabStats', 'Statistiques'), icon: BarChart2 },
        { id: 'EVENT_LOG', label: t('tabLogs', 'Journal d\'Événements'), icon: List }
    ]

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            width: '100%',
            height: 'calc(100vh - 52px)',
            background: isDark ? '#0b0f19' : '#f8fafc',
            color: isDark ? '#f1f5f9' : '#0f172a',
            overflow: 'hidden'
        }}>
            {/* Sub-Tabs Bar (1:1 JavaFX simSubTabs) */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: 6,
                padding: '8px 16px',
                background: isDark ? '#0f172a' : '#ffffff',
                borderBottom: isDark ? '1px solid #1e293b' : '1px solid #e2e8f0',
                flexShrink: 0
            }}>
                {subTabs.map(tab => {
                    const Icon = tab.icon
                    const isActive = activeSubTab === tab.id
                    return (
                        <button
                            key={tab.id}
                            onClick={() => setActiveSubTab(tab.id)}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 7,
                                padding: '8px 16px',
                                fontSize: 13,
                                fontWeight: 700,
                                borderRadius: 6,
                                border: 'none',
                                cursor: 'pointer',
                                background: isActive
                                    ? (isDark ? '#1e293b' : '#e0f2fe')
                                    : 'transparent',
                                color: isActive
                                    ? (isDark ? '#38bdf8' : '#0284c7')
                                    : (isDark ? '#94a3b8' : '#64748b'),
                                borderBottom: isActive ? '2px solid #38bdf8' : '2px solid transparent',
                                transition: 'all 0.15s ease'
                            }}
                        >
                            <Icon size={16} />
                            <span>{tab.label}</span>
                        </button>
                    )
                })}
            </div>

            {/* Sub-Tab Content View */}
            <div style={{
                flex: 1,
                overflow: activeSubTab === 'VISUAL_3D' ? 'hidden' : 'auto',
                position: 'relative'
            }}>
                {activeSubTab === 'CONTROLS' && <SimulationControlPanel />}
                {activeSubTab === 'VISUAL_3D' && <SimulationVisualViewport />}
                {activeSubTab === 'GOD_MODE' && <GodModePanel />}
                {activeSubTab === 'STATISTICS' && <StatisticsDashboardPanel />}
                {activeSubTab === 'EVENT_LOG' && <EventLogPanel />}
            </div>
        </div>
    )
}
