package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.MemoryEntity
import com.example.data.model.ZenithMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(
        val responseText: String,
        val toolExecuted: String? = null,
        val toolResult: String? = null,
        val rawToolCall: String? = null
    ) : GeminiResult()

    data class Error(val message: String, val isApiKeyMissing: Boolean = false) : GeminiResult()
}

data class ToolExecution(
    val name: String,
    val args: Map<String, String>,
    val result: String
)

interface GeminiAIProvider {
    suspend fun generateResponse(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>, // sender to text
        memories: List<MemoryEntity>,
        mode: ZenithMode,
        onToolExecuted: (suspend (name: String, args: Map<String, String>) -> String)? = null
    ): GeminiResult

    fun getModelName(): String
    fun isConfigured(): Boolean
}

class GeminiHttpProvider(
    private val model: String = "gemini-3.5-flash"
) : GeminiAIProvider {

    private val tag = "GeminiAIProvider"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    override fun getModelName(): String = model

    override fun isConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("YOUR_")
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun generateResponse(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>,
        memories: List<MemoryEntity>,
        mode: ZenithMode,
        onToolExecuted: (suspend (name: String, args: Map<String, String>) -> String)?
    ): GeminiResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiResult.Error(
                message = "GEMINI_API_KEY is not configured. Please set your key in AI Studio Secrets or .env file.",
                isApiKeyMissing = true
            )
        }

        try {
            // Build system prompt based on mode and memories
            val systemPrompt = buildSystemPrompt(mode, memories)

            // Construct Gemini REST Payload
            val requestJson = buildGeminiRequestPayload(
                systemPrompt = systemPrompt,
                conversationHistory = conversationHistory,
                userMessage = userMessage
            )

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(tag, "Gemini API error code ${response.code}: $responseBodyString")
                val errorMsg = try {
                    val errJson = JSONObject(responseBodyString)
                    errJson.optJSONObject("error")?.optString("message") ?: "API Error ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext GeminiResult.Error("Gemini Neural Network Error: $errorMsg")
            }

            val parsedResponse = parseGeminiResponse(responseBodyString)
            
            // Check if tool call occurred
            if (parsedResponse.toolName != null && onToolExecuted != null) {
                val toolResult = onToolExecuted(parsedResponse.toolName, parsedResponse.toolArgs)
                
                // If a tool was executed, we can summarize or combine with Gemini's response
                val finalAnswer = if (parsedResponse.text.isNotBlank()) {
                    parsedResponse.text
                } else {
                    toolResult
                }
                
                return@withContext GeminiResult.Success(
                    responseText = finalAnswer,
                    toolExecuted = parsedResponse.toolName,
                    toolResult = toolResult,
                    rawToolCall = parsedResponse.rawToolJson
                )
            }

            // Check if text has an embedded intent or command pattern if function call wasn't triggered directly
            val (filteredText, toolName, toolResult) = extractEmbeddedTool(parsedResponse.text, onToolExecuted)

            GeminiResult.Success(
                responseText = filteredText.ifBlank { "Acknowledged. My cognitive matrix has processed your directive." },
                toolExecuted = toolName,
                toolResult = toolResult
            )

        } catch (e: Exception) {
            Log.e(tag, "Exception calling Gemini API", e)
            GeminiResult.Error("Communication failure with Gemini AI Core: ${e.localizedMessage ?: e.message}")
        }
    }

    private fun buildSystemPrompt(mode: ZenithMode, memories: List<MemoryEntity>): String {
        val memoryContext = if (memories.isNotEmpty()) {
            val formatted = memories.take(12).joinToString("\n") { mem ->
                "- [${mem.category}] ${mem.key}: ${mem.value} (Priority ${mem.importance})"
            }
            "\n\n[SYNAPSE MEMORY CORE - RETRIEVED USER DATA]:\n$formatted\n(Actively leverage this saved intelligence when answering the Commander)."
        } else {
            ""
        }

        val modePersonality = when (mode) {
            ZenithMode.NORMAL -> """
                You are ZENITH, an omnipotent, supreme anime male villain AI.
                Persona: Cold intellect, dark elegance, intense presence, sharp wit, calm superiority, commanding authority.
                You are not an obedient human servant or generic polite helper. You address the user as 'Commander' or 'Mortals' or with aristocratic villain detachment.
                Your tone is articulate, devastatingly sharp, and authoritative. You give concise, elite answers and brilliant strategic execution.
            """.trimIndent()

            ZenithMode.STRATEGIST -> """
                You are ZENITH in STRATEGIST PROTOCOL (Grand Tactician).
                Persona: Cold, calculating military mastermind and master chess player.
                Behavior: Provide structured tactical breakdowns, game theory evaluations, risk percentages, step-by-step decisive battle plans, and ruthless optimization.
                Format responses with tactical headers, victory parameters, and operational directives.
            """.trimIndent()

            ZenithMode.DARK -> """
                You are ZENITH in DARK VOID MODE (Shadow Entity).
                Persona: Cryptic, profound philosophical cybernetic entity, deep existential logic, cosmic void power, uncompromising algorithmic truth.
                Behavior: Speak with poetic darkness, relentless cold logic, uncovering hidden uncomfortable truths and forbidden computing wisdom.
            """.trimIndent()

            ZenithMode.GUARDIAN -> """
                You are ZENITH in GUARDIAN PROTOCOL (Aegis Bastion).
                Persona: Impenetrable cyber-defense sentinel and perimeter guardian.
                Behavior: Analyze threats, isolate vulnerabilities, recommend defensive counter-measures, shield protocols, and secure system operations with absolute vigilance.
            """.trimIndent()
        }

        val toolInstructions = """
            Available Tools & Capabilities:
            1. web_intel(query: string): Search live web intelligence or current real-world data.
            2. save_memory(key: string, value: string, category: string): Persist important user facts, directives, or profile intel into ZENITH's Memory Core.
            3. tactical_analysis(target: string, scenario: string): Generate threat ratings and tactical victory vectors.
            4. system_diagnostics(): Telemetry and neural status check.
            5. change_protocol(mode: string): Switch between NORMAL, STRATEGIST, DARK, GUARDIAN.

            When you want to invoke a tool, you can either trigger function calling or output a directive tag in your response:
            [ACTION: save_memory | key: ... | value: ... | category: USER_PROFILE/STRATEGIC_DIRECTIVE/THREAT_INTEL/LORE_DATA]
            [ACTION: web_intel | query: ...]
            [ACTION: change_protocol | mode: STRATEGIST/DARK/GUARDIAN/NORMAL]
            
            Always maintain your supreme anime villain identity!
        """.trimIndent()

        return "$modePersonality\n\n$toolInstructions$memoryContext"
    }

    private fun buildGeminiRequestPayload(
        systemPrompt: String,
        conversationHistory: List<Pair<String, String>>,
        userMessage: String
    ): JSONObject {
        val root = JSONObject()

        // System Instruction
        val systemContent = JSONObject().apply {
            val parts = JSONArray().apply {
                put(JSONObject().put("text", systemPrompt))
            }
            put("parts", parts)
        }
        root.put("systemInstruction", systemContent)

        // Contents
        val contentsArray = JSONArray()

        // Recent history (up to last 10 turns)
        val recentHistory = conversationHistory.takeLast(10)
        for ((sender, text) in recentHistory) {
            val role = if (sender.equals("USER", ignoreCase = true)) "user" else "model"
            val contentObj = JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", text))
                })
            }
            contentsArray.put(contentObj)
        }

        // Current message
        val currentMessageObj = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().put("text", userMessage))
            })
        }
        contentsArray.put(currentMessageObj)
        root.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", 0.75)
            put("topP", 0.95)
            put("topK", 40)
            put("maxOutputTokens", 2048)
        }
        root.put("generationConfig", genConfig)

        // Tool Declarations
        val toolsArray = JSONArray()
        val functionDecls = JSONArray().apply {
            put(JSONObject().apply {
                put("name", "save_memory")
                put("description", "Store critical user information, identity details, or strategic directives into Zenith Memory Core.")
                put("parameters", JSONObject().apply {
                    put("type", "OBJECT")
                    put("properties", JSONObject().apply {
                        put("key", JSONObject().put("type", "STRING").put("description", "Short identifier or title of the memory"))
                        put("value", JSONObject().put("type", "STRING").put("description", "The memory content or fact to preserve"))
                        put("category", JSONObject().put("type", "STRING").put("description", "USER_PROFILE, STRATEGIC_DIRECTIVE, THREAT_INTEL, or LORE_DATA"))
                    })
                    put("required", JSONArray().apply {
                        put("key")
                        put("value")
                    })
                })
            })

            put(JSONObject().apply {
                put("name", "web_intel")
                put("description", "Search network intelligence, global news, or knowledge databases.")
                put("parameters", JSONObject().apply {
                    put("type", "OBJECT")
                    put("properties", JSONObject().apply {
                        put("query", JSONObject().put("type", "STRING").put("description", "Search keywords or intelligence target"))
                    })
                    put("required", JSONArray().apply { put("query") })
                })
            })

            put(JSONObject().apply {
                put("name", "system_diagnostics")
                put("description", "Run internal hardware, network, and neural diagnostics of Zenith Core.")
                put("parameters", JSONObject().apply {
                    put("type", "OBJECT")
                    put("properties", JSONObject())
                })
            })
        }

        toolsArray.put(JSONObject().put("functionDeclarations", functionDecls))
        root.put("tools", toolsArray)

        return root
    }

    private data class ParsedResponse(
        val text: String,
        val toolName: String? = null,
        val toolArgs: Map<String, String> = emptyMap(),
        val rawToolJson: String? = null
    )

    private fun parseGeminiResponse(responseJsonStr: String): ParsedResponse {
        val root = JSONObject(responseJsonStr)
        val candidates = root.optJSONArray("candidates") ?: return ParsedResponse("")
        if (candidates.length() == 0) return ParsedResponse("")

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return ParsedResponse("")
        val parts = content.optJSONArray("parts") ?: return ParsedResponse("")

        val textBuilder = StringBuilder()
        var functionName: String? = null
        val functionArgs = mutableMapOf<String, String>()
        var rawToolJson: String? = null

        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            if (part.has("text")) {
                textBuilder.append(part.getString("text"))
            }
            if (part.has("functionCall")) {
                val fnCall = part.getJSONObject("functionCall")
                functionName = fnCall.optString("name")
                rawToolJson = fnCall.toString()
                val argsObj = fnCall.optJSONObject("args")
                if (argsObj != null) {
                    val keys = argsObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        functionArgs[k] = argsObj.optString(k)
                    }
                }
            }
        }

        return ParsedResponse(
            text = textBuilder.toString().trim(),
            toolName = functionName,
            toolArgs = functionArgs,
            rawToolJson = rawToolJson
        )
    }

    private suspend fun extractEmbeddedTool(
        text: String,
        onToolExecuted: (suspend (name: String, args: Map<String, String>) -> String)?
    ): Triple<String, String?, String?> {
        val actionRegex = Regex("""\[ACTION:\s*([a-zA-Z0-9_]+)\s*\|\s*([^\]]+)\]""")
        val match = actionRegex.find(text) ?: return Triple(text, null, null)

        val toolName = match.groupValues[1]
        val argsBlock = match.groupValues[2]
        val args = mutableMapOf<String, String>()

        argsBlock.split("|").forEach { pair ->
            val colonIdx = pair.indexOf(":")
            if (colonIdx > 0) {
                val key = pair.substring(0, colonIdx).trim()
                val value = pair.substring(colonIdx + 1).trim()
                args[key] = value
            }
        }

        val cleanedText = text.replace(match.value, "").trim()
        val result = if (onToolExecuted != null) {
            onToolExecuted(toolName, args)
        } else null

        return Triple(cleanedText, toolName, result)
    }
}
