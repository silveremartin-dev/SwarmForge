import React, { useState } from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import {
    Bug,
    Heart,
    Zap,
    MapPin,
    Briefcase,
    Package,
    Clock,
    ChevronLeft,
    ChevronRight,
    Camera,
    Eye,
    Crosshair,
    Search,
    X,
    Info
} from 'lucide-react'

/**
 * Tracked Ant HUD & Interactive Inspection Pane for SwarmForge Viewport.
 * Aligned 1:1 with TrackedAntPane.java.
 */
export default function TrackedAntInspectorHUD({ onFocusAnt }) {
    const {
        trackedAntData,
        selectedEntity,
        setSelectedEntity,
        setTrackedAntId,
        selectNextAnt,
        selectPreviousAnt,
        cameraFollowMode,
        setCameraFollowMode,
        setCustomCameraTarget,
        ants,
        colonies,
        theme,
        language
    } = useSimulationStore()

    const [searchQuery, setSearchQuery] = useState('')
    const [searchError, setSearchError] = useState('')
    const [showAiTooltip, setShowAiTooltip] = useState(false)

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    const ant = trackedAntData || selectedEntity
    if (!ant) return null

    const antColony = colonies?.find(c => c.id === ant.colonyId) || {
        name: ant.colonyName || t('colonyNative', 'Colonie Native'),
        color: ant.color || '#38bdf8'
    }

    const shortId = ant.id ? ant.id.replace('ant_', '').slice(0, 8) : '001'
    const formattedId = ant.formattedId || `IND-${shortId.toUpperCase()}`
    const isFollowing = Boolean(cameraFollowMode)

    const antX = ant.x ?? 50
    const antZ = ant.z !== undefined ? ant.z : (ant.y ?? 50)
    const antY = ant.y !== undefined && ant.z !== undefined ? ant.y : 0.15

    const health = Math.max(0, Math.min(100, Math.round(ant.health ?? 100)))
    const energy = Math.max(0, Math.min(100, Math.round(ant.energy ?? 90)))
    const hunger = Math.max(0, Math.min(100, Math.round(ant.hunger ?? (100 - energy * 0.8))))
    const thirst = Math.max(0, Math.min(100, Math.round(ant.thirst ?? (100 - energy * 0.7))))

    const ageDays = typeof ant.age === 'number'
        ? (ant.age > 1000 ? (ant.age / 86400.0).toFixed(1) : (ant.age * 0.1).toFixed(1))
        : '12.4'

    const headingDeg = ant.heading !== undefined ? Math.round(ant.heading * (180 / Math.PI)) : 45
    const carriedItem = ant.carriedItem && ant.carriedItem !== 'NONE' ? ant.carriedItem : t('noLoad', 'Aucun chargement')

    const handleSearch = (e) => {
        if (e) e.preventDefault()
        const q = searchQuery.trim()
        if (!q) return

        const found = (ants || []).find(a =>
            a.id?.toLowerCase() === q.toLowerCase() ||
            a.id?.toLowerCase().includes(q.toLowerCase()) ||
            a.formattedId?.toLowerCase().includes(q.toLowerCase())
        )

        if (found) {
            setSelectedEntity(found)
            setSearchError('')
        } else {
            setSearchError(`ID "${q}" not found.`)
            setTimeout(() => setSearchError(''), 3000)
        }
    }

    const handleCenter = () => {
        if (onFocusAnt) {
            onFocusAnt(antX, antY, antZ)
        } else {
            setCustomCameraTarget([antX, antY, antZ])
        }
    }

    const casteLabel = ant.caste === 'QUEEN' ? t('queens', 'REINE') : (ant.caste === 'SOLDIER' ? t('soldiers', 'SOLDAT') : (ant.caste === 'MALE' ? t('malesCardLabel', 'MÂLE') : t('workers', 'OUVRIÈRE')))
    const jobLabel = ant.job || ant.task || 'Patrouille & Forage'
    const stateLabel = ant.state || 'ACTIF / EXPLORATION'

    return (
        <div style={{
            position: 'absolute',
            bottom: 20,
            left: 20,
            width: 350,
            maxWidth: 370,
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.96)',
            border: isDark ? '1.5px solid #f59e0b' : '1.5px solid #0284c7',
            borderRadius: 10,
            padding: '10px 12px',
            color: isDark ? '#fff' : '#0f172a',
            zIndex: 95,
            backdropFilter: 'blur(14px)',
            boxShadow: '0 8px 30px rgba(0,0,0,0.45)',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            fontSize: 11,
            display: 'flex',
            flexDirection: 'column',
            gap: 6
        }}>
            {/* Header: Title + Close Button (1:1 with TrackedAntPane.java) */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingBottom: 4
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, overflow: 'hidden' }}>
                    <Bug size={14} color="#f59e0b" />
                    <span style={{ fontWeight: 800, color: isDark ? '#f59e0b' : '#0369a1', fontSize: 12.5, whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>
                        🎯 {isFollowing ? t('trackedAntTitle', 'Fourmi Suivie') : t('trackedAntSelectedTitle', 'Fourmi Sélectionnée')}: {casteLabel} [{formattedId}] (#{shortId})
                    </span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <button
                        onClick={selectPreviousAnt}
                        title="Previous"
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: 2 }}
                    >
                        <ChevronLeft size={14} />
                    </button>
                    <button
                        onClick={selectNextAnt}
                        title="Next"
                        style={{ background: 'transparent', border: 'none', color: isDark ? '#94a3b8' : '#64748b', cursor: 'pointer', padding: 2 }}
                    >
                        <ChevronRight size={14} />
                    </button>
                    <button
                        onClick={() => {
                            setSelectedEntity(null)
                            setTrackedAntId(null)
                            setCameraFollowMode(null)
                        }}
                        title={t('eventDetailsClose', 'Fermer')}
                        style={{ background: 'transparent', border: 'none', color: '#94a3b8', fontWeight: 'bold', fontSize: 13, cursor: 'pointer', padding: '0 4px' }}
                    >
                        ✕
                    </button>
                </div>
            </div>

            {/* Separator 1 */}
            <div style={{ height: 1, background: 'rgba(245, 158, 11, 0.3)', width: '100%' }} />

            {/* Telemetry Section (1:1 with TrackedAntPane.java) */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4.5 }}>
                {/* 0. Species & Colony Row */}
                <div style={{ fontSize: 11, fontWeight: 'bold', color: isDark ? '#38bdf8' : '#0284c7' }}>
                    🧬 {ant.species || 'Formica fusca'} | 🏛️ {antColony.name || ant.colonyId || 'Colony #1'}
                </div>

                {/* 1. Health Row */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span style={{ fontSize: 11, color: isDark ? '#cbd5e1' : '#334155' }}>
                        ❤️ {t('health', 'Santé')} : {health}% {health <= 0 ? '💀 [DEAD]' : ''}
                    </span>
                    <div style={{ width: 90, height: 10, background: isDark ? '#1e293b' : '#e2e8f0', borderRadius: 3, overflow: 'hidden' }}>
                        <div style={{ width: `${health}%`, height: '100%', background: health > 50 ? '#22c55e' : '#ef4444', transition: 'width 0.3s' }} />
                    </div>
                </div>

                {/* 2. Energy / Hunger / Thirst */}
                <div style={{ fontSize: 11, color: isDark ? '#38bdf8' : '#0284c7' }}>
                    ⚡ {t('energy', 'Énergie')}: {energy}% | 🍗 {t('hunger', 'Faim')}: {hunger}% | 💧 {t('thirst', 'Soif')}: {thirst}%
                </div>

                {/* 3. Age / Stage / Job */}
                <div style={{ fontSize: 11, color: isDark ? '#e2e8f0' : '#1e293b' }}>
                    ⏳ {t('age', 'Âge')}: {ageDays} d | 🐛 {t('stageAdult', 'Adulte')} | 💼 {t('job', 'Tâche')}: {jobLabel}
                </div>

                {/* 4. AI State */}
                <div style={{ position: 'relative' }}>
                    <div
                        onMouseEnter={() => setShowAiTooltip(true)}
                        onMouseLeave={() => setShowAiTooltip(false)}
                        style={{
                            fontSize: 11,
                            color: isDark ? '#a78bfa' : '#6d28d9',
                            cursor: 'help',
                            textDecoration: 'underline',
                            display: 'inline-block'
                        }}
                    >
                        🧠 AI : {stateLabel} | {jobLabel}
                    </div>

                    {showAiTooltip && (
                        <div style={{
                            position: 'absolute',
                            bottom: '100%',
                            left: 0,
                            marginBottom: 6,
                            width: 280,
                            background: '#0f172a',
                            border: '1px solid #a78bfa',
                            borderRadius: 6,
                            padding: '8px 10px',
                            color: '#e2e8f0',
                            fontSize: 10,
                            lineHeight: 1.4,
                            boxShadow: '0 4px 20px rgba(0,0,0,0.6)',
                            zIndex: 130,
                            pointerEvents: 'none'
                        }}>
                            <div style={{ fontWeight: 'bold', color: '#a78bfa', marginBottom: 2 }}>🧠 AI - {stateLabel}</div>
                            <div>💼 {jobLabel}</div>
                            <div style={{ marginTop: 2 }}>🧬 {ant.species || 'Formica fusca'}</div>
                        </div>
                    )}
                </div>

                {/* 5. 3D Position */}
                <div style={{ fontSize: 11, color: isDark ? '#cbd5e1' : '#334155' }}>
                    📐 {t('position3D', 'Position 3D')}: X={antX.toFixed(1)}m, Y={antY.toFixed(1)}m, Z={antZ.toFixed(1)}m
                </div>

                {/* 6. Heading & Cargo */}
                <div style={{ fontSize: 11, color: isDark ? '#cbd5e1' : '#334155' }}>
                    🧭 {t('heading', 'Cap')}: {headingDeg}° | 🍯 {t('carriedItem', 'Transporte')}: {carriedItem}
                </div>

                {/* 7. CHC Gestalt Status */}
                <div style={{ fontSize: 11, color: '#4ade80', fontWeight: 600 }}>
                    {t('chcGestaltValid', '🟢 CHC Profil Hydrocarbures : Gestalt Colonial Reconnu')}
                </div>
            </div>

            {/* Separator 2 */}
            <div style={{ height: 1, background: 'rgba(245, 158, 11, 0.3)', width: '100%' }} />

            {/* Action Controls Box (1:1 with TrackedAntPane.java) */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
                {/* Row 1: Camera Modes (TPS & FPS) */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
                    <button
                        onClick={() => setCameraFollowMode(cameraFollowMode === 'TPS' ? null : 'TPS')}
                        style={{
                            background: cameraFollowMode === 'TPS' ? '#0369a1' : '#0284c7',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: 4,
                            padding: '5px 8px',
                            fontSize: 11,
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: 5
                        }}
                    >
                        <Camera size={12} />
                        {cameraFollowMode === 'TPS' ? t('cameraTpsActiveBtn', '✓ Suivi TPS') : t('cameraTpsBtn', '🎥 Vue TPS')}
                    </button>

                    <button
                        onClick={() => setCameraFollowMode(cameraFollowMode === 'FPS' ? null : 'FPS')}
                        style={{
                            background: cameraFollowMode === 'FPS' ? '#6d28d9' : '#8b5cf6',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: 4,
                            padding: '5px 8px',
                            fontSize: 11,
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: 5
                        }}
                    >
                        <Eye size={12} />
                        {cameraFollowMode === 'FPS' ? t('cameraFpsActiveBtn', '✓ Suivi FPS') : t('cameraFpsBtn', '👁️ Vue FPS')}
                    </button>
                </div>

                {/* Row 2: Center Viewport Button */}
                <button
                    onClick={handleCenter}
                    style={{
                        background: '#059669',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: 4,
                        padding: '5px 8px',
                        fontSize: 11,
                        fontWeight: 'bold',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: 5
                    }}
                >
                    <Crosshair size={12} />
                    {t('centerCameraBtn', '🎯 Centrer la vue sur l\'entité')}
                </button>

                {/* Row 3: ID Direct Search Row */}
                <form onSubmit={handleSearch} style={{ display: 'flex', gap: 6 }}>
                    <input
                        type="text"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        placeholder={t('searchAntPlaceholder', 'ID ou #UUID...')}
                        style={{
                            flex: 1,
                            background: isDark ? '#1e293b' : '#f8fafc',
                            color: isDark ? '#ffffff' : '#0f172a',
                            border: isDark ? '1px solid #334155' : '1px solid #cbd5e1',
                            borderRadius: 4,
                            padding: '4px 8px',
                            fontSize: 11,
                            outline: 'none'
                        }}
                    />
                    <button
                        type="submit"
                        style={{
                            background: '#0284c7',
                            color: '#ffffff',
                            border: 'none',
                            borderRadius: 4,
                            padding: '4px 10px',
                            fontSize: 11,
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 4
                        }}
                    >
                        <Search size={11} />
                        {t('searchBtn', 'Rechercher')}
                    </button>
                </form>

                {searchError && (
                    <div style={{ color: '#ef4444', fontSize: 10 }}>
                        {searchError}
                    </div>
                )}
            </div>
        </div>
    )
}
