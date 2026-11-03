import React, { useState, useEffect, useRef } from 'react'
import {
    Maximize,
    Minimize,
    Camera,
    Video
} from 'lucide-react'
import { showToast } from '../store/toastStore'

export default function SimulationMediaSection({ t, isDark, onTriggerFlash }) {
    const [isFullscreen, setIsFullscreen] = useState(false)
    const [isRecording, setIsRecording] = useState(false)
    const [recSeconds, setRecSeconds] = useState(0)
    const [photoSaved, setPhotoSaved] = useState(false)

    const mediaRecorderRef = useRef(null)
    const recordedChunksRef = useRef([])

    useEffect(() => {
        const handleFullscreenChange = () => {
            setIsFullscreen(!!document.fullscreenElement)
        }
        document.addEventListener('fullscreenchange', handleFullscreenChange)
        return () => document.removeEventListener('fullscreenchange', handleFullscreenChange)
    }, [])

    const toggleFullscreen = () => {
        if (!document.fullscreenElement) {
            document.documentElement.requestFullscreen().catch(() => {})
        } else {
            if (document.exitFullscreen) {
                document.exitFullscreen().catch(() => {})
            }
        }
    }

    const handleScreenshot = () => {
        try {
            if (onTriggerFlash) onTriggerFlash()
            const canvas = document.querySelector('canvas')
            if (canvas) {
                const dataUrl = canvas.toDataURL('image/png')
                const a = document.createElement('a')
                a.href = dataUrl
                a.download = `swarmforge_hd_${Date.now()}.png`
                a.click()
                setPhotoSaved(true)
                showToast('📸 Capture d\'écran HD enregistrée (PNG) !', 'success')
                setTimeout(() => setPhotoSaved(false), 3000)
            }
        } catch (e) {
            showToast('Erreur lors de la capture d\'écran', 'error')
        }
    }

    useEffect(() => {
        let interval = null
        if (isRecording) {
            interval = setInterval(() => {
                setRecSeconds(s => s + 1)
            }, 1000)
        } else {
            setRecSeconds(0)
        }
        return () => clearInterval(interval)
    }, [isRecording])

    const toggleVideoRecording = () => {
        if (isRecording) {
            try {
                if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
                    mediaRecorderRef.current.stop()
                }
            } catch (e) {}
            setIsRecording(false)
        } else {
            try {
                const canvas = document.querySelector('canvas')
                if (!canvas) {
                    showToast('Impossible de trouver le canvas 3D pour la capture vidéo', 'error')
                    return
                }

                const stream = canvas.captureStream(30)
                recordedChunksRef.current = []

                const mimeType = MediaRecorder.isTypeSupported('video/webm;codecs=vp9')
                    ? 'video/webm;codecs=vp9'
                    : (MediaRecorder.isTypeSupported('video/webm') ? 'video/webm' : '')

                const recorder = new MediaRecorder(stream, mimeType ? { mimeType } : {})
                recorder.ondataavailable = (event) => {
                    if (event.data && event.data.size > 0) {
                        recordedChunksRef.current.push(event.data)
                    }
                }
                recorder.onstop = () => {
                    const blob = new Blob(recordedChunksRef.current, { type: 'video/webm' })
                    const url = URL.createObjectURL(blob)
                    const a = document.createElement('a')
                    a.href = url
                    a.download = `swarmforge_recording_${Date.now()}.webm`
                    a.click()
                    URL.revokeObjectURL(url)
                    showToast(`🎥 Clip vidéo 3D enregistré (${recSeconds}s) !`, 'success')
                }

                recorder.start()
                mediaRecorderRef.current = recorder
                setIsRecording(true)
                showToast('🔴 Enregistrement vidéo 3D démarré...', 'info')
            } catch (e) {
                showToast('Erreur lors de l\'enregistrement vidéo WebGL: ' + e.message, 'error')
            }
        }
    }

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6, background: isDark ? 'rgba(0,0,0,0.25)' : 'rgba(0,0,0,0.04)', padding: 8, borderRadius: 8 }}>
            <span style={{ fontSize: 10, fontWeight: 700, color: '#a78bfa' }}>{t('mediaTitle', 'MÉDIA & CAPTURES 3D')}</span>

            <button
                onClick={toggleFullscreen}
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 6,
                    background: '#38bdf8',
                    color: '#0f172a',
                    border: 'none',
                    borderRadius: 6,
                    padding: '6px 10px',
                    fontSize: 11,
                    fontWeight: 800,
                    cursor: 'pointer'
                }}
            >
                {isFullscreen ? <Minimize size={13} /> : <Maximize size={13} />}
                <span>{isFullscreen ? t('exitFullscreenBtn', 'Quitter Plein Écran (ESC)') : t('fullscreenBtn', 'Mode Plein Écran (F11)')}</span>
            </button>

            <button
                onClick={handleScreenshot}
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 6,
                    background: photoSaved ? '#22c55e' : (isDark ? '#334155' : '#e2e8f0'),
                    color: photoSaved ? '#ffffff' : (isDark ? '#f1f5f9' : '#0f172a'),
                    border: 'none',
                    borderRadius: 6,
                    padding: '6px 10px',
                    fontSize: 11,
                    fontWeight: 700,
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                }}
            >
                <Camera size={13} />
                <span>{photoSaved ? t('screenshotSaved', '✓ Screenshot Enregistré !') : t('screenshotBtn', 'Capture d\'écran HD (PNG)')}</span>
            </button>

            <button
                onClick={toggleVideoRecording}
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 6,
                    background: isRecording ? '#ef4444' : (isDark ? '#334155' : '#e2e8f0'),
                    color: isRecording ? '#ffffff' : (isDark ? '#f1f5f9' : '#0f172a'),
                    border: 'none',
                    borderRadius: 6,
                    padding: '6px 10px',
                    fontSize: 11,
                    fontWeight: 700,
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                }}
            >
                <Video size={13} />
                <span>
                    {isRecording
                        ? `${t('videoStopBtn', '🔴 Arrêter REC')} (${Math.floor(recSeconds / 60).toString().padStart(2, '0')}:${(recSeconds % 60).toString().padStart(2, '0')})`
                        : t('videoRecBtn', 'Enregistrement Vidéo 3D (WebM)')}
                </span>
            </button>
        </div>
    )
}
