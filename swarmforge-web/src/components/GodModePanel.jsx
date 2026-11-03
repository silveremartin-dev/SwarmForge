import React, { useState } from 'react'
import {
    Users,
    Package,
    AlertTriangle,
    Sun,
    Wind,
    ShieldAlert,
    Activity,
    Zap,
    Calendar,
    Copy,
    Trash2,
    Play,
    Pause,
    Clock,
    Crosshair,
    Thermometer,
    Sliders
} from 'lucide-react'
import { useSimulationStore, formatSimCalendarTime } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'
import { showToast } from '../store/toastStore'
import { GodModeCategorySubForms, ScheduledEventsQueueTable } from './GodModeSubPanels'

export default function GodModePanel() {
    const {
        ticks,
        stepSeconds,
        startDateTime,
        colonies,
        scheduledEvents,
        addScheduledEvent,
        removeScheduledEvent,
        togglePauseScheduledEvent,
        duplicateScheduledEvent,
        executeInstantIntervention,
        theme,
        language
    } = useSimulationStore()

    const isDark = theme === 'dark'
    const t = (key, fallbackOrParams, params) => getTranslation(language, key, fallbackOrParams, params)

    const categories = [
        { id: 'ENTITIES', label: t('catEntities', '1. Entités & Couvain'), icon: Users, color: '#3b82f6' },
        { id: 'RESOURCE', label: t('catResource', '2. Ressources & Taxonomie'), icon: Package, color: '#10b981' },
        { id: 'DISASTER', label: t('catDisaster', '3. Désastres Climatiques'), icon: AlertTriangle, color: '#ef4444' },
        { id: 'ABIOTIC', label: t('catAbiotic', '4. Physique Abiotique (+/-)'), icon: Thermometer, color: '#f59e0b' },
        { id: 'PHEROMONE', label: t('catPheromone', '5. Phéromones & Pistes'), icon: Wind, color: '#8b5cf6' },
        { id: 'INVASION', label: t('catInvasion', '6. Invasions & Prédateurs'), icon: ShieldAlert, color: '#dc2626' },
        { id: 'MUTATION', label: t('catMutation', '7. Génétique & Mutagènes'), icon: Activity, color: '#ec4899' }
    ]

    const [activeCategory, setActiveCategory] = useState('ENTITIES')

    // Common spatial & time parameters
    const [targetColonyId, setTargetColonyId] = useState(colonies[0]?.id || '')
    const [posX, setPosX] = useState(32)
    const [posY] = useState(32)
    const [posZ, setPosZ] = useState(1.0)
    const [targetOffsetSeconds, setTargetOffsetSeconds] = useState(60)

    // Sub-block parameters
    const [entityAction, setEntityAction] = useState('SPAWN')
    const [caste, setCaste] = useState('WORKER')
    const [antCount, setAntCount] = useState(10)

    const [resourceType, setResourceType] = useState('SUGAR')
    const [foodNature, setFoodNature] = useState('Nectar & Honeydew')
    const [resourceAmount, setResourceAmount] = useState(150)

    const [disasterType, setDisasterType] = useState('WILDFIRE')
    const [disasterIntensity, setDisasterIntensity] = useState(0.6)
    const [disasterDurationMinutes, setDisasterDurationMinutes] = useState(30)

    // Abiotic relative vs absolute
    const [isRelativeAbiotic, setIsRelativeAbiotic] = useState(true)
    const [tempCelsius, setTempCelsius] = useState(24.0)
    const [deltaTemp, setDeltaTemp] = useState(4.0)
    const [humidityPercent, setHumidityPercent] = useState(70.0)
    const [deltaHumidity, setDeltaHumidity] = useState(15.0)
    const [windSpeed, setWindSpeed] = useState(2.0)
    const [deltaWind, setDeltaWind] = useState(1.5)
    const [solarWatts, setSolarWatts] = useState(450.0)
    const [abioticDurationMinutes, setAbioticDurationMinutes] = useState(60)

    const [pheromoneType, setPheromoneType] = useState('FOOD_TRAIL')
    const [pheromoneIntensity, setPheromoneIntensity] = useState(80)
    const [pheromoneRadius, setPheromoneRadius] = useState(6.0)
    const [pheromoneDurationMinutes, setPheromoneDurationMinutes] = useState(20)

    const [invasionType, setInvasionType] = useState('MEGAPONERA')
    const [invasionCount, setInvasionCount] = useState(12)
    const [invasionDurationMinutes, setInvasionDurationMinutes] = useState(45)

    const [mutationType, setMutationType] = useState('SPEED_BOOST')
    const [mutationMultiplier, setMutationMultiplier] = useState(1.5)
    const [mutationDurationMinutes, setMutationDurationMinutes] = useState(120)

    const buildInterventionObject = () => {
        let typeName = ''
        let desc = ''

        if (activeCategory === 'ENTITIES') {
            typeName = `${entityAction} ${caste}`
            desc = `${entityAction === 'SPAWN' ? 'Apparition' : 'Suppression'} de ${antCount} ${caste}s`
        } else if (activeCategory === 'RESOURCE') {
            typeName = `Dépôt ${resourceType}`
            desc = `${resourceAmount} unités de ${resourceType} (${foodNature})`
        } else if (activeCategory === 'DISASTER') {
            typeName = `Catastrophe ${disasterType}`
            desc = `${disasterType} (Intensité ${(disasterIntensity * 100).toFixed(0)}%, Durée ${disasterDurationMinutes} min)`
        } else if (activeCategory === 'ABIOTIC') {
            typeName = isRelativeAbiotic ? `Delta Climat (+${deltaTemp}°C, +${deltaHumidity}%)` : `Cible Climat (${tempCelsius}°C, ${humidityPercent}%)`
            desc = isRelativeAbiotic
                ? `ΔTemp: +${deltaTemp}°C, ΔHygro: +${deltaHumidity}%, ΔVent: +${deltaWind}m/s (${abioticDurationMinutes} min)`
                : `Temp: ${tempCelsius}°C, Hygro: ${humidityPercent}%, Vent: ${windSpeed}m/s (${abioticDurationMinutes} min)`
        } else if (activeCategory === 'PHEROMONE') {
            typeName = `Phéromone ${pheromoneType}`
            desc = `Piste ${pheromoneType} (Conc: ${pheromoneIntensity}, Rayon: ${pheromoneRadius}m, Durée: ${pheromoneDurationMinutes} min)`
        } else if (activeCategory === 'INVASION') {
            typeName = `Incursion ${invasionType}`
            desc = `${invasionCount} prédateurs (${invasionType}) pendant ${invasionDurationMinutes} min`
        } else if (activeCategory === 'MUTATION') {
            typeName = `Mutagène ${mutationType}`
            desc = `Boost ${mutationType} (x${mutationMultiplier}) pendant ${mutationDurationMinutes} min`
        }

        return {
            category: activeCategory,
            type: typeName,
            description: desc,
            action: entityAction,
            caste: caste,
            count: antCount,
            colonyId: targetColonyId,
            resourceType: resourceType,
            foodNature: foodNature,
            amount: resourceAmount,
            disasterType: disasterType,
            intensity: disasterIntensity,
            durationMinutes: disasterDurationMinutes,
            isRelativeAbiotic: isRelativeAbiotic,
            tempCelsius: tempCelsius,
            deltaTemp: deltaTemp,
            humidityPercent: humidityPercent,
            deltaHumidity: deltaHumidity,
            windMetersPerSec: windSpeed,
            deltaWind: deltaWind,
            solarWatts: solarWatts,
            pheromoneType: pheromoneType,
            radius: pheromoneRadius,
            invasionType: invasionType,
            mutationType: mutationType,
            multiplier: mutationMultiplier,
            posX: posX,
            posY: posY,
            posZ: posZ
        }
    }

    const handleInstantExecute = () => {
        const intervention = buildInterventionObject()
        executeInstantIntervention(intervention)
        showToast(`⚡ Intervention [${activeCategory}] exécutée !`, 'info')
    }

    const handleScheduleEvent = () => {
        const intervention = buildInterventionObject()
        const targetTick = ticks + Math.round(targetOffsetSeconds / stepSeconds)
        const calendarTime = formatSimCalendarTime(startDateTime, targetTick * stepSeconds)

        addScheduledEvent({
            ...intervention,
            targetTick: targetTick,
            scheduledCalendarTime: calendarTime,
            eventType: intervention.type,
            colonyTarget: colonies.find(c => c.id === targetColonyId)?.name || 'TOUTES'
        })
        showToast(`📅 Événement planifié pour le tick ${targetTick} !`, 'success')
    }

    const cardBg = isDark ? '#1e293b' : '#ffffff'
    const borderCol = isDark ? '#334155' : '#e2e8f0'
    const inputBg = isDark ? '#0f172a' : '#f8fafc'
    const textMain = isDark ? '#f1f5f9' : '#0f172a'
    const textMuted = isDark ? '#94a3b8' : '#64748b'

    return (
        <div style={{
            maxWidth: 1100,
            margin: '0 auto',
            padding: '20px 24px',
            display: 'flex',
            flexDirection: 'column',
            gap: 20
        }}>
            {/* Header */}
            <div>
                <h2 style={{ margin: 0, fontSize: 18, fontWeight: 800, color: '#f59e0b', display: 'flex', alignItems: 'center', gap: 8 }}>
                    <Zap size={20} /> {t('godModePanelTitle', "God Mode (Interventions Divines & File d'Événements)")}
                </h2>
                <p style={{ margin: '4px 0 0', fontSize: 12, color: textMuted }}>
                    {t('godModePanelSubtitle', "Manipulation directe des entités, biocénose, désastres, physique abiotique relative/absolue et calendrier d'interventions.")}
                </p>

                {/* Quick Macro Presets Bar */}
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap', marginTop: 10 }}>
                    <span style={{ fontSize: 11, fontWeight: 800, color: '#f59e0b' }}>{t('quickMacrosLabel', '⚡ Macros Rapides :')}</span>
                    {[
                        { label: t('macroWildfire', '🔥 Grand Incendie'), cat: 'DISASTER', setup: () => { setActiveCategory('DISASTER'); setDisasterType('WILDFIRE'); setDisasterIntensity(0.85); setDisasterDurationMinutes(40); } },
                        { label: t('macroFlood', '🌊 Inondation Crue'), cat: 'DISASTER', setup: () => { setActiveCategory('DISASTER'); setDisasterType('FLOOD'); setDisasterIntensity(0.75); setDisasterDurationMinutes(30); } },
                        { label: t('macroSugar', '🍯 Manne Sucrée'), cat: 'RESOURCE', setup: () => { setActiveCategory('RESOURCE'); setResourceType('SUGAR'); setFoodNature('Nectar & Honeydew'); setResourceAmount(500); } },
                        { label: t('macroAlarm', '🚨 Alerte Chimique'), cat: 'PHEROMONE', setup: () => { setActiveCategory('PHEROMONE'); setPheromoneType('ALARM'); setPheromoneIntensity(100); setPheromoneRadius(15); } },
                        { label: t('macroLocusts', '🦗 Nuée de Criquets'), cat: 'INVASION', setup: () => { setActiveCategory('INVASION'); setInvasionType('LOCUST'); setInvasionCount(25); } },
                        { label: t('macroQueens', '👑 Renfort Reines'), cat: 'ENTITIES', setup: () => { setActiveCategory('ENTITIES'); setEntityAction('SPAWN'); setCaste('QUEEN'); setAntCount(2); } }
                    ].map((macro, idx) => (
                        <button
                            key={idx}
                            onClick={() => {
                                macro.setup()
                                showToast(`⚡ ${macro.label}`, 'info')
                            }}
                            style={{
                                background: isDark ? '#334155' : '#e2e8f0',
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 5,
                                padding: '4px 8px',
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer'
                            }}
                        >
                            {macro.label}
                        </button>
                    ))}
                </div>
            </div>

            {/* Category Navigation Bar */}
            <div style={{
                display: 'flex',
                gap: 8,
                flexWrap: 'wrap',
                background: cardBg,
                padding: '8px 12px',
                borderRadius: 8,
                border: `1px solid ${borderCol}`
            }}>
                {categories.map(cat => {
                    const Icon = cat.icon
                    const isActive = activeCategory === cat.id
                    return (
                        <button
                            key={cat.id}
                            onClick={() => setActiveCategory(cat.id)}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 6,
                                padding: '7px 12px',
                                fontSize: 12,
                                fontWeight: 700,
                                borderRadius: 6,
                                border: 'none',
                                cursor: 'pointer',
                                background: isActive ? cat.color : 'transparent',
                                color: isActive ? '#ffffff' : (isDark ? '#cbd5e1' : '#475569'),
                                transition: 'all 0.15s ease'
                            }}
                        >
                            <Icon size={14} />
                            <span>{cat.label}</span>
                        </button>
                    )
                })}
            </div>

            {/* Main Action Config Card */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '18px 20px',
                display: 'flex',
                flexDirection: 'column',
                gap: 16
            }}>
                <GodModeCategorySubForms
                    activeCategory={activeCategory}
                    t={t}
                    inputBg={inputBg}
                    textMain={textMain}
                    textMuted={textMuted}
                    borderCol={borderCol}
                    entityAction={entityAction}
                    setEntityAction={setEntityAction}
                    caste={caste}
                    setCaste={setCaste}
                    antCount={antCount}
                    setAntCount={setAntCount}
                    targetColonyId={targetColonyId}
                    setTargetColonyId={setTargetColonyId}
                    colonies={colonies}
                    resourceType={resourceType}
                    setResourceType={setResourceType}
                    foodNature={foodNature}
                    setFoodNature={setFoodNature}
                    resourceAmount={resourceAmount}
                    setResourceAmount={setResourceAmount}
                    disasterType={disasterType}
                    setDisasterType={setDisasterType}
                    disasterIntensity={disasterIntensity}
                    setDisasterIntensity={setDisasterIntensity}
                    disasterDurationMinutes={disasterDurationMinutes}
                    setDisasterDurationMinutes={setDisasterDurationMinutes}
                    isRelativeAbiotic={isRelativeAbiotic}
                    setIsRelativeAbiotic={setIsRelativeAbiotic}
                    deltaTemp={deltaTemp}
                    setDeltaTemp={setDeltaTemp}
                    deltaHumidity={deltaHumidity}
                    setDeltaHumidity={setDeltaHumidity}
                    deltaWind={deltaWind}
                    setDeltaWind={setDeltaWind}
                    tempCelsius={tempCelsius}
                    setTempCelsius={setTempCelsius}
                    humidityPercent={humidityPercent}
                    setHumidityPercent={setHumidityPercent}
                    windSpeed={windSpeed}
                    setWindSpeed={setWindSpeed}
                    abioticDurationMinutes={abioticDurationMinutes}
                    setAbioticDurationMinutes={setAbioticDurationMinutes}
                    pheromoneType={pheromoneType}
                    setPheromoneType={setPheromoneType}
                    pheromoneIntensity={pheromoneIntensity}
                    setPheromoneIntensity={setPheromoneIntensity}
                    pheromoneRadius={pheromoneRadius}
                    setPheromoneRadius={setPheromoneRadius}
                    invasionType={invasionType}
                    setInvasionType={setInvasionType}
                    invasionCount={invasionCount}
                    setInvasionCount={setInvasionCount}
                    invasionDurationMinutes={invasionDurationMinutes}
                    setInvasionDurationMinutes={setInvasionDurationMinutes}
                    mutationType={mutationType}
                    setMutationType={setMutationType}
                    mutationMultiplier={mutationMultiplier}
                    setMutationMultiplier={setMutationMultiplier}
                />

                {/* Spatial Sliders (Center X / Y / Z) */}
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 16,
                    background: inputBg,
                    padding: '10px 14px',
                    borderRadius: 6,
                    border: `1px solid ${borderCol}`,
                    flexWrap: 'wrap'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <Crosshair size={14} color="#38bdf8" />
                        <span style={{ fontSize: 12, fontWeight: 700 }}>{t('posXLabel', 'X (Ouest-Est) :')}</span>
                        <input
                            type="range"
                            min="5"
                            max="95"
                            value={posX}
                            onChange={(e) => setPosX(parseFloat(e.target.value))}
                            style={{ width: 90, accentColor: '#38bdf8' }}
                        />
                        <span style={{ fontSize: 11, fontWeight: 600 }}>{posX.toFixed(1)}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, fontWeight: 700 }}>{t('posYLabel', 'Y (Sud-Nord) :')}</span>
                        <input
                            type="range"
                            min="5"
                            max="95"
                            value={posY}
                            onChange={(e) => setPosY(parseFloat(e.target.value))}
                            style={{ width: 90, accentColor: '#38bdf8' }}
                        />
                        <span style={{ fontSize: 11, fontWeight: 600 }}>{posY.toFixed(1)}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, fontWeight: 700 }}>{t('posZLabel', 'Z (Altitude) :')}</span>
                        <input
                            type="range"
                            min="-2.5"
                            max="15"
                            value={posZ}
                            onChange={(e) => setPosZ(parseFloat(e.target.value))}
                            style={{ width: 80, accentColor: '#38bdf8' }}
                        />
                        <span style={{ fontSize: 11, fontWeight: 600 }}>{posZ.toFixed(1)}m</span>
                    </div>
                </div>

                {/* Action Trigger Buttons */}
                <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
                    <button
                        onClick={handleInstantExecute}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 8,
                            background: '#f59e0b',
                            color: '#000',
                            border: 'none',
                            padding: '9px 18px',
                            borderRadius: 6,
                            fontSize: 13,
                            fontWeight: 800,
                            cursor: 'pointer'
                        }}
                    >
                        <Zap size={16} /> {t('executeNowBtn', '⚡ EXÉCUTER MAINTENANT')}
                    </button>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{ fontSize: 12, color: textMuted }}>{t('inLabel', 'dans')}</span>
                        <input
                            type="number"
                            min="5"
                            step="30"
                            value={targetOffsetSeconds}
                            onChange={(e) => setTargetOffsetSeconds(parseInt(e.target.value) || 30)}
                            style={{ width: 65, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '5px 6px', fontSize: 12 }}
                        />
                        <span style={{ fontSize: 12, color: textMuted }}>{t('inSecondsLabel', 'secondes')}</span>
                        <button
                            onClick={handleScheduleEvent}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 6,
                                background: isDark ? '#334155' : '#e2e8f0',
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                padding: '8px 14px',
                                borderRadius: 6,
                                fontSize: 12,
                                fontWeight: 700,
                                cursor: 'pointer'
                            }}
                        >
                            <Calendar size={14} /> {t('scheduleEventBtn', "📅 Planifier l'événement")}
                        </button>
                    </div>
                </div>
            </div>

            <ScheduledEventsQueueTable
                t={t}
                cardBg={cardBg}
                borderCol={borderCol}
                textMuted={textMuted}
                ticks={ticks}
                scheduledEvents={scheduledEvents}
                togglePauseScheduledEvent={togglePauseScheduledEvent}
                duplicateScheduledEvent={duplicateScheduledEvent}
                removeScheduledEvent={removeScheduledEvent}
            />
        </div>
    )
}
