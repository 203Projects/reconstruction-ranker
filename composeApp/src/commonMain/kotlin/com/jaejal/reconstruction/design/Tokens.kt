package com.jaejal.reconstruction.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

/**
 * Construction Trust palette.
 *
 * Foundations: warm parchment (paper trail / blueprints), navy (steel & expertise),
 * burnt copper (alloy / craftsmanship), forest green (gains), terracotta (cost / loss),
 * matte gold (recognition).
 *
 * All values chosen to clear WCAG AA on the chosen surfaces.
 */
object ConstructionColors {
    // Surfaces
    val Paper = Color(0xFFF8F5EE)        // app background, warmer than #F7F4ED
    val PaperAlt = Color(0xFFEFE9DC)     // muted section background
    val Surface = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFFBF8F0)
    val SurfaceSunken = Color(0xFFF1EBDC) // input/track tone
    val Hairline = Color(0xFFEAE3D2)
    val Border = Color(0xFFD8CFB8)

    // Ink
    val Ink = Color(0xFF14181F)          // near-black
    val InkStrong = Color(0xFF1F2937)
    val InkSoft = Color(0xFF52606D)
    val InkMuted = Color(0xFF8B8676)
    val InkOnDark = Color(0xFFF7F4EC)

    // Brand
    val Navy = Color(0xFF1B3556)         // primary action / brand
    val NavyDeep = Color(0xFF0F2238)
    val NavySoft = Color(0xFF2F5278)
    val NavyTint = Color(0xFFE3EAF3)     // primary container tint

    // Accents
    val Copper = Color(0xFFB05B2C)       // construction accent (steel beams at dusk)
    val CopperSoft = Color(0xFFE9CFB7)
    val Gold = Color(0xFFA8801A)         // rank #1 / award
    val GoldSoft = Color(0xFFEFE0B0)

    // Semantic
    val Gain = Color(0xFF0E6B3E)
    val GainSoft = Color(0xFFD2EBDD)
    val Loss = Color(0xFFA0431C)
    val LossSoft = Color(0xFFF4DBC9)

    // Map / blurred
    val MapMist = Color(0xFFE6DFCE)
}

/** Compact spacing scale (4dp baseline). */
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val gutter: Dp = 20.dp,
    val sectionGap: Dp = 24.dp
)

/** Radii — Zillow-style: large, friendly, but not pillowy. */
data class Radii(
    val xs: Dp = 6.dp,
    val sm: Dp = 10.dp,
    val md: Dp = 14.dp,
    val lg: Dp = 18.dp,
    val xl: Dp = 22.dp,
    val pill: Dp = 999.dp
)

/** Motion durations + curves (kept centralized for consistency). */
data class Motion(
    val instant: Int = 0,
    val fast: Int = 120,
    val medium: Int = 220,
    val slow: Int = 360
)

data class ConstructionDesign(
    val spacing: Spacing = Spacing(),
    val radii: Radii = Radii(),
    val motion: Motion = Motion()
)

val LocalDesign = staticCompositionLocalOf { ConstructionDesign() }

/** Typography sizes pulled out so they're reusable + tunable. */
object TypeScale {
    val displayLg: TextUnit = 34.sp
    val displayMd: TextUnit = 28.sp
    val headlineLg: TextUnit = 24.sp
    val headlineMd: TextUnit = 20.sp
    val headlineSm: TextUnit = 18.sp
    val titleLg: TextUnit = 17.sp
    val titleMd: TextUnit = 15.sp
    val titleSm: TextUnit = 13.sp
    val bodyLg: TextUnit = 16.sp
    val bodyMd: TextUnit = 14.sp
    val bodySm: TextUnit = 13.sp
    val labelLg: TextUnit = 13.sp
    val labelMd: TextUnit = 12.sp
    val labelSm: TextUnit = 11.sp
    // Big number style — for hero values like "9.83억"
    val numericHero: TextUnit = 38.sp
    val numericLarge: TextUnit = 26.sp
    val numericMedium: TextUnit = 20.sp
}

/** Material 3 Shapes set, in case child material components need them. */
val ConstructionShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp)
)
