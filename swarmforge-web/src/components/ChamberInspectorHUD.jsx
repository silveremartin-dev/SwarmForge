import React from 'react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { Crosshair, X } from 'lucide-react'

/**
 * Dedicated HUD & Inspection Overlay Pane for Subterranean Nest Chambers.
 * Aligned 1:1 with ChamberInfoPane.java.
 */
export default function ChamberInspectorHUD({ onFocusChamber }) {
    const {
        selectedChamber,
        setSelectedChamber,
        setCustomCameraTarget,
        ants,
        colonies,
        theme,
        language
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallback) => getTranslation(language, key, fallback)

    if (!selectedChamber) return null

    const node = selectedChamber
    const colony = colonies?.find(c => c.id === node.colonyId) || colonies?.[0] || {
        name: 'Colony #1',
        speciesId: 'Formica fusca',
        food: 250
    }

    const posX = node.x ?? 50
    const posZ = node.z !== undefined ? node.z : (node.y ?? 50)
    const posY = node.y !== undefined && node.z !== undefined ? node.y : -1.2
    const depthM = Math.abs(posY)

    // Format Chamber Type Name (1:1 with ChamberInfoPane.java)
    const typeUpper = (node.type || 'CHAMBER').toUpperCase()
    let chamberTypeName = t('chamberStandard', 'Chambre Standard')
    let specialtyText = '🏛️ Standard'
    let architectureType = 'Chambre Ovale Maçonnée'

    if (typeUpper.includes('QUEEN') || typeUpper.includes('ROYAL')) {
        chamberTypeName = t('chamberRoyal', 'Chambre Royale')
        specialtyText = '👑 1 Reine(s) | Q Pheromone'
        architectureType = 'Crypte Royale Renforcée'
    } else if (typeUpper.includes('BROOD') || typeUpper.includes('NURSERY')) {
        chamberTypeName = t('chamberBrood', 'Chambre de Couvain')
        specialtyText = '🥚 Brood: 18 units | Survival: 98%'
        architectureType = 'Alvéoles Thermorégulées'
    } else if (typeUpper.includes('FOOD') || typeUpper.includes('GRAIN') || typeUpper.includes('STORAGE')) {
        chamberTypeName = t('chamberFood', 'Grenier à Graines')
        specialtyText = `🌾 ${(colony.food || 250).toFixed(1)} mg`
        architectureType = 'Silo Hydrofuge Sécurisé'
    } else if (typeUpper.includes('FUNGUS') || typeUpper.includes('GARDEN')) {
        chamberTypeName = t('chamberFungus', 'Jardin Champignonnière')
        specialtyText = '🍄 45.0g Leucoagaricus'
        architectureType = 'Chambre Humide à Piliers'
    } else if (typeUpper.includes('WASTE') || typeUpper.includes('DUMP')) {
        chamberTypeName = t('chamberWaste', 'Dépotoir / Cloaque')
        specialtyText = '🗑️ Refuse'
        architectureType = 'Cavité Isolée de Décharge'
    } else if (typeUpper.includes('VENTILATION') || typeUpper.includes('CHIMNEY')) {
        chamberTypeName = t('chamberVent', 'Cheminée de Ventilation')
        specialtyText = '🌪️ Ventilation Shaft'
        architectureType = 'Puits Vertical Aérateur'
    }

    // Dynamic Occupant calculations
    const radius = node.radius || node.radiusX || 1.4
    let occupants = 0
    let queens = 0, workers = 0, soldiers = 0, brood = 2
    if (ants && ants.length > 0) {
        for (const a of ants) {
            const ax = a.x ?? 50
            const az = a.z !== undefined ? a.z : (a.y ?? 50)
            const d = Math.hypot(ax - posX, az - posZ)
            if (d <= radius * 2.5) {
                occupants++
                if (a.caste === 'QUEEN') queens++
                else if (a.caste === 'SOLDIER') soldiers++
                else workers++
            }
        }
    }
    if (occupants === 0 && typeUpper.includes('QUEEN')) {
        queens = 1; occupants = 4; workers = 3
    }

    const occProgress = Math.min(1.0, occupants / 40.0)
    const foodStored = colony.food ?? 250.0

    // Microclimate & Atmospheric Gas calculations (1:1 with ChamberInfoPane.java)
    const tempC = (20.5 + Math.sin(posX * 0.1) * 1.5).toFixed(1)
    const humPct = Math.min(95, Math.round(68 + depthM * 6.5))
    const co2Ppm = Math.round(420 + depthM * 140 + occupants * 50)
    const co2Pct = (co2Ppm / 10000.0).toFixed(3)
    const o2Pct = (20.95 - (co2Ppm - 400) * 0.0006).toFixed(1)
    const airFlowSpeed = typeUpper.includes('VENTILATION') ? '0.35' : (typeUpper.includes('FUNGUS') ? '0.14' : '0.08')
    const hygieneScore = typeUpper.includes('WASTE') ? 45 : (typeUpper.includes('BROOD') ? 99 : 98)

    const rx = radius.toFixed(1)
    const ry = (radius * 0.7).toFixed(1)
    const rz = radius.toFixed(1)

    const handleCenter = () => {
        if (onFocusChamber) {
            onFocusChamber(posX, posY, posZ)
        } else {
            setCustomCameraTarget([posX, posY, posZ])
        }
    }

    return (
        <div style={{
            position: 'absolute',
            bottom: 20,
            left: 20,
            width: 360,
            maxWidth: 380,
            background: isDark ? 'rgba(15, 23, 42, 0.94)' : 'rgba(255, 255, 255, 0.96)',
            border: isDark ? '1.5px solid #38bdf8' : '1.5px solid #0284c7',
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
            {/* Header: Title + Close Button (1:1 with ChamberInfoPane.java) */}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                paddingBottom: 4
            }}>
                <span style={{ fontWeight: 800, color: isDark ? '#38bdf8' : '#0369a1', fontSize: 13 }}>
                    🏛️ {t('chamberTitle', 'Chambre')} : {chamberTypeName}
                </span>

                <button
                    onClick={() => setSelectedChamber(null)}
                    title={t('eventDetailsClose', 'Fermer')}
                    style={{ background: 'transparent', border: 'none', color: '#94a3b8', fontWeight: 'bold', fontSize: 12, cursor: 'pointer', padding: '0 4px' }}
                >
                    ✕
                </button>
            </div>

            {/* Separator 1 */}
            <div style={{ height: 1, background: 'rgba(56, 189, 248, 0.3)', width: '100%' }} />

            {/* Telemetry Section (1:1 with ChamberInfoPane.java) */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4.5 }}>
                {/* 0. Species & Colony Row */}
                <div style={{ fontSize: 11, fontWeight: 'bold', color: isDark ? '#38bdf8' : '#0284c7' }}>
                    🧬 {colony.speciesId || 'Formica fusca'} | 🏛️ {colony.name || colony.id || 'Colony #1'}
                </div>

                {/* 1. Occupants Row */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span style={{ fontSize: 11, color: isDark ? '#cbd5e1' : '#334155' }}>
                        👥 {t('chamberOccupants', 'Occupants')} : {occupants} (👑{queens} | ⚒️{workers} | ⚔️{soldiers} | 🥚{brood})
                    </span>
                    <div style={{ width: 90, height: 10, background: isDark ? '#1e293b' : '#e2e8f0', borderRadius: 3, overflow: 'hidden' }}>
                        <div style={{ width: `${Math.round(occProgress * 100)}%`, height: '100%', background: '#38bdf8', transition: 'width 0.3s' }} />
                    </div>
                </div>

                {/* 2. Resources & Radius */}
                <div style={{ fontSize: 11, color: isDark ? '#22c55e' : '#15803d' }}>
                    📦 Stock: {foodStored.toFixed(1)} mg | R: {radius.toFixed(2)} m
                </div>

                {/* 3. Specialty Function */}
                <div style={{ fontSize: 11, color: isDark ? '#f59e0b' : '#b45309' }}>
                    {specialtyText}
                </div>

                {/* 4. Position & Depth */}
                <div style={{ fontSize: 11, color: isDark ? '#e2e8f0' : '#1e293b' }}>
                    📐 Pos: ({posX.toFixed(1)}m, {posZ.toFixed(1)}m) | {t('chamberDepth', 'Profondeur')}: -{depthM.toFixed(2)} m
                </div>

                {/* 5. Microclimate (Temp & Humidity) */}
                <div style={{ fontSize: 11, color: isDark ? '#38bdf8' : '#0284c7' }}>
                    🌡️ {t('voxelTemp', 'Température')}: {tempC}°C | 💧 {t('voxelHumidity', 'Humidité')}: {humPct}%
                </div>

                {/* 6. Atmospheric Gases */}
                <div style={{ fontSize: 11, fontWeight: 'bold', color: isDark ? '#4ade80' : '#16a34a' }}>
                    💨 {t('voxelGas', 'Gaz')} : CO₂: {co2Ppm} ppm ({co2Pct}%) | O₂: {o2Pct}%
                </div>

                {/* 7. Ventilation & Hygiene */}
                <div style={{ fontSize: 11, color: isDark ? '#38bdf8' : '#0284c7' }}>
                    🌬️ Flow: {airFlowSpeed} m/s | 🛡️ {t('chamberHygiene', 'Score de Salubrité')}: {hygieneScore}%
                </div>

                {/* 8. Architecture */}
                <div style={{ fontSize: 11, color: isDark ? '#a78bfa' : '#6d28d9' }}>
                    🏛️ {architectureType}
                </div>

                {/* 9. Dimensions */}
                <div style={{ fontSize: 11, color: isDark ? '#cbd5e1' : '#334155' }}>
                    📏 Rx={rx}m, Ry={ry}m, Rz={rz}m
                </div>
            </div>

            {/* Separator 2 */}
            <div style={{ height: 1, background: 'rgba(56, 189, 248, 0.3)', width: '100%' }} />

            {/* Action: Center Viewport Button (1:1 with ChamberInfoPane.java) */}
            <button
                onClick={handleCenter}
                style={{
                    background: '#059669',
                    color: '#ffffff',
                    border: 'none',
                    borderRadius: 4,
                    padding: '6px 10px',
                    fontSize: 11,
                    fontWeight: 'bold',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 6
                }}
            >
                <Crosshair size={12} />
                {t('chamberCenterBtn', '🎯 Centrer la vue sur la chambre')}
            </button>
        </div>
    )
}
