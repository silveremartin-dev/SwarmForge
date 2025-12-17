import { useMemo } from 'react'
import * as THREE from 'three'
import { useSimulationStore } from '../store/simulationStore'

const CHAMBER_COLORS = {
    QUEEN_QUARTERS: '#9933ff', // Royal Purple
    NURSERY: '#ff9999',       // Soft Pink
    FOOD_STORAGE: '#66ff66',  // Green
    WASTE_DUMP: '#663300',    // Brown
    ENTRANCE: '#ffff00'       // Yellow
}

function Tunnel({ start, end }) {
    const { position, rotation, length } = useMemo(() => {
        const startVec = new THREE.Vector3(start.x, start.y, start.z)
        const endVec = new THREE.Vector3(end.x, end.y, end.z)

        const length = startVec.distanceTo(endVec)
        const position = startVec.clone().add(endVec).multiplyScalar(0.5)

        const direction = endVec.clone().sub(startVec).normalize()
        const quaternion = new THREE.Quaternion().setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction)
        const rotation = new THREE.Euler().setFromQuaternion(quaternion)

        return { position, rotation, length }
    }, [start, end])

    return (
        <mesh position={position} rotation={rotation}>
            <cylinderGeometry args={[0.5, 0.5, length, 8]} />
            <meshStandardMaterial color="#885533" />
        </mesh>
    )
}

export default function UndergroundView() {
    const { nests } = useSimulationStore()

    if (!nests || nests.length === 0) return null

    return (
        <group>
            {nests.map(nest => (
                <group key={nest.id}>
                    {/* Render Chambers */}
                    {nest.chambers && nest.chambers.map(chamber => (
                        <mesh key={chamber.id} position={[chamber.position.x, chamber.position.y, chamber.position.z]}>
                            <sphereGeometry args={[2, 16, 16]} />
                            <meshStandardMaterial
                                color={CHAMBER_COLORS[chamber.type] || '#cccccc'}
                                transparent
                                opacity={0.8}
                            />
                        </mesh>
                    ))}

                    {/* Render Tunnels */}
                    {nest.tunnels && nest.tunnels.map((tunnel, idx) => {
                        const startChamber = nest.chambers.find(c => c.id === tunnel.startChamberId)
                        const endChamber = nest.chambers.find(c => c.id === tunnel.endChamberId)

                        if (startChamber && endChamber) {
                            return (
                                <Tunnel
                                    key={`${nest.id}-tunnel-${idx}`}
                                    start={startChamber.position}
                                    end={endChamber.position}
                                />
                            )
                        }
                        return null
                    })}
                </group>
            ))}
        </group>
    )
}
