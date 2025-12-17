import { useXR, useController } from '@react-three/xr'
import { useFrame } from '@react-three/fiber'
import { useRef, useState } from 'react'
import * as THREE from 'three'

export default function ImmersiveControls() {
    const { player } = useXR()
    const leftController = useController('left')
    const rightController = useController('right')

    const [mode, setMode] = useState('god') // 'god' or 'ant'
    const lastPress = useRef(0)

    useFrame((state, delta) => {
        // Toggle Mode on Button Press (Right 'A' or 'Trigger' depending on binding, usually 0 or 4)
        if (rightController && rightController.inputSource.gamepad) {
            const gamepad = rightController.inputSource.gamepad
            // Button 0 (A) or 4 (Trigger/Select)
            if (gamepad.buttons[4] && gamepad.buttons[4].pressed) {
                const now = Date.now()
                if (now - lastPress.current > 500) { // Debounce
                    setMode(m => m === 'god' ? 'ant' : 'god')
                    lastPress.current = now
                }
            }
        }

        // Movement Speed depends on mode
        const moveSpeed = mode === 'god' ? 10 : 2.0

        // Height Constraint
        if (mode === 'ant') {
            // Keep head near ground (~0.5 units high, ant-scale)
            // Ideally we Raycast to found ground height, but for now fixed Y=2
            player.position.y = THREE.MathUtils.lerp(player.position.y, 1.0, 0.1)
        } else {
            // God mode allows free vertical movement via Right Stick
        }

        // Simple movement logic: Left stick moves player
        if (leftController && leftController.inputSource.gamepad) {
            const axes = leftController.inputSource.gamepad.axes
            // axes[2] is X (left/right), axes[3] is Y (up/down) usually
            const dx = axes[2] || 0
            const dz = axes[3] || 0

            if (Math.abs(dx) > 0.1 || Math.abs(dz) > 0.1) {
                const speed = moveSpeed * delta
                player.position.x += dx * speed
                player.position.z += dz * speed
            }
        }

        // Vertical movement logic: Right stick (Only in God Mode)
        if (mode === 'god' && rightController && rightController.inputSource.gamepad) {
            const axes = rightController.inputSource.gamepad.axes
            const dy = axes[3] || 0

            if (Math.abs(dy) > 0.1) {
                const speed = moveSpeed * delta
                player.position.y -= dy * speed
            }
        }
    })

    return null
}
