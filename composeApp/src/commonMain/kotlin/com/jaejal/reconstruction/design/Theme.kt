package com.jaejal.reconstruction.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jaejal.reconstruction.resources.NotoSansKR_Bold
import com.jaejal.reconstruction.resources.NotoSansKR_Medium
import com.jaejal.reconstruction.resources.NotoSansKR_Regular
import com.jaejal.reconstruction.resources.NotoSansKR_SemiBold
import com.jaejal.reconstruction.resources.NotoSerifKR_Bold
import com.jaejal.reconstruction.resources.NotoSerifKR_Medium
import com.jaejal.reconstruction.resources.NotoSerifKR_SemiBold
import com.jaejal.reconstruction.resources.Res
import org.jetbrains.compose.resources.Font

private val ConstructionColorScheme = darkColorScheme(
    primary = ConstructionColors.Navy,
    onPrimary = ConstructionColors.InkOnDark,
    primaryContainer = ConstructionColors.NavyTint,
    onPrimaryContainer = ConstructionColors.InkStrong,

    secondary = ConstructionColors.Copper,
    onSecondary = ConstructionColors.InkOnDark,
    secondaryContainer = ConstructionColors.CopperSoft,
    onSecondaryContainer = ConstructionColors.Copper,

    tertiary = ConstructionColors.Gold,
    onTertiary = ConstructionColors.InkOnDark,
    tertiaryContainer = ConstructionColors.GoldSoft,
    onTertiaryContainer = ConstructionColors.Gold,

    background = ConstructionColors.Paper,
    onBackground = ConstructionColors.Ink,

    surface = ConstructionColors.Surface,
    onSurface = ConstructionColors.Ink,
    surfaceVariant = ConstructionColors.SurfaceMuted,
    onSurfaceVariant = ConstructionColors.InkSoft,
    surfaceContainerLowest = ConstructionColors.Surface,
    surfaceContainerLow = ConstructionColors.SurfaceMuted,
    surfaceContainer = ConstructionColors.PaperAlt,
    surfaceContainerHigh = ConstructionColors.SurfaceSunken,
    surfaceContainerHighest = ConstructionColors.Hairline,

    outline = ConstructionColors.Border,
    outlineVariant = ConstructionColors.Hairline,

    error = ConstructionColors.Loss,
    onError = ConstructionColors.InkOnDark,
    errorContainer = ConstructionColors.LossSoft,
    onErrorContainer = ConstructionColors.Loss
)

/**
 * Korean Google Fonts — Noto Serif KR for headlines, Noto Sans KR for body.
 * Both bundled under SIL OFL 1.1 (see composeResources/font/OFL.txt).
 */
@Composable
private fun notoSerifKr(): FontFamily = FontFamily(
    Font(Res.font.NotoSerifKR_Medium, weight = FontWeight.Medium),
    Font(Res.font.NotoSerifKR_SemiBold, weight = FontWeight.SemiBold),
    Font(Res.font.NotoSerifKR_Bold, weight = FontWeight.Bold)
)

@Composable
private fun notoSansKr(): FontFamily = FontFamily(
    Font(Res.font.NotoSansKR_Regular, weight = FontWeight.Normal),
    Font(Res.font.NotoSansKR_Medium, weight = FontWeight.Medium),
    Font(Res.font.NotoSansKR_SemiBold, weight = FontWeight.SemiBold),
    Font(Res.font.NotoSansKR_Bold, weight = FontWeight.Bold)
)

@Composable
private fun constructionTypography(): Typography {
    val serif = notoSerifKr()
    val sans = notoSansKr()
    return Typography(
        displayLarge = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = TypeScale.displayLg, letterSpacing = (-0.6).sp, lineHeight = 40.sp),
        displayMedium = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = TypeScale.displayMd, letterSpacing = (-0.5).sp, lineHeight = 34.sp),
        displaySmall = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = TypeScale.headlineLg, letterSpacing = (-0.3).sp, lineHeight = 30.sp),

        headlineLarge = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = TypeScale.headlineLg, lineHeight = 30.sp),
        headlineMedium = TextStyle(fontFamily = serif, fontWeight = FontWeight.SemiBold, fontSize = TypeScale.headlineMd, lineHeight = 26.sp),
        headlineSmall = TextStyle(fontFamily = serif, fontWeight = FontWeight.Medium, fontSize = TypeScale.headlineSm, lineHeight = 24.sp),

        titleLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = TypeScale.titleLg, lineHeight = 22.sp),
        titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = TypeScale.titleMd, letterSpacing = 0.1.sp, lineHeight = 20.sp),
        titleSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = TypeScale.titleSm, letterSpacing = 0.3.sp, lineHeight = 18.sp),

        bodyLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = TypeScale.bodyLg, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = TypeScale.bodyMd, lineHeight = 21.sp),
        bodySmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = TypeScale.bodySm, lineHeight = 19.sp),

        labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = TypeScale.labelLg, letterSpacing = 0.4.sp),
        labelMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = TypeScale.labelMd, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = TypeScale.labelSm, letterSpacing = 0.6.sp)
    )
}

@Composable
fun ConstructionTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDesign provides ConstructionDesign()) {
        MaterialTheme(
            colorScheme = ConstructionColorScheme,
            typography = constructionTypography(),
            shapes = ConstructionShapes,
            content = content
        )
    }
}

/** Quick access from any composable: `Design.spacing.lg`. */
val Design: ConstructionDesign
    @Composable get() = LocalDesign.current
