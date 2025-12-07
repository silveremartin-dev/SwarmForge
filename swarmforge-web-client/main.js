import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';

class SwarmForgeViewer {
    constructor() {
        this.container = document.getElementById('canvas-container');
        this.scene = new THREE.Scene();
        this.camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
        this.renderer = new THREE.WebGLRenderer({ antialias: true });

        this.init();
        this.animate();
    }

    init() {
        // Setup renderer
        this.renderer.setSize(window.innerWidth, window.innerHeight);
        this.renderer.setClearColor(0x1a1a2e);
        this.container.appendChild(this.renderer.domElement);

        // Setup camera
        this.camera.position.set(50, 50, 50);
        this.camera.lookAt(0, 0, 0);

        // Controls
        this.controls = new OrbitControls(this.camera, this.renderer.domElement);
        this.controls.enableDamping = true;

        // Lights
        const ambientLight = new THREE.AmbientLight(0x404040);
        this.scene.add(ambientLight);

        const directionalLight = new THREE.DirectionalLight(0xffffff, 1);
        directionalLight.position.set(10, 20, 10);
        this.scene.add(directionalLight);

        // Dummy ground
        const gridHelper = new THREE.GridHelper(100, 100, 0x0f3460, 0x16213e);
        this.scene.add(gridHelper);

        // Dummy ant (cube)
        const geometry = new THREE.BoxGeometry(1, 1, 1);
        const material = new THREE.MeshLambertMaterial({ color: 0xe94560 });
        this.cube = new THREE.Mesh(geometry, material);
        this.scene.add(this.cube);

        // Pheromone Particles
        const particleCount = 1000;
        const particleGeo = new THREE.BufferGeometry();
        const positions = new Float32Array(particleCount * 3);
        const colors = new Float32Array(particleCount * 3);

        for (let i = 0; i < particleCount; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 100;
            positions[i * 3 + 1] = Math.random() * 20;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 100;

            colors[i * 3] = 0.5; // G
            colors[i * 3 + 1] = 1.0;
            colors[i * 3 + 2] = 0.5;
        }

        particleGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        particleGeo.setAttribute('color', new THREE.BufferAttribute(colors, 3));

        const particleMat = new THREE.PointsMaterial({
            size: 0.5,
            vertexColors: true,
            transparent: true,
            opacity: 0.6
        });

        this.particles = new THREE.Points(particleGeo, particleMat);
        this.scene.add(this.particles);

        // Handle resize
        window.addEventListener('resize', () => this.onWindowResize(), false);

        // UI Events
        document.getElementById('btn-connect').addEventListener('click', () => {
            alert('Connecting via Envoy Proxy (port 8080)...');
            document.getElementById('status').innerText = 'Connected (Mock)';
            document.getElementById('status').style.color = '#4ade80';
        });

        document.getElementById('btn-camera').addEventListener('click', () => {
            this.camera.position.set(50, 50, 50);
            this.controls.reset();
        });
    }

    onWindowResize() {
        this.camera.aspect = window.innerWidth / window.innerHeight;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(window.innerWidth, window.innerHeight);
    }

    animate() {
        requestAnimationFrame(() => this.animate());

        // Rotate dummy ant
        this.cube.rotation.x += 0.01;
        this.cube.rotation.y += 0.01;

        // Animate particles (mock pheromone diffusion)
        const positions = this.particles.geometry.attributes.position.array;
        for (let i = 0; i < positions.length; i += 3) {
            positions[i + 1] += (Math.random() - 0.5) * 0.1; // Jiggle Y
            if (positions[i + 1] < 0) positions[i + 1] = 0;
        }
        this.particles.geometry.attributes.position.needsUpdate = true;

        this.controls.update();
        this.renderer.render(this.scene, this.camera);
    }
}

// Start viewer
new SwarmForgeViewer();
