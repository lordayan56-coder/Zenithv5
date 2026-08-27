package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ZenithMode
import com.example.data.model.ZenithRingState
import com.example.ui.theme.ZenithAccentCyan
import com.example.ui.theme.ZenithAccentMagenta
import com.example.ui.theme.ZenithBgCard
import com.example.ui.theme.ZenithBgDark
import com.example.ui.theme.ZenithBgSurface
import com.example.ui.theme.ZenithBgSurfaceElevated
import com.example.ui.theme.ZenithGlowBorder
import com.example.ui.theme.ZenithPrimaryNeon
import com.example.ui.theme.ZenithSecondaryViolet
import com.example.ui.theme.ZenithTertiaryLavender
import com.example.ui.theme.ZenithTextHigh
import com.example.ui.theme.ZenithTextMedium
import com.example.ui.theme.ZenithTextMuted
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ZenithTopBar(
    activeMode: ZenithMode,
    ringState: ZenithRingState,
    cognitiveLoad: Int,
    isTtsEnabled: Boolean,
    onModeSelected: (ZenithMode) -> Unit,
    onToggleTts: () -> Unit,
    onOpenMemoryCore: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenSecurityGuide: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            val now = Date()
            val timeStr = dateFormat.format(now)
            val millis = (now.time % 1000) / 10
            currentTimeString = String.format("%s.%02d", timeStr, millis)
            delay(100)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ZenithBgDark.copy(alpha = 0.95f),
                        ZenithBgSurface.copy(alpha = 0.85f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Row 1: Brand + Status + Core Action Triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ZENITH",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 2.sp,
                        color = ZenithPrimaryNeon
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ZenithBgCard)
                            .border(0.8.dp, ZenithAccentCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "GEMINI 3.5",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZenithAccentCyan
                        )
                    }
                }
                Text(
                    text = "$currentTimeString UTC // ONLINE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = ZenithTextMuted
                )
            }

            // Quick Sci-Fi Action Triggers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Voice Toggle
                IconButton(
                    onClick = onToggleTts,
                    modifier = Modifier.size(34.dp).testTag("tts_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isTtsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Voice Synthesis",
                        tint = if (isTtsEnabled) ZenithAccentCyan else ZenithTextMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Memory Core
                IconButton(
                    onClick = onOpenMemoryCore,
                    modifier = Modifier.size(34.dp).testTag("memory_core_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Memory Core",
                        tint = ZenithPrimaryNeon,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Skills Matrix
                IconButton(
                    onClick = onOpenSkills,
                    modifier = Modifier.size(34.dp).testTag("skills_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Skills Matrix",
                        tint = ZenithTertiaryLavender,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Security Arch
                IconButton(
                    onClick = onOpenSecurityGuide,
                    modifier = Modifier.size(34.dp).testTag("security_guide_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Architecture",
                        tint = ZenithAccentMagenta,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Clear Chat
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier.size(34.dp).testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Session",
                        tint = ZenithTextMuted,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2: Personality Modes Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ZenithMode.values().forEach { mode ->
                val isSelected = mode == activeMode
                val chipBorderColor by animateColorAsState(
                    targetValue = if (isSelected) ZenithPrimaryNeon else ZenithGlowBorder.copy(alpha = 0.4f),
                    label = "ChipBorder"
                )
                val chipBgColor by animateColorAsState(
                    targetValue = if (isSelected) ZenithPrimaryNeon.copy(alpha = 0.25f) else ZenithBgSurfaceElevated.copy(alpha = 0.7f),
                    label = "ChipBg"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(chipBgColor)
                        .border(1.dp, chipBorderColor, RoundedCornerShape(8.dp))
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                        .testTag("mode_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = mode.title.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp,
                        color = if (isSelected) ZenithTextHigh else ZenithTextMedium
                    )
                }
            }
        }
    }
}
