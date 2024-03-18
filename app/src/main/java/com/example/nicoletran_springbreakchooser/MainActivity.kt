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

// citation: https://developers.google.com/maps/documentation/urls/android-intents#kotlin
// citation: https://www.geeksforgeeks.org/how-to-detect-shake-event-in-android/
// citation: https://www.geeksforgeeks.org/speech-to-text-application-in-android-with-kotlin/
// citation: https://www.geeksforgeeks.org/how-to-add-audio-files-to-android-app-in-android-studio/
// citation: ChatGPT

class MainActivity : AppCompatActivity() {

    // declare variables
    private lateinit var editTextPhrase: EditText
    private lateinit var languageSpinner: Spinner
    private lateinit var submitButton: Button
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    // speech input handling
    private val speechInputLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            editTextPhrase.setText(data?.get(0))
        }
    }

    // map languages to locations
    private val predefinedLocations = mapOf(
        "English" to listOf("geo:51.5074,-0.1278?z=10", "geo:-33.8688,151.2093?z=10"), // London, England; Sydney,Australia
        "Spanish" to listOf("geo:21.1619,-86.8515?z=10", "geo:41.3874, 2.1686?z=10", "geo:18.4671,-66.1185?z=10"), // Cancun, Mexico; Barcelona, Spain; San Juan, Puerto Rico
        "French" to listOf("geo:48.8566,2.3522?z=10", "geo:46.2044,6.1432?z=10"), // Paris, France; Geneva, Switzerland
        "German" to listOf("geo:52.5200,13.4050?z=10", "geo:49.6116,6.1319?z=10"), // Berlin, Germany; Luxembourg, Luxembourg
        "Vietnamese" to listOf("geo:10.8231,106.6297?z=10", "geo:21.0278,105.8342?z=10"), // Ho Chi Minh City, Vietnam; Hanoi, Vietnam
        "Italian" to listOf("geo:41.9028,12.4964?z=10", "geo:45.4642,9.1900?z=10") // Rome, Italy; Milan, Italy
    )
    // map languages to greetings
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

        // setOnClickListener to start speech recognition
        submitButton.setOnClickListener{
            // get selected language and language tag
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

            // speech recognition intent
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
        // getting the Sensor Manager instance
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
            // fetch x,y,z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            // get current accelerations with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            // acceleration value is over 10 launch google maps intent and play greeting
            if (acceleration > 10) {
                val selectedLanguage = languageSpinner.selectedItem.toString()

                // map
                val locations = predefinedLocations[selectedLanguage]
                val randomLocation = locations?.random()
                randomLocation?.let { location ->
                    val gmmIntentUri = Uri.parse(location)
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }

                // greeting
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