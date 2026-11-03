/**
 * SwarmForge Web - Temporal Utilities
 * Formats calendar dates, colonial simulation clock, and relative elapsed time.
 */

/**
 * Formats absolute calendar datetime based on scenario start timestamp and elapsed simulation seconds.
 *
 * @param {string} startDateTimeIso - ISO start datetime (e.g. "2026-03-20T08:00:00")
 * @param {number} simTimeSeconds - Elapsed simulation seconds
 * @returns {string} Formatted string "YYYY-MM-DD HH:MM:SS"
 */
export function formatSimCalendarTime(startDateTimeIso, simTimeSeconds) {
    try {
        const startDate = new Date(startDateTimeIso || '2026-03-20T08:00:00')
        const currentMs = startDate.getTime() + (simTimeSeconds || 0) * 1000
        const curDate = new Date(currentMs)
        const y = curDate.getFullYear()
        const m = String(curDate.getMonth() + 1).padStart(2, '0')
        const d = String(curDate.getDate()).padStart(2, '0')
        const hh = String(curDate.getHours()).padStart(2, '0')
        const mm = String(curDate.getMinutes()).padStart(2, '0')
        const ss = String(curDate.getSeconds()).padStart(2, '0')
        return `${y}-${m}-${d} ${hh}:${mm}:${ss}`
    } catch {
        return '2026-03-20 08:00:00'
    }
}

/**
 * Formats relative simulation time in metric days and time units (e.g. "J+12 14:05:32").
 *
 * @param {number} totalSeconds - Total simulation elapsed seconds
 * @returns {string} Formatted string "J+D HH:MM:SS"
 */
export function formatSimRelativeTime(totalSeconds) {
    const totalSec = Math.floor(Math.max(0, totalSeconds || 0))
    const days = Math.floor(totalSec / 86400)
    const rem = totalSec % 86400
    const hours = Math.floor(rem / 3600)
    const mins = Math.floor((rem % 3600) / 60)
    const secs = Math.floor(rem % 60)
    return `J+${days} ${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}
