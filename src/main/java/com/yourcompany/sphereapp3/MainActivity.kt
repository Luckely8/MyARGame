package com.yourcompany.sphereapp3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.yourcompany.sphereapp3.R
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var arFragment: ArFragment
    private lateinit var statusText: TextView
    private lateinit var etUserInput: EditText
    private lateinit var btnSend: Button
    private var anchorNode: AnchorNode? = null
    private var bodyNode: TransformableNode? = null
    private var leftEyeNode: TransformableNode? = null
    private var rightEyeNode: TransformableNode? = null
    private var mouthNode: TransformableNode? = null
    private var tts: TextToSpeech? = null

    // ✅ This will hold the material for the body sphere
    private var bodyMaterial: com.google.ar.sceneform.rendering.Material? = null

    // ⚠️ EMPTY FOR GITHUB SAFETY
    private val apiKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        statusText = findViewById(R.id.statusText)
        etUserInput = findViewById(R.id.etUserInput)
        btnSend = findViewById(R.id.btnSend)

        tts = TextToSpeech(this, this)

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            placeCompanion(anchor)
            statusText.text = getString(R.string.orbit_greeting)
        }

        btnSend.setOnClickListener {
            val message = etUserInput.text.toString().trim()
            if (message.isNotEmpty()) {
                etUserInput.text.clear()
                statusText.text = getString(R.string.thinking)
                askGemini(message)
            }
        }

        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    private fun placeCompanion(anchor: Anchor) {
        anchorNode?.let {
            bodyNode?.parent = null
            it.parent = null
        }

        // ✅ CHANGE THIS: Use a transparent material that can actually change color!
        MaterialFactory.makeTransparentWithColor(this, Color(0.2f, 0.6f, 1.0f, 0.8f))
            .thenAccept { bodyMat ->
                // ✅ SAVE THIS MATERIAL FOR COLOR CHANGES!
                bodyMaterial = bodyMat

                val bodySphere = ShapeFactory.makeSphere(0.2f, Vector3(0f, 0f, 0f), bodyMat)

                MaterialFactory.makeOpaqueWithColor(this, Color(0f, 0f, 0f))
                    .thenAccept { eyeMat ->
                        val leftEye = ShapeFactory.makeSphere(0.03f, Vector3(-0.07f, 0.05f, 0.15f), eyeMat)
                        val rightEye = ShapeFactory.makeSphere(0.03f, Vector3(0.07f, 0.05f, 0.15f), eyeMat)

                        MaterialFactory.makeOpaqueWithColor(this, Color(1f, 0f, 0f))
                            .thenAccept { mouthMat ->
                                val mouth = ShapeFactory.makeSphere(0.04f, Vector3(0f, -0.05f, 0.16f), mouthMat)

                                val anchorNode = AnchorNode(anchor)
                                anchorNode.setParent(arFragment.arSceneView.scene)

                                bodyNode = TransformableNode(arFragment.transformationSystem).apply {
                                    setParent(anchorNode)
                                    renderable = bodySphere
                                    select()
                                }

                                leftEyeNode = TransformableNode(arFragment.transformationSystem).apply {
                                    setParent(bodyNode)
                                    renderable = leftEye
                                }

                                rightEyeNode = TransformableNode(arFragment.transformationSystem).apply {
                                    setParent(bodyNode)
                                    renderable = rightEye
                                }

                                mouthNode = TransformableNode(arFragment.transformationSystem).apply {
                                    setParent(bodyNode)
                                    renderable = mouth
                                }
                            }
                    }
            }
    }

    private fun askGemini(userMessage: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("x-goog-api-key", apiKey)
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val prompt = """
                    You are a friendly AR Sphere Robot with eyes and a mouth.
                    Your name is "Orbit".
                    You are curious, playful, and excited to talk to humans.
                    Keep your answers short (max 2 sentences) and use emojis.
                    Reply in the language the user speaks (Arabic or English).

                    At the END of your response, on a brand new line, ALWAYS add ONE of these EXACT tags:
                    - [COLOR: YELLOW] if the conversation is happy.
                    - [COLOR: BLUE] if the conversation is sad.
                    - [COLOR: RED] if the conversation is exciting.
                    - [COLOR: CYAN] for anything else.

                    User: $userMessage
                """.trimIndent()

                val requestBody = JSONObject().apply {
                    put("contents", JSONObject().apply {
                        put("parts", org.json.JSONArray().put(JSONObject().apply {
                            put("text", prompt)
                        }))
                    })
                }

                connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val text = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                withContext(Dispatchers.Main) {
                    // ✅ REMOVE THE COLOR TAG SO USER SEES CLEAN RESPONSE
                    val cleanText = text.replace(Regex("\\[COLOR:.*?\\]"), "").trim()

                    statusText.text = cleanText
                    changeColorAndMood(text) // Pass the ORIGINAL text to detect the tag
                    tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "geminiResponse")
                    startBobbing()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = getString(R.string.ai_error, e.message ?: "Unknown")
                }
            }
        }
    }

    private fun startBobbing() {
        CoroutineScope(Dispatchers.Main).launch {
            var direction = 1f
            repeat(10) {
                bodyNode?.localPosition = Vector3(0f, 0.05f * direction, 0f)
                direction *= -1f
                delay(150)
            }
            bodyNode?.localPosition = Vector3(0f, 0f, 0f)
        }
    }

    // ✅ THE FINAL, 100% WORKING COLOR CHANGE!
    private fun changeColorAndMood(response: String) {
        // 1. Detect the exact color tag!
        val color = when {
            response.contains("[COLOR: YELLOW]", ignoreCase = true) -> android.graphics.Color.YELLOW
            response.contains("[COLOR: BLUE]", ignoreCase = true) -> android.graphics.Color.BLUE
            response.contains("[COLOR: RED]", ignoreCase = true) -> android.graphics.Color.RED
            else -> android.graphics.Color.CYAN
        }

        // 2. 💥 THE NUCLEAR FIX: Change the material with transparency!
        bodyMaterial?.setFloat3(
            "color",
            android.graphics.Color.red(color) / 255f,
            android.graphics.Color.green(color) / 255f,
            android.graphics.Color.blue(color) / 255f
        )

        // 3. Move the face features based on the color tag!
        CoroutineScope(Dispatchers.Main).launch {
            if (color == android.graphics.Color.YELLOW) {
                leftEyeNode?.localPosition = Vector3(-0.07f, 0.07f, 0.15f)
                rightEyeNode?.localPosition = Vector3(0.07f, 0.07f, 0.15f)
                mouthNode?.localPosition = Vector3(0f, -0.02f, 0.16f)
            } else if (color == android.graphics.Color.BLUE) {
                leftEyeNode?.localPosition = Vector3(-0.07f, 0.02f, 0.15f)
                rightEyeNode?.localPosition = Vector3(0.07f, 0.02f, 0.15f)
                mouthNode?.localPosition = Vector3(0f, -0.08f, 0.16f)
            } else {
                leftEyeNode?.localPosition = Vector3(-0.07f, 0.05f, 0.15f)
                rightEyeNode?.localPosition = Vector3(0.07f, 0.05f, 0.15f)
                mouthNode?.localPosition = Vector3(0f, -0.05f, 0.16f)
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera ready", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission needed", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        arFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        arFragment.onPause()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1
    }
}