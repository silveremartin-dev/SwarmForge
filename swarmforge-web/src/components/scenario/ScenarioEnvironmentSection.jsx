import React from 'react'
import { Save, Trash2, Download, Upload } from 'lucide-react'
import { useSimulationStore } from '../../store/simulationStore'
import {
    DEFAULT_WORLD_PRESETS,
    DEFAULT_WEATHER_PRESETS,
    DEFAULT_SCENARIO_META_PRESETS
} from '../../store/presetStore'
import { getTranslation } from '../../i18n/translations'
import { showToast } from '../../store/toastStore'

/**
 * Biotope world preset, simulated/real weather climate, start date/time, and RNG seed section.
 */
export default function ScenarioEnvironmentSection({ isDark, borderCol, inputBg, textMain, textMuted }) {
    const {
        selectedScenarioPresetId,
        setScenarioPresetId,
        selectedWorldPresetId,
        setWorldPresetId,
        selectedWeatherPresetId,
        setWeatherPresetId,
        masterSeed,
        setMasterSeed,
        scenarioDescription,
        setScenarioDescription,
        startDateTime,
        setStartDateTime,
        exportScenarioJson,
        importScenarioJson,
        isRealWeatherMode,
        realWeatherCity,
        realWeatherStatus,
        setRealWeatherMode,
        setRealWeatherCity,
        fetchRealWeather,
        language
    } = useSimulationStore()

    const t = (key, fallback) => getTranslation(language, key, fallback)

    const dtObj = new Date(startDateTime || '2026-03-20T08:00:00')
    const curDateStr = dtObj.toISOString().split('T')[0]
    const curHour = dtObj.getHours()
    const curMin = dtObj.getMinutes()
    const curSec = dtObj.getSeconds()

    const handleDateChange = (newDateStr) => {
        const [y, m, d] = newDateStr.split('-').map(Number)
        const updated = new Date(dtObj)
        if (!isNaN(y) && !isNaN(m) && !isNaN(d)) {
            updated.setFullYear(y, m - 1, d)
            setStartDateTime(updated.toISOString().slice(0, 19))
        }
    }

    const handleTimeChange = (h, m, s) => {
        const updated = new Date(dtObj)
        updated.setHours(h ?? curHour, m ?? curMin, s ?? curSec)
        setStartDateTime(updated.toISOString().slice(0, 19))
    }

    return (
        <>
            {/* Global Preset Selector */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('globalScenarioPresetLabel', 'Méta-Preset de Scénario Global :')}</span>
                <select
                    value={selectedScenarioPresetId}
                    onChange={(e) => setScenarioPresetId(e.target.value)}
                    style={{
                        flex: 1,
                        background: inputBg,
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 6,
                        padding: '7px 12px',
                        fontSize: 12,
                        fontWeight: 600
                    }}
                >
                    {DEFAULT_SCENARIO_META_PRESETS.map(p => (
                        <option key={p.id} value={p.id}>{p.name} ({p.academicCategory})</option>
                    ))}
                </select>
            </div>

            {/* Preset Actions Buttons */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <button
                    onClick={() => showToast('✓ Configuration du scénario enregistrée.', 'success')}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 5,
                        background: isDark ? '#1e293b' : '#e2e8f0',
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 6,
                        padding: '6px 14px',
                        fontSize: 11,
                        fontWeight: 700,
                        cursor: 'pointer'
                    }}
                >
                    <Save size={13} /> {t('btnSave', 'Enregistrer')}
                </button>

                <button
                    onClick={() => showToast('Scénario réinitialisé par défaut.', 'info')}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 5,
                        background: '#dc2626',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: 6,
                        padding: '6px 14px',
                        fontSize: 11,
                        fontWeight: 700,
                        cursor: 'pointer'
                    }}
                >
                    <Trash2 size={13} /> {t('btnDelete', 'Supprimer')}
                </button>

                <button
                    onClick={exportScenarioJson}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 5,
                        background: isDark ? '#1e293b' : '#e2e8f0',
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 6,
                        padding: '6px 14px',
                        fontSize: 11,
                        fontWeight: 700,
                        cursor: 'pointer'
                    }}
                >
                    <Download size={13} /> {t('btnExport', 'Exporter...')}
                </button>

                <label
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 5,
                        background: isDark ? '#1e293b' : '#e2e8f0',
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 6,
                        padding: '6px 14px',
                        fontSize: 11,
                        fontWeight: 700,
                        cursor: 'pointer'
                    }}
                >
                    <Upload size={13} /> {t('btnImport', 'Importer...')}
                    <input
                        type="file"
                        accept=".json"
                        style={{ display: 'none' }}
                        onChange={(e) => {
                            const file = e.target.files?.[0]
                            if (file) {
                                const reader = new FileReader()
                                reader.onload = (ev) => {
                                    const success = importScenarioJson(ev.target?.result)
                                    if (success) showToast('✓ Scénario importé avec succès !', 'success')
                                    else showToast('Erreur lors de l\'import JSON', 'error')
                                }
                                reader.readAsText(file)
                            }
                        }}
                    />
                </label>
            </div>

            {/* Scenario Description */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>{t('scientificDescLabel', 'Description Scientifique du Scénario :')}</span>
                <textarea
                    value={scenarioDescription}
                    onChange={(e) => setScenarioDescription(e.target.value)}
                    rows={3}
                    style={{
                        width: '100%',
                        background: inputBg,
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 6,
                        padding: '8px 10px',
                        fontSize: 11,
                        fontFamily: 'inherit',
                        resize: 'vertical',
                        boxSizing: 'border-box'
                    }}
                />
            </div>

            {/* 1. World Preset */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('worldPresetSectionLabel', '1. Biotope & Monde (World Preset) :')}</span>
                <select
                    value={selectedWorldPresetId}
                    onChange={(e) => setWorldPresetId(e.target.value)}
                    style={{
                        flex: 1,
                        background: inputBg,
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 6,
                        padding: '6px 10px',
                        fontSize: 12,
                        fontWeight: 600
                    }}
                >
                    {DEFAULT_WORLD_PRESETS.map(w => (
                        <option key={w.id} value={w.id}>{w.name}</option>
                    ))}
                </select>
            </div>

            {/* 2. Weather & Climate Preset */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('weatherPresetSectionLabel', '2. Profil Météo & Climat :')}</span>

                <div style={{ display: 'flex', borderRadius: 5, overflow: 'hidden', border: `1px solid ${borderCol}` }}>
                    <button
                        onClick={() => setRealWeatherMode(false)}
                        style={{
                            padding: '4px 10px',
                            fontSize: 11,
                            fontWeight: 700,
                            border: 'none',
                            background: !isRealWeatherMode ? '#0284c7' : inputBg,
                            color: !isRealWeatherMode ? '#ffffff' : textMuted,
                            cursor: 'pointer'
                        }}
                    >
                        {t('weatherSimulated', 'Simulé')}
                    </button>
                    <button
                        onClick={() => setRealWeatherMode(true)}
                        style={{
                            padding: '4px 10px',
                            fontSize: 11,
                            fontWeight: 700,
                            border: 'none',
                            background: isRealWeatherMode ? '#0284c7' : inputBg,
                            color: isRealWeatherMode ? '#ffffff' : textMuted,
                            cursor: 'pointer'
                        }}
                    >
                        {t('weatherReal', 'Réel')}
                    </button>
                </div>

                {!isRealWeatherMode ? (
                    <>
                        <select
                            value={selectedWeatherPresetId}
                            onChange={(e) => setWeatherPresetId(e.target.value)}
                            style={{
                                flex: 1,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 6,
                                padding: '6px 10px',
                                fontSize: 12,
                                fontWeight: 600
                            }}
                        >
                            {DEFAULT_WEATHER_PRESETS.map(w => (
                                <option key={w.id} value={w.id}>{w.name}</option>
                            ))}
                        </select>

                        <button
                            onClick={() => showToast('✓ Profil météo aligné avec le biome du monde.', 'info')}
                            style={{
                                background: isDark ? '#334155' : '#e2e8f0',
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 6,
                                padding: '5px 12px',
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer'
                            }}
                        >
                            {t('btnAlignWeather', '⛅ Aligner')}
                        </button>
                    </>
                ) : (
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1 }}>
                        <input
                            type="text"
                            value={realWeatherCity}
                            onChange={(e) => setRealWeatherCity(e.target.value)}
                            placeholder={t('realWeatherPlaceholder', 'Ville ou Lat, Lon...')}
                            style={{
                                width: 140,
                                background: inputBg,
                                color: textMain,
                                border: `1px solid ${borderCol}`,
                                borderRadius: 5,
                                padding: '5px 8px',
                                fontSize: 11
                            }}
                        />
                        <button
                            onClick={() => fetchRealWeather(realWeatherCity)}
                            style={{
                                background: '#0284c7',
                                color: '#ffffff',
                                border: 'none',
                                borderRadius: 5,
                                padding: '5px 10px',
                                fontSize: 11,
                                fontWeight: 700,
                                cursor: 'pointer'
                            }}
                        >
                            {t('btnGetWeather', 'Obtenir')}
                        </button>
                        <span style={{ fontSize: 10, color: textMuted }}>{realWeatherStatus}</span>
                    </div>
                )}
            </div>

            {/* 3. Start Date & Time */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('startDateTimeSectionLabel', '3. Date & Heure de Démarrage :')}</span>
                <input
                    type="date"
                    value={curDateStr}
                    onChange={(e) => handleDateChange(e.target.value)}
                    style={{
                        background: inputBg,
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 5,
                        padding: '4px 8px',
                        fontSize: 11,
                        fontWeight: 600
                    }}
                />

                <input
                    type="number"
                    min="0"
                    max="23"
                    value={curHour}
                    onChange={(e) => handleTimeChange(parseInt(e.target.value), curMin, curSec)}
                    style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px', fontSize: 11, textAlign: 'center' }}
                />
                <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>h</span>

                <input
                    type="number"
                    min="0"
                    max="59"
                    value={curMin}
                    onChange={(e) => handleTimeChange(curHour, parseInt(e.target.value), curSec)}
                    style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px', fontSize: 11, textAlign: 'center' }}
                />
                <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>m</span>

                <input
                    type="number"
                    min="0"
                    max="59"
                    value={curSec}
                    onChange={(e) => handleTimeChange(curHour, curMin, parseInt(e.target.value))}
                    style={{ width: 45, background: inputBg, color: textMain, border: `1px solid ${borderCol}`, borderRadius: 4, padding: '4px', fontSize: 11, textAlign: 'center' }}
                />
                <span style={{ fontSize: 11, fontWeight: 700, color: textMuted }}>s</span>
            </div>

            {/* 4. Master Random Seed */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <span style={{ fontSize: 12, fontWeight: 700, minWidth: 160 }}>{t('masterSeedSectionLabel', '4. Graine Aléatoire Maîtresse (RNG Master Seed) :')}</span>
                <input
                    type="number"
                    value={masterSeed}
                    onChange={(e) => setMasterSeed(parseInt(e.target.value))}
                    style={{
                        width: 120,
                        background: inputBg,
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 5,
                        padding: '5px 8px',
                        fontSize: 12,
                        fontWeight: 700
                    }}
                />
                <button
                    onClick={() => {
                        const newSeed = Math.floor(Math.random() * 900000 + 100000)
                        setMasterSeed(newSeed)
                        showToast(`Nouvelle graine générée : ${newSeed}`, 'info')
                    }}
                    style={{
                        background: isDark ? '#334155' : '#e2e8f0',
                        color: textMain,
                        border: `1px solid ${borderCol}`,
                        borderRadius: 5,
                        padding: '5px 12px',
                        fontSize: 11,
                        fontWeight: 700,
                        cursor: 'pointer'
                    }}
                >
                    {t('btnNew', '🎲 Nouvelle')}
                </button>
            </div>
        </>
    )
}
