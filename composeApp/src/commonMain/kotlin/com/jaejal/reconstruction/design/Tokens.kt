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
    // Surfaces (Dark Mode & Glassmorphism)
    val Paper = Color(0xFF0B0F19)        // app background, deep navy/indigo
    val PaperAlt = Color(0xFF070A11)     // darker section background
    val Surface = Color(0x08FFFFFF)      // rgba(255, 255, 255, 0.03) - glass panel
    val SurfaceMuted = Color(0x04FFFFFF) // subtle
    val SurfaceSunken = Color(0x0AFFFFFF)// input/track tone
    val Hairline = Color(0x14FFFFFF)     // rgba(255, 255, 255, 0.08)
    val Border = Color(0x28FFFFFF)       // slightly stronger border

    // Ink (Light Text for Dark Mode)
    val Ink = Color(0xFFF8FAFC)          // primary text (slate-50)
    val InkStrong = Color(0xFFFFFFFF)    // white
    val InkSoft = Color(0xFF94A3B8)      // secondary text (slate-400)
    val InkMuted = Color(0xFF64748B)     // muted text (slate-500)
    val InkOnDark = Color(0xFFF8FAFC)    // Same as Ink

    // Brand.
    // DARK-MODE PAIRING RULE: Navy/NavyDeep/NavySoft are dark blues — never use them as
    // text/icon color or as a button container on the dark Paper background (they fail
    // WCAG AA, ~1.5:1). On dark surfaces use Ink/InkStrong/InkSoft for text and Gold for
    // accents/CTAs. NavyTint is a translucent navy BADGE surface only (e.g. RankBadge);
    // pair it ONLY with light ink text, never with NavyDeep/Navy.
    val Navy = Color(0xFF1E3A8A)         // blue-900
    val NavyDeep = Color(0xFF172554)     // blue-950
    val NavySoft = Color(0xFF1E40AF)     // blue-800
    val NavyTint = Color(0x1A1E3A8A)     // translucent navy badge surface (light text only)

    // Accents
    val Copper = Color(0xFFD97706)       // amber-600
    val CopperSoft = Color(0x1AD97706)
    val Gold = Color(0xFFFBBF24)         // amber-400, rank #1 / award / active state
    val GoldSoft = Color(0x1AFBBF24)     // transparent glow

    // Semantic
    val Gain = Color(0xFF10B981)         // emerald-500
    val GainSoft = Color(0x1A10B981)
    val Loss = Color(0xFFF43F5E)         // rose-500
    val LossSoft = Color(0x1AF43F5E)

    // Map / blurred
    val MapMist = Color(0xFF1E293B)      // slate-800
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
