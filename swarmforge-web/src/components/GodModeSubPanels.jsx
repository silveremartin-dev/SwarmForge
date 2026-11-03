import React from 'react'
import {
    Copy,
    Trash2,
    Play,
    Pause,
    Clock
} from 'lucide-react'

export function GodModeCategorySubForms({
    activeCategory,
    t,
    inputBg,
    textMain,
    textMuted,
    borderCol,
    entityAction,
    setEntityAction,
    caste,
    setCaste,
    antCount,
    setAntCount,
    targetColonyId,
    setTargetColonyId,
    colonies,
    resourceType,
    setResourceType,
    foodNature,
    setFoodNature,
    resourceAmount,
    setResourceAmount,
    disasterType,
    setDisasterType,
    disasterIntensity,
    setDisasterIntensity,
    disasterDurationMinutes,
    setDisasterDurationMinutes,
    isRelativeAbiotic,
    setIsRelativeAbiotic,
    deltaTemp,
    setDeltaTemp,
    deltaHumidity,
    setDeltaHumidity,
    deltaWind,
    setDeltaWind,
    tempCelsius,
    setTempCelsius,
    humidityPercent,
    setHumidityPercent,
    windSpeed,
    setWindSpeed,
    abioticDurationMinutes,
    setAbioticDurationMinutes,
    pheromoneType,
    setPheromoneType,
    pheromoneIntensity,
    setPheromoneIntensity,
    pheromoneRadius,
    setPheromoneRadius,
    invasionType,
    setInvasionType,
    invasionCount,
    setInvasionCount,
    invasionDurationMinutes,
    setInvasionDurationMinutes,
    mutationType,
    setMutationType,
    mutationMultiplier,
    setMutationMultiplier
}) {
    return (
        <>
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

            {/* 2. Resources & Food */}
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

            {/* 4. Abiotic Physical Drivers */}
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
        </>
    )
}

export { ScheduledEventsQueueTable } from './ScheduledEventsQueueTable'
