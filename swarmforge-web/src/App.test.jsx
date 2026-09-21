import { describe, it, expect } from 'vitest'
import React from 'react'
import ReactDOMServer from 'react-dom/server'
import App from './App'
import SimulationControlPanel from './components/SimulationControlPanel'
import GodModePanel from './components/GodModePanel'
import StatisticsDashboardPanel from './components/StatisticsDashboardPanel'
import EventLogPanel from './components/EventLogPanel'
import SettingsPanel from './components/SettingsPanel'
import { useSimulationStore } from './store/simulationStore'

describe('Component Rendering Smoke Tests', () => {
    it('renders App without crashing', () => {
        const html = ReactDOMServer.renderToString(<App />)
        expect(html).toContain('SwarmForge')
    })

    it('renders SimulationControlPanel without ReferenceError', () => {
        const html = ReactDOMServer.renderToString(<SimulationControlPanel />)
        expect(html).toContain('Preset de Monde')
    })

    it('renders GodModePanel without ReferenceError', () => {
        const html = ReactDOMServer.renderToString(<GodModePanel />)
        expect(html).toContain('Interventions Divines')
    })

    it('renders StatisticsDashboardPanel without ReferenceError', () => {
        const html = ReactDOMServer.renderToString(<StatisticsDashboardPanel />)
        expect(html).toContain('Tableau de Bord')
    })

    it('renders EventLogPanel without ReferenceError', () => {
        const html = ReactDOMServer.renderToString(<EventLogPanel />)
        expect(html).toContain('Journal')
    })

    it('renders SettingsPanel without ReferenceError', () => {
        const html = ReactDOMServer.renderToString(<SettingsPanel />)
        expect(html).toContain('Configuration &amp; Préférences')
    })

    it('renders App when switched to VISUAL_3D tab without crashing', () => {
        useSimulationStore.getState().setActiveTab('VISUAL_3D')
        const html = ReactDOMServer.renderToString(<App />)
        expect(html).toBeTruthy()
        expect(html).toContain('SwarmForge')
    })
})
