# Cognitive Architectures & Dynamic Brain Plugin System in SwarmForge

## 1. Overview & Biological Philosophy

In eusocial insect societies (*Formicidae*, *Apidae*, *Vespidae*, *Isoptera*), individual decision-making arises from decentralized local perception, chemical feedback loops, endocrine modulations, and behavioral plasticity. Rather than relying on a monolithic decision algorithm, **SwarmForge** provides an extensible, modular cognitive framework enabling researchers and developers to design, swap, and benchmark heterogeneous cognitive architectures.

SwarmForge supports four primary cognitive formats:
1. **Built-in Behavioral Architectures**: High-throughput rule-based FSM, BDI (Belief-Desire-Intention), Fuzzy Logic, and Deep Q-Network (DQN) models.
2. **ONNX Deep Learning Models (`.onnx`)**: Pre-trained neural networks (Reinforcement Learning, PPO, SAC, Actor-Critic, Graph Neural Networks) trained in Python (PyTorch, TensorFlow, Stable-Baselines3, RLlib) and evaluated with zero GC overhead.
3. **Dynamic Java Bytecode Plugins (`.jar` / `.class`)**: Compiled Java classes implementing the `ReasoningArchitecture` Service Provider Interface (SPI), hot-loaded into isolated sandboxed class loaders.
4. **Declarative Behavior Trees & State Machines (`.sfbrain` / `.json`)**: JSON-based declarative behavior definitions specifying conditions, sensory selectors, sequences, fallbacks, and action executors.

---

## 2. Core Architecture & Extensibility

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                     BrainPluginRegistry                                          │
│  - Built-in Enum Registry (FSM, BDI, FUZZY, REINFORCEMENT_LEARNING, HYBRID)                      │
│  - Dynamic Plugin Descriptors (ONNX, JAR SPI, JSON Behavior Trees)                                │
│  - Directory Scanners: data/brains/ & plugins/brains/                                            │
└───────────────┬───────────────────────────────┬───────────────────────────────────┬──────────────┘
                │                               │                                   │
                ▼                               ▼                                   ▼
┌───────────────────────────────┐ ┌───────────────────────────────┐ ┌───────────────────────────────┐
│     ONNX Neural Engine        │ │     Java SPI ClassLoader      │ │    Declarative JSON Trees     │
│   (Observation Vector d=24)   │ │  (Isolated JavaBrainClassLoader│ │     (JsonBrainArchitecture)     │
│   (Action Logits Vector d=14) │ │   ReasoningArchitecture SPI)  │ │ (Jackson Fast-Path Evaluator) │
└───────────────────────────────┘ └───────────────────────────────┘ └───────────────────────────────┘
```

### 2.1 The `ReasoningArchitecture` Contract

All cognitive architectures in SwarmForge implement the canonical `ReasoningArchitecture` interface:

```java
package org.swarmforge.core.behavior;

import org.swarmforge.core.domain.AgentView;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ActionType;
import org.swarmforge.core.world.World;

public interface ReasoningArchitecture {
    
    /**
     * Decides the next operational action for the given agent.
     *
     * @param agent The individual insect making the decision
     * @param world The current state of the simulation world
     * @return The chosen ActionType to execute this tick
     */
    ActionType decide(Individual agent, World world);

    /**
     * Decides action via AgentView abstraction (zero-cost ECS bridge).
     */
    default ActionType decide(AgentView agent, World world) {
        if (agent instanceof Individual ind) {
            return decide(ind, world);
        }
        return ActionType.IDLE;
    }

    /**
     * Resets or clears internal cognitive state / memory caches.
     */
    default void reset() {}

    /**
     * Factory method resolving built-ins and dynamic plugins via BrainPluginRegistry.
     */
    static ReasoningArchitecture create(String identifier) {
        return BrainPluginRegistry.getInstance().createBrain(identifier);
    }
}
```

---

## 3. Supported Brain Formats

### 3.1 ONNX Neural Network Format (`.onnx`)

SwarmForge uses a standardized state observation vector ($d_{obs} = 24$) and discrete action output space ($d_{act} = 14$) for deep reinforcement learning agents.

#### Sensory Observation Tensor ($d_{obs} = 24$)

| Index | Feature Description | Range | Biological Interpretation |
| :--- | :--- | :--- | :--- |
| `0` | Health / Vitality Ratio | $[0.0, 1.0]$ | Physical integrity and cuticle wear. |
| `1` | Energy / Glycogen Reserves | $[0.0, 1.0]$ | Hemolymph sugar level triggering hunger. |
| `2` | Hydration Level | $[0.0, 1.0]$ | Internal water balance / desiccation risk. |
| `3` | Age Ratio ($\text{Age} / \text{MaxLifespan}$) | $[0.0, 1.0]$ | Temporal polyethism progression. |
| `4` | Carrying Load Fraction | $[0.0, 1.0]$ | Mass carried relative to mandible carrying capacity. |
| `5` | Carried Item Type ID (Normalized) | $[0.0, 1.0]$ | Material ID (0 = None, 0.2 = Food, 0.4 = Larva, etc.). |
| `6` | Local Pheromone: Forage Gradient | $[0.0, 1.0]$ | Trail pheromone concentration heading to food source. |
| `7` | Local Pheromone: Home/Nest Gradient | $[0.0, 1.0]$ | Trail pheromone concentration leading back to nest. |
| `8` | Local Pheromone: Alarm Gradient | $[0.0, 1.0]$ | Volatile alarm pheromone (mandibular gland secretion). |
| `9` | Local Pheromone: Recruit Gradient | $[0.0, 1.0]$ | Mass-recruitment / tandem-running trail chemical. |
| `10` | Relative Distance to Nest Origin | $[0.0, 1.0]$ | Celestial/path-integration estimate to colony center. |
| `11` | Ambient Temperature Factor | $[0.0, 1.0]$ | Local microclimate temperature normalized to thermal limits. |
| `12` | Ambient Relative Humidity Factor | $[0.0, 1.0]$ | Local moisture level. |
| `13` | Ambient Light / Photoperiod Level | $[0.0, 1.0]$ | Solar irradiance (0.0 = dark underground, 1.0 = noon sun). |
| `14` | Nearby Nestmates Density | $[0.0, 1.0]$ | Antenna tactile contact rate with friendly nestmates. |
| `15` | Nearby Enemies / Predators Density | $[0.0, 1.0]$ | Chemical / visual perception of hostiles. |
| `16` | Nearby Food Density | $[0.0, 1.0]$ | Olfactory / visual detection of edible biomass. |
| `17` | Nearby Brood / Eggs Density | $[0.0, 1.0]$ | Perception of vulnerable brood needing nursing. |
| `18` | Endocrine: Juvenile Hormone (JH) | $[0.0, 1.0]$ | Regulates behavioral transition from nursing to foraging. |
| `19` | Endocrine: Ecdysone Titers | $[0.0, 1.0]$ | Molting / caste development regulator. |
| `20` | Endocrine: Octopamine Level | $[0.0, 1.0]$ | Stress & arousal neurotransmitter (fight-or-flight). |
| `21` | Velocity X (Normalized) | $[-1.0, 1.0]$ | Heading vector X component. |
| `22` | Velocity Y (Normalized) | $[-1.0, 1.0]$ | Heading vector Y component. |
| `23` | Velocity Z (Normalized) | $[-1.0, 1.0]$ | Heading vector Z component. |

#### Action Output Logits Space ($d_{act} = 14$)

The neural model must output a 1D tensor of length 14 (or $1 \times 14$), where the index with the maximum logit ($\text{argmax}$) selects the action:

| Index | `ActionType` Enum | Action Behavior |
| :--- | :--- | :--- |
| `0` | `IDLE` | Rest, groom cuticle, conserve metabolic energy. |
| `1` | `MOVE_FORWARD` | Advance along current heading vector. |
| `2` | `MOVE_TOWARD_TARGET` | Steer towards active sensory or navigation target. |
| `3` | `FORAGE` | Search for food, flowers, aphid colonies, or foliage. |
| `4` | `HARVEST` | Clip vegetation, extract honeydew, collect seeds. |
| `5` | `DIG` | Excavate substrate/soil voxel, create tunnels/chambers. |
| `6` | `BUILD` | Deposit construction pellet or paper/wax comb. |
| `7` | `FEED_LARVA` | Perform trophallaxis with developing brood. |
| `8` | `DEFEND` | Engage hostile invader with mandibles, sting, or acid spray. |
| `9` | `FLEE` | Evade threat along reverse threat vector. |
| `10` | `RETURN_TO_NEST` | Navigate towards nest entrance using path integration. |
| `11` | `COMMUNICATE` | Perform antennation, waggle dance, or lay pheromone. |
| `12` | `EXPLORE` | Levy-walk stochastic spatial exploration. |
| `13` | `BURROW_UNDERGROUND` | Excavate emergency shelter or enter subterranean chamber. |

#### Python Training & ONNX Export Example

```python
import torch
import torch.nn as nn

class AntBrainModel(nn.Module):
    def __init__(self, obs_dim=24, hidden_dim=64, act_dim=14):
        super().__init__()
        self.network = nn.Sequential(
            nn.Linear(obs_dim, hidden_dim),
            nn.LayerNorm(hidden_dim),
            nn.ReLU(),
            nn.Linear(hidden_dim, hidden_dim),
            nn.ReLU(),
            nn.Linear(hidden_dim, act_dim)
        )

    def forward(self, x):
        return self.network(x)

# Instantiate and export to ONNX
model = AntBrainModel()
model.eval()
dummy_input = torch.randn(1, 24)

torch.onnx.export(
    model,
    dummy_input,
    "leafcutter_forager_ppo.onnx",
    input_names=["obs"],
    output_names=["logits"],
    dynamic_axes={"obs": {0: "batch_size"}, "logits": {0: "batch_size"}},
    opset_version=17
)
print("Exported leafcutter_forager_ppo.onnx successfully!")
```

---

### 3.2 Dynamic Java SPI Plugin Format (`.jar` / `.class`)

Researchers can write custom high-speed cognitive algorithms directly in Java by implementing `ReasoningArchitecture`.

#### Writing a Custom Java Brain Class

```java
package com.myresearch.colony;

import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.domain.ActionType;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.world.World;

public class BioInspiredTrophallaxisBrain implements ReasoningArchitecture {

    @Override
    public ActionType decide(Individual agent, World world) {
        // High urgency: if energy is critical, prioritize food or trophallaxis
        if (agent.getEnergy() < 0.20f) {
            if (agent.getCarriedLoad() > 0) {
                return ActionType.HARVEST;
            }
            return ActionType.FORAGE;
        }

        // Brood care: if near larvae and well-fed, nurse
        if (agent.getAge() < (agent.getMaxLifespan() * 0.35f)) {
            return ActionType.FEED_LARVA;
        }

        // Foraging / Nesting
        if (agent.getCarriedLoad() > 0.8f) {
            return ActionType.RETURN_TO_NEST;
        }

        return ActionType.EXPLORE;
    }
}
```

#### Packaging and Metadata

When packaging into a `.jar`, provide a `brain-plugin.json` in the root:

```json
{
  "id": "myresearch-trophallaxis-brain",
  "name": "Bio-Inspired Trophallaxis & Brood Nurse Brain",
  "version": "1.2.0",
  "author": "Dr. Jane Doe (Entomology Lab)",
  "type": "JAVA_SPI",
  "mainClass": "com.myresearch.colony.BioInspiredTrophallaxisBrain",
  "description": "Adaptive age-polyethic decision loop prioritizing larval salivary exchange and trophallaxis."
}
```

---

### 3.3 Declarative JSON Behavior Trees (`.sfbrain` / `.json`)

For rapid prototyping without compiling code or training neural networks, SwarmForge includes a declarative behavior tree evaluator (`JsonBrainArchitecture`).

#### Behavior Tree File Example: `desert_ant_cataglyphis.sfbrain`

```json
{
  "id": "desert-ant-cataglyphis",
  "name": "Cataglyphis Fortis Polarized Light Navigator",
  "version": "1.0.0",
  "author": "SwarmForge Ethology Team",
  "type": "DECLARATIVE_JSON",
  "description": "High-temperature desert scavenger using polarized skylight navigation and thermal evasion.",
  "root": {
    "type": "SELECTOR",
    "children": [
      {
        "type": "SEQUENCE",
        "description": "Thermal emergency evasion: escape critical soil heat",
        "condition": {
          "sensor": "TEMPERATURE",
          "operator": "GREATER_THAN",
          "value": 48.0
        },
        "action": "BURROW_UNDERGROUND"
      },
      {
        "type": "SEQUENCE",
        "description": "Critical hunger: forage immediately",
        "condition": {
          "sensor": "ENERGY",
          "operator": "LESS_THAN",
          "value": 0.25
        },
        "action": "FORAGE"
      },
      {
        "type": "SEQUENCE",
        "description": "Harvest prey when found",
        "condition": {
          "sensor": "NEARBY_FOOD",
          "operator": "GREATER_THAN",
          "value": 0.05
        },
        "action": "HARVEST"
      },
      {
        "type": "SEQUENCE",
        "description": "Return home when carrying heavy thermal prey",
        "condition": {
          "sensor": "CARRIED_LOAD",
          "operator": "GREATER_THAN",
          "value": 0.50
        },
        "action": "RETURN_TO_NEST"
      },
      {
        "type": "FALLBACK",
        "action": "EXPLORE"
      }
    ]
  }
}
```

---

## 4. Visual Studio Brain Importer

The **SwarmForge Species Studio** (`SpeciesEditorPane`) provides a dedicated visual import workflow:

1. Open the **Species Editor** tab in `swarmforge-editor`.
2. Under the **Cognitive Architecture** section, select or click **Import Brain...** (`btnImportBrain`).
3. In the **Brain Architecture Importer** dialog:
   - **Browse** for your `.onnx`, `.jar`, `.class`, `.sfbrain`, or `.json` file.
   - The dialog automatically validates the file structure and inspects class bytecode / neural metadata.
   - Customize the **Identifier**, **Display Name**, **Author**, and **Description**.
   - Click **Import & Register Brain**.
4. The file is copied to the persistent `data/brains/` storage directory and immediately registered in `BrainPluginRegistry`.
5. The new cognitive architecture is now instantly available in the Species Editor dropdown and ready for simulation ticks.

---

## 5. Directory Layout & Auto-Discovery

At application startup, `BrainPluginRegistry` scans the following locations automatically:
- `data/brains/`: User-imported ONNX models, JSON behavior trees, and JAR plugins.
- `plugins/brains/`: Enterprise/academic system plugins.

Files placed in these folders are hot-discovered, parsed, and registered on startup without requiring manual UI configuration.
