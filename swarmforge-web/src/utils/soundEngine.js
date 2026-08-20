/**
 * SoundEngine.js - WebAudio Procedural Audio Synthesizer & Sound FX Engine
 * Manages atmospheric sounds, weather effects, biome ambiance, and insect activity:
 * 1. Thunder & Lightning (Éclairs & Tonnerre): Sharp electric crack + multi-stage rolling thunder rumble
 * 2. Rain (Pluie & Gouttes): Layered pink noise + individual raindrop impact synthesis
 * 3. Storm & Wind (Tempête & Vent): Dual LFO howling wind + whistling canopy gusts
 * 4. Tree Leaves (Feuilles dans les arbres): High-frequency bandpass foliage rustling
 * 5. Birds & Wildlife (Chants d'oiseaux & Forêt): Multi-tone bird chirps, trills, and calls
 * 6. Insects & Digging (Activité insectes & Creusement): Mandible clicks + soil grain excavation
 */

class ProceduralSoundEngine {
    constructor() {
        this.ctx = null;
        this.isInitialized = false;
        this.masterGain = null;
        this.muted = false;

        // Channel Gain Nodes
        this.gains = {
            ambiance: null,
            weather: null,
            insects: null,
            digging: null,
        };

        // Volumes (0.0 to 1.0)
        this.volumes = {
            master: 0.85,
            ambiance: 0.65,
            weather: 0.60,
            insects: 0.45,
            digging: 0.50,
        };

        // Active sound nodes & timers
        this.windNode = null;
        this.windFilter = null;
        this.windLfo = null;
        this.rainNode = null;
        this.rainFilter = null;
        this.birdTimer = null;
        this.leavesTimer = null;
        this.raindropTimer = null;
    }

    init() {
        if (this.isInitialized) return;
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;

        this.ctx = new AudioCtx();
        
        // Master Gain Node
        this.masterGain = this.ctx.createGain();
        this.masterGain.gain.setValueAtTime(this.volumes.master, this.ctx.currentTime);
        this.masterGain.connect(this.ctx.destination);

        // Create Channel Gains
        Object.keys(this.gains).forEach(key => {
            const gain = this.ctx.createGain();
            gain.gain.setValueAtTime(this.volumes[key], this.ctx.currentTime);
            gain.connect(this.masterGain);
            this.gains[key] = gain;
        });

        this.isInitialized = true;
        this.startWindAmbiance();
        this.scheduleNextBirdChirp();
        this.scheduleNextLeavesRustle();
    }

    async ensureContext() {
        if (!this.isInitialized) {
            this.init();
        }
        if (this.ctx && this.ctx.state === 'suspended') {
            await this.ctx.resume();
        }
    }

    // --- 1. PROCEDURAL WIND & TREE CANOPY AMBIANCE ---
    startWindAmbiance() {
        if (!this.ctx || !this.gains.ambiance) return;

        // 3-second buffer of smooth pink/brown noise for breeze
        const bufferSize = this.ctx.sampleRate * 3;
        const noiseBuffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
        const output = noiseBuffer.getChannelData(0);
        let b0 = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0, b5 = 0, b6 = 0;

        for (let i = 0; i < bufferSize; i++) {
            const white = Math.random() * 2 - 1;
            b0 = 0.99886 * b0 + white * 0.0555179;
            b1 = 0.99332 * b1 + white * 0.0750759;
            b2 = 0.96900 * b2 + white * 0.1538520;
            b3 = 0.86650 * b3 + white * 0.3104856;
            b4 = 0.55000 * b4 + white * 0.5329522;
            b5 = -0.7616 * b5 - white * 0.0168980;
            output[i] = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.05;
            b6 = white * 0.115926;
        }

        const whiteNoise = this.ctx.createBufferSource();
        whiteNoise.buffer = noiseBuffer;
        whiteNoise.loop = true;

        // Lowpass filter with dynamic frequency modulation
        this.windFilter = this.ctx.createBiquadFilter();
        this.windFilter.type = 'lowpass';
        this.windFilter.frequency.setValueAtTime(300, this.ctx.currentTime);

        // Dual LFO for natural wind howling
        this.windLfo = this.ctx.createOscillator();
        this.windLfo.frequency.value = 0.15; // Slow breeze pulse
        const lfoGain = this.ctx.createGain();
        lfoGain.gain.value = 220; // Modulate frequency between 100Hz and 500Hz

        this.windLfo.connect(lfoGain);
        lfoGain.connect(this.windFilter.frequency);
        this.windLfo.start();

        whiteNoise.connect(this.windFilter);
        this.windFilter.connect(this.gains.ambiance);
        whiteNoise.start();
        this.windNode = whiteNoise;
    }

    // --- 2. LEAVES IN TREES RUSTLING (Feuilles dans les arbres) ---
    triggerLeavesRustle() {
        if (!this.ctx || !this.gains.ambiance || this.muted) return;
        const now = this.ctx.currentTime;

        // High frequency bandpass noise burst representing foliage friction
        const duration = 0.4 + Math.random() * 0.6;
        const bufferSize = Math.floor(this.ctx.sampleRate * duration);
        const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
        const data = buffer.getChannelData(0);

        for (let i = 0; i < bufferSize; i++) {
            data[i] = (Math.random() * 2 - 1);
        }

        const noise = this.ctx.createBufferSource();
        noise.buffer = buffer;

        const filter = this.ctx.createBiquadFilter();
        filter.type = 'bandpass';
        filter.frequency.setValueAtTime(3200 + Math.random() * 1800, now);
        filter.Q.value = 2.5;

        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.001, now);
        gain.gain.linearRampToValueAtTime(0.08 + Math.random() * 0.05, now + duration * 0.3);
        gain.gain.exponentialRampToValueAtTime(0.001, now + duration);

        noise.connect(filter);
        filter.connect(gain);
        gain.connect(this.gains.ambiance);

        noise.start(now);
    }

    scheduleNextLeavesRustle() {
        if (this.leavesTimer) clearTimeout(this.leavesTimer);
        const delay = 3000 + Math.random() * 6000;
        this.leavesTimer = setTimeout(() => {
            this.triggerLeavesRustle();
            this.scheduleNextLeavesRustle();
        }, delay);
    }

    // --- 3. BIRD CHIRPS & FOREST SONG SYNTHESIS ---
    triggerBirdChirp() {
        if (!this.ctx || !this.gains.ambiance || this.muted) return;

        const now = this.ctx.currentTime;
        const callType = Math.random();

        if (callType < 0.6) {
            // Short 2-note bird chirp
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();

            const baseFreq = 2400 + Math.random() * 1000;
            osc.type = 'sine';
            osc.frequency.setValueAtTime(baseFreq, now);
            osc.frequency.exponentialRampToValueAtTime(baseFreq + 1200, now + 0.06);
            osc.frequency.exponentialRampToValueAtTime(baseFreq + 400, now + 0.14);

            gain.gain.setValueAtTime(0.001, now);
            gain.gain.linearRampToValueAtTime(0.18, now + 0.02);
            gain.gain.exponentialRampToValueAtTime(0.001, now + 0.16);

            osc.connect(gain);
            gain.connect(this.gains.ambiance);
            osc.start(now);
            osc.stop(now + 0.18);
        } else {
            // Rapid 3-trill bird warble
            for (let i = 0; i < 3; i++) {
                const noteTime = now + i * 0.07;
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();

                osc.type = 'sine';
                osc.frequency.setValueAtTime(3200 + i * 300, noteTime);
                osc.frequency.linearRampToValueAtTime(4200, noteTime + 0.04);

                gain.gain.setValueAtTime(0.001, noteTime);
                gain.gain.linearRampToValueAtTime(0.14, noteTime + 0.01);
                gain.gain.exponentialRampToValueAtTime(0.001, noteTime + 0.05);

                osc.connect(gain);
                gain.connect(this.gains.ambiance);
                osc.start(noteTime);
                osc.stop(noteTime + 0.06);
            }
        }
    }

    scheduleNextBirdChirp() {
        if (this.birdTimer) clearTimeout(this.birdTimer);
        const delay = 2000 + Math.random() * 5000;
        this.birdTimer = setTimeout(() => {
            this.triggerBirdChirp();
            this.scheduleNextBirdChirp();
        }, delay);
    }

    // --- 4. RAIN & INDIVIDUAL RAINDROPS SYNTHESIS (Bruits de pluie) ---
    updateRainSound(intensity) {
        this.ensureContext();
        if (!this.ctx || !this.gains.weather) return;

        if (intensity <= 0.05) {
            if (this.rainNode) {
                try { this.rainNode.stop(); } catch (e) {}
                this.rainNode = null;
            }
            if (this.raindropTimer) clearInterval(this.raindropTimer);
            return;
        }

        if (!this.rainNode) {
            // Continuous rain shower background noise
            const bufferSize = this.ctx.sampleRate * 2;
            const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
            const output = buffer.getChannelData(0);
            for (let i = 0; i < bufferSize; i++) {
                output[i] = (Math.random() * 2 - 1) * 0.2;
            }

            const noise = this.ctx.createBufferSource();
            noise.buffer = buffer;
            noise.loop = true;

            this.rainFilter = this.ctx.createBiquadFilter();
            this.rainFilter.type = 'highpass';
            this.rainFilter.frequency.value = 1400;

            noise.connect(this.rainFilter);
            this.rainFilter.connect(this.gains.weather);
            noise.start();
            this.rainNode = noise;

            // Start periodic individual raindrop impact sounds
            this.raindropTimer = setInterval(() => {
                this.triggerSingleRaindrop();
            }, 120);
        }

        // Adjust rain volume smoothly
        const rainVol = Math.min(0.85, Math.max(0.08, intensity / 25.0));
        this.gains.weather.gain.setTargetAtTime(rainVol, this.ctx.currentTime, 0.2);
    }

    triggerSingleRaindrop() {
        if (!this.ctx || !this.gains.weather || this.muted) return;
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        // Sine droplet pitch drop (1400Hz -> 300Hz)
        osc.type = 'sine';
        osc.frequency.setValueAtTime(1200 + Math.random() * 600, now);
        osc.frequency.exponentialRampToValueAtTime(300 + Math.random() * 200, now + 0.02);

        gain.gain.setValueAtTime(0.03, now);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.025);

        osc.connect(gain);
        gain.connect(this.gains.weather);

        osc.start(now);
        osc.stop(now + 0.03);
    }

    // --- 5. THUNDER & LIGHTNING STRIKES (Tonnerre & Éclairs) ---
    triggerThunder() {
        this.ensureContext();
        if (!this.ctx || !this.gains.weather) return;

        const now = this.ctx.currentTime;

        // 1. Initial Electric Crack (High Pass Snap)
        const snapOsc = this.ctx.createOscillator();
        const snapGain = this.ctx.createGain();
        snapOsc.type = 'sawtooth';
        snapOsc.frequency.setValueAtTime(800, now);
        snapOsc.frequency.exponentialRampToValueAtTime(120, now + 0.05);

        snapGain.gain.setValueAtTime(0.4, now);
        snapGain.gain.exponentialRampToValueAtTime(0.001, now + 0.08);

        snapOsc.connect(snapGain);
        snapGain.connect(this.gains.weather);
        snapOsc.start(now);
        snapOsc.stop(now + 0.1);

        // 2. Sub-bass Rumble (60Hz -> 25Hz)
        const sub = this.ctx.createOscillator();
        const subGain = this.ctx.createGain();
        sub.type = 'triangle';
        sub.frequency.setValueAtTime(70, now);
        sub.frequency.exponentialRampToValueAtTime(25, now + 1.8);

        subGain.gain.setValueAtTime(0.01, now);
        subGain.gain.linearRampToValueAtTime(0.5, now + 0.08);
        subGain.gain.exponentialRampToValueAtTime(0.001, now + 2.0);

        sub.connect(subGain);
        subGain.connect(this.gains.weather);
        sub.start(now);
        sub.stop(now + 2.1);

        // 3. Multi-stage Rolling Thunder Echo (Filtered Noise Crash)
        const bufferSize = Math.floor(this.ctx.sampleRate * 2.2);
        const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
        const data = buffer.getChannelData(0);
        for (let i = 0; i < bufferSize; i++) {
            data[i] = (Math.random() * 2 - 1);
        }

        const crash = this.ctx.createBufferSource();
        crash.buffer = buffer;

        const filter = this.ctx.createBiquadFilter();
        filter.type = 'lowpass';
        filter.frequency.setValueAtTime(280, now);
        filter.frequency.linearRampToValueAtTime(90, now + 1.8);

        const crashGain = this.ctx.createGain();
        crashGain.gain.setValueAtTime(0.01, now);
        crashGain.gain.linearRampToValueAtTime(0.45, now + 0.04);
        crashGain.gain.exponentialRampToValueAtTime(0.001, now + 2.0);

        crash.connect(filter);
        filter.connect(crashGain);
        crashGain.connect(this.gains.weather);

        crash.start(now);
    }

    // --- 6. STORM & TEMPÊTE (Howling wind storm gusts) ---
    triggerStormGust() {
        if (!this.ctx || !this.gains.weather || this.muted) return;
        const now = this.ctx.currentTime;

        const duration = 2.5;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        // Howling wind pitch sweep (180Hz -> 550Hz -> 200Hz)
        osc.type = 'sine';
        osc.frequency.setValueAtTime(200, now);
        osc.frequency.exponentialRampToValueAtTime(580, now + 1.0);
        osc.frequency.exponentialRampToValueAtTime(180, now + duration);

        gain.gain.setValueAtTime(0.001, now);
        gain.gain.linearRampToValueAtTime(0.2, now + 0.8);
        gain.gain.exponentialRampToValueAtTime(0.001, now + duration);

        osc.connect(gain);
        gain.connect(this.gains.weather);
        osc.start(now);
        osc.stop(now + duration + 0.1);
    }

    // --- 7. INSECTS & DIGGING SOUNDS (Pas d'insectes & excavation) ---
    triggerInsectStep() {
        if (!this.ctx || !this.gains.insects || this.muted) return;
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();

        osc.type = 'square';
        osc.frequency.setValueAtTime(3800 + Math.random() * 2200, now);

        gain.gain.setValueAtTime(0.05, now);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.015);

        osc.connect(gain);
        gain.connect(this.gains.insects);

        osc.start(now);
        osc.stop(now + 0.02);
    }

    triggerNestDiggingSound() {
        if (!this.ctx || !this.gains.digging || this.muted) return;
        const now = this.ctx.currentTime;

        const bufferSize = Math.floor(this.ctx.sampleRate * 0.12);
        const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
        const data = buffer.getChannelData(0);
        for (let i = 0; i < bufferSize; i++) {
            data[i] = (Math.random() * 2 - 1);
        }

        const scratch = this.ctx.createBufferSource();
        scratch.buffer = buffer;

        const filter = this.ctx.createBiquadFilter();
        filter.type = 'bandpass';
        filter.frequency.setValueAtTime(650 + Math.random() * 550, now);
        filter.Q.value = 3.5;

        const gain = this.ctx.createGain();
        gain.gain.setValueAtTime(0.15, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.1);

        scratch.connect(filter);
        filter.connect(gain);
        gain.connect(this.gains.digging);

        scratch.start(now);
    }

    // --- CONTROLS & VOLUME MANAGEMENT ---
    setMasterVolume(val) {
        this.volumes.master = val;
        if (this.masterGain && this.ctx) {
            this.masterGain.gain.setValueAtTime(this.muted ? 0 : val, this.ctx.currentTime);
        }
    }

    setChannelVolume(channel, val) {
        if (this.volumes[channel] !== undefined) {
            this.volumes[channel] = val;
            if (this.gains[channel] && this.ctx) {
                this.gains[channel].gain.setValueAtTime(val, this.ctx.currentTime);
            }
        }
    }

    toggleMute() {
        this.muted = !this.muted;
        if (this.masterGain && this.ctx) {
            this.masterGain.gain.setValueAtTime(this.muted ? 0 : this.volumes.master, this.ctx.currentTime);
        }
        return this.muted;
    }
}

export const soundEngine = new ProceduralSoundEngine();
