<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>AR Alien Incursion</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Rajdhani:wght@500;700&display=swap" rel="stylesheet">
    <!-- Three.js -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <style>
        :root {
            --primary: #00ffcc;
            --primary-glow: rgba(0, 255, 204, 0.4);
            --secondary: #ff0055;
            --secondary-glow: rgba(255, 0, 85, 0.4);
            --dark-bg: rgba(10, 15, 26, 0.85);
            --hud-blue: #00b3ff;
        }

        * {
            box-sizing: border-box;
            user-select: none;
            -webkit-user-select: none;
            margin: 0;
            padding: 0;
        }

        body, html {
            width: 100%;
            height: 100%;
            overflow: hidden;
            background-color: #000;
            font-family: 'Rajdhani', sans-serif;
            color: #fff;
            touch-action: none;
        }

        /* Video Background */
        #webcam {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
            z-index: 1;
            opacity: 0;
            transition: opacity 1s ease;
        }

        /* Three.js Canvas */
        #game-canvas {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 2;
            pointer-events: auto;
        }

        /* HUD overlay */
        .hud-layer {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 3;
            pointer-events: none;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 20px;
        }

        /* Screens (Start / Game Over) */
        .screen {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 10;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            background: radial-gradient(circle, rgba(10, 20, 35, 0.9) 0%, rgba(5, 10, 15, 0.98) 100%);
            padding: 30px;
            text-align: center;
            pointer-events: auto;
            transition: opacity 0.5s ease, visibility 0.5s;
        }

        .screen.hidden {
            opacity: 0;
            visibility: hidden;
            pointer-events: none;
        }

        h1 {
            font-family: 'Orbitron', sans-serif;
            font-weight: 900;
            font-size: 2.8rem;
            color: var(--primary);
            text-shadow: 0 0 20px var(--primary-glow);
            margin-bottom: 10px;
            letter-spacing: 2px;
            text-transform: uppercase;
        }

        p {
            font-size: 1.2rem;
            color: #a0aec0;
            max-width: 500px;
            margin-bottom: 30px;
            line-height: 1.5;
        }

        .btn {
            font-family: 'Orbitron', sans-serif;
            font-weight: 700;
            font-size: 1.1rem;
            color: #000;
            background-color: var(--primary);
            border: none;
            padding: 15px 40px;
            border-radius: 4px;
            cursor: pointer;
            box-shadow: 0 0 15px var(--primary-glow);
            transition: all 0.2s ease;
            text-transform: uppercase;
            letter-spacing: 1px;
            position: relative;
            overflow: hidden;
        }

        .btn:hover, .btn:active {
            transform: scale(1.05);
            box-shadow: 0 0 25px var(--primary);
        }

        /* HUD top elements */
        .hud-top {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            width: 100%;
            pointer-events: none;
        }

        .hud-card {
            background: var(--dark-bg);
            border-left: 3px solid var(--primary);
            padding: 10px 20px;
            border-radius: 0 8px 8px 0;
            box-shadow: 0 4px 20px rgba(0,0,0,0.5);
            pointer-events: auto;
        }

        .hud-card.right-card {
            border-left: none;
            border-right: 3px solid var(--hud-blue);
            border-radius: 8px 0 0 8px;
        }

        .label {
            font-size: 0.8rem;
            text-transform: uppercase;
            color: #718096;
            letter-spacing: 1px;
            font-weight: 700;
        }

        .value {
            font-family: 'Orbitron', sans-serif;
            font-size: 1.8rem;
            font-weight: 700;
            color: #fff;
            text-shadow: 0 0 10px rgba(255,255,255,0.2);
        }

        .value.score {
            color: var(--primary);
            text-shadow: 0 0 10px var(--primary-glow);
        }

        /* HUD bottom elements */
        .hud-bottom {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            width: 100%;
        }

        /* Health Bar */
        .health-container {
            width: 200px;
            background: var(--dark-bg);
            border: 1px solid rgba(255,255,255,0.1);
            padding: 12px;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.5);
            pointer-events: auto;
        }

        .health-label-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 6px;
        }

        .bar-bg {
            width: 100%;
            height: 10px;
            background: rgba(255,255,255,0.1);
            border-radius: 5px;
            overflow: hidden;
        }

        #health-bar {
            height: 100%;
            width: 100%;
            background: linear-gradient(90deg, var(--secondary), #ff5500, var(--primary));
            background-size: 200% 100%;
            transition: width 0.3s ease;
        }

        /* Weapon Overheat Bar */
        .weapon-container {
            width: 200px;
            background: var(--dark-bg);
            border: 1px solid rgba(255,255,255,0.1);
            padding: 12px;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.5);
            pointer-events: auto;
        }

        #weapon-bar {
            height: 100%;
            width: 0%;
            background: var(--hud-blue);
            box-shadow: 0 0 10px var(--hud-blue);
            transition: width 0.1s linear;
        }

        #weapon-bar.overheated {
            background: var(--secondary);
            box-shadow: 0 0 10px var(--secondary);
        }

        /* Crosshair in the absolute center */
        .crosshair-container {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 80px;
            height: 80px;
            pointer-events: none;
            z-index: 5;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .reticle {
            width: 40px;
            height: 40px;
            border: 2px solid var(--primary);
            border-radius: 50%;
            position: relative;
            transition: all 0.15s ease;
            box-shadow: 0 0 10px var(--primary-glow);
        }

        .reticle::before, .reticle::after {
            content: '';
            position: absolute;
            background: var(--primary);
            box-shadow: 0 0 8px var(--primary-glow);
        }

        /* Horizontal crosshair lines */
        .reticle::before {
            top: 50%;
            left: -10px;
            width: 60px;
            height: 2px;
            transform: translateY(-50%);
        }

        /* Vertical crosshair lines */
        .reticle::after {
            left: 50%;
            top: -10px;
            width: 2px;
            height: 60px;
            transform: translateX(-50%);
        }

        /* Outer brackets */
        .brackets {
            position: absolute;
            width: 100%;
            height: 100%;
            border-top: 2px solid rgba(0, 255, 204, 0.3);
            border-bottom: 2px solid rgba(0, 255, 204, 0.3);
            transition: all 0.3s ease;
        }

        .brackets::before, .brackets::after {
            content: '';
            position: absolute;
            width: 10px;
            height: 100%;
            border-left: 2px solid rgba(0, 255, 204, 0.3);
            border-right: 2px solid rgba(0, 255, 204, 0.3);
            top: 0;
        }
        .brackets::before { left: 0; }
        .brackets::after { right: 0; }

        /* Lock on state */
        .crosshair-container.locked .reticle {
            border-color: var(--secondary);
            transform: scale(0.85) rotate(45deg);
            box-shadow: 0 0 15px var(--secondary-glow);
        }
        .crosshair-container.locked .reticle::before,
        .crosshair-container.locked .reticle::after {
            background: var(--secondary);
            box-shadow: 0 0 10px var(--secondary-glow);
        }
        .crosshair-container.locked .brackets {
            border-color: var(--secondary);
            transform: scale(1.15) rotate(-45deg);
        }

        /* Flash Screen (Damage effect) */
        #flash-overlay {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 4;
            background: radial-gradient(circle, rgba(255,0,85,0) 40%, rgba(255,0,85,0.4) 100%);
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.1s ease;
        }

        /* Onscreen Instructions Info Bubble */
        .info-bubble {
            background: var(--dark-bg);
            border: 1px solid rgba(0, 255, 204, 0.2);
            padding: 15px 20px;
            border-radius: 30px;
            font-size: 1rem;
            color: #fff;
            text-align: center;
            box-shadow: 0 10px 30px rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            gap: 10px;
            pointer-events: auto;
            animation: float 3s ease-in-out infinite;
        }

        @keyframes float {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-8px); }
        }

        /* Dynamic wave alert splash */
        #wave-splash {
            position: absolute;
            top: 40%;
            left: 50%;
            transform: translate(-50%, -50%) scale(0.8);
            z-index: 5;
            font-family: 'Orbitron', sans-serif;
            font-size: 3rem;
            font-weight: 900;
            color: #fff;
            text-shadow: 0 0 20px var(--primary);
            text-transform: uppercase;
            letter-spacing: 5px;
            opacity: 0;
            pointer-events: none;
            transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }

        #wave-splash.active {
            opacity: 1;
            transform: translate(-50%, -50%) scale(1);
        }

        /* Settings Toggle */
        .controls-hint {
            font-size: 0.9rem;
            color: #718096;
            margin-top: 15px;
        }

        /* Fallback environment indicator */
        #fallback-indicator {
            position: absolute;
            bottom: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0,0,0,0.7);
            border: 1px solid rgba(255,255,255,0.2);
            padding: 5px 15px;
            border-radius: 15px;
            font-size: 0.8rem;
            color: #ccc;
            z-index: 3;
            display: none;
        }
    </style>
</head>
<body>

    <!-- Camera Feed -->
    <video id="webcam" autoplay playsinline muted></video>

    <!-- Game Canvas (Three.js Overlay) -->
    <canvas id="game-canvas"></canvas>

    <!-- Flash Overlay for Damage -->
    <div id="flash-overlay"></div>

    <!-- Fallback Indicator -->
    <div id="fallback-indicator">Camera Offline - Space Simulator Mode Active</div>

    <!-- Introduction & Start Screen -->
    <div id="start-screen" class="screen">
        <h1>Alien Incursion</h1>
        <p>Virtual entities are materializing in your space. Calibrate your sensors, aim using your device rotation (or swipe to steer your visor), and tap the screen to neutralize the threat before they breach your shield!</p>
        <button id="start-btn" class="btn">Initiate Mission</button>
        <div class="controls-hint">Requests Camera & Gyroscope permissions for optimal AR experience.</div>
    </div>

    <!-- Game Over Screen -->
    <div id="game-over-screen" class="screen hidden">
        <h1 style="color: var(--secondary); text-shadow: 0 0 20px var(--secondary-glow);">Visor Terminated</h1>
        <p id="game-over-summary">You held off the alien swarm, but your visor shield collapsed.</p>
        <div style="margin-bottom: 30px;">
            <div class="label">Final Score</div>
            <div id="final-score" class="value" style="font-size: 3rem; color: var(--primary);">0</div>
        </div>
        <button id="restart-btn" class="btn">Reboot Visor</button>
    </div>

    <!-- HUD Overlay -->
    <div class="hud-layer">
        <!-- Top Info -->
        <div class="hud-top">
            <div class="hud-card">
                <div class="label">Swarm Threat</div>
                <div id="hud-wave" class="value">Wave 1</div>
            </div>
            <div class="hud-card right-card">
                <div class="label">Energy Core</div>
                <div id="hud-score" class="value score">0000</div>
            </div>
        </div>

        <!-- Center Crosshair Area -->
        <div class="crosshair-container" id="crosshair">
            <div class="brackets"></div>
            <div class="reticle"></div>
        </div>

        <!-- Splash Wave Indicator -->
        <div id="wave-splash">Wave 1</div>

        <!-- Bottom Info -->
        <div class="hud-bottom">
            <!-- Health/Shield Left Side -->
            <div class="health-container">
                <div class="health-label-row">
                    <span class="label" style="color: #fff;">Shield Integrity</span>
                    <span id="health-pct" class="label" style="color: var(--primary);">100%</span>
                </div>
                <div class="bar-bg">
                    <div id="health-bar"></div>
                </div>
            </div>

            <!-- Swipe visual guide (Middle) -->
            <div class="info-bubble" id="info-tip">
                <span style="font-weight: 700; color: var(--primary);">SWIPE</span> or <span style="font-weight: 700; color: var(--primary);">TILT</span> to look around
            </div>

            <!-- Heat/Ammo Right Side -->
            <div class="weapon-container">
                <div class="health-label-row">
                    <span class="label" style="color: #fff;">Laser Core Temp</span>
                    <span id="heat-pct" class="label" style="color: var(--hud-blue);">0%</span>
                </div>
                <div class="bar-bg">
                    <div id="weapon-bar"></div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // --- Web Audio Synthesizer (No external sound files required!) ---
        class SoundFX {
            constructor() {
                this.ctx = null;
            }

            init() {
                if (!this.ctx) {
                    this.ctx = new (window.AudioContext || window.webkitAudioContext)();
                }
                if (this.ctx.state === 'suspended') {
                    this.ctx.resume();
                }
            }

            playShoot() {
                this.init();
                const now = this.ctx.currentTime;
                
                // Laser sound
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();
                
                osc.type = 'sawtooth';
                osc.frequency.setValueAtTime(880, now);
                osc.frequency.exponentialRampToValueAtTime(110, now + 0.15);
                
                gain.gain.setValueAtTime(0.2, now);
                gain.gain.exponentialRampToValueAtTime(0.01, now + 0.15);
                
                // Add a lowpass sweep
                const filter = this.ctx.createBiquadFilter();
                filter.type = 'lowpass';
                filter.frequency.setValueAtTime(2000, now);
                filter.frequency.exponentialRampToValueAtTime(400, now + 0.15);

                osc.connect(filter);
                filter.connect(gain);
                gain.connect(this.ctx.destination);
                
                osc.start();
                osc.stop(now + 0.15);
            }

            playExplosion() {
                this.init();
                const now = this.ctx.currentTime;
                const duration = 0.5;
                
                // Noise buffer generation for explosion hiss/rumble
                const bufferSize = this.ctx.sampleRate * duration;
                const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
                const data = buffer.getChannelData(0);
                for (let i = 0; i < bufferSize; i++) {
                    data[i] = Math.random() * 2 - 1;
                }
                
                const noise = this.ctx.createBufferSource();
                noise.buffer = buffer;
                
                const filter = this.ctx.createBiquadFilter();
                filter.type = 'lowpass';
                filter.frequency.setValueAtTime(600, now);
                filter.frequency.exponentialRampToValueAtTime(20, now + duration);
                
                const gain = this.ctx.createGain();
                gain.gain.setValueAtTime(0.4, now);
                gain.gain.exponentialRampToValueAtTime(0.01, now + duration);
                
                noise.connect(filter);
                filter.connect(gain);
                gain.connect(this.ctx.destination);
                
                noise.start();
                noise.stop(now + duration);
            }

            playHit() {
                this.init();
                const now = this.ctx.currentTime;
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();
                
                osc.type = 'sine';
                osc.frequency.setValueAtTime(300, now);
                osc.frequency.setValueAtTime(600, now + 0.05);
                
                gain.gain.setValueAtTime(0.15, now);
                gain.gain.exponentialRampToValueAtTime(0.01, now + 0.12);
                
                osc.connect(gain);
                gain.connect(this.ctx.destination);
                
                osc.start();
                osc.stop(now + 0.12);
            }

            playHurt() {
                this.init();
                const now = this.ctx.currentTime;
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();
                
                osc.type = 'sawtooth';
                osc.frequency.setValueAtTime(150, now);
                osc.frequency.linearRampToValueAtTime(50, now + 0.25);
                
                gain.gain.setValueAtTime(0.35, now);
                gain.gain.exponentialRampToValueAtTime(0.01, now + 0.25);
                
                osc.connect(gain);
                gain.connect(this.ctx.destination);
                
                osc.start();
                osc.stop(now + 0.25);
            }

            playWaveAlert() {
                this.init();
                const now = this.ctx.currentTime;
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();
                
                osc.type = 'square';
                osc.frequency.setValueAtTime(330, now);
                osc.frequency.setValueAtTime(440, now + 0.1);
                osc.frequency.setValueAtTime(550, now + 0.2);
                
                gain.gain.setValueAtTime(0.2, now);
                gain.gain.exponentialRampToValueAtTime(0.01, now + 0.4);
                
                osc.connect(gain);
                gain.connect(this.ctx.destination);
                
                osc.start();
                osc.stop(now + 0.4);
            }

            playOverheat() {
                this.init();
                const now = this.ctx.currentTime;
                const osc = this.ctx.createOscillator();
                const gain = this.ctx.createGain();
                
                osc.type = 'triangle';
                osc.frequency.setValueAtTime(180, now);
                osc.frequency.setValueAtTime(140, now + 0.1);
                osc.frequency.setValueAtTime(120, now + 0.2);
                
                gain.gain.setValueAtTime(0.3, now);
                gain.gain.exponentialRampToValueAtTime(0.01, now + 0.35);
                
                osc.connect(gain);
                gain.connect(this.ctx.destination);
                
                osc.start();
                osc.stop(now + 0.35);
            }
        }

        const sounds = new SoundFX();

        // --- Game Setup Constants & Variables ---
        const CONFIG = {
            maxHeat: 100,
            heatCost: 20,
            coolRate: 35, // per second
            damageFlashDuration: 200 // ms
        };

        let gameState = 'START'; // START, PLAYING, GAMEOVER
        let score = 0;
        let wave = 1;
        let health = 100;
        let heat = 0;
        let weaponOverheated = false;

        let scene, camera, renderer;
        let enemies = [];
        let particles = [];
        let lasers = [];
        let starsGroup;
        let gunGroup;

        // View Orientation Coordinates
        let lon = 0, lat = 0; // Target look variables
        let curLon = 0, curLat = 0; // Current look variables (smoothed)
        let isDragging = false;
        let prevPointerX = 0, prevPointerY = 0;

        // Gyroscope tracking states
        let gyroEnabled = false;
        let initialAlpha = null;
        let initialBeta = null;
        let gyroLonOffset = 0;
        let gyroLatOffset = 0;

        // Interactive UI Handles
        const domWebcam = document.getElementById('webcam');
        const domCanvas = document.getElementById('game-canvas');
        const domStartScreen = document.getElementById('start-screen');
        const domGameOverScreen = document.getElementById('game-over-screen');
        const domStartBtn = document.getElementById('start-btn');
        const domRestartBtn = document.getElementById('restart-btn');
        const domFlash = document.getElementById('flash-overlay');
        const domCrosshair = document.getElementById('crosshair');
        
        const hudScore = document.getElementById('hud-score');
        const hudWave = document.getElementById('hud-wave');
        const hudHealthBar = document.getElementById('health-bar');
        const hudHealthPct = document.getElementById('health-pct');
        const hudWeaponBar = document.getElementById('weapon-bar');
        const hudHeatPct = document.getElementById('heat-pct');
        const hudWaveSplash = document.getElementById('wave-splash');
        const hudFinalScore = document.getElementById('final-score');
        const fallbackIndicator = document.getElementById('fallback-indicator');

        // --- Init Web Camera Background ---
        async function startWebcam() {
            const constraints = {
                video: {
                    facingMode: 'environment',
                    width: { ideal: 1280 },
                    height: { ideal: 720 }
                },
                audio: false
            };

            try {
                const stream = await navigator.mediaDevices.getUserMedia(constraints);
                domWebcam.srcObject = stream;
                domWebcam.onloadedmetadata = () => {
                    domWebcam.play();
                    domWebcam.style.opacity = '1';
                };
            } catch (err) {
                console.warn("Rear camera not found or permission denied. Activating starfield simulator fallback.");
                fallbackIndicator.style.display = 'block';
                // Activate visible fallback space stars inside Three.js
                starsGroup.visible = true;
            }
        }

        // --- Device Orientation Permissions & Setup ---
        function requestOrientationPermission() {
            if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
                DeviceOrientationEvent.requestPermission()
                    .then(permissionState => {
                        if (permissionState === 'granted') {
                            window.addEventListener('deviceorientation', handleOrientation, true);
                        }
                    })
                    .catch(console.error);
            } else {
                window.addEventListener('deviceorientation', handleOrientation, true);
            }
        }

        function handleOrientation(event) {
            if (event.alpha === null || event.beta === null) return;
            gyroEnabled = true;

            // Save initial base calibration positions
            if (initialAlpha === null) {
                initialAlpha = event.alpha;
                initialBeta = event.beta;
            }

            // Calculate deltas and normalize wrapping
            let deltaAlpha = event.alpha - initialAlpha;
            if (deltaAlpha > 180) deltaAlpha -= 360;
            if (deltaAlpha < -180) deltaAlpha += 360;

            let deltaBeta = event.beta - initialBeta;

            // Map orientation change to navigation
            gyroLonOffset = deltaAlpha;
            gyroLatOffset = deltaBeta;
        }

        // --- Three.js Setup & Graphics Scene ---
        function initThree() {
            scene = new THREE.Scene();

            // Set up transparent camera so video behind remains visible
            camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
            renderer = new THREE.WebGLRenderer({ canvas: domCanvas, alpha: true, antialias: true });
            renderer.setSize(window.innerWidth, window.innerHeight);
            renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

            // Lighting setup
            const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
            scene.add(ambientLight);

            const dirLight = new THREE.DirectionalLight(0x00ffcc, 1);
            dirLight.position.set(5, 10, 7);
            scene.add(dirLight);

            // Digital grid stars overlay (gives depth cues, absolutely vital for AR and fallback)
            createStarfield();

            // Create Futuristic Visor Gun Mesh
            createWeapon();

            // Add camera to scene to anchor weapon
            scene.add(camera);

            // Handle browser resizing
            window.addEventListener('resize', onWindowResize);
        }

        function createStarfield() {
            starsGroup = new THREE.Group();
            const starGeometry = new THREE.BufferGeometry();
            const starCount = 400;
            const positions = new Float32Array(starCount * 3);
            const colors = new Float32Array(starCount * 3);

            for (let i = 0; i < starCount * 3; i += 3) {
                // Spherical random layout far away
                const r = 50 + Math.random() * 50;
                const theta = Math.random() * Math.PI * 2;
                const phi = Math.acos((Math.random() * 2) - 1);

                positions[i] = r * Math.sin(phi) * Math.cos(theta);
                positions[i + 1] = r * Math.sin(phi) * Math.sin(theta);
                positions[i + 2] = r * Math.cos(phi);

                // Cyan & white digital color stars
                colors[i] = 0.0;
                colors[i + 1] = Math.random() * 0.8 + 0.2;
                colors[i + 2] = 1.0;
            }

            starGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
            starGeometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));

            const starMaterial = new THREE.PointsMaterial({
                size: 0.6,
                vertexColors: true,
                transparent: true,
                opacity: 0.8,
                sizeAttenuation: true
            });

            const starPoints = new THREE.Points(starGeometry, starMaterial);
            starsGroup.add(starPoints);
            scene.add(starsGroup);
            
            // Hide standard stars by default, only enable if camera fails or background is space
            starsGroup.visible = false; 
        }

        function createWeapon() {
            gunGroup = new THREE.Group();
            
            // Main barrel cylinder
            const barrelGeom = new THREE.CylinderGeometry(0.04, 0.05, 0.6, 12);
            barrelGeom.rotateX(Math.PI / 2); // Align looking forward
            const gunMat = new THREE.MeshStandardMaterial({
                color: 0x1a2230,
                roughness: 0.3,
                metalness: 0.8,
                emissive: 0x002233
            });
            const barrel = new THREE.Mesh(barrelGeom, gunMat);
            gunGroup.add(barrel);

            // Glowing reactor coils
            const coilGeom = new THREE.TorusGeometry(0.07, 0.015, 8, 24);
            const coilMat = new THREE.MeshBasicMaterial({ color: 0x00ffcc });
            const coil1 = new THREE.Mesh(coilGeom, coilMat);
            coil1.position.set(0, 0, -0.1);
            const coil2 = coil1.clone();
            coil2.position.set(0, 0, 0.1);
            gunGroup.add(coil1);
            gunGroup.add(coil2);

            // Gun side panel details
            const plateGeom = new THREE.BoxGeometry(0.02, 0.15, 0.4);
            const plateLeft = new THREE.Mesh(plateGeom, gunMat);
            plateLeft.position.set(-0.06, 0, 0);
            const plateRight = plateLeft.clone();
            plateRight.position.set(0.06, 0, 0);
            gunGroup.add(plateLeft);
            gunGroup.add(plateRight);

            // Position gun in lower right screen space view of the camera
            gunGroup.position.set(0.3, -0.28, -0.7);
            camera.add(gunGroup);
        }

        function onWindowResize() {
            camera.aspect = window.innerWidth / window.innerHeight;
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        }

        // --- Enemy Construction class ---
        class Enemy {
            constructor(waveLevel) {
                this.type = this.selectType(waveLevel);
                this.mesh = this.buildMesh();
                this.health = this.type.baseHealth;
                this.maxHealth = this.type.baseHealth;
                this.speed = this.type.speed * (1 + (waveLevel * 0.1)); // Gets faster with waves
                this.zigzagSeed = Math.random() * 1000;

                // Spherical coordinate placement randomly around player
                const distance = 30 + Math.random() * 15; // Spawn at safe distance
                const theta = Math.random() * Math.PI * 2;
                const phi = Math.acos((Math.random() * 1.5) - 0.75); // Keep somewhat center latitudinally initially

                this.mesh.position.set(
                    distance * Math.sin(phi) * Math.cos(theta),
                    distance * Math.sin(phi) * Math.sin(theta),
                    distance * Math.cos(phi)
                );

                // Setup floating state indicators
                this.shieldRing = this.mesh.children[1];
                
                scene.add(this.mesh);
            }

            selectType(wave) {
                const roll = Math.random();
                if (wave >= 3 && roll > 0.75) {
                    // Beast Heavy Drone
                    return { color: 0xff3300, emissive: 0x551100, speed: 1.5, baseHealth: 30, scale: 1.5, points: 250 };
                } else if (wave >= 2 && roll > 0.45) {
                    // Hunter Speed Anomalies
                    return { color: 0xffcc00, emissive: 0x554400, speed: 4.2, baseHealth: 10, scale: 0.8, points: 150 };
                } else {
                    // Standard Anomalous Scout
                    return { color: 0x00ffcc, emissive: 0x004433, speed: 2.2, baseHealth: 10, scale: 1.0, points: 100 };
                }
            }

            buildMesh() {
                const group = new THREE.Group();

                // Geometry core shape
                const coreGeom = new THREE.IcosahedronGeometry(0.7 * this.type.scale, 1);
                const coreMat = new THREE.MeshStandardMaterial({
                    color: this.type.color,
                    emissive: this.type.emissive,
                    roughness: 0.2,
                    metalness: 0.9,
                    flatShading: true
                });
                const core = new THREE.Mesh(coreGeom, coreMat);
                group.add(core);

                // Edge wire-ring
                const ringGeom = new THREE.TorusGeometry(1.2 * this.type.scale, 0.05, 8, 32);
                const ringMat = new THREE.MeshBasicMaterial({
                    color: this.type.color,
                    wireframe: true,
                    transparent: true,
                    opacity: 0.6
                });
                const ring = new THREE.Mesh(ringGeom, ringMat);
                group.add(ring);

                return group;
            }

            update(delta, playerPos) {
                // Spin elements
                this.mesh.children[0].rotation.y += delta * 1.5;
                this.mesh.children[0].rotation.x += delta * 0.7;
                this.shieldRing.rotation.z -= delta * 2.0;

                // Move towards player coordinate (0,0,0)
                const dir = new THREE.Vector3().subVectors(playerPos, this.mesh.position).normalize();
                
                // Add zigzag steering mapping if speed hunter type
                if (this.type.color === 0xffcc00) {
                    const zigzag = Math.sin(Date.now() * 0.005 + this.zigzagSeed) * 0.45;
                    const perp = new THREE.Vector3(-dir.z, 0, dir.x).normalize();
                    dir.addScaledVector(perp, zigzag).normalize();
                }

                this.mesh.position.addScaledVector(dir, this.speed * delta);

                // Orient tracking ring facing player always
                this.shieldRing.lookAt(playerPos);

                // Returns distance left
                return this.mesh.position.distanceTo(playerPos);
            }

            takeDamage(amt) {
                this.health -= amt;
                
                // Temporary impact feedback glow visual shift
                this.mesh.children[0].material.emissive.setHex(0xffffff);
                setTimeout(() => {
                    if (this.mesh && this.mesh.children[0]) {
                        this.mesh.children[0].material.emissive.setHex(this.type.emissive);
                    }
                }, 80);

                return this.health <= 0;
            }

            destroy() {
                scene.remove(this.mesh);
                // Recursive cleanup
                this.mesh.traverse(child => {
                    if (child.geometry) child.geometry.dispose();
                    if (child.material) child.material.dispose();
                });
            }
        }

        // --- Particle Explosion Systems ---
        class ParticleExplosion {
            constructor(pos, color) {
                this.particles = [];
                const count = 25;
                const geom = new THREE.BoxGeometry(0.08, 0.08, 0.08);
                const mat = new THREE.MeshBasicMaterial({
                    color: color,
                    transparent: true,
                    opacity: 1.0
                });

                this.group = new THREE.Group();
                this.group.position.copy(pos);

                for (let i = 0; i < count; i++) {
                    const mesh = new THREE.Mesh(geom, mat);
                    const velocity = new THREE.Vector3(
                        (Math.random() - 0.5) * 8,
                        (Math.random() - 0.5) * 8,
                        (Math.random() - 0.5) * 8
                    );
                    this.group.add(mesh);
                    this.particles.push({ mesh, velocity });
                }

                scene.add(this.group);
                this.life = 1.0; // Life multiplier
            }

            update(delta) {
                this.life -= delta * 1.8;
                this.group.traverse(child => {
                    if (child instanceof THREE.Mesh) {
                        child.material.opacity = this.life;
                    }
                });

                this.particles.forEach(p => {
                    p.mesh.position.addScaledVector(p.velocity, delta);
                    p.velocity.multiplyScalar(0.94); // Air drag reduction
                    p.mesh.rotation.x += 0.1;
                    p.mesh.rotation.y += 0.1;
                });

                if (this.life <= 0) {
                    scene.remove(this.group);
                    this.group.traverse(child => {
                        if (child.geometry) child.geometry.dispose();
                        if (child.material) child.material.dispose();
                    });
                    return true; // flag to delete
                }
                return false;
            }
        }

        // --- Active Weapon Laser Cylinder Ray visualizer ---
        class LaserBeam {
            constructor(start, end, color) {
                const distance = start.distanceTo(end);
                const geom = new THREE.CylinderGeometry(0.015, 0.015, distance, 6);
                geom.rotateX(Math.PI / 2);
                geom.translate(0, 0, distance / 2); // Origin offset

                this.mat = new THREE.MeshBasicMaterial({
                    color: color,
                    transparent: true,
                    opacity: 0.9
                });

                this.mesh = new THREE.Mesh(geom, this.mat);
                this.mesh.position.copy(start);
                this.mesh.lookAt(end);
                scene.add(this.mesh);

                this.life = 0.12; // active display duration
            }

            update(delta) {
                this.life -= delta;
                this.mesh.scale.set(this.life / 0.12, this.life / 0.12, 1);
                
                if (this.life <= 0) {
                    scene.remove(this.mesh);
                    this.mesh.geometry.dispose();
                    this.mat.dispose();
                    return true;
                }
                return false;
            }
        }

        // --- Core Game Flow Management ---

        function triggerDamageFlash() {
            domFlash.style.opacity = '1';
            setTimeout(() => {
                domFlash.style.opacity = '0';
            }, CONFIG.damageFlashDuration);
        }

        function triggerWaveSplash(text) {
            hudWaveSplash.textContent = text;
            hudWaveSplash.classList.add('active');
            sounds.playWaveAlert();
            setTimeout(() => {
                hudWaveSplash.classList.remove('active');
            }, 1800);
        }

        function spawnSwarm() {
            const count = 3 + (wave * 2);
            triggerWaveSplash(`Threat Wave ${wave}`);
            hudWave.textContent = `Wave ${wave}`;

            for (let i = 0; i < count; i++) {
                // Add staggered spawning
                setTimeout(() => {
                    if (gameState === 'PLAYING') {
                        enemies.push(new Enemy(wave));
                    }
                }, i * 1500);
            }
        }

        function firePlayerLaser() {
            if (weaponOverheated || gameState !== 'PLAYING') return;

            // Heat addition logic
            heat += CONFIG.heatCost;
            if (heat >= CONFIG.maxHeat) {
                heat = CONFIG.maxHeat;
                weaponOverheated = true;
                hudWeaponBar.classList.add('overheated');
                sounds.playOverheat();
            }

            sounds.playShoot();

            // Gun kick back physics simulation recoil anim
            gunGroup.position.z = -0.55;
            gunGroup.rotation.x = -0.15;

            // Setup Raycasting source matching actual crosshair coordinate center
            const raycaster = new THREE.Raycaster();
            // Vector center represents exact reticle focal point
            raycaster.setFromCamera(new THREE.Vector2(0, 0), camera);

            // Fetch muzzle position translation vector
            const muzzleWorldPos = new THREE.Vector3(0.3, -0.25, -0.9).applyMatrix4(camera.matrixWorld);

            // Fetch center intersection raycast path projection vector
            const traceVec = new THREE.Vector3(0, 0, -50).applyMatrix4(camera.matrixWorld);

            // Determine if ray intersected any threat mesh
            const threatMeshes = enemies.map(e => e.mesh);
            const intersects = raycaster.intersectObjects(threatMeshes, true);

            let hitPoint = traceVec;
            let hitTarget = null;

            if (intersects.length > 0) {
                // First hit node
                const hitObj = intersects[0].object;
                hitPoint = intersects[0].point;

                // Trace upward parent container to identify owning Enemy object
                let parent = hitObj.parent;
                while (parent && !parent.userData.enemyOwner) {
                    // Check direct mapping helper
                    const match = enemies.find(e => e.mesh === parent);
                    if (match) {
                        hitTarget = match;
                        break;
                    }
                    parent = parent.parent;
                }
            }

            // Create laser visual trace
            const laserColor = weaponOverheated ? 0xff0055 : 0x00ffcc;
            lasers.push(new LaserBeam(muzzleWorldPos, hitPoint, laserColor));

            // Execute hit evaluation damage
            if (hitTarget) {
                sounds.playHit();
                const died = hitTarget.takeDamage(10);
                
                // Spawn impact sparks
                particles.push(new ParticleExplosion(hitPoint, hitTarget.type.color));

                if (died) {
                    sounds.playExplosion();
                    score += hitTarget.type.points;
                    hudScore.textContent = String(score).padStart(4, '0');
                    
                    // Cleanup enemy
                    enemies = enemies.filter(e => e !== hitTarget);
                    hitTarget.destroy();

                    // Check if current Wave cleared out completely
                    if (enemies.length === 0) {
                        wave++;
                        setTimeout(() => {
                            if (gameState === 'PLAYING') spawnSwarm();
                        }, 2500);
                    }
                }
            }
        }

        // --- Gamestate Controllers ---

        function startGame() {
            sounds.init();
            requestOrientationPermission();
            startWebcam();

            gameState = 'PLAYING';
            score = 0;
            wave = 1;
            health = 100;
            heat = 0;
            weaponOverheated = false;

            hudScore.textContent = '0000';
            hudWave.textContent = `Wave ${wave}`;
            hudHealthPct.textContent = '100%';
            hudHealthBar.style.width = '100%';
            hudWeaponBar.style.width = '0%';
            hudWeaponBar.classList.remove('overheated');

            // Clear legacy entities
            enemies.forEach(e => e.destroy());
            enemies = [];
            particles.forEach(p => scene.remove(p.group));
            particles = [];

            domStartScreen.classList.add('hidden');
            domGameOverScreen.classList.add('hidden');

            spawnSwarm();
        }

        function triggerGameOver() {
            gameState = 'GAMEOVER';
            sounds.playHurt();
            
            domGameOverScreen.classList.remove('hidden');
            hudFinalScore.textContent = score;

            if (score > 1500) {
                document.getElementById('game-over-summary').textContent = `Superb tactical defense! Your memory core recorded a glorious service.`;
            } else {
                document.getElementById('game-over-summary').textContent = `Visor shields compromised by standard anomalies. Level up your tracking to hold the perimeter.`;
            }
        }

        // --- Interaction Event Listeners ---

        // Screen Touch / Drag handling
        function setupPointerControls() {
            window.addEventListener('pointerdown', e => {
                // Ignore button overlay interactions
                if (e.target.closest('.btn') || e.target.closest('.hud-card') || e.target.closest('.health-container') || e.target.closest('.weapon-container')) {
                    return;
                }
                
                isDragging = true;
                prevPointerX = e.clientX;
                prevPointerY = e.clientY;

                // Fire weapon upon direct screen clicks/taps
                if (gameState === 'PLAYING') {
                    firePlayerLaser();
                }
            });

            window.addEventListener('pointermove', e => {
                if (!isDragging) return;
                
                const deltaX = e.clientX - prevPointerX;
                const deltaY = e.clientY - prevPointerY;
                
                // Sensitivity multiplier
                lon -= deltaX * 0.15;
                lat += deltaY * 0.15;
                lat = Math.max(-85, Math.min(85, lat)); // Cap vertical look

                prevPointerX = e.clientX;
                prevPointerY = e.clientY;
            });

            window.addEventListener('pointerup', () => {
                isDragging = false;
            });
        }

        // --- Main Frame Animation Loop ---
        let lastTime = performance.now();

        function animate(now) {
            requestAnimationFrame(animate);

            const delta = (now - lastTime) / 1000;
            lastTime = now;

            // 1. Visor Gun Recoil Animation Smooth Recovery
            if (gunGroup) {
                gunGroup.position.z += ( -0.7 - gunGroup.position.z ) * 0.15;
                gunGroup.rotation.x += ( 0 - gunGroup.rotation.x ) * 0.15;
            }

            // 2. Continuous Gun Core Temp cooling process
            if (gameState === 'PLAYING') {
                if (heat > 0) {
                    heat -= CONFIG.coolRate * delta;
                    if (heat < 0) heat = 0;
                } else if (heat === 0 && weaponOverheated) {
                    weaponOverheated = false;
                    hudWeaponBar.classList.remove('overheated');
                }

                // Smooth cool reset override if fully hot
                if (weaponOverheated && heat < 15) {
                    weaponOverheated = false;
                    hudWeaponBar.classList.remove('overheated');
                }

                hudWeaponBar.style.width = `${heat}%`;
                hudHeatPct.textContent = `${Math.ceil(heat)}%`;
            }

            // 3. Smoothed Camera Rotations (combining gyro offset + touch offsets)
            const targetLon = lon + gyroLonOffset;
            const targetLat = lat + gyroLatOffset;

            // Interpolated damping filter (Smoothly approaches targets)
            curLon += (targetLon - curLon) * 0.15;
            curLat += (targetLat - curLat) * 0.15;

            // Limit boundary looking
            const safeLat = Math.max(-85, Math.min(85, curLat));

            // Convert Euler degrees to target point on focus unit sphere
            const phi = THREE.MathUtils.degToRad(90 - safeLat);
            const theta = THREE.MathUtils.degToRad(curLon);

            const lookTarget = new THREE.Vector3();
            lookTarget.setFromSphericalCoords(1, phi, theta).add(camera.position);
            camera.lookAt(lookTarget);

            // Dynamic Reticle Raycasting lock-on tracking (visual HUD feed highlight)
            if (gameState === 'PLAYING') {
                const reticleRay = new THREE.Raycaster();
                reticleRay.setFromCamera(new THREE.Vector2(0, 0), camera);
                const intersects = reticleRay.intersectObjects(enemies.map(e => e.mesh), true);
                if (intersects.length > 0) {
                    domCrosshair.classList.add('locked');
                } else {
                    domCrosshair.classList.remove('locked');
                }
            }

            // 4. Game System Updates
            if (gameState === 'PLAYING') {
                const origin = new THREE.Vector3(0, 0, 0);

                // Update and handle threat movements
                for (let i = enemies.length - 1; i >= 0; i--) {
                    const dist = enemies[i].update(delta, origin);

                    // Threshold representing close proximity collision hit points
                    if (dist < 2.5) {
                        sounds.playHurt();
                        triggerDamageFlash();
                        
                        // Deduct shield integrity
                        health -= 15;
                        if (health < 0) health = 0;
                        
                        hudHealthBar.style.width = `${health}%`;
                        hudHealthPct.textContent = `${health}%`;

                        // Visual color alerts
                        if (health < 35) {
                            hudHealthPct.style.color = 'var(--secondary)';
                        }

                        // Cleanup invader
                        enemies[i].destroy();
                        enemies.splice(i, 1);

                        // Shield depleted check
                        if (health <= 0) {
                            triggerGameOver();
                            break;
                        }

                        // Spawner restart checker
                        if (enemies.length === 0 && gameState === 'PLAYING') {
                            wave++;
                            setTimeout(spawnSwarm, 2000);
                        }
                    }
                }
            }

            // Update particle explosions animation
            for (let i = particles.length - 1; i >= 0; i--) {
                if (particles[i].update(delta)) {
                    particles.splice(i, 1);
                }
            }

            // Update visible laser cylinders
            for (let i = lasers.length - 1; i >= 0; i--) {
                if (lasers[i].update(delta)) {
                    lasers.splice(i, 1);
                }
            }

            // Render updated scene frame
            renderer.render(scene, camera);
        }

        // --- App Entry Bindings ---
        window.addEventListener('DOMContentLoaded', () => {
            initThree();
            setupPointerControls();

            // Wire screen controllers
            domStartBtn.addEventListener('click', startGame);
            domRestartBtn.addEventListener('click', startGame);

            // Boot render processing tick
            requestAnimationFrame(animate);
        });
    </script>
</body>
</html>