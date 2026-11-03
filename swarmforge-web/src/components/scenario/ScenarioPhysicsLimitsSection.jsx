import React from 'react'
import { Globe } from 'lucide-react'
import { useSimulationStore } from '../../store/simulationStore'
import { getTranslation } from '../../i18n/translations'

/**
 * Physics integration step, duration limits, and Megaterrarium sharded spatial topology card.
 */
export default function ScenarioPhysicsLimitsSection({ isDark, borderCol, inputBg, textMain, textMuted }) {
    const {
        stepSeconds,
        setStepSeconds,
        maxDuration,
        setMaxDuration,
        durationUnit,
        setDurationUnit,
        minPopStop,
        setMinPopStop,
        isMultiplayerOnly,
        setIsMultiplayerOnly,
        requiredPlayerCount,
        setRequiredPlayerCount,
        gridTilesX,
        setGridTilesX,
        gridTilesY,
        setGridTilesY,
        language
    } = useSimulationStore()

    const t = (key, fallback) => getTranslation(language, key, fallback)

    const getCalculatedDurationInfo = () => {
        if (durationUnit === '∞ Unlimited' || durationUnit === '∞ Illimité') {
            return `🔄 ${t('equivalentDurationLabel', 'Durée Équivalente :')} ∞`
        }
        let totalSeconds = 0
        const dur = Number(maxDuration) || 100
        switch (durationUnit) {
            case 'Seconds (s)': totalSeconds = dur; break;
            case 'Minutes (min)': totalSeconds = dur * 60; break;
            case 'Hours (h)': totalSeconds = dur * 3600; break;
            case 'Days (d)': case 'Days': totalSeconds = dur * 86400; break;
            case 'Months (30d)': totalSeconds = dur * 86400 * 30; break;
            case 'Years (365d)': totalSeconds = dur * 86400 * 365; break;
            case 'Ticks': totalSeconds = dur * (stepSeconds || 0.0166); break;
            default: totalSeconds = dur * 86400; break;
        }

        const days = Math.floor(totalSeconds / 86400)
        const remSec1 = totalSeconds % 86400
        const hours = Math.floor(remSec1 / 3600)
        const remSec2 = remSec1 % 3600
        const mins = Math.floor(remSec2 / 60)
        const secs = Math.floor(remSec2 % 60)
        const dt = stepSeconds || 0.0166
        const steps = Math.round(totalSeconds / dt)

        return `🔄 ${t('equivalentDurationLabel', 'Durée Équivalente :')} ${days}d ${hours}h ${mins}m ${secs}s (${steps.toLocaleString()} steps @ Δt = ${dt.toFixed(3)}s)`
    }

    return (
        <>
            {/* 5. Physics Step (Integration Δt) */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('physicsStepSectionLabel', '5. Pas Physique (Intégration Δt) :')}</span>
                    <select
                        value={stepSeconds}
                        onChange={(e) => setStepSeconds(parseFloat(e.target.value))}
                        style={{
                            width: 280,
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 6,
                            padding: '6px 10px',
                            fontSize: 12,
                            fontWeight: 600
                        }}
                    >
                        <option value={0.0166}>16.6 ms (60 Hz - Max Physics Fidelity / Default)</option>
                        <option value={0.05}>50 ms (20 Hz - Standard Precision)</option>
                        <option value={0.1}>100 ms (10 Hz - Fast Mode)</option>
                        <option value={1.0}>1.0 s (Macroscopic Ecosystem Mode)</option>
                        <option value={5.0}>5.0 s (Ultra Macroscopic Mode)</option>
                    </select>
                </div>
                <div style={{ fontSize: 10, fontStyle: 'italic', color: textMuted, marginLeft: 170 }}>
                    {t('physicsStepNote', 'Note : Pour une graine et un pas Δt donnés, l\'exécution de la simulation est strictement déterministe et reproductible.')}
                </div>
            </div>

            {/* 6. Maximum Simulation Duration */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('maxDurationSectionLabel', '6. Durée Maximale de Simulation :')}</span>
                    <input
                        type="number"
                        min="1"
                        max="2000000000"
                        value={maxDuration}
                        onChange={(e) => setMaxDuration(parseFloat(e.target.value))}
                        style={{
                            width: 80,
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 5,
                            padding: '5px 8px',
                            fontSize: 12,
                            fontWeight: 700
                        }}
                    />
                    <select
                        value={durationUnit}
                        onChange={(e) => setDurationUnit(e.target.value)}
                        style={{
                            width: 140,
                            background: inputBg,
                            color: textMain,
                            border: `1px solid ${borderCol}`,
                            borderRadius: 5,
                            padding: '5px 8px',
                            fontSize: 12,
                            fontWeight: 600
                        }}
                    >
                        <option value="Days (d)">Days (d)</option>
                        <option value="Hours (h)">Hours (h)</option>
                        <option value="Minutes (min)">Minutes (min)</option>
                        <option value="Seconds (s)">Seconds (s)</option>
                        <option value="Months (30d)">Months (30d)</option>
                        <option value="Years (365d)">Years (365d)</option>
                        <option value="Ticks">Ticks</option>
                        <option value="∞ Unlimited">∞ Unlimited</option>
                    </select>
                </div>
                <div style={{ fontSize: 10, fontWeight: 700, color: '#38bdf8', marginLeft: 170 }}>
                    {getCalculatedDurationInfo()}
                </div>
            </div>

            {/* 7. Min Population Stop Threshold */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }} title={t('minPopStopSectionTt', 'Arrête automatiquement la simulation si la population totale tombe sous ce seuil (0 = désactivé).')}>
                    {t('minPopStopSectionLabel', '7. Seuil d\'Arrêt Population Minimale :')}
                </span>
                <input
                    type="number"
                    min="0"
                    max="10000"
                    value={minPopStop}
                    onChange={(e) => setMinPopStop(parseInt(e.target.value))}
                    title={t('minPopStopInputTt', 'Nombre d\'individus minimum sous lequel la simulation se met automatiquement en pause.')}
                    style={{
                        width: 80,
                        background: inputBg,
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 5,
                        padding: '5px 8px',
                        fontSize: 12,
                        fontWeight: 700
                    }}
                />
                <span style={{ fontSize: 11, color: textMuted }}>ind</span>
            </div>

            {/* 8. Megaterrarium & Multiplayer Topology Card */}
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                gap: 12,
                background: isDark ? 'rgba(56, 189, 248, 0.04)' : '#f0f9ff',
                border: `1px solid ${isDark ? '#0369a1' : '#bae6fd'}`,
                borderRadius: 8,
                padding: '14px 16px'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <Globe size={16} color="#0284c7" />
                    <span style={{ fontSize: 13, fontWeight: 800, color: textMain }} title={t('megaterrariumCardTt', 'Configuration de la topologie multi-nœuds (sharding spatial) et des paramètres multijoueur.')}>
                        {t('megaterrariumCardTitle', '🌐 Topologie Mégaterrarium & Multijoueur')}
                    </span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, fontWeight: 600, cursor: 'pointer' }} title={t('multiplayerOnlyTt', 'Si coché, ce scénario nécessite plusieurs participants et un serveur/cluster pour s\'exécuter.')}>
                        <input
                            type="checkbox"
                            checked={isMultiplayerOnly}
                            onChange={(e) => setIsMultiplayerOnly(e.target.checked)}
                        />
                        <span>{t('multiplayerOnlyChk', 'Scénario Exclusivement Multijoueur')}</span>
                    </label>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }} title={t('requiredPlayersTt', 'Nombre minimum de participants (slots de colonies) pour lancer la partie (1 à 16 joueurs).')}>
                            {t('requiredPlayersLabel', 'Nombre de Joueurs Requis :')}
                        </span>
                        <input
                            type="number"
                            min="1"
                            max="16"
                            value={requiredPlayerCount}
                            onChange={(e) => setRequiredPlayerCount(parseInt(e.target.value))}
                            title={t('requiredPlayersTt', 'Nombre minimum de participants (slots de colonies) pour lancer la partie (1 à 16 joueurs).')}
                            style={{
                                width: 55,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 4,
                                padding: '4px 6px',
                                fontSize: 11,
                                fontWeight: 700,
                                textAlign: 'center'
                            }}
                        />
                        <span style={{ fontSize: 11, color: textMuted }}>joueur(s)</span>
                    </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }} title={t('megaterrariumTilesXTt', 'Nombre de sous-volumes shardés sur l\'axe X (1 à 8).')}>
                            {t('megaterrariumTilesXLabel', 'Tuiles X (Colonnes) :')}
                        </span>
                        <input
                            type="number"
                            min="1"
                            max="8"
                            value={gridTilesX}
                            onChange={(e) => setGridTilesX(parseInt(e.target.value))}
                            title={t('megaterrariumTilesXTt', 'Nombre de sous-volumes shardés sur l\'axe X (1 à 8).')}
                            style={{
                                width: 50,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 4,
                                padding: '4px 6px',
                                fontSize: 11,
                                fontWeight: 700,
                                textAlign: 'center'
                            }}
                        />
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, fontWeight: 600, color: textMuted }} title={t('megaterrariumTilesYTt', 'Nombre de sous-volumes shardés sur l\'axe Y (1 à 8).')}>
                            {t('megaterrariumTilesYLabel', 'Tuiles Y (Lignes) :')}
                        </span>
                        <input
                            type="number"
                            min="1"
                            max="8"
                            value={gridTilesY}
                            onChange={(e) => setGridTilesY(parseInt(e.target.value))}
                            title={t('megaterrariumTilesYTt', 'Nombre de sous-volumes shardés sur l\'axe Y (1 à 8).')}
                            style={{
                                width: 50,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 4,
                                padding: '4px 6px',
                                fontSize: 11,
                                fontWeight: 700,
                                textAlign: 'center'
                            }}
                        />
                    </div>

                    {/* Reactive Megaterrarium Topology Badge */}
                    <div style={{
                        fontSize: 11,
                        fontWeight: 800,
                        padding: '4px 10px',
                        borderRadius: 6,
                        background: (gridTilesX > 1 || gridTilesY > 1) 
                            ? (isDark ? 'rgba(56, 189, 248, 0.2)' : '#e0f2fe')
                            : (isDark ? '#334155' : '#e2e8f0'),
                        color: (gridTilesX > 1 || gridTilesY > 1) ? '#0284c7' : textMuted
                    }}>
                        {(gridTilesX === 1 && gridTilesY === 1)
                            ? `🗺️ ${t('megaterrariumMonolithic', 'Monde Monolithique (1 Tile)')} | ${requiredPlayerCount} ${t('playersCountUnit', 'Joueur(s)')}`
                            : `🌐 ${t('megaterrariumSharded', 'Mégaterrarium Shardé')} : Grille ${gridTilesX} × ${gridTilesY} = ${gridTilesX * gridTilesY} ${t('subvolumesUnit', 'Sous-Volumes')} | ${requiredPlayerCount} ${t('playersCountUnit', 'Joueur(s)')}`
                        }
                    </div>
                </div>

                <div style={{ fontSize: 10, color: textMuted, fontStyle: 'italic' }}>
                    {(gridTilesX === 1 && gridTilesY === 1)
                        ? t('megaterrariumMonolithicDesc', 'Simulation standard non shardée. Volume unique calculé sur un seul worker ou thread.')
                        : t('megaterrariumShardedDesc', 'Topologie distribuée multi-nœuds : halo d\'échange de phéromones (3 cellules) et migration continue des entités entre sous-volumes shardés.')
                    }
                </div>
            </div>
        </>
    )
}
