import React, { useState } from 'react';
import { Play, Save, FolderOpen, Plus, Zap, Award, Layers, Thermometer, Cpu, Globe } from 'lucide-react';
import { showToast } from '../store/toastStore';

const PRESET_SCENARIOS = [
  {
    id: 'ACAD_01_LEVY_BROWNIAN',
    title: 'Vol de Lévy vs Marche Brownienne',
    academicCategory: 'Optimal Foraging Theory',
    masterSeed: 12345,
    description: 'Comparaison de l\'efficacité de récolte de nourriture entre des ouvrières guidées par Réseau de Neurones (Lévy Flight) et par Marche Brownienne.',
    biome: 'TEMPERATE_FOREST',
    width: 256, height: 256, depth: 64,
    soilDensity: 0.6,
    initialTemp: 22, initialHumidity: 0.65,
    colonies: [
      { id: 'COLONY_LEVY', species: 'Formica fusca (Lévy RL)', queenCount: 1, workerCount: 100, workerEngine: 'NEURAL_NETWORK' },
      { id: 'COLONY_BROWNIAN', species: 'Formica fusca (Brownian FSM)', queenCount: 1, workerCount: 100, workerEngine: 'FINITE_STATE_MACHINE' }
    ],
    targetMetrics: ['FORAGING_EFFICIENCY_INDEX', 'MEAN_SEARCH_TIME_PER_ITEM', 'TRAIL_BIFURCATION_COUNT']
  },
  {
    id: 'ACAD_02_POLYETHISM_BDI',
    title: 'Polyéthisme & Spécialisation BDI',
    academicCategory: 'Division du Travail & Raisonnement Symbolique',
    masterSeed: 424242,
    description: 'Analyse de l\'émergence de la division du travail (soin du couvain, excavation, récolte) via un moteur BDI (Croyances-Désirs-Intentions).',
    biome: 'MEDITERRANEAN_SCRUB',
    width: 256, height: 256, depth: 64,
    soilDensity: 0.7,
    initialTemp: 26, initialHumidity: 0.50,
    colonies: [
      { id: 'COLONY_BDI', species: 'Messor barbarus (BDI)', queenCount: 1, workerCount: 150, workerEngine: 'BDI', soldierEngine: 'BDI' }
    ],
    targetMetrics: ['TASK_ALLOCATION_ENTROPY', 'SPECIALIZATION_INDEX', 'BROOD_SURVIVAL_RATE']
  },
  {
    id: 'ACAD_03_NEST_MORPHOGENESIS',
    title: 'Morphogenèse du Nid & Microclimat',
    academicCategory: 'Bio-Architecture & Stigmergie',
    masterSeed: 99999,
    description: 'Étude de la géométrie émergente des tunnels et de la régulation thermique du nid sous-terrain.',
    biome: 'SUBTERRANEAN_CAVE',
    width: 384, height: 384, depth: 96,
    soilDensity: 0.8,
    initialTemp: 18, initialHumidity: 0.85,
    colonies: [
      { id: 'COLONY_DIGGERS', species: 'Lasius niger (Excavateurs BT)', queenCount: 1, workerCount: 200, workerEngine: 'BEHAVIOR_TREE' }
    ],
    targetMetrics: ['TUNNEL_FRACTAL_DIMENSION', 'CHAMBER_DEPTH_DISTRIBUTION', 'THERMAL_STABILITY_DELTA']
  },
  {
    id: 'ACAD_04_INTERSPECIFIC_COMPETITION',
    title: 'Compétition Interspécifique Territorial',
    academicCategory: 'Écologie des Populations',
    masterSeed: 777777,
    description: 'Conflit territorial entre une espèce indigène monogyne (Lasius niger) et une espèce invasive polygyne (Linepithema humile).',
    biome: 'TROPICAL_RAINFOREST',
    width: 512, height: 512, depth: 64,
    soilDensity: 0.5,
    initialTemp: 28, initialHumidity: 0.80,
    colonies: [
      { id: 'COLONY_NATIVE', species: 'Lasius niger (Indigène)', queenCount: 1, workerCount: 120, workerEngine: 'BEHAVIOR_TREE' },
      { id: 'COLONY_INVASIVE', species: 'Linepithema humile (Invasive)', queenCount: 3, workerCount: 250, workerEngine: 'NEURAL_NETWORK' }
    ],
    targetMetrics: ['TERRITORIAL_DOMINANCE_RATIO', 'MORTALITY_CONTEST_RATE', 'RESOURCE_MONOPOLIZATION_SPEED']
  },
  {
    id: 'ACAD_09_DULOSIS_RAID',
    title: 'Raid Duloce & Esclavage (Polyergus vs Formica)',
    academicCategory: 'Sociobiologie & Parasitisme Social',
    masterSeed: 555123,
    description: 'Raid d\'esclavage obligatoire : colonne d\'assaut de Polyergus rufescens infiltrant un nid de Formica fusca pour capturer le couvain (cocons) et le rapatrier au nid.',
    biome: 'TEMPERATE_FOREST',
    width: 350, height: 350, depth: 48,
    soilDensity: 0.65,
    initialTemp: 23, initialHumidity: 0.70,
    colonies: [
      { id: 'COLONY_POLYERGUS', species: 'Polyergus rufescens (Amazones)', queenCount: 1, workerCount: 80, soldierCount: 40, workerEngine: 'BEHAVIOR_TREE' },
      { id: 'COLONY_FORMICA', species: 'Formica fusca (Hôte)', queenCount: 1, workerCount: 150, workerEngine: 'BEHAVIOR_TREE' }
    ],
    targetMetrics: ['PUPAE_CAPTURED_COUNT', 'RAID_COLUMN_COHESION', 'REPATRIATION_SUCCESS_RATE', 'HOST_DEFENSE_CASUALTIES']
  }
];

export default function ScenarioEditorPanel({ onLaunchScenario }) {
  const [selectedPresetId, setSelectedPresetId] = useState(PRESET_SCENARIOS[0].id);
  const [activeTab, setActiveTab] = useState('meta');
  const [scenarioData, setScenarioData] = useState(PRESET_SCENARIOS[0]);

  const handleSelectPreset = (e) => {
    const presetId = e.target.value;
    setSelectedPresetId(presetId);
    const found = PRESET_SCENARIOS.find(p => p.id === presetId);
    if (found) {
      setScenarioData(JSON.parse(JSON.stringify(found)));
    }
  };

  const handleExportJSON = () => {
    const jsonStr = JSON.stringify(scenarioData, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${scenarioData.id || 'scenario'}.json`;
    a.click();
  };

  const handleImportJSON = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (evt) => {
      try {
        const parsed = JSON.parse(evt.target.result);
        setScenarioData(parsed);
        showToast('Scénario JSON importé avec succès', 'success');
      } catch (err) {
        showToast('Fichier de scénario invalide JSON', 'error');
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="flex flex-col h-full bg-zinc-950 text-zinc-100 border border-zinc-800 rounded-xl overflow-hidden shadow-2xl">
      {/* Header */}
      <div className="flex items-center justify-between px-6 py-4 bg-zinc-900 border-b border-zinc-800">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-sky-500/20 text-sky-400 rounded-lg">
            <Globe className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-sky-400">Éditeur de Scénario de Simulation</h2>
            <p className="text-xs text-zinc-400">Composition dynamique multi-espèces, détermisme et moteurs décisionnels (BDI, Neural RL, FSM, BT)</p>
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <select 
            value={selectedPresetId} 
            onChange={handleSelectPreset}
            className="bg-zinc-800 border border-zinc-700 text-xs text-zinc-200 rounded-lg px-3 py-2 focus:ring-2 focus:ring-sky-500 outline-none"
          >
            {PRESET_SCENARIOS.map(p => (
              <option key={p.id} value={p.id}>{p.title}</option>
            ))}
          </select>

          <label className="flex items-center space-x-1.5 px-3 py-2 bg-zinc-800 hover:bg-zinc-700 text-xs font-semibold rounded-lg cursor-pointer transition">
            <FolderOpen className="w-4 h-4 text-zinc-400" />
            <span>Importer JSON</span>
            <input type="file" accept=".json" onChange={handleImportJSON} className="hidden" />
          </label>

          <button 
            onClick={handleExportJSON}
            className="flex items-center space-x-1.5 px-3 py-2 bg-zinc-800 hover:bg-zinc-700 text-xs font-semibold rounded-lg transition"
          >
            <Save className="w-4 h-4 text-zinc-400" />
            <span>Sauvegarder JSON</span>
          </button>

          <button 
            onClick={() => onLaunchScenario && onLaunchScenario(scenarioData)}
            className="flex items-center space-x-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg shadow-lg shadow-emerald-900/30 transition transform active:scale-95"
          >
            <Play className="w-4 h-4 fill-white" />
            <span>LANCER SIMULATION</span>
          </button>
        </div>
      </div>

      {/* Main Body */}
      <div className="flex flex-1 overflow-hidden">
        {/* Navigation Sidebar */}
        <div className="w-64 bg-zinc-900/60 border-r border-zinc-800 p-4 space-y-2">
          {[
            { id: 'meta', label: '1. Métadonnées & Graine', icon: Award },
            { id: 'world', label: '2. Monde & Substrat', icon: Layers },
            { id: 'climate', label: '3. Climat & Photopériode', icon: Thermometer },
            { id: 'colony', label: '4. Castes & Moteurs IA', icon: Cpu },
            { id: 'metrics', label: '5. Télémétrie Académique', icon: Zap }
          ].map(tab => {
            const Icon = tab.icon;
            const active = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center space-x-3 w-full px-3 py-2.5 rounded-lg text-xs font-medium transition ${
                  active ? 'bg-sky-500/20 text-sky-400 border border-sky-500/30' : 'text-zinc-400 hover:bg-zinc-800/60'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Dynamic Form Area */}
        <div className="flex-1 p-6 overflow-y-auto bg-zinc-950">
          {activeTab === 'meta' && (
            <div className="space-y-4 max-w-2xl">
              <h3 className="text-sm font-bold text-sky-400 uppercase tracking-wider">Spécification du Scénario</h3>
              
              <div>
                <label className="block text-xs font-semibold text-zinc-400 mb-1">Titre du Scénario</label>
                <input 
                  type="text" 
                  value={scenarioData.title}
                  onChange={e => setScenarioData({...scenarioData, title: e.target.value})}
                  className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 focus:border-sky-500 outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-zinc-400 mb-1">Catégorie Académique</label>
                <input 
                  type="text" 
                  value={scenarioData.academicCategory}
                  onChange={e => setScenarioData({...scenarioData, academicCategory: e.target.value})}
                  className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 focus:border-sky-500 outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-zinc-400 mb-1">Graine Maître Déterministe (Master Seed)</label>
                <input 
                  type="number" 
                  value={scenarioData.masterSeed}
                  onChange={e => setScenarioData({...scenarioData, masterSeed: parseInt(e.target.value) || 0})}
                  className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-sky-400 font-mono focus:border-sky-500 outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-zinc-400 mb-1">Description Scientifique</label>
                <textarea 
                  rows={4}
                  value={scenarioData.description}
                  onChange={e => setScenarioData({...scenarioData, description: e.target.value})}
                  className="w-full bg-zinc-900 border border-zinc-800 rounded-lg px-3 py-2 text-sm text-zinc-100 focus:border-sky-500 outline-none"
                />
              </div>
            </div>
          )}

          {activeTab === 'colony' && (
            <div className="space-y-6 max-w-3xl">
              <h3 className="text-sm font-bold text-sky-400 uppercase tracking-wider">Colonies & Moteurs d'IA par Caste</h3>

              {scenarioData.colonies.map((colony, idx) => (
                <div key={idx} className="p-4 bg-zinc-900 border border-zinc-800 rounded-xl space-y-3">
                  <div className="flex items-center justify-between border-b border-zinc-800 pb-2">
                    <span className="text-xs font-bold text-sky-400">Colonie #{idx + 1} ({colony.id})</span>
                    <input 
                      type="text"
                      value={colony.species}
                      onChange={e => {
                        const newCols = [...scenarioData.colonies];
                        newCols[idx].species = e.target.value;
                        setScenarioData({...scenarioData, colonies: newCols});
                      }}
                      className="bg-zinc-800 text-xs px-2 py-1 rounded text-zinc-200 border border-zinc-700"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs text-zinc-400 mb-1">Nombre d'Ouvrières</label>
                      <input 
                        type="number"
                        value={colony.workerCount}
                        onChange={e => {
                          const newCols = [...scenarioData.colonies];
                          newCols[idx].workerCount = parseInt(e.target.value) || 0;
                          setScenarioData({...scenarioData, colonies: newCols});
                        }}
                        className="w-full bg-zinc-800 border border-zinc-700 text-xs px-2 py-1.5 rounded text-zinc-100"
                      />
                    </div>

                    <div>
                      <label className="block text-xs text-zinc-400 mb-1">Moteur d'IA Caste Ouvrière</label>
                      <select 
                        value={colony.workerEngine}
                        onChange={e => {
                          const newCols = [...scenarioData.colonies];
                          newCols[idx].workerEngine = e.target.value;
                          setScenarioData({...scenarioData, colonies: newCols});
                        }}
                        className="w-full bg-zinc-800 border border-zinc-700 text-xs px-2 py-1.5 rounded text-sky-300 font-semibold"
                      >
                        <option value="NEURAL_NETWORK">Réseau de Neurones (RL)</option>
                        <option value="BDI">Symbolique (BDI: Croyances/Désirs/Intentions)</option>
                        <option value="BEHAVIOR_TREE">Arbre de Comportement (Behavior Tree)</option>
                        <option value="FINITE_STATE_MACHINE">Machine à États (FSM)</option>
                        <option value="FUZZY_LOGIC">Logique Floue (Fuzzy Logic)</option>
                      </select>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {activeTab === 'metrics' && (
            <div className="space-y-4 max-w-xl">
              <h3 className="text-sm font-bold text-sky-400 uppercase tracking-wider">Métriques Académiques à Enregistrer</h3>
              {scenarioData.targetMetrics.map((metric, i) => (
                <div key={i} className="flex items-center space-x-3 p-3 bg-zinc-900 border border-zinc-800 rounded-lg">
                  <Zap className="w-4 h-4 text-emerald-400" />
                  <span className="text-xs font-mono text-emerald-300">{metric}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
