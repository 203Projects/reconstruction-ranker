package com.jaejal.reconstruction.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------- Surfaces ----------

/**
 * Standard elevated card with a hairline border and subtle shadow.
 * Use for content blocks — districts, stats, sections.
 */
@Composable
fun TrustCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    emphasized: Boolean = false,
    padding: PaddingValues = PaddingValues(Design.spacing.lg),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(Design.radii.xl)
    val baseMod = modifier
        .shadow(
            elevation = if (emphasized) 8.dp else 3.dp,
            shape = shape,
            ambientColor = ConstructionColors.NavyDeep.copy(alpha = 0.08f),
            spotColor = ConstructionColors.NavyDeep.copy(alpha = 0.10f)
        )
    val borderColor =
        if (emphasized) ConstructionColors.Gold.copy(alpha = 0.55f) else ConstructionColors.Hairline
    Surface(
        modifier = if (onClick != null) baseMod.clickable(onClick = onClick) else baseMod,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        border = BorderStroke(if (emphasized) 1.2.dp else 1.dp, borderColor)
    ) {
        Box(Modifier.padding(padding)) { content() }
    }
}

/** Quiet inline card — flat, no shadow, used inside scrollable lists. */
@Composable
fun QuietCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(Design.spacing.lg),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(Design.radii.lg)
    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        border = BorderStroke(1.dp, ConstructionColors.Hairline)
    ) {
        Box(Modifier.padding(padding)) { content() }
    }
}

// ---------- Chips & badges ----------

enum class ChipTone { Neutral, Primary, Gold, Gain, Loss }

@Composable
fun ToneChip(
    text: String,
    tone: ChipTone = ChipTone.Neutral,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (tone) {
        ChipTone.Neutral -> ConstructionColors.Hairline to ConstructionColors.InkSoft
        ChipTone.Primary -> ConstructionColors.NavyTint to ConstructionColors.NavyDeep
        ChipTone.Gold -> ConstructionColors.GoldSoft to ConstructionColors.Gold
        ChipTone.Gain -> ConstructionColors.GainSoft to ConstructionColors.Gain
        ChipTone.Loss -> ConstructionColors.LossSoft to ConstructionColors.Loss
    }
    Box(
        modifier
            .background(bg, RoundedCornerShape(Design.radii.pill))
            .padding(horizontal = Design.spacing.sm, vertical = Design.spacing.xs)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Rank badge for ranking-list rows. Position 1 is gold, others ghost navy. */
@Composable
fun RankBadge(position: Int, modifier: Modifier = Modifier) {
    val gold = position == 1
    val bg = if (gold) ConstructionColors.GoldSoft else ConstructionColors.NavyTint
    val borderColor = if (gold) ConstructionColors.Gold else ConstructionColors.NavySoft.copy(alpha = 0.5f)
    val fg = if (gold) ConstructionColors.Gold else ConstructionColors.Navy
    val shape = RoundedCornerShape(Design.radii.pill)
    Box(
        modifier
            .size(40.dp)
            .background(bg, shape)
            .border(BorderStroke(1.dp, borderColor), shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            position.toString(),
            color = fg,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------- Stats ----------

/** Hero numeric value: large, tabular, colored by sentiment. */
@Composable
fun HeroNumber(
    value: String,
    tone: ChipTone = ChipTone.Primary,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val fg = when (tone) {
        ChipTone.Gain -> ConstructionColors.Gain
        ChipTone.Loss -> ConstructionColors.Loss
        ChipTone.Gold -> ConstructionColors.Gold
        else -> ConstructionColors.Navy
    }
    val mediumMs = Design.motion.medium
    val fastMs = Design.motion.fast
    Column(modifier) {
        if (label != null) {
            Text(
                label,
                color = ConstructionColors.InkMuted,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(2.dp))
        }
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(tween(mediumMs)) togetherWith fadeOut(tween(fastMs))
            },
            label = "heroValue"
        ) { v ->
            Text(
                v,
                style = MaterialTheme.typography.displayMedium,
                color = fg,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatLine(
    label: String,
    value: String,
    tone: ChipTone = ChipTone.Neutral,
    modifier: Modifier = Modifier
) {
    val fg = when (tone) {
        ChipTone.Gain -> ConstructionColors.Gain
        ChipTone.Loss -> ConstructionColors.Loss
        ChipTone.Primary -> ConstructionColors.Navy
        else -> ConstructionColors.Ink
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .padding(vertical = Design.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = ConstructionColors.InkSoft, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            color = fg,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------- Layout helpers ----------

@Composable
fun SectionHeader(
    title: String,
    sub: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = ConstructionColors.Ink
            )
            if (sub != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    sub,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ConstructionColors.InkSoft
                )
            }
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ConstructionColors.Hairline)
    )
}

@Composable
fun VSpace(value: Dp) {
    Spacer(Modifier.height(value))
}

@Composable
fun HSpace(value: Dp) {
    Spacer(Modifier.width(value))
}
