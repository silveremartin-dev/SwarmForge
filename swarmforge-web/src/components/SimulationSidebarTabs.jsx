import React from 'react'
import {
    Volume2,
    VolumeX,
    Bird,
    Bug,
    Layers,
    Scissors,
    Sparkles,
    Sun,
    CloudRain,
    Trees,
    Shield,
    Grid as GridIcon,
    Thermometer,
    Zap,
    Map,
    Waves
} from 'lucide-react'
import { soundEngine } from '../utils/soundEngine'

const DEFAULT_SUBSTRATES = [
    { name: 'Humus Organique', color: '#523219', desc: 'Couche superficielle riche en litière' },
    { name: 'Terre Végétale', color: '#3d2817', desc: 'Sol meuble propice aux galeries' },
    { name: 'Sable Fin', color: '#eab308', desc: 'Berges et zones meubles perméables' },
    { name: 'Argile Compacte', color: '#9a3412', desc: 'Chambres royales et stabilisation' },
    { name: 'Limon Humide', color: '#ca8a04', desc: 'Substrat alluvial hydraté' },
    { name: 'Tourbe Noire', color: '#451a03', desc: 'Matière organique dense et acide' },
    { name: 'Gravier / Cailloux', color: '#94a3b8', desc: 'Drainage naturel et barrière' },
    { name: 'Roche-Mère', color: '#64748b', desc: 'Socle rocheux infranchissable' },
    { name: 'Cavités / Galeries', color: '#0f172a', desc: 'Tunnels creusés par la colonie' },
    { name: 'Racines Végétales', color: '#78350f', desc: 'Ancrage et conduits de sève' },
    { name: 'Nappe Phréatique', color: '#0284c7', desc: 'Source hydrique souterraine' },
    { name: 'Flore de Surface', color: '#15803d', desc: 'Végétation et canopée' }
]

const DEFAULT_CASTES = [
    { name: 'Reine Fondatrice', icon: '👑', color: '#a855f7', desc: 'Reproduction & phéromone royale' },
    { name: 'Ouvrière Fourrageuse', icon: '🌾', color: '#f59e0b', desc: 'Collecte de nourriture & exploration' },
    { name: 'Soldat Défenseur', icon: '⚔️', color: '#ef4444', desc: 'Mandibules fortes & défense du nid' },
    { name: 'Gardienne d\'Entrée', icon: '🛡️', color: '#38bdf8', desc: 'Contrôle cuticulaire CHC' },
    { name: 'Larve en Incubation', icon: '🍼', color: '#86efac', desc: 'Nourrie par régurgitation' },
    { name: 'Œuf Colonial', icon: '🥚', color: '#fef08a', desc: 'Soin constant en chambre royale' },
    { name: 'Araignée / Prédateur', icon: '🕷️', color: '#e11d48', desc: 'Chasse les fourmis isolées' }
]

export default function SimulationSidebarTabs({
    activeSidebarTab,
    isDark,
    t,
    styles,
    slicePlaneRatio,
    setSlicePlaneRatio,
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
    showScientificIsolinesTopo,
    toggleScientificIsolinesTopo,
    showScientificIsolinesMicroclimate,
    toggleScientificIsolinesMicroclimate,
    showScientificIsolinesPheromones,
    toggleScientificIsolinesPheromones,
    isUVVisionMode,
    toggleUVVisionMode,
    isMuted,
    setIsMuted,
    masterVolume,
    setMasterVolume,
    ambientEnabled,
    toggleAmbient,
    riverEnabled,
    toggleRiver,
    weatherEnabled,
    toggleWeatherAudio,
    insectEnabled,
    toggleInsect,
    substrates = DEFAULT_SUBSTRATES,
    castes = DEFAULT_CASTES
}) {
    return (
        <>
            {/* TAB 2: RENDER LAYERS & 3D MODES */}
            {activeSidebarTab === 'LAYERS' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                    {/* 1. Axial Slicing Plane Slider (Coupe Axiale Z) */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 10, fontWeight: 700 }}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 5, color: isDark ? '#38bdf8' : '#0284c7' }}>
                                <Scissors size={12} />
                                <span>{t('sliceScannerTitle', 'Coupe Axiale (Slice Scanner)')}</span>
                            </span>
                            <span style={{ color: '#00d4ff', fontSize: 10, fontWeight: 800 }}>{((slicePlaneRatio ?? 1.0) * 100).toFixed(0)} %</span>
                        </div>
                        <input
                            type="range"
                            min="0"
                            max="1"
                            step="0.01"
                            value={slicePlaneRatio ?? 1.0}
                            onChange={(e) => setSlicePlaneRatio(parseFloat(e.target.value))}
                            style={styles.sliderTrack}
                        />
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 8, color: isDark ? '#64748b' : '#94a3b8' }}>
                            <span>{t('depth0', 'Profondeur 0%')}</span>
                            <span>{t('surface100', 'Surface 100%')}</span>
                        </div>
                    </div>

                    {/* 2. Layer Visibility Checkboxes */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.sectionTitle}>
                            <span>{t('visibleLayersTitle', 'Calques Visibles')}</span>
                        </div>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Layers size={12} color="#ca8a04" />
                                <span>{t('layerTerrain', 'Sol & Relief')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showTerrain)} onChange={toggleTerrain} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Layers size={12} color="#f59e0b" />
                                <span>{t('layerSkirt', 'Jupe 3D Géologique')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(show3DSkirt)} onChange={toggle3DSkirt} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Trees size={12} color="#22c55e" />
                                <span>{t('layerVegetation', 'Végétation')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showVegetation)} onChange={toggleVegetation} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Shield size={12} color="#a855f7" />
                                <span>{t('layerChambers', 'Cavités & Nids')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showChambers)} onChange={toggleChambers} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Sparkles size={12} color="#38bdf8" />
                                <span>{t('layerPheromones', 'Phéromones & Traces')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showPheromones)} onChange={togglePheromones} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Zap size={12} color="#ef4444" />
                                <span>{t('layerAnts', 'Fourmis & Castes')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showAnts)} onChange={toggleAnts} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <CloudRain size={12} color="#60a5fa" />
                                <span>{t('layerWeather', 'Météo & Ciel')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showWeather)} onChange={toggleWeather} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Map size={12} color="#10b981" />
                                <span>{t('layerMinimap', 'Minimap 2D Radar')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showMinimap)} onChange={toggleMinimap} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <GridIcon size={12} color="#818cf8" />
                                <span>{t('layerGrid', 'Grille 3D')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showGrid)} onChange={toggleGrid} />
                        </label>
                    </div>

                    {/* 3. Scientific Isolines & UV Vision */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <div style={styles.sectionTitle}>
                            <span>{t('sciFiltersTitle', 'Filtres Scientifiques')}</span>
                        </div>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <GridIcon size={12} color="#eab308" />
                                <span>{t('isolinesTopo', 'Isolines Topographiques')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showScientificIsolinesTopo)} onChange={toggleScientificIsolinesTopo} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Thermometer size={12} color="#f97316" />
                                <span>{t('isolinesMicroclimate', 'Isolines Microclimat')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showScientificIsolinesMicroclimate)} onChange={toggleScientificIsolinesMicroclimate} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Sparkles size={12} color="#8b5cf6" />
                                <span>{t('isolinesPhero', 'Isolines Phéromonales')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(showScientificIsolinesPheromones)} onChange={toggleScientificIsolinesPheromones} />
                        </label>

                        <label style={styles.toggleRow}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Sun size={12} color="#ec4899" />
                                <span>{t('uvVision', 'Spectre Vision UV')}</span>
                            </span>
                            <input type="checkbox" checked={Boolean(isUVVisionMode)} onChange={toggleUVVisionMode} />
                        </label>
                    </div>
                </div>
            )}

            {/* TAB 3: PROCEDURAL AUDIO CONTROLS (1:1 with Desktop Client) */}
            {activeSidebarTab === 'AUDIO' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                    <div style={styles.sectionTitle}>
                        <span>{t('sidebarAudioTitle', 'Sons :')}</span>
                        <button
                            onClick={() => {
                                const m = soundEngine.toggleMute()
                                setIsMuted(m)
                            }}
                            style={{ background: 'transparent', border: 'none', color: isMuted ? '#ef4444' : '#a78bfa', cursor: 'pointer', padding: 2 }}
                            title={isMuted ? t('unmute', 'Réactiver le son') : t('mute', 'Couper le son')}
                        >
                            {isMuted ? <VolumeX size={14} /> : <Volume2 size={14} />}
                        </button>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 10, fontWeight: 700 }}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: 4, color: isDark ? '#a78bfa' : '#7c3aed' }}>
                                <Volume2 size={12} /> {t('sidebarAudioVolume', 'Volume :')}
                            </span>
                            <span style={{ color: isDark ? '#a78bfa' : '#7c3aed', fontWeight: 800 }}>{isMuted ? '0%' : `${Math.round(masterVolume * 100)}%`}</span>
                        </div>
                        <input
                            type="range" min="0" max="1" step="0.01"
                            value={isMuted ? 0 : masterVolume}
                            onChange={(e) => {
                                const v = parseFloat(e.target.value)
                                setMasterVolume(v)
                                if (isMuted && v > 0) setIsMuted(false)
                            }}
                            style={styles.sliderTrack}
                        />
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <label
                            style={styles.toggleRow}
                            title={t('sidebarAudioAmbientTt', 'Activer le paysage sonore d\'ambiance naturelle (faune, oiseaux, forêt).')}
                        >
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Bird size={13} color="#38bdf8" />
                                <span>{t('sidebarAudioAmbient', 'Ambiance Biome (Oiseaux/Faune)')}</span>
                            </span>
                            <input
                                type="checkbox"
                                checked={Boolean(ambientEnabled)}
                                onChange={toggleAmbient}
                            />
                        </label>

                        <label
                            style={styles.toggleRow}
                            title={t('sidebarAudioRiverTt', 'Activer le son du courant d’eau et clapotis de rivière.')}
                        >
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Waves size={13} color="#06b6d4" />
                                <span>{t('sidebarAudioRiver', 'Bruit Eau Rivière')}</span>
                            </span>
                            <input
                                type="checkbox"
                                checked={Boolean(riverEnabled)}
                                onChange={toggleRiver}
                            />
                        </label>

                        <label
                            style={styles.toggleRow}
                            title={t('sidebarAudioWeatherTt', 'Activer les effets sonores météorologiques synchrone.')}
                        >
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <CloudRain size={13} color="#60a5fa" />
                                <span>{t('sidebarAudioWeather', 'Sons Météo (Pluie/Grêle/Orage)')}</span>
                            </span>
                            <input
                                type="checkbox"
                                checked={Boolean(weatherEnabled)}
                                onChange={toggleWeatherAudio}
                            />
                        </label>

                        <label
                            style={styles.toggleRow}
                            title={t('sidebarAudioInsectTt', 'Activer les sons procéduraux d\'activité des insectes et d\'excavation.')}
                        >
                            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                <Bug size={13} color="#f59e0b" />
                                <span>{t('sidebarAudioInsect', 'Activité Insectes & Nid (Creusement)')}</span>
                            </span>
                            <input
                                type="checkbox"
                                checked={Boolean(insectEnabled)}
                                onChange={toggleInsect}
                            />
                        </label>
                    </div>
                </div>
            )}

            {/* TAB 4: LEGENDS & SUBSTRATES */}
            {activeSidebarTab === 'LEGENDS' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                    <div style={styles.sectionTitle}>
                        <span>{t('legendsFullTitle', 'LÉGENDE COMPLÈTE 1:1 JAVAFX')}</span>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                        <span style={{ fontSize: 10, fontWeight: 800, color: '#38bdf8' }}>{t('nestChambersLegendTitle', 'Structure des Nids & Chambres :')}</span>
                        {[
                            { name: t('chamberRoyal', 'Chambre Royale (Reine)'), icon: '👑', color: '#a855f7', desc: t('descChamberRoyal', 'Ponte & phéromone de fécondité') },
                            { name: t('chamberBrood', 'Couvain & Pouponnière'), icon: '🍼', color: '#38bdf8', desc: t('descChamberBrood', 'Soins aux larves et œufs') },
                            { name: t('chamberFood', 'Grenier à Graines / Aliments'), icon: '🌾', color: '#f59e0b', desc: t('descChamberFood', 'Stockage des ressources nutritives') },
                            { name: t('chamberFungus', 'Champignonnière (Symbiote)'), icon: '🍄', color: '#22c55e', desc: t('descChamberFungus', 'Culture fongique (Atta/Acromyrmex)') },
                            { name: t('chamberWaste', 'Dépotoir / Décharge'), icon: '🗑️', color: '#78350f', desc: t('descChamberWaste', 'Évacuation des déchets & cadavres') },
                            { name: t('chamberRest', 'Dortoir & Repos'), icon: '💤', color: '#6366f1', desc: t('descChamberRest', 'Repos des ouvrières et alates') },
                            { name: t('chamberTunnels', 'Tunnels & Galeries'), icon: '🕳️', color: '#94a3b8', desc: t('descChamberTunnels', 'Réseau de circulation souterrain') }
                        ].map(nc => (
                            <div key={nc.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                <span>{nc.icon}</span>
                                <strong style={{ minWidth: 120, color: nc.color }}>{nc.name}</strong>
                                <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{nc.desc}</span>
                            </div>
                        ))}
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 3, marginTop: 4 }}>
                        <span style={{ fontSize: 10, fontWeight: 800, color: '#f59e0b' }}>{t('geologicalSubstratesTitle', '12 Couches Géologiques :')}</span>
                        {substrates.map(s => (
                            <div key={s.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                <div style={{ width: 10, height: 10, borderRadius: 2, background: s.color, border: '1px solid rgba(255,255,255,0.2)' }} />
                                <strong style={{ minWidth: 120 }}>{s.name}</strong>
                                <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{s.desc}</span>
                            </div>
                        ))}
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 3, marginTop: 4 }}>
                        <span style={{ fontSize: 10, fontWeight: 800, color: '#a855f7' }}>{t('castesTitle', 'Castes & Individus :')}</span>
                        {castes.map(c => (
                            <div key={c.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                <span>{c.icon}</span>
                                <strong style={{ minWidth: 120, color: c.color }}>{c.name}</strong>
                                <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{c.desc}</span>
                            </div>
                        ))}
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 3, marginTop: 4 }}>
                        <span style={{ fontSize: 10, fontWeight: 800, color: '#ec4899' }}>{t('pheromonesLegendTitle', '8 Phéromones Chimiques (1:1 JavaFX) :')}</span>
                        {[
                            { name: t('pheroFood', '1. Food (Nourriture)'), color: '#4caf50', desc: 'Vert (#4caf50) : Piste alimentaire & miellat' },
                            { name: t('pheroHome', '2. Home (Retour Nid)'), color: '#2196f3', desc: 'Bleu (#2196f3) : Orientation vers entrées' },
                            { name: t('pheroAlarm', '3. Alarm (Alerte Danger)'), color: '#f44336', desc: 'Rouge (#f44336) : Attaque & défense' },
                            { name: t('pheroRecruitment', '4. Trail (Recrutement)'), color: '#ffc107', desc: 'Jaune (#ffc107) : Amplification flux de masse' },
                            { name: t('pheroQueen', '5. Queen (Phéromone Royale)'), color: '#9c27b0', desc: 'Violet (#9c27b0) : Cohésion & fertilité' },
                            { name: t('pheroBrood', '6. Brood (Couvain)'), color: '#ff9800', desc: 'Orange (#ff9800) : Soins larves & œufs' },
                            { name: t('pheroDeath', '7. Death (Nécrophorèse)'), color: '#607d8b', desc: 'Gris (#607d8b) : Transport vers dépotoir' },
                            { name: t('pheroTerritory', '8. Territory (Marquage)'), color: '#009688', desc: 'Sarcelle (#009688) : Limite territoriale' }
                        ].map(p => (
                            <div key={p.name} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10, padding: '2px 4px', borderRadius: 4, background: isDark ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.02)' }}>
                                <div style={{ width: 10, height: 10, borderRadius: '50%', background: p.color, border: '1px solid rgba(255,255,255,0.3)', flexShrink: 0 }} />
                                <strong style={{ minWidth: 120, color: p.color }}>{p.name}</strong>
                                <span style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{p.desc}</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </>
    )
}
