import React from 'react'
import {
    Globe,
    Moon,
    Sun,
    RotateCcw,
    Check,
    Info
} from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { SUPPORTED_LANGUAGES, getTranslation } from '../i18n/translations'
import { showToast } from '../store/toastStore'

export default function SettingsPanel() {
    const {
        language,
        setLanguage,
        theme,
        setTheme
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const cardBg = isDark ? '#1e293b' : '#ffffff'
    const borderCol = isDark ? '#334155' : '#e2e8f0'
    const inputBg = isDark ? '#0f172a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    const handleResetDefaults = () => {
        setLanguage('fr')
        setTheme('dark')
        showToast(t('resetSettingsBtn', '✓ Paramètres rétablis par défaut !'), 'info')
    }

    return (
        <div style={{
            maxWidth: 800,
            margin: '0 auto',
            display: 'flex',
            flexDirection: 'column',
            gap: 20
        }}>
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                    <h2 style={{ margin: 0, fontSize: 18, fontWeight: 800, color: '#38bdf8' }}>
                        ⚙️ {t('settingsTitle', 'Paramètres de l\'Application & Préférences')}
                    </h2>
                    <p style={{ margin: '4px 0 0', fontSize: 12, color: textMuted }}>
                        {t('settingsSubtitle', 'Personnalisation de la langue, de l\'affichage et du moteur 3D.')}
                    </p>
                </div>
            </div>

            {/* 1. Language Row */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#38bdf8' }}>
                    <Globe size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>{t('langTitle', 'Langue de l\'Interface (Language)')}</span>
                </div>
                <p style={{ margin: 0, fontSize: 12, color: textMuted }}>
                    {t('langDesc', 'Sélectionnez la langue d\'affichage de l\'application.')}
                </p>

                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                    {SUPPORTED_LANGUAGES.map(lang => (
                        <button
                            key={lang.code}
                            onClick={() => {
                                setLanguage(lang.code)
                                showToast(`${lang.name}`, 'info')
                            }}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 8,
                                padding: '8px 16px',
                                borderRadius: 6,
                                border: language === lang.code ? '2px solid #38bdf8' : `1px solid ${borderCol}`,
                                background: language === lang.code ? (isDark ? '#0284c7' : '#e0f2fe') : inputBg,
                                color: language === lang.code ? (isDark ? '#fff' : '#0284c7') : textMain,
                                fontSize: 13,
                                fontWeight: 700,
                                cursor: 'pointer'
                            }}
                        >
                            <span>{lang.flag}</span>
                            <span>{lang.name}</span>
                            {language === lang.code && <Check size={14} />}
                        </button>
                    ))}
                </div>
            </div>

            {/* 2. Theme Row */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#f59e0b' }}>
                    <Sun size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>{t('themeTitle', 'Thème Graphique')}</span>
                </div>
                <p style={{ margin: 0, fontSize: 12, color: textMuted }}>
                    {t('themeDesc', 'Basculez entre le thème sombre pour observation nocturne et le thème clair haute lisibilité.')}
                </p>

                <div style={{ display: 'flex', gap: 12 }}>
                    <button
                        onClick={() => setTheme('dark')}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 8,
                            padding: '10px 20px',
                            borderRadius: 6,
                            border: theme === 'dark' ? '2px solid #38bdf8' : `1px solid ${borderCol}`,
                            background: theme === 'dark' ? '#0f172a' : inputBg,
                            color: theme === 'dark' ? '#38bdf8' : textMain,
                            fontSize: 13,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Moon size={16} /> {t('themeDark', 'Mode Sombre (Dark Theme)')}
                    </button>

                    <button
                        onClick={() => setTheme('light')}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 8,
                            padding: '10px 20px',
                            borderRadius: 6,
                            border: theme === 'light' ? '2px solid #0284c7' : `1px solid ${borderCol}`,
                            background: theme === 'light' ? '#f1f5f9' : inputBg,
                            color: theme === 'light' ? '#0284c7' : textMain,
                            fontSize: 13,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Sun size={16} /> {t('themeLight', 'Mode Clair (Light Theme)')}
                    </button>
                </div>
            </div>

            {/* 3. System & Engine Info Card */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 8,
                fontSize: 12,
                color: textMuted
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#38bdf8', fontWeight: 800, fontSize: 13 }}>
                    <Info size={16} />
                    <span>{t('aboutTitle', 'À propos de SwarmForge Web Visualizer')}</span>
                </div>
                <div><strong>{t('aboutVersion', 'Version :')}</strong> 2.4.0 (Unified Simulation Studio Architecture)</div>
                <div><strong>{t('aboutArchitecture', 'Architecture :')}</strong> {t('engineModeDesc', 'Client Léger WebGL (Three.js / React Three Fiber) avec moteur sonore procédural WebAudio.')}</div>
                <div><strong>{t('aboutAuthor', 'Auteur :')}</strong> Silvère Martin-Michiellot & Gemini AI Assistant (Google DeepMind)</div>
            </div>
        </div>
    )
}
