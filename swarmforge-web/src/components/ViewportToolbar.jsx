import React, { useState, useRef, useEffect } from 'react'
import { Camera, Video, Square, Download, Play, Pause, Eye, Biohazard, ShieldAlert } from 'lucide-react'
import { showToast } from '../store/toastStore'
import { useSimulationStore } from '../store/simulationStore'
import { soundEngine } from '../utils/soundEngine'

export default function ViewportToolbar({ canvasRef }) {
    const { phantomNestsVisible, togglePhantomNests, triggerEpidemic, cureEpidemic, diseaseParams } = useSimulationStore()
    const [recording, setRecording] = useState(false)
    const [recordingPaused, setRecordingPaused] = useState(false)
    const [recordTime, setRecordTime] = useState(0)
    const [recordedChunks, setRecordedChunks] = useState([])
    const mediaRecorderRef = useRef(null)
    const timerIntervalRef = useRef(null)

    // Format seconds to mm:ss
    const formatTime = (secs) => {
        const m = Math.floor(secs / 60).toString().padStart(2, '0')
        const s = (secs % 60).toString().padStart(2, '0')
        return `${m}:${s}`
    }

    // Take High-Res Photo Screenshot
    const takePhotoSnapshot = () => {
        const canvas = document.querySelector('canvas')
        if (!canvas) {
            showToast('Canvas 3D non disponible pour la capture d\'écran.', 'error')
            return
        }

        try {
            const dataUrl = canvas.toDataURL('image/png')
            const link = document.createElement('a')
            const timestamp = new Date().toISOString().replace(/[:.]/g, '-')
            link.download = `swarmforge_snapshot_${timestamp}.png`
            link.href = dataUrl
            link.click()
            showToast('📸 Capture photo HD 3D enregistrée avec succès !', 'success')
        } catch (e) {
            console.error('Snapshot failed:', e)
            showToast('Erreur lors de la capture d\'image: ' + e.message, 'error')
        }
    }

    // Start Recording 3D Video Stream
    const startVideoRecording = () => {
        const canvas = document.querySelector('canvas')
        if (!canvas) {
            showToast('Canvas 3D introuvable.', 'error')
            return
        }

        try {
            const stream = canvas.captureStream(60) // 60 FPS
            let options = { mimeType: 'video/webm;codecs=vp9' }
            if (!MediaRecorder.isTypeSupported(options.mimeType)) {
                options = { mimeType: 'video/webm' }
            }

            const mediaRecorder = new MediaRecorder(stream, options)
            mediaRecorderRef.current = mediaRecorder
            setRecordedChunks([])

            mediaRecorder.ondataavailable = (event) => {
                if (event.data && event.data.size > 0) {
                    setRecordedChunks(prev => [...prev, event.data])
                }
            }

            mediaRecorder.onstop = () => {
                clearInterval(timerIntervalRef.current)
            }

            mediaRecorder.start(100) // Collect 100ms chunks
            setRecording(true)
            setRecordingPaused(false)
            setRecordTime(0)

            timerIntervalRef.current = setInterval(() => {
                setRecordTime(t => t + 1)
            }, 1000)

            showToast('🎥 Enregistrement vidéo 3D démarré', 'info')
        } catch (err) {
            console.error('Video recording failed:', err)
            showToast('Erreur lors du démarrage de l\'enregistrement: ' + err.message, 'error')
        }
    }

    // Pause / Resume Video Recording
    const togglePauseRecording = () => {
        if (!mediaRecorderRef.current) return
        if (recordingPaused) {
            mediaRecorderRef.current.resume()
            setRecordingPaused(false)
            timerIntervalRef.current = setInterval(() => {
                setRecordTime(t => t + 1)
            }, 1000)
            showToast('▶ Enregistrement vidéo repris', 'info')
        } else {
            mediaRecorderRef.current.pause()
            setRecordingPaused(true)
            clearInterval(timerIntervalRef.current)
            showToast('⏸ Enregistrement vidéo suspendu', 'info')
        }
    }

    // Stop & Export Video File (MP4/WebM)
    const stopAndExportVideo = () => {
        if (!mediaRecorderRef.current) return
        mediaRecorderRef.current.stop()
        clearInterval(timerIntervalRef.current)
        setRecording(false)

        setTimeout(() => {
            if (recordedChunks.length === 0) {
                return
            }
            exportBlob(recordedChunks)
        }, 300)
    }

    const exportBlob = (chunks) => {
        const blob = new Blob(chunks, { type: 'video/webm' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.style.display = 'none'
        a.href = url
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-')
        a.download = `swarmforge_simulation_video_${timestamp}.mp4`
        document.body.appendChild(a)
        a.click()
        setTimeout(() => {
            document.body.removeChild(a)
            window.URL.revokeObjectURL(url)
        }, 100)
        showToast('🎬 Vidéo 3D exportée avec succès au format MP4 !', 'success')
    }

    useEffect(() => {
        if (!recording && recordedChunks.length > 0) {
            exportBlob(recordedChunks)
        }
    }, [recording, recordedChunks])

    const handleEpidemicToggle = () => {
        if (diseaseParams.activeOutbreak) {
            cureEpidemic()
            showToast('💉 Soin fongicide appliqué: Épidémie stoppée', 'info')
        } else {
            triggerEpidemic('CORDYCEPS', 4)
            soundEngine.triggerDiseaseOutbreakSound()
            showToast('☣️ ÉPIDÉMIE DE CORDYCEPS DÉCLENCHÉE !', 'warn')
        }
    }

    return (
        <div style={{
            position: 'absolute',
            bottom: 20,
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 1000,
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            background: 'var(--bg-panel, rgba(15, 23, 42, 0.85))',
            backdropFilter: 'blur(12px)',
            border: '1px solid var(--bg-panel-border, rgba(255, 255, 255, 0.15))',
            padding: '8px 16px',
            borderRadius: 30,
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
            color: '#fff',
            fontFamily: 'var(--font-family, system-ui, sans-serif)'
        }}>
            {/* Phantom / Ghost Nests Overlay Toggle Button */}
            <button
                onClick={togglePhantomNests}
                title="Activer/Désactiver l'affichage Fantomatique (Hologramme) des Nids"
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 6,
                    padding: '6px 12px',
                    background: phantomNestsVisible ? 'rgba(56, 189, 248, 0.25)' : 'rgba(255, 255, 255, 0.05)',
                    border: `1px solid ${phantomNestsVisible ? '#38bdf8' : 'rgba(255, 255, 255, 0.15)'}`,
                    color: phantomNestsVisible ? '#38bdf8' : '#94a3b8',
                    borderRadius: 20,
                    fontSize: 12,
                    fontWeight: 700,
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                }}
            >
                <Eye size={15} />
                <span>{phantomNestsVisible ? '👻 Fantomatique Activé' : '👻 Fantomatique Masqué'}</span>
            </button>

            {/* Disease Epidemic Outbreak Quick Action Button */}
            <button
                onClick={handleEpidemicToggle}
                title={diseaseParams.activeOutbreak ? "Éradiquer l'épidémie avec un fongicide" : "Déclencher une épidémie de Cordyceps"}
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 6,
                    padding: '6px 12px',
                    background: diseaseParams.activeOutbreak ? 'rgba(239, 68, 68, 0.3)' : 'rgba(163, 230, 53, 0.15)',
                    border: `1px solid ${diseaseParams.activeOutbreak ? '#ef4444' : '#84cc16'}`,
                    color: diseaseParams.activeOutbreak ? '#f87171' : '#a3e635',
                    borderRadius: 20,
                    fontSize: 12,
                    fontWeight: 700,
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                }}
            >
                <Biohazard size={15} />
                <span>{diseaseParams.activeOutbreak ? '☣️ Soigner Épidémie' : '☣️ Injecter Pathogène'}</span>
            </button>

            <div style={{ width: 1, height: 20, background: 'rgba(255,255,255,0.2)' }} />

            {/* Photo Capture Button */}
            <button
                onClick={takePhotoSnapshot}
                title="Capturer une Photo HD (PNG)"
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 6,
                    padding: '6px 12px',
                    background: 'rgba(56, 189, 248, 0.2)',
                    border: '1px solid #38bdf8',
                    color: '#38bdf8',
                    borderRadius: 20,
                    fontSize: 12,
                    fontWeight: 700,
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                }}
            >
                <Camera size={15} />
                <span>Photo HD</span>
            </button>

            <div style={{ width: 1, height: 20, background: 'rgba(255,255,255,0.2)' }} />

            {/* Video Recording Controls */}
            {!recording ? (
                <button
                    onClick={startVideoRecording}
                    title="Démarrer l'enregistrement vidéo 3D"
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 6,
                        padding: '6px 14px',
                        background: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
                        border: 'none',
                        color: '#fff',
                        borderRadius: 20,
                        fontSize: 12,
                        fontWeight: 700,
                        cursor: 'pointer',
                        boxShadow: '0 2px 10px rgba(239, 68, 68, 0.4)'
                    }}
                >
                    <Video size={15} />
                    <span>Enregistrer Vidéo</span>
                </button>
            ) : (
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: '#f87171', fontWeight: 800 }}>
                        <span style={{
                            width: 10,
                            height: 10,
                            borderRadius: '50%',
                            background: recordingPaused ? '#f59e0b' : '#ef4444',
                            display: 'inline-block',
                            animation: recordingPaused ? 'none' : 'pulse 1s infinite'
                        }} />
                        <span>{recordingPaused ? 'PAUSE' : 'REC'} {formatTime(recordTime)}</span>
                    </div>

                    <button
                        onClick={togglePauseRecording}
                        title={recordingPaused ? 'Reprendre Enregistrement' : 'Mettre en Pause'}
                        style={{
                            padding: 6,
                            background: '#334155',
                            border: '1px solid #475569',
                            color: '#fff',
                            borderRadius: '50%',
                            cursor: 'pointer'
                        }}
                    >
                        {recordingPaused ? <Play size={14} /> : <Pause size={14} />}
                    </button>

                    <button
                        onClick={stopAndExportVideo}
                        title="Arrêter & Exporter Vidéo MP4/WebM"
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 6,
                            padding: '6px 12px',
                            background: '#22c55e',
                            border: 'none',
                            color: '#fff',
                            borderRadius: 20,
                            fontSize: 12,
                            fontWeight: 700,
                            cursor: 'pointer'
                        }}
                    >
                        <Square size={13} fill="#fff" />
                        <span>Stop & Exporter MP4</span>
                    </button>
                </div>
            )}
        </div>
    )
}
