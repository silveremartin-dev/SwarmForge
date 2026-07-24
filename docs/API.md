# SwarmForge gRPC API

## Simulation Service

`org.swarmforge.protocol.SimulationService`

### Methods

| Method | Request | Response | Description |
|--------|---------|----------|-------------|
| `GetState` | `GetStateRequest` | `SimulationState` | Retrieve snapshot of current world state. |
| `StreamUpdates` | stream `ClientCommand` | stream `SimulationUpdate` | Real-time bidirectional stream for clients. |
| `Control` | `ControlRequest` | `ControlResponse` | Start/Pause/Stop simulation. |
| `SaveWorld` | `SaveWorldRequest` | `SaveWorldResponse` | Persist current world state to DB/Disk. |
| `LoadWorld` | `LoadWorldRequest` | `LoadWorldResponse` | Load world state. |

## Diplomacy Actions

New in v2.0.0.

### DiplomacyAction

Use `ClientCommand` with `DiplomacyAction` payload.

| Action Type | Description | Payload |
|-------------|-------------|---------|
| `PROPOSE_ALLIANCE` | Offer alliance to another colony. | - |
| `ACCEPT_ALLIANCE` | Accept incoming alliance offer. | - |
| `REJECT_ALLIANCE` | Reject offer. | - |
| `BREAK_ALLIANCE` | End existing alliance. | - |
| `DECLARE_WAR` | Set relationship to ENEMY. | - |
| `OFFER_TRIBUTE` | Send resources. | `resource_type`, `amount` |
