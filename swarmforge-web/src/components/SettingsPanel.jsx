import React from 'react'
import {
    Globe,
    Moon,
    Sun,
    Volume2,
    VolumeX,
    Server,
    Sliders,
    RotateCcw,
    Check,
    Cpu
} from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { SUPPORTED_LANGUAGES, getTranslation } from '../i18n/translations'
import { showToast } from '../store/toastStore'

export default function SettingsPanel() {
    const {
        language,
        setLanguage,
        theme,
        setTheme,
        masterVolume,
        setMasterVolume,
        ambientVolume,
        setAmbientVolume,
        sfxVolume,
        setSfxVolume,
        stepSeconds,
        setStepSeconds,
        serverHost,
        setServerHost,
        serverPort,
        setServerPort
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
        setMasterVolume(0.8)
        setAmbientVolume(0.6)
        setSfxVolume(0.7)
        setStepSeconds(0.05)
        setServerHost('localhost')
        setServerPort(50051)
        showToast('✓ Paramètres rétablis par défaut !', 'info')
    }

    return (
        <div style={{
            maxWidth: 900,
            margin: '0 auto',
            display: 'flex',
            flexDirection: 'column',
            gap: 20
        }}>
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                    <h2 style={{ margin: 0, fontSize: 18, fontWeight: 800, color: '#38bdf8' }}>
                        ⚙️ {t('settingsTitle', 'Paramètres & Préférences du Visualiseur')}
                    </h2>
                    <p style={{ margin: '4px 0 0', fontSize: 12, color: textMuted }}>
                        Configuration générale de l'interface, de la langue, du thème, de l'audio et des connexions serveur.
                    </p>
                </div>

                <button
                    onClick={handleResetDefaults}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 6,
                        background: 'transparent',
                        color: textMuted,
                        border: `1px solid ${borderCol}`,
                        padding: '6px 12px',
                        borderRadius: 6,
                        fontSize: 12,
                        cursor: 'pointer'
                    }}
                >
                    <RotateCcw size={13} /> {t('resetSettings', 'Rétablir par défaut')}
                </button>
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

                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                    {SUPPORTED_LANGUAGES.map(lang => (
                        <button
                            key={lang.code}
                            onClick={() => {
                                setLanguage(lang.code)
                                showToast(`Langue changée en ${lang.name}`, 'info')
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
                    <span style={{ fontSize: 14, fontWeight: 800 }}>{t('themeTitle', 'Thème Graphique (Dark / Light Theme)')}</span>
                </div>

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
                        <Moon size={16} /> Mode Sombre (Dark Theme)
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
                        <Sun size={16} /> Mode Clair (Light Theme)
                    </button>
                </div>
            </div>

            {/* 3. Audio & Sound FX */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 14
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#10b981' }}>
                    <Volume2 size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>{t('audioTitle', 'Audio & Synthétiseur Sonore Procédural')}</span>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16 }}>
                    <div>
                        <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                            Volume Général ({Math.round(masterVolume * 100)}%) :
                        </label>
                        <input
                            type="range"
                            min="0"
                            max="1"
                            step="0.05"
                            value={masterVolume}
                            onChange={(e) => setMasterVolume(parseFloat(e.target.value))}
                            style={{ width: '100%', accentColor: '#10b981' }}
                        />
                    </div>

                    <div>
                        <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                            Ambiance Nature & Vent ({Math.round(ambientVolume * 100)}%) :
                        </label>
                        <input
                            type="range"
                            min="0"
                            max="1"
                            step="0.05"
                            value={ambientVolume}
                            onChange={(e) => setAmbientVolume(parseFloat(e.target.value))}
                            style={{ width: '100%', accentColor: '#10b981' }}
                        />
                    </div>

                    <div>
                        <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                            Bruits d'Essaim & Mandibules ({Math.round(sfxVolume * 100)}%) :
                        </label>
                        <input
                            type="range"
                            min="0"
                            max="1"
                            step="0.05"
                            value={sfxVolume}
                            onChange={(e) => setSfxVolume(parseFloat(e.target.value))}
                            style={{ width: '100%', accentColor: '#10b981' }}
                        />
                    </div>
                </div>
            </div>

            {/* 4. Engine & Network Default Parameters */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 14
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#a855f7' }}>
                    <Cpu size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>Paramètres Moteur & Réseau par Défaut</span>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16 }}>
                    <div>
                        <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                            Pas Temporel Par Défaut (dt) :
                        </label>
                        <select
                            value={stepSeconds}
                            onChange={(e) => setStepSeconds(parseFloat(e.target.value))}
                            style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '7px 8px', fontSize: 12 }}
                        >
                            <option value="0.0166">16.6 ms (60 Hz Ultra-précis)</option>
                            <option value="0.0333">33.3 ms (30 Hz)</option>
                            <option value="0.05">50.0 ms (20 Hz Standard)</option>
                            <option value="0.1">100 ms (10 Hz Macro)</option>
                        </select>
                    </div>

                    <div>
                        <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                            Hôte Serveur gRPC / WebSocket :
                        </label>
                        <input
                            type="text"
                            value={serverHost}
                            onChange={(e) => setServerHost(e.target.value)}
                            style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                        />
                    </div>

                    <div>
                        <label style={{ fontSize: 12, fontWeight: 700, color: textMuted, display: 'block', marginBottom: 6 }}>
                            Port Serveur :
                        </label>
                        <input
                            type="number"
                            value={serverPort}
                            onChange={(e) => setServerPort(e.target.value)}
                            style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                        />
                    </div>
                </div>
            </div>
        </div>
    )
}
