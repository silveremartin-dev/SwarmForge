import { create } from 'zustand'

export const useToastStore = create((set) => ({
    toasts: [],
    addToast: (message, type = 'success', duration = 3500) => {
        const id = Date.now() + Math.random()
        set(state => ({
            toasts: [...state.toasts, { id, message, type }]
        }))
        setTimeout(() => {
            set(state => ({
                toasts: state.toasts.filter(t => t.id !== id)
            }))
        }, duration)
    },
    removeToast: (id) => {
        set(state => ({
            toasts: state.toasts.filter(t => t.id !== id)
        }))
    }
}))

export const showToast = (message, type = 'success', duration = 3500) => {
    useToastStore.getState().addToast(message, type, duration)
}
