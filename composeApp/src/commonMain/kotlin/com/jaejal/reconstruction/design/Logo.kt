package com.jaejal.reconstruction.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Logo ImageVectors — baked into ConstructionIcons
// ---------------------------------------------------------------------------
//
// 'ㅈ' (jieut) styled as a building rooftop / A-frame silhouette:
//   - Top ridge bar  → the ㅈ horizontal stroke / roof ridge
//   - Center apex    → the ㅈ vertical connector from ridge to fork
//   - Two diagonal legs → A-frame rafters
//   - Ground baseline → building foundation tie
//
// Stroke idiom matches ConstructionIcons: Round cap/join, width 1.8f, fill=null.
// Navy is the entire structural mark; Gold is a single filled diamond ridge-cap.

private fun logoVector(
    name: String,
    strokeColor: Color
): ImageVector = Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    // PATH 1 — structural mark in Navy (or InkOnDark on dark backgrounds)
    path(
        stroke = SolidColor(strokeColor),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        fill = null
    ) {
        // Top ridge bar (ㅈ horizontal / roof ridge — full width)
        moveTo(4.5f, 6.5f)
        lineTo(19.5f, 6.5f)
        // Center apex connector (ㅈ vertical from ridge down to the fork)
        moveTo(12f, 6.5f)
        lineTo(12f, 9f)
        // Left descending leg (A-frame left rafter)
        moveTo(12f, 9f)
        lineTo(5.5f, 19f)
        // Right descending leg (A-frame right rafter)
        moveTo(12f, 9f)
        lineTo(18.5f, 19f)
        // Ground baseline (foundation — ties both feet)
        moveTo(5.5f, 19f)
        lineTo(18.5f, 19f)
    }
    // PATH 2 — Gold ridge-cap diamond (rank #1 accent, only filled element)
    // Diamond centered at (12, 6.5), ~2.2f across
    path(
        fill = SolidColor(ConstructionColors.Gold),
        stroke = null,
        fillAlpha = 1f,
        pathFillType = PathFillType.NonZero
    ) {
        moveTo(12f, 4.9f)
        lineTo(13.1f, 6.5f)
        lineTo(12f, 8.1f)
        lineTo(10.9f, 6.5f)
        close()
    }
}.build()

// Extend ConstructionIcons with Logo + LogoOnDark
val ConstructionIcons.Logo: ImageVector
    get() = logoVector("Logo", ConstructionColors.Navy)

val ConstructionIcons.LogoOnDark: ImageVector
    get() = logoVector("LogoOnDark", ConstructionColors.InkOnDark)

// ---------------------------------------------------------------------------
// ConstructionLogo composable
// ---------------------------------------------------------------------------
//
// The mark is two-tone (Navy stroke + Gold fill), so we use Image rather than
// Icon — Icon applies a single tint that would flatten both colors.

/**
 * The ㅈ-rooftop monogram. Navy on light backgrounds; use [onDark]=true on the
 * navy-gradient hero so the mark appears in InkOnDark with the Gold diamond
 * still visible.
 *
 * Default size 24.dp; pass a larger value for hero placement.
 */
@Composable
fun ConstructionLogo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    onDark: Boolean = false
) {
    val vector = if (onDark) ConstructionIcons.LogoOnDark else ConstructionIcons.Logo
    Image(
        imageVector = vector,
        contentDescription = "재건축 랭커",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

// ---------------------------------------------------------------------------
// ConstructionWordmark composable
// ---------------------------------------------------------------------------
//
// Mark + "재건축 랭커" in Noto Serif KR (headlineSmall) side-by-side.
// Intended for: splash, My-tab header, future global top bar.

/**
 * Horizontal lockup: [ConstructionLogo] + serif wordmark text.
 *
 * @param markSize  size of the logo mark (default 28.dp)
 * @param color     text color (default Ink)
 * @param style     text style (default headlineSmall — Noto Serif KR, the "mentor" serif)
 */
@Composable
fun ConstructionWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 28.dp,
    color: Color = ConstructionColors.Ink,
    style: TextStyle = MaterialTheme.typography.headlineSmall
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConstructionLogo(size = markSize)
        Spacer(Modifier.width(Design.spacing.sm))
        Text(
            text = "재건축 랭커",
            style = style,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
