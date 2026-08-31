<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Neon Hunter AR - Shooter Game</title>
    <!-- Google Font for Cyberpunk theme -->
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&display=swap" rel="stylesheet">
    <!-- Three.js Library -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <style>
        :root {
            --neon-blue: #00f3ff;
            --neon-pink: #ff007f;
            --neon-green: #39ff14;
            --glass-bg: rgba(10, 10, 25, 0.75);
            --font-family: 'Orbitron', sans-serif;
        }

        * {
            box-sizing: border-box;
            user-select: none;
            -webkit-user-select: none;
            margin: 0;
            padding: 0;
        }

        html, body {
            width: 100%;
            height: 100%;
            overflow: hidden;
            background-color: #05050c;
            font-family: var(--font-family);
            color: #fff;
        }

        /* Fullscreen containers */
        #app-container {
            position: relative;
            width: 100%;
            height: 100%;
            overflow: hidden;
            touch-action: none;
        }

        /* AR Video Background */
        #webcam-video {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
            z-index: 1;
            transform: scaleX(1); /* No mirror for rear-facing camera */
        }

        /* Three.js Canvas Overlay */
        #game-canvas {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 2;
            pointer-events: auto;
        }

        /* Universal Sci-Fi Overlay/HUD styling */
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

        /* Interactivity on active HUD components */
        .interactive {
            pointer-events: auto;
        }

        /* Screen overlays for damage & flash effects */
        #damage-flash {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(255, 0, 85, 0.4);
            opacity: 0;
            z-index: 4;
            pointer-events: none;
            transition: opacity 0.1s ease-out;
        }

        /* Screens: Start, Game Over, Permissions */
        .screen-overlay {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 10;
            background: radial-gradient(circle, rgba(15, 15, 35, 0.95) 0%, rgba(5, 5, 15, 0.98) 100%);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 30px;
            text-align: center;
            transition: opacity 0.4s ease, transform 0.4s ease;
        }

        .screen-overlay.hidden {
            opacity: 0;
            pointer-events: none;
            transform: scale(1.05);
        }

        h1 {
            font-size: 2.8rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 3px;
            color: #fff;
            text-shadow: 0 0 15px var(--neon-blue), 0 0 30px var(--neon-blue);
            margin-bottom: 10px;
        }

        h1 span {
            color: var(--neon-pink);
            text-shadow: 0 0 15px var(--neon-pink), 0 0 30px var(--neon-pink);
        }

        p.subtitle {
            font-size: 1rem;
            color: #8fa0dd;
            margin-bottom: 40px;
            max-width: 500px;
            line-height: 1.6;
        }

        /* Action Buttons */
        .btn {
            background: linear-gradient(135deg, var(--neon-blue) 0%, #005f9e 100%);
            color: white;
            border: none;
            padding: 15px 40px;
            font-size: 1.1rem;
            font-weight: bold;
            font-family: var(--font-family);
            text-transform: uppercase;
            letter-spacing: 2px;
            border-radius: 4px;
            cursor: pointer;
            box-shadow: 0 0 15px rgba(0, 243, 255, 0.4);
            transition: all 0.2s ease;
            position: relative;
            overflow: hidden;
            margin: 10px;
        }

        .btn::after {
            content: '';
            position: absolute;
            top: 0;
            left: -50%;
            width: 200%;
            height: 100%;
            background: linear-gradient(to right, rgba(255,255,255,0) 0%, rgba(255,255,255,0.3) 50%, rgba(255,255,255,0) 100%);
            transform: skewX(-25deg);
            transition: 0.75s;
        }

        .btn:hover::after {
            left: 120%;
        }

        .btn:active {
            transform: scale(0.96);
            box-shadow: 0 0 5px rgba(0, 243, 255, 0.2);
        }

        .btn-pink {
            background: linear-gradient(135deg, var(--neon-pink) 0%, #9e0050 100%);
            box-shadow: 0 0 15px rgba(255, 0, 127, 0.4);
        }

        /* Heads Up Display Layout */
        .hud-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            width: 100%;
        }

        .hud-panel {
            background: var(--glass-bg);
            border: 1px solid rgba(0, 243, 255, 0.3);
            border-radius: 8px;
            padding: 10px 18px;
            box-shadow: 0 0 15px rgba(0, 243, 255, 0.1);
            backdrop-filter: blur(5px);
            -webkit-backdrop-filter: blur(5px);
        }

        .panel-label {
            font-size: 0.65rem;
            text-transform: uppercase;
            color: #8fa0dd;
            letter-spacing: 2px;
            margin-bottom: 2px;
        }

        .panel-value {
            font-size: 1.5rem;
            font-weight: bold;
            color: var(--neon-blue);
            text-shadow: 0 0 8px rgba(0, 243, 255, 0.5);
        }

        .health-bar-container {
            width: 160px;
            height: 10px;
            background: rgba(255,255,255,0.1);
            border-radius: 5px;
            margin-top: 5px;
            overflow: hidden;
            border: 1px solid rgba(255, 0, 127, 0.3);
        }

        #health-bar-fill {
            width: 100%;
            height: 100%;
            background: linear-gradient(to right, var(--neon-pink), #ff0055);
            box-shadow: 0 0 8px var(--neon-pink);
            transition: width 0.3s ease;
        }

        /* Target Radar (Crucial for finding enemies 360) */
        #radar-container {
            position: relative;
            width: 85px;
            height: 85px;
            border-radius: 50%;
            background: rgba(10, 10, 25, 0.6);
            border: 2px solid var(--neon-green);
            box-shadow: 0 0 10px rgba(57, 255, 20, 0.2);
            display: flex;
            align-items: center;
            justify-content: center;
        }

        #radar-sweep {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            border-radius: 50%;
            background: conic-gradient(from 0deg, rgba(57, 255, 20, 0.15) 0deg, rgba(57, 255, 20, 0) 90deg);
            animation: radar-sweep-animation 3s linear infinite;
            pointer-events: none;
        }

        #radar-canvas {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
        }

        @keyframes radar-sweep-animation {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }

        /* Center Crosshair */
        #crosshair-container {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 60px;
            height: 60px;
            pointer-events: none;
            z-index: 3;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .crosshair-element {
            position: absolute;
            border: 2px solid var(--neon-blue);
            box-shadow: 0 0 8px var(--neon-blue);
            transition: all 0.1s ease;
        }

        .crosshair-ring {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            border-style: dashed;
        }

        .crosshair-dot {
            width: 6px;
            height: 6px;
            background-color: var(--neon-pink);
            box-shadow: 0 0 8px var(--neon-pink);
            border-radius: 50%;
            border: none;
        }

        .crosshair-h-line {
            width: 20px;
            height: 0;
            border-bottom: 2px solid var(--neon-blue);
        }

        .crosshair-v-line {
            width: 0;
            height: 20px;
            border-left: 2px solid var(--neon-blue);
        }

        /* Bottom Controls (Ammo / Reload) */
        .hud-footer {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            width: 100%;
        }

        .ammo-panel {
            min-width: 120px;
        }

        .ammo-indicators {
            display: flex;
            gap: 4px;
            margin-top: 8px;
        }

        .ammo-pip {
            width: 8px;
            height: 18px;
            background-color: var(--neon-blue);
            box-shadow: 0 0 5px var(--neon-blue);
            border-radius: 2px;
            transition: opacity 0.15s, background-color 0.15s;
        }

        .ammo-pip.spent {
            background-color: rgba(255,255,255,0.1);
            box-shadow: none;
        }

        /* Instructions Banner */
        #hint-banner {
            background: rgba(10, 10, 25, 0.85);
            border: 1px solid rgba(255, 255, 255, 0.1);
            color: #fff;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            animation: pulse-glow 2s infinite alternate;
            margin-bottom: 15px;
            align-self: center;
        }

        @keyframes pulse-glow {
            from { box-shadow: 0 0 5px rgba(255,255,255,0.1); }
            to { box-shadow: 0 0 15px rgba(0, 243, 255, 0.3); }
        }

        /* Quick Settings Panel */
        #settings-trigger {
            background: var(--glass-bg);
            border: 1px solid rgba(255, 255, 255, 0.15);
            color: white;
            width: 44px;
            height: 44px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            box-shadow: 0 4px 10px rgba(0,0,0,0.3);
        }

        /* Floating Info Panels */
        .combo-alert {
            position: absolute;
            top: 30%;
            left: 50%;
            transform: translate(-50%, -50%) scale(0.5);
            font-size: 1.8rem;
            font-weight: 900;
            color: var(--neon-pink);
            text-shadow: 0 0 10px var(--neon-pink);
            opacity: 0;
            pointer-events: none;
            z-index: 5;
            transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }

        .combo-alert.show {
            opacity: 1;
            transform: translate(-50%, -50%) scale(1.2);
        }

        /* Device Orientation Request Block */
        #sensor-panel {
            background: rgba(255, 255, 255, 0.05);
            border: 1px dashed rgba(0, 243, 255, 0.3);
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 25px;
            width: 100%;
            max-width: 400px;
        }

        /* Fallback Notice */
        .fallback-badge {
            background-color: #ff9800;
            color: black;
            font-size: 0.65rem;
            font-weight: bold;
            padding: 3px 8px;
            border-radius: 10px;
            text-transform: uppercase;
            display: none;
            margin-top: 5px;
        }

        /* Custom scrollbar just in case */
        ::-webkit-scrollbar {
            width: 6px;
        }
        ::-webkit-scrollbar-track {
            background: rgba(0,0,0,0.1);
        }
        ::-webkit-scrollbar-thumb {
            background: var(--neon-blue);
            border-radius: 3px;
        }
    </style>
</head>
<body>

    <div id="app-container">
        <!-- Live Camera Stream Background -->
        <video id="webcam-video" autoplay playsinline muted></video>

        <!-- WebGL Virtual World Rendering -->
        <canvas id="game-canvas"></canvas>

        <!-- Dynamic Screen Flash Layer (taking damage) -->
        <div id="damage-flash"></div>

        <!-- Float Combo Notification -->
        <div id="combo-popup" class="combo-alert">CRITICAL HIT!</div>

        <!-- 1. PERMISSION AND CALIBRATION START SCREEN -->
        <div id="start-screen" class="screen-overlay">
            <h1>NEON <span>HUNTER AR</span></h1>
            <p class="subtitle">An immersive mixed-reality shooter. Track and eliminate the cyber-drones entering your physical airspace.</p>
            
            <div id="sensor-panel">
                <p style="font-size: 0.85rem; color:#a5b5e9; margin-bottom: 10px;">To play in AR, we need camera and gyroscope access. If blocked or on desktop, interactive touch-drag mode will automatically activate.</p>
                <div id="fallback-status" class="fallback-badge">VIRTUAL SIMULATOR READY (NO GYRO)</div>
            </div>

            <button id="btn-init" class="btn">INITIALIZE CORE AR</button>
        </div>

        <!-- 2. GAME OVER OVERLAY -->
        <div id="gameover-screen" class="screen-overlay hidden">
            <h1 style="color:var(--neon-pink); text-shadow: 0 0 15px var(--neon-pink);">SYSTEM CRITICAL</h1>
            <p style="font-size: 1.2rem; margin-bottom: 5px;">AIRSPACE BREACHED</p>
            <p class="subtitle" id="final-stats">Drones Downed: 0 | Score: 0</p>
            
            <button id="btn-restart" class="btn btn-pink">REBOOT SYSTEM & RETRY</button>
        </div>

        <!-- 3. REALTIME HEADS UP DISPLAY (HUD) -->
        <div class="hud-layer">
            <!-- Top HUD Bar -->
            <div class="hud-header">
                <!-- Health Tracker -->
                <div class="hud-panel">
                    <div class="panel-label">Shield Integrity</div>
                    <div class="panel-value" id="health-text">100%</div>
                    <div class="health-bar-container">
                        <div id="health-bar-fill"></div>
                    </div>
                </div>

                <!-- Minimap / Compass Radar -->
                <div id="radar-container">
                    <div id="radar-sweep"></div>
                    <canvas id="radar-canvas" width="85" height="85"></canvas>
                </div>

                <!-- Global Score Tracker -->
                <div class="hud-panel" style="text-align: right;">
                    <div class="panel-label">SYS_SCORE</div>
                    <div id="score-text" class="panel-value">000000</div>
                    <div class="panel-label" style="font-size:0.55rem; margin-top:2px;" id="high-score">HI_SCORE: 0000</div>
                </div>
            </div>

            <!-- Central HUD Elements -->
            <div id="crosshair-container">
                <div class="crosshair-element crosshair-ring"></div>
                <div class="crosshair-element crosshair-h-line"></div>
                <div class="crosshair-element crosshair-v-line"></div>
                <div class="crosshair-element crosshair-dot"></div>
            </div>

            <!-- Bottom Control panel -->
            <div class="hud-footer">
                <!-- Ammo Panel -->
                <div class="hud-panel ammo-panel">
                    <div class="panel-label">LASER CHARGES</div>
                    <div class="panel-value" id="ammo-text">10 / 10</div>
                    <div class="ammo-indicators" id="ammo-pips-container">
                        <!-- Instantiated programmatically -->
                    </div>
                </div>

                <!-- Contextual Hint Overlay -->
                <div id="hint-banner">TAP CENTER TARGETS TO FIRE</div>

                <!-- Settings / Sound Controls -->
                <div id="settings-trigger" class="interactive">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                        <path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path>
                    </svg>
                </div>
            </div>
        </div>
    </div>

    <script>
        /**
         * Global State Controller
         */
        const state = {
            isPlaying: false,
            score: 0,
            highScore: parseInt(localStorage.getItem('ar_neon_high') || '0'),
            health: 100,
            ammo: 10,
            maxAmmo: 10,
            isReloading: false,
            waveCount: 1,
            audioEnabled: true,
            gyroscopeActive: false,
            isMobile: false,
            cameraReady: false,
            deviceYaw: 0,
            devicePitch: 0,
            dragOffsetYaw: 0,
            dragOffsetPitch: 0
        };

        // DOM elements cache
        const dom = {
            appContainer: document.getElementById('app-container'),
            webcamVideo: document.getElementById('webcam-video'),
            gameCanvas: document.getElementById('game-canvas'),
            startScreen: document.getElementById('start-screen'),
            gameoverScreen: document.getElementById('gameover-screen'),
            btnInit: document.getElementById('btn-init'),
            btnRestart: document.getElementById('btn-restart'),
            healthText: document.getElementById('health-text'),
            healthBarFill: document.getElementById('health-bar-fill'),
            scoreText: document.getElementById('score-text'),
            highScoreText: document.getElementById('high-score'),
            ammoText: document.getElementById('ammo-text'),
            ammoContainer: document.getElementById('ammo-pips-container'),
            radarCanvas: document.getElementById('radar-canvas'),
            damageFlash: document.getElementById('damage-flash'),
            comboPopup: document.getElementById('combo-popup'),
            settingsTrigger: document.getElementById('settings-trigger'),
            hintBanner: document.getElementById('hint-banner'),
            fallbackStatus: document.getElementById('fallback-status')
        };

        // Web Audio System Synthesis (No remote asset dependency)
        const audio = {
            ctx: null,

            init() {
                if (this.ctx) return;
                this.ctx = new (window.AudioContext || window.webkitAudioContext)();
            },

            play(type) {
                if (!state.audioEnabled || !this.ctx) return;
                if (this.ctx.state === 'suspended') this.ctx.resume();

                const now = this.ctx.currentTime;
                
                switch(type) {
                    case 'shoot':
                        const osc = this.ctx.createOscillator();
                        const gain = this.ctx.createGain();
                        osc.type = 'sawtooth';
                        osc.frequency.setValueAtTime(800, now);
                        osc.frequency.exponentialRampToValueAtTime(80, now + 0.15);
                        
                        gain.gain.setValueAtTime(0.2, now);
                        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.15);
                        
                        osc.connect(gain);
                        gain.connect(this.ctx.destination);
                        osc.start(now);
                        osc.stop(now + 0.15);
                        break;

                    case 'hit':
                        const hOsc = this.ctx.createOscillator();
                        const hGain = this.ctx.createGain();
                        hOsc.type = 'triangle';
                        hOsc.frequency.setValueAtTime(220, now);
                        hOsc.frequency.linearRampToValueAtTime(50, now + 0.2);
                        
                        hGain.gain.setValueAtTime(0.3, now);
                        hGain.gain.exponentialRampToValueAtTime(0.01, now + 0.25);
                        
                        hOsc.connect(hGain);
                        hGain.connect(this.ctx.destination);
                        hOsc.start(now);
                        hOsc.stop(now + 0.25);
                        break;

                    case 'reload':
                        const rOsc = this.ctx.createOscillator();
                        const rGain = this.ctx.createGain();
                        rOsc.type = 'sine';
                        rOsc.frequency.setValueAtTime(300, now);
                        rOsc.frequency.exponentialRampToValueAtTime(900, now + 0.4);
                        
                        rGain.gain.setValueAtTime(0.15, now);
                        rGain.gain.linearRampToValueAtTime(0.15, now + 0.3);
                        rGain.gain.exponentialRampToValueAtTime(0.001, now + 0.4);
                        
                        rOsc.connect(rGain);
                        rGain.connect(this.ctx.destination);
                        rOsc.start(now);
                        rOsc.stop(now + 0.4);
                        break;

                    case 'damage':
                        const dOsc = this.ctx.createOscillator();
                        const dGain = this.ctx.createGain();
                        dOsc.type = 'sawtooth';
                        dOsc.frequency.setValueAtTime(100, now);
                        dOsc.frequency.linearRampToValueAtTime(30, now + 0.5);
                        
                        dGain.gain.setValueAtTime(0.4, now);
                        dGain.gain.exponentialRampToValueAtTime(0.01, now + 0.5);
                        
                        dOsc.connect(dGain);
                        dGain.connect(this.ctx.destination);
                        dOsc.start(now);
                        dOsc.stop(now + 0.5);
                        break;

                    case 'gameover':
                        const gOsc = this.ctx.createOscillator();
                        const gGain = this.ctx.createGain();
                        gOsc.type = 'sawtooth';
                        gOsc.frequency.setValueAtTime(150, now);
                        gOsc.frequency.exponentialRampToValueAtTime(30, now + 1.2);
                        
                        gGain.gain.setValueAtTime(0.4, now);
                        gGain.gain.exponentialRampToValueAtTime(0.001, now + 1.2);
                        
                        gOsc.connect(gGain);
                        gGain.connect(this.ctx.destination);
                        gOsc.start(now);
                        gOsc.stop(now + 1.2);
                        break;
                }
            }
        };

        // UI Updates
        function setupAmmoHUD() {
            dom.ammoContainer.innerHTML = '';
            for(let i = 0; i < state.maxAmmo; i++) {
                const pip = document.createElement('div');
                pip.className = 'ammo-pip';
                dom.ammoContainer.appendChild(pip);
            }
        }

        function updateAmmoDisplay() {
            dom.ammoText.innerText = `${state.ammo} / ${state.maxAmmo}`;
            const pips = dom.ammoContainer.querySelectorAll('.ammo-pip');
            pips.forEach((pip, index) => {
                if (index < state.ammo) {
                    pip.classList.remove('spent');
                } else {
                    pip.classList.add('spent');
                }
            });
            if (state.ammo === 0) {
                dom.hintBanner.innerText = 'TAP AND HOLD OR PRESS SCREEN TO AUTO-RELOAD';
            }
        }

        function updateScore(points) {
            state.score += points;
            dom.scoreText.innerText = String(state.score).padStart(6, '0');
            if (state.score > state.highScore) {
                state.highScore = state.score;
                localStorage.setItem('ar_neon_high', state.highScore);
                dom.highScoreText.innerText = `HI_SCORE: ${String(state.highScore).padStart(4, '0')}`;
            }
        }

        function triggerDamageFlash() {
            dom.damageFlash.style.opacity = '1';
            setTimeout(() => {
                dom.damageFlash.style.opacity = '0';
            }, 150);
        }

        function adjustShield(amount) {
            state.health = Math.max(0, state.health + amount);
            dom.healthText.innerText = `${state.health}%`;
            dom.healthBarFill.style.width = `${state.health}%`;

            if (state.health <= 0 && state.isPlaying) {
                endGame();
            }
        }

        function showComboAlert(message) {
            dom.comboPopup.innerText = message;
            dom.comboPopup.classList.add('show');
            setTimeout(() => {
                dom.comboPopup.classList.remove('show');
            }, 1000);
        }

        /**
         * Camera & Gyroscope Systems Initialization (AR setup)
         */
        async function initCamera() {
            try {
                const constraints = {
                    video: {
                        facingMode: 'environment',
                        width: { ideal: 1280 },
                        height: { ideal: 720 }
                    },
                    audio: false
                };
                const stream = await navigator.mediaDevices.getUserMedia(constraints);
                dom.webcamVideo.srcObject = stream;
                state.cameraReady = true;
                return true;
            } catch (err) {
                console.warn('Webcam feed blocked or unavailable. Entering Virtual Space Mode.', err);
                dom.webcamVideo.style.display = 'none';
                state.cameraReady = false;
                return false;
            }
        }

        function requestOrientationAccess() {
            return new Promise((resolve) => {
                // iOS 13+ requires permissions to access DeviceOrientation events
                if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
                    DeviceOrientationEvent.requestPermission()
                        .then(response => {
                            if (response === 'granted') {
                                window.addEventListener('deviceorientation', onDeviceOrientation, true);
                                state.gyroscopeActive = true;
                                resolve(true);
                            } else {
                                state.gyroscopeActive = false;
                                resolve(false);
                            }
                        })
                        .catch(err => {
                            console.error('Error requesting orientation permission', err);
                            state.gyroscopeActive = false;
                            resolve(false);
                        });
                } else {
                    // Standard Web standards / Android / Desktop fallback
                    if ('ondeviceorientation' in window) {
                        window.addEventListener('deviceorientation', onDeviceOrientation, true);
                        state.gyroscopeActive = true;
                        resolve(true);
                    } else {
                        state.gyroscopeActive = false;
                        resolve(false);
                    }
                }
            });
        }

        // Keep track of first reference gyro readings to implement relative orientation offsets
        let baseAlpha = null;
        let baseBeta = null;

        function onDeviceOrientation(event) {
            if (!state.isPlaying) return;

            // iOS absolute tracking alpha, beta
            let alpha = event.alpha; // Compass orientation (yaw) [0-360]
            let beta = event.beta;   // Front-back tilt (pitch) [-180, 180]

            if (alpha === null || beta === null) return;

            if (baseAlpha === null) {
                baseAlpha = alpha;
                baseBeta = beta;
            }

            // Convert to relative coordinate changes
            let diffAlpha = alpha - baseAlpha;
            let diffBeta = beta - baseBeta;

            // Handle wrap-around math
            if (diffAlpha > 180) diffAlpha -= 360;
            if (diffAlpha < -180) diffAlpha += 360;

            // Apply updates smoothly to device target rotation vectors (scaled for responsiveness)
            state.deviceYaw = THREE.MathUtils.degToRad(diffAlpha);
            state.devicePitch = THREE.MathUtils.degToRad(diffBeta);
        }

        // Setup Desktop Touch-Drag Interaction
        let isPointerDown = false;
        let prevPointerX = 0;
        let prevPointerY = 0;
        let dragThresholdMet = false;

        function setupDragInteraction() {
            const el = dom.gameCanvas;

            el.addEventListener('pointerdown', (e) => {
                isPointerDown = true;
                prevPointerX = e.clientX;
                prevPointerY = e.clientY;
                dragThresholdMet = false;
            });

            el.addEventListener('pointermove', (e) => {
                if (!isPointerDown) return;
                const dx = e.clientX - prevPointerX;
                const dy = e.clientY - prevPointerY;
                
                if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
                    dragThresholdMet = true;
                }

                // Smooth relative orientation changes
                const rotationSensitivity = 0.0035;
                state.dragOffsetYaw -= dx * rotationSensitivity;
                // Clamp looking pitch to prevent flipping upside down
                state.dragOffsetPitch = Math.max(-Math.PI / 2.5, Math.min(Math.PI / 2.5, state.dragOffsetPitch - dy * rotationSensitivity));

                prevPointerX = e.clientX;
                prevPointerY = e.clientY;
            });

            window.addEventListener('pointerup', (e) => {
                if (!isPointerDown) return;
                isPointerDown = false;

                // Tap (No substantial drag detected) maps directly to fire
                if (!dragThresholdMet && state.isPlaying) {
                    handlePlayerFire(e.clientX, e.clientY);
                }
            });
        }


        /**
         * Three.js Interactive Gameplay World Representation
         */
        let scene, camera, renderer;
        let enemies = [];
        let particles = [];
        let lasers = [];
        let skyboxGrid; // Fallback context representation in case of camera failure

        function initThreeJS() {
            const width = window.innerWidth;
            const height = window.innerHeight;

            scene = new THREE.Scene();
            camera = new THREE.PerspectiveCamera(70, width / height, 0.1, 1000);
            
            // WebGL Renderer with Alpha:true enabled to render translucent visual layers on top of camera stream
            renderer = new THREE.WebGLRenderer({
                canvas: dom.gameCanvas,
                antialias: true,
                alpha: true
            });
            renderer.setSize(width, height);
            renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

            // Lighting Setup
            const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
            scene.add(ambientLight);

            const directionalLight = new THREE.DirectionalLight(0x00f3ff, 1.2);
            directionalLight.position.set(0, 5, 10);
            scene.add(directionalLight);

            const pointLight = new THREE.PointLight(0xff007f, 1, 30);
            pointLight.position.set(0, 0, 0);
            scene.add(pointLight);

            // Construct Neon Grid (Fallback Virtual Space)
            const gridHelper = new THREE.GridHelper(100, 50, 0xff007f, 0x00f3ff);
            gridHelper.position.y = -10;
            scene.add(gridHelper);

            // Create some starfields for depth
            const starsGeom = new THREE.BufferGeometry();
            const starsCount = 500;
            const starPositions = new Float32Array(starsCount * 3);

            for (let i = 0; i < starsCount * 3; i += 3) {
                // Distribute on sphere of radius ~50
                const u = Math.random();
                const v = Math.random();
                const theta = u * 2.0 * Math.PI;
                const phi = Math.acos(2.0 * v - 1.0);
                const r = 40 + Math.random() * 20;

                starPositions[i] = r * Math.sin(phi) * Math.cos(theta);
                starPositions[i+1] = r * Math.sin(phi) * Math.sin(theta);
                starPositions[i+2] = r * Math.cos(phi);
            }

            starsGeom.setAttribute('position', new THREE.BufferAttribute(starPositions, 3));
            const starsMat = new THREE.PointsMaterial({
                color: 0x00f3ff,
                size: 0.15,
                transparent: true,
                opacity: 0.8
            });
            const starField = new THREE.Points(starsGeom, starsMat);
            scene.add(starField);

            window.addEventListener('resize', onWindowResize);
        }

        function onWindowResize() {
            const width = window.innerWidth;
            const height = window.innerHeight;
            camera.aspect = width / height;
            camera.updateProjectionMatrix();
            renderer.setSize(width, height);
        }

        /**
         * Enemy Architecture and Compound Geometry Instantiations
         */
        class CyberDrone {
            constructor() {
                this.mesh = new THREE.Group();

                // Generate outer energy armor rings
                const ringGeom = new THREE.TorusGeometry(1.2, 0.08, 10, 32);
                const ringMat = new THREE.MeshStandardMaterial({
                    color: 0x00f3ff,
                    emissive: 0x005f9e,
                    wireframe: true
                });
                this.outerRing = new THREE.Mesh(ringGeom, ringMat);
                this.mesh.add(this.outerRing);

                // Inner core energy sphere
                const coreGeom = new THREE.OctahedronGeometry(0.65, 1);
                const coreMat = new THREE.MeshStandardMaterial({
                    color: 0xff007f,
                    emissive: 0x5a0028,
                    roughness: 0.1,
                    metalness: 0.9
                });
                this.core = new THREE.Mesh(coreGeom, coreMat);
                this.mesh.add(this.core);

                // Floating sensor eyes
                const eyeGeom = new THREE.BoxGeometry(0.2, 0.2, 0.2);
                const eyeMat = new THREE.MeshBasicMaterial({ color: 0x39ff14 });
                for(let i = 0; i < 4; i++) {
                    const eye = new THREE.Mesh(eyeGeom, eyeMat);
                    const angle = (i / 4) * Math.PI * 2;
                    eye.position.set(Math.cos(angle)*1.2, 0, Math.sin(angle)*1.2);
                    this.mesh.add(eye);
                }

                // Game stats
                this.id = Math.random();
                this.health = 1 + Math.floor(state.waveCount * 0.5);
                this.speed = 1.8 + Math.random() * 1.5 + (state.waveCount * 0.15);
                this.sizeScale = 1.0;

                // Set initial random spherical coordinate placement around player camera
                const u = Math.random();
                const v = Math.random();
                const theta = u * 2.0 * Math.PI;
                const phi = Math.acos(2.0 * v - 1.0);
                const initialSpawnRadius = 30 + Math.random() * 10;

                this.mesh.position.set(
                    initialSpawnRadius * Math.sin(phi) * Math.cos(theta),
                    (initialSpawnRadius * Math.sin(phi) * Math.sin(theta)) * 0.5, // keep somewhat level
                    initialSpawnRadius * Math.cos(phi)
                );

                scene.add(this.mesh);
            }

            update(delta) {
                // Animate individual structural parts
                this.outerRing.rotation.y += delta * 1.5;
                this.outerRing.rotation.x += delta * 0.7;
                this.core.rotation.z -= delta * 0.9;

                // Steer towards target origin coordinates (0, 0, 0 where camera sits)
                const dir = new THREE.Vector3().copy(this.mesh.position).multiplyScalar(-1).normalize();
                
                // Keep drones floating at varying elevations to push player's tracking capability
                this.mesh.position.addScaledVector(dir, this.speed * delta);

                // Rotate object towards player to keep face-on
                this.mesh.lookAt(0, 0, 0);

                // Check distance threshold to player camera
                const distToPlayer = this.mesh.position.distanceTo(new THREE.Vector3(0,0,0));
                if (distToPlayer < 4.0) {
                    // Explode on player, triggering high shield degradation
                    this.detonateOnPlayer();
                    return false; // Request deletion from active entity array
                }
                return true; // Keep alive
            }

            takeDamage(dmg) {
                this.health -= dmg;
                audio.play('hit');
                
                // Visual dynamic response scale pop
                this.mesh.scale.set(1.4, 1.4, 1.4);
                setTimeout(() => {
                    if (this.mesh) this.mesh.scale.set(1,1,1);
                }, 80);

                if (this.health <= 0) {
                    createExplosion(this.mesh.position, 0xff007f);
                    updateScore(150 * state.waveCount);
                    showComboAlert("DESTROYED!");
                    scene.remove(this.mesh);
                    return true; // Dead
                }
                return false; // Still active
            }

            detonateOnPlayer() {
                audio.play('damage');
                triggerDamageFlash();
                adjustShield(-15);
                createExplosion(this.mesh.position, 0x00f3ff);
                scene.remove(this.mesh);
            }

            destroyQuietly() {
                scene.remove(this.mesh);
            }
        }

        /**
         * Dynamic Particle Explosion Systems
         */
        class ParticleExplosion {
            constructor(pos, color) {
                this.particles = [];
                const count = 25;
                const geom = new THREE.BufferGeometry();
                const positions = new Float32Array(count * 3);
                this.velocities = [];

                for (let i = 0; i < count; i++) {
                    positions[i*3] = pos.x;
                    positions[i*3+1] = pos.y;
                    positions[i*3+2] = pos.z;

                    this.velocities.push(new THREE.Vector3(
                        (Math.random() - 0.5) * 12,
                        (Math.random() - 0.5) * 12,
                        (Math.random() - 0.5) * 12
                    ));
                }

                geom.setAttribute('position', new THREE.BufferAttribute(positions, 3));
                const mat = new THREE.PointsMaterial({
                    color: color,
                    size: 0.35,
                    transparent: true,
                    opacity: 1.0,
                    blending: THREE.AdditiveBlending
                });

                this.points = new THREE.Points(geom, mat);
                this.life = 1.0;
                scene.add(this.points);
            }

            update(delta) {
                this.life -= delta * 1.8;
                if (this.life <= 0) {
                    scene.remove(this.points);
                    this.points.geometry.dispose();
                    this.points.material.dispose();
                    return false;
                }

                const posArr = this.points.geometry.attributes.position.array;
                for (let i = 0; i < posArr.length / 3; i++) {
                    posArr[i*3] += this.velocities[i].x * delta;
                    posArr[i*3+1] += this.velocities[i].y * delta;
                    posArr[i*3+2] += this.velocities[i].z * delta;
                }
                this.points.geometry.attributes.position.needsUpdate = true;
                this.points.material.opacity = this.life;
                return true;
            }
        }

        /**
         * Energy Projectile Tracers
         */
        class LaserBolt {
            constructor(targetVector) {
                // Trace from screen peripheral (bottom right) to 3D target coordinates
                const origin = new THREE.Vector3(0.6, -0.8, -1.5).applyMatrix4(camera.matrixWorld);
                const points = [origin, targetVector];
                
                const geom = new THREE.BufferGeometry().setFromPoints(points);
                const mat = new THREE.LineBasicMaterial({
                    color: 0x00f3ff,
                    transparent: true,
                    opacity: 1.0,
                    linewidth: 3 // Note: Custom linewidth is restricted on some WebGL stacks, line looks clean regardless
                });

                this.line = new THREE.Line(geom, mat);
                this.life = 1.0;
                scene.add(this.line);
            }

            update(delta) {
                this.life -= delta * 6.0; // Dissipates rapidly
                if (this.life <= 0) {
                    scene.remove(this.line);
                    this.line.geometry.dispose();
                    this.line.material.dispose();
                    return false;
                }
                this.line.material.opacity = this.life;
                return true;
            }
        }

        function createExplosion(pos, color) {
            particles.push(new ParticleExplosion(pos, color));
        }

        /**
         * Gameplay Loop Logic & Input Interceptors
         */
        function handlePlayerFire(clientX, clientY) {
            if (state.isReloading) return;
            if (state.ammo <= 0) {
                triggerReload();
                return;
            }

            state.ammo--;
            updateAmmoDisplay();
            audio.play('shoot');

            // Perform 3D raycast to translate screen center coordinates directly into viewport 3D vectors
            const raycaster = new THREE.Raycaster();
            // Fire center screen (0, 0)
            const ndc = new THREE.Vector2(0, 0);
            raycaster.setFromCamera(ndc, camera);

            // Intersect targeted enemies
            const targetableMeshes = enemies.map(e => e.core);
            const intersects = raycaster.intersectObjects(targetableMeshes);

            let hitPoint = new THREE.Vector3();
            raycaster.ray.at(30, hitPoint); // Standard vector point depth fallback for tracer line

            if (intersects.length > 0) {
                const hitObj = intersects[0].object;
                hitPoint.copy(intersects[0].point);

                // Look up parent drone associated with standard core mesh hits
                const droneInstance = enemies.find(e => e.core === hitObj);
                if (droneInstance) {
                    const isDead = droneInstance.takeDamage(1);
                    if (isDead) {
                        enemies = enemies.filter(e => e.id !== droneInstance.id);
                    }
                }
            } else {
                // Splash/Miss indicator at distance
                setTimeout(() => {
                    createExplosion(hitPoint, 0x005f9e);
                }, 100);
            }

            // Fire tracer laser bolt visual feedback
            lasers.push(new LaserBolt(hitPoint));
        }

        function triggerReload() {
            if (state.isReloading) return;
            state.isReloading = true;
            dom.hintBanner.innerText = "RECHARGING LASER CORES...";
            audio.play('reload');

            let countdown = 1.2; // seconds
            const interval = setInterval(() => {
                state.ammo = Math.min(state.maxAmmo, state.ammo + 2);
                updateAmmoDisplay();
                if (state.ammo >= state.maxAmmo) {
                    clearInterval(interval);
                    state.isReloading = false;
                    dom.hintBanner.innerText = "SYSTEM CHARGED - SHOOT";
                }
            }, 200);
        }

        // Radar Dynamic renderer
        function drawRadar() {
            const ctx = dom.radarCanvas.getContext('2d');
            const center = dom.radarCanvas.width / 2;
            ctx.clearRect(0, 0, dom.radarCanvas.width, dom.radarCanvas.height);

            // Fetch camera yaw direction from orientation vectors
            const camDir = new THREE.Vector3();
            camera.getWorldDirection(camDir);
            const camYaw = Math.atan2(camDir.x, camDir.z);

            enemies.forEach(enemy => {
                // Track relative direction from camera position
                const relPos = new THREE.Vector3().copy(enemy.mesh.position);
                const dist = relPos.length();
                
                // Map distance scaling within the scope of radar radius
                const maxRadarDist = 40;
                const radialScale = (dist / maxRadarDist) * (center - 5);

                const angleToEnemy = Math.atan2(relPos.x, relPos.z);
                // Rotate mapping relative to camera horizontal yaw view
                const relativeAngle = angleToEnemy - camYaw;

                const x = center + Math.sin(relativeAngle) * radialScale;
                const y = center - Math.cos(relativeAngle) * radialScale;

                // Render Blip
                ctx.beginPath();
                ctx.arc(x, y, 3, 0, Math.PI * 2);
                ctx.fillStyle = '#ff007f';
                ctx.shadowColor = '#ff007f';
                ctx.shadowBlur = 6;
                ctx.fill();
                ctx.closePath();
            });

            // Draw player center reference anchor
            ctx.beginPath();
            ctx.arc(center, center, 2, 0, Math.PI * 2);
            ctx.fillStyle = '#00f3ff';
            ctx.fill();
            ctx.closePath();
        }

        // Keep Track of Active Waves
        function checkWaveProgress() {
            if (enemies.length === 0 && state.isPlaying) {
                state.waveCount++;
                showComboAlert(`WAVE ${state.waveCount} DETECTED`);
                // Spawn higher number of cyber drones
                const spawnCount = 3 + state.waveCount;
                for (let i = 0; i < spawnCount; i++) {
                    enemies.push(new CyberDrone());
                }
            }
        }

        /**
         * Main animation cycle
         */
        const clock = new THREE.Clock();

        function animate() {
            requestAnimationFrame(animate);

            const delta = clock.getDelta();

            if (state.isPlaying) {
                // Update camera orientation by combining device sensors and user pointer corrections
                const targetPitch = state.devicePitch + state.dragOffsetPitch;
                const targetYaw = state.deviceYaw + state.dragOffsetYaw;

                // Smoothly interpolate rotations to eliminate sensor noise & jitter
                camera.rotation.x += (targetPitch - camera.rotation.x) * 0.15;
                camera.rotation.y += (targetYaw - camera.rotation.y) * 0.15;
                camera.rotation.z = 0; // Lock roll to retain level gameplay horizon

                // Handle entities update cycles
                for (let i = enemies.length - 1; i >= 0; i--) {
                    const keep = enemies[i].update(delta);
                    if (!keep) {
                        enemies.splice(i, 1);
                    }
                }

                for (let i = particles.length - 1; i >= 0; i--) {
                    const keep = particles[i].update(delta);
                    if (!keep) {
                        particles.splice(i, 1);
                    }
                }

                for (let i = lasers.length - 1; i >= 0; i--) {
                    const keep = lasers[i].update(delta);
                    if (!keep) {
                        lasers.splice(i, 1);
                    }
                }

                checkWaveProgress();
                drawRadar();
            }

            renderer.render(scene, camera);
        }

        /**
         * Life-cycle State Transitions
         */
        async function startGame() {
            audio.init();
            
            // Set up HUD elements
            state.score = 0;
            state.health = 100;
            state.ammo = state.maxAmmo;
            state.waveCount = 1;
            state.isReloading = false;
            
            updateScore(0);
            adjustShield(0);
            setupAmmoHUD();
            updateAmmoDisplay();

            // Clear legacy entities
            enemies.forEach(e => e.destroyQuietly());
            enemies = [];
            particles = [];
            lasers = [];

            // Spawn first wave
            for(let i = 0; i < 3; i++) {
                enemies.push(new CyberDrone());
            }

            dom.startScreen.classList.add('hidden');
            dom.gameoverScreen.classList.add('hidden');
            
            state.isPlaying = true;
            clock.getElapsedTime(); // Flush clock delta
        }

        function endGame() {
            state.isPlaying = false;
            audio.play('gameover');
            
            dom.finalStats.innerText = `SYS_WAVE: ${state.waveCount} | Score: ${state.score}`;
            dom.gameoverScreen.classList.remove('hidden');
        }

        // Core Initialization Logic flow
        async function initApp() {
            // Check for Mobile parameters
            state.isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
            
            if (!state.isMobile) {
                dom.fallbackStatus.style.display = 'inline-block';
            }

            initThreeJS();
            setupDragInteraction();
            animate();

            dom.btnInit.addEventListener('click', async () => {
                // Force Audio Activation inside interaction Context
                audio.init();

                // 1. Request camera stream
                await initCamera();

                // 2. Request Gyro Sensor Authorization
                if (state.isMobile) {
                    await requestOrientationAccess();
                }

                // Start active loop
                startGame();
            });

            dom.btnRestart.addEventListener('click', () => {
                startGame();
            });

            // Toggle sound settings handler
            dom.settingsTrigger.addEventListener('click', () => {
                state.audioEnabled = !state.audioEnabled;
                if (state.audioEnabled) {
                    dom.settingsTrigger.innerHTML = `
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                            <path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path>
                        </svg>`;
                } else {
                    dom.settingsTrigger.innerHTML = `
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
                            <line x1="23" y1="9" x2="17" y2="15"></line>
                            <line x1="17" y1="9" x2="23" y2="15"></line>
                        </svg>`;
                }
            });

            // Setup automated reloading on long continuous touches (Desktop/Mobile UX)
            let holdTimer;
            dom.gameCanvas.addEventListener('pointerdown', (e) => {
                if (state.ammo === 0) {
                    holdTimer = setTimeout(() => {
                        triggerReload();
                    }, 300);
                }
            });
            dom.gameCanvas.addEventListener('pointerup', () => {
                clearTimeout(holdTimer);
            });
        }

        // Fire initial sequence when document completes loading
        window.addEventListener('DOMContentLoaded', initApp);
    </script>
</body>
</html>