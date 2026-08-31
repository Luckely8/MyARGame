<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, viewport-fit=cover">
    <title>Neon Tempest AR - Cyberpunk Drone Hunter</title>
    <!-- Sci-fi Font -->
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Share+Tech+Mono&display=swap" rel="stylesheet">
    <!-- Three.js CDN -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <style>
        :root {
            --neon-pink: #ff0055;
            --neon-cyan: #00ffff;
            --neon-green: #05ff50;
            --neon-yellow: #ffea00;
            --bg-dark: rgba(10, 10, 18, 0.85);
            --border-glow: 0 0 10px rgba(0, 255, 255, 0.5), inset 0 0 10px rgba(0, 255, 255, 0.2);
        }

        * {
            box-sizing: border-box;
            user-select: none;
            -webkit-user-select: none;
        }

        html, body {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            overflow: hidden;
            background-color: #000;
            font-family: 'Share Tech Mono', monospace;
            color: #fff;
        }

        /* Camera Background Stream */
        #webcam {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
            z-index: 1;
        }

        /* Three.js Canvas Overlaid */
        #gameCanvas {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 2;
            pointer-events: auto;
        }

        /* HUD & User Interface Containers */
        .overlay {
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
            padding-top: max(20px, env(safe-area-inset-top));
            padding-bottom: max(20px, env(safe-area-inset-bottom));
        }

        /* Screen Scanlines / Cyberpunk Grid Filter */
        .scanlines {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(
                rgba(18, 16, 16, 0) 50%, 
                rgba(0, 0, 0, 0.25) 50%
            );
            background-size: 100% 4px;
            z-index: 4;
            pointer-events: none;
        }

        /* Sci-Fi Vignette Border */
        .vignette {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            box-shadow: inset 0 0 80px rgba(0, 0, 0, 0.8);
            z-index: 5;
            pointer-events: none;
            transition: box-shadow 0.1s ease;
        }

        .vignette.hit {
            box-shadow: inset 0 0 100px rgba(255, 0, 85, 0.8);
        }

        /* HUD Panels */
        .hud-top {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            width: 100%;
        }

        .hud-panel {
            background: var(--bg-dark);
            border: 1px solid var(--neon-cyan);
            box-shadow: var(--border-glow);
            padding: 10px 15px;
            border-radius: 4px;
            pointer-events: auto;
        }

        .score-box {
            font-family: 'Orbitron', sans-serif;
            font-weight: 900;
            font-size: 1.2rem;
            color: var(--neon-cyan);
            text-shadow: 0 0 8px rgba(0, 255, 255, 0.6);
            letter-spacing: 1px;
        }

        .score-val {
            color: #fff;
            margin-left: 5px;
        }

        .ammo-box {
            font-family: 'Orbitron', sans-serif;
            text-align: right;
            font-size: 1rem;
            color: var(--neon-pink);
            text-shadow: 0 0 8px rgba(255, 0, 85, 0.6);
        }

        /* Health & Shield Bar Bottom Left */
        .hud-bottom {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            width: 100%;
        }

        .status-container {
            width: 220px;
        }

        .status-label {
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 2px;
            color: var(--neon-cyan);
            margin-bottom: 5px;
            display: flex;
            justify-content: space-between;
        }

        .bar-outer {
            width: 100%;
            height: 12px;
            background: rgba(0, 255, 255, 0.1);
            border: 1px solid var(--neon-cyan);
            padding: 1px;
            border-radius: 2px;
            overflow: hidden;
            box-shadow: 0 0 5px rgba(0, 255, 255, 0.2);
        }

        .bar-inner {
            height: 100%;
            background: linear-gradient(90deg, var(--neon-cyan), #00bcff);
            width: 100%;
            transition: width 0.3s ease;
            box-shadow: 0 0 8px var(--neon-cyan);
        }

        /* Dynamic Minimap / Cyber Radar */
        .radar-container {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            background: var(--bg-dark);
            border: 2px solid var(--neon-cyan);
            box-shadow: var(--border-glow);
            position: relative;
            overflow: hidden;
            pointer-events: auto;
        }

        #radarCanvas {
            width: 100%;
            height: 100%;
        }

        /* Center Reticle Crosshair */
        .crosshair-container {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            z-index: 10;
            pointer-events: none;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .reticle {
            width: 40px;
            height: 40px;
            border: 2px solid rgba(0, 255, 255, 0.4);
            border-radius: 50%;
            position: relative;
            transition: all 0.1s ease;
        }

        .reticle::before, .reticle::after {
            content: '';
            position: absolute;
            background-color: var(--neon-cyan);
        }

        /* Crosshair tick marks */
        .reticle::before {
            top: 50%;
            left: -8px;
            width: 6px;
            height: 2px;
            transform: translateY(-50%);
        }
        .reticle::after {
            top: 50%;
            right: -8px;
            width: 6px;
            height: 2px;
            transform: translateY(-50%);
        }

        .reticle-center {
            position: absolute;
            width: 4px;
            height: 4px;
            background-color: var(--neon-pink);
            border-radius: 50%;
            box-shadow: 0 0 5px var(--neon-pink);
        }

        .reticle.active {
            width: 55px;
            height: 55px;
            border-color: var(--neon-pink);
            box-shadow: 0 0 15px rgba(255, 0, 85, 0.8);
        }

        /* Screen States (Overlay Menus) */
        .screen {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(8, 8, 15, 0.95);
            z-index: 100;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 30px;
            text-align: center;
            pointer-events: auto;
        }

        .screen.hidden {
            display: none !important;
        }

        h1 {
            font-family: 'Orbitron', sans-serif;
            font-size: 2.5rem;
            font-weight: 900;
            text-transform: uppercase;
            margin: 0 0 10px 0;
            color: #fff;
            text-shadow: 0 0 15px var(--neon-cyan), 0 0 30px rgba(0, 255, 255, 0.5);
            letter-spacing: 2px;
            line-height: 1.1;
        }

        h1 span {
            color: var(--neon-pink);
            text-shadow: 0 0 15px var(--neon-pink);
        }

        .subtitle {
            font-size: 1.1rem;
            color: var(--neon-cyan);
            margin-bottom: 30px;
            letter-spacing: 1px;
            text-shadow: 0 0 5px rgba(0, 255, 255, 0.3);
        }

        .card {
            background: rgba(255, 255, 255, 0.03);
            border: 1px solid rgba(0, 255, 255, 0.2);
            border-radius: 8px;
            padding: 20px;
            max-width: 450px;
            margin-bottom: 30px;
            box-shadow: inset 0 0 15px rgba(0, 255, 255, 0.05);
        }

        .card p {
            font-size: 0.95rem;
            line-height: 1.6;
            color: #ccc;
            margin: 0 0 15px 0;
        }

        .card p:last-child {
            margin-bottom: 0;
        }

        /* Cyber Button */
        .btn-cyber {
            background: transparent;
            border: 2px solid var(--neon-green);
            color: var(--neon-green);
            font-family: 'Orbitron', sans-serif;
            font-weight: 700;
            font-size: 1.1rem;
            text-transform: uppercase;
            padding: 15px 35px;
            cursor: pointer;
            border-radius: 4px;
            transition: all 0.2s ease;
            box-shadow: 0 0 10px rgba(5, 255, 80, 0.2), inset 0 0 10px rgba(5, 255, 80, 0.1);
            letter-spacing: 2px;
            outline: none;
        }

        .btn-cyber:hover, .btn-cyber:active {
            background: var(--neon-green);
            color: #000;
            box-shadow: 0 0 20px var(--neon-green);
            text-shadow: none;
        }

        .btn-cyber-pink {
            border-color: var(--neon-pink);
            color: var(--neon-pink);
            box-shadow: 0 0 10px rgba(255, 0, 85, 0.2), inset 0 0 10px rgba(255, 0, 85, 0.1);
        }

        .btn-cyber-pink:hover, .btn-cyber-pink:active {
            background: var(--neon-pink);
            color: #fff;
            box-shadow: 0 0 20px var(--neon-pink);
        }

        /* Warning text */
        .warning-text {
            color: var(--neon-yellow) !important;
            font-size: 0.85rem !important;
            margin-top: 10px !important;
        }

        /* Floating Level Alert Splash */
        #levelAlert {
            position: absolute;
            top: 25%;
            left: 50%;
            transform: translate(-50%, -50%) scale(0.8);
            font-family: 'Orbitron', sans-serif;
            font-size: 2rem;
            font-weight: 900;
            color: var(--neon-yellow);
            text-shadow: 0 0 20px var(--neon-yellow);
            z-index: 50;
            opacity: 0;
            pointer-events: none;
            transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            letter-spacing: 3px;
            text-align: center;
        }

        #levelAlert.show {
            opacity: 1;
            transform: translate(-50%, -50%) scale(1);
        }

        /* Dynamic Hit indicator overlay */
        #damageIndicator {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 40;
            pointer-events: none;
            background: radial-gradient(circle, rgba(255,0,85,0) 40%, rgba(255,0,85,0.4) 100%);
            opacity: 0;
            transition: opacity 0.15s ease-out;
        }

        /* Support / Gyro status indicator */
        .gyro-status {
            font-size: 0.85rem;
            color: #777;
            margin-top: 15px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #444;
            display: inline-block;
        }

        .status-dot.active {
            background: var(--neon-green);
            box-shadow: 0 0 8px var(--neon-green);
        }
    </style>
</head>
<body>

    <!-- Camera Background Stream -->
    <video id="webcam" autoplay playsinline muted></video>

    <!-- Interactive Game Canvas -->
    <canvas id="gameCanvas"></canvas>

    <!-- Visual Overlays -->
    <div class="scanlines"></div>
    <div id="vignette" class="vignette"></div>
    <div id="damageIndicator"></div>

    <!-- Center Reticle -->
    <div class="crosshair-container">
        <div id="crosshair" class="reticle">
            <div class="reticle-center"></div>
        </div>
    </div>

    <!-- HUD Overlay UI -->
    <div id="hud" class="overlay hidden">
        <div class="hud-top">
            <div class="hud-panel score-box">
                SYS_SCORE:<span id="scoreVal" class="score-val">000000</span>
            </div>
            <div class="hud-panel ammo-box">
                LVL: <span id="levelVal" style="color: #fff;">1</span>
            </div>
        </div>

        <div id="levelAlert">AIRSPACE SECURED<br><span style="font-size: 1.2rem; color: #fff;">LEVEL UP</span></div>

        <div class="hud-bottom">
            <!-- Shield Indicator -->
            <div class="hud-panel status-container">
                <div class="status-label">
                    <span>SHIELD INTEGRITY</span>
                    <span id="shieldVal">100%</span>
                </div>
                <div class="bar-outer">
                    <div id="shieldBar" class="bar-inner"></div>
                </div>
            </div>

            <!-- Cyber Radar Map -->
            <div class="radar-container">
                <canvas id="radarCanvas" width="100" height="100"></canvas>
            </div>
        </div>
    </div>

    <!-- Screen 1: Welcome & Request Permissions -->
    <div id="startScreen" class="screen">
        <h1>NEON<span>TEMPEST</span> AR</h1>
        <div class="subtitle">TACTICAL DRONE INTERCEPTOR</div>
        
        <div class="card">
            <p>Threat vectors detected in your immediate atmospheric sector. You must engage and eliminate virtual hostiles immediately.</p>
            <p><strong>AR STEERING GUIDE:</strong> Turn your device to target, or swipe/drag across the screen if you prefer fixed position play.</p>
            <p><strong>COMBAT PROTOCOL:</strong> Direct tap anywhere to initiate primary laser fire at hostile drone coordinate points.</p>
            <p class="warning-text">Camera and Gyroscope permissions are required for the full tactical AR tracking layout.</p>
        </div>

        <button id="btnStart" class="btn-cyber">BOOT SYSTEM</button>

        <div class="gyro-status">
            <span id="gyroDot" class="status-dot"></span>
            <span id="gyroText">GYRO SENSORS: UNINITIALIZED</span>
        </div>
    </div>

    <!-- Screen 2: Game Over / Redeploy -->
    <div id="gameOverScreen" class="screen hidden">
        <h1 style="color: var(--neon-pink); text-shadow: 0 0 15px var(--neon-pink)">NEURAL LINK LOST</h1>
        <div class="subtitle">MISSION COMPROMISED</div>

        <div class="card" style="border-color: rgba(255, 0, 85, 0.3);">
            <p style="font-size: 1.2rem; color: #fff;">TOTAL THREATS PURGED: <span id="finalKills" style="color: var(--neon-green); font-weight: bold;">0</span></p>
            <p style="font-size: 1.4rem; color: #fff;">FINAL SYSTEM SCORE: <span id="finalScore" style="color: var(--neon-cyan); font-weight: bold;">00000</span></p>
            <p style="font-size: 0.9rem; color: #aaa; margin-top: 15px;">TACTICAL HIGHSCORE: <span id="highScore" style="color: var(--neon-yellow);">00000</span></p>
        </div>

        <button id="btnRestart" class="btn-cyber btn-cyber-pink">REDEPLOY SYSTEM</button>
    </div>

    <script>
        // --- CONSTANTS & CONFIGS ---
        const SPHERE_RADIUS = 20; // Distance drones spawn at
        const MIN_HIT_DISTANCE = 3.0; // Distance drone explodes & damages player
        const SYSTEM_MAX_SHIELD = 100;

        // --- GAME VARIABLES ---
        let scene, camera, renderer, raycaster;
        let webcamStream = null;
        let enemies = [];
        let particles = [];
        let laserBeams = [];
        let score = 0;
        let shield = SYSTEM_MAX_SHIELD;
        let level = 1;
        let killsCount = 0;
        let totalKills = 0;
        let highScore = localStorage.getItem('ar_neon_high') || 0;
        let gameState = 'START'; // 'START', 'PLAYING', 'GAMEOVER'
        let nextSpawnTime = 0;
        let spawnInterval = 3000; // start with 3 seconds
        let lastFrameTime = performance.now();

        // --- CONTROLS OFFSET ---
        let lon = 0, lat = 0;
        let isUserInteracting = false;
        let onPointerDownMouseX = 0, onPointerDownMouseY = 0;
        let onPointerDownLon = 0, onPointerDownLat = 0;
        let tapStartTime = 0;
        let tapStartX = 0, tapStartY = 0;

        // --- AR / SENSORS ---
        let useGyro = false;
        let lastA = null, lastB = null;

        // --- AUDIO SYNTHESIZER ---
        let audioCtx = null;

        // --- INITIALIZE SYSTEM ---
        document.getElementById('btnStart').addEventListener('click', bootGame);
        document.getElementById('btnRestart').addEventListener('click', resetGame);

        async function bootGame() {
            // Trigger Audio Context (Browser requirement)
            audioCtx = new (window.AudioContext || window.webkitAudioContext)();
            
            // 1. Initialize Camera Feed
            await initCamera();

            // 2. Request Gyro Permissions (Required for iOS 13+)
            await initSensors();

            // 3. Setup Three.js Scene
            initThree();

            // 4. Start Game
            startGame();
        }

        async function initCamera() {
            try {
                webcamStream = await navigator.mediaDevices.getUserMedia({
                    video: { facingMode: { ideal: 'environment' } },
                    audio: false
                });
                const video = document.getElementById('webcam');
                video.srcObject = webcamStream;
                // Avoid virtual black screens, mirror stream isn't needed for rear cams
                video.onloadedmetadata = () => {
                    video.play();
                };
            } catch (err) {
                console.warn("Rear Camera inaccessible. Activating Virtual VR Grid Space Background instead.", err);
                // Grid background fallback
                document.body.style.background = "radial-gradient(circle, #151525 0%, #050510 100%)";
            }
        }

        async function initSensors() {
            const gyroDot = document.getElementById('gyroDot');
            const gyroText = document.getElementById('gyroText');

            if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
                try {
                    const permissionState = await DeviceOrientationEvent.requestPermission();
                    if (permissionState === 'granted') {
                        setupOrientationListener();
                    } else {
                        gyroText.textContent = "GYRO SENSORS: BLOCKED (SWIPE TO LOOK)";
                    }
                } catch (err) {
                    console.error("Device orientation permission error: ", err);
                    gyroText.textContent = "GYRO SENSORS: DENIED (SWIPE TO LOOK)";
                }
            } else {
                // Non-iOS or older browser
                if ('ondeviceorientation' in window) {
                    setupOrientationListener();
                } else {
                    gyroText.textContent = "GYRO SENSORS: UNSUPPORTED (SWIPE TO LOOK)";
                }
            }
        }

        function setupOrientationListener() {
            window.addEventListener('deviceorientation', handleOrientation, true);
            useGyro = true;
            document.getElementById('gyroDot').classList.add('active');
            document.getElementById('gyroText').textContent = "GYRO SENSORS: TRACKING ACTIVE";
            document.getElementById('gyroText').style.color = 'var(--neon-green)';
        }

        function handleOrientation(e) {
            if (!useGyro || e.alpha === null || e.beta === null) return;

            // Initialize baseline on first input
            if (lastA === null) {
                lastA = e.alpha;
                lastB = e.beta;
                return;
            }

            let deltaA = e.alpha - lastA;
            let deltaB = e.beta - lastB;

            // Smooth out coordinate wrap boundaries
            if (deltaA > 180) deltaA -= 360;
            if (deltaA < -180) deltaA += 360;

            // Update Look Rotations (Horizontal & Vertical Spherical Coords)
            lon -= deltaA;
            lat += deltaB;
            lat = Math.max(-80, Math.min(80, lat)); // Cap up/down gaze limits

            lastA = e.alpha;
            lastB = e.beta;
        }

        // --- AUDIO SYNTHS ---
        function playSynthSound(type) {
            if (!audioCtx) return;
            if (audioCtx.state === 'suspended') {
                audioCtx.resume();
            }

            const now = audioCtx.currentTime;

            if (type === 'laser') {
                const osc = audioCtx.createOscillator();
                const gain = audioCtx.createGain();
                osc.connect(gain);
                gain.connect(audioCtx.destination);
                
                osc.type = 'triangle';
                osc.frequency.setValueAtTime(1200, now);
                osc.frequency.exponentialRampToValueAtTime(100, now + 0.15);

                gain.gain.setValueAtTime(0.3, now);
                gain.gain.linearRampToValueAtTime(0.01, now + 0.15);

                osc.start(now);
                osc.stop(now + 0.15);
            } 
            else if (type === 'explosion') {
                const osc = audioCtx.createOscillator();
                const gain = audioCtx.createGain();
                osc.connect(gain);
                gain.connect(audioCtx.destination);

                osc.type = 'sawtooth';
                osc.frequency.setValueAtTime(250, now);
                osc.frequency.exponentialRampToValueAtTime(30, now + 0.5);

                gain.gain.setValueAtTime(0.4, now);
                gain.gain.linearRampToValueAtTime(0.01, now + 0.5);

                osc.start(now);
                osc.stop(now + 0.5);
            } 
            else if (type === 'damage') {
                const osc1 = audioCtx.createOscillator();
                const osc2 = audioCtx.createOscillator();
                const gain = audioCtx.createGain();

                osc1.connect(gain);
                osc2.connect(gain);
                gain.connect(audioCtx.destination);

                osc1.type = 'sawtooth';
                osc2.type = 'sawtooth';

                osc1.frequency.setValueAtTime(90, now);
                osc2.frequency.setValueAtTime(94, now); // Detune buzz

                gain.gain.setValueAtTime(0.4, now);
                gain.gain.linearRampToValueAtTime(0.01, now + 0.35);

                osc1.start(now);
                osc2.start(now);
                osc1.stop(now + 0.35);
                osc2.stop(now + 0.35);
            }
            else if (type === 'levelup') {
                const osc = audioCtx.createOscillator();
                const gain = audioCtx.createGain();
                osc.connect(gain);
                gain.connect(audioCtx.destination);

                osc.type = 'sine';
                osc.frequency.setValueAtTime(261.6, now); // C4
                osc.frequency.setValueAtTime(329.6, now + 0.1); // E4
                osc.frequency.setValueAtTime(392.0, now + 0.2); // G4
                osc.frequency.setValueAtTime(523.3, now + 0.3); // C5

                gain.gain.setValueAtTime(0.25, now);
                gain.gain.setValueAtTime(0.25, now + 0.3);
                gain.gain.linearRampToValueAtTime(0.01, now + 0.5);

                osc.start(now);
                osc.stop(now + 0.5);
            }
        }

        // --- THREE.JS GRAPHICS SETUP ---
        function initThree() {
            const canvas = document.getElementById('gameCanvas');
            
            // WebGL Renderer Setup (Alpha: True overlay on camera)
            renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true, antialias: true });
            renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
            renderer.setSize(window.innerWidth, window.innerHeight);

            scene = new THREE.Scene();

            camera = new THREE.PerspectiveCamera(70, window.innerWidth / window.innerHeight, 0.1, 100);
            camera.position.set(0, 0, 0);

            raycaster = new THREE.Raycaster();

            // Ambient tactical light
            const ambientLight = new THREE.AmbientLight(0xffffff, 0.8);
            scene.add(ambientLight);

            // Add virtual cyberspace structural floor in case camera lacks details
            const gridHelper = new THREE.GridHelper(100, 50, 0x00ffff, 0x112244);
            gridHelper.position.y = -8;
            scene.add(gridHelper);

            // Interaction Listeners
            window.addEventListener('resize', onWindowResize);
            
            // Pointer Controls (Supports dual drag rotation and tap coordinates)
            canvas.addEventListener('pointerdown', onPointerDown);
            canvas.addEventListener('pointermove', onPointerMove);
            canvas.addEventListener('pointerup', onPointerUp);
        }

        // --- GAMEPLAY STATES ---
        function startGame() {
            document.getElementById('startScreen').classList.add('hidden');
            document.getElementById('hud').classList.remove('hidden');
            gameState = 'PLAYING';
            score = 0;
            shield = SYSTEM_MAX_SHIELD;
            level = 1;
            killsCount = 0;
            totalKills = 0;
            spawnInterval = 3000;
            updateHUD();

            // Clear outstanding elements
            enemies.forEach(e => scene.remove(e.group));
            enemies = [];
            particles.forEach(p => scene.remove(p.mesh));
            particles = [];
            laserBeams.forEach(l => scene.remove(l.mesh));
            laserBeams = [];

            nextSpawnTime = performance.now() + 1000;
            requestAnimationFrame(gameLoop);
        }

        function resetGame() {
            document.getElementById('gameOverScreen').classList.add('hidden');
            startGame();
        }

        function gameOver() {
            gameState = 'GAMEOVER';
            document.getElementById('hud').classList.add('hidden');
            document.getElementById('gameOverScreen').classList.remove('hidden');

            document.getElementById('finalKills').textContent = totalKills;
            document.getElementById('finalScore').textContent = score.toString().padStart(6, '0');

            if (score > highScore) {
                highScore = score;
                localStorage.setItem('ar_neon_high', highScore);
            }
            document.getElementById('highScore').textContent = highScore.toString().padStart(6, '0');
        }

        // --- MAIN ENGINE TICK ---
        function gameLoop(now) {
            if (gameState !== 'PLAYING') return;

            const dt = (now - lastFrameTime) / 1000;
            lastFrameTime = now;

            // 1. Smoothly update camera orientation if using swipes
            if (!useGyro) {
                const phi = THREE.MathUtils.degToRad(90 - lat);
                const theta = THREE.MathUtils.degToRad(lon);

                const target = new THREE.Vector3();
                target.x = Math.sin(phi) * Math.sin(theta);
                target.y = Math.cos(phi);
                target.z = Math.sin(phi) * Math.cos(theta);

                camera.lookAt(target);
            } else {
                // If using gyro, standard Three Euler tracking is directly governed by handleOrientation updates
                const phi = THREE.MathUtils.degToRad(90 - lat);
                const theta = THREE.MathUtils.degToRad(lon);
                const target = new THREE.Vector3();
                target.x = Math.sin(phi) * Math.sin(theta);
                target.y = Math.cos(phi);
                target.z = Math.sin(phi) * Math.cos(theta);
                camera.lookAt(target);
            }

            // 2. Spawn Enemies
            if (now > nextSpawnTime) {
                spawnEnemy();
                nextSpawnTime = now + spawnInterval;
            }

            // 3. Update Enemies
            updateEnemies(dt);

            // 4. Update Laser Visuals
            updateLasers(dt);

            // 5. Update Particle Expansions
            updateParticles(dt);

            // 6. Draw 2D Minimap Radar
            drawRadar();

            // Render Frame
            renderer.render(scene, camera);

            requestAnimationFrame(gameLoop);
        }

        // --- ENEMY SPANNING & MANAGEMENT ---
        class Enemy {
            constructor(type) {
                this.type = type; // 'scout', 'warrior', 'boss'
                this.group = new THREE.Group();
                
                let geometry, material, wireframeColor, coreColor;
                this.speed = 1.8 + (level * 0.2);
                this.scoreValue = 100;
                this.damage = 15;
                this.health = 1;

                if (type === 'boss') {
                    // Big Spikey Boss
                    geometry = new THREE.TorusKnotGeometry(0.8, 0.25, 64, 8);
                    wireframeColor = 0x05ff50; // Neon Green
                    coreColor = 0xffea00;
                    this.speed = 0.7 + (level * 0.1);
                    this.scoreValue = 500;
                    this.damage = 40;
                    this.health = 4;
                    this.scale = 1.4;
                    this.colorHex = '#05ff50';
                } else if (type === 'warrior') {
                    // Octahedron Fighter
                    geometry = new THREE.OctahedronGeometry(0.7, 0);
                    wireframeColor = 0xff0055; // Neon Pink
                    coreColor = 0xff55aa;
                    this.speed = 1.4 + (level * 0.15);
                    this.scoreValue = 250;
                    this.damage = 25;
                    this.health = 2;
                    this.scale = 1.0;
                    this.colorHex = '#ff0055';
                } else {
                    // Swift Scout
                    geometry = new THREE.IcosahedronGeometry(0.5, 0);
                    wireframeColor = 0x00ffff; // Neon Cyan
                    coreColor = 0xaaffff;
                    this.speed = 2.4 + (level * 0.2);
                    this.scoreValue = 100;
                    this.damage = 10;
                    this.health = 1;
                    this.scale = 0.85;
                    this.colorHex = '#00ffff';
                }

                // Outer wireframe shell
                const wireframeMat = new THREE.MeshBasicMaterial({
                    color: wireframeColor,
                    wireframe: true
                });
                const shellMesh = new THREE.Mesh(geometry, wireframeMat);
                this.group.add(shellMesh);

                // Inner glowing energy core
                const coreGeom = new THREE.SphereGeometry(geometry.parameters.radius ? geometry.parameters.radius * 0.4 : 0.2, 8, 8);
                const coreMat = new THREE.MeshBasicMaterial({ color: coreColor });
                const coreMesh = new THREE.Mesh(coreGeom, coreMat);
                this.group.add(coreMesh);

                this.group.scale.set(this.scale, this.scale, this.scale);

                // Spherical Spawning Coordinates around player
                const u = Math.random();
                const v = Math.random();
                const theta = u * 2.0 * Math.PI;
                const phi = Math.acos(2.0 * v - 1.0);

                // Positions strictly on sphere shell radius
                this.group.position.x = SPHERE_RADIUS * Math.sin(phi) * Math.cos(theta);
                this.group.position.y = (SPHERE_RADIUS * Math.sin(phi) * Math.sin(theta)) * 0.6; // Slightly flatten horizon
                this.group.position.z = SPHERE_RADIUS * Math.cos(phi);

                scene.add(this.group);
            }

            tick(dt) {
                // Rotations of core mesh parts
                this.group.children[0].rotation.y += dt * 1.5;
                this.group.children[0].rotation.x += dt * 0.5;

                // Lock-on track directly to center point (Player coordinate 0,0,0)
                this.group.lookAt(0, 0, 0);

                // Move directly towards player
                const dir = new THREE.Vector3(0, 0, 0).sub(this.group.position).normalize();
                this.group.position.addScaledVector(dir, this.speed * dt);
            }
        }

        function spawnEnemy() {
            if (gameState !== 'PLAYING') return;

            let type = 'scout';
            const roll = Math.random();
            
            if (roll > 0.85) {
                type = 'boss';
            } else if (roll > 0.6) {
                type = 'warrior';
            }

            enemies.push(new Enemy(type));
        }

        function updateEnemies(dt) {
            for (let i = enemies.length - 1; i >= 0; i--) {
                const enemy = enemies[i];
                enemy.tick(dt);

                // Evaluate distance to central weapon systems
                const dist = enemy.group.position.length();

                if (dist <= MIN_HIT_DISTANCE) {
                    // Self-destruct impact damage triggered on player
                    playerTakeDamage(enemy.damage);
                    createExplosion(enemy.group.position, enemy.colorHex, 8);
                    
                    // Cleanup
                    scene.remove(enemy.group);
                    enemies.splice(i, 1);
                }
            }
        }

        // --- WEAPONS FIRE & IMPACTS ---
        function onPointerDown(e) {
            isUserInteracting = true;
            onPointerDownMouseX = e.clientX;
            onPointerDownMouseY = e.clientY;
            onPointerDownLon = lon;
            onPointerDownLat = lat;

            // Track tap details to separate click fire vs swipe
            tapStartTime = performance.now();
            tapStartX = e.clientX;
            tapStartY = e.clientY;
        }

        function onPointerMove(e) {
            if (!isUserInteracting) return;

            const dx = e.clientX - onPointerDownMouseX;
            const dy = e.clientY - onPointerDownMouseY;

            // Update Look Rotations based on swipes
            lon = onPointerDownLon - dx * 0.15;
            lat = onPointerDownLat + dy * 0.15;
            lat = Math.max(-80, Math.min(80, lat));
        }

        function onPointerUp(e) {
            isUserInteracting = false;

            const dx = e.clientX - tapStartX;
            const dy = e.clientY - tapStartY;
            const tapDuration = performance.now() - tapStartTime;

            // If user did not drag further than 8px and quick duration, fire weapon!
            if (Math.sqrt(dx * dx + dy * dy) < 8 && tapDuration < 300) {
                fireWeapon(e.clientX, e.clientY);
            }
        }

        function fireWeapon(screenX, screenY) {
            if (gameState !== 'PLAYING') return;

            // Trigger visual kickback of reticle
            const crosshair = document.getElementById('crosshair');
            crosshair.classList.add('active');
            setTimeout(() => crosshair.classList.remove('active'), 100);

            // Audio FX
            playSynthSound('laser');

            // Normalized screen coords for raycasting target
            const mouse = new THREE.Vector2();
            mouse.x = (screenX / window.innerWidth) * 2 - 1;
            mouse.y = -(screenY / window.innerHeight) * 2 + 1;

            raycaster.setFromCamera(mouse, camera);

            // Extract all geometry children for clean intersects
            const targetMeshes = [];
            const meshMap = new Map(); // Link hit child back to parent Enemy object
            
            enemies.forEach(enemy => {
                enemy.group.children.forEach(child => {
                    targetMeshes.push(child);
                    meshMap.set(child.id, enemy);
                });
            });

            const intersects = raycaster.intersectObjects(targetMeshes);
            let hitPoint = new THREE.Vector3();

            if (intersects.length > 0) {
                const hitMesh = intersects[0].object;
                hitPoint.copy(intersects[0].point);
                const targetedEnemy = meshMap.get(hitMesh.id);

                if (targetedEnemy) {
                    damageEnemy(targetedEnemy, hitPoint);
                }
            } else {
                // Aim line into deep empty cyberspace coordinates
                const direction = new THREE.Vector3();
                raycaster.ray.direction.normalize();
                hitPoint.copy(camera.position).addScaledVector(raycaster.ray.direction, SPHERE_RADIUS);
            }

            // Render laser projectile line
            createLaserBeam(hitPoint);
        }

        function damageEnemy(enemy, hitPoint) {
            enemy.health--;

            if (enemy.health <= 0) {
                // Clear Enemy
                playSynthSound('explosion');
                createExplosion(enemy.group.position, enemy.colorHex, enemy.scoreValue / 5);

                score += enemy.scoreValue;
                killsCount++;
                totalKills++;
                updateHUD();

                // Delete
                scene.remove(enemy.group);
                enemies = enemies.filter(e => e !== enemy);

                // Progression checks
                if (killsCount >= 10) {
                    levelUp();
                }
            } else {
                // Survive Hit - Flash Core Bright White
                playSynthSound('damage');
                const origColor = enemy.group.children[1].material.color.getHex();
                enemy.group.children[1].material.color.setHex(0xffffff);
                setTimeout(() => {
                    if (enemy.group && enemy.group.children[1]) {
                        enemy.group.children[1].material.color.setHex(origColor);
                    }
                }, 80);
            }
        }

        // --- LEVEL UP LOGIC ---
        function levelUp() {
            level++;
            killsCount = 0;
            spawnInterval = Math.max(1000, 3000 - (level * 300)); // decrease spawning delta times
            
            // Highlight Alert Box
            const alertBox = document.getElementById('levelAlert');
            alertBox.classList.add('show');
            playSynthSound('levelup');

            setTimeout(() => {
                alertBox.classList.remove('show');
            }, 1800);

            updateHUD();
        }

        // --- PLAYER HARM & VISUAL SHAKES ---
        function playerTakeDamage(amount) {
            shield = Math.max(0, shield - amount);
            updateHUD();

            // Trigger Visual Alerts
            playSynthSound('damage');
            const overlay = document.getElementById('damageIndicator');
            const vignette = document.getElementById('vignette');
            
            overlay.style.opacity = '1';
            vignette.classList.add('hit');

            setTimeout(() => {
                overlay.style.opacity = '0';
                vignette.classList.remove('hit');
            }, 200);

            // Screen rumble simulation
            shakeScreen();

            if (shield <= 0) {
                gameOver();
            }
        }

        function shakeScreen() {
            let shakeTicks = 0;
            const originalPosition = camera.position.clone();

            function doShake() {
                if (shakeTicks < 12 && gameState === 'PLAYING') {
                    camera.position.set(
                        (Math.random() - 0.5) * 0.4,
                        (Math.random() - 0.5) * 0.4,
                        (Math.random() - 0.5) * 0.4
                    );
                    shakeTicks++;
                    requestAnimationFrame(doShake);
                } else {
                    camera.position.set(0, 0, 0);
                }
            }
            doShake();
        }

        // --- HUD ELEMENTS & RADAR DRAWING ---
        function updateHUD() {
            document.getElementById('scoreVal').textContent = score.toString().padStart(6, '0');
            document.getElementById('levelVal').textContent = level;
            document.getElementById('shieldVal').textContent = `${shield}%`;
            document.getElementById('shieldBar').style.width = `${shield}%`;

            if (shield < 30) {
                document.getElementById('shieldBar').style.background = 'var(--neon-pink)';
                document.getElementById('shieldBar').style.boxShadow = '0 0 8px var(--neon-pink)';
            } else {
                document.getElementById('shieldBar').style.background = 'linear-gradient(90deg, var(--neon-cyan), #00bcff)';
                document.getElementById('shieldBar').style.boxShadow = '0 0 8px var(--neon-cyan)';
            }
        }

        function drawRadar() {
            const canvas = document.getElementById('radarCanvas');
            const ctx = canvas.getContext('2d');
            const halfSize = canvas.width / 2;
            
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            // Draw circular radar sweep grids
            ctx.strokeStyle = 'rgba(0, 255, 255, 0.2)';
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.arc(halfSize, halfSize, halfSize - 4, 0, Math.PI * 2);
            ctx.stroke();

            ctx.beginPath();
            ctx.arc(halfSize, halfSize, halfSize / 2, 0, Math.PI * 2);
            ctx.stroke();

            // Intersecting Reticle Lines
            ctx.beginPath();
            ctx.moveTo(halfSize, 0); ctx.lineTo(halfSize, canvas.height);
            ctx.moveTo(0, halfSize); ctx.lineTo(canvas.width, halfSize);
            ctx.stroke();

            // Self Marker (Player)
            ctx.fillStyle = 'var(--neon-pink)';
            ctx.beginPath();
            ctx.arc(halfSize, halfSize, 3, 0, Math.PI * 2);
            ctx.fill();

            // Draw hostile pips
            enemies.forEach(enemy => {
                // Vector transformations local to camera space
                const localPos = enemy.group.position.clone().applyMatrix4(camera.matrixWorldInverse);

                // Map game coordinates to circular scale (Outer limits represent full SPHERE_RADIUS max)
                const scale = (halfSize - 5) / SPHERE_RADIUS;
                
                // localPos.x -> left/right viewport offset
                // localPos.z -> depth offset (Negative coordinates are forwards, positive backwards)
                const px = halfSize + (localPos.x * scale);
                const py = halfSize + (localPos.z * scale);

                // Clamp targets at maximum scope bounds if too distant
                const dx = px - halfSize;
                const dy = py - halfSize;
                const dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < (halfSize - 3)) {
                    ctx.fillStyle = enemy.colorHex;
                    ctx.beginPath();
                    ctx.arc(px, py, 4, 0, Math.PI * 2);
                    ctx.fill();

                    // Ring outline pulse on radar pips
                    ctx.strokeStyle = enemy.colorHex;
                    ctx.beginPath();
                    ctx.arc(px, py, 6, 0, Math.PI * 2);
                    ctx.stroke();
                } else {
                    // Draw outer border warning wedge indicator
                    const angle = Math.atan2(dy, dx);
                    const wx = halfSize + Math.cos(angle) * (halfSize - 6);
                    const wy = halfSize + Math.sin(angle) * (halfSize - 6);
                    
                    ctx.fillStyle = 'var(--neon-pink)';
                    ctx.beginPath();
                    ctx.arc(wx, wy, 3, 0, Math.PI * 2);
                    ctx.fill();
                }
            });
        }

        // --- WEAPONS FX UPDATES ---
        function createLaserBeam(hitPoint) {
            const points = [];
            // Weapon outputs originate visually from weapon barrel below camera
            const origin = new THREE.Vector3(0, -0.6, -1).applyMatrix4(camera.matrixWorld);
            points.push(origin);
            points.push(hitPoint.clone());

            const geom = new THREE.BufferGeometry().setFromPoints(points);
            const mat = new THREE.LineBasicMaterial({
                color: 0x00ffff,
                linewidth: 2
            });
            const line = new THREE.Line(geom, mat);
            scene.add(line);

            laserBeams.push({
                mesh: line,
                life: 0.12 // Render life in seconds
            });
        }

        function updateLasers(dt) {
            for (let i = laserBeams.length - 1; i >= 0; i--) {
                const beam = laserBeams[i];
                beam.life -= dt;
                if (beam.life <= 0) {
                    scene.remove(beam.mesh);
                    beam.mesh.geometry.dispose();
                    beam.mesh.material.dispose();
                    laserBeams.splice(i, 1);
                }
            }
        }

        // --- CYBER PARTICLE EXPLOSIONS ---
        function createExplosion(position, colorHex, count) {
            const particleGeo = new THREE.BoxGeometry(0.12, 0.12, 0.12);
            const col = new THREE.Color(colorHex);

            for (let i = 0; i < count; i++) {
                const mat = new THREE.MeshBasicMaterial({ color: col });
                const mesh = new THREE.Mesh(particleGeo, mat);
                mesh.position.copy(position);

                // Add random direction velocities
                const velocity = new THREE.Vector3(
                    (Math.random() - 0.5) * 12,
                    (Math.random() - 0.5) * 12,
                    (Math.random() - 0.5) * 12
                );

                scene.add(mesh);
                particles.push({
                    mesh: mesh,
                    velocity: velocity,
                    life: 0.8 + Math.random() * 0.4 // Lifetime span
                });
            }
        }

        function updateParticles(dt) {
            for (let i = particles.length - 1; i >= 0; i--) {
                const p = particles[i];
                p.life -= dt;

                if (p.life <= 0) {
                    scene.remove(p.mesh);
                    p.mesh.geometry.dispose();
                    p.mesh.material.dispose();
                    particles.splice(i, 1);
                } else {
                    // Update positional translation & shrink over time
                    p.mesh.position.addScaledVector(p.velocity, dt);
                    const scale = p.life * 1.0;
                    p.mesh.scale.set(scale, scale, scale);
                }
            }
        }

        // --- RESIZE CANVAS HANDLING ---
        function onWindowResize() {
            camera.aspect = window.innerWidth / window.innerHeight;
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        }
    </script>
</body>
</html>