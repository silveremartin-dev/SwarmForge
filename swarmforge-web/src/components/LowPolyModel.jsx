import React, { useRef, useState, useEffect } from 'react'
import { useFrame } from '@react-three/fiber'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OBJLoader } from 'three/examples/jsm/loaders/OBJLoader.js'
import { MTLLoader } from 'three/examples/jsm/loaders/MTLLoader.js'
import { FBXLoader } from 'three/examples/jsm/loaders/FBXLoader.js'

// Global cache for parsed 3D scenes
const modelCache = new Map()

// Seasonal foliage tint helper
function applySeasonalTint(material, season) {
    if (!material) return material
    const mat = material.clone ? material.clone() : material

    const origColor = mat.color ? mat.color.getHex() : 0xffffff

    if (season === 'WINTER') {
        mat.color = new THREE.Color('#e2e8f0')
        mat.roughness = 0.95
    } else if (season === 'AUTUMN') {
        const hsl = { h: 0, s: 0, l: 0 }
        if (mat.color) {
            mat.color.getHSL(hsl)
            if (hsl.h > 0.18 && hsl.h < 0.48) {
                mat.color.setHSL(0.08 + (origColor % 5) * 0.02, 0.85, 0.45)
            }
        }
    } else if (season === 'SPRING') {
        const hsl = { h: 0, s: 0, l: 0 }
        if (mat.color) {
            mat.color.getHSL(hsl)
            if (hsl.h > 0.18 && hsl.h < 0.48) {
                mat.color.setHSL(0.33, 0.8, 0.5)
            }
        }
    }
    return mat
}

/**
 * Enhanced Component to load 3D models (.glb, .obj, .fbx), with:
 * - MTL material loading support for OBJ models
 * - Scale alignment for 100m terrarium terrain
 * - Wind sway dynamic micro-animations
 * - Seasonal foliage tinting (Autumn amber, Winter snow cover)
 * - Rich multi-part 3D volumetric tree fallback
 */
export default function LowPolyModel({
    url,
    objectName,
    objectIndex,
    targetHeight,
    textureUrl,
    position = [0, 0, 0],
    rotation = [0, 0, 0],
    scale = 1.0,
    sway = false,
    windSpeed = 2.4,
    season = 'SUMMER',
    fallbackGeometry = 'dodecahedron',
    fallbackColor = '#15803d'
}) {
    const [modelScene, setModelScene] = useState(null)
    const [failed, setFailed] = useState(false)
    const swayRef = useRef()

    useEffect(() => {
        if (!url) {
            setFailed(true)
            return
        }

        const cacheKey = `${url}_${objectName || 'all'}_${objectIndex ?? 'none'}_${targetHeight || 'auto'}_${season}_${textureUrl || 'none'}`
        if (modelCache.has(cacheKey)) {
            setModelScene(modelCache.get(cacheKey).clone(true))
            return
        }

        const lowerUrl = url.toLowerCase()
        const isObj = lowerUrl.endsWith('.obj')
        const isGltf = lowerUrl.endsWith('.glb') || lowerUrl.endsWith('.gltf')
        const isFbx = lowerUrl.endsWith('.fbx')

        const processScene = (sceneObj) => {
            let target = new THREE.Group()

            // Find all candidate top-level nodes
            const candidates = []
            if (sceneObj.children.length === 1 && sceneObj.children[0].name === 'RootNode') {
                sceneObj.children[0].children.forEach(c => candidates.push(c))
            } else {
                sceneObj.children.forEach(c => candidates.push(c))
            }

            if (objectName) {
                // Find named object or node matching objectName (case-insensitive)
                let found = null
                const lowerName = objectName.toLowerCase()
                sceneObj.traverse((child) => {
                    if (!found && child.name && child.name.toLowerCase().includes(lowerName)) {
                        found = child
                    }
                })
                if (found) {
                    target.add(found.clone(true))
                } else {
                    target = sceneObj.clone(true)
                }
            } else if (objectIndex !== undefined && candidates.length > 0) {
                const chosen = candidates[objectIndex % candidates.length]
                target.add(chosen.clone(true))
            } else {
                // Filter out any full-scene ground plane objects (e.g. SimpleGround_Plane.024)
                const toKeep = []
                sceneObj.traverse((child) => {
                    if (child.isMesh) {
                        const name = (child.name || '').toLowerCase()
                        if (!name.includes('simpleground') && !name.includes('plane.024') && !name.includes('ground_plane')) {
                            toKeep.push(child.clone())
                        }
                    }
                })
                if (toKeep.length > 0) {
                    toKeep.forEach(m => target.add(m))
                } else {
                    target = sceneObj.clone(true)
                }
            }

            // Calculate bounding box and center target at base (X=0, Z=0, MinY=0)
            target.updateMatrixWorld(true)
            const initialBbox = new THREE.Box3().setFromObject(target)
            if (!initialBbox.isEmpty()) {
                const size = new THREE.Vector3()
                initialBbox.getSize(size)
                const centerX = (initialBbox.min.x + initialBbox.max.x) / 2
                const centerZ = (initialBbox.min.z + initialBbox.max.z) / 2
                const minY = initialBbox.min.y

                // Height normalization if targetHeight is provided (e.g. 9.5m for trees, 1.2m for bushes)
                let normFactor = 1.0
                if (targetHeight && size.y > 0.001) {
                    normFactor = targetHeight / size.y
                    target.scale.set(normFactor, normFactor, normFactor)
                }

                target.position.set(-centerX * normFactor, -minY * normFactor, -centerZ * normFactor)
            }

            const wrapper = new THREE.Group()
            wrapper.add(target)

            if (textureUrl) {
                const texLoader = new THREE.TextureLoader()
                texLoader.load(textureUrl, (tex) => {
                    tex.wrapS = THREE.RepeatWrapping
                    tex.wrapT = THREE.RepeatWrapping
                    wrapper.traverse((child) => {
                        if (child.isMesh) {
                            child.frustumCulled = false
                            child.material = new THREE.MeshStandardMaterial({
                                map: tex,
                                roughness: 0.8,
                                metalness: 0.05,
                                depthWrite: true,
                                depthTest: true,
                                side: THREE.FrontSide,
                                transparent: false,
                                opacity: 1.0
                            })
                            if (season && season !== 'SUMMER') {
                                child.material = applySeasonalTint(child.material, season)
                            }
                            child.castShadow = true
                            child.receiveShadow = true
                        }
                    })
                })
            } else {
                wrapper.traverse((child) => {
                    if (child.isMesh) {
                        child.castShadow = true
                        child.receiveShadow = true
                        child.frustumCulled = false
                        if (child.material) {
                            child.material = child.material.clone ? child.material.clone() : child.material
                            child.material.depthWrite = true
                            child.material.depthTest = true
                            child.material.side = THREE.FrontSide
                            child.material.transparent = false
                            child.material.opacity = 1.0
                            child.material = applySeasonalTint(child.material, season)
                        }
                    }
                })
            }

            modelCache.set(cacheKey, wrapper)
            setModelScene(wrapper.clone(true))
        }

        if (isGltf) {
            const loader = new GLTFLoader()
            loader.load(
                url,
                (gltf) => processScene(gltf.scene),
                undefined,
                (err) => {
                    console.warn(`[LowPolyModel] GLTF load fallback for ${url}:`, err?.message || err)
                    setFailed(true)
                }
            )
        } else if (isObj) {
            const mtlUrl = url.replace(/\.obj$/i, '.mtl')
            const mtlLoader = new MTLLoader()
            mtlLoader.load(
                mtlUrl,
                (materials) => {
                    materials.preload()
                    const objLoader = new OBJLoader()
                    objLoader.setMaterials(materials)
                    objLoader.load(
                        url,
                        (obj) => processScene(obj),
                        undefined,
                        () => {
                            const rawObjLoader = new OBJLoader()
                            rawObjLoader.load(url, (obj) => processScene(obj), undefined, () => setFailed(true))
                        }
                    )
                },
                undefined,
                () => {
                    const rawObjLoader = new OBJLoader()
                    rawObjLoader.load(url, (obj) => processScene(obj), undefined, () => setFailed(true))
                }
            )
        } else if (isFbx) {
            const loader = new FBXLoader()
            loader.load(
                url,
                (fbx) => processScene(fbx),
                undefined,
                (err) => {
                    console.warn(`[LowPolyModel] FBX load fallback for ${url}:`, err?.message || err)
                    setFailed(true)
                }
            )
        } else {
            setFailed(true)
        }
    }, [url, objectName, objectIndex, targetHeight, textureUrl, season])

    // Wind Sway Micro-Animations (modulated by windSpeed m/s)
    useFrame((state) => {
        if (sway && swayRef.current) {
            const t = state.clock.getElapsedTime()
            const windFactor = Math.min(15, Math.max(0.5, windSpeed))
            const swayAmpX = 0.02 + windFactor * 0.003
            const swayAmpZ = 0.015 + windFactor * 0.0025
            const swayFreq = 1.0 + windFactor * 0.12

            swayRef.current.rotation.x = Math.sin(t * swayFreq) * swayAmpX
            swayRef.current.rotation.z = Math.cos(t * (swayFreq * 0.85)) * swayAmpZ
        }
    })

    if (failed || !modelScene) {
        // High quality multi-part 3D volumetric tree assembly fallback
        const scaleVal = typeof scale === 'number' ? scale : scale[0] || 1
        const seasonalFoliageColor = season === 'WINTER' ? '#e2e8f0' : season === 'AUTUMN' ? '#d97706' : fallbackColor

        return (
            <group position={position} rotation={rotation} scale={typeof scale === 'number' ? [scale, scale, scale] : scale} frustumCulled={false}>
                <group ref={swayRef} frustumCulled={false}>
                    {/* 3D Trunk */}
                    <mesh position={[0, scaleVal * 1.5, 0]} castShadow receiveShadow frustumCulled={false}>
                        <cylinderGeometry args={[scaleVal * 0.25, scaleVal * 0.45, scaleVal * 3.0, 8]} />
                        <meshStandardMaterial color="#4a2f1b" roughness={0.9} metalness={0.05} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    {/* 3D Main Canopy Blob */}
                    <mesh position={[0, scaleVal * 3.4, 0]} castShadow receiveShadow frustumCulled={false}>
                        <dodecahedronGeometry args={[scaleVal * 1.6, 1]} />
                        <meshStandardMaterial color={seasonalFoliageColor} roughness={0.8} metalness={0.05} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    {/* 3D Secondary Foliage Clusters */}
                    <mesh position={[scaleVal * 0.9, scaleVal * 2.8, scaleVal * 0.5]} castShadow receiveShadow frustumCulled={false}>
                        <dodecahedronGeometry args={[scaleVal * 1.1, 1]} />
                        <meshStandardMaterial color={seasonalFoliageColor} roughness={0.8} metalness={0.05} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                    <mesh position={[-scaleVal * 0.8, scaleVal * 3.1, -scaleVal * 0.6]} castShadow receiveShadow frustumCulled={false}>
                        <dodecahedronGeometry args={[scaleVal * 1.2, 1]} />
                        <meshStandardMaterial color={seasonalFoliageColor} roughness={0.8} metalness={0.05} side={THREE.FrontSide} depthWrite depthTest />
                    </mesh>
                </group>
            </group>
        )
    }

    return (
        <group position={position} rotation={rotation} scale={typeof scale === 'number' ? [scale, scale, scale] : scale} frustumCulled={false}>
            <group ref={swayRef} frustumCulled={false}>
                <primitive object={modelScene} />
            </group>
        </group>
    )
}

