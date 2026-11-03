/**
 * Static tree placement and 3D mesh model configurations per biome climate type.
 *
 * @param {string} climateType Biome climate identifier (ARID, TROPICAL, BOREAL, TEMPERATE)
 * @returns {Array} Tree descriptors with positions, models, and scaling factors
 */
export function getBiomeTreePositions(climateType) {
    if (climateType === 'ARID' || climateType === 'SAVANNA' || climateType === 'DESERT') {
        return [
            { id: 1, pos: [15, 0, 30], scale: 1.0, targetHeight: 8.5, variant: 0, url: '/3d/cactus.obj', objectName: '0_lowpoly_Circle.034' },
            { id: 2, pos: [78, 0, 25], scale: 1.1, targetHeight: 13.5, variant: 1, url: '/3d/nature_pack/Dead Trees.glb', objectName: 'DeadTree_1' },
            { id: 3, pos: [82, 0, 75], scale: 1.0, targetHeight: 9.0, variant: 2, url: '/3d/cactus.obj', objectName: '1_lowpoly_Circle.035' },
            { id: 4, pos: [18, 0, 80], scale: 1.1, targetHeight: 14.5, variant: 0, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_1' },
            { id: 5, pos: [55, 0, 15], scale: 1.15, targetHeight: 13.5, variant: 1, url: '/3d/nature_pack/Dead Trees.glb', objectName: 'DeadTree_3' },
            { id: 6, pos: [88, 0, 88], scale: 1.0, targetHeight: 15.0, variant: 2, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_2' },
        ]
    }
    if (climateType === 'TROPICAL') {
        return [
            { id: 1, pos: [15, 0, 30], scale: 1.05, targetHeight: 15.0, variant: 0, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_1' },
            { id: 2, pos: [78, 0, 25], scale: 1.1, targetHeight: 13.0, variant: 1, url: '/3d/bamboo_set.obj', objectName: 'bamboo_tree_1_Cylinder' },
            { id: 3, pos: [82, 0, 75], scale: 1.0, targetHeight: 15.5, variant: 2, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_3' },
            { id: 4, pos: [18, 0, 80], scale: 1.1, targetHeight: 16.0, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_1' },
            { id: 5, pos: [55, 0, 15], scale: 1.15, targetHeight: 13.5, variant: 1, url: '/3d/bamboo_set.obj', objectName: 'bamboo_tree_2_Cylinder.001' },
            { id: 6, pos: [88, 0, 88], scale: 1.0, targetHeight: 14.5, variant: 2, url: '/3d/nature_pack/Palm Trees.glb', objectName: 'PalmTree_2' },
        ]
    }
    if (climateType === 'BOREAL' || climateType === 'TUNDRA' || climateType === 'ALPINE') {
        return [
            { id: 1, pos: [15, 0, 30], scale: 1.1, targetHeight: 16.0, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'PineTree_1' },
            { id: 2, pos: [78, 0, 25], scale: 1.05, targetHeight: 14.0, variant: 1, url: '/3d/nature_pack/Trees.glb', objectName: 'PineTree_2' },
            { id: 3, pos: [82, 0, 75], scale: 1.15, targetHeight: 17.0, variant: 2, url: '/3d/nature_pack/Trees.glb', objectName: 'PineTree_3' },
            { id: 4, pos: [18, 0, 80], scale: 1.0, targetHeight: 15.0, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'PineTree_4' },
            { id: 5, pos: [55, 0, 15], scale: 1.05, targetHeight: 13.5, variant: 1, url: '/3d/nature_pack/Dead Trees.glb', objectName: 'DeadTree_2' },
            { id: 6, pos: [88, 0, 88], scale: 1.2, targetHeight: 18.0, variant: 2, url: '/3d/nature_pack/Trees.glb', objectName: 'PineTree_5' },
        ]
    }
    // Default TEMPERATE
    return [
        { id: 1, pos: [15, 0, 30], scale: 1.1, targetHeight: 15.0, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_1' },
        { id: 2, pos: [78, 0, 25], scale: 1.0, targetHeight: 14.0, variant: 1, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_2' },
        { id: 3, pos: [82, 0, 75], scale: 1.15, targetHeight: 16.0, variant: 2, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_3' },
        { id: 4, pos: [18, 0, 80], scale: 1.05, targetHeight: 14.5, variant: 0, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_4' },
        { id: 5, pos: [55, 0, 15], scale: 1.2, targetHeight: 17.0, variant: 1, url: '/3d/nature_pack/Trees.glb', objectName: 'NormalTree_5' },
        { id: 6, pos: [88, 0, 88], scale: 0.95, targetHeight: 13.0, variant: 2, url: '/3d/nature_pack/Dead Trees.glb', objectName: 'DeadTree_1' },
    ]
}
