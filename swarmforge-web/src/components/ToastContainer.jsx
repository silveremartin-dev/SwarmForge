import React from 'react'
import { useToastStore } from '../store/toastStore'
import { CheckCircle, AlertCircle, Info, X } from 'lucide-react'

export default function ToastContainer() {
    const { toasts, removeToast } = useToastStore()

    if (!toasts || toasts.length === 0) return null

    return (
        <div style={{
            position: 'fixed',
            top: 65,
            left: '50%',
            transform: 'translateX(-50%)',
            zIndex: 9999,
            display: 'flex',
            flexDirection: 'column',
            gap: 8,
            pointerEvents: 'none',
            alignItems: 'center'
        }}>
            {toasts.map(toast => {
                const isSuccess = toast.type === 'success'
                const isError = toast.type === 'error'

                return (
                    <div
                        key={toast.id}
                        style={{
                            pointerEvents: 'auto',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 10,
                            padding: '10px 18px',
                            borderRadius: 24,
                            background: isSuccess
                                ? 'rgba(6, 78, 59, 0.94)'
                                : isError
                                ? 'rgba(127, 29, 29, 0.94)'
                                : 'rgba(15, 23, 42, 0.94)',
                            color: '#fff',
                            border: `1px solid ${isSuccess ? '#10b981' : isError ? '#ef4444' : '#38bdf8'}`,
                            boxShadow: '0 10px 30px rgba(0, 0, 0, 0.6)',
                            backdropFilter: 'blur(12px)',
                            fontSize: 13,
                            fontWeight: 600,
                            animation: 'slideDownFade 0.3s ease-out forwards',
                        }}
                    >
                        {isSuccess && <CheckCircle size={18} color="#34d399" />}
                        {isError && <AlertCircle size={18} color="#f87171" />}
                        {!isSuccess && !isError && <Info size={18} color="#38bdf8" />}
                        <span>{toast.message}</span>
                        <button
                            onClick={() => removeToast(toast.id)}
                            style={{
                                background: 'transparent',
                                border: 'none',
                                color: 'rgba(255,255,255,0.7)',
                                cursor: 'pointer',
                                padding: 2,
                                display: 'flex',
                                alignItems: 'center'
                            }}
                        >
                            <X size={14} />
                        </button>
                    </div>
                )
            })}
        </div>
    )
}
