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
