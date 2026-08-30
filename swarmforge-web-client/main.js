import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';

class SwarmForgeEngine {
    constructor() {
        this.container = document.getElementById('canvas-container');
        this.scene = new THREE.Scene();
        this.camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
        this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false });
        
        // Simulation parameters
        this.running = true;
        this.speedMult = 1;
        this.tps = 60;
        this.fps = 60;
        this.frameCount = 0;
        this.lastTime = performance.now();
        this.viewMode = 'surface'; // 'surface', 'nest', 'split'

        // Agent configuration
        this.agentCount = 2500;
        this.agents = [];

        this.initRenderer();
        this.initLights();
        this.initTerrainAndNest();
        this.initInstancedAgents();
        this.initPheromones();
        this.initWeather();
        this.initControls();
        this.initUI();

        // Animation Loop
        this.animate();
    }

    initRenderer() {
        this.renderer.setSize(window.innerWidth, window.innerHeight);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        this.renderer.setClearColor(0x090d16);
        this.renderer.shadowMap.enabled = true;
        this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        this.container.appendChild(this.renderer.domElement);

        this.camera.position.set(40, 35, 45);
        this.camera.lookAt(0, 0, 0);

        window.addEventListener('resize', () => this.onWindowResize(), false);
    }

    initLights() {
        // Sun light
        this.dirLight = new THREE.DirectionalLight(0xfffaed, 1.4);
        this.dirLight.position.set(40, 60, 30);
        this.dirLight.castShadow = true;
        this.dirLight.shadow.mapSize.width = 2048;
        this.dirLight.shadow.mapSize.height = 2048;
        this.scene.add(this.dirLight);

        // Ambient sky light
        this.ambientLight = new THREE.AmbientLight(0x38bdf8, 0.4);
        this.scene.add(this.ambientLight);

        // Subterranean glowing lights (Queen chamber & Fungus gardens)
        const queenGlow = new THREE.PointLight(0xf59e0b, 2.5, 25);
        queenGlow.position.set(0, -12, 0);
        this.scene.add(queenGlow);

        const fungusGlow = new THREE.PointLight(0x10b981, 2.0, 20);
        fungusGlow.position.set(12, -8, -10);
        this.scene.add(fungusGlow);
    }

    initTerrainAndNest() {
        // 1. Surface Ground Terrain
        const groundGeo = new THREE.PlaneGeometry(120, 120, 64, 64);
        const pos = groundGeo.attributes.position;
        for (let i = 0; i < pos.count; i++) {
            const x = pos.getX(i);
            const y = pos.getY(i);
            const z = Math.sin(x * 0.1) * Math.cos(y * 0.1) * 1.5;
            pos.setZ(i, z);
        }
        groundGeo.computeVertexNormals();

        const groundMat = new THREE.MeshStandardMaterial({
            color: 0x1c2b36,
            roughness: 0.8,
            metalness: 0.1,
            flatShading: true
        });

        this.terrainMesh = new THREE.Mesh(groundGeo, groundMat);
        this.terrainMesh.rotation.x = -Math.PI / 2;
        this.terrainMesh.receiveShadow = true;
        this.scene.add(this.terrainMesh);

        // Grid overlay
        const grid = new THREE.GridHelper(120, 40, 0x10b981, 0x1e293b);
        grid.position.y = 0.05;
        this.scene.add(grid);

        // 2. Subterranean Nest Strata & Chamber Meshes
        this.nestGroup = new THREE.Group();

        // Strata Glass Box
        const strataGeo = new THREE.BoxGeometry(80, 25, 80);
        const strataMat = new THREE.MeshStandardMaterial({
            color: 0x0f172a,
            roughness: 0.9,
            transparent: true,
            opacity: 0.45
        });
        const strataBox = new THREE.Mesh(strataGeo, strataMat);
        strataBox.position.set(0, -12.5, 0);
        this.nestGroup.add(strataBox);

        // Central Crater Mound Entrance
        const moundGeo = new THREE.ConeGeometry(8, 4, 16);
        const moundMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.9 });
        const mound = new THREE.Mesh(moundGeo, moundMat);
        mound.position.set(0, 2, 0);
        this.nestGroup.add(mound);

        // Queen Chamber (Golden Vault)
        const queenChamberGeo = new THREE.SphereGeometry(4.5, 16, 16);
        const queenChamberMat = new THREE.MeshStandardMaterial({ color: 0xb45309, roughness: 0.5, metalness: 0.3 });
        const queenChamber = new THREE.Mesh(queenChamberGeo, queenChamberMat);
        queenChamber.position.set(0, -12, 0);
        this.nestGroup.add(queenChamber);

        // Fungus Garden Chamber
        const fungusChamberGeo = new THREE.SphereGeometry(3.5, 16, 16);
        const fungusChamberMat = new THREE.MeshStandardMaterial({ color: 0x047857, roughness: 0.6 });
        const fungusChamber = new THREE.Mesh(fungusChamberGeo, fungusChamberMat);
        fungusChamber.position.set(12, -8, -10);
        this.nestGroup.add(fungusChamber);

        // Brood Nursery Chamber
        const broodChamberGeo = new THREE.SphereGeometry(3.5, 16, 16);
        const broodChamberMat = new THREE.MeshStandardMaterial({ color: 0x6d28d9, roughness: 0.6 });
        const broodChamber = new THREE.Mesh(broodChamberGeo, broodChamberMat);
        broodChamber.position.set(-10, -10, 8);
        this.nestGroup.add(broodChamber);

        // Food Storage Granary
        const foodChamberGeo = new THREE.SphereGeometry(3.8, 16, 16);
        const foodChamberMat = new THREE.MeshStandardMaterial({ color: 0x0284c7, roughness: 0.6 });
        const foodChamber = new THREE.Mesh(foodChamberGeo, foodChamberMat);
        foodChamber.position.set(14, -6, 12);
        this.nestGroup.add(foodChamber);

        // Tunnels connecting entrance to chambers
        this.createTunnel(new THREE.Vector3(0, 0, 0), new THREE.Vector3(0, -12, 0));
        this.createTunnel(new THREE.Vector3(0, -5, 0), new THREE.Vector3(12, -8, -10));
        this.createTunnel(new THREE.Vector3(0, -6, 0), new THREE.Vector3(-10, -10, 8));
        this.createTunnel(new THREE.Vector3(0, -4, 0), new THREE.Vector3(14, -6, 12));

        this.scene.add(this.nestGroup);

        // Food Cluster Items on surface
        this.foodGroup = new THREE.Group();
        this.spawnFoodCluster(22, 0.5, 18);
        this.spawnFoodCluster(-25, 0.5, -20);
        this.scene.add(this.foodGroup);
    }

    createTunnel(start, end) {
        const path = new THREE.LineCurve3(start, end);
        const tubeGeo = new THREE.TubeGeometry(path, 12, 1.2, 8, false);
        const tubeMat = new THREE.MeshStandardMaterial({ color: 0x475569, roughness: 0.9, side: THREE.DoubleSide });
        const tube = new THREE.Mesh(tubeGeo, tubeMat);
        this.nestGroup.add(tube);
    }

    spawnFoodCluster(cx, cy, cz) {
        const sugarMat = new THREE.MeshStandardMaterial({ color: 0x38bdf8, roughness: 0.3, metalness: 0.4 });
        for (let i = 0; i < 15; i++) {
            const geo = new THREE.DodecahedronGeometry(0.5 + Math.random() * 0.4);
            const m = new THREE.Mesh(geo, sugarMat);
            m.position.set(cx + (Math.random() - 0.5) * 4, cy, cz + (Math.random() - 0.5) * 4);
            this.foodGroup.add(m);
        }
    }

    initInstancedAgents() {
        // High-performance InstancedMesh for agent swarm
        const antGeo = new THREE.BoxGeometry(0.4, 0.25, 0.7);
        const antMat = new THREE.MeshStandardMaterial({ roughness: 0.4, metalness: 0.2 });

        this.instancedAnts = new THREE.InstancedMesh(antGeo, antMat, this.agentCount);
        this.dummy = new THREE.Object3D();

        const colorWorker = new THREE.Color(0x06b6d4);
        const colorSoldier = new THREE.Color(0xf43f5e);
        const colorQueen = new THREE.Color(0xf59e0b);

        for (let i = 0; i < this.agentCount; i++) {
            let caste = 'worker';
            let color = colorWorker;
            let scale = 1.0;

            if (i < 10) {
                caste = 'queen';
                color = colorQueen;
                scale = 2.4;
            } else if (i < 450) {
                caste = 'soldier';
                color = colorSoldier;
                scale = 1.5;
            }

            // Initialize agent state
            const inNest = Math.random() < 0.4;
            const x = inNest ? (Math.random() - 0.5) * 30 : (Math.random() - 0.5) * 90;
            const z = inNest ? (Math.random() - 0.5) * 30 : (Math.random() - 0.5) * 90;
            const y = inNest ? -Math.random() * 18 : 0.2;

            this.agents.push({
                caste,
                x, y, z,
                heading: Math.random() * Math.PI * 2,
                speed: 0.1 + Math.random() * 0.15,
                inNest
            });

            this.dummy.position.set(x, y, z);
            this.dummy.scale.set(scale, scale, scale);
            this.dummy.updateMatrix();
            this.instancedAnts.setMatrixAt(i, this.dummy.matrix);
            this.instancedAnts.setColorAt(i, color);
        }

        this.instancedAnts.instanceMatrix.needsUpdate = true;
        this.instancedAnts.instanceColor.needsUpdate = true;
        this.scene.add(this.instancedAnts);
    }

    initPheromones() {
        const count = 1200;
        const geo = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        const colors = new Float32Array(count * 3);

        for (let i = 0; i < count; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 80;
            positions[i * 3 + 1] = 0.2 + Math.random() * 2.5;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 80;

            colors[i * 3] = 0.06;
            colors[i * 3 + 1] = 0.72;
            colors[i * 3 + 2] = 0.5;
        }

        geo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        geo.setAttribute('color', new THREE.BufferAttribute(colors, 3));

        const mat = new THREE.PointsMaterial({
            size: 0.6,
            vertexColors: true,
            transparent: true,
            opacity: 0.5
        });

        this.pheromoneParticles = new THREE.Points(geo, mat);
        this.scene.add(this.pheromoneParticles);
    }

    initWeather() {
        const count = 1500;
        const geo = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);

        for (let i = 0; i < count; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 120;
            positions[i * 3 + 1] = Math.random() * 50;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 120;
        }

        geo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        const mat = new THREE.PointsMaterial({
            size: 0.3,
            color: 0x38bdf8,
            transparent: true,
            opacity: 0.0 // Clear by default
        });

        this.rainParticles = new THREE.Points(geo, mat);
        this.isRaining = false;
        this.scene.add(this.rainParticles);
    }

    initControls() {
        this.controls = new OrbitControls(this.camera, this.renderer.domElement);
        this.controls.enableDamping = true;
        this.controls.dampingFactor = 0.05;
        this.controls.maxPolarAngle = Math.PI / 2 + 0.1;
    }

    initUI() {
        // Play/Pause
        document.getElementById('btn-toggle-sim').addEventListener('click', (e) => {
            this.running = !this.running;
            e.currentTarget.classList.toggle('active', !this.running);
            e.currentTarget.querySelector('.label').innerText = this.running ? 'Pause' : 'Resume';
            e.currentTarget.querySelector('.icon').innerText = this.running ? '⏸' : '▶';
        });

        // Speed buttons
        const speeds = [
            { id: 'btn-speed-1', val: 1 },
            { id: 'btn-speed-2', val: 2 },
            { id: 'btn-speed-5', val: 5 }
        ];
        speeds.forEach(s => {
            document.getElementById(s.id).addEventListener('click', () => {
                speeds.forEach(x => document.getElementById(x.id).classList.remove('active'));
                document.getElementById(s.id).classList.add('active');
                this.speedMult = s.val;
            });
        });

        // View Modes
        document.getElementById('btn-view-surface').addEventListener('click', () => this.setViewMode('surface'));
        document.getElementById('btn-view-nest').addEventListener('click', () => this.setViewMode('nest'));
        document.getElementById('btn-view-split').addEventListener('click', () => this.setViewMode('split'));

        // Actions
        document.getElementById('btn-spawn-food').addEventListener('click', () => {
            const rx = (Math.random() - 0.5) * 60;
            const rz = (Math.random() - 0.5) * 60;
            this.spawnFoodCluster(rx, 0.5, rz);
            this.addLog(`Spawned sugar food cluster at (${rx.toFixed(1)}, ${rz.toFixed(1)})`, 'success');
        });

        document.getElementById('btn-trigger-alarm').addEventListener('click', () => {
            this.triggerAlarmPheromone();
            this.addLog(`⚠️ High-intensity alarm pheromone released!`, 'warning');
        });

        document.getElementById('btn-nuptial-flight').addEventListener('click', () => {
            this.addLog(`👑 Nuptial flight triggered: Alate queens emerging.`, 'info');
        });

        document.getElementById('btn-toggle-rain').addEventListener('click', (e) => {
            this.isRaining = !this.isRaining;
            this.rainParticles.material.opacity = this.isRaining ? 0.6 : 0.0;
            e.currentTarget.innerText = this.isRaining ? '🌧 Weather: Rain' : '🌧 Weather: Clear';
            this.addLog(`Weather state changed: ${this.isRaining ? 'Rainfall active' : 'Clear sky'}`, 'info');
        });

        // Camera Presets
        document.getElementById('btn-cam-iso').addEventListener('click', () => {
            this.camera.position.set(40, 35, 45);
            this.controls.target.set(0, 0, 0);
        });
        document.getElementById('btn-cam-top').addEventListener('click', () => {
            this.camera.position.set(0, 70, 0.1);
            this.controls.target.set(0, 0, 0);
        });
        document.getElementById('btn-cam-close').addEventListener('click', () => {
            this.camera.position.set(8, 5, 8);
            this.controls.target.set(0, 0, 0);
        });

        // Species Selector
        document.getElementById('species-select').addEventListener('change', (e) => {
            this.addLog(`Species switched to: ${e.target.options[e.target.selectedIndex].text}`, 'info');
        });
    }

    setViewMode(mode) {
        this.viewMode = mode;
        ['btn-view-surface', 'btn-view-nest', 'btn-view-split'].forEach(id => document.getElementById(id).classList.remove('active'));

        if (mode === 'surface') {
            document.getElementById('btn-view-surface').classList.add('active');
            this.terrainMesh.visible = true;
            this.camera.position.set(40, 35, 45);
        } else if (mode === 'nest') {
            document.getElementById('btn-view-nest').classList.add('active');
            this.terrainMesh.visible = false;
            this.camera.position.set(0, -8, 30);
            this.controls.target.set(0, -10, 0);
        } else if (mode === 'split') {
            document.getElementById('btn-view-split').classList.add('active');
            this.terrainMesh.visible = true;
            this.camera.position.set(25, 10, 50);
        }
    }

    triggerAlarmPheromone() {
        const colors = this.pheromoneParticles.geometry.attributes.color.array;
        for (let i = 0; i < colors.length; i += 3) {
            colors[i] = 0.96; // Red/Amber alarm
            colors[i + 1] = 0.38;
            colors[i + 2] = 0.1;
        }
        this.pheromoneParticles.geometry.attributes.color.needsUpdate = true;
    }

    addLog(msg, type = 'info') {
        const stream = document.getElementById('log-stream');
        const now = new Date().toLocaleTimeString();
        const div = document.createElement('div');
        div.className = `log-entry ${type}`;
        div.innerHTML = `<span class="time">[${now}]</span> ${msg}`;
        stream.appendChild(div);
        stream.scrollTop = stream.scrollHeight;
    }

    onWindowResize() {
        this.camera.aspect = window.innerWidth / window.innerHeight;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(window.innerWidth, window.innerHeight);
    }

    animate() {
        requestAnimationFrame(() => this.animate());

        const now = performance.now();
        this.frameCount++;
        if (now - this.lastTime >= 1000) {
            this.fps = this.frameCount;
            this.frameCount = 0;
            this.lastTime = now;
            document.getElementById('fps-val').innerText = this.fps;
        }

        if (this.running) {
            this.updateAgents();
            this.updatePheromones();
            this.updateWeather();
        }

        this.controls.update();
        this.renderer.render(this.scene, this.camera);
    }

    updateAgents() {
        const steps = this.speedMult;
        for (let step = 0; step < steps; step++) {
            for (let i = 0; i < this.agentCount; i++) {
                const a = this.agents[i];
                a.heading += (Math.random() - 0.5) * 0.2;
                a.x += Math.cos(a.heading) * a.speed;
                a.z += Math.sin(a.heading) * a.speed;

                // Boundary bounce
                if (Math.abs(a.x) > 55) { a.x = Math.sign(a.x) * 55; a.heading += Math.PI; }
                if (Math.abs(a.z) > 55) { a.z = Math.sign(a.z) * 55; a.heading += Math.PI; }

                this.dummy.position.set(a.x, a.y, a.z);
                this.dummy.rotation.y = -a.heading + Math.PI / 2;
                const scale = a.caste === 'queen' ? 2.4 : (a.caste === 'soldier' ? 1.5 : 1.0);
                this.dummy.scale.set(scale, scale, scale);
                this.dummy.updateMatrix();

                this.instancedAnts.setMatrixAt(i, this.dummy.matrix);
            }
        }
        this.instancedAnts.instanceMatrix.needsUpdate = true;
    }

    updatePheromones() {
        const positions = this.pheromoneParticles.geometry.attributes.position.array;
        for (let i = 0; i < positions.length; i += 3) {
            positions[i + 1] += (Math.random() - 0.5) * 0.05;
            if (positions[i + 1] < 0.2) positions[i + 1] = 0.2;
        }
        this.pheromoneParticles.geometry.attributes.position.needsUpdate = true;
    }

    updateWeather() {
        if (!this.isRaining) return;
        const positions = this.rainParticles.geometry.attributes.position.array;
        for (let i = 0; i < positions.length; i += 3) {
            positions[i + 1] -= 0.8 * this.speedMult;
            if (positions[i + 1] < 0) positions[i + 1] = 50;
        }
        this.rainParticles.geometry.attributes.position.needsUpdate = true;
    }
}

// Start Web Engine
new SwarmForgeEngine();
