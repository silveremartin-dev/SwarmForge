import React, { useState } from 'react';
import { Sliders, Zap, Terminal, Volume2 } from 'lucide-react';
import ControlPanel from './ControlPanel';
import GodModePanel from './GodModePanel';
import EventLogPanel from './EventLogPanel';
import AudioMixerWidget from './AudioMixerWidget';

export default function SimulationLeftSidebar() {
    const [activeTab, setActiveTab] = useState('controls'); // 'controls' | 'godmode' | 'events' | 'audio'

    const tabs = [
        { id: 'controls', label: '⚙️ Contrôles', icon: Sliders },
        { id: 'godmode', label: '👑 Mode Divin', icon: Zap },
        { id: 'events', label: '📜 Événements', icon: Terminal },
        { id: 'audio', label: '🔊 Audio Mixer', icon: Volume2 },
    ];

    const styles = {
        container: {
            position: 'absolute',
            top: 60,
            left: 20,
            width: 380,
            maxHeight: 'calc(100vh - 75px)',
            zIndex: 90,
            display: 'flex',
            flexDirection: 'column',
            gap: 8,
            pointerEvents: 'auto',
        },
        tabBar: {
            display: 'flex',
            background: 'rgba(15, 23, 42, 0.94)',
            backdropFilter: 'blur(16px)',
            border: '1px solid rgba(56, 189, 248, 0.3)',
            borderRadius: 10,
            padding: 4,
            gap: 4,
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
        },
        tabBtn: (active) => ({
            flex: 1,
            padding: '7px 6px',
            fontSize: 11,
            fontWeight: 700,
            border: active ? '1px solid #38bdf8' : '1px solid transparent',
            borderRadius: 6,
            background: active ? '#0284c7' : 'transparent',
            color: active ? '#ffffff' : '#94a3b8',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 4,
            transition: 'all 0.15s ease',
        }),
        contentArea: {
            maxHeight: 'calc(100vh - 135px)',
            overflowY: 'auto',
            borderRadius: 12,
        }
    };

    return (
        <div style={styles.container}>
            {/* Top Navigation Tabs for Left Simulation Sidebar */}
            <div style={styles.tabBar}>
                {tabs.map(tab => {
                    const Icon = tab.icon;
                    const active = activeTab === tab.id;
                    return (
                        <button
                            key={tab.id}
                            style={styles.tabBtn(active)}
                            onClick={() => setActiveTab(tab.id)}
                        >
                            <Icon size={13} />
                            <span>{tab.label.split(' ')[1]}</span>
                        </button>
                    );
                })}
            </div>

            {/* Active Panel View */}
            <div style={styles.contentArea}>
                {activeTab === 'controls' && <ControlPanel inline={true} />}
                {activeTab === 'godmode' && <GodModePanel inline={true} />}
                {activeTab === 'events' && <EventLogPanel />}
                {activeTab === 'audio' && <AudioMixerWidget />}
            </div>
        </div>
    );
}
