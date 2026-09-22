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
                {/* 1. Entities & Brood */}
                {activeCategory === 'ENTITIES' && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 14 }}>
                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('actionLabel', 'Action :')}</label>
                            <select
                                value={entityAction}
                                onChange={(e) => setEntityAction(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                <option value="SPAWN">{t('actionSpawn', 'Faire Apparaître (Spawn)')}</option>
                                <option value="KILL">{t('actionKill', 'Éliminer Sélectivement (Kill)')}</option>
                                <option value="EXTINCT">{t('actionExtinct', 'Extinction Totale (Extinct)')}</option>
                                <option value="BROOD_SPAWN">{t('actionBroodSpawn', 'Injecter Couvain (Larves/Œufs)')}</option>
                                <option value="BROOD_KILL">{t('actionBroodKill', 'Détruire Couvain')}</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('targetCasteLabel', 'Caste Cible :')}</label>
                            <select
                                value={caste}
                                onChange={(e) => setCaste(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                <option value="WORKER">{t('casteWorker', 'Ouvrière (Worker)')}</option>
                                <option value="SOLDIER">{t('casteSoldier', 'Soldat (Soldier)')}</option>
                                <option value="QUEEN">{t('casteQueen', 'Reine (Queen)')}</option>
                                <option value="MALE">{t('casteMale', 'Mâle (Male)')}</option>
                                <option value="BROOD_LARVAE">{t('casteBroodLarvae', 'Couvain - Larves')}</option>
                                <option value="BROOD_EGGS">{t('casteBroodEggs', 'Couvain - Œufs')}</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('quantityLabel', 'Quantité :')}</label>
                            <input
                                type="number"
                                min="1"
                                max="500"
                                value={antCount}
                                onChange={(e) => setAntCount(parseInt(e.target.value) || 1)}
                                style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            />
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('targetColonyLabel', 'Colonie Cible :')}</label>
                            <select
                                value={targetColonyId}
                                onChange={(e) => setTargetColonyId(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                {(colonies || []).map(c => (
                                    <option key={c.id} value={c.id}>{c.name}</option>
                                ))}
                            </select>
                        </div>
                    </div>
                )}

                {/* 2. Resources & Complete Food Taxonomy */}
                {activeCategory === 'RESOURCE' && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 14 }}>
                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('trophicCategoryLabel', 'Catégorie Trophique :')}</label>
                            <select
                                value={resourceType}
                                onChange={(e) => setResourceType(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                <option value="SUGAR">{t('trophicSugar', 'Miellat & Glucides (Surface)')}</option>
                                <option value="SEEDS">{t('trophicSeeds', 'Graines de Graminées (Messor)')}</option>
                                <option value="PREY">{t('trophicPrey', "Cadavre d'Insecte (Protéines)")}</option>
                                <option value="WATER">{t('trophicWater', "Point d'Eau / Rosée")}</option>
                                <option value="FUNGI">{t('trophicFungi', 'Mycélium Champignon (Atta)')}</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('biologicalNatureLabel', 'Nature Biologique :')}</label>
                            <select
                                value={foodNature}
                                onChange={(e) => setFoodNature(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                <option value="Nectar & Honeydew">{t('natureSugar', 'Miellat de Pucerons & Nectar Floral')}</option>
                                <option value="Grass Seeds">{t('natureSeeds', 'Graines Végétales Oléagineuses')}</option>
                                <option value="Protein Biomass">{t('naturePrey', 'Biomasse Protéique Animale')}</option>
                                <option value="Fungal Mycelium">{t('natureFungi', 'Champignon Symbiotique')}</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('energyAmountLabel', "Quantité d'Énergie :")}</label>
                            <input
                                type="number"
                                min="10"
                                max="5000"
                                value={resourceAmount}
                                onChange={(e) => setResourceAmount(parseFloat(e.target.value) || 100)}
                                style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            />
                        </div>
                    </div>
                )}

                {/* 3. Disasters */}
                {activeCategory === 'DISASTER' && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 14 }}>
                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('climateDisasterLabel', 'Désastre Climatique :')}</label>
                            <select
                                value={disasterType}
                                onChange={(e) => setDisasterType(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                <option value="WILDFIRE">{t('disasterWildfire', '🔥 Incendie & Feux de Forêt')}</option>
                                <option value="FLOOD">{t('disasterFlood', '🌊 Inondation & Pluie Torrentielle')}</option>
                                <option value="DROUGHT">{t('disasterDrought', '☀️ Sécheresse & Canicule Critique')}</option>
                                <option value="COLD_SNAP">{t('disasterColdSnap', '❄️ Vague de Froid & Gel Substrat')}</option>
                                <option value="EARTHQUAKE">{t('disasterEarthquake', '⛰️ Séisme & Éboulement Souterrain')}</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>
                                {t('intensityLabel', 'Intensité')} ({(disasterIntensity * 100).toFixed(0)}%) :
                            </label>
                            <input
                                type="range"
                                min="0.1"
                                max="1.0"
                                step="0.05"
                                value={disasterIntensity}
                                onChange={(e) => setDisasterIntensity(parseFloat(e.target.value))}
                                style={{ width: '100%', accentColor: '#ef4444' }}
                            />
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('durationMinutesLabel', 'Durée (minutes) :')}</label>
                            <input
                                type="number"
                                min="1"
                                max="300"
                                value={disasterDurationMinutes}
                                onChange={(e) => setDisasterDurationMinutes(parseInt(e.target.value) || 30)}
                                style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            />
                        </div>
                    </div>
                )}

                {/* 4. Abiotic Physical Drivers (Relative Delta vs Absolute Target Mode) */}
                {activeCategory === 'ABIOTIC' && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                            <label style={{ fontSize: 12, fontWeight: 700, color: textMuted }}>{t('abioticModeLabel', 'Mode Abiotique :')}</label>
                            <label style={{ fontSize: 12, display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer' }}>
                                <input
                                    type="radio"
                                    name="abioticMode"
                                    checked={isRelativeAbiotic}
                                    onChange={() => setIsRelativeAbiotic(true)}
                                />
                                {t('modeRelativeDelta', 'Modificateur Relatif (Delta +/-)')}
                            </label>
                            <label style={{ fontSize: 12, display: 'flex', alignItems: 'center', gap: 6, cursor: 'pointer' }}>
                                <input
                                    type="radio"
                                    name="abioticMode"
                                    checked={!isRelativeAbiotic}
                                    onChange={() => setIsRelativeAbiotic(false)}
                                />
                                {t('modeAbsoluteTarget', 'Valeur Absolue Cible')}
                            </label>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 14 }}>
                            {isRelativeAbiotic ? (
                                <>
                                    <div>
                                        <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('deltaTempLabel', 'Δ Température (°C) :')}</label>
                                        <input
                                            type="number"
                                            value={deltaTemp}
                                            onChange={(e) => setDeltaTemp(parseFloat(e.target.value))}
                                            style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                                        />
                                    </div>
                                    <div>
                                        <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('deltaHumidityLabel', 'Δ Hygrométrie (%) :')}</label>
                                        <input
                                            type="number"
                                            value={deltaHumidity}
                                            onChange={(e) => setDeltaHumidity(parseFloat(e.target.value))}
                                            style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                                        />
                                    </div>
                                    <div>
                                        <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('deltaWindLabel', 'Δ Vent (m/s) :')}</label>
                                        <input
                                            type="number"
                                            value={deltaWind}
                                            onChange={(e) => setDeltaWind(parseFloat(e.target.value))}
                                            style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                                        />
                                    </div>
                                </>
                            ) : (
                                <>
                                    <div>
                                        <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('targetTempLabel', 'Température Cible (°C) :')}</label>
                                        <input
                                            type="number"
                                            value={tempCelsius}
                                            onChange={(e) => setTempCelsius(parseFloat(e.target.value))}
                                            style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                                        />
                                    </div>
                                    <div>
                                        <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('targetHumidityLabel', 'Hygrométrie Cible (%) :')}</label>
                                        <input
                                            type="number"
                                            value={humidityPercent}
                                            onChange={(e) => setHumidityPercent(parseFloat(e.target.value))}
                                            style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                                        />
                                    </div>
                                    <div>
                                        <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('targetWindLabel', 'Vitesse Vent (m/s) :')}</label>
                                        <input
                                            type="number"
                                            value={windSpeed}
                                            onChange={(e) => setWindSpeed(parseFloat(e.target.value))}
                                            style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                                        />
                                    </div>
                                </>
                            )}
                            <div>
                                <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('durationMinutesLabel', 'Durée (minutes) :')}</label>
                                <input
                                    type="number"
                                    value={abioticDurationMinutes}
                                    onChange={(e) => setAbioticDurationMinutes(parseInt(e.target.value) || 60)}
                                    style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                                />
                            </div>
                        </div>
                    </div>
                )}

                {/* 5. Pheromones */}
                {activeCategory === 'PHEROMONE' && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 14 }}>
                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('pheromoneTypeLabel', 'Type de Phéromone :')}</label>
                            <select
                                value={pheromoneType}
                                onChange={(e) => setPheromoneType(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                <option value="FOOD_TRAIL">{t('pheroTypeFood', 'Piste Alimentaire (Attracteur)')}</option>
                                <option value="HOME_TRAIL">{t('pheroTypeHome', 'Piste de Retour au Nid')}</option>
                                <option value="ALARM">{t('pheroTypeAlarm', "Signal d'Alarme (Agressivité)")}</option>
                                <option value="QUEEN_SCENT">{t('pheroTypeQueen', 'Odeur Royale (Ralliement)')}</option>
                                <option value="RECRUITMENT">{t('pheroTypeRecruitment', 'Recrutement Massif')}</option>
                                <option value="DISPERSION">{t('pheroTypeDispersion', 'Gomme / Dissolvant de Piste')}</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('concentrationLabel', 'Concentration (0-100) :')}</label>
                            <input
                                type="number"
                                min="10"
                                max="100"
                                value={pheromoneIntensity}
                                onChange={(e) => setPheromoneIntensity(parseFloat(e.target.value))}
                                style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            />
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('actionRadiusLabel', "Rayon d'Action (m) :")}</label>
                            <input
                                type="number"
                                min="1"
                                max="25"
                                value={pheromoneRadius}
                                onChange={(e) => setPheromoneRadius(parseFloat(e.target.value))}
                                style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            />
                        </div>
                    </div>
                )}

                {/* 6. Invasions */}
                {activeCategory === 'INVASION' && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 14 }}>
                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('invadingOrganismLabel', 'Organisme Envahisseur :')}</label>
                            <select
                                value={invasionType}
                                onChange={(e) => setInvasionType(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                <option value="MEGAPONERA">{t('invaderMegaponera', 'Fourmis Légionnaires Pillardes (Megaponera)')}</option>
                                <option value="VARROA">{t('invaderVarroa', 'Acariens Parasites du Couvain (Varroa)')}</option>
                                <option value="LOCUSTS">{t('invaderLocusts', 'Criquets Ravageurs de Biomasse')}</option>
                                <option value="WASP">{t('invaderWasp', 'Guêpe Solitaire Chasseresse (Vespula)')}</option>
                                <option value="SPIDER">{t('invaderSpider', 'Araignée Chasseresse (Salticidae)')}</option>
                                <option value="BEETLE">{t('invaderBeetle', 'Coléoptère Blindé Carnassier')}</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('headcountLabel', 'Effectif :')}</label>
                            <input
                                type="number"
                                min="1"
                                max="100"
                                value={invasionCount}
                                onChange={(e) => setInvasionCount(parseInt(e.target.value) || 1)}
                                style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            />
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('presenceDurationLabel', 'Durée Présence (min) :')}</label>
                            <input
                                type="number"
                                min="5"
                                max="240"
                                value={invasionDurationMinutes}
                                onChange={(e) => setInvasionDurationMinutes(parseInt(e.target.value) || 30)}
                                style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            />
                        </div>
                    </div>
                )}

                {/* 7. Mutations */}
                {activeCategory === 'MUTATION' && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 14 }}>
                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('mutationTypeLabel', 'Type de Mutation :')}</label>
                            <select
                                value={mutationType}
                                onChange={(e) => setMutationType(e.target.value)}
                                style={{ width: '100%', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            >
                                <option value="SPEED_BOOST">{t('mutationSpeed', 'Vitesse de Déplacement (+50%)')}</option>
                                <option value="AGGRESSION">{t('mutationAggression', 'Agressivité & Dégâts de Mandibules (x2)')}</option>
                                <option value="FORAGING">{t('mutationForaging', 'Efficacité & Vitesse de Récolte (+80%)')}</option>
                                <option value="LONGEVITY">{t('mutationLongevity', 'Espérance de Vie Décuplée (x3)')}</option>
                                <option value="THERMAL_RESISTANCE">{t('mutationThermal', 'Résistance Thermique Extrême (-10°C à +50°C)')}</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ fontSize: 12, color: textMuted, display: 'block', marginBottom: 4 }}>{t('effectMultiplierLabel', "Multiplicateur d'Effet :")}</label>
                            <input
                                type="number"
                                min="1.1"
                                max="5.0"
                                step="0.1"
                                value={mutationMultiplier}
                                onChange={(e) => setMutationMultiplier(parseFloat(e.target.value))}
                                style={{ width: '100%', boxSizing: 'border-box', background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '6px 8px', fontSize: 12 }}
                            />
                        </div>
                    </div>
                )}

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

            {/* Scheduled Events Queue Table */}
            <div style={{
                background: cardBg,
                border: `1px solid ${borderCol}`,
                borderRadius: 10,
                padding: '16px',
                display: 'flex',
                flexDirection: 'column',
                gap: 12
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#38bdf8' }}>
                        <Clock size={18} />
                        <span style={{ fontSize: 14, fontWeight: 800 }}>
                            {t('scheduledEventsTitle', "File d'Événements Programmés")} ({scheduledEvents.length})
                        </span>
                    </div>

                    <span style={{ fontSize: 11, color: textMuted }}>
                        {t('currentTickLabel', 'Tick actuel :')} <strong>{ticks}</strong>
                    </span>
                </div>

                {scheduledEvents.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '24px', color: textMuted, fontSize: 12 }}>
                        {t('noScheduledEventsMsg', "Aucun événement planifié dans la file d'attente.")}
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12, textAlign: 'left' }}>
                            <thead>
                                <tr style={{ borderBottom: `2px solid ${borderCol}`, color: textMuted }}>
                                    <th style={{ padding: '8px 10px' }}>{t('tickLabel', 'Tick')}</th>
                                    <th style={{ padding: '8px 10px' }}>{t('colCalendarTime', 'Heure Calendrier')}</th>
                                    <th style={{ padding: '8px 10px' }}>{t('categoryCol', 'Catégorie')}</th>
                                    <th style={{ padding: '8px 10px' }}>{t('colTypeAction', 'Type / Action')}</th>
                                    <th style={{ padding: '8px 10px' }}>{t('colTarget', 'Cible')}</th>
                                    <th style={{ padding: '8px 10px' }}>{t('colDescription', 'Description')}</th>
                                    <th style={{ padding: '8px 10px' }}>{t('colStatus', 'Statut')}</th>
                                    <th style={{ padding: '8px 10px', textAlign: 'right' }}>{t('colActions', 'Actions')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {(scheduledEvents || []).map(evt => {
                                    const isExecuted = evt.executed
                                    const isPaused = evt.paused
                                    return (
                                        <tr key={evt.id} style={{
                                            borderBottom: `1px solid ${borderCol}`,
                                            opacity: isExecuted ? 0.5 : 1.0,
                                            background: isExecuted ? 'transparent' : (isPaused ? 'rgba(239, 68, 68, 0.05)' : 'transparent')
                                        }}>
                                            <td style={{ padding: '8px 10px', fontWeight: 700, color: '#38bdf8' }}>
                                                {evt.targetTick}
                                            </td>
                                            <td style={{ padding: '8px 10px', fontFamily: 'monospace' }}>
                                                {evt.scheduledCalendarTime || '2026-03-20 08:00:00'}
                                            </td>
                                            <td style={{ padding: '8px 10px' }}>
                                                <span style={{
                                                    background: evt.category === 'DISASTER' ? 'rgba(239, 68, 68, 0.2)' : 'rgba(56, 189, 248, 0.2)',
                                                    color: evt.category === 'DISASTER' ? '#ef4444' : '#38bdf8',
                                                    padding: '2px 6px',
                                                    borderRadius: 4,
                                                    fontSize: 10,
                                                    fontWeight: 800
                                                }}>
                                                    {evt.category}
                                                </span>
                                            </td>
                                            <td style={{ padding: '8px 10px', fontWeight: 600 }}>{evt.eventType}</td>
                                            <td style={{ padding: '8px 10px', color: textMuted }}>{evt.colonyTarget}</td>
                                            <td style={{ padding: '8px 10px', maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                                {evt.description}
                                            </td>
                                            <td style={{ padding: '8px 10px' }}>
                                                {isExecuted ? (
                                                    <span style={{ color: '#10b981', fontWeight: 700, fontSize: 11 }}>{t('statusExecuted', '✓ Exécuté')}</span>
                                                ) : isPaused ? (
                                                    <span style={{ color: '#ef4444', fontWeight: 700, fontSize: 11 }}>{t('statusPaused', '⏸ Suspendu')}</span>
                                                ) : (
                                                    <span style={{ color: '#f59e0b', fontWeight: 700, fontSize: 11 }}>{t('statusPending', '⏳ En attente')}</span>
                                                )}
                                            </td>
                                            <td style={{ padding: '8px 10px', textAlign: 'right' }}>
                                                <div style={{ display: 'inline-flex', gap: 6 }}>
                                                    {!isExecuted && (
                                                        <button
                                                            onClick={() => togglePauseScheduledEvent(evt.id)}
                                                            title={isPaused ? t('btnResumeEvent', 'Reprendre') : t('btnPauseEvent', 'Suspendre')}
                                                            style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: textMuted }}
                                                        >
                                                            {isPaused ? <Play size={14} color="#10b981" /> : <Pause size={14} color="#ef4444" />}
                                                        </button>
                                                    )}
                                                    <button
                                                        onClick={() => duplicateScheduledEvent(evt.id, 100)}
                                                        title={t('btnDuplicate100', 'Dupliquer (+100 ticks)')}
                                                        style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: textMuted }}
                                                    >
                                                        <Copy size={14} color="#38bdf8" />
                                                    </button>
                                                    <button
                                                        onClick={() => {
                                                            if (window.confirm(t('confirmDeleteEvent', 'Confirmer la suppression de cet événement ?'))) {
                                                                removeScheduledEvent(evt.id)
                                                            }
                                                        }}
                                                        title={t('btnDeleteEvent', "Supprimer l'événement")}
                                                        style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: '#ef4444' }}
                                                    >
                                                        <Trash2 size={14} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    )
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    )
}
