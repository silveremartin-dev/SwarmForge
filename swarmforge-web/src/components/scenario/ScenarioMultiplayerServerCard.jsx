import React from 'react'
import { Server, Zap } from 'lucide-react'
import { useSimulationStore } from '../../store/simulationStore'
import { DEFAULT_SPECIES_PRESETS } from '../../store/presetStore'
import { getTranslation } from '../../i18n/translations'

/**
 * Remote SwarmForge server gRPC matchmaking, connection settings, and lobby status card.
 */
export default function ScenarioMultiplayerServerCard({ isDark, cardBg, borderCol, inputBg, textMain, textMuted }) {
    const {
        serverRole,
        setServerRole,
        serverHost,
        setServerHost,
        serverPort,
        setServerPort,
        connected,
        serverStatusText,
        connect,
        disconnect,
        discover,
        playerAlias,
        setPlayerAlias,
        playerSpecies,
        setPlayerSpecies,
        serverScenarios,
        lobbyStatus,
        lobbyPlayers,
        selectedServerScenarioId,
        isPlayerReady,
        togglePlayerReady,
        startServerMatch,
        selectServerScenario,
        language
    } = useSimulationStore()

    const t = (key, fallback) => getTranslation(language, key, fallback)
    const isJoin = serverRole === 'JOIN'

    return (
        <div style={{
            background: cardBg,
            border: `1px solid ${borderCol}`,
            borderRadius: 8,
            padding: '14px 16px',
            display: 'flex',
            flexDirection: 'column',
            gap: 12
        }}>
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                gap: 12,
                padding: '14px 16px',
                borderRadius: 8,
                background: isDark ? '#111827' : '#f0f9ff',
                border: '1px solid rgba(2, 132, 199, 0.35)'
            }}>
                {/* Row 1: Host, Port, Connect/Disconnect, Discover */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('hostLabel', 'Hôte :')}</span>
                        <input
                            type="text"
                            value={serverHost}
                            onChange={(e) => setServerHost(e.target.value)}
                            disabled={connected}
                            placeholder="127.0.0.1"
                            style={{
                                width: 110,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 5,
                                padding: '4px 8px',
                                fontSize: 11,
                                fontWeight: 600
                            }}
                        />
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('portLabel', 'Port :')}</span>
                        <input
                            type="number"
                            value={serverPort}
                            onChange={(e) => setServerPort(parseInt(e.target.value))}
                            disabled={connected}
                            style={{
                                width: 65,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 5,
                                padding: '4px 8px',
                                fontSize: 11,
                                fontWeight: 600
                            }}
                        />
                    </div>

                    <button
                        onClick={connected ? disconnect : connect}
                        style={{
                            background: connected ? '#ef4444' : '#0284c7',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: 5,
                            padding: '6px 14px',
                            fontSize: 11,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        {connected ? t('btnDisconnect', 'Déconnexion') : t('btnConnect', 'Connexion')}
                    </button>

                    <button
                        onClick={discover}
                        style={{
                            background: isDark ? '#334155' : '#e2e8f0',
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 5,
                            padding: '6px 12px',
                            fontSize: 11,
                            fontWeight: 600,
                            cursor: 'pointer'
                        }}
                    >
                        {t('btnDiscover', '🔍 Découvrir')}
                    </button>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginLeft: 'auto' }}>
                        <div style={{
                            width: 8,
                            height: 8,
                            borderRadius: '50%',
                            background: connected ? '#10b981' : '#f59e0b',
                            boxShadow: connected ? '0 0 8px #10b981' : 'none'
                        }} />
                        <span style={{ fontSize: 11, fontWeight: 700, color: connected ? '#10b981' : '#f59e0b' }}>
                            {serverStatusText || (connected ? t('connectedLabel', 'Connecté') : t('offlineLabel', 'Hors-ligne'))}
                        </span>
                    </div>
                </div>

                {/* Row 2: Role Selector (Host vs Join) */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                    <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('roleLabel', 'Rôle :')}</span>
                    <div style={{ display: 'flex', borderRadius: 6, overflow: 'hidden', border: `1px solid ${borderCol}` }}>
                        <button
                            onClick={() => setServerRole('HOST')}
                            style={{
                                padding: '5px 14px',
                                fontSize: 11,
                                fontWeight: 700,
                                border: 'none',
                                background: serverRole === 'HOST' ? '#0284c7' : inputBg,
                                color: serverRole === 'HOST' ? '#ffffff' : textMuted,
                                cursor: 'pointer'
                            }}
                        >
                            {t('roleHostBtn', '👑 Héberger la Partie (Host)')}
                        </button>
                        <button
                            onClick={() => setServerRole('JOIN')}
                            style={{
                                padding: '5px 14px',
                                fontSize: 11,
                                fontWeight: 700,
                                border: 'none',
                                background: serverRole === 'JOIN' ? '#0284c7' : inputBg,
                                color: serverRole === 'JOIN' ? '#ffffff' : textMuted,
                                cursor: 'pointer'
                            }}
                        >
                            {t('roleJoinBtn', '⚔️ Rejoindre une Colonie (Join)')}
                        </button>
                    </div>
                </div>

                {/* Row 3: Participant Alias & Assigned Species */}
                {isJoin && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1 }}>
                            <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('playerAliasLabel', 'Pseudo Chercheur :')}</span>
                            <input
                                type="text"
                                value={playerAlias}
                                onChange={(e) => setPlayerAlias(e.target.value)}
                                placeholder="Player"
                                style={{
                                    flex: 1,
                                    background: inputBg,
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 5,
                                    padding: '5px 8px',
                                    fontSize: 12,
                                    fontWeight: 600
                                }}
                            />
                        </div>

                        <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1 }}>
                            <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }}>{t('speciesLabel', 'Espèce Affectée :')}</span>
                            <select
                                value={playerSpecies}
                                onChange={(e) => setPlayerSpecies(e.target.value)}
                                style={{
                                    flex: 1,
                                    background: inputBg,
                                    color: textMain,
                                    border: `1px solid ${borderCol}`,
                                    borderRadius: 5,
                                    padding: '5px 8px',
                                    fontSize: 12,
                                    fontWeight: 600
                                }}
                            >
                                {DEFAULT_SPECIES_PRESETS.map(s => (
                                    <option key={s.id} value={s.name}>{s.name} ({s.latinName})</option>
                                ))}
                            </select>
                        </div>
                    </div>
                )}

                {/* Row 4: Mode Explainer Card */}
                {isJoin ? (
                    <div style={{
                        padding: '10px 14px',
                        borderRadius: 6,
                        background: isDark ? 'rgba(234, 179, 8, 0.08)' : '#fefce8',
                        border: '1px solid #ca8a04',
                        fontSize: 11,
                        color: isDark ? '#fef08a' : '#854d0e',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 4
                    }}>
                        <div style={{ fontWeight: 800, color: '#eab308' }}>
                            {t('joinModeExplainerTitle', '▶ Mode Rejoindre — Matchmaking & Mégaterrarium (Autorité Serveur)')}
                        </div>
                        <div>{t('joinModeExplainer1', '• Monde & Climat : Régis par l\'hôte serveur distant (paramètres locaux désactivés).')}</div>
                        <div>{t('joinModeExplainer2', '• Votre Colonie : Le serveur assigne une colonie équilibrée et l\'emplacement du nid.')}</div>
                        <div>{t('joinModeExplainer3', '• Espèce & Tag : Sélectionnez l\'espèce que vous apportez depuis votre bibliothèque (champ ci-dessus).')}</div>
                    </div>
                ) : (
                    <div style={{
                        padding: '10px 14px',
                        borderRadius: 6,
                        background: isDark ? 'rgba(16, 185, 129, 0.08)' : '#ecfdf5',
                        border: '1px solid #059669',
                        fontSize: 11,
                        color: isDark ? '#a7f3d0' : '#065f46',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 4
                    }}>
                        <div style={{ fontWeight: 800, color: '#10b981' }}>
                            {t('hostModeExplainerTitle', '▶ Mode Hébergement — Contrôle Total du Chercheur (Autorité Client)')}
                        </div>
                        <div>{t('hostModeExplainer1', '• Scénario Maître : Tous vos paramètres ci-dessous (Monde, Climat, Espèces, Nids, Démographie) sont déployés sur le serveur.')}</div>
                        <div>{t('hostModeExplainer2', '• Rôle : Vous définissez l\'écosystème complet. D\'autres participants peuvent rejoindre votre simulation et piloter des colonies.')}</div>
                    </div>
                )}

                {/* Row 5: Matchmaking Lobby & Scenario Status Banner */}
                {connected && (
                    <div style={{
                        marginTop: 8,
                        padding: '12px 14px',
                        borderRadius: 8,
                        background: isDark ? '#0f172a' : '#f1f5f9',
                        border: `1px solid ${isDark ? '#334155' : '#cbd5e1'}`,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 10
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <Server size={16} color="#38bdf8" />
                                <span style={{ fontSize: 12, fontWeight: 800, color: textMain }}>
                                    {t('serverScenariosTitle', 'Catalogue des Scénarios Serveur & Matchmaking')}
                                </span>
                            </div>
                            <span style={{
                                fontSize: 10,
                                fontWeight: 800,
                                padding: '3px 8px',
                                borderRadius: 4,
                                background: lobbyStatus === 'ACTIVE' ? '#059669' : (lobbyStatus === 'LOBBY_WAITING' ? '#d97706' : '#64748b'),
                                color: '#ffffff',
                                letterSpacing: '0.5px'
                            }}>
                                {lobbyStatus === 'ACTIVE' 
                                    ? t('lobbyStatusActive', '🟢 PARTIE EN COURS (ACTIVE)') 
                                    : (lobbyStatus === 'LOBBY_WAITING' 
                                        ? t('lobbyStatusWaiting', '⏳ EN ATTENTE DE DÉMARRAGE (LOBBY)') 
                                        : t('lobbyStatusInactive', '⚪ INACTIF'))}
                            </span>
                        </div>

                        {/* Lobby Players List */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                            <div style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>
                                {t('lobbyPlayersListTitle', 'Participants Connectés au Lobby :')} ({lobbyPlayers.length})
                            </div>
                            {lobbyPlayers.length === 0 ? (
                                <div style={{ fontSize: 11, color: textMuted, fontStyle: 'italic' }}>
                                    {t('noLobbyPlayers', 'En attente de participants...')}
                                </div>
                            ) : (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                    {lobbyPlayers.map((p, idx) => (
                                        <div
                                            key={p.id || idx}
                                            style={{
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'space-between',
                                                padding: '4px 8px',
                                                borderRadius: 4,
                                                background: isDark ? '#1e293b' : '#ffffff',
                                                border: `1px solid ${borderCol}`,
                                                fontSize: 11
                                            }}
                                        >
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                                <span style={{ fontWeight: 700, color: textMain }}>{p.alias || 'Chercheur'}</span>
                                                <span style={{ color: textMuted, fontSize: 10 }}>({p.species || 'Fourmi'})</span>
                                            </div>
                                            <span style={{
                                                fontSize: 10,
                                                fontWeight: 700,
                                                color: p.isReady ? '#10b981' : '#f59e0b'
                                            }}>
                                                {p.isReady ? '✓ Prêt' : '○ En attente'}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Lobby Action Bar */}
                        <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                            {isJoin ? (
                                <button
                                    onClick={togglePlayerReady}
                                    style={{
                                        flex: 1,
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        gap: 6,
                                        background: isPlayerReady ? '#059669' : '#d97706',
                                        color: '#ffffff',
                                        border: 'none',
                                        borderRadius: 6,
                                        padding: '8px 14px',
                                        fontSize: 12,
                                        fontWeight: 800,
                                        cursor: 'pointer'
                                    }}
                                >
                                    {isPlayerReady ? t('btnToggleReady', '✓ Je suis Prêt') : t('btnToggleNotReady', '✕ Pas Prêt')}
                                </button>
                            ) : (
                                <button
                                    onClick={startServerMatch}
                                    disabled={lobbyStatus === 'ACTIVE'}
                                    style={{
                                        flex: 1,
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        gap: 6,
                                        background: lobbyStatus === 'ACTIVE' ? '#475569' : '#059669',
                                        color: '#ffffff',
                                        border: 'none',
                                        borderRadius: 6,
                                        padding: '8px 14px',
                                        fontSize: 12,
                                        fontWeight: 800,
                                        cursor: lobbyStatus === 'ACTIVE' ? 'not-allowed' : 'pointer',
                                        opacity: lobbyStatus === 'ACTIVE' ? 0.6 : 1.0
                                    }}
                                >
                                    <Zap size={14} />
                                    {t('btnStartServerMatch', '🚀 Lancer la Partie (Hôte)')}
                                </button>
                            )}
                        </div>

                        {/* Server Known Scenarios List */}
                        {serverScenarios && serverScenarios.length > 0 && (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 4 }}>
                                <div style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>
                                    {t('serverScenariosTitle', 'Scénarios Disponibles sur le Serveur :')}
                                </div>
                                <div style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: 4,
                                    maxHeight: 180,
                                    overflowY: 'auto',
                                    paddingRight: 4
                                }}>
                                    {serverScenarios.map(sc => {
                                        const isSelected = sc.id === selectedServerScenarioId
                                        return (
                                            <div
                                                key={sc.id}
                                                style={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    justifyContent: 'space-between',
                                                    padding: '6px 8px',
                                                    borderRadius: 5,
                                                    background: isSelected 
                                                        ? (isDark ? 'rgba(56, 189, 248, 0.15)' : '#e0f2fe')
                                                        : (isDark ? '#1e293b' : '#ffffff'),
                                                    border: `1px solid ${isSelected ? '#38bdf8' : borderCol}`,
                                                    fontSize: 11
                                                }}
                                            >
                                                <div style={{ display: 'flex', flexDirection: 'column', gap: 2, flex: 1, minWidth: 0, paddingRight: 8 }}>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                                        <span style={{ fontWeight: 800, color: textMain, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                            {sc.title}
                                                        </span>
                                                        <span style={{
                                                            fontSize: 9,
                                                            padding: '1px 5px',
                                                            borderRadius: 3,
                                                            background: isDark ? '#334155' : '#e2e8f0',
                                                            color: textMuted,
                                                            fontWeight: 600
                                                        }}>
                                                            {sc.requiredPlayerCount || 1}P {sc.isMultiplayerOnly ? 'Multijoueur' : 'Solo/Multi'}
                                                        </span>
                                                    </div>
                                                    <span style={{ fontSize: 10, color: textMuted, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                                        {sc.academicCategory} • Biome : {sc.biomeName}
                                                    </span>
                                                </div>

                                                {!isJoin && lobbyStatus !== 'ACTIVE' && (
                                                    <button
                                                        onClick={() => selectServerScenario(sc.id)}
                                                        disabled={isSelected}
                                                        style={{
                                                            background: isSelected ? '#0284c7' : (isDark ? '#334155' : '#cbd5e1'),
                                                            color: isSelected ? '#ffffff' : textMain,
                                                            border: 'none',
                                                            borderRadius: 4,
                                                            padding: '4px 8px',
                                                            fontSize: 10,
                                                            fontWeight: 700,
                                                            cursor: isSelected ? 'default' : 'pointer'
                                                        }}
                                                    >
                                                        {isSelected ? '✓ Actif' : t('btnSelectScenario', 'Sélectionner')}
                                                    </button>
                                                )}
                                            </div>
                                        )
                                    })}
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    )
}
