package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ZenithRingState
import com.example.ui.theme.ZenithAccentCyan
import com.example.ui.theme.ZenithAccentMagenta
import com.example.ui.theme.ZenithBgDark
import com.example.ui.theme.ZenithGlowPurple
import com.example.ui.theme.ZenithPrimaryNeon
import com.example.ui.theme.ZenithPrimaryPurple
import com.example.ui.theme.ZenithSecondaryViolet
import com.example.ui.theme.ZenithTertiaryLavender
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Large Futuristic 3D-Animated Purple Energy Ring framing the central ZENITH Anime Male Villain.
 * Dominates the screen as the primary cinematic visual anchor.
 */
@Composable
fun ZenithEnergyRing(
    modifier: Modifier = Modifier,
    ringState: ZenithRingState = ZenithRingState.IDLE,
    audioAmplitude: Float = 0f,
    sizeDp: Dp = 240.dp
) {
    ZenithHeroVillainCore(
        modifier = modifier,
        ringState = ringState,
        audioAmplitude = audioAmplitude,
        sizeDp = sizeDp
    )
}

@Composable
fun ZenithHeroVillainCore(
    modifier: Modifier = Modifier,
    ringState: ZenithRingState = ZenithRingState.IDLE,
    audioAmplitude: Float = 0f,
    sizeDp: Dp = 320.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ZenithHeroRingTransition")

    // Dynamic rotation speeds based on AI state
    val baseRotationSpeed = when (ringState) {
        ZenithRingState.IDLE -> 10000
        ZenithRingState.LISTENING -> 4500
        ZenithRingState.THINKING -> 1200 // Hyper-fast computation spin
        ZenithRingState.SPEAKING -> 3200
    }

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(baseRotationSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HeroRingRotation"
    )

    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween((baseRotationSpeed * 1.6f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HeroCounterRotation"
    )

    val orbitalAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (ringState == ZenithRingState.THINKING) 1500 else 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "HeroOrbitalRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                when (ringState) {
                    ZenithRingState.THINKING -> 500
                    ZenithRingState.LISTENING -> 900
                    ZenithRingState.SPEAKING -> 1200
                    ZenithRingState.IDLE -> 2400
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeroPulseScale"
    )

    // Animated audio amplitude for dynamic mic/speaking waveform
    val animatedAmplitude = remember { Animatable(0f) }
    LaunchedEffect(audioAmplitude) {
        animatedAmplitude.animateTo(
            targetValue = audioAmplitude,
            animationSpec = tween(70)
        )
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Full-Scale 3D Holographic Particle & Plasma Canvas (Back & Foreground)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.minDimension / 2f) - 6.dp.toPx()
            val ampOffset = animatedAmplitude.value * 28.dp.toPx()

            // A. Deep Ambient Energy Cloud Glow
            val glowColor = when (ringState) {
                ZenithRingState.IDLE -> ZenithGlowPurple.copy(alpha = 0.25f)
                ZenithRingState.LISTENING -> ZenithAccentCyan.copy(alpha = 0.40f + (animatedAmplitude.value * 0.35f))
                ZenithRingState.THINKING -> ZenithPrimaryNeon.copy(alpha = 0.55f)
                ZenithRingState.SPEAKING -> ZenithAccentMagenta.copy(alpha = 0.45f)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor,
                        ZenithSecondaryViolet.copy(alpha = 0.22f),
                        ZenithPrimaryPurple.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = outerRadius * 1.35f * pulseScale
                ),
                radius = outerRadius * 1.35f * pulseScale,
                center = centerOffset
            )

            // B. Outermost Tachyon Segmented Track
            rotate(degrees = counterRotationAngle, pivot = centerOffset) {
                drawSegmentedOuterTrack(centerOffset, outerRadius, ringState, ampOffset)
            }

            // C. Main Blazing 3D Plasma Ring
            rotate(degrees = rotationAngle, pivot = centerOffset) {
                drawBlazingPlasmaRing(centerOffset, outerRadius * 0.94f, ringState, pulseScale)
            }

            // D. Concentric Arc Blades
            rotate(degrees = counterRotationAngle * 1.5f, pivot = centerOffset) {
                drawCyberneticArcBlades(centerOffset, outerRadius * 0.86f, ringState)
            }

            // E. Inner Resonant Glyphs & Energy Nodes
            rotate(degrees = rotationAngle * 0.7f, pivot = centerOffset) {
                drawInnerGlyphRing(centerOffset, outerRadius * 0.76f, ringState)
            }

            // F. Multi-Tier Orbiting Plasma Spark Particles
            drawOrbitingPlasmaSparks(centerOffset, outerRadius * 0.94f, orbitalAngle, ringState, animatedAmplitude.value)

            // G. Live Audio Soundwave Pulse Rings (Listening / Speaking)
            if (ringState == ZenithRingState.SPEAKING || ringState == ZenithRingState.LISTENING) {
                val waveColor = if (ringState == ZenithRingState.LISTENING) ZenithAccentCyan else ZenithPrimaryNeon
                val dynamicRadius = (outerRadius * 0.88f) + ampOffset

                drawCircle(
                    color = waveColor.copy(alpha = 0.6f),
                    radius = dynamicRadius,
                    center = centerOffset,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 14f), rotationAngle)
                    )
                )

                drawCircle(
                    color = ZenithTertiaryLavender.copy(alpha = 0.35f),
                    radius = dynamicRadius + 14.dp.toPx(),
                    center = centerOffset,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 24f), counterRotationAngle)
                    )
                )
            }
        }

        // 2. Centerpiece: Powerful Anime Male Villain Character Core
        Box(
            modifier = Modifier
                .size(sizeDp * 0.62f)
                .clip(CircleShape)
                .background(ZenithBgDark)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            ZenithPrimaryNeon,
                            ZenithAccentCyan,
                            ZenithAccentMagenta,
                            ZenithTertiaryLavender,
                            ZenithPrimaryNeon
                        )
                    ),
                    shape = CircleShape
                )
                .shadow(
                    elevation = if (ringState == ZenithRingState.THINKING) 30.dp else 18.dp,
                    shape = CircleShape,
                    ambientColor = ZenithPrimaryNeon,
                    spotColor = ZenithGlowPurple
                ),
            contentAlignment = Alignment.Center
        ) {
            // High-resolution generated anime villain illustration
            Image(
                painter = painterResource(id = R.drawable.img_zenith_villain_full_1787861114936),
                contentDescription = "ZENITH Anime Male Villain",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.18f)
                    .offset(y = (-6).dp)
            )

            // Dynamic Reactive Aura Tint Over Character
            val overlayBrush = when (ringState) {
                ZenithRingState.IDLE -> Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Transparent, ZenithPrimaryPurple.copy(alpha = 0.25f))
                )
                ZenithRingState.LISTENING -> Brush.radialGradient(
                    listOf(ZenithAccentCyan.copy(alpha = 0.15f + animatedAmplitude.value * 0.2f), Color.Transparent)
                )
                ZenithRingState.THINKING -> Brush.radialGradient(
                    listOf(ZenithPrimaryNeon.copy(alpha = 0.30f), ZenithAccentMagenta.copy(alpha = 0.20f), Color.Transparent)
                )
                ZenithRingState.SPEAKING -> Brush.radialGradient(
                    listOf(ZenithTertiaryLavender.copy(alpha = 0.20f), Color.Transparent)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overlayBrush)
            )
        }
    }
}

private fun DrawScope.drawBlazingPlasmaRing(
    center: Offset,
    radius: Float,
    state: ZenithRingState,
    pulse: Float
) {
    val colors = when (state) {
        ZenithRingState.IDLE -> listOf(
            ZenithPrimaryNeon,
            ZenithSecondaryViolet,
            ZenithPrimaryPurple,
            ZenithTertiaryLavender,
            ZenithPrimaryNeon
        )
        ZenithRingState.LISTENING -> listOf(
            ZenithAccentCyan,
            ZenithPrimaryNeon,
            ZenithAccentCyan,
            ZenithTertiaryLavender,
            ZenithAccentCyan
        )
        ZenithRingState.THINKING -> listOf(
            ZenithPrimaryNeon,
            ZenithAccentMagenta,
            ZenithAccentCyan,
            ZenithGlowPurple,
            ZenithPrimaryNeon
        )
        ZenithRingState.SPEAKING -> listOf(
            ZenithPrimaryNeon,
            ZenithTertiaryLavender,
            ZenithAccentMagenta,
            ZenithPrimaryPurple,
            ZenithPrimaryNeon
        )
    }

    // Outer Neon Glow Line
    drawCircle(
        brush = Brush.sweepGradient(colors, center),
        radius = radius * pulse,
        center = center,
        style = Stroke(
            width = if (state == ZenithRingState.THINKING) 6.5.dp.toPx() else 4.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    )

    // Inner Concentric Energy Core
    drawCircle(
        color = (if (state == ZenithRingState.LISTENING) ZenithAccentCyan else ZenithTertiaryLavender).copy(alpha = 0.85f),
        radius = radius * pulse,
        center = center,
        style = Stroke(
            width = 1.2.dp.toPx()
        )
    )
}

private fun DrawScope.drawSegmentedOuterTrack(
    center: Offset,
    radius: Float,
    state: ZenithRingState,
    ampOffset: Float
) {
    val color = when (state) {
        ZenithRingState.IDLE -> ZenithSecondaryViolet.copy(alpha = 0.5f)
        ZenithRingState.LISTENING -> ZenithAccentCyan.copy(alpha = 0.85f)
        ZenithRingState.THINKING -> ZenithPrimaryNeon.copy(alpha = 0.95f)
        ZenithRingState.SPEAKING -> ZenithTertiaryLavender.copy(alpha = 0.75f)
    }

    // Segmented Dashes
    drawCircle(
        color = color,
        radius = radius + ampOffset * 0.4f,
        center = center,
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 18f, 10f, 18f), 0f)
        )
    )

    // 8 Cardinal & Diagonal Cybernetic Markers
    for (i in 0 until 8) {
        val angleRad = (i * 45) * (PI / 180.0)
        val isCardinal = i % 2 == 0
        val tickLen = if (isCardinal) 8.dp.toPx() else 4.dp.toPx()

        val startX = center.x + (radius - tickLen) * cos(angleRad).toFloat()
        val startY = center.y + (radius - tickLen) * sin(angleRad).toFloat()
        val endX = center.x + (radius + tickLen) * cos(angleRad).toFloat()
        val endY = center.y + (radius + tickLen) * sin(angleRad).toFloat()

        drawLine(
            color = if (state == ZenithRingState.THINKING || state == ZenithRingState.LISTENING) ZenithAccentCyan else ZenithPrimaryNeon,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isCardinal) 2.5.dp.toPx() else 1.5.dp.toPx(),
            cap = StrokeCap.Square
        )
    }
}

private fun DrawScope.drawCyberneticArcBlades(
    center: Offset,
    radius: Float,
    state: ZenithRingState
) {
    val arcColor = when (state) {
        ZenithRingState.IDLE -> ZenithGlowPurple.copy(alpha = 0.45f)
        ZenithRingState.LISTENING -> ZenithAccentCyan.copy(alpha = 0.7f)
        ZenithRingState.THINKING -> ZenithAccentMagenta.copy(alpha = 0.9f)
        ZenithRingState.SPEAKING -> ZenithPrimaryNeon.copy(alpha = 0.65f)
    }

    val arcSize = Size(radius * 2, radius * 2)
    val topLeft = Offset(center.x - radius, center.y - radius)

    // Triple Cyber Blades (120 deg apart)
    for (angle in listOf(0f, 120f, 240f)) {
        drawArc(
            color = arcColor,
            startAngle = angle + 15f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawInnerGlyphRing(
    center: Offset,
    radius: Float,
    state: ZenithRingState
) {
    val glyphColor = if (state == ZenithRingState.LISTENING) ZenithAccentCyan.copy(alpha = 0.6f) else ZenithTertiaryLavender.copy(alpha = 0.45f)
    
    // Hexagonal / 12-point inner energy anchors
    for (i in 0 until 12) {
        val angleRad = (i * 30) * (PI / 180.0)
        val px = center.x + radius * cos(angleRad).toFloat()
        val py = center.y + radius * sin(angleRad).toFloat()

        drawCircle(
            color = glyphColor,
            radius = if (i % 3 == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawOrbitingPlasmaSparks(
    center: Offset,
    radius: Float,
    angle: Float,
    state: ZenithRingState,
    amplitude: Float
) {
    val sparkCount = if (state == ZenithRingState.THINKING) 10 else 6
    val sparkColor = when (state) {
        ZenithRingState.IDLE -> ZenithTertiaryLavender
        ZenithRingState.LISTENING -> ZenithAccentCyan
        ZenithRingState.THINKING -> ZenithPrimaryNeon
        ZenithRingState.SPEAKING -> ZenithAccentMagenta
    }

    for (i in 0 until sparkCount) {
        val sparkAngle = angle + (i * (360f / sparkCount))
        val rad = sparkAngle * (PI / 180.0)
        val radialWobble = if (i % 2 == 0) 8.dp.toPx() else -8.dp.toPx()
        val ampWobble = amplitude * 12.dp.toPx()

        val px = center.x + (radius + radialWobble + ampWobble) * cos(rad).toFloat()
        val py = center.y + (radius + radialWobble + ampWobble) * sin(rad).toFloat()

        // Outer glow spark
        drawCircle(
            color = sparkColor.copy(alpha = 0.35f),
            radius = if (state == ZenithRingState.THINKING) 6.dp.toPx() else 4.dp.toPx(),
            center = Offset(px, py)
        )
        // Core spark
        drawCircle(
            color = Color.White,
            radius = if (state == ZenithRingState.THINKING) 2.5.dp.toPx() else 1.8.dp.toPx(),
            center = Offset(px, py)
        )
    }
}
