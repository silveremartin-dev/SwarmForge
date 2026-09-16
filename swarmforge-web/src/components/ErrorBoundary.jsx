import React from 'react'

export class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props)
        this.state = { hasError: false, error: null, errorInfo: null }
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error }
    }

    componentDidCatch(error, errorInfo) {
        console.error('SwarmForge React Uncaught Error:', error, errorInfo)
        this.setState({ errorInfo })
    }

    render() {
        if (this.state.hasError) {
            return (
                <div style={{
                    width: '100vw',
                    height: '100vh',
                    background: '#0b0f19',
                    color: '#f8fafc',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    padding: 32,
                    fontFamily: 'monospace'
                }}>
                    <div style={{
                        background: '#1e293b',
                        border: '1px solid #ef4444',
                        borderRadius: 12,
                        padding: 24,
                        maxWidth: 800,
                        width: '100%',
                        boxShadow: '0 20px 40px rgba(0,0,0,0.5)'
                    }}>
                        <h2 style={{ color: '#ef4444', marginTop: 0, display: 'flex', alignItems: 'center', gap: 10 }}>
                            ⚠️ Erreur d'Exécution Client
                        </h2>
                        <p style={{ color: '#cbd5e1', fontSize: 14 }}>
                            {this.state.error?.toString()}
                        </p>
                        {this.state.errorInfo?.componentStack && (
                            <pre style={{
                                background: '#0f172a',
                                padding: 12,
                                borderRadius: 6,
                                overflow: 'auto',
                                maxHeight: 250,
                                fontSize: 12,
                                color: '#94a3b8'
                            }}>
                                {this.state.errorInfo.componentStack}
                            </pre>
                        )}
                        <button
                            onClick={() => {
                                localStorage.clear()
                                window.location.reload()
                            }}
                            style={{
                                marginTop: 16,
                                padding: '8px 16px',
                                background: '#38bdf8',
                                color: '#0f172a',
                                fontWeight: 'bold',
                                border: 'none',
                                borderRadius: 6,
                                cursor: 'pointer'
                            }}
                        >
                            🔄 Réinitialiser le cache & Recharger
                        </button>
                    </div>
                </div>
            )
        }

        return this.props.children
    }
}
