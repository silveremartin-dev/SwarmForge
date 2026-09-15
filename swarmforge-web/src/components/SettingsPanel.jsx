import React from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { SUPPORTED_LANGUAGES, getTranslation } from '../i18n/translations'
import {
    Globe,
    Moon,
    Sun,
    Monitor,
    Layers,
    Map,
    Grid as GridIcon,
    Sparkles,
    Eye,
    Shield,
    RotateCcw,
    Check,
    Cpu,
    Activity
} from 'lucide-react'

export default function SettingsPanel() {
    const {
        language,
        setLanguage,
        theme,
        setTheme,
        toggleTheme,
        showMinimap,
        setShowMinimap,
        toggleMinimap,
        showGrid,
        setShowGrid,
        toggleGrid,
        show3DSkirt,
        toggle3DSkirt,
        showScientificIsolinesTopo,
        toggleScientificIsolinesTopo,
        showScientificIsolinesMicroclimate,
        toggleScientificIsolinesMicroclimate,
        showScientificIsolinesPheromones,
        toggleScientificIsolinesPheromones,
        isUVVisionMode,
        toggleUVVisionMode,
        lookAndFeel,
        setLookAndFeel,
        measuredTps
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const styles = {
        container: {
            display: 'flex',
            flexDirection: 'column',
            gap: 16,
            color: isDark ? '#fff' : '#0f172a',
            fontFamily: 'system-ui, -apple-system, sans-serif'
        },
        section: {
            background: isDark ? 'rgba(30, 41, 59, 0.65)' : 'rgba(241, 245, 249, 0.9)',
            border: isDark ? '1px solid rgba(255, 255, 255, 0.08)' : '1px solid rgba(0, 0, 0, 0.1)',
            borderRadius: 10,
            padding: 12,
            display: 'flex',
            flexDirection: 'column',
            gap: 10
        },
        title: {
            fontSize: 12,
            fontWeight: 800,
            color: isDark ? '#38bdf8' : '#0284c7',
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            textTransform: 'uppercase',
            letterSpacing: '0.05em'
        },
        subtext: {
            fontSize: 11,
            color: isDark ? '#94a3b8' : '#64748b',
            lineHeight: 1.4,
            margin: 0
        },
        langGrid: {
            display: 'grid',
            gridTemplateColumns: 'repeat(2, 1fr)',
            gap: 6
        },
        langBtn: (active) => ({
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '8px 10px',
            borderRadius: 8,
            border: active
                ? '1px solid #38bdf8'
                : isDark ? '1px solid #334155' : '1px solid #cbd5e1',
            background: active
                ? (isDark ? 'rgba(56, 189, 248, 0.18)' : 'rgba(56, 189, 248, 0.15)')
                : (isDark ? '#0f172a' : '#ffffff'),
            color: active ? (isDark ? '#38bdf8' : '#0369a1') : (isDark ? '#e2e8f0' : '#334155'),
            cursor: 'pointer',
            fontSize: 11,
            fontWeight: active ? 700 : 500,
            transition: 'all 0.15s ease'
        }),
        themeRow: {
            display: 'grid',
            gridTemplateColumns: 'repeat(2, 1fr)',
            gap: 8
        },
        themeBtn: (active) => ({
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            padding: '10px 12px',
            borderRadius: 8,
            border: active
                ? '2px solid #38bdf8'
                : isDark ? '1px solid #334155' : '1px solid #cbd5e1',
            background: active
                ? (isDark ? 'rgba(56, 189, 248, 0.18)' : 'rgba(56, 189, 248, 0.15)')
                : (isDark ? '#0f172a' : '#ffffff'),
            color: active ? (isDark ? '#38bdf8' : '#0369a1') : (isDark ? '#cbd5e1' : '#475569'),
            cursor: 'pointer',
            fontSize: 12,
            fontWeight: 700,
            transition: 'all 0.15s ease'
        }),
        checkboxRow: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '6px 8px',
            borderRadius: 6,
            background: isDark ? 'rgba(15, 23, 42, 0.5)' : 'rgba(255, 255, 255, 0.6)',
            cursor: 'pointer',
            fontSize: 11,
            color: isDark ? '#e2e8f0' : '#1e293b'
        }
    }

    return (
        <div style={styles.container}>
            {/* Header Description */}
            <div>
                <h3 style={{ fontSize: 14, fontWeight: 800, margin: '0 0 4px 0', color: isDark ? '#38bdf8' : '#0284c7' }}>
                    🌐 {t('settingsTitle', 'Configuration & Préférences')}
                </h3>
                <p style={styles.subtext}>
                    {t('langDesc', 'Personnalisez la langue, le thème graphique et les paramètres de visualisation 3D.')}
                </p>
            </div>

            {/* 1. Language Selection */}
            <div style={styles.section}>
                <div style={styles.title}>
                    <Globe size={14} />
                    <span>{t('langTitle', 'Langue de l\'Interface')}</span>
                </div>
                <div style={styles.langGrid}>
                    {SUPPORTED_LANGUAGES.map(lang => {
                        const isCurrent = language === lang.code
                        return (
                            <button
                                key={lang.code}
                                style={styles.langBtn(isCurrent)}
                                onClick={() => setLanguage(lang.code)}
                            >
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <span style={{ fontSize: 14 }}>{lang.flag}</span>
                                    <span>{lang.name}</span>
                                </span>
                                {isCurrent && <Check size={13} color="#38bdf8" />}
                            </button>
                        )
                    })}
                </div>
                <div style={{ fontSize: 10, color: isDark ? '#64748b' : '#94a3b8', display: 'flex', alignItems: 'center', gap: 4 }}>
                    <span style={{ color: '#10b981', fontWeight: 'bold' }}>●</span>
                    <span>{t('defaultLangBadge', 'Langue par défaut sauvegardée localement (localStorage).')}</span>
                </div>
            </div>

            {/* 2. Theme Selection (Dark / Light) */}
            <div style={styles.section}>
                <div style={styles.title}>
                    <Monitor size={14} />
                    <span>{t('themeTitle', 'Thème Graphique')}</span>
                </div>
                <div style={styles.themeRow}>
                    <button
                        style={styles.themeBtn(isDark)}
                        onClick={() => setTheme('dark')}
                    >
                        <Moon size={15} />
                        <span>{t('themeDark', 'Mode Sombre')}</span>
                    </button>
                    <button
                        style={styles.themeBtn(!isDark)}
                        onClick={() => setTheme('light')}
                    >
                        <Sun size={15} />
                        <span>{t('themeLight', 'Mode Clair')}</span>
                    </button>
                </div>
                <p style={styles.subtext}>
                    {t('themeDesc', 'Basculez entre le thème sombre pour observation nocturne et le thème clair haute lisibilité.')}
                </p>
            </div>

            {/* 3. Display & Viewport Checkboxes */}
            <div style={styles.section}>
                <div style={styles.title}>
                    <Layers size={14} />
                    <span>{t('displaySettingsTitle', 'Options d\'Affichage & Rendu')}</span>
                </div>

                <label style={styles.checkboxRow}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <Map size={13} color="#38bdf8" />
                        <span>{t('showMinimap', 'Afficher la Minimap 2D Radar')}</span>
                    </span>
                    <input
                        type="checkbox"
                        checked={showMinimap}
                        onChange={toggleMinimap}
                        style={{ cursor: 'pointer' }}
                    />
                </label>

                <label style={styles.checkboxRow}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <GridIcon size={13} color="#a855f7" />
                        <span>{t('showGrid', 'Afficher la Grille Spatiale de Référence')}</span>
                    </span>
                    <input
                        type="checkbox"
                        checked={showGrid}
                        onChange={toggleGrid}
                        style={{ cursor: 'pointer' }}
                    />
                </label>

                <label style={styles.checkboxRow}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <Layers size={13} color="#f59e0b" />
                        <span>{t('layerSkirt', 'Jupe Géologique 3D')}</span>
                    </span>
                    <input
                        type="checkbox"
                        checked={show3DSkirt}
                        onChange={toggle3DSkirt}
                        style={{ cursor: 'pointer' }}
                    />
                </label>

                <label style={styles.checkboxRow}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <Eye size={13} color="#ec4899" />
                        <span>{t('uvMode', 'Mode Vision Ultraviolette (UV)')}</span>
                    </span>
                    <input
                        type="checkbox"
                        checked={isUVVisionMode}
                        onChange={toggleUVVisionMode}
                        style={{ cursor: 'pointer' }}
                    />
                </label>
            </div>

            {/* 4. 3D Engine & Scientific Mode Optimization */}
            <div style={styles.section}>
                <div style={styles.title}>
                    <Cpu size={14} />
                    <span>{t('engineSectionTitle', 'Moteur 3D & Performance')}</span>
                </div>
                <div style={{
                    background: isDark ? '#0f172a' : '#ffffff',
                    border: isDark ? '1px solid #1e293b' : '1px solid #e2e8f0',
                    borderRadius: 8,
                    padding: 10,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 6,
                    fontSize: 11
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: isDark ? '#94a3b8' : '#64748b' }}>Moteur WebGL :</span>
                        <strong style={{ color: '#38bdf8' }}>Three.js / Three Fiber 8.x</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: isDark ? '#94a3b8' : '#64748b' }}>Mode de Rendu Dédié :</span>
                        <strong style={{ color: '#10b981' }}>🔬 Scientifique (60 FPS)</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: isDark ? '#94a3b8' : '#64748b' }}>Cadence Réelle (TPS) :</span>
                        <strong style={{ color: '#f59e0b' }}>{measuredTps} ticks/s</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: isDark ? '#94a3b8' : '#64748b' }}>Accélération Matérielle :</span>
                        <strong style={{ color: '#a855f7' }}>GPU Shader Pipeline</strong>
                    </div>
                </div>
                <p style={styles.subtext}>
                    {t('engineModeDesc', 'Moteur 3D Scientifique Three.js haute fidélité optimisé pour un taux de rafraîchissement constant à 60 FPS.')}
                </p>
            </div>
        </div>
    )
}
