import React, { useState } from 'react'
import { BookOpen, X, Search, Layers, Crown, HardHat, Shield, Sparkles } from 'lucide-react'
import { useSimulationStore } from '../store/simulationStore'
import { getTranslation } from '../i18n/translations'

const GLOSSARY_DATA = {
    fr: {
        modalTitle: '📖 Guide & Légende Scientifique',
        modalSubtitle: 'Répertoire des substrats, gradients de phéromones et lexique de myrmécologie',
        tabLegend: '🎨 Légende des Éléments 3D',
        tabGlossary: '📚 Glossaire Myrmécologique',
        searchPlaceholder: 'Rechercher un terme (ex: Stigmergie, Quorum, Trophallaxie...)',
        substratesHeader: 'Substrats & Strates Géologiques',
        pheromonesHeader: 'Gradients de Phéromones (8 Canaux)',
        castesHeader: 'Castes d\'Insectes Sociaux',
        substrates: [
            { name: 'Terre Végétale (Humus)', color: '#4d7c0f', desc: 'Couche superficielle arable, excavation rapide' },
            { name: 'Argile & Sédiment', color: '#854d0e', desc: 'Sol compact idéal pour la solidité des galeries' },
            { name: 'Roche Mère (Socle)', color: '#475569', desc: 'Substrat impénétrable formant le plancher géologique' },
            { name: 'Sable & Alluvions', color: '#ca8a04', desc: 'Matériau meuble sensible aux éboulements' },
            { name: 'Rivière & Flaques', color: '#0284c7', desc: 'Point d\'hydratation et obstacle de surface' },
            { name: 'Chambres & Tunnels', color: '#1e293b', desc: 'Espace excavé pour stockage et incubation' },
        ],
        pheromones: [
            { name: '1. Piste Alimentaire', color: '#4caf50', desc: 'Guide vers la biomasse glucidique et les sources de miellat' },
            { name: '2. Retour au Nid', color: '#2196f3', desc: 'Orientation vectorielle vers les entrées et cavités du nid' },
            { name: '3. Alarme Chimique', color: '#f44336', desc: 'Mobilisation défensive et alerte face aux prédateurs' },
            { name: '4. Recrutement de Masse', color: '#ffc107', desc: 'Amplification rapide et canalisation du flux d\'ouvrières' },
            { name: '5. Phéromone Royale', color: '#9c27b0', desc: 'Inhibition de la reproduction et maintien de la cohésion' },
            { name: '6. Soin du Couvain', color: '#ff9800', desc: 'Identification chimique et protection des œufs et larves' },
            { name: '7. Nécrophorèse', color: '#607d8b', desc: 'Transport des cadavres d\'ouvrières vers le dépotoir' },
            { name: '8. Marquage Territorial', color: '#009688', desc: 'Délimitation des frontières d\'affrontement et du domaine vital' },
        ],
        castes: [
            { name: 'Reine', desc: 'Longévité maximale, ponte continue des œufs' },
            { name: 'Ouvrière', desc: 'Soins au couvain, construction, récolte et exploration' },
            { name: 'Soldat', desc: 'Garde de l\'entrée, broyage et dissuasion des prédateurs' },
        ],
        categories: [
            {
                title: 'Stigmergie & Auto-organisation',
                items: [
                    { term: 'Stigmergie', definition: 'Mécanisme de coordination indirecte entre insectes sociaux où la trace laissée dans l\'environnement stimule l\'action suivante.' },
                    { term: 'Trophallaxie', definition: 'Transfert direct de nourriture liquide régurgitée de jabot à jabot entre individus de la colonie.' },
                    { term: 'Polyéthisme temporel', definition: 'Changement prévisible des fonctions d\'un individu au cours de son cycle de vie (nourrice -> bâtisseuse -> fourrageuse).' },
                    { term: 'Quorum Sensing', definition: 'Seuil critique de densité ou de phéromones requis pour faire basculer une décision collective.' }
                ]
            },
            {
                title: 'Castes & Écologie Comportementale',
                items: [
                    { term: 'Reine (Gynomorphe)', definition: 'Femelle fertile assurant la pérennité démographique et émettant les phéromones de cohésion sociale.' },
                    { term: 'Ouvrière Minor / Médian', definition: 'Castes spécialisées dans le soin larvaire, l\'excavation et la prospection de surface.' },
                    { term: 'Soldat (Major)', definition: 'Individu à forte masse mandibulaire dédié à la défense du nid et au broyage.' },
                    { term: 'Nécrophorèse', definition: 'Évacuation sanitaire des cadavres hors du nid sous stimulation de l\'acide oléique.' }
                ]
            },
            {
                title: 'Chimio-réception & Pistes Chimiques',
                items: [
                    { term: 'Piste Chimique Active', definition: 'Gradient de substances volatiles déposé au sol guidant le flux d\'approvisionnement.' },
                    { term: 'Phéromone d\'Alarme', definition: 'Signal d\'alerte à diffusion rapide déclenchant l\'attaque ou la dispersion.' },
                    { term: 'Hydrocarbures Cuticulaires', definition: 'Signature chimique tégumentaire permettant la reconnaissance coloniale de parenté.' }
                ]
            }
        ]
    },
    en: {
        modalTitle: '📖 Scientific Guide & Legend',
        modalSubtitle: 'Substrates directory, pheromone gradients, and myrmecology glossary',
        tabLegend: '🎨 3D Elements Legend',
        tabGlossary: '📚 Myrmecology Glossary',
        searchPlaceholder: 'Search a term (e.g. Stigmergy, Quorum, Trophallaxis...)',
        substratesHeader: 'Geological Substrates & Layers',
        pheromonesHeader: 'Pheromone Gradients (8 Channels)',
        castesHeader: 'Social Insect Castes',
        substrates: [
            { name: 'Organic Topsoil (Humus)', color: '#4d7c0f', desc: 'Arable surface layer, fast excavation' },
            { name: 'Clay & Sediment', color: '#854d0e', desc: 'Compact soil ideal for gallery structural integrity' },
            { name: 'Bedrock (Base)', color: '#475569', desc: 'Impenetrable substrate forming geological base' },
            { name: 'Sand & Alluvium', color: '#ca8a04', desc: 'Loose material prone to soil shifting' },
            { name: 'River & Stream', color: '#0284c7', desc: 'Hydration source and surface barrier' },
            { name: 'Chambers & Tunnels', color: '#1e293b', desc: 'Excavated space for brood and food storage' },
        ],
        pheromones: [
            { name: '1. Food Trail', color: '#4caf50', desc: 'Guides towards carbohydrate biomass and honeydew sources' },
            { name: '2. Return to Nest', color: '#2196f3', desc: 'Vector orientation towards nest entrances and chambers' },
            { name: '3. Chemical Alarm', color: '#f44336', desc: 'Defensive mobilization and predator threat alert' },
            { name: '4. Mass Recruitment', color: '#ffc107', desc: 'Rapid amplification and channeling of worker flow' },
            { name: '5. Queen Pheromone', color: '#9c27b0', desc: 'Reproductive inhibition and colonial cohesion maintenance' },
            { name: '6. Brood Care', color: '#ff9800', desc: 'Chemical identification and protection of eggs and larvae' },
            { name: '7. Necrophoresis', color: '#607d8b', desc: 'Transport of worker corpses to the refuse midden' },
            { name: '8. Territorial Marking', color: '#009688', desc: 'Demarcation of territorial borders and home range' },
        ],
        castes: [
            { name: 'Queen', desc: 'Maximum longevity, continuous egg laying' },
            { name: 'Worker', desc: 'Brood care, nest construction, foraging, and exploration' },
            { name: 'Soldier', desc: 'Entrance defense, crushing seeds, and predator deterrence' },
        ],
        categories: [
            {
                title: 'Stigmergy & Self-Organization',
                items: [
                    { term: 'Stigmergy', definition: 'Indirect coordination mechanism where physical traces left in the environment stimulate subsequent actions.' },
                    { term: 'Trophallaxis', definition: 'Direct mutual exchange of regurgitated liquid food between colony members.' },
                    { term: 'Age Polyethism', definition: 'Predictable shift in individual tasks across life stages (nurse -> builder -> forager).' },
                    { term: 'Quorum Sensing', definition: 'Critical threshold of density or pheromone concentration required to trigger collective shifts.' }
                ]
            },
            {
                title: 'Castes & Behavioral Ecology',
                items: [
                    { term: 'Queen (Gynomorph)', definition: 'Fertile reproductive female ensuring colony longevity and emitting cohesion pheromones.' },
                    { term: 'Worker Minor / Media', definition: 'Specialized castes dedicated to nursing, underground excavation, and surface foraging.' },
                    { term: 'Soldier (Major)', definition: 'Morphological caste with large cephalic capsule and powerful mandibles for defense.' },
                    { term: 'Necrophoresis', definition: 'Sanitary corpse removal behavior triggered by oleic acid release.' }
                ]
            },
            {
                title: 'Chemoreception & Chemical Trails',
                items: [
                    { term: 'Active Trail Pheromone', definition: 'Volatile ground chemical gradient directing group foraging flows.' },
                    { term: 'Alarm Pheromone', definition: 'Rapidly diffusing chemical signal triggering collective defense or dispersal.' },
                    { term: 'Cuticular Hydrocarbons', definition: 'Waxy epicuticular layer providing nestmate recognition signatures.' }
                ]
            }
        ]
    },
    de: {
        modalTitle: '📖 Wissenschaftlicher Leitfaden & Legende',
        modalSubtitle: 'Verzeichnis der Substrate, Pheromongradienten und myrmekologisches Glossar',
        tabLegend: '🎨 3D-Elemente-Legende',
        tabGlossary: '📚 Myrmekologisches Glossar',
        searchPlaceholder: 'Begriff suchen (z. B. Stigmergie, Quorum, Trophallaxis...)',
        substratesHeader: 'Geologische Substrate & Schichten',
        pheromonesHeader: 'Pheromongradienten (8 Kanäle)',
        castesHeader: 'Kasten der Sozialen Insekten',
        substrates: [
            { name: 'Humus-Oberboden', color: '#4d7c0f', desc: 'Fruchtbare Oberschicht, schnelles Graben' },
            { name: 'Ton & Sediment', color: '#854d0e', desc: 'Kompakter Boden, ideal für stabile Gänge' },
            { name: 'Grundgestein', color: '#475569', desc: 'Undurchdringliches Gestein als geologischer Sockel' },
            { name: 'Sand & Alluvium', color: '#ca8a04', desc: 'Lockeres Material, anfällig für Einstürze' },
            { name: 'Fluss & Wasser', color: '#0284c7', desc: 'Hydratationsquelle und Oberflächenhindernis' },
            { name: 'Kammern & Tunnel', color: '#1e293b', desc: 'Ausgegrabener Raum für Brut und Vorräte' },
        ],
        pheromones: [
            { name: '1. Nahrungsspur', color: '#4caf50', desc: 'Führt zu Kohlenhydrat-Biomasse und Honigtauquellen' },
            { name: '2. Rückkehr zum Nest', color: '#2196f3', desc: 'Vektorielle Orientierung zu Nesteingängen und Kammern' },
            { name: '3. Chemischer Alarm', color: '#f44336', desc: 'Verteidigungsmobilisierung und Warnung vor Räubern' },
            { name: '4. Massenrekrutierung', color: '#ffc107', desc: 'Rasche Verstärkung und Bündelung des Arbeiterinnenstroms' },
            { name: '5. Königinnenpheromon', color: '#9c27b0', desc: 'Fortpflanzungshemmung und Erhaltung des Koloniezusammenhalts' },
            { name: '6. Brutpflege', color: '#ff9800', desc: 'Chemische Identifikation und Schutz von Eiern und Larven' },
            { name: '7. Nekrophorese', color: '#607d8b', desc: 'Transport von Arbeiterinnenkadavern zum Müllhaufen' },
            { name: '8. Reviermarkierung', color: '#009688', desc: 'Abgrenzung der Reviergrenzen und des Streifgebiets' },
        ],
        castes: [
            { name: 'Königin', desc: 'Maximale Langlebigkeit, kontinuierliche Eiablage' },
            { name: 'Arbeiterin', desc: 'Brutpflege, Nestbau, Nahrungssuche und Erkundung' },
            { name: 'Soldat', desc: 'Eingangsverteidigung, Zerkleinern von Samen und Räuberabwehr' },
        ],
        categories: [
            {
                title: 'Stigmergie & Selbstorganisation',
                items: [
                    { term: 'Stigmergie', definition: 'Indirekter Koordinationsmechanismus, bei dem Umweltveränderungen Folgereaktionen anregen.' },
                    { term: 'Trophallaxis', definition: 'Direkte Weitergabe vorverdauter Nahrung zwischen Koloniemitgliedern.' },
                    { term: 'Altersstufenteilung', definition: 'Vorhersehbarer Aufgabenwechsel während der Lebensspanne (Pflegerin -> Bauarbeiterin -> Sammlerin).' },
                    { term: 'Quorum Sensing', definition: 'Kritische Dichte- oder Pheromonschwelle für kollektive Verhaltensumschaltungen.' }
                ]
            },
            {
                title: 'Kasten & Verhaltensökologie',
                items: [
                    { term: 'Königin (Gynomorph)', definition: 'Fertiles Weibchen für Kolonieerhalt und Abgabe von Zusammenhaltspheromonen.' },
                    { term: 'Arbeiterin Minor / Media', definition: 'Spezialisierte Kasten für Brutversorgung, Grabarbeiten und Futtersuche.' },
                    { term: 'Soldat (Major)', definition: 'Großköpfige Morphe mit kräftigen Mandibeln zur Nestverteidigung.' },
                    { term: 'Nekrophorese', definition: 'Hygienischer Abtransport von Kadavern aus dem Nestbereich.' }
                ]
            },
            {
                title: 'Chemorezeption & Duftspuren',
                items: [
                    { term: 'Aktive Duftspur', definition: 'Am Boden abgelegter flüchtiger Gradient zur Leitung von Sammlergruppen.' },
                    { term: 'Alarmpheromon', definition: 'Schnell diffundierendes Signal zur Verteidigungsmobilisierung oder Flucht.' },
                    { term: 'Kutikuläre Kohlenwasserstoffe', definition: 'Wachsschicht der Kutikula als koloniespezifischer Erkennungsduft.' }
                ]
            }
        ]
    },
    es: {
        modalTitle: '📖 Guía y Leyenda Científica',
        modalSubtitle: 'Directorio de sustratos, gradientes de feromonas y glosario mirmecológico',
        tabLegend: '🎨 Leyenda de Elementos 3D',
        tabGlossary: '📚 Glosario Mirmecológico',
        searchPlaceholder: 'Buscar un término (ej: Estigmergia, Quórum, Trofalaxia...)',
        substratesHeader: 'Sustratos Geológicos y Estratos',
        pheromonesHeader: 'Gradientes de Feromonas (8 Canales)',
        castesHeader: 'Castas de Insectos Sociales',
        substrates: [
            { name: 'Tierra Vegetal (Humus)', color: '#4d7c0f', desc: 'Capa superficial arable, excavación rápida' },
            { name: 'Arcilla y Sedimento', color: '#854d0e', desc: 'Suelo compacto ideal para la solidez de galerías' },
            { name: 'Roca Madre (Base)', color: '#475569', desc: 'Sustrato impenetrable que forma el suelo geológico' },
            { name: 'Arena y Aluvión', color: '#ca8a04', desc: 'Material suelto propenso a derrumbes' },
            { name: 'Río y Agua', color: '#0284c7', desc: 'Fuente de hidratación y barrera superficial' },
            { name: 'Cámaras y Túneles', color: '#1e293b', desc: 'Espacio excavado para cría y almacenamiento' },
        ],
        pheromones: [
            { name: '1. Rastro de Comida', color: '#4caf50', desc: 'Guía hacia la biomasa de carbohidratos y fuentes de melaza' },
            { name: '2. Regreso al Nido', color: '#2196f3', desc: 'Orientación vectorial hacia las entradas y cámaras del nido' },
            { name: '3. Alarma Química', color: '#f44336', desc: 'Movilización defensiva y alerta de depredadores' },
            { name: '4. Reclutamiento Masivo', color: '#ffc107', desc: 'Amplificación rápida y canalización del flujo de obreras' },
            { name: '5. Feromona Real', color: '#9c27b0', desc: 'Inhibición reproductiva y mantenimiento de la cohesión colonial' },
            { name: '6. Cuidado de Cría', color: '#ff9800', desc: 'Identificación química y protección de huevos y larvas' },
            { name: '7. Necroforesis', color: '#607d8b', desc: 'Transporte de cadáveres de obreras al vertedero' },
            { name: '8. Marcaje Territorial', color: '#009688', desc: 'Delimitación de las fronteras territoriales y área de vida' },
        ],
        castes: [
            { name: 'Reina', desc: 'Longevidad máxima, puesta continua de huevos' },
            { name: 'Obrera', desc: 'Cuidado de cría, construcción del nido, forrajeo y exploración' },
            { name: 'Soldado', desc: 'Defensa de la entrada, trituración y disuasión de depredadores' },
        ],
        categories: [
            {
                title: 'Estigmergia y Autoorganización',
                items: [
                    { term: 'Estigmergia', definition: 'Mecanismo de coordinación indirecta donde las huellas ambientales estimulan acciones siguientes.' },
                    { term: 'Trofalaxia', definition: 'Transferencia directa de alimento líquido regurgitado entre individuos de la colonia.' },
                    { term: 'Polietismo temporal', definition: 'Cambio predecible en las tareas a lo largo de la vida (nodriza -> constructora -> forrajera).' },
                    { term: 'Quórum Sensing', definition: 'Umbral crítico de densidad o feromonas requerido para cambios colectivos de comportamiento.' }
                ]
            },
            {
                title: 'Castas y Ecología del Comportamiento',
                items: [
                    { term: 'Reina (Ginomorfa)', definition: 'Hembra reproductora fértil que garantiza la colonia y emite feromonas de cohesión.' },
                    { term: 'Obrera Menor / Media', definition: 'Castas especializadas en cuidado de crías, excavación y recolección exterior.' },
                    { term: 'Soldado (Mayor)', definition: 'Individuo con gran cápsula cefálica y fuertes mandíbulas para la defensa.' },
                    { term: 'Necroforesis', definition: 'Comportamiento sanitario de remoción de cadáveres fuera del nido.' }
                ]
            },
            {
                title: 'Quimiorrecepción y Rastros Químicos',
                items: [
                    { term: 'Rastro Químico Activo', definition: 'Gradiente de sustancias volátiles en el suelo que orienta a los grupos de forrajeo.' },
                    { term: 'Feromona de Alarma', definition: 'Señal de difusión rápida que activa defensa colectiva o dispersión de emergencia.' },
                    { term: 'Hidrocarburos Cuticulares', definition: 'Capa cerosa que actúa como firma de reconocimiento colonial.' }
                ]
            }
        ]
    },
    zh: {
        modalTitle: '📖 科学指南与图例',
        modalSubtitle: '地质基质名录、信息素浓度梯度与蚁类学词汇表',
        tabLegend: '🎨 3D 视觉元素图例',
        tabGlossary: '📚 蚁类学词汇表',
        searchPlaceholder: '搜索词条 (例如: 协作自组织, 营养共享, 群体感应...)',
        substratesHeader: '地质基质与土层',
        pheromonesHeader: '信息素浓度梯度 (8 种通道)',
        castesHeader: '社会性昆虫品级',
        substrates: [
            { name: '腐殖质表土', color: '#4d7c0f', desc: '肥沃表层土壤，挖掘阻力小' },
            { name: '粘土与沉积层', color: '#854d0e', desc: '致密土壤，适合构筑坚固地下隧道' },
            { name: '基岩底板', color: '#475569', desc: '坚硬不可穿透的地质基底' },
            { name: '细砂与冲积物', color: '#ca8a04', desc: '松散颗粒材料，易发生坍塌' },
            { name: '河流与水源', color: '#0284c7', desc: '补充水分源与地表通行屏障' },
            { name: '腔室与隧道', color: '#1e293b', desc: '用于育幼和储存食物的地下空间' },
        ],
        pheromones: [
            { name: '1. 食物轨迹', color: '#4caf50', desc: '引导工蚁前往碳水化合物生物质与蜜露源' },
            { name: '2. 返回巢穴', color: '#2196f3', desc: '指向巢穴入口与内部腔室的矢量定向' },
            { name: '3. 化学警报', color: '#f44336', desc: '防御动员与捕食者威胁警报' },
            { name: '4. 群体招募', color: '#ffc107', desc: '快速放大并引导工蚁行进队列' },
            { name: '5. 蚁后信息素', color: '#9c27b0', desc: '抑制工蚁卵巢发育并维持群体凝聚力' },
            { name: '6. 幼体抚育', color: '#ff9800', desc: '化学识别并保护卵与幼虫' },
            { name: '7. 搬尸行为', color: '#607d8b', desc: '将死亡工蚁尸体搬运至巢外垃圾堆' },
            { name: '8. 领地标记', color: '#009688', desc: '界定巢穴领地防御边界与活动范围' },
        ],
        castes: [
            { name: '蚁后', desc: '最高寿命个体，负责持续产卵与繁衍' },
            { name: '工蚁', desc: '负责抚育幼虫、修筑巢室、出外觅食与探索' },
            { name: '兵蚁', desc: '负责洞口警戒保卫、粉碎硬壳食物与抵御敌害' },
        ],
        categories: [
            {
                title: '协作机制与自组织',
                items: [
                    { term: '痕迹协作 (Stigmergy)', definition: '社会性昆虫之间的一种间接协调机制，个体在环境中留下的物理痕迹会激发后续个体的行动。' },
                    { term: '交哺营养共享 (Trophallaxis)', definition: '群体成员之间直接反刍并互相传递液态食物与化学信号的行为。' },
                    { term: '年龄多形行为 (Age Polyethism)', definition: '工蚁随年龄增长而表现出的规律性分工演变 (育幼 -> 筑巢 -> 觅食)。' },
                    { term: '群体感应 (Quorum Sensing)', definition: '个体密度或信息素浓度达到特定阈值时触发的集体决策转换机制。' }
                ]
            },
            {
                title: '品级分化与行为生态学',
                items: [
                    { term: '蚁后 (雌性生殖阶级)', definition: '负责整个群体繁衍的核心雌蚁，通过分泌特异性化学信息素维系群体秩序。' },
                    { term: '小型/中型工蚁', definition: '专注于巢内幼体护理、地下隧道开掘与近距离资源搜索的分工阶级。' },
                    { term: '兵蚁 (大型特化品级)', definition: '头部与上颚高度发达，专司巢穴入口防御、外敌威慑与硬物粉碎。' },
                    { term: '搬尸卫生行为 (Necrophoresis)', definition: '工蚁识别尸体释放的油酸气味后，将其移出巢室运往垃圾堆的卫生行为。' }
                ]
            },
            {
                title: '化学感应与踪迹信息素',
                items: [
                    { term: '活性食物轨迹', definition: '铺设于地表的挥发性化学梯度，用于引导采集工蚁群高效往返。' },
                    { term: '警报信息素', definition: '快速扩散的气味信号，根据浓度触发群体攻击动员或紧急疏散。' },
                    { term: '表皮碳氢化合物', definition: '覆盖于虫体表面的蜡质化学指纹，作为同巢个体身份识别密码。' }
                ]
            }
        ]
    }
}

export default function LegendGlossaryModal({ isOpen, onClose }) {
    const { theme, language } = useSimulationStore()
    const isDark = theme === 'dark'
    const [activeTab, setActiveTab] = useState('legend')
    const [searchQuery, setSearchQuery] = useState('')

    if (!isOpen) return null

    const langData = GLOSSARY_DATA[language] || GLOSSARY_DATA.fr

    return (
        <div style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0, 0, 0, 0.65)',
            backdropFilter: 'blur(6px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: 20
        }}>
            <div style={{
                width: '100%',
                maxWidth: 750,
                maxHeight: '85vh',
                background: isDark ? '#0f172a' : '#ffffff',
                border: isDark ? '1px solid #334155' : '1px solid #cbd5e1',
                borderRadius: 12,
                boxShadow: '0 20px 50px rgba(0,0,0,0.5)',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
                color: isDark ? '#f1f5f9' : '#0f172a'
            }}>
                {/* Modal Header */}
                <div style={{
                    padding: '16px 20px',
                    borderBottom: isDark ? '1px solid #1e293b' : '1px solid #e2e8f0',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                        <BookOpen size={20} color="#38bdf8" />
                        <div>
                            <h3 style={{ margin: 0, fontSize: 16, fontWeight: 800, color: '#38bdf8' }}>
                                {langData.modalTitle}
                            </h3>
                            <div style={{ fontSize: 11, color: isDark ? '#94a3b8' : '#64748b' }}>
                                {langData.modalSubtitle}
                            </div>
                        </div>
                    </div>

                    <button
                        onClick={onClose}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: isDark ? '#94a3b8' : '#64748b',
                            cursor: 'pointer',
                            padding: 4
                        }}
                    >
                        <X size={18} />
                    </button>
                </div>

                {/* Sub-Tabs */}
                <div style={{
                    display: 'flex',
                    borderBottom: isDark ? '1px solid #1e293b' : '1px solid #e2e8f0',
                    background: isDark ? '#1e293b' : '#f8fafc',
                    padding: '0 16px'
                }}>
                    <button
                        onClick={() => setActiveTab('legend')}
                        style={{
                            padding: '10px 18px',
                            background: 'transparent',
                            border: 'none',
                            borderBottom: activeTab === 'legend' ? '2px solid #38bdf8' : '2px solid transparent',
                            color: activeTab === 'legend' ? '#38bdf8' : (isDark ? '#94a3b8' : '#64748b'),
                            fontWeight: 700,
                            fontSize: 12,
                            cursor: 'pointer'
                        }}
                    >
                        {langData.tabLegend}
                    </button>
                    <button
                        onClick={() => setActiveTab('glossary')}
                        style={{
                            padding: '10px 18px',
                            background: 'transparent',
                            border: 'none',
                            borderBottom: activeTab === 'glossary' ? '2px solid #38bdf8' : '2px solid transparent',
                            color: activeTab === 'glossary' ? '#38bdf8' : (isDark ? '#94a3b8' : '#64748b'),
                            fontWeight: 700,
                            fontSize: 12,
                            cursor: 'pointer'
                        }}
                    >
                        {langData.tabGlossary}
                    </button>
                </div>

                {/* Body Content */}
                <div style={{ padding: 20, overflowY: 'auto', flex: 1, display: 'flex', flexDirection: 'column', gap: 20 }}>
                    {activeTab === 'legend' ? (
                        <>
                            {/* 1. Geological Substrates */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#38bdf8', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Layers size={15} /> {langData.substratesHeader}
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {langData.substrates.map((sub, i) => (
                                        <div key={i} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            background: isDark ? 'rgba(30,41,59,0.5)' : '#f1f5f9',
                                            padding: 8,
                                            borderRadius: 6
                                        }}>
                                            <div style={{ width: 18, height: 18, borderRadius: 4, background: sub.color, flexShrink: 0, border: '1px solid rgba(255,255,255,0.2)' }} />
                                            <div>
                                                <div style={{ fontWeight: 700, fontSize: 11 }}>{sub.name}</div>
                                                <div style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{sub.desc}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* 2. Pheromone Gradients (8 Canonical Channels - No English parentheticals, no redundant color text in descriptions) */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#a855f7', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Sparkles size={15} /> {langData.pheromonesHeader}
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {langData.pheromones.map((phero, i) => (
                                        <div key={i} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            background: isDark ? 'rgba(30,41,59,0.5)' : '#f1f5f9',
                                            padding: 8,
                                            borderRadius: 6
                                        }}>
                                            <div style={{ width: 16, height: 16, borderRadius: '50%', background: phero.color, flexShrink: 0, boxShadow: `0 0 8px ${phero.color}` }} />
                                            <div>
                                                <div style={{ fontWeight: 700, fontSize: 11 }}>{phero.name}</div>
                                                <div style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{phero.desc}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* 3. Castes */}
                            <div>
                                <h4 style={{ margin: '0 0 10px', fontSize: 13, fontWeight: 800, color: '#fbbf24', display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <Crown size={15} /> {langData.castesHeader}
                                </h4>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 10 }}>
                                    {langData.castes.map((caste, i) => (
                                        <div key={i} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            background: isDark ? 'rgba(30,41,59,0.5)' : '#f1f5f9',
                                            padding: 8,
                                            borderRadius: 6
                                        }}>
                                            {i === 0 ? <Crown size={15} color="#fbbf24" /> : (i === 1 ? <HardHat size={15} color="#38bdf8" /> : <Shield size={15} color="#ef4444" />)}
                                            <div>
                                                <div style={{ fontWeight: 700, fontSize: 11 }}>{caste.name}</div>
                                                <div style={{ fontSize: 9, color: isDark ? '#94a3b8' : '#64748b' }}>{caste.desc}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </>
                    ) : (
                        <>
                            {/* Search Input */}
                            <div style={{ position: 'relative' }}>
                                <Search size={14} color="#94a3b8" style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)' }} />
                                <input
                                    type="text"
                                    placeholder={langData.searchPlaceholder}
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    style={{
                                        width: '100%',
                                        padding: '8px 12px 8px 32px',
                                        background: isDark ? '#1e293b' : '#f8fafc',
                                        border: isDark ? '1px solid #334155' : '1px solid #cbd5e1',
                                        borderRadius: 6,
                                        color: isDark ? '#fff' : '#000',
                                        fontSize: 12
                                    }}
                                />
                            </div>

                            {/* Glossary List */}
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                                {langData.categories.map((cat, idx) => {
                                    const filteredItems = cat.items.filter(item =>
                                        item.term.toLowerCase().includes(searchQuery.toLowerCase()) ||
                                        item.definition.toLowerCase().includes(searchQuery.toLowerCase())
                                    )

                                    if (filteredItems.length === 0) return null

                                    return (
                                        <div key={idx}>
                                            <h4 style={{ margin: '0 0 8px', fontSize: 12, fontWeight: 800, color: '#38bdf8' }}>
                                                {cat.title}
                                            </h4>
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                                                {filteredItems.map((item, i) => (
                                                    <div key={i} style={{
                                                        background: isDark ? 'rgba(30,41,59,0.5)' : '#f8fafc',
                                                        padding: 10,
                                                        borderRadius: 6,
                                                        borderLeft: '3px solid #38bdf8'
                                                    }}>
                                                        <div style={{ fontWeight: 800, fontSize: 12, color: isDark ? '#fff' : '#0f172a', marginBottom: 3 }}>
                                                            {item.term}
                                                        </div>
                                                        <div style={{ fontSize: 11, color: isDark ? '#cbd5e1' : '#475569', lineHeight: 1.4 }}>
                                                            {item.definition}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    )
                                })}
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    )
}
