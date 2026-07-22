package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpeakingId = MutableStateFlow<Long?>(null)
    val currentlySpeakingId: StateFlow<Long?> = _currentlySpeakingId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val bnLocale = Locale.forLanguageTag("bn-BD")
                val result = tts?.setLanguage(bnLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("TtsHelper", "Bangla language not directly supported on this TTS engine, falling back to default locale.")
                    tts?.language = Locale.getDefault()
                }
                isInitialized = true
                setupProgressListener()
            } else {
                Log.e("TtsHelper", "TextToSpeech Initialization failed with status $status")
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _currentlySpeakingId.value = null
            }

            @Suppress("DEPRECATION")
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _currentlySpeakingId.value = null
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                _currentlySpeakingId.value = null
            }
        })
    }

    fun speak(text: String, messageId: Long? = null) {
        if (!isInitialized || text.isBlank()) return
        stop()
        _currentlySpeakingId.value = messageId
        _isSpeaking.value = true

        val hasBengali = text.any { it in '\u0980'..'\u09FF' }
        val targetLocale = if (hasBengali) Locale.forLanguageTag("bn-BD") else Locale.US
        val res = tts?.setLanguage(targetLocale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.ENGLISH)
        }

        val utteranceId = messageId?.toString() ?: "tutor_speech"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _isSpeaking.value = false
        _currentlySpeakingId.value = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
