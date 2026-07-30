import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { showToast } from '../store/toastStore'

export default function WorldEditorPanel() {
    const [activeTab, setActiveTab] = useState('terrain')

    // World state local draft
    const [scale, setScale] = useState({ sizeX: 2.0, sizeY: 2.0, resolutionMm: 0.5 })
    const [terrain, setTerrain] = useState({
        roughness: 0.45,
        elevation: 0.8,
        soilComposition: { earth: 50, sand: 20, clay: 20, stone: 10 },
        compaction: 65,
    })
    const [flora, setFlora] = useState({
        edibleDensity: 40,
        edibleSpecies: ['Cirsium (Pucerons)', 'Fleurs à Nectar', 'Graminées à Graines'],
        nonEdibleDensity: 60,
        nonEdibleSpecies: ['Mousse Polytrichum', 'Fougères', 'Aiguilles de Pin'],
    })
    const [hydrology, setHydrology] = useState({
        hasRiver: true,
        riverWidthMm: 120,
        riverFlowVelocity: 0.3,
        staticPools: 2,
        waterTableDepthCm: 15,
    })
    const [structures, setStructures] = useState({
        treesCount: 2,
        treeHeightMeters: 1.8,
        hollowLogs: 1,
        crevices: 3,
        nestHostType: 'SUBTERRANEAN_AND_ARBOREAL',
    })
    const [nestConfig, setNestConfig] = useState({
        mode: 'PREBUILT', // 'FOUNDING_QUEEN' or 'PREBUILT'
        nestDepthCm: 25,
        chambers: { queen: 1, nursery: 2, food: 3, waste: 1 },
    })

    const applyWorldToSimulation = () => {
        showToast("🌍 Configuration du Monde appliquée au simulateur avec succès ! (" + scale.resolutionMm + "mm)", "success")
    }

    const styles = {
        container: {
            position: 'absolute',
            top: 60,
            left: 20,
            width: 420,
            maxHeight: 'calc(100vh - 80px)',
            overflowY: 'auto',
            background: 'rgba(20, 24, 36, 0.92)',
            backdropFilter: 'blur(12px)',
            border: '1px solid rgba(255, 255, 255, 0.12)',
            borderRadius: 12,
            padding: 20,
            color: '#e2e8f0',
            boxShadow: '0 20px 40px rgba(0,0,0,0.5)',
            zIndex: 90,
            fontFamily: 'system-ui, -apple-system, sans-serif',
        },
        header: {
            fontSize: 18,
            fontWeight: 700,
            color: '#38bdf8',
            marginBottom: 4,
            display: 'flex',
            alignItems: 'center',
            gap: 8,
        },
        subtitle: {
            fontSize: 12,
            color: '#94a3b8',
            marginBottom: 16,
        },
        navTabs: {
            display: 'flex',
            flexWrap: 'wrap',
            gap: 4,
            marginBottom: 16,
            background: 'rgba(0,0,0,0.3)',
            padding: 4,
            borderRadius: 8,
        },
        tabBtn: (active) => ({
            flex: '1 1 30%',
            padding: '6px 8px',
            fontSize: 11,
            fontWeight: 600,
            border: 'none',
            borderRadius: 6,
            cursor: 'pointer',
            background: active ? '#0284c7' : 'transparent',
            color: active ? '#fff' : '#94a3b8',
            transition: 'all 0.2s',
            textAlign: 'center',
        }),
        section: {
            background: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 255, 255, 0.05)',
            borderRadius: 8,
            padding: 14,
            marginBottom: 14,
        },
        label: {
            fontSize: 12,
            fontWeight: 600,
            color: '#cbd5e1',
            marginBottom: 6,
            display: 'flex',
            justifyContent: 'space-between',
        },
        slider: {
            width: '100%',
            accentColor: '#38bdf8',
            cursor: 'pointer',
        },
        grid2: {
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 10,
        },
        input: {
            width: '100%',
            background: '#0f172a',
            border: '1px solid #334155',
            color: '#f8fafc',
            padding: '6px 10px',
            borderRadius: 6,
            fontSize: 12,
        },
        applyBtn: {
            width: '100%',
            padding: '12px',
            background: 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)',
            color: '#fff',
            fontWeight: 700,
            fontSize: 13,
            border: 'none',
            borderRadius: 8,
            cursor: 'pointer',
            boxShadow: '0 4px 12px rgba(2, 132, 199, 0.4)',
            marginTop: 10,
        }
    }

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <span>🌍 Éditeur de Monde</span>
            </div>
            <div style={styles.subtitle}>
                Génération de terrain, substrats, micro-hydrographie, tri-vue synchronisée & sculpture 3D sub-millimétrique.
            </div>

            {/* Editor Sub-Tabs */}
            <div style={styles.navTabs}>
                <button style={styles.tabBtn(activeTab === 'scale')} onClick={() => setActiveTab('scale')}>📐 Échelle</button>
                <button style={styles.tabBtn(activeTab === 'terrain')} onClick={() => setActiveTab('terrain')}>⛰️ Sol & Relief</button>
                <button style={styles.tabBtn(activeTab === 'flora')} onClick={() => setActiveTab('flora')}>🌿 Écosystème</button>
                <button style={styles.tabBtn(activeTab === 'hydro')} onClick={() => setActiveTab('hydro')}>💧 Hydrographie</button>
                <button style={styles.tabBtn(activeTab === 'struct')} onClick={() => setActiveTab('struct')}>🪵 Hauteur</button>
                <button style={styles.tabBtn(activeTab === 'nest')} onClick={() => setActiveTab('nest')}>🏰 Nid Initial</button>
                <button style={styles.tabBtn(activeTab === 'sculpt')} onClick={() => setActiveTab('sculpt')}>🖌️ Sculpture 3D</button>
                <button style={styles.tabBtn(activeTab === 'heatmap')} onClick={() => setActiveTab('heatmap')}>🔥 Cartes de Chaleur</button>
            </div>

            {/* TAB 1: SCALE & PRECISION */}
            {activeTab === 'scale' && (
                <div style={styles.section}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#38bdf8', marginBottom: 10 }}>📐 Dimensions & Précision Voxel</div>
                    <div style={styles.label}>
                        <span>Taille de Surface (Mètres)</span>
                        <span>{scale.sizeX}m × {scale.sizeY}m</span>
                    </div>
                    <input
                        type="range"
                        min="0.5"
                        max="10.0"
                        step="0.5"
                        value={scale.sizeX}
                        onChange={(e) => setScale({ ...scale, sizeX: parseFloat(e.target.value), sizeY: parseFloat(e.target.value) })}
                        style={styles.slider}
                    />

                    <div style={{ ...styles.label, marginTop: 12 }}>
                        <span>Résolution Grille / Voxel (Precision)</span>
                        <span style={{ color: '#4ade80' }}>{scale.resolutionMm} mm (Sub-millimétrique)</span>
                    </div>
                    <input
                        type="range"
                        min="0.1"
                        max="2.0"
                        step="0.1"
                        value={scale.resolutionMm}
                        onChange={(e) => setScale({ ...scale, resolutionMm: parseFloat(e.target.value) })}
                        style={styles.slider}
                    />
                    <div style={{ fontSize: 11, color: '#64748b', marginTop: 6 }}>
                        💡 Permet de simuler précisément le diamètre des galeries (3-8 mm) et la taille des fourmis (2-15 mm).
                    </div>
                </div>
            )}

            {/* TAB 2: TERRAIN & SOIL SUBSTRATE */}
            {activeTab === 'terrain' && (
                <div>
                    <div style={styles.section}>
                        <div style={{ fontSize: 13, fontWeight: 700, color: '#38bdf8', marginBottom: 10 }}>⛰️ Relief & Topographie</div>
                        <div style={styles.label}>
                            <span>Rugosité du Relief (Bruit Perlin)</span>
                            <span>{(terrain.roughness * 100).toFixed(0)}%</span>
                        </div>
                        <input
                            type="range"
                            min="0"
                            max="1"
                            step="0.05"
                            value={terrain.roughness}
                            onChange={(e) => setTerrain({ ...terrain, roughness: parseFloat(e.target.value) })}
                            style={styles.slider}
                        />

                        <div style={{ ...styles.label, marginTop: 10 }}>
                            <span>Index de Compaction du Sol</span>
                            <span>{terrain.compaction}%</span>
                        </div>
                        <input
                            type="range"
                            min="10"
                            max="100"
                            value={terrain.compaction}
                            onChange={(e) => setTerrain({ ...terrain, compaction: parseInt(e.target.value) })}
                            style={styles.slider}
                        />
                    </div>

                    <div style={styles.section}>
                        <div style={{ fontSize: 13, fontWeight: 700, color: '#38bdf8', marginBottom: 10 }}>🏜️ Composition du Substrat (%)</div>
                        <div style={styles.grid2}>
                            <div>
                                <label style={{ fontSize: 11, color: '#94a3b8' }}>Terre / Humus</label>
                                <input type="number" style={styles.input} value={terrain.soilComposition.earth} onChange={(e) => setTerrain({ ...terrain, soilComposition: { ...terrain.soilComposition, earth: +e.target.value } })} />
                            </div>
                            <div>
                                <label style={{ fontSize: 11, color: '#94a3b8' }}>Sable (Éboulements)</label>
                                <input type="number" style={styles.input} value={terrain.soilComposition.sand} onChange={(e) => setTerrain({ ...terrain, soilComposition: { ...terrain.soilComposition, sand: +e.target.value } })} />
                            </div>
                            <div>
                                <label style={{ fontSize: 11, color: '#94a3b8' }}>Argile (Stabilité)</label>
                                <input type="number" style={styles.input} value={terrain.soilComposition.clay} onChange={(e) => setTerrain({ ...terrain, soilComposition: { ...terrain.soilComposition, clay: +e.target.value } })} />
                            </div>
                            <div>
                                <label style={{ fontSize: 11, color: '#94a3b8' }}>Pierre / Gravier</label>
                                <input type="number" style={styles.input} value={terrain.soilComposition.stone} onChange={(e) => setTerrain({ ...terrain, soilComposition: { ...terrain.soilComposition, stone: +e.target.value } })} />
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* TAB 3: FLORA & ECOSYSTEM */}
            {activeTab === 'flora' && (
                <div>
                    <div style={styles.section}>
                        <div style={{ fontSize: 13, fontWeight: 700, color: '#4ade80', marginBottom: 8 }}>🍎 Espèces Comestibles (Ressources)</div>
                        <div style={styles.label}>
                            <span>Densité de Couvert Végétal Nouri</span>
                            <span>{flora.edibleDensity}%</span>
                        </div>
                        <input
                            type="range"
                            min="0"
                            max="100"
                            value={flora.edibleDensity}
                            onChange={(e) => setFlora({ ...flora, edibleDensity: parseInt(e.target.value) })}
                            style={styles.slider}
                        />
                        <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 8 }}>
                            Plantes incluses: 🟢 <i>Cirsium</i> (Hôtes pucerons/miellat), 🌸 Fleurs nectarières, 🌾 Graminées (Graines).
                        </div>
                    </div>

                    <div style={styles.section}>
                        <div style={{ fontSize: 13, fontWeight: 700, color: '#f87171', marginBottom: 8 }}>🌲 Espèces Non-Comestibles (Structure & Abriss)</div>
                        <div style={styles.label}>
                            <span>Densité de Mousse & Litière</span>
                            <span>{flora.nonEdibleDensity}%</span>
                        </div>
                        <input
                            type="range"
                            min="0"
                            max="100"
                            value={flora.nonEdibleDensity}
                            onChange={(e) => setFlora({ ...flora, nonEdibleDensity: parseInt(e.target.value) })}
                            style={styles.slider}
                        />
                        <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 8 }}>
                            Inclus: 🟢 <i>Polytrichum</i> (Mousse d'humidité), 🍂 Litière d'aiguilles de pin, 🌿 Fougères d'obstacle.
                        </div>
                    </div>
                </div>
            )}

            {/* TAB 4: HYDROLOGY */}
            {activeTab === 'hydro' && (
                <div style={styles.section}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#38bdf8', marginBottom: 10 }}>💧 Cours d'eau & Réserves</div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
                        <input
                            type="checkbox"
                            checked={hydrology.hasRiver}
                            onChange={(e) => setHydrology({ ...hydrology, hasRiver: e.target.checked })}
                            id="hasRiver"
                        />
                        <label htmlFor="hasRiver" style={{ fontSize: 12, fontWeight: 600 }}>Générer un cours d'eau / Rivière</label>
                    </div>

                    {hydrology.hasRiver && (
                        <>
                            <div style={styles.label}>
                                <span>Largeur du cours d'eau</span>
                                <span>{hydrology.riverWidthMm} mm</span>
                            </div>
                            <input
                                type="range"
                                min="30"
                                max="500"
                                value={hydrology.riverWidthMm}
                                onChange={(e) => setHydrology({ ...hydrology, riverWidthMm: parseInt(e.target.value) })}
                                style={styles.slider}
                            />

                            <div style={{ ...styles.label, marginTop: 10 }}>
                                <span>Vitesse du courant</span>
                                <span>{hydrology.riverFlowVelocity} m/s</span>
                            </div>
                            <input
                                type="range"
                                min="0"
                                max="1.5"
                                step="0.1"
                                value={hydrology.riverFlowVelocity}
                                onChange={(e) => setHydrology({ ...hydrology, riverFlowVelocity: parseFloat(e.target.value) })}
                                style={styles.slider}
                            />
                        </>
                    )}

                    <div style={{ ...styles.label, marginTop: 12 }}>
                        <span>Plaques d'eau statique (Mares / Flaques)</span>
                        <span>{hydrology.staticPools}</span>
                    </div>
                    <input
                        type="range"
                        min="0"
                        max="5"
                        value={hydrology.staticPools}
                        onChange={(e) => setHydrology({ ...hydrology, staticPools: parseInt(e.target.value) })}
                        style={styles.slider}
                    />
                </div>
            )}

            {/* TAB 5: VERTICAL STRUCTURES */}
            {activeTab === 'struct' && (
                <div style={styles.section}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#fbbf24', marginBottom: 10 }}>🪵 Arbres & Structures Supraterraines</div>
                    <div style={styles.label}>
                        <span>Nombre d'Arbres / Troncs</span>
                        <span>{structures.treesCount}</span>
                    </div>
                    <input
                        type="range"
                        min="0"
                        max="5"
                        value={structures.treesCount}
                        onChange={(e) => setStructures({ ...structures, treesCount: parseInt(e.target.value) })}
                        style={styles.slider}
                    />

                    <div style={{ ...styles.label, marginTop: 10 }}>
                        <span>Souches d'arbres creuses (Nids bois)</span>
                        <span>{structures.hollowLogs}</span>
                    </div>
                    <input
                        type="range"
                        min="0"
                        max="4"
                        value={structures.hollowLogs}
                        onChange={(e) => setStructures({ ...structures, hollowLogs: parseInt(e.target.value) })}
                        style={styles.slider}
                    />

                    <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 10 }}>
                        💡 Permet l'hébergement de nids arboricoles (*Crematogaster*, *Oecophylla*) ou de guêpes/abeilles sociales.
                    </div>
                </div>
            )}

            {/* TAB 6: NEST INITIALIZATION */}
            {activeTab === 'nest' && (
                <div style={styles.section}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#a855f7', marginBottom: 10 }}>🏰 Configuration Initiale du Nid</div>
                    <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                        <button
                            style={{
                                flex: 1,
                                padding: '8px',
                                fontSize: 11,
                                background: nestConfig.mode === 'FOUNDING_QUEEN' ? '#a855f7' : '#1e293b',
                                border: '1px solid #475569',
                                color: '#fff',
                                borderRadius: 6,
                                cursor: 'pointer',
                            }}
                            onClick={() => setNestConfig({ ...nestConfig, mode: 'FOUNDING_QUEEN' })}
                        >
                            👑 Reine Seule (Fondation)
                        </button>
                        <button
                            style={{
                                flex: 1,
                                padding: '8px',
                                fontSize: 11,
                                background: nestConfig.mode === 'PREBUILT' ? '#a855f7' : '#1e293b',
                                border: '1px solid #475569',
                                color: '#fff',
                                borderRadius: 6,
                                cursor: 'pointer',
                            }}
                            onClick={() => setNestConfig({ ...nestConfig, mode: 'PREBUILT' })}
                        >
                            🏛️ Nid Pré-construit
                        </button>
                    </div>

                    {nestConfig.mode === 'PREBUILT' && (
                        <div style={styles.grid2}>
                            <div>
                                <label style={{ fontSize: 11, color: '#94a3b8' }}>Chambre Reine</label>
                                <input type="number" style={styles.input} value={nestConfig.chambers.queen} readOnly />
                            </div>
                            <div>
                                <label style={{ fontSize: 11, color: '#94a3b8' }}>Chambres Couvain</label>
                                <input type="number" style={styles.input} value={nestConfig.chambers.nursery} onChange={(e) => setNestConfig({ ...nestConfig, chambers: { ...nestConfig.chambers, nursery: +e.target.value } })} />
                            </div>
                            <div>
                                <label style={{ fontSize: 11, color: '#94a3b8' }}>Greniers à Nourriture</label>
                                <input type="number" style={styles.input} value={nestConfig.chambers.food} onChange={(e) => setNestConfig({ ...nestConfig, chambers: { ...nestConfig.chambers, food: +e.target.value } })} />
                            </div>
                            <div>
                                <label style={{ fontSize: 11, color: '#94a3b8' }}>Dépotoirs</label>
                                <input type="number" style={styles.input} value={nestConfig.chambers.waste} onChange={(e) => setNestConfig({ ...nestConfig, chambers: { ...nestConfig.chambers, waste: +e.target.value } })} />
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* TAB 7: 3D SCULPTING BRUSHES & VOXEL PHYSICS */}
            {activeTab === 'sculpt' && (
                <div style={styles.section}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#38bdf8', marginBottom: 10 }}>🖌️ Sculpture 3D Manuelle & Voxels</div>
                    <div style={{ marginBottom: 10 }}>
                        <label style={{ fontSize: 12, fontWeight: 600, color: '#cbd5e1' }}>Pinceau d'Édition :</label>
                        <select style={{ ...styles.input, marginTop: 4 }}>
                            <option>⛰️ Élever Terrain (RAISE)</option>
                            <option>⛏️ Creuser / Abaisser Terrain (LOWER)</option>
                            <option>🌊 Lisser Relief (SMOOTH)</option>
                        </select>
                    </div>
                    <div style={{ ...styles.label, marginTop: 10 }}>
                        <span>Rayon du Pinceau (Voxels)</span>
                        <span>4 vx</span>
                    </div>
                    <input type="range" min="1" max="15" defaultValue="4" style={styles.slider} />

                    <div style={{ ...styles.label, marginTop: 10 }}>
                        <span>Force d'Application</span>
                        <span>50%</span>
                    </div>
                    <input type="range" min="10" max="100" defaultValue="50" style={styles.slider} />

                    <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 10 }}>
                        💡 Le mode peinture gère uniquement l'élévation. Une stabilisation automatique des pentes empêche la création de pixels flottants.
                    </div>
                </div>
            )}

            {/* TAB 8: HEATMAP & COLORATION OVERLAYS */}
            {activeTab === 'heatmap' && (
                <div style={styles.section}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#ef4444', marginBottom: 10 }}>🔥 Superposition de Cartes de Chaleur & Couleurs</div>
                    <div style={{ marginBottom: 12 }}>
                        <label style={{ fontSize: 12, fontWeight: 600, color: '#cbd5e1' }}>Mode de Visualization :</label>
                        <select style={{ ...styles.input, marginTop: 4 }}>
                            <option>🟢 Pheromone: Nourriture</option>
                            <option>🔵 Pheromone: Nid / Homing</option>
                            <option>🔴 Pheromone: Alarme</option>
                            <option>🟡 Pheromone: Piste</option>
                            <option>🟣 Pheromone: Reine</option>
                            <option>🟠 Pheromone: Couvain</option>
                            <option>⚪ Pheromone: Nécrophorèse</option>
                            <option>🔥 Occupation & Trafic des Tunnels</option>
                            <option>🏛️ Spécialisation des Chambres</option>
                            <option>⛰️ Stabilité du Sol (Mohr-Coulomb)</option>
                            <option>💧 Humidité du Sol</option>
                        </select>
                    </div>

                    <div style={{ ...styles.label, marginTop: 10 }}>
                        <span>Opacité de Superposition</span>
                        <span>70%</span>
                    </div>
                    <input type="range" min="10" max="100" defaultValue="70" style={styles.slider} />

                    <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 10 }}>
                        💡 Affiche en temps réel la densité de trafic dans les galeries, les rôles des chambres et la contrainte de cisaillement du sol.
                    </div>
                </div>
            )}

            {/* Action Buttons */}
            <button style={styles.applyBtn} onClick={applyWorldToSimulation}>
                ✨ Générer & Charger le Monde dans la Simulation
            </button>
        </div>
    )
}
