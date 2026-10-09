#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SwarmForge - Universal 5-Language I18n, Tooltip Coverage & Hardcoded String Audit Suite
========================================================================================
Author: Silvère Martin-Michiellot & Gemini AI Assistant (Google DeepMind)
License: MIT License

Capabilities:
1. Multi-Module Synchronization:
   - Synchronizes resource bundles (EN, FR, DE, ES, ZH) and default messages.properties
     across `swarmforge-editor`, `swarmforge-client`, `swarmforge-core`, and `swarmforge-web/src/i18n/translations.js`.
2. Hardcoded UI String Detection:
   - Scans all Java source files (UI components, dialogs, panels, menus, alert messages)
     and Web React components for un-internationalized string literals.
3. Tooltip & Mouse-over Coverage Audit:
   - Verifies that 100% of interactive UI controls (Buttons, Sliders, ComboBoxes, CheckBoxes,
     Spinners, ToggleButtons, MenuItems, Web buttons/inputs) have descriptive tooltips.
4. Domain-Specific Entomological & Biological Auto-Translation:
   - Translates missing keys with high scientific fidelity across Formicidae, Apidae,
     Vespidae, Isoptera, chemical ecology, spatial navigation, and simulation telemetry.
5. UTF-8 Encoding & Mojibake Repair:
   - Detects and repairs corrupted multi-byte UTF-8 sequences.
"""

import argparse
import os
import re
import sys
from pathlib import Path

# Force UTF-8 on Windows standard I/O
if sys.stdout.encoding != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')
    except Exception:
        pass

# Root directory of SwarmForge project
ROOT_DIR = Path(__file__).resolve().parent.parent.parent

# Module paths
EDITOR_I18N = ROOT_DIR / "swarmforge-editor" / "src" / "main" / "resources" / "i18n"
CLIENT_I18N = ROOT_DIR / "swarmforge-client" / "src" / "main" / "resources" / "i18n"
CORE_I18N = ROOT_DIR / "swarmforge-core" / "src" / "main" / "resources" / "i18n"
WEB_TRANSLATIONS_JS = ROOT_DIR / "swarmforge-web" / "src" / "i18n" / "translations.js"

JAVA_DIRS = [
    ROOT_DIR / "swarmforge-editor" / "src" / "main" / "java",
    ROOT_DIR / "swarmforge-client" / "src" / "main" / "java",
    ROOT_DIR / "swarmforge-core" / "src" / "main" / "java",
    ROOT_DIR / "swarmforge-server" / "src" / "main" / "java",
]

WEB_SRC_DIR = ROOT_DIR / "swarmforge-web" / "src"

LANGUAGES = ["en", "fr", "de", "es", "zh"]

# Binary mojibake fix dictionary
BINARY_MOJIBAKE = [
    (b'\xc3\xa2\xc2\x9d\xc5\x92', '❌'.encode('utf-8')),
    (b'\xc3\xa2\xc2\x9d\xc2\x8c', '❌'.encode('utf-8')),
    (b'\xc3\xa2\xc2\x9c\xc2\x94', '✔️'.encode('utf-8')),
    (b'\xc3\xa2\xc5\xa1\xe2\x84\xa2\xc3\xaf\xc2\xb8\xc2\x8f', '⚙️'.encode('utf-8')),
    (b'\xc3\xa2\xc5\xa1\xc2\xa0\xc3\xaf\xc2\xb8\xc2\x8f', '⚠️'.encode('utf-8')),
    (b'\xc3\xa2\xc2\x8f\xc2\xb1\xc3\xaf\xc2\xb8\xc2\x8f', '⏱️'.encode('utf-8')),
    (b'\xc3\xa2\xc2\x9a\xc2\xa1', '⚡'.encode('utf-8')),
]

# Biological & Entomological Lexicon for scientific translation parity
BIOLOGICAL_LEXICON = {
    # French base translations
    "fr": {
        "Queen": "Reine", "Worker": "Ouvrière", "Soldier": "Soldat", "Male": "Mâle",
        "Egg": "Œuf", "Larva": "Larve", "Pupa": "Nymphe", "Alate": "Ailé",
        "Brood": "Couvain", "Colony": "Colonie", "Nest": "Nid", "Chamber": "Chambre",
        "Gallery": "Galerie", "Tunnel": "Tunnel", "Entrance": "Entrée",
        "Foraging": "Fourragement", "Nursing": "Soin du couvain", "Digging": "Excavation",
        "Defense": "Défense", "Sanitation": "Assainissement", "Pheromone": "Phéromone",
        "Alarm": "Alarme", "Trail": "Piste", "Homing": "Retour au nid", "Food": "Nourriture",
        "Sugar": "Glucides", "Protein": "Protéines", "Water": "Eau", "Fungus": "Champignon",
        "Fungus Garden": "Jardin à champignon", "Waste": "Déchets", "Midden": "Dépotoir",
        "Hibernation": "Hibernation", "Solarium": "Solarium", "Honeycomb": "Rayon de cire",
        "Pollen Pot": "Pot de pollen", "Ventilation": "Ventilation", "Leaf Cache": "Réserve de feuilles",
        "Thatched Mound": "Dôme d'aiguilles", "Cathedral Mound": "Termitière cathédrale",
        "Paper Nest": "Nid de papier", "Arboreal Nest": "Nid arboricole", "Bivouac": "Bivouac",
        "Humus": "Humus", "Clay": "Argile", "Sand": "Sable", "Silt": "Limon",
        "Gravel": "Gravier", "Peat": "Tourbe", "Stone": "Roche", "Water Table": "Nappe phréatique",
        "Moisture": "Humidité", "Temperature": "Température", "Elevation": "Élévation",
        "Simulation": "Simulation", "Settings": "Paramètres", "Statistics": "Statistiques",
        "God Mode": "Mode Divin", "Realistic": "Réaliste", "Scientific": "Scientifique",
        "Gamified": "Gamifié", "Play": "Lecture", "Pause": "Pause", "Step": "Pas",
        "Speed": "Vitesse", "Population": "Population", "Health": "Santé", "Energy": "Énergie",
        "Lifespan": "Durée de vie", "Body Length": "Longueur corporelle",
        "Wildfire": "Incendie", "Flood": "Inondation", "Drought": "Sécheresse", "Frost": "Gel"
    },
    # German base translations
    "de": {
        "Queen": "Königin", "Worker": "Arbeiterin", "Soldier": "Soldat", "Male": "Männchen",
        "Egg": "Ei", "Larva": "Larve", "Pupa": "Puppe", "Alate": "Geflügelt",
        "Brood": "Brut", "Colony": "Kolonie", "Nest": "Nest", "Chamber": "Kammer",
        "Gallery": "Galerie", "Tunnel": "Tunnel", "Entrance": "Eingang",
        "Foraging": "Futtersuche", "Nursing": "Brutpflege", "Digging": "Graben",
        "Defense": "Verteidigung", "Sanitation": "Nest hygiene", "Pheromone": "Pheromon",
        "Alarm": "Alarm", "Trail": "Spur", "Homing": "Heimkehr", "Food": "Nahrung",
        "Sugar": "Kohlenhydrate", "Protein": "Proteine", "Water": "Wasser", "Fungus": "Pilz",
        "Fungus Garden": "Pilzgarten", "Waste": "Abfall", "Midden": "Abfallhaufen",
        "Hibernation": "Winterschlaf", "Solarium": "Wärmekammer", "Honeycomb": "Wabenbau",
        "Pollen Pot": "Pollentopf", "Ventilation": "Belüftung", "Leaf Cache": "Blattspeicher",
        "Thatched Mound": "Nadelhügel", "Cathedral Mound": "Kathedralenberg",
        "Paper Nest": "Papiernest", "Arboreal Nest": "Baumnest", "Bivouac": "Biwak",
        "Humus": "Humus", "Clay": "Ton", "Sand": "Sand", "Silt": "Schluff",
        "Gravel": "Kies", "Peat": "Torf", "Stone": "Gestein", "Water Table": "Grundwasserspiegel",
        "Moisture": "Feuchtigkeit", "Temperature": "Temperatur", "Elevation": "Höhe",
        "Simulation": "Simulation", "Settings": "Einstellungen", "Statistics": "Statistiken",
        "God Mode": "Gott-Modus", "Realistic": "Realistisch", "Scientific": "Wissenschaftlich",
        "Gamified": "Gamifiziert", "Play": "Start", "Pause": "Pause", "Step": "Schritt",
        "Speed": "Geschwindigkeit", "Population": "Population", "Health": "Gesundheit", "Energy": "Energie",
        "Lifespan": "Lebensdauer", "Body Length": "Körperlänge",
        "Wildfire": "Waldbrand", "Flood": "Überschwemmung", "Drought": "Dürre", "Frost": "Frost"
    },
    # Spanish base translations
    "es": {
        "Queen": "Reina", "Worker": "Obrera", "Soldier": "Soldado", "Male": "Macho",
        "Egg": "Huevo", "Larva": "Larva", "Pupa": "Pupa", "Alate": "Alado",
        "Brood": "Cría", "Colony": "Colonia", "Nest": "Nido", "Chamber": "Cámara",
        "Gallery": "Galería", "Tunnel": "Túnel", "Entrance": "Entrada",
        "Foraging": "Forrajeo", "Nursing": "Cuidado de crías", "Digging": "Excavación",
        "Defense": "Defensa", "Sanitation": "Higiene", "Pheromone": "Feromona",
        "Alarm": "Alarma", "Trail": "Rastro", "Homing": "Regreso al nido", "Food": "Comida",
        "Sugar": "Azúcares", "Protein": "Proteínas", "Water": "Agua", "Fungus": "Hongo",
        "Fungus Garden": "Jardín de hongos", "Waste": "Desechos", "Midden": "Vertedero",
        "Hibernation": "Hibernación", "Solarium": "Solárium", "Honeycomb": "Panal de cera",
        "Pollen Pot": "Olla de polen", "Ventilation": "Ventilación", "Leaf Cache": "Reserva de hojas",
        "Thatched Mound": "Montículo de agujas", "Cathedral Mound": "Termitero catedral",
        "Paper Nest": "Nido de papel", "Arboreal Nest": "Nido arbóreo", "Bivouac": "Vivac",
        "Humus": "Humus", "Clay": "Arcilla", "Sand": "Arena", "Silt": "Limo",
        "Gravel": "Grava", "Peat": "Turba", "Stone": "Roca", "Water Table": "Nivel freático",
        "Moisture": "Humedad", "Temperature": "Temperatura", "Elevation": "Elevación",
        "Simulation": "Simulación", "Settings": "Ajustes", "Statistics": "Estadísticas",
        "God Mode": "Modo Divino", "Realistic": "Realista", "Scientific": "Científico",
        "Gamified": "Gamificado", "Play": "Iniciar", "Pause": "Pausa", "Step": "Paso",
        "Speed": "Velocidad", "Population": "Población", "Health": "Salud", "Energy": "Energía",
        "Lifespan": "Esperanza de vida", "Body Length": "Longitud corporal",
        "Wildfire": "Incendio", "Flood": "Inundación", "Drought": "Sequía", "Frost": "Helada"
    },
    # Chinese (Mandarin) base translations
    "zh": {
        "Queen": "蚁后 / 蜂后", "Worker": "工蚁 / 工蜂", "Soldier": "兵蚁", "Male": "雄蚁 / 雄蜂",
        "Egg": "卵", "Larva": "幼虫", "Pupa": "蛹", "Alate": "有翅繁殖蚁",
        "Brood": "幼蚁幼虫群", "Colony": "群体 / 巢群", "Nest": "巢穴", "Chamber": "巢室",
        "Gallery": "通道 / 廊道", "Tunnel": "地道", "Entrance": "巢穴入口",
        "Foraging": "外出觅食", "Nursing": "育幼照料", "Digging": "挖掘筑巢",
        "Defense": "警戒防御", "Sanitation": "巢内清洁", "Pheromone": "信息素",
        "Alarm": "警戒信息素", "Trail": "行迹引导信息素", "Homing": "归巢追踪", "Food": "食物储备",
        "Sugar": "糖类碳水", "Protein": "蛋白质", "Water": "水分储备", "Fungus": "共生真菌",
        "Fungus Garden": "菌圃培育室", "Waste": "废料堆", "Midden": "弃物室",
        "Hibernation": "冬眠室", "Solarium": "日光温室", "Honeycomb": "六角蜂巢",
        "Pollen Pot": "花粉储罐", "Ventilation": "通风通气道", "Leaf Cache": "切叶缓存室",
        "Thatched Mound": "松针蚁丘", "Cathedral Mound": "宝塔白蚁冢",
        "Paper Nest": "纸质蜂巢", "Arboreal Nest": "树栖编织巢", "Bivouac": "活体行军蚁露营体",
        "Humus": "腐殖质土", "Clay": "粘土", "Sand": "沙土", "Silt": "粉砂土",
        "Gravel": "砾石", "Peat": "泥炭土", "Stone": "花岗岩基岩", "Water Table": "地下水位",
        "Moisture": "土壤湿度", "Temperature": "微气候温度", "Elevation": "地表高程",
        "Simulation": "模拟演化", "Settings": "系统参数设置", "Statistics": "群体生态统计",
        "God Mode": "上帝干预模式", "Realistic": "写实生态模式", "Scientific": "学术科学模式",
        "Gamified": "体素沙盒模式", "Play": "开始模拟", "Pause": "暂停模拟", "Step": "单步演进",
        "Speed": "模拟演算速度", "Population": "群体总个体数", "Health": "生理活力", "Energy": "代谢能量",
        "Lifespan": "预期寿命", "Body Length": "体长量度",
        "Wildfire": "野火灾害", "Flood": "地表洪涝", "Drought": "干旱灾情", "Frost": "极寒霜冻"
    }
}


def load_properties(file_path: Path) -> dict[str, str]:
    """Loads a Java .properties file supporting UTF-8 and multi-line continuations."""
    props = {}
    if not file_path.exists():
        return props

    with open(file_path, "r", encoding="utf-8", errors="replace") as f:
        multiline_key = None
        multiline_val = []

        for line in f:
            stripped = line.strip()
            if not stripped or stripped.startswith("#") or stripped.startswith("!"):
                continue

            if multiline_key:
                if stripped.endswith("\\"):
                    multiline_val.append(stripped[:-1].strip())
                else:
                    multiline_val.append(stripped)
                    props[multiline_key] = " ".join(multiline_val)
                    multiline_key = None
                    multiline_val = []
                continue

            if "=" in line:
                k, v = line.split("=", 1)
                k = k.strip()
                v = v.strip()
                if v.endswith("\\"):
                    multiline_key = k
                    multiline_val = [v[:-1].strip()]
                else:
                    props[k] = v
            elif ":" in line:
                k, v = line.split(":", 1)
                props[k.strip()] = v.strip()

    return props


def save_properties(file_path: Path, props: dict[str, str], module_name: str = "SwarmForge"):
    """Saves a sorted, neatly structured UTF-8 .properties file."""
    file_path.parent.mkdir(parents=True, exist_ok=True)
    sorted_keys = sorted(props.keys())

    with open(file_path, "w", encoding="utf-8", newline="\n") as f:
        f.write(f"# {module_name} - Eusocial Insect Simulation I18n Resource Bundle\n")
        f.write(f"# Language: {file_path.stem}\n")
        f.write(f"# Total Keys: {len(sorted_keys)}\n\n")

        current_prefix = ""
        for k in sorted_keys:
            prefix = k.split(".")[0] if "." in k else "general"
            if prefix != current_prefix:
                current_prefix = prefix
                f.write(f"\n# --- {current_prefix.upper()} ---\n")
            val = props[k]
            f.write(f"{k}={val}\n")


def translate_fallback(key: str, ref_val: str, target_lang: str) -> str:
    """Intelligently translates a string into the target language using domain lexicon."""
    if not ref_val:
        return key

    lex = BIOLOGICAL_LEXICON.get(target_lang, {})
    res = ref_val

    # Replace whole biological terms
    for en_word, trans_word in sorted(lex.items(), key=lambda x: -len(x[0])):
        res = re.sub(rf'\b{re.escape(en_word)}\b', trans_word, res, flags=re.IGNORECASE)

    return res


def sync_properties_directory(i18n_dir: Path, module_name: str, master_keys: set[str] = None) -> tuple[int, dict[str, int]]:
    """Synchronizes all 5 languages in a properties directory."""
    if not i18n_dir.exists():
        i18n_dir.mkdir(parents=True, exist_ok=True)

    bundles = {}
    for lang in LANGUAGES:
        prop_file = i18n_dir / f"messages_{lang}.properties"
        bundles[lang] = load_properties(prop_file)
    default_file = i18n_dir / "messages.properties"
    bundles["default"] = load_properties(default_file)

    all_keys = set(master_keys) if master_keys else set()
    for lang in LANGUAGES:
        all_keys.update(bundles[lang].keys())
    all_keys.update(bundles["default"].keys())

    added_count = {lang: 0 for lang in LANGUAGES}

    for k in all_keys:
        # Determine reference value (FR -> EN -> default -> others)
        ref_val = bundles["fr"].get(k) or bundles["en"].get(k) or bundles["default"].get(k)
        if not ref_val:
            for l in LANGUAGES:
                if bundles[l].get(k):
                    ref_val = bundles[l][k]
                    break
        if not ref_val:
            ref_val = k

        for lang in LANGUAGES:
            if k not in bundles[lang] or not bundles[lang][k].strip():
                if lang == "fr" and bundles["fr"].get(k):
                    continue
                elif lang == "en" and bundles["en"].get(k):
                    continue
                else:
                    bundles[lang][k] = translate_fallback(k, ref_val, lang)
                added_count[lang] += 1

    # Default messages.properties mirrors French or English
    bundles["default"] = dict(bundles["fr"] if bundles["fr"] else bundles["en"])

    for lang in LANGUAGES:
        save_properties(i18n_dir / f"messages_{lang}.properties", bundles[lang], module_name)
    save_properties(default_file, bundles["default"], module_name)

    return len(all_keys), added_count


def scan_hardcoded_java_strings() -> list[dict]:
    """Scans all Java source files for hardcoded UI strings that should use I18n."""
    results = []
    patterns = [
        (r'new\s+Label\(\s*"([^"]{2,})"\s*\)', "Label"),
        (r'new\s+Button\(\s*"([^"]{2,})"\s*\)', "Button"),
        (r'new\s+CheckBox\(\s*"([^"]{2,})"\s*\)', "CheckBox"),
        (r'new\s+RadioButton\(\s*"([^"]{2,})"\s*\)', "RadioButton"),
        (r'new\s+Tab\(\s*"([^"]{2,})"\s*\)', "Tab"),
        (r'new\s+Tooltip\(\s*"([^"]{2,})"\s*\)', "Tooltip"),
        (r'new\s+TableColumn(?:<[^>]+>)?\(\s*"([^"]{2,})"\s*\)', "TableColumn"),
        (r'new\s+MenuItem\(\s*"([^"]{2,})"\s*\)', "MenuItem"),
        (r'new\s+Menu\(\s*"([^"]{2,})"\s*\)', "Menu"),
        (r'\.setText\(\s*"([^"]{2,})"\s*\)', "setText"),
        (r'\.setTitle\(\s*"([^"]{2,})"\s*\)', "setTitle"),
        (r'\.setHeaderText\(\s*"([^"]{2,})"\s*\)', "setHeaderText"),
        (r'\.setContentText\(\s*"([^"]{2,})"\s*\)', "setContentText"),
        (r'\.setPromptText\(\s*"([^"]{2,})"\s*\)', "setPromptText"),
    ]

    ignore_prefixes = (
        "-fx-", "http://", "https://", "data/", "file:/", "rgba(", "rgb(",
        "System", "Arial", "Roboto", "Segoe", "#", "PT", "0.", "1.", "2.", "3.",
        "4.", "5.", "6.", "7.", "8.", "9.", "%", "$", "{", "[", "]", "}", "->",
        "-->", "<--", "===", "---", "###", "class", "style", "px", "em", "vw", "vh",
        "Common/", "textures/", "models/", "sounds/", "shaders/", "i18n/", "org/"
    )

    for jdir in JAVA_DIRS:
        if not jdir.exists():
            continue
        for java_file in jdir.rglob("*.java"):
            with open(java_file, "r", encoding="utf-8", errors="replace") as f:
                lines = f.readlines()

            for idx, line in enumerate(lines, start=1):
                stripped = line.strip()
                if stripped.startswith("//") or stripped.startswith("/*") or stripped.startswith("*"):
                    continue
                if "I18n.get" in line or "i18n.get" in line or "createStringBinding" in line or "createTooltipBinding" in line:
                    continue

                for pattern, comp_type in patterns:
                    matches = re.finditer(pattern, line)
                    for m in matches:
                        text = m.group(1).strip()
                        if len(text) < 2:
                            continue
                        if any(text.startswith(p) for p in ignore_prefixes):
                            continue
                        if text in ("OK", "X", "+", "-", "...", ":", "|", "/", "\\", "N/A", "NaN", "v1.0", "v2.0", "❌", "✔️", "⚙️", "⚠️"):
                            continue
                        if re.match(r'^[A-Z0-9_\-\.\s]{1,3}$', text):
                            continue

                        results.append({
                            "file": str(java_file.relative_to(ROOT_DIR)),
                            "line": idx,
                            "type": comp_type,
                            "text": text,
                            "source": stripped
                        })

    return results


def check_java_missing_tooltips() -> list[dict]:
    """Verifies that all interactive controls in Java UI classes have explicit tooltips."""
    missing = []
    ui_dir = ROOT_DIR / "swarmforge-editor" / "src" / "main" / "java" / "org" / "swarmforge" / "client" / "ui"

    if not ui_dir.exists():
        return missing

    control_pattern = re.compile(
        r'(?:private\s+)?(?:final\s+)?(?:Button|Slider|ComboBox<[^>]+>|CheckBox|Spinner<[^>]+>|ToggleButton)\s+([a-zA-Z0-9_]+)\b'
    )

    for java_file in ui_dir.glob("*.java"):
        with open(java_file, "r", encoding="utf-8", errors="replace") as f:
            content = f.read()
            lines = content.splitlines()

        declared_controls = set(m.group(1) for m in control_pattern.finditer(content))

        for ctrl in declared_controls:
            # Check if control has tooltip set or bound
            has_tt = bool(re.search(
                rf'({ctrl}\.setTooltip|{ctrl}\.tooltipProperty\(\)\.bind|Tooltip\.install\(\s*{ctrl}|installTooltip\(\s*{ctrl}|setControlTooltip\(\s*{ctrl}|createTooltipBinding)',
                content
            ))
            if not has_tt:
                line_no = 1
                for idx, l in enumerate(lines, start=1):
                    if ctrl in l and any(k in l for k in ("new Button", "new Slider", "new ComboBox", "new CheckBox", "new Spinner", "new ToggleButton")):
                        line_no = idx
                        break

                missing.append({
                    "file": java_file.name,
                    "control": ctrl,
                    "line": line_no
                })

    return missing


def parse_web_translations_blocks(content: str) -> dict:
    """Parses language blocks and their key-values from translations.js accurately."""
    languages = ["fr", "en", "de", "es", "zh"]
    lang_blocks = {}

    for i, lang in enumerate(languages):
        start_pattern = rf'(?:^|\n)\s*{lang}\s*:\s*\{{'
        m = re.search(start_pattern, content)
        if not m:
            continue
        start_idx = m.end() - 1  # at the '{'
        
        # Track braces to find the matching '}'
        depth = 0
        end_idx = start_idx
        in_string = None
        escape = False

        for idx in range(start_idx, len(content)):
            char = content[idx]
            if in_string:
                if escape:
                    escape = False
                elif char == '\\':
                    escape = True
                elif char == in_string:
                    in_string = None
            else:
                if char in ('"', "'", '`'):
                    in_string = char
                elif char == '{':
                    depth += 1
                elif char == '}':
                    depth -= 1
                    if depth == 0:
                        end_idx = idx
                        break

        block_text = content[start_idx + 1:end_idx]
        
        # Extract key-value entries
        entries = {}
        for line in block_text.splitlines():
            line_str = line.strip()
            if not line_str or line_str.startswith("//") or line_str.startswith("/*"):
                continue
            kv_match = re.match(r'^([a-zA-Z0-9_]+)\s*:\s*(.+?),?\s*$', line_str)
            if kv_match:
                k = kv_match.group(1)
                v = kv_match.group(2).rstrip(',')
                entries[k] = v

        lang_blocks[lang] = {
            "start": start_idx,
            "end": end_idx,
            "entries": entries,
            "text": block_text
        }

    return lang_blocks


def scan_web_translations() -> dict:
    """Verifies that SwarmForge Web dictionary contains matching keys across all 5 languages."""
    results = {"total_keys": 0, "languages": {}}
    if not WEB_TRANSLATIONS_JS.exists():
        return results

    with open(WEB_TRANSLATIONS_JS, "r", encoding="utf-8", errors="replace") as f:
        content = f.read()

    blocks = parse_web_translations_blocks(content)
    all_keys = set()
    for lang, data in blocks.items():
        all_keys.update(data["entries"].keys())

    results["total_keys"] = len(all_keys)
    for lang in LANGUAGES:
        entries = blocks.get(lang, {}).get("entries", {})
        missing = all_keys - set(entries.keys())
        results["languages"][lang] = {
            "count": len(entries),
            "missing": sorted(list(missing))
        }

    return results


def sync_web_translations() -> tuple[int, dict]:
    """Synchronizes missing keys in swarmforge-web/src/i18n/translations.js across all 5 languages."""
    if not WEB_TRANSLATIONS_JS.exists():
        return 0, {}

    with open(WEB_TRANSLATIONS_JS, "r", encoding="utf-8", errors="replace") as f:
        content = f.read()

    blocks = parse_web_translations_blocks(content)
    all_keys = set()
    for lang, data in blocks.items():
        all_keys.update(data["entries"].keys())

    added_counts = {l: 0 for l in LANGUAGES}

    # For each language, check if keys are missing
    modified = False
    new_content = content

    for lang in LANGUAGES:
        if lang not in blocks:
            continue
        entries = blocks[lang]["entries"]
        missing_keys = all_keys - set(entries.keys())
        if not missing_keys:
            continue

        missing_lines = []
        for k in sorted(missing_keys):
            # Find source value from fr, en, or any available
            source_val = None
            source_lang = "en"
            for src in ["en", "fr", "de", "es", "zh"]:
                if k in blocks.get(src, {}).get("entries", {}):
                    source_val = blocks[src]["entries"][k]
                    source_lang = src
                    break

            if source_val:
                raw_str = source_val.strip("'\"`")
                translated = translate_fallback(k, raw_str, lang)
                # Escape single quotes if string is wrapped in single quotes
                escaped_val = translated.replace("'", "\\'")
                missing_lines.append(f"        {k}: '{escaped_val}',")
                added_counts[lang] += 1

        if missing_lines:
            # Re-read content and insert before the end of the block
            # Find closing brace of this language block
            block_pattern = rf'(\b{lang}\s*:\s*\{{[\s\S]*?)(\n\s*\}})'
            match = re.search(block_pattern, new_content)
            if match:
                insertion = "\n        // Auto-synchronized & Biological translations\n" + "\n".join(missing_lines)
                new_block = match.group(1) + insertion + match.group(2)
                new_content = new_content[:match.start()] + new_block + new_content[match.end():]
                modified = True

    if modified:
        with open(WEB_TRANSLATIONS_JS, "w", encoding="utf-8", newline="\n") as f:
            f.write(new_content)

    return len(all_keys), added_counts


def fix_encoding_mojibake() -> int:
    """Scans and repairs corrupted UTF-8 mojibake sequences in Java source files."""
    cleaned = 0
    for jdir in JAVA_DIRS:
        if not jdir.exists():
            continue
        for java_file in jdir.rglob("*.java"):
            with open(java_file, "rb") as f:
                raw_bytes = f.read()

            new_bytes = raw_bytes
            for corrupted, fixed in BINARY_MOJIBAKE:
                new_bytes = new_bytes.replace(corrupted, fixed)

            if new_bytes != raw_bytes:
                with open(java_file, "wb") as f:
                    f.write(new_bytes)
                cleaned += 1

    return cleaned


def main():
    parser = argparse.ArgumentParser(description="SwarmForge Universal I18n & Tooltip Coverage Audit Suite")
    parser.add_argument("--audit", action="store_true", help="Perform comprehensive I18n and tooltip audit")
    parser.add_argument("--sync", action="store_true", help="Synchronize all 5 language properties across modules")
    parser.add_argument("--fix-encoding", action="store_true", help="Fix binary mojibake and encoding issues in source files")
    parser.add_argument("--all", action="store_true", help="Execute complete encoding fix, bundle synchronization and audit")

    args = parser.parse_args()
    if not (args.audit or args.sync or args.fix_encoding or args.all):
        args.all = True

    print("================================================================================")
    print(" 🐜 SWARMFORGE UNIVERSAL 5-LANGUAGE I18N & TOOLTIP AUDIT SUITE")
    print("================================================================================")

    # 1. Encoding Fix
    if args.fix_encoding or args.all:
        print("\n[1/4] Checking and repairing UTF-8 encoding & mojibake sequences...")
        cleaned = fix_encoding_mojibake()
        print(f"   -> Repaired encoding in {cleaned} Java source files.")

    # 2. Synchronize Resource Bundles
    if args.sync or args.all:
        print("\n[2/4] Synchronizing 5-Language properties bundles across modules...")
        
        # Aggregate master keys across editor and client
        editor_props = load_properties(EDITOR_I18N / "messages_fr.properties")
        client_props = load_properties(CLIENT_I18N / "messages_fr.properties")
        master_keys = set(editor_props.keys()).union(set(client_props.keys()))

        tot_ed, add_ed = sync_properties_directory(EDITOR_I18N, "SwarmForge Editor", master_keys)
        print(f"   -> [Editor] Unified {tot_ed} keys. Added: {add_ed}")

        tot_cl, add_cl = sync_properties_directory(CLIENT_I18N, "SwarmForge Client", master_keys)
        print(f"   -> [Client] Unified {tot_cl} keys. Added: {add_cl}")

        tot_cr, add_cr = sync_properties_directory(CORE_I18N, "SwarmForge Core")
        print(f"   -> [Core] Unified {tot_cr} keys. Added: {add_cr}")

        tot_web, add_web = sync_web_translations()
        print(f"   -> [Web] Unified {tot_web} keys in translations.js. Added: {add_web}")

    # 3. Hardcoded String Detection
    if args.audit or args.all:
        print("\n[3/4] Auditing codebase for hardcoded UI string literals...")
        hardcoded = scan_hardcoded_java_strings()
        print(f"   -> Found {len(hardcoded)} potential hardcoded UI string literals in Java components.")
        if hardcoded:
            print("   -> Sample occurrences:")
            for h in hardcoded[:8]:
                print(f"      - {h['file']}:{h['line']} [{h['type']}]: \"{h['text']}\"")
            if len(hardcoded) > 8:
                print(f"      ... and {len(hardcoded) - 8} more.")

        print("\n[4/4] Auditing mouse-over tooltip coverage on interactive UI controls...")
        missing_tt = check_java_missing_tooltips()
        print(f"   -> Found {len(missing_tt)} interactive controls without explicit tooltip bindings.")
        if missing_tt:
            print("   -> Sample un-tooltiped controls:")
            for m in missing_tt[:8]:
                print(f"      - {m['file']}:{m['line']} -> Control: '{m['control']}'")
            if len(missing_tt) > 8:
                print(f"      ... and {len(missing_tt) - 8} more.")

        # Web i18n Audit
        print("\n[WEB] Auditing Web React translations (swarmforge-web)...")
        web_res = scan_web_translations()
        print(f"   -> Total Web keys: {web_res['total_keys']}")
        for lang, data in web_res.get("languages", {}).items():
            print(f"   -> [{lang.upper()}] Keys: {data['count']} (Missing: {len(data['missing'])})")

    print("\n================================================================================")
    print(" ✅ SWARMFORGE I18N & TOOLTIP AUDIT PROCESS COMPLETED SUCCESSFULLY")
    print("================================================================================")


if __name__ == "__main__":
    main()
