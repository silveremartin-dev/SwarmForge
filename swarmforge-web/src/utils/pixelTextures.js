import * as THREE from 'three'

/**
 * Helper to build a 16x16 pixel-art CanvasTexture with NearestFilter (zero blur, crisp pixel-art).
 */
function createPixelTexture(drawFn, width = 16, height = 16) {
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const ctx = canvas.getContext('2d')
    ctx.imageSmoothingEnabled = false
    drawFn(ctx, width, height)

    const texture = new THREE.CanvasTexture(canvas)
    texture.magFilter = THREE.NearestFilter
    texture.minFilter = THREE.NearestFilter
    texture.generateMipmaps = false
    return texture
}

// 1. Minecraft Grass Top (Lush Green with pixel noise)
export function getPixelGrassTopTexture() {
    return createPixelTexture((ctx) => {
        const palette = ['#4d7c0f', '#5b8c32', '#3f6212', '#65a30d', '#446e10']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const rnd = (Math.sin(x * 12.9898 + y * 78.233) * 43758.5453) % 1
                const colorIdx = Math.floor(Math.abs(rnd) * palette.length) % palette.length
                ctx.fillStyle = palette[colorIdx]
                ctx.fillRect(x, y, 1, 1)
            }
        }
    })
}

// 2. Minecraft Grass Side (Green overhang over rich brown dirt)
export function getPixelGrassSideTexture() {
    return createPixelTexture((ctx) => {
        const dirtPalette = ['#593d25', '#452b18', '#6b4c33', '#4a321f']
        const grassPalette = ['#4d7c0f', '#5b8c32', '#3f6212', '#65a30d']

        // Dirt base
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const rnd = (Math.sin(x * 15.3 + y * 35.7) * 43758.5453) % 1
                ctx.fillStyle = dirtPalette[Math.floor(Math.abs(rnd) * dirtPalette.length) % dirtPalette.length]
                ctx.fillRect(x, y, 1, 1)
            }
        }

        // Stepped Grass Top Overhang
        for (let x = 0; x < 16; x++) {
            const overhang = 3 + Math.floor(Math.abs((Math.sin(x * 3.14) * 2)))
            for (let y = 0; y < overhang; y++) {
                const rnd = (Math.sin(x * 9.1 + y * 18.2) * 43758.5453) % 1
                ctx.fillStyle = grassPalette[Math.floor(Math.abs(rnd) * grassPalette.length) % grassPalette.length]
                ctx.fillRect(x, y, 1, 1)
            }
        }
    })
}

// 3. Minecraft Cobblestone / Rock Cliff
export function getPixelCobbleTexture() {
    return createPixelTexture((ctx) => {
        const stonePalette = ['#64748b', '#475569', '#334155', '#78716c', '#57534e']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const isBorder = (x % 4 === 0 && y % 4 === 0) || (x === 0 || y === 0 || x === 15 || y === 15)
                if (isBorder && (x + y) % 3 === 0) {
                    ctx.fillStyle = '#1e293b' // Dark mortar seam
                } else {
                    const rnd = (Math.sin(x * 7.7 + y * 9.9) * 43758.5453) % 1
                    ctx.fillStyle = stonePalette[Math.floor(Math.abs(rnd) * stonePalette.length) % stonePalette.length]
                }
                ctx.fillRect(x, y, 1, 1)
            }
        }
    })
}

// 4. Minecraft Sand (Rive / Plage)
export function getPixelSandTexture() {
    return createPixelTexture((ctx) => {
        const sandPalette = ['#d4b483', '#c2a675', '#e2c99b', '#b59765']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const rnd = (Math.sin(x * 11.2 + y * 22.4) * 43758.5453) % 1
                ctx.fillStyle = sandPalette[Math.floor(Math.abs(rnd) * sandPalette.length) % sandPalette.length]
                ctx.fillRect(x, y, 1, 1)
            }
        }
    })
}

// 5. 2.5D Cross-Mesh Red Poppy (Coquelicot Minecraft)
export function getPixelPoppyTexture() {
    return createPixelTexture((ctx) => {
        ctx.clearRect(0, 0, 16, 16)
        // Green stem
        ctx.fillStyle = '#22c55e'
        ctx.fillRect(7, 6, 2, 10)
        ctx.fillStyle = '#15803d'
        ctx.fillRect(6, 10, 1, 3)
        ctx.fillRect(9, 12, 1, 2)

        // Red blossom petals
        ctx.fillStyle = '#dc2626'
        ctx.fillRect(5, 2, 6, 5)
        ctx.fillStyle = '#ef4444'
        ctx.fillRect(6, 3, 4, 3)
        ctx.fillStyle = '#991b1b'
        ctx.fillRect(5, 6, 6, 1)

        // Black flower center
        ctx.fillStyle = '#18181b'
        ctx.fillRect(7, 4, 2, 2)
    })
}

// 6. 2.5D Cross-Mesh Yellow Dandelion (Pissenlit Minecraft)
export function getPixelDandelionTexture() {
    return createPixelTexture((ctx) => {
        ctx.clearRect(0, 0, 16, 16)
        // Green stem
        ctx.fillStyle = '#22c55e'
        ctx.fillRect(7, 7, 2, 9)

        // Yellow flower head
        ctx.fillStyle = '#eab308'
        ctx.fillRect(5, 3, 6, 5)
        ctx.fillStyle = '#fde047'
        ctx.fillRect(6, 4, 4, 3)
        ctx.fillStyle = '#ca8a04'
        ctx.fillRect(5, 7, 6, 1)
    })
}

// 7. 2.5D Cross-Mesh Tall Wild Grass (Herbe haute Minecraft)
export function getPixelTallGrassTexture() {
    return createPixelTexture((ctx) => {
        ctx.clearRect(0, 0, 16, 16)
        const blades = [
            { x: 3, h: 10, col: '#4d7c0f' },
            { x: 5, h: 14, col: '#65a30d' },
            { x: 7, h: 15, col: '#5b8c32' },
            { x: 9, h: 13, col: '#4d7c0f' },
            { x: 11, h: 12, col: '#65a30d' },
            { x: 13, h: 8, col: '#3f6212' },
        ]
        blades.forEach(b => {
            ctx.fillStyle = b.col
            ctx.fillRect(b.x, 16 - b.h, 2, b.h)
        })
    })
}

// 8. River Lily Pad (Nénuphar Minecraft)
export function getPixelLilyPadTexture() {
    return createPixelTexture((ctx) => {
        ctx.clearRect(0, 0, 16, 16)
        ctx.fillStyle = '#16a34a'
        ctx.fillRect(2, 2, 12, 12)
        ctx.fillStyle = '#15803d'
        ctx.fillRect(3, 3, 10, 10)
        ctx.fillStyle = '#22c55e'
        ctx.fillRect(4, 4, 8, 8)

        // Notch cutout
        ctx.clearRect(8, 0, 3, 8)
        // Flower bud on top
        ctx.fillStyle = '#f43f5e'
        ctx.fillRect(10, 5, 2, 2)
        ctx.fillStyle = '#fb7185'
        ctx.fillRect(11, 4, 1, 1)
    })
}

// 9. Sugar Cane Reeds (Canne à sucre Minecraft)
export function getPixelSugarCaneTexture() {
    return createPixelTexture((ctx) => {
        ctx.clearRect(0, 0, 16, 16)
        ctx.fillStyle = '#84cc16'
        ctx.fillRect(4, 0, 3, 16)
        ctx.fillRect(9, 0, 3, 16)

        // Reed node rings
        ctx.fillStyle = '#4d7c0f'
        ctx.fillRect(4, 4, 3, 1)
        ctx.fillRect(4, 9, 3, 1)
        ctx.fillRect(4, 14, 3, 1)
        ctx.fillRect(9, 3, 3, 1)
        ctx.fillRect(9, 8, 3, 1)
        ctx.fillRect(9, 13, 3, 1)
    })
}

// 10. Minecraft Oak Log Side (Écorce de chêne)
export function getPixelOakLogSideTexture() {
    return createPixelTexture((ctx) => {
        const barkPalette = ['#6b4423', '#57371c', '#4a2f17', '#3d2511', '#7a4f2b']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const stripe = (x % 3 === 0) ? 1 : 0
                const rnd = (Math.sin(x * 5.3 + y * 17.7) * 43758.5453) % 1
                const idx = (Math.floor(Math.abs(rnd) * (barkPalette.length - 1)) + stripe) % barkPalette.length
                ctx.fillStyle = barkPalette[idx]
                ctx.fillRect(x, y, 1, 1)
            }
        }
        // Vertical bark grooves
        ctx.fillStyle = '#2e1c0c'
        for (let y = 0; y < 16; y++) {
            if ((y + 1) % 4 !== 0) {
                ctx.fillRect(4, y, 1, 1)
                ctx.fillRect(11, y, 1, 1)
            }
        }
    })
}

// 11. Minecraft Oak Log Top (Cernes de bois de chêne)
export function getPixelOakLogTopTexture() {
    return createPixelTexture((ctx) => {
        // Outer bark rim
        ctx.fillStyle = '#4a2f17'
        ctx.fillRect(0, 0, 16, 16)
        // Inner heartwood
        ctx.fillStyle = '#b8945f'
        ctx.fillRect(2, 2, 12, 12)
        // Tree rings
        ctx.fillStyle = '#8f6e3c'
        ctx.strokeRect(3.5, 3.5, 9, 9)
        ctx.strokeRect(5.5, 5.5, 5, 5)
        ctx.fillStyle = '#6b5029'
        ctx.fillRect(7, 7, 2, 2)
    })
}

// 12. Minecraft Oak Leaves (Feuilles de chêne avec découpes alpha)
export function getPixelOakLeavesTexture() {
    return createPixelTexture((ctx) => {
        const leafPalette = ['#2e7d32', '#388e3c', '#1b5e20', '#4caf50', '#14532d']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const rnd = (Math.sin(x * 9.7 + y * 23.1) * 43758.5453) % 1
                // 15% transparent cutout holes typical of Minecraft fast/fancy leaves
                if (Math.abs(rnd) < 0.12 && (x + y) % 2 === 0) {
                    ctx.clearRect(x, y, 1, 1)
                } else {
                    const idx = Math.floor(Math.abs(rnd) * leafPalette.length) % leafPalette.length
                    ctx.fillStyle = leafPalette[idx]
                    ctx.fillRect(x, y, 1, 1)
                }
            }
        }
    })
}

// 13. Minecraft Birch Log (Tronc de bouleau blanc avec striations sombres)
export function getPixelBirchLogTexture() {
    return createPixelTexture((ctx) => {
        ctx.fillStyle = '#e2e8f0'
        ctx.fillRect(0, 0, 16, 16)
        const whitePalette = ['#ffffff', '#f1f5f9', '#e2e8f0', '#cbd5e1']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const rnd = (Math.sin(x * 13.1 + y * 7.9) * 43758.5453) % 1
                ctx.fillStyle = whitePalette[Math.floor(Math.abs(rnd) * whitePalette.length) % whitePalette.length]
                ctx.fillRect(x, y, 1, 1)
            }
        }
        // Horizontal black spots
        ctx.fillStyle = '#1e293b'
        ctx.fillRect(2, 3, 3, 1)
        ctx.fillRect(9, 7, 4, 1)
        ctx.fillRect(3, 11, 4, 1)
        ctx.fillRect(11, 13, 2, 1)
    })
}

// 14. Minecraft Birch Leaves (Feuilles de bouleau vert clair)
export function getPixelBirchLeavesTexture() {
    return createPixelTexture((ctx) => {
        const birchLeafPalette = ['#65a30d', '#84cc16', '#4d7c0f', '#a3e635', '#3f6212']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const rnd = (Math.sin(x * 11.3 + y * 19.4) * 43758.5453) % 1
                if (Math.abs(rnd) < 0.12 && (x + y) % 2 === 0) {
                    ctx.clearRect(x, y, 1, 1)
                } else {
                    ctx.fillStyle = birchLeafPalette[Math.floor(Math.abs(rnd) * birchLeafPalette.length) % birchLeafPalette.length]
                    ctx.fillRect(x, y, 1, 1)
                }
            }
        }
    })
}

// 15. Minecraft Pine / Spruce Log (Épinette sombre)
export function getPixelPineLogTexture() {
    return createPixelTexture((ctx) => {
        const pineLogPalette = ['#3e2723', '#2e1c14', '#4e342e', '#1e100a']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const rnd = (Math.sin(x * 6.1 + y * 14.3) * 43758.5453) % 1
                ctx.fillStyle = pineLogPalette[Math.floor(Math.abs(rnd) * pineLogPalette.length) % pineLogPalette.length]
                ctx.fillRect(x, y, 1, 1)
            }
        }
    })
}

// 16. Minecraft Pine Leaves (Aiguilles de pin sombre / bleuté)
export function getPixelPineLeavesTexture() {
    return createPixelTexture((ctx) => {
        const pineLeafPalette = ['#064e3b', '#065f46', '#047857', '#022c22', '#0f766e']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const rnd = (Math.sin(x * 8.5 + y * 27.2) * 43758.5453) % 1
                if (Math.abs(rnd) < 0.12 && (x + y) % 2 === 0) {
                    ctx.clearRect(x, y, 1, 1)
                } else {
                    ctx.fillStyle = pineLeafPalette[Math.floor(Math.abs(rnd) * pineLeafPalette.length) % pineLeafPalette.length]
                    ctx.fillRect(x, y, 1, 1)
                }
            }
        }
    })
}

// 17. Minecraft Pixel Water Texture
export function getPixelWaterTexture() {
    return createPixelTexture((ctx) => {
        const waterPalette = ['#0284c7', '#0369a1', '#075985', '#38bdf8', '#0c4a6e']
        for (let x = 0; x < 16; x++) {
            for (let y = 0; y < 16; y++) {
                const wave = Math.sin((x + y) * 0.8)
                const idx = Math.floor(Math.abs(wave) * waterPalette.length) % waterPalette.length
                ctx.fillStyle = waterPalette[idx]
                ctx.fillRect(x, y, 1, 1)
            }
        }
    })
}

