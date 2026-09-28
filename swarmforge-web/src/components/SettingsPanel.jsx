import React, { useState, useEffect } from 'react'
import {
    Globe,
    Moon,
    Sun,
    RotateCcw,
    Check,
    Info,
    Cpu,
    Zap,
    Boxes,
    Activity,
    Monitor,
    Sparkles,
    Sliders
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
        computeAcceleration,
        setComputeAcceleration,
        lodLevel,
        setLodLevel
    } = useSimulationStore()

    const [hasWebGpu, setHasWebGpu] = useState(false)
    const [targetFps, setTargetFps] = useState(60)

    useEffect(() => {
        if (typeof navigator !== 'undefined' && navigator.gpu) {
            setHasWebGpu(true)
        }
    }, [])

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
        setComputeAcceleration('auto')
        if (setLodLevel) setLodLevel('medium')
        setTargetFps(60)
        showToast(t('resetSettingsBtn', '✓ Paramètres rétablis par défaut !'), 'info')
    }

    const currentAccel = computeAcceleration || 'auto'

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
                        ⚙️ {t('settingsTitle', 'Paramètres du Client Visuel & Préférences')}
                    </h2>
                    <p style={{ margin: '4px 0 0', fontSize: 12, color: textMuted }}>
                        {t('settingsSubtitle', 'Personnalisation de la langue, du thème visuel et de l\'accélération de rendu GPU client (WebGPU / WebGL).')}
                    </p>
                </div>
                <button
                    onClick={handleResetDefaults}
                    title={t('resetSettingsBtn', 'Rétablir les paramètres par défaut')}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 6,
                        padding: '6px 14px',
                        borderRadius: 6,
                        border: `1px solid ${borderCol}`,
                        background: inputBg,
                        color: textMuted,
                        fontSize: 12,
                        fontWeight: 600,
                        cursor: 'pointer'
                    }}
                >
                    <RotateCcw size={14} />
                    <span>{t('resetSettingsBtn', 'Rétablir par défaut')}</span>
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

            {/* 3. Client GPU Compute & Graphics Acceleration */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#8b5cf6' }}>
                        <Zap size={18} />
                        <span style={{ fontSize: 14, fontWeight: 800 }}>{t('clientGpuTitle', 'Accélération Graphique & GPU Client (Rendu WebGPU / WebGL)')}</span>
                    </div>
                    <span
                        title={hasWebGpu ? t('clientGpuAvail', '✓ WebGPU Disponible') : t('clientGpuUnavail', '⚠ WebGPU Non supporté (Repli WebGL)')}
                        style={{
                            fontSize: 11,
                            padding: '2px 8px',
                            borderRadius: 4,
                            fontWeight: 700,
                            background: hasWebGpu ? (isDark ? '#064e3b' : '#d1fae5') : (isDark ? '#451a03' : '#ffedd5'),
                            color: hasWebGpu ? (isDark ? '#34d399' : '#065f46') : (isDark ? '#fb923c' : '#9a3412'),
                            border: `1px solid ${hasWebGpu ? '#10b981' : '#f97316'}`
                        }}
                    >
                        {hasWebGpu ? t('clientGpuAvail', '✓ WebGPU Disponible') : t('clientGpuUnavail', '⚠ WebGPU Non supporté (Repli WebGL)')}
                    </span>
                </div>
                <p style={{ margin: 0, fontSize: 12, color: textMuted }}>
                    {t('clientGpuDesc', 'Configurez le pipeline de calcul GPU local du navigateur (WebGPU Compute WGSL) pour la diffusion continue des nuages de phéromones et le rendu Three.js.')}
                </p>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <button
                        onClick={() => {
                            setComputeAcceleration('auto')
                            showToast(t('clientGpuAuto', '⚡ Automatique (WebGPU WGSL si supporté / Repli WebGL)'), 'info')
                        }}
                        title={t('clientGpuAutoTt', 'Sélectionne automatiquement WebGPU si supporté par votre navigateur, sinon utilise les Shaders WebGL standards.')}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '10px 16px',
                            borderRadius: 6,
                            border: currentAccel === 'auto' ? '2px solid #8b5cf6' : `1px solid ${borderCol}`,
                            background: currentAccel === 'auto' ? (isDark ? '#4c1d95' : '#ede9fe') : inputBg,
                            color: currentAccel === 'auto' ? (isDark ? '#c4b5fd' : '#5b21b6') : textMain,
                            fontSize: 13,
                            fontWeight: 700,
                            cursor: 'pointer',
                            textAlign: 'left'
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <Zap size={16} />
                            <span>{t('clientGpuAuto', '⚡ Automatique (WebGPU WGSL si supporté / Repli WebGL)')}</span>
                        </div>
                        {currentAccel === 'auto' && <Check size={16} />}
                    </button>

                    <button
                        onClick={() => {
                            setComputeAcceleration('gpu')
                            showToast(t('clientGpuWebGpu', '🎮 WebGPU Compute Pipeline (WGSL Dédié — Diffusion phéromones sur GPU client)'), 'info')
                        }}
                        title={t('clientGpuWebGpuTt', 'Force l\'activation du pipeline de calcul WebGPU dédié (WGSL) pour la diffusion et l\'évaporation matricielle 3D en local.')}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '10px 16px',
                            borderRadius: 6,
                            border: currentAccel === 'gpu' ? '2px solid #ec4899' : `1px solid ${borderCol}`,
                            background: currentAccel === 'gpu' ? (isDark ? '#831843' : '#fce7f3') : inputBg,
                            color: currentAccel === 'gpu' ? (isDark ? '#f472b6' : '#9d174d') : textMain,
                            fontSize: 13,
                            fontWeight: 700,
                            cursor: 'pointer',
                            textAlign: 'left'
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <Activity size={16} />
                            <span>{t('clientGpuWebGpu', '🎮 WebGPU Compute Pipeline (WGSL Dédié — Diffusion phéromones sur GPU client)')}</span>
                        </div>
                        {currentAccel === 'gpu' && <Check size={16} />}
                    </button>

                    <button
                        onClick={() => {
                            setComputeAcceleration('cpu')
                            showToast(t('clientGpuWebGl', '💻 Pure WebGL 2.0 Shaders (Compatibilité universelle)'), 'info')
                        }}
                        title={t('clientGpuWebGlTt', 'Utilise uniquement le pipeline de shaders WebGL standard compatible avec tous les navigateurs.')}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '10px 16px',
                            borderRadius: 6,
                            border: currentAccel === 'cpu' ? '2px solid #38bdf8' : `1px solid ${borderCol}`,
                            background: currentAccel === 'cpu' ? (isDark ? '#0369a1' : '#e0f2fe') : inputBg,
                            color: currentAccel === 'cpu' ? (isDark ? '#7dd3fc' : '#075985') : textMain,
                            fontSize: 13,
                            fontWeight: 700,
                            cursor: 'pointer',
                            textAlign: 'left'
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <Monitor size={16} />
                            <span>{t('clientGpuWebGl', '💻 Pure WebGL 2.0 Shaders (Compatibilité universelle)')}</span>
                        </div>
                        {currentAccel === 'cpu' && <Check size={16} />}
                    </button>
                </div>
            </div>

            {/* 4. Display Quality & Framerate Limit */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#10b981' }}>
                    <Sliders size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>{t('visualQualityTitle', 'Qualité Visuelle & Fréquence de Rendu Client')}</span>
                </div>
                <p style={{ margin: 0, fontSize: 12, color: textMuted }}>
                    {t('visualQualityDesc', 'Ajustez le niveau de détail des modèles d\'insectes (LOD) et le taux de rafraîchissement d\'affichage.')}
                </p>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <div>
                        <label
                            title={t('lodAntTt', 'Ajuste la complexité géométrique et l\'animation des modèles d\'insectes en fonction des performances.')}
                            style={{ fontSize: 12, fontWeight: 700, color: textMain, display: 'block', marginBottom: 6, cursor: 'help' }}
                        >
                            {t('lodAntLabel', 'Niveau de Détail (LOD Fourmis) :')}
                        </label>
                        <select
                            value={lodLevel || 'medium'}
                            title={t('lodAntTt', 'Ajuste la complexité géométrique et l\'animation des modèles d\'insectes en fonction des performances.')}
                            onChange={(e) => {
                                if (setLodLevel) setLodLevel(e.target.value)
                                showToast(`${t('lodAntLabel', 'LOD Fourmis :')} ${e.target.value}`, 'info')
                            }}
                            style={{
                                width: '100%',
                                padding: '8px 12px',
                                borderRadius: 6,
                                border: `1px solid ${borderCol}`,
                                background: inputBg,
                                color: textMain,
                                fontSize: 13,
                                fontWeight: 600,
                                cursor: 'pointer'
                            }}
                        >
                            <option value="high" title={t('lodAntHigh', 'Élevé (Modèles 3D détaillés avec pattes)')}>{t('lodAntHigh', 'Élevé (Modèles 3D détaillés avec pattes)')}</option>
                            <option value="medium" title={t('lodAntMed', 'Moyen (Low-Poly instancié équilibré)')}>{t('lodAntMed', 'Moyen (Low-Poly instancié équilibré)')}</option>
                            <option value="low" title={t('lodAntLow', 'Simplifié (Particules / Haute fluidité)')}>{t('lodAntLow', 'Simplifié (Particules / Haute fluidité)')}</option>
                        </select>
                    </div>

                    <div>
                        <label
                            title={t('targetFpsTt', 'Limite le taux de rafraîchissement maximal pour économiser la batterie et les ressources GPU.')}
                            style={{ fontSize: 12, fontWeight: 700, color: textMain, display: 'block', marginBottom: 6, cursor: 'help' }}
                        >
                            {t('targetFpsLabel', 'Plafond Fréquence d\'Affichage (FPS) :')}
                        </label>
                        <select
                            value={targetFps}
                            title={t('targetFpsTt', 'Limite le taux de rafraîchissement maximal pour économiser la batterie et les ressources GPU.')}
                            onChange={(e) => {
                                setTargetFps(Number(e.target.value))
                                showToast(`${t('targetFpsLabel', 'Plafond FPS :')} ${e.target.value} FPS`, 'info')
                            }}
                            style={{
                                width: '100%',
                                padding: '8px 12px',
                                borderRadius: 6,
                                border: `1px solid ${borderCol}`,
                                background: inputBg,
                                color: textMain,
                                fontSize: 13,
                                fontWeight: 600,
                                cursor: 'pointer'
                            }}
                        >
                            <option value={60} title={t('targetFps60', '60 FPS (Fluide standard)')}>{t('targetFps60', '60 FPS (Fluide standard)')}</option>
                            <option value={30} title={t('targetFps30', '30 FPS (Économie de batterie / GPU)')}>{t('targetFps30', '30 FPS (Économie d\'énergie / GPU)')}</option>
                            <option value={120} title={t('targetFps120', '120 FPS (Écrans haute fréquence)')}>{t('targetFps120', '120 FPS (Écrans haute fréquence)')}</option>
                        </select>
                    </div>
                </div>
            </div>

            {/* 5. System & Architecture Info Card */}
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
                <div><strong>{t('aboutArchitecture', 'Architecture :')}</strong> {t('aboutArchDesc', 'Client Léger WebGL 2.0 / WebGPU (Three.js / React Three Fiber) avec moteur sonore procédural WebAudio. La simulation physique et l\'intelligence collective sont exécutées sur le serveur SwarmForge.')}</div>
                <div><strong>{t('aboutAuthor', 'Auteur :')}</strong> Silvère Martin-Michiellot & Gemini AI Assistant (Google DeepMind)</div>
            </div>
        </div>
    )
}

