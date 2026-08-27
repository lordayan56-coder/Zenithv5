package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ZenithRingState
import com.example.ui.ZenithViewModel
import com.example.ui.components.MemoryCoreBottomSheet
import com.example.ui.components.SecurityArchitectureBottomSheet
import com.example.ui.components.SkillsBottomSheet
import com.example.ui.components.ZenithBottomVoiceHUD
import com.example.ui.components.ZenithChatMessages
import com.example.ui.components.ZenithHeroVillainCore
import com.example.ui.components.ZenithQuickActions
import com.example.ui.components.ZenithTopBar
import com.example.ui.theme.ZenithAccentCyan
import com.example.ui.theme.ZenithBgDark
import com.example.ui.theme.ZenithBgSurface
import com.example.ui.theme.ZenithPrimaryNeon
import com.example.ui.theme.ZenithSecondaryViolet
import com.example.ui.theme.ZenithTertiaryLavender
import com.example.ui.theme.ZenithTextMuted
import com.example.ui.theme.ZenithTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ZenithViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenithTheme {
                ZenithMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ZenithMainScreen(viewModel: ZenithViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()

    val chatListState = rememberLazyListState()
    var isKeyboardExpanded by remember { mutableStateOf(false) }

    // Scroll to bottom when messages update
    LaunchedEffect(messages.size, uiState.ringState) {
        if (messages.isNotEmpty()) {
            chatListState.animateScrollToItem(messages.size - 1)
        }
    }

    // Audio recording permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceListening()
        } else {
            Toast.makeText(context, "Microphone permission required for voice directives", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleVoiceClick() {
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasAudioPermission) {
            viewModel.toggleVoiceListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenithBgDark),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ZenithBgDark,
                            Color(0xFF0C0618),
                            ZenithBgDark
                        )
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // 1. Top Sci-Fi HUD Header
                ZenithTopBar(
                    activeMode = uiState.activeMode,
                    ringState = uiState.ringState,
                    cognitiveLoad = uiState.cognitiveLoad,
                    isTtsEnabled = uiState.isTtsEnabled,
                    onModeSelected = { viewModel.setMode(it) },
                    onToggleTts = { viewModel.toggleTts() },
                    onOpenMemoryCore = { viewModel.openMemorySheet(true) },
                    onOpenSkills = { viewModel.openSkillsSheet(true) },
                    onOpenSecurityGuide = { viewModel.openSecuritySheet(true) },
                    onClearChat = { viewModel.clearChat() }
                )

                // 2. Centerpiece: Dominant Anime Male Villain + Large 3D Glowing Purple Energy Ring
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ZenithHeroVillainCore(
                            ringState = uiState.ringState,
                            audioAmplitude = uiState.audioAmplitude,
                            sizeDp = 240.dp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Dynamic Status Subtitle
                        Text(
                            text = when (uiState.ringState) {
                                ZenithRingState.IDLE -> "NEURAL MATRIX READY // PROTOCOL: ${uiState.activeMode.code}"
                                ZenithRingState.LISTENING -> "VOICE INGEST ACTIVE // DECODING DIRECTIVE"
                                ZenithRingState.THINKING -> "GEMINI REASONING // SYNTHESIZING RESPONSE"
                                ZenithRingState.SPEAKING -> "ZENITH VOCAL SYNTHESIS TRANSMITTING"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = when (uiState.ringState) {
                                ZenithRingState.IDLE -> ZenithAccentCyan
                                ZenithRingState.LISTENING -> ZenithAccentCyan
                                ZenithRingState.THINKING -> ZenithPrimaryNeon
                                ZenithRingState.SPEAKING -> ZenithTertiaryLavender
                            }
                        )
                    }
                }

                // 3. Floating Quick Action Directive Pills
                ZenithQuickActions(
                    onActionSelected = { prompt -> viewModel.sendMessage(prompt) }
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 4. Floating HUD Chat Message Stream
                ZenithChatMessages(
                    messages = messages,
                    listState = chatListState,
                    ringState = uiState.ringState,
                    onReplaySpeech = { viewModel.replayMessageSpeech(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // 5. Bottom Voice HUD with Large Glowing Central Microphone Button ("Tap to speak")
                ZenithBottomVoiceHUD(
                    inputText = uiState.inputText,
                    onInputChange = { viewModel.onInputTextChange(it) },
                    onSendMessage = {
                        viewModel.sendMessage()
                        isKeyboardExpanded = false
                    },
                    ringState = uiState.ringState,
                    audioAmplitude = uiState.audioAmplitude,
                    onToggleVoice = { handleVoiceClick() },
                    onOpenKeyboard = { isKeyboardExpanded = true },
                    isKeyboardExpanded = isKeyboardExpanded,
                    onCloseKeyboard = { isKeyboardExpanded = false }
                )
            }

            // Bottom Sheets
            if (uiState.isMemorySheetOpen) {
                MemoryCoreBottomSheet(
                    memories = memories,
                    onSaveMemory = { key, value, category, importance ->
                        viewModel.saveMemory(key, value, category, importance)
                    },
                    onDeleteMemory = { viewModel.deleteMemory(it) },
                    onClearAllMemories = { viewModel.clearAllMemories() },
                    onDismiss = { viewModel.openMemorySheet(false) }
                )
            }

            if (uiState.isSkillsSheetOpen) {
                SkillsBottomSheet(
                    onTriggerSkill = { skillName ->
                        viewModel.openSkillsSheet(false)
                        viewModel.sendMessage("Execute $skillName protocol.")
                    },
                    onDismiss = { viewModel.openSkillsSheet(false) }
                )
            }

            if (uiState.isSecuritySheetOpen) {
                SecurityArchitectureBottomSheet(
                    onDismiss = { viewModel.openSecuritySheet(false) }
                )
            }
        }
    }
}
