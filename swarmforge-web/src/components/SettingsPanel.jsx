import React from 'react'
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
    Activity
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
        engineBackend,
        setEngineBackend,
        computeAcceleration,
        setComputeAcceleration
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
        setEngineBackend('auto')
        setComputeAcceleration('auto')
        showToast(t('resetSettingsBtn', '✓ Paramètres rétablis par défaut !'), 'info')
    }

    const currentEngine = engineBackend || 'auto'
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
                        ⚙️ {t('settingsTitle', 'Paramètres de l\'Application & Préférences')}
                    </h2>
                    <p style={{ margin: '4px 0 0', fontSize: 12, color: textMuted }}>
                        {t('settingsSubtitle', 'Personnalisation de la langue, de l\'affichage et du moteur 3D.')}
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

            {/* 3. Simulation Compute Engine (Dual-Engine Selection) */}
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
                    <Boxes size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>{t('engineComputeTitle', 'Moteur de Calcul de la Simulation')}</span>
                </div>
                <p style={{ margin: 0, fontSize: 12, color: textMuted }}>
                    {t('engineComputeDesc', 'Sélectionnez le backend d\'exécution de la simulation : Rust Natif haute performance ou Java Artemis ECS.')}
                </p>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <button
                        onClick={() => {
                            setEngineBackend('auto')
                            showToast(t('engineBackendAuto', '⚡ Automatique (Rust SIMD si disponible / Repli Java ECS)'), 'info')
                        }}
                        title={t('engineComputeTt', 'Sélection automatique du backend le plus performant')}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '10px 16px',
                            borderRadius: 6,
                            border: currentEngine === 'auto' ? '2px solid #10b981' : `1px solid ${borderCol}`,
                            background: currentEngine === 'auto' ? (isDark ? '#064e3b' : '#d1fae5') : inputBg,
                            color: currentEngine === 'auto' ? (isDark ? '#34d399' : '#065f46') : textMain,
                            fontSize: 13,
                            fontWeight: 700,
                            cursor: 'pointer',
                            textAlign: 'left'
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <Zap size={16} />
                            <span>{t('engineBackendAuto', '⚡ Automatique (Rust SIMD si disponible / Repli Java ECS)')}</span>
                        </div>
                        {currentEngine === 'auto' && <Check size={16} />}
                    </button>

                    <button
                        onClick={() => {
                            setEngineBackend('rust_native')
                            showToast(t('engineBackendRust', '🦀 Rust Natif SIMD (Zero-GC, Panama FFM, Morton 3D)'), 'info')
                        }}
                        title="Force l'utilisation du moteur natif compilé Rust cdylib via Project Panama FFM"
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '10px 16px',
                            borderRadius: 6,
                            border: currentEngine === 'rust_native' ? '2px solid #f97316' : `1px solid ${borderCol}`,
                            background: currentEngine === 'rust_native' ? (isDark ? '#7c2d12' : '#ffedd5') : inputBg,
                            color: currentEngine === 'rust_native' ? (isDark ? '#fb923c' : '#9a3412') : textMain,
                            fontSize: 13,
                            fontWeight: 700,
                            cursor: 'pointer',
                            textAlign: 'left'
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <Activity size={16} />
                            <span>{t('engineBackendRust', '🦀 Rust Natif SIMD (Zero-GC, Panama FFM, Morton 3D)')}</span>
                        </div>
                        {currentEngine === 'rust_native' && <Check size={16} />}
                    </button>

                    <button
                        onClick={() => {
                            setEngineBackend('java_ecs')
                            showToast(t('engineBackendJava', '☕ Java 21 Artemis ECS (Moteur de Référence Standard)'), 'info')
                        }}
                        title="Utilise le moteur standard Java 21 Artemis-odb ECS"
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            padding: '10px 16px',
                            borderRadius: 6,
                            border: currentEngine === 'java_ecs' ? '2px solid #38bdf8' : `1px solid ${borderCol}`,
                            background: currentEngine === 'java_ecs' ? (isDark ? '#0369a1' : '#e0f2fe') : inputBg,
                            color: currentEngine === 'java_ecs' ? (isDark ? '#7dd3fc' : '#075985') : textMain,
                            fontSize: 13,
                            fontWeight: 700,
                            cursor: 'pointer',
                            textAlign: 'left'
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <Cpu size={16} />
                            <span>{t('engineBackendJava', '☕ Java 21 Artemis ECS (Moteur de Référence Standard)')}</span>
                        </div>
                        {currentEngine === 'java_ecs' && <Check size={16} />}
                    </button>
                </div>
            </div>

            {/* 4. Hardware Acceleration Mode Card */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#8b5cf6' }}>
                    <Zap size={18} />
                    <span style={{ fontSize: 14, fontWeight: 800 }}>{t('hardwareAccelTitle', 'Accélération Matérielle (Calculs GPU / CPU)')}</span>
                </div>
                <p style={{ margin: 0, fontSize: 12, color: textMuted }}>
                    {t('hardwareAccelDesc', 'Configurez l\'accélération matérielle pour la diffusion des phéromones, l\'hydrologie 3D et la biomécanique.')}
                </p>

                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                    <button
                        onClick={() => {
                            setComputeAcceleration('auto')
                            showToast(t('hardwareAccelAuto', '⚡ Automatique (GPU si disponible / Repli CPU Vectoriel)'), 'info')
                        }}
                        title={t('hardwareAccelTt', 'Détection automatique de l\'accélérateur matériel le plus rapide')}
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
                            <span>{t('hardwareAccelAuto', '⚡ Automatique (GPU si disponible / Repli CPU Vectoriel)')}</span>
                        </div>
                        {currentAccel === 'auto' && <Check size={16} />}
                    </button>

                    <button
                        onClick={() => {
                            setComputeAcceleration('gpu')
                            showToast(t('hardwareAccelGpu', '🎮 Accélération GPU (WebGPU / TornadoVM OpenCL)'), 'info')
                        }}
                        title="Force les calculs de grille matricielle continue sur le GPU"
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
                            <span>{t('hardwareAccelGpu', '🎮 Accélération GPU (WebGPU / TornadoVM OpenCL)')}</span>
                        </div>
                        {currentAccel === 'gpu' && <Check size={16} />}
                    </button>

                    <button
                        onClick={() => {
                            setComputeAcceleration('cpu')
                            showToast(t('hardwareAccelCpu', '💻 Pur CPU Multithreadé (SIMD / Vector API)'), 'info')
                        }}
                        title="Force l'exécution des calculs continus sur CPU multithreadé"
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
                            <Cpu size={16} />
                            <span>{t('hardwareAccelCpu', '💻 Pur CPU Multithreadé (SIMD / Vector API)')}</span>
                        </div>
                        {currentAccel === 'cpu' && <Check size={16} />}
                    </button>
                </div>
            </div>

            {/* 5. System & Engine Info Card */}
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

