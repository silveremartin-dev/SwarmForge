import React, { useState } from 'react'
import { CheckCircle } from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { showToast } from '../store/toastStore'
import ScenarioMultiplayerServerCard from './scenario/ScenarioMultiplayerServerCard'
import ScenarioEnvironmentSection from './scenario/ScenarioEnvironmentSection'
import ScenarioPhysicsLimitsSection from './scenario/ScenarioPhysicsLimitsSection'
import ScenarioSpeciesSection from './scenario/ScenarioSpeciesSection'

/**
 * Main Simulation Control Panel orchestrating multi-species scenarios,
 * remote server clusters, environmental profiles, and limits.
 */
export default function SimulationControlPanel() {
    const {
        applyScenarioSetup,
        serverRole,
        theme,
        language
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const [isApplied, setIsApplied] = useState(false)

    const handleApply = () => {
        applyScenarioSetup()
        setIsApplied(true)
        showToast(t('scenarioAppliedBadge', '✓ Scénario Appliqué !'), 'success')
        setTimeout(() => setIsApplied(false), 2500)
    }

    const isJoin = serverRole === 'JOIN'
    const isScenarioDisabled = isJoin

    const cardBg = isDark ? '#181b22' : '#ffffff'
    const borderCol = isDark ? '#2d3340' : '#e2e8f0'
    const inputBg = isDark ? '#0f131a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    const commonStyleProps = {
        isDark,
        cardBg,
        borderCol,
        inputBg,
        textMain,
        textMuted
    }

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
            {/* Header: Title & Deploy/Apply Button */}
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

            {/* 1. SwarmForge Remote Server gRPC Cluster Connection Card */}
            <ScenarioMultiplayerServerCard {...commonStyleProps} />

            {/* 2. Global Scenario Preset, Environment, Physics, & Species Sections */}
            <div style={{
                opacity: isScenarioDisabled ? 0.35 : 1.0,
                pointerEvents: isScenarioDisabled ? 'none' : 'auto',
                display: 'flex',
                flexDirection: 'column',
                gap: 16
            }}>
                <ScenarioEnvironmentSection {...commonStyleProps} />
                <ScenarioPhysicsLimitsSection {...commonStyleProps} />
                <ScenarioSpeciesSection {...commonStyleProps} />
            </div>
        </div>
    )
}
