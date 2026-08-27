package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAIProvider
import com.example.ai.GeminiHttpProvider
import com.example.ai.GeminiResult
import com.example.ai.WebIntelHelper
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.ZenithDatabase
import com.example.data.model.MemoryCategory
import com.example.data.model.ZenithMode
import com.example.data.model.ZenithRingState
import com.example.data.repository.ZenithRepository
import com.example.voice.ZenithVoiceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ZenithUiState(
    val activeMode: ZenithMode = ZenithMode.NORMAL,
    val ringState: ZenithRingState = ZenithRingState.IDLE,
    val audioAmplitude: Float = 0f,
    val inputText: String = "",
    val isTtsEnabled: Boolean = true,
    val isMemorySheetOpen: Boolean = false,
    val isSkillsSheetOpen: Boolean = false,
    val isSecuritySheetOpen: Boolean = false,
    val cognitiveLoad: Int = 42,
    val errorMessage: String? = null
)

class ZenithViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ZenithDatabase.getInstance(application)
    private val repository = ZenithRepository(database.zenithDao())
    private val aiProvider: GeminiAIProvider = GeminiHttpProvider(model = "gemini-3.5-flash")

    private val _uiState = MutableStateFlow(ZenithUiState())
    val uiState: StateFlow<ZenithUiState> = _uiState.asStateFlow()

    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var voiceEngine: ZenithVoiceEngine? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDefaultDirectivesIfEmpty()
        }

        // Initialize Voice Engine
        voiceEngine = ZenithVoiceEngine(
            context = application,
            onSpeechRecognized = { recognizedText ->
                if (recognizedText.isNotBlank()) {
                    sendMessage(recognizedText)
                }
            },
            onStateChanged = { newState ->
                _uiState.value = _uiState.value.copy(ringState = newState)
            }
        )

        // Observe voice amplitude
        viewModelScope.launch {
            voiceEngine?.audioRms?.collect { rms ->
                _uiState.value = _uiState.value.copy(audioAmplitude = rms)
            }
        }
    }

    fun onInputTextChange(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun setMode(mode: ZenithMode) {
        _uiState.value = _uiState.value.copy(activeMode = mode)
    }

    fun toggleTts() {
        val next = !_uiState.value.isTtsEnabled
        _uiState.value = _uiState.value.copy(isTtsEnabled = next)
        if (!next) {
            voiceEngine?.stopSpeaking()
        }
    }

    fun toggleVoiceListening() {
        val currentlyListening = _uiState.value.ringState == ZenithRingState.LISTENING
        if (currentlyListening) {
            voiceEngine?.stopListening()
            _uiState.value = _uiState.value.copy(ringState = ZenithRingState.IDLE)
        } else {
            voiceEngine?.startListening()
        }
    }

    fun openMemorySheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(isMemorySheetOpen = open)
    }

    fun openSkillsSheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSkillsSheetOpen = open)
    }

    fun openSecuritySheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSecuritySheetOpen = open)
    }

    fun saveMemory(key: String, value: String, category: MemoryCategory, importance: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMemory(key, value, category, importance)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearMemories()
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChat()
        }
    }

    fun replayMessageSpeech(text: String) {
        voiceEngine?.speak(text, enabled = _uiState.value.isTtsEnabled)
    }

    fun sendMessage(userText: String = _uiState.value.inputText) {
        val prompt = userText.trim()
        if (prompt.isBlank()) return

        _uiState.value = _uiState.value.copy(
            inputText = "",
            ringState = ZenithRingState.THINKING,
            cognitiveLoad = (70..98).random()
        )

        viewModelScope.launch {
            // 1. Save user message to Room DB
            withContext(Dispatchers.IO) {
                repository.insertChatMessage(
                    ChatMessageEntity(
                        sender = "USER",
                        text = prompt,
                        mode = _uiState.value.activeMode.name
                    )
                )
            }

            // 2. Retrieve relevant saved memories
            val currentMemories = withContext(Dispatchers.IO) { repository.getAllMemories() }
            val history = messages.value.takeLast(8).map { it.sender to it.text }

            // 3. Send message to Gemini via GeminiAIProvider with tool handler
            val result = aiProvider.generateResponse(
                userMessage = prompt,
                conversationHistory = history,
                memories = currentMemories,
                mode = _uiState.value.activeMode,
                onToolExecuted = { toolName, args ->
                    handleToolExecution(toolName, args)
                }
            )

            when (result) {
                is GeminiResult.Success -> {
                    // Check if prompt suggests saving memory automatically
                    autoExtractMemoryIfApplicable(prompt, result.responseText)

                    // 4. Insert Zenith's response to Room
                    val entity = ChatMessageEntity(
                        sender = "ZENITH",
                        text = result.responseText,
                        mode = _uiState.value.activeMode.name,
                        status = "COMPLETED",
                        toolCallName = result.toolExecuted,
                        toolCallResult = result.toolResult
                    )
                    withContext(Dispatchers.IO) {
                        repository.insertChatMessage(entity)
                    }

                    // 5. Speak response via Anime villain TTS
                    if (_uiState.value.isTtsEnabled) {
                        voiceEngine?.speak(result.responseText, enabled = true)
                    } else {
                        _uiState.value = _uiState.value.copy(ringState = ZenithRingState.IDLE)
                    }
                }

                is GeminiResult.Error -> {
                    val fallbackVillainText = if (result.isApiKeyMissing) {
                        "Commander, my Gemini AI neural link requires authentication. Configure GEMINI_API_KEY in your AI Studio Secrets panel or .env file to awaken my full consciousness."
                    } else {
                        "Neural interference encountered: ${result.message}. My internal core remains vigilant."
                    }

                    withContext(Dispatchers.IO) {
                        repository.insertChatMessage(
                            ChatMessageEntity(
                                sender = "ZENITH",
                                text = fallbackVillainText,
                                mode = _uiState.value.activeMode.name,
                                status = "ERROR"
                            )
                        )
                    }

                    if (_uiState.value.isTtsEnabled) {
                        voiceEngine?.speak(fallbackVillainText, enabled = true)
                    } else {
                        _uiState.value = _uiState.value.copy(ringState = ZenithRingState.IDLE)
                    }
                }
            }

            _uiState.value = _uiState.value.copy(cognitiveLoad = (30..50).random())
        }
    }

    private suspend fun handleToolExecution(name: String, args: Map<String, String>): String {
        return when (name) {
            "save_memory" -> {
                val key = args["key"] ?: "Intel Note"
                val value = args["value"] ?: args.values.joinToString(" ")
                val categoryName = args["category"] ?: MemoryCategory.USER_PROFILE.name
                val category = try {
                    MemoryCategory.valueOf(categoryName)
                } catch (e: Exception) {
                    MemoryCategory.USER_PROFILE
                }
                repository.saveMemory(key, value, category, importance = 4)
                "SYNAPSE COMMITTED: [$key] indexed to Zenith Memory Core."
            }

            "web_intel" -> {
                val query = args["query"] ?: args["target"] ?: "quantum AI"
                WebIntelHelper.searchWebIntel(query)
            }

            "system_diagnostics" -> {
                "DIAGNOSTICS: Neural Synapse Integrity 100% | Quantum Encryption Active | Shield Protocols Optimal | Gemini 3.5 Flash Synchronized."
            }

            "change_protocol" -> {
                val modeStr = args["mode"] ?: ""
                val targetMode = ZenithMode.values().firstOrNull { it.name.equals(modeStr, ignoreCase = true) }
                if (targetMode != null) {
                    _uiState.value = _uiState.value.copy(activeMode = targetMode)
                    "PROTOCOL SHIFTED: Now operating in ${targetMode.title} mode."
                } else {
                    "PROTOCOL UNCHANGED: Specified mode not recognized."
                }
            }

            else -> {
                "TOOL EXECUTED: $name processed with parameter set."
            }
        }
    }

    private suspend fun autoExtractMemoryIfApplicable(userPrompt: String, modelResponse: String) {
        val lower = userPrompt.lowercase()
        if (lower.startsWith("remember that") || lower.startsWith("my name is") || lower.startsWith("i am ") || lower.contains("my favorite") || lower.contains("my goal is")) {
            val key = when {
                lower.startsWith("my name is") -> "Master Identity"
                lower.startsWith("my goal is") -> "Strategic Objective"
                lower.contains("my favorite") -> "User Preference"
                else -> "Direct Intelligence"
            }
            repository.saveMemory(key, userPrompt.removePrefix("remember that").trim(), MemoryCategory.USER_PROFILE, 4)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine?.destroy()
    }
}
