@file:OptIn(ExperimentalMaterial3Api::class)

package com.jaejal.reconstruction.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
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
                    .background(ConstructionColors.GoldSoft, RoundedCornerShape(Design.radii.pill))
                    .padding(horizontal = Design.spacing.md, vertical = 4.dp)
            ) {
                Text(
                    valueDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    color = ConstructionColors.Gold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (!compact) VSpace(Design.spacing.xs)

        // M3-expressive restyle: hoist colors so the same palette feeds both the
        // custom thumb and track slots. Track is thickened to 8dp with fully
        // rounded caps (stop indicators removed) for a softer, more confident feel.
        val sliderColors = SliderDefaults.colors(
            thumbColor = ConstructionColors.InkStrong,
            activeTrackColor = ConstructionColors.Gold,
            inactiveTrackColor = ConstructionColors.Hairline,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        )
        val thumbInteractionSource = remember { MutableInteractionSource() }
        // Compact mode keeps the original ~20dp thumb; the standard panel uses a
        // slightly larger 22dp thumb to balance the thicker track.
        val thumbDiameter = if (compact) 20.dp else 22.dp
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
            colors = sliderColors,
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                Box(
                    Modifier
                        .size(thumbDiameter)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = ConstructionColors.Gold,
                            spotColor = ConstructionColors.Gold
                        )
                        .background(ConstructionColors.InkStrong, CircleShape)
                        .border(1.dp, ConstructionColors.Hairline, CircleShape)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    colors = sliderColors,
                    modifier = Modifier.height(8.dp),
                    // Remove the M3 stop indicator dots and let the 8dp track keep
                    // its default fully-rounded end caps for an expressive look.
                    drawStopIndicator = null,
                    trackInsideCornerSize = 0.dp
                )
            }
        )

        // Peer markers. Two parts that CAN'T overlap each other or the next slider:
        //   1. dots positioned by value on a thin rail (the spatial "where each peer sits"),
        //   2. a single legend row of names in NORMAL layout flow (so it reserves its own
        //      height and can never bleed into the slider below).
        // The old design absolutely-positioned a name pill under every dot, which collided
        // both with sibling pills (clustered peers) and with the next slider's title.
        if (markers.isNotEmpty()) {
            val span = (range.endInclusive - range.start).coerceAtLeast(0.0001)
            // (1) value dots on a rail — fixed small height, no labels here.
            Box(Modifier.fillMaxWidth().height(railMin)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = 6.dp)
                        .background(ConstructionColors.Hairline)
                )
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val totalWidthDp = maxWidth
                    markers.forEach { m ->
                        val clipped = m.value.coerceIn(range.start, range.endInclusive)
                        val frac = ((clipped - range.start) / span).coerceIn(0.0, 1.0).toFloat()
                        val isClipped = m.value < range.start || m.value > range.endInclusive
                        Box(
                            Modifier
                                .offset(x = (totalWidthDp * frac) - 4.dp, y = 2.dp)
                                .size(8.dp)
                                .background(
                                    if (isClipped) ConstructionColors.InkMuted else ConstructionColors.Gold,
                                    CircleShape
                                )
                        )
                    }
                }
            }
            // (2) legend row — names evenly spread, in flow, single line, never overlapping.
            if (!compact) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    markers.forEach { m ->
                        Box(
                            Modifier
                                .background(ConstructionColors.PaperAlt, RoundedCornerShape(Design.radii.pill))
                                .border(1.dp, ConstructionColors.Border, RoundedCornerShape(Design.radii.pill))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                m.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = ConstructionColors.InkSoft,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
