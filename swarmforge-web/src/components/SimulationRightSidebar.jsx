import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import {
    Eye,
    Layers,
    Scissors,
    Sparkles,
    Sun,
    CloudRain,
    Wind,
    Trees,
    Shield,
    Sliders,
    ChevronDown,
    ChevronUp,
    Info,
    Grid as GridIcon,
    Flame,
    Droplet,
    Thermometer,
    Zap,
    Map,
    Activity,
    Compass
} from 'lucide-react'

export default function SimulationRightSidebar() {
    const {
        language,
        theme,
        lookAndFeel,
        setLookAndFeel,
        showTerrain,
        toggleTerrain,
        show3DSkirt,
        toggle3DSkirt,
        showVegetation,
        toggleVegetation,
        showChambers,
        toggleChambers,
        showPheromones,
        togglePheromones,
        showAnts,
        toggleAnts,
        showWeather,
        toggleWeather,
        showMinimap,
        toggleMinimap,
        showGrid,
        toggleGrid,
        slicePlaneRatio,
        setSlicePlaneRatio,
        showScientificIsolinesTopo,
        toggleScientificIsolinesTopo,
        showScientificIsolinesMicroclimate,
        toggleScientificIsolinesMicroclimate,
        showScientificIsolinesPheromones,
        toggleScientificIsolinesPheromones,
        isUVVisionMode,
        toggleUVVisionMode,
        showLegend,
        toggleLegend
    } = useSimulationStore()

    const [collapsed, setCollapsed] = useState(false)
    const [legendSubstratesOpen, setLegendSubstratesOpen] = useState(true)
    const [legendCastesOpen, setLegendCastesOpen] = useState(true)
    const [legendPheroOpen, setLegendPheroOpen] = useState(true)

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const substrates = [
        { name: 'Humus Organique', color: '#523219' },
        { name: 'Terre Végétale', color: '#3d2817' },
        { name: 'Sable Fin', color: '#eab308' },
        { name: 'Argile Compacte', color: '#9a3412' },
        { name: 'Limon Humide', color: '#ca8a04' },
        { name: 'Tourbe Noire', color: '#451a03' },
        { name: 'Gravier / Cailloux', color: '#94a3b8' },
        { name: 'Roche-Mère', color: '#64748b' },
        { name: 'Cavités / Galeries', color: '#0f172a' },
        { name: 'Racines Végétales', color: '#78350f' },
        { name: 'Nappe Phréatique', color: '#0284c7' },
        { name: 'Flore de Surface', color: '#15803d' },
    ]

    const castes = [
        { name: 'Reine Fondatrice', icon: '👑', color: '#a855f7', desc: 'Reproduction & phéromone royale' },
        { name: 'Ouvrière Fourrageuse', icon: '🌾', color: '#f59e0b', desc: 'Collecte de nourriture & exploration' },
        { name: 'Soldat Défenseur', icon: '⚔️', color: '#ef4444', desc: 'Mandibules fortes & défense du nid' },
        { name: 'Gardienne d\'Entrée', icon: '🛡️', color: '#38bdf8', desc: 'Contrôle cuticulaire CHC' },
        { name: 'Larve en Incubation', icon: '🍼', color: '#86efac', desc: 'Nourrie par régurgitation' },
        { name: 'Œuf Colonial', icon: '🥚', color: '#fef08a', desc: 'Soin constant en chambre royale' },
        { name: 'Araignée / Prédateur', icon: '🕷️', color: '#e11d48', desc: 'Chasse les fourmis isolées' }
    ]

    const pheromones = [
        { name: 'Piste Alimentaire', color: '#f59e0b', icon: '🟡', desc: 'Indique une source de miellat/graines' },
        { name: 'Signal d\'Alerte', color: '#ef4444', icon: '🔴', desc: 'Mobilisation face à un danger' },
        { name: 'Phéromone Royale', color: '#a855f7', icon: '🟣', desc: 'Cohésion coloniale et hiérarchie' },
        { name: 'Recrutement Massif', color: '#0284c7', icon: '🔵', desc: 'Attaque ou transport de grosse proie' },
    ]

    const styles = {
        container: {
            position: 'absolute',
            top: 60,
            right: 20,
            width: collapsed ? 46 : 330,
            maxHeight: 'calc(100vh - 75px)',
            zIndex: 90,
            display: 'flex',
            flexDirection: 'column',
            gap: 8,
            transition: 'width 0.25s ease',
            pointerEvents: 'auto',
            fontFamily: 'system-ui, -apple-system, sans-serif'
        },
        card: {
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.94)',
            backdropFilter: 'blur(16px)',
            border: isDark ? '1px solid rgba(56, 189, 248, 0.3)' : '1px solid rgba(56, 189, 248, 0.5)',
            borderRadius: 12,
            padding: collapsed ? '8px 4px' : 12,
            color: isDark ? '#fff' : '#0f172a',
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
            display: 'flex',
            flexDirection: 'column',
            gap: 10,
            overflowY: 'auto'
        },
        sectionTitle: {
            fontSize: 11,
            fontWeight: 800,
            color: isDark ? '#38bdf8' : '#0284c7',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            textTransform: 'uppercase',
            letterSpacing: '0.5px'
        },
        toggleRow: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: 11,
            color: isDark ? '#cbd5e1' : '#334155',
            padding: '4px 6px',
            borderRadius: 6,
            background: isDark ? 'rgba(255,255,255,0.03)' : 'rgba(0,0,0,0.03)',
            cursor: 'pointer',
            transition: 'background 0.15s ease'
        },
        sliderTrack: {
            width: '100%',
            height: 6,
            borderRadius: 3,
            background: isDark ? '#1e293b' : '#cbd5e1',
            accentColor: '#38bdf8',
            cursor: 'pointer'
        },
        accordionHeader: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            background: isDark ? '#1e293b' : '#e2e8f0',
            padding: '6px 8px',
            borderRadius: 6,
            cursor: 'pointer',
            fontSize: 11,
            fontWeight: 700,
            color: isDark ? '#38bdf8' : '#0369a1'
        },
        legendItem: {
            display: 'flex',
            alignItems: 'center',
            gap: 6,
            fontSize: 10,
            color: isDark ? '#94a3b8' : '#475569',
            padding: '3px 0'
        },
        modeBtn: (active) => ({
            flex: 1,
            padding: '6px 4px',
            fontSize: 10,
            fontWeight: 700,
            borderRadius: 6,
            border: active
                ? '1px solid #38bdf8'
                : isDark ? '1px solid #334155' : '1px solid #cbd5e1',
            background: active
                ? (isDark ? 'rgba(56, 189, 248, 0.2)' : 'rgba(56, 189, 248, 0.15)')
                : (isDark ? '#0f172a' : '#ffffff'),
            color: active
                ? (isDark ? '#38bdf8' : '#0284c7')
                : (isDark ? '#94a3b8' : '#64748b'),
            cursor: 'pointer',
            textAlign: 'center',
            transition: 'all 0.15s ease'
        })
    }

    if (collapsed) {
        return (
            <div style={styles.container}>
                <div style={styles.card}>
                    <button
                        onClick={() => setCollapsed(false)}
                        title="Ouvrir les options de rendu et légendes"
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: '#38bdf8',
                            cursor: 'pointer',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            gap: 4,
                            padding: 6
                        }}
                    >
                        <Layers size={20} />
                        <span style={{ fontSize: 9, writingMode: 'vertical-rl', transform: 'rotate(180deg)' }}>OPTIONS 3D</span>
                    </button>
                </div>
            </div>
        )
    }

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                {/* Header with Collapse Button */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: isDark ? '1px solid rgba(255,255,255,0.1)' : '1px solid rgba(0,0,0,0.1)', paddingBottom: 6 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, fontWeight: 800, color: isDark ? '#38bdf8' : '#0284c7' }}>
                        <Sliders size={14} />
                        <span>{t('renderOptionsTitle', 'OPTIONS DE RENDU 3D')}</span>
                    </div>
                    <button
                        onClick={() => setCollapsed(true)}
                        title="Réduire le panneau"
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: 2 }}
                    >
                        <ChevronUp size={16} />
                    </button>
                </div>

                {/* 1. View Mode Switcher */}
                <div>
                    <div style={{ ...styles.sectionTitle, marginBottom: 6 }}>
                        <span>{t('viewMode', 'Mode de Vue 3D')}</span>
                        <span style={{ fontSize: 9, color: '#10b981', fontWeight: 'bold' }}>● 60 FPS</span>
                    </div>
                    <div style={{ display: 'flex', gap: 4 }}>
                        <button
                            style={styles.modeBtn(lookAndFeel === 'SCIENTIFIC')}
                            onClick={() => setLookAndFeel('SCIENTIFIC')}
                            title="Vue analytique et thermique scientifique"
                        >
                            {t('modeScientific', '🔬 Scientifique')}
                        </button>
                        <button
                            style={styles.modeBtn(lookAndFeel === 'REALISTIC')}
                            onClick={() => setLookAndFeel('REALISTIC')}
                            title="Textures PBR et ombres solaires"
                        >
                            {t('modeRealistic', '🌿 Réaliste')}
                        </button>
                        <button
                            style={styles.modeBtn(lookAndFeel === 'GAMING')}
                            onClick={() => setLookAndFeel('GAMING')}
                            title="Rendu toon et stylisé"
                        >
                            {t('modeGamified', '🎮 Gamifié')}
                        </button>
                    </div>
                </div>

                {/* 2. Layer Visibility Checkboxes */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <div style={styles.sectionTitle}>
                        <span>Calques Visibles</span>
                    </div>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Layers size={12} color="#ca8a04" />
                            <span>{t('layerTerrain', 'Sol & Relief')}</span>
                        </span>
                        <input type="checkbox" checked={showTerrain} onChange={toggleTerrain} />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Layers size={12} color="#f59e0b" />
                            <span>{t('layerSkirt', 'Jupe 3D Géologique')}</span>
                        </span>
                        <input type="checkbox" checked={show3DSkirt} onChange={toggle3DSkirt} />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Trees size={12} color="#22c55e" />
                            <span>{t('layerVegetation', 'Végétation')}</span>
                        </span>
                        <input type="checkbox" checked={showVegetation} onChange={toggleVegetation} />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Shield size={12} color="#a855f7" />
                            <span>{t('layerChambers', 'Cavités & Nids')}</span>
                        </span>
                        <input type="checkbox" checked={showChambers} onChange={toggleChambers} />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Sparkles size={12} color="#38bdf8" />
                            <span>{t('layerPheromones', 'Phéromones')}</span>
                        </span>
                        <input type="checkbox" checked={showPheromones} onChange={togglePheromones} />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Zap size={12} color="#ef4444" />
                            <span>{t('layerAnts', 'Fourmis & Castes')}</span>
                        </span>
                        <input type="checkbox" checked={showAnts} onChange={toggleAnts} />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <CloudRain size={12} color="#60a5fa" />
                            <span>{t('layerWeather', 'Météo & Ciel')}</span>
                        </span>
                        <input type="checkbox" checked={showWeather} onChange={toggleWeather} />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Map size={12} color="#10b981" />
                            <span>{t('showMinimap', 'Minimap 2D Radar')}</span>
                        </span>
                        <input type="checkbox" checked={showMinimap} onChange={toggleMinimap} />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <GridIcon size={12} color="#818cf8" />
                            <span>{t('showGrid', 'Grille de Référence')}</span>
                        </span>
                        <input type="checkbox" checked={showGrid} onChange={toggleGrid} />
                    </label>
                </div>

                {/* 3. Axial Slicing Plane Slider (Coupe Axiale) */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 11, fontWeight: 700 }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 5, color: isDark ? '#38bdf8' : '#0284c7' }}>
                            <Scissors size={13} />
                            <span>Coupe Axiale (Slice Plane)</span>
                        </span>
                        <span style={{ color: '#f59e0b', fontSize: 10 }}>{(slicePlaneRatio * 100).toFixed(0)}%</span>
                    </div>
                    <input
                        type="range"
                        min="0"
                        max="1"
                        step="0.01"
                        value={slicePlaneRatio}
                        onChange={(e) => setSlicePlaneRatio(parseFloat(e.target.value))}
                        style={styles.sliderTrack}
                    />
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 9, color: isDark ? '#64748b' : '#94a3b8' }}>
                        <span>Profondeur 0%</span>
                        <span>Surface 100%</span>
                    </div>
                </div>

                {/* 4. Scientific Mode Isolines & UV Vision */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <div style={styles.sectionTitle}>
                        <span>{t('scientificFilters', 'Filtres Scientifiques')}</span>
                    </div>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <GridIcon size={12} color="#eab308" />
                            <span>{t('isolineTopo', 'Isolines Topographiques')}</span>
                        </span>
                        <input
                            type="checkbox"
                            checked={showScientificIsolinesTopo}
                            onChange={toggleScientificIsolinesTopo}
                        />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Thermometer size={12} color="#f97316" />
                            <span>{t('isolineClimate', 'Isolines Microclimat')}</span>
                        </span>
                        <input
                            type="checkbox"
                            checked={showScientificIsolinesMicroclimate}
                            onChange={toggleScientificIsolinesMicroclimate}
                        />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Sparkles size={12} color="#06b6d4" />
                            <span>{t('isolinePhero', 'Isolines Phéromones')}</span>
                        </span>
                        <input
                            type="checkbox"
                            checked={showScientificIsolinesPheromones}
                            onChange={toggleScientificIsolinesPheromones}
                        />
                    </label>

                    <label style={styles.toggleRow}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <Eye size={12} color="#ec4899" />
                            <span>{t('uvMode', 'Mode Ultraviolet (UV)')}</span>
                        </span>
                        <input
                            type="checkbox"
                            checked={isUVVisionMode}
                            onChange={toggleUVVisionMode}
                        />
                    </label>
                </div>

                {/* 5. Complete Accordion Legend */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 4 }}>
                    <div style={{ ...styles.sectionTitle, borderTop: isDark ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)', paddingTop: 8 }}>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                            <Info size={12} />
                            <span>{t('legendTitle', 'Légendes & Glossaire')}</span>
                        </span>
                        <label style={{ display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer', fontSize: 10, textTransform: 'none', color: isDark ? '#94a3b8' : '#64748b' }}>
                            <input type="checkbox" checked={showLegend} onChange={toggleLegend} />
                            <span>Afficher</span>
                        </label>
                    </div>

                    {showLegend && (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                            {/* Substrats */}
                            <div>
                                <div style={styles.accordionHeader} onClick={() => setLegendSubstratesOpen(!legendSubstratesOpen)}>
                                    <span>{t('legendSubstrates', 'Substrats Géologiques')}</span>
                                    {legendSubstratesOpen ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                                </div>
                                {legendSubstratesOpen && (
                                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 3, padding: '6px 4px' }}>
                                        {substrates.map((s, idx) => (
                                            <div key={idx} style={styles.legendItem}>
                                                <span style={{ width: 8, height: 8, borderRadius: 2, background: s.color, display: 'inline-block', flexShrink: 0 }} />
                                                <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{s.name}</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {/* Castes */}
                            <div>
                                <div style={styles.accordionHeader} onClick={() => setLegendCastesOpen(!legendCastesOpen)}>
                                    <span>{t('legendCastes', 'Castes & Organismes')}</span>
                                    {legendCastesOpen ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                                </div>
                                {legendCastesOpen && (
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 3, padding: '6px 4px' }}>
                                        {castes.map((c, idx) => (
                                            <div key={idx} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 10, color: isDark ? '#cbd5e1' : '#334155' }}>
                                                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                                    <span>{c.icon}</span>
                                                    <span style={{ color: c.color, fontWeight: 700 }}>{c.name}</span>
                                                </span>
                                                <span style={{ fontSize: 9, color: isDark ? '#64748b' : '#94a3b8' }}>{c.desc}</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            {/* Phéromones */}
                            <div>
                                <div style={styles.accordionHeader} onClick={() => setLegendPheroOpen(!legendPheroOpen)}>
                                    <span>{t('legendPheromones', 'Phéromones Chimiques')}</span>
                                    {legendPheroOpen ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                                </div>
                                {legendPheroOpen && (
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 3, padding: '6px 4px' }}>
                                        {pheromones.map((p, idx) => (
                                            <div key={idx} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 10, color: isDark ? '#cbd5e1' : '#334155' }}>
                                                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                                    <span>{p.icon}</span>
                                                    <span style={{ color: p.color, fontWeight: 700 }}>{p.name}</span>
                                                </span>
                                                <span style={{ fontSize: 9, color: isDark ? '#64748b' : '#94a3b8' }}>{p.desc}</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    )
}
