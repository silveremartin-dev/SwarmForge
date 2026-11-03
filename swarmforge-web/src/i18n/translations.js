// SwarmForge Web i18n Translation Dictionary
// Modular 5-Language Support: French (fr), English (en), German (de), Spanish (es), Chinese (zh)

import { fr } from './locales/fr.js'
import { en } from './locales/en.js'
import { de } from './locales/de.js'
import { es } from './locales/es.js'
import { zh } from './locales/zh.js'

export const DEFAULT_LANGUAGE = 'fr'

export const SUPPORTED_LANGUAGES = [
    { code: 'fr', name: 'FranÃ§ais', flag: 'ðŸ‡«ðŸ‡·' },
    { code: 'en', name: 'English', flag: 'ðŸ‡¬ðŸ‡§' },
    { code: 'de', name: 'Deutsch', flag: 'ðŸ‡©ðŸ‡ª' },
    { code: 'es', name: 'EspaÃ±ol', flag: 'ðŸ‡ªðŸ‡¸' },
    { code: 'zh', name: 'ä¸­æ–‡ (Mandarin)', flag: 'ðŸ‡¨ðŸ‡³' }
]

export const translations = {
    fr,
    en,
    de,
    es,
    zh
}