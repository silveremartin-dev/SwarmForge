import React from 'react'

export default function Navbar({ activeMode, setActiveMode }) {
    const styles = {
        nav: {
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            height: 50,
            background: 'rgba(15, 23, 42, 0.85)',
            backdropFilter: 'blur(16px)',
            borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 20px',
            zIndex: 1000,
            fontFamily: 'system-ui, -apple-system, sans-serif',
        },
        brand: {
            fontSize: 16,
            fontWeight: 800,
            letterSpacing: '0.5px',
            background: 'linear-gradient(135deg, #38bdf8 0%, #818cf8 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            display: 'flex',
            alignItems: 'center',
            gap: 8,
        },
        modeGroup: {
            display: 'flex',
            gap: 6,
            background: 'rgba(0, 0, 0, 0.4)',
            padding: 4,
            borderRadius: 8,
            border: '1px solid rgba(255, 255, 255, 0.08)',
        },
        modeBtn: (active, activeColor) => ({
            padding: '6px 14px',
            fontSize: 12,
            fontWeight: 700,
            border: 'none',
            borderRadius: 6,
            cursor: 'pointer',
            background: active ? activeColor : 'transparent',
            color: active ? '#ffffff' : '#94a3b8',
            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
            boxShadow: active ? '0 2px 8px rgba(0,0,0,0.3)' : 'none',
        }),
    }

    return (
        <div style={styles.nav}>
            <div style={styles.brand}>
                <span>🐜 Studio SwarmForge</span>
            </div>

            <div style={styles.modeGroup}>
                <button
                    style={styles.modeBtn(activeMode === 'WORLD_EDITOR', '#0284c7')}
                    onClick={() => setActiveMode('WORLD_EDITOR')}
                >
                    🌍 Éditeur de Monde
                </button>
                <button
                    style={styles.modeBtn(activeMode === 'CLIMATE_STUDIO', '#d97706')}
                    onClick={() => setActiveMode('CLIMATE_STUDIO')}
                >
                    ⛅ Studio Climat
                </button>
                <button
                    style={styles.modeBtn(activeMode === 'SIMULATION', '#16a34a')}
                    onClick={() => setActiveMode('SIMULATION')}
                >
                    🚀 Vue Simulation Live
                </button>
            </div>

            <div style={{ fontSize: 11, color: '#64748b', fontWeight: 500 }}>
                Précision: <span style={{ color: '#38bdf8' }}>0.1 - 1.0mm Sub-Millimétrique</span>
            </div>
        </div>
    )
}
