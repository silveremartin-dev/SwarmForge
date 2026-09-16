import React, { useEffect } from 'react'
import { useSimulationStore } from './store/simulationStore'
import Navbar from './components/Navbar'
import SimulationControlPanel from './components/SimulationControlPanel'
import SimulationVisualViewport from './components/SimulationVisualViewport'
import GodModePanel from './components/GodModePanel'
import StatisticsDashboardPanel from './components/StatisticsDashboardPanel'
import EventLogPanel from './components/EventLogPanel'
import SettingsPanel from './components/SettingsPanel'
import ToastContainer from './components/ToastContainer'
import { soundEngine } from './utils/soundEngine'
import { showToast } from './store/toastStore'

export default function App() {
    const {
        activeTab,
        theme,
        running,
        play,
        pause,
        stepForward,
        stepBackward,
        setSpeed,
        setTrackedAntId,
        setFollowAntCamera,
        connect,
        disconnect
    } = useSimulationStore()

    const isDark = theme === 'dark'

    useEffect(() => {
        connect()
        return () => disconnect()
    }, [])

    // WebAudio Auto-Unlock on first user interaction
    useEffect(() => {
        const unlockAudio = () => {
            soundEngine.ensureContext()
            window.removeEventListener('click', unlockAudio)
            window.removeEventListener('keydown', unlockAudio)
            window.removeEventListener('touchstart', unlockAudio)
        }
        window.addEventListener('click', unlockAudio)
        window.addEventListener('keydown', unlockAudio)
        window.addEventListener('touchstart', unlockAudio)
        return () => {
            window.removeEventListener('click', unlockAudio)
            window.removeEventListener('keydown', unlockAudio)
            window.removeEventListener('touchstart', unlockAudio)
        }
    }, [])

    // Global Desktop Client Keyboard Shortcuts Listener
    useEffect(() => {
        const handleKeyDown = (e) => {
            const activeTag = document.activeElement ? document.activeElement.tagName.toLowerCase() : ''
            const isEditing = activeTag === 'input' || activeTag === 'textarea' || activeTag === 'select'
            if (isEditing) return

            // Space: Play/Pause
            if (e.code === 'Space') {
                e.preventDefault()
                if (running) {
                    pause()
                    showToast('⏸️ Simulation mise en pause', 'info')
                } else {
                    play()
                    showToast('▶️ Simulation démarrée', 'info')
                }
                return
            }

            // ArrowRight: Step Forward / Fast Forward
            if (e.code === 'ArrowRight') {
                e.preventDefault()
                stepForward()
                return
            }

            // ArrowLeft: Step Backward / Rewind
            if (e.code === 'ArrowLeft') {
                e.preventDefault()
                stepBackward()
                return
            }

            // Number keys 1-5 for Speed Multipliers
            if (e.key === '1') { setSpeed(1); showToast('Vitesse: 1x', 'info'); }
            else if (e.key === '2') { setSpeed(2); showToast('Vitesse: 2x', 'info'); }
            else if (e.key === '3') { setSpeed(5); showToast('Vitesse: 5x', 'info'); }
            else if (e.key === '4') { setSpeed(10); showToast('Vitesse: 10x', 'info'); }
            else if (e.key === '5') { setSpeed(50); showToast('Vitesse: 50x', 'info'); }

            // Escape: Deselect Ant or Exit Fullscreen
            if (e.code === 'Escape') {
                setTrackedAntId(null)
                setFollowAntCamera(false)
            }
        }

        window.addEventListener('keydown', handleKeyDown)
        return () => window.removeEventListener('keydown', handleKeyDown)
    }, [running, play, pause, stepForward, stepBackward, setSpeed, setTrackedAntId, setFollowAntCamera])

    return (
        <div style={{
            width: '100vw',
            height: '100vh',
            background: isDark ? '#0b0f19' : '#f8fafc',
            overflow: 'hidden',
            display: 'flex',
            flexDirection: 'column',
            fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif'
        }}>
            {/* Top Navigation Bar with Telemetry Banner */}
            <Navbar />

            {/* Non-intrusive Toast Notifications */}
            <ToastContainer />

            {/* Main Content Area: Direct 6 Tabs (1:1 JavaFX Client) */}
            <main style={{ flex: 1, overflow: 'hidden', display: 'flex', position: 'relative' }}>
                {(activeTab === 'SIMULATION' || !['VISUAL_3D', 'GOD_MODE', 'STATISTICS', 'EVENT_LOG', 'SETTINGS'].includes(activeTab)) && (
                    <div style={{ flex: 1, overflowY: 'auto' }}>
                        <SimulationControlPanel />
                    </div>
                )}
                {activeTab === 'VISUAL_3D' && (
                    <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
                        <SimulationVisualViewport />
                    </div>
                )}
                {activeTab === 'GOD_MODE' && (
                    <div style={{ flex: 1, overflowY: 'auto' }}>
                        <GodModePanel />
                    </div>
                )}
                {activeTab === 'STATISTICS' && (
                    <div style={{ flex: 1, overflowY: 'auto' }}>
                        <StatisticsDashboardPanel />
                    </div>
                )}
                {activeTab === 'EVENT_LOG' && (
                    <div style={{ flex: 1, overflowY: 'auto' }}>
                        <EventLogPanel />
                    </div>
                )}
                {activeTab === 'SETTINGS' && (
                    <div style={{ flex: 1, overflowY: 'auto', padding: '24px 32px' }}>
                        <SettingsPanel />
                    </div>
                )}
            </main>
        </div>
    )
}
