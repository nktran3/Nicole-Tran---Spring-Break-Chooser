package com.example.nicoletran_springbreakchooser

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.*
import android.widget.*
import java.util.*
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : AppCompatActivity() {
    private lateinit var editTextPhrase: EditText
    private lateinit var languageSpinner: Spinner
    private lateinit var submitButton: Button

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
    }

}