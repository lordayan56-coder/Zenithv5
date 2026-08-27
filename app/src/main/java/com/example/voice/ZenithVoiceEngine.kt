package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.ZenithRingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class ZenithVoiceEngine(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onStateChanged: (ZenithRingState) -> Unit
) : TextToSpeech.OnInitListener {

    private val tag = "ZenithVoiceEngine"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(tag, "STT Ready for speech")
                        _isListening.value = true
                        onStateChanged(ZenithRingState.LISTENING)
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(tag, "STT Beginning of speech")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS (usually -2 to 10 dB) to 0.0 .. 1.0
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioRms.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d(tag, "STT End of speech")
                        _isListening.value = false
                        _audioRms.value = 0f
                    }

                    override fun onError(error: Int) {
                        Log.w(tag, "STT Error code: $error")
                        _isListening.value = false
                        _audioRms.value = 0f
                        onStateChanged(ZenithRingState.IDLE)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioRms.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            onSpeechRecognized(text)
                        } else {
                            onStateChanged(ZenithRingState.IDLE)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            // Can update live preview if desired
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                // Customize voice: Pitch 0.78f (deep, commanding anime villain resonance), Speech rate 0.94f
                textToSpeech?.setPitch(0.78f)
                textToSpeech?.setSpeechRate(0.94f)
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        onStateChanged(ZenithRingState.SPEAKING)
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        onStateChanged(ZenithRingState.IDLE)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        onStateChanged(ZenithRingState.IDLE)
                    }
                })
            }
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }
        stopSpeaking()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start listening", e)
            _isListening.value = false
            onStateChanged(ZenithRingState.IDLE)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(tag, "Failed to stop listening", e)
        }
        _isListening.value = false
        _audioRms.value = 0f
    }

    fun speak(text: String, enabled: Boolean = true) {
        if (!enabled || !isTtsReady || text.isBlank()) return

        // Clean out formatting brackets/action tags before speaking
        val cleanText = text
            .replace(Regex("""\[ACTION:[^\]]+\]"""), "")
            .replace(Regex("""[*#_`~]"""), "")
            .trim()

        if (cleanText.isBlank()) return

        stopSpeaking()
        val utteranceId = "ZENITH_VOICE_${System.currentTimeMillis()}"
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        if (_isSpeaking.value) {
            textToSpeech?.stop()
            _isSpeaking.value = false
            onStateChanged(ZenithRingState.IDLE)
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(tag, "Error during cleanup", e)
        }
    }
}
