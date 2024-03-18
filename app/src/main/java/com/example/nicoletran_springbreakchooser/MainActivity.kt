package com.example.nicoletran_springbreakchooser

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.*
import android.widget.*
import android.hardware.*
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {
    private lateinit var editTextPhrase: EditText
    private lateinit var languageSpinner: Spinner
    private lateinit var submitButton: Button
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private val speechInputLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            editTextPhrase.setText(data?.get(0))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editTextPhrase = findViewById(R.id.editTextPhrase)
        languageSpinner = findViewById(R.id.languageSpinner)
        submitButton = findViewById(R.id.submit_button)

        submitButton.setOnClickListener{
            val selectedLanguage = languageSpinner.selectedItem.toString()
            val languageTag: String
            if (selectedLanguage== "English") {
                languageTag = "en"
            } else if (selectedLanguage == "Spanish") {
                languageTag = "es"
            }else if (selectedLanguage == "French") {
                languageTag = "fr"
            } else if (selectedLanguage == "German") {
                languageTag = "de"
            } else if (selectedLanguage =="Vietnamese") {
                languageTag = "vi"
            } else {
                languageTag = Locale.getDefault().toLanguageTag()
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt))

            try {
                speechInputLauncher.launch(intent)
            } catch (e: Exception) {
                Toast
                    .makeText(
                        this,
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 8) {
                Toast.makeText(this@MainActivity, "Shake event detected", Toast.LENGTH_SHORT).show()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }
}