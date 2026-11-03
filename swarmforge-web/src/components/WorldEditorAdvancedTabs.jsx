import React from 'react'

/**
 * Advanced configuration tabs for WorldEditorPanel:
 * - Vertical structures (trees, hollow logs)
 * - Initial nest setup (founding queen vs prebuilt)
 * - 3D voxel sculpting brushes
 * - Topological heatmaps & soil mechanics overlays
 */
export function WorldEditorAdvancedTabs({
    activeTab,
    styles,
    structures,
    setStructures,
    nestConfig,
    setNestConfig
}) {
    return (
        <>
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
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#a855f7', marginBottom: 10 }}>🏛️ Configuration Initiale du Nid</div>
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
                        <label style={{ fontSize: 12, fontWeight: 600, color: '#cbd5e1' }}>Mode de Visualisation Topologique :</label>
                        <select style={{ ...styles.input, marginTop: 4 }}>
                            <option>🔥 Densité & Capacité des Tunnels</option>
                            <option>🏛️ Rôles & Spécialisation des Chambres</option>
                            <option>⛰️ Stabilité Mécanique du Sol (Mohr-Coulomb)</option>
                            <option>💧 Porosité & Teneur en Eau du Substrat</option>
                            <option>🪨 Gradient de Compacité Géologique</option>
                        </select>
                    </div>

                    <div style={{ ...styles.label, marginTop: 10 }}>
                        <span>Opacité de Superposition</span>
                        <span>70%</span>
                    </div>
                    <input type="range" min="10" max="100" defaultValue="70" style={styles.slider} />

                    <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 10 }}>
                        💡 Visualise la stabilité mécanique des cavités, la porosité du substrat et la contrainte de cisaillement du sol dans l'éditeur de monde. <em>(Les isolignes phéromonales et micro-climatiques dynamiques sont gérées en direct dans la vue Simulation 3D).</em>
                    </div>
                </div>
            )}
        </>
    )
}
