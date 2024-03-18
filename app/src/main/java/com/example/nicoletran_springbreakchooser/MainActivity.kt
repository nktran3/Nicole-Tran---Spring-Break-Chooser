package com.example.nicoletran_springbreakchooser

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.*
import android.widget.*
import android.hardware.*
import android.media.*
import android.net.*
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.*


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

    private val predefinedLocations = mapOf(
        "English" to listOf("geo:51.5074,-0.1278?z=10", "geo:-33.8688,151.2093?z=10"),
        "Spanish" to listOf("geo:21.1619,-86.8515?z=10", "geo:41.3874, 2.1686?z=10", "geo:18.4671,-66.1185?z=10"),
        "French" to listOf("geo:48.8566,2.3522?z=10", "geo:46.2044,6.1432?z=10"),
        "German" to listOf("geo:52.5200,13.4050?z=10", "geo:49.6116,6.1319?z=10"),
        "Vietnamese" to listOf("geo:10.8231,106.6297?z=10", "geo:21.0278,105.8342?z=10"),
        "Italian" to listOf("geo:41.9028,12.4964?z=10", "geo:45.4642,9.1900?z=10")
    )

    private val greetings = mapOf(
        "English" to R.raw.greeting_english,
        "Spanish" to R.raw.greeting_spanish,
        "French" to R.raw.greeting_french,
        "German" to R.raw.greeting_german,
        "Vietnamese" to R.raw.greeting_vietnamese,
        "Italian" to R.raw.greeting_italian
    )

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
            } else if (selectedLanguage =="Italian") {
                languageTag = "it"
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

            if (acceleration > 10) {
                val selectedLanguage = languageSpinner.selectedItem.toString()

                val locations = predefinedLocations[selectedLanguage]
                val randomLocation = locations?.random()
                randomLocation?.let { location ->
                    val gmmIntentUri = Uri.parse(location)
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }

                val greeting =  greetings[selectedLanguage]
                greeting?.let { greetingId ->
                    val mediaPlayer = MediaPlayer.create(this@MainActivity, greetingId)
                    mediaPlayer?.start()
                    mediaPlayer?.setOnCompletionListener {
                        it.release()
                    }
                }

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