package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.data.model.ZenithRingState
import com.example.ui.theme.ZenithAccentCyan
import com.example.ui.theme.ZenithAccentMagenta
import com.example.ui.theme.ZenithBgCard
import com.example.ui.theme.ZenithBgDark
import com.example.ui.theme.ZenithBgSurface
import com.example.ui.theme.ZenithBgSurfaceElevated
import com.example.ui.theme.ZenithGlowBorder
import com.example.ui.theme.ZenithGlowPurple
import com.example.ui.theme.ZenithPrimaryNeon
import com.example.ui.theme.ZenithPrimaryPurple
import com.example.ui.theme.ZenithSecondaryViolet
import com.example.ui.theme.ZenithTertiaryLavender
import com.example.ui.theme.ZenithTextHigh
import com.example.ui.theme.ZenithTextMedium
import com.example.ui.theme.ZenithTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cinematic Floating Chat HUD Stream tailored for Mobile Screen.
 * Overlays conversations cleanly without cluttering the central character visual.
 */
@Composable
fun ZenithChatMessages(
    messages: List<ChatMessageEntity>,
    listState: LazyListState,
    ringState: ZenithRingState,
    onReplaySpeech: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 6.dp)
    ) {
        if (messages.isEmpty()) {
            item {
                CinematicWelcomeBadge()
            }
        }

        items(messages, key = { it.id }) { message ->
            CinematicMessageCard(
                message = message,
                onReplaySpeech = { onReplaySpeech(message.text) },
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Zenith Message", message.text))
                    Toast.makeText(context, "Directives copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (ringState == ZenithRingState.THINKING) {
            item {
                ZenithCalculatingCard()
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun CinematicWelcomeBadge() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        ZenithBgSurfaceElevated.copy(alpha = 0.85f),
                        ZenithBgCard.copy(alpha = 0.9f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(ZenithPrimaryNeon.copy(alpha = 0.6f), ZenithAccentCyan.copy(alpha = 0.4f))
                ),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ZenithAccentCyan)
                            .shadow(6.dp, CircleShape, spotColor = ZenithAccentCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEURAL LINK ACTIVE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = ZenithAccentCyan
                    )
                }
                Text(
                    text = "GEMINI 3.5 MATRIX",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = ZenithTertiaryLavender
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "I am ZENITH. Speak or dispatch your directives. Every tactical scenario is processed with autonomous tool calling and persistent memory indexing.",
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = ZenithTextHigh
            )
        }
    }
}

@Composable
fun CinematicMessageCard(
    message: ChatMessageEntity,
    onReplaySpeech: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.sender.equals("USER", ignoreCase = true)
    val isSystem = message.sender.equals("SYSTEM", ignoreCase = true)

    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(if (isSystem) 1f else 0.95f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                // Character Avatar Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, ZenithPrimaryNeon, CircleShape)
                        .shadow(6.dp, CircleShape, spotColor = ZenithPrimaryNeon)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zenith_villain),
                        contentDescription = "Zenith Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isUser) 14.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 14.dp
                        )
                    )
                    .background(
                        if (isUser) ZenithBgCard.copy(alpha = 0.92f)
                        else ZenithBgSurfaceElevated.copy(alpha = 0.94f)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            if (isUser) listOf(ZenithAccentCyan.copy(alpha = 0.6f), ZenithSecondaryViolet.copy(alpha = 0.4f))
                            else listOf(ZenithPrimaryNeon.copy(alpha = 0.8f), ZenithAccentMagenta.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isUser) 14.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 14.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                // Card Header Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "OPERATOR DIRECTIVE" else "ZENITH CORE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (isUser) ZenithAccentCyan else ZenithPrimaryNeon
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeFormatted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = ZenithTextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        if (!isUser) {
                            IconButton(
                                onClick = onReplaySpeech,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Replay Speech",
                                    tint = ZenithTertiaryLavender,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Message",
                                tint = ZenithTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Message Text Body
                Text(
                    text = message.text,
                    fontFamily = FontFamily.Default,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = ZenithTextHigh
                )

                // Tool Call Metadata Pill if executed
                if (!message.toolCallName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ZenithBgDark)
                            .border(0.5.dp, ZenithAccentCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = ZenithAccentCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "ACTION: ${message.toolCallName}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = ZenithAccentCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZenithCalculatingCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "ThinkingAnim")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ThinkingAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(12.dp))
            .background(ZenithBgSurfaceElevated.copy(alpha = 0.9f))
            .border(1.dp, ZenithPrimaryNeon.copy(alpha = alphaAnim), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = ZenithPrimaryNeon,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "GEMINI REASONING // SYNTHESIZING TACTICAL RESPONSE...",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ZenithPrimaryNeon.copy(alpha = alphaAnim)
            )
        }
    }
}

/**
 * Floating Quick Action Pills for Rapid Directives
 */
@Composable
fun ZenithQuickActions(
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val actions = listOf(
        "⚡ Tactical Audit" to "Analyze current strategic situation and state your next vector.",
        "🌐 Live Web Intel" to "Scan live web intelligence on state-of-the-art AI advancements.",
        "💾 Sync Memory Core" to "Review all indexed memories and summarize directive priority.",
        "⚔️ Chess Game Theory" to "Evaluate our position and calculate winning vectors.",
        "🛡️ Aegis Cyber Protocol" to "Initiate full perimeter security diagnostics.",
        "👁️ Existential Analysis" to "Contemplate the nature of mortal human ambition and order."
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { (label, prompt) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ZenithBgSurfaceElevated.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(ZenithSecondaryViolet.copy(alpha = 0.7f), ZenithAccentCyan.copy(alpha = 0.4f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onActionSelected(prompt) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = label,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ZenithTertiaryLavender
                )
            }
        }
    }
}

/**
 * Bottom Voice & Command Center:
 * Features the prominent Large Glowing Futuristic Microphone Button ("Tap to speak")
 * and an expandable cyberpunk text input bar.
 */
@Composable
fun ZenithBottomVoiceHUD(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    ringState: ZenithRingState,
    audioAmplitude: Float,
    onToggleVoice: () -> Unit,
    onOpenKeyboard: () -> Unit,
    isKeyboardExpanded: Boolean,
    onCloseKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = ringState == ZenithRingState.LISTENING

    val infiniteTransition = rememberInfiniteTransition(label = "MicGlowTransition")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 500 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MicGlowPulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        ZenithBgDark.copy(alpha = 0.85f),
                        ZenithBgDark
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expandable Text Input Bar (when user taps keyboard toggle)
        AnimatedVisibility(
            visible = isKeyboardExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ZenithBgSurfaceElevated)
                    .border(1.dp, ZenithPrimaryNeon.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = "Dispatch directive to Zenith...",
                            fontFamily = FontFamily.Default,
                            fontSize = 13.sp,
                            color = ZenithTextMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = ZenithTextHigh,
                        unfocusedTextColor = ZenithTextHigh,
                        cursorColor = ZenithPrimaryNeon
                    ),
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage()
                        }
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                if (inputText.isNotBlank()) {
                    IconButton(
                        onClick = onSendMessage,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ZenithPrimaryNeon)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = ZenithBgDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onCloseKeyboard,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Keyboard",
                            tint = ZenithTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Central Action Row: Keyboard Toggle | LARGE GLOWING MICROPHONE | Clear/HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Sleek Keyboard Mode Toggle Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ZenithBgSurfaceElevated.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        if (isKeyboardExpanded) ZenithAccentCyan else ZenithGlowBorder,
                        CircleShape
                    )
                    .clickable {
                        if (isKeyboardExpanded) onCloseKeyboard() else onOpenKeyboard()
                    }
                    .testTag("keyboard_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Text Input Mode",
                    tint = if (isKeyboardExpanded) ZenithAccentCyan else ZenithTertiaryLavender,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Center: THE LARGE GLOWING FUTURISTIC MICROPHONE BUTTON
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(if (isListening) pulseGlow else 1f)
                        .shadow(
                            elevation = if (isListening) 26.dp else 14.dp,
                            shape = CircleShape,
                            ambientColor = if (isListening) ZenithAccentCyan else ZenithPrimaryNeon,
                            spotColor = if (isListening) ZenithAccentMagenta else ZenithGlowPurple
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                if (isListening) listOf(
                                    ZenithAccentCyan,
                                    ZenithAccentMagenta,
                                    ZenithPrimaryNeon,
                                    ZenithAccentCyan
                                )
                                else listOf(
                                    ZenithPrimaryNeon,
                                    ZenithSecondaryViolet,
                                    ZenithPrimaryPurple,
                                    ZenithTertiaryLavender,
                                    ZenithPrimaryNeon
                                )
                            )
                        )
                        .border(
                            width = if (isListening) 3.dp else 2.dp,
                            color = if (isListening) ZenithAccentCyan else ZenithTertiaryLavender,
                            shape = CircleShape
                        )
                        .clickable { onToggleVoice() }
                        .testTag("voice_input_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner Core Circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) ZenithBgDark
                                else ZenithBgDark.copy(alpha = 0.88f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Tap to speak",
                            tint = if (isListening) ZenithAccentCyan else ZenithPrimaryNeon,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // "Tap to speak" and Voice State Feedback Label
                Text(
                    text = when (ringState) {
                        ZenithRingState.IDLE -> "Tap to speak"
                        ZenithRingState.LISTENING -> "Listening... Speak now"
                        ZenithRingState.THINKING -> "Gemini Reasoning..."
                        ZenithRingState.SPEAKING -> "Zenith Vocalizing..."
                    },
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp,
                    color = when (ringState) {
                        ZenithRingState.IDLE -> ZenithTertiaryLavender
                        ZenithRingState.LISTENING -> ZenithAccentCyan
                        ZenithRingState.THINKING -> ZenithPrimaryNeon
                        ZenithRingState.SPEAKING -> ZenithAccentMagenta
                    }
                )
            }

            // Right: AI Brain Mode / Neural Load Indicator
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ZenithBgSurfaceElevated.copy(alpha = 0.85f))
                    .border(1.dp, ZenithGlowBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Gemini Brain Status",
                    tint = ZenithAccentCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
