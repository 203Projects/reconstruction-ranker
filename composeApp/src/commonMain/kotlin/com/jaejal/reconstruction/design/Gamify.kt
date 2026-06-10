package com.jaejal.reconstruction.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Gamification components for the 분담금 quest: the live "building grows" facade, the 교환비
 * needle meter, the reveal count-up number, and the refund confetti burst.
 *
 * All hand-coded, KMP-common, no new dependency.
 */

// 교환비(비례율) → 0..1 fraction, shared by the facade lighting AND the meter so they always
// agree. The window must cover the FULL range the sliders can drive the ratio to: with three
// independent 0.6..1.4 factors the engine reaches ~1.37 at one district and dips below 0.55 at
// another, so a narrow 0.55..1.05 window froze (clamped) the visual at the extremes. 0.40..1.45
// keeps the building + meter responsive across the entire slider space.
private const val RATIO_MIN = 0.40
private const val RATIO_MAX = 1.45

/** Map a 교환비 ratio to a 0..1 fill/lit fraction over the full slider-reachable range. */
fun litFractionFor(ratio: Double): Float =
    (((ratio - RATIO_MIN) / (RATIO_MAX - RATIO_MIN)).coerceIn(0.0, 1.0)).toFloat()

// ---------------------------------------------------------------------------
// Live "building grows" facade
// ---------------------------------------------------------------------------

/**
 * The district building, drawn live. Windows light up bottom-to-top in proportion to
 * [litFraction] (0..1, derived from the engine's 교환비). [isRefund] tints lit windows emerald
 * (a refund) vs gold (you pay) — so the building's color FEELS like the outcome before the
 * number is even revealed.
 */
@Composable
fun BuildingFacade(
    building: Building,
    litFraction: Float,
    isRefund: Boolean,
    modifier: Modifier = Modifier
) {
    val lit by animateFloatAsState(
        targetValue = litFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "litFraction"
    )
    val litColor = if (isRefund) ConstructionColors.Gain else ConstructionColors.Gold
    val sprite = building.sprite
    // Which windows are lit at the current fraction — computed once per `lit` step (windows
    // light bottom-to-top), so the per-cell Canvas callback is just an O(1) set lookup instead
    // of a height calc on all ~300 cells every frame.
    val litWindows = remember(lit, building) {
        building.windowIndexes.filterTo(HashSet()) { idx ->
            val y = sprite.cells[idx].y
            (1f - y.toFloat() / sprite.rows) <= lit
        }
    }
    PixelCanvas(sprite, modifier) { idx, cell ->
        when {
            idx !in building.windowIndexes -> cell.color
            idx in litWindows -> litColor
            else -> ConstructionColors.Paper
        }
    }
}

// ---------------------------------------------------------------------------
// 교환비 (proportion ratio) needle meter
// ---------------------------------------------------------------------------

/**
 * A horizontal meter for 교환비 (비례율). 100% (본전, break-even) is marked; the fill animates
 * as the sliders move. Above 100% reads gold (good), below reads muted.
 */
@Composable
fun ExchangeMeter(
    ratio: Double,
    modifier: Modifier = Modifier
) {
    // Same mapping as the building facade so the meter and the lit windows always agree.
    val frac by animateFloatAsState(
        targetValue = litFractionFor(ratio),
        animationSpec = tween(160, easing = FastOutSlowInEasing),
        label = "exchangeFrac"
    )
    val good = ratio >= 1.0
    Box(modifier.fillMaxWidth()) {
        // track
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(ConstructionColors.Hairline, RoundedCornerShape(Design.radii.pill))
        )
        // fill
        Box(
            Modifier
                .fillMaxWidth(frac)
                .height(10.dp)
                .background(
                    if (good) ConstructionColors.Gold else ConstructionColors.InkSoft,
                    RoundedCornerShape(Design.radii.pill)
                )
        )
    }
}

// ---------------------------------------------------------------------------
// Reveal count-up number
// ---------------------------------------------------------------------------

/**
 * Animates from 0 up to [targetMillionWon], formatting each frame with [format]. This is the
 * 분담금 reveal centerpiece — WEIGHT tier: decelerate INTO the number, never bounce. The
 * duration is magnitude-scaled (bigger numbers take longer) and clamped to [800, 1500]ms.
 *
 * A new composable (NOT a tweak to HeroNumber, which is used for instant values).
 *
 * @param key change to re-trigger the count-up (e.g. a reveal counter).
 * @param animate when false, jumps straight to the final value.
 */
@Composable
fun CountUpNumber(
    targetMillionWon: Double,
    format: (Double) -> String,
    tone: ChipTone,
    modifier: Modifier = Modifier,
    key: Any = targetMillionWon,
    animate: Boolean = isAnimationEnabled
) {
    val fg = when (tone) {
        ChipTone.Gain -> ConstructionColors.Gain
        ChipTone.Loss -> ConstructionColors.Loss
        ChipTone.Gold -> ConstructionColors.Gold
        else -> ConstructionColors.Ink
    }
    val display = remember { Animatable(if (animate) 0f else targetMillionWon.toFloat()) }
    LaunchedEffect(key) {
        if (!animate) { display.snapTo(targetMillionWon.toFloat()); return@LaunchedEffect }
        display.snapTo(0f)
        // magnitude-scaled duration: ~0.15ms per 만원, clamped 800..1500
        val mag = abs(targetMillionWon)
        val dur = (mag * 0.15).toInt().coerceIn(800, 1500)
        display.animateTo(
            targetMillionWon.toFloat(),
            animationSpec = tween(durationMillis = dur, easing = FastOutSlowInEasing)
        )
    }
    Text(
        format(display.value.toDouble()),
        style = MaterialTheme.typography.displayLarge,
        color = fg,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

// ---------------------------------------------------------------------------
// Confetti (refund celebration only)
// ---------------------------------------------------------------------------

private data class Confetto(val x0: Float, val drift: Float, val rot: Float, val color: Color, val size: Float)

/**
 * A one-shot confetti burst — 40 pixel squares falling with drift, fading out. Driven by a
 * SINGLE [Animatable] time value `t` (0..1), not 40 separate animations. Renders nothing when
 * [play] is false or animations are disabled.
 *
 * Use as an overlay on the refund reveal only.
 */
@Composable
fun ConfettiBurst(
    play: Boolean,
    modifier: Modifier = Modifier,
    animate: Boolean = isAnimationEnabled
) {
    if (!play || !animate) return
    val palette = listOf(
        ConstructionColors.Gold, ConstructionColors.Gain, ConstructionColors.Copper,
        ConstructionColors.SkySoft, ConstructionColors.Cloud
    )
    // 40 seeded particles — deterministic pseudo-random from the index (no Math.random,
    // which is unavailable). Extract three values from DISJOINT bit-fields of the hash so each
    // spans the full [0,1) — decimal-digit slicing of a 24-bit value collapsed the high field
    // to [0,0.016], making every particle the same size. Re-seeded when `play` flips.
    val particles = remember(play) {
        (0 until 40).map { i ->
            val s = (i * 2654435761u.toLong()) and 0xFFFFFF      // 24-bit cheap hash
            val r1 = (s and 0xFF) / 256f                         // low byte
            val r2 = ((s shr 8) and 0xFF) / 256f                 // mid byte
            val r3 = ((s shr 16) and 0xFF) / 256f                // high byte
            Confetto(
                x0 = r1,
                drift = (r2 - 0.5f) * 0.4f,
                rot = r3 * 360f,
                color = palette[i % palette.size],
                size = 4f + r3 * 4f
            )
        }
    }
    val t = remember { Animatable(0f) }
    LaunchedEffect(play) {
        t.snapTo(0f)
        t.animateTo(1f, animationSpec = tween(1400, easing = LinearEasing))
    }
    Canvas(modifier.fillMaxSize()) {
        val tv = t.value
        for (p in particles) {
            val x = (p.x0 + p.drift * tv) * size.width
            val y = (tv * tv) * size.height            // accelerating fall
            val alpha = (1f - tv * tv).coerceIn(0f, 1f)
            drawRect(
                color = p.color.copy(alpha = alpha),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(p.size, p.size)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Progress header (the rising building = the progress bar)
// ---------------------------------------------------------------------------

/** A compact step indicator for the quest. [step] is 1-based; [total] is the count. */
@Composable
fun QuestProgress(
    step: Int,
    total: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.xs)
    ) {
        androidx.compose.foundation.layout.Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(ConstructionColors.Hairline, RoundedCornerShape(Design.radii.pill))
            )
            Box(
                Modifier
                    .fillMaxWidth((step.toFloat() / total).coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(ConstructionColors.Gold, RoundedCornerShape(Design.radii.pill))
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = ConstructionColors.InkSoft,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
