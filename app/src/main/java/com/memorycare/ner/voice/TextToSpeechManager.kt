package com.memorycare.ner.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.memorycare.ner.AppLanguage
import java.util.Locale

class TextToSpeechManager private constructor(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null

    @Volatile
    var isInitialized = false
        private set

    @Volatile
    var initStatusMessage = "Initializing TTS..."
        private set

    @Volatile
    var languageStatusMessage = "Checking Language..."
        private set

    var lastSpokenResponse: String = ""
        private set

    private var pendingSpeechText: String? = null
    private var currentLanguage: AppLanguage = AppLanguage.ENGLISH

    init {
        try {
            Log.i(TAG, "Initializing TextToSpeech service...")
            tts = TextToSpeech(appContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error instantiating TextToSpeech", e)
            initStatusMessage = "TTS Initialization Failed: ${e.message}"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            initStatusMessage = "TTS Initialized & Ready"
            Log.i(TAG, "TTS Initialization Successful!")

            // Apply active language locale
            setLanguageLocale(currentLanguage)

            // Speak any pending speech that was queued before init completed
            val pending = pendingSpeechText
            if (!pending.isNullOrBlank()) {
                pendingSpeechText = null
                speak(pending)
            }
        } else {
            isInitialized = false
            initStatusMessage = "TTS Initialization Failed (Status Code $status)"
            Log.e(TAG, "TTS Initialization Failed with status code $status")
        }
    }

    fun setLanguageLocale(lang: AppLanguage) {
        currentLanguage = lang
        val ttsEngine = tts ?: return
        if (!isInitialized) return

        val primaryLocale = when (lang) {
            AppLanguage.ENGLISH -> Locale("en", "IN")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.ASSAMESE -> Locale("as", "IN")
            AppLanguage.BENGALI -> Locale("bn", "IN")
        }

        try {
            var result = ttsEngine.setLanguage(primaryLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback for English to Locale.US
                if (lang == AppLanguage.ENGLISH) {
                    result = ttsEngine.setLanguage(Locale.US)
                }
            }

            languageStatusMessage = when (result) {
                TextToSpeech.LANG_AVAILABLE, TextToSpeech.LANG_COUNTRY_AVAILABLE, TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                    Log.i(TAG, "Language ${lang.name} ($primaryLocale) is available and set.")
                    "Language ${lang.name} Available"
                }
                TextToSpeech.LANG_MISSING_DATA -> {
                    Log.w(TAG, "Language ${lang.name} ($primaryLocale) missing data pack on device.")
                    "Missing Voice Data Pack for ${lang.name}"
                }
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.w(TAG, "Language ${lang.name} ($primaryLocale) not supported on this TTS engine.")
                    "Language ${lang.name} Not Supported on Device Engine"
                }
                else -> {
                    Log.w(TAG, "Language ${lang.name} status: $result")
                    "Language Status Code $result"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting language $lang on TTS engine", e)
            languageStatusMessage = "Error setting language: ${e.message}"
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        lastSpokenResponse = text

        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized yet. Queueing text: \"$text\"")
            pendingSpeechText = text
            return
        }

        try {
            Log.i(TAG, "Speaking text: \"$text\"")
            tts?.stop() // Stop any previous speech to avoid overlapping
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NERMemoryCare_TTS_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.e(TAG, "Error during TTS speak()", e)
        }
    }

    fun stop() {
        if (isInitialized && tts != null) {
            try {
                tts?.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping TTS", e)
            }
        }
    }

    fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            instance = null
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }

    companion object {
        private const val TAG = "NER_TTS_Manager"

        @Volatile
        private var instance: TextToSpeechManager? = null

        fun getInstance(context: Context): TextToSpeechManager {
            return instance ?: synchronized(this) {
                instance ?: TextToSpeechManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
