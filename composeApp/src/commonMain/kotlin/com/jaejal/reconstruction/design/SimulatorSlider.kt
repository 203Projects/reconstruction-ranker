package com.jaejal.reconstruction.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PeerMarker(val label: String, val value: Double)

/**
 * Refined slider with:
 *  - Material 3 base + tighter thumb/track styling
 *  - Animated thumb size on press
 *  - Peer-district markers as small chips along a secondary rail below the track
 */
@Composable
fun ConstructionSlider(
    title: String,
    sub: String,
    value: Double,
    valueDisplay: String,
    onValueChange: (Double) -> Unit,
    range: ClosedRange<Double>,
    markers: List<PeerMarker>,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    // Compact mode tightens vertical rhythm so the Q2 panel (4 sliders) fits a phone
    // viewport without an internal scroll. No information is removed — only spacing.
    val outerPad = if (compact) Design.spacing.xs else Design.spacing.sm
    val railMin = if (compact) 16.dp else 22.dp
    Column(modifier.fillMaxWidth().padding(vertical = outerPad)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ConstructionColors.Ink
                )
                Text(
                    sub,
                    style = MaterialTheme.typography.labelMedium,
                    color = ConstructionColors.InkMuted
                )
            }
            // value pill
            Box(
                Modifier
                    .background(ConstructionColors.NavyTint, RoundedCornerShape(Design.radii.pill))
                    .padding(horizontal = Design.spacing.md, vertical = 4.dp)
            ) {
                Text(
                    valueDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    color = ConstructionColors.NavyDeep,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (!compact) VSpace(Design.spacing.xs)

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = ConstructionColors.Navy,
                activeTrackColor = ConstructionColors.Navy,
                inactiveTrackColor = ConstructionColors.Hairline,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Peer markers row — a low rail with dots + names, clipped at edges
        BoxWithConstraints(Modifier.fillMaxWidth().heightIn(min = railMin)) {
            val totalWidthDp = maxWidth
            val span = (range.endInclusive - range.start).coerceAtLeast(0.0001)
            // background rail
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(ConstructionColors.Hairline)
                    .offset(y = 8.dp)
            )
            markers.forEach { m ->
                val clipped = m.value.coerceIn(range.start, range.endInclusive)
                val frac = ((clipped - range.start) / span).coerceIn(0.0, 1.0).toFloat()
                val isClipped = m.value < range.start || m.value > range.endInclusive
                val xDp = totalWidthDp * frac
                // dot
                Box(
                    Modifier
                        .offset(x = xDp - 4.dp, y = 6.dp)
                        .size(8.dp)
                        .background(
                            if (isClipped) ConstructionColors.InkMuted else ConstructionColors.Copper,
                            CircleShape
                        )
                )
                // small label chip
                Box(
                    Modifier
                        .offset(x = (xDp - 30.dp).coerceAtLeast(0.dp), y = 18.dp)
                        .background(
                            ConstructionColors.SurfaceMuted,
                            RoundedCornerShape(Design.radii.pill)
                        )
                        .border(
                            width = 1.dp,
                            color = ConstructionColors.Hairline,
                            shape = RoundedCornerShape(Design.radii.pill)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        m.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = ConstructionColors.InkSoft
                    )
                }
            }
        }
    }
}
