package com.jaejal.reconstruction.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
            elevation = if (emphasized) 12.dp else 4.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.4f),
            spotColor = Color.Black.copy(alpha = 0.6f)
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
        ChipTone.Primary -> ConstructionColors.NavyTint to ConstructionColors.InkStrong
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
    val bg = if (gold) ConstructionColors.Copper else ConstructionColors.NavyTint
    val borderColor = if (gold) ConstructionColors.Gold else ConstructionColors.Hairline
    val fg = if (gold) ConstructionColors.InkStrong else ConstructionColors.InkSoft
    val shape = RoundedCornerShape(Design.radii.pill)
    Box(
        modifier
            .size(40.dp)
            .shadow(
                elevation = if (gold) 6.dp else 0.dp,
                shape = shape,
                ambientColor = ConstructionColors.Gold.copy(alpha = 0.5f),
                spotColor = ConstructionColors.Gold.copy(alpha = 0.8f)
            )
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
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val fg = when (tone) {
        ChipTone.Gain -> ConstructionColors.Gain
        ChipTone.Loss -> ConstructionColors.Loss
        // Primary values sit on glass cards; InkStrong reads cleanly on dark.
        ChipTone.Primary -> ConstructionColors.InkStrong
        else -> ConstructionColors.Ink
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .padding(vertical = Design.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Design.spacing.sm)
    ) {
        // Label takes flexible space and ellipsizes if cramped; the VALUE never wraps
        // (softWrap=false) so e.g. "2.29억 (환급)" stays on one line in a narrow 2-up card
        // instead of breaking mid-token as "(환" / "급)".
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ConstructionColors.InkMuted,
                    modifier = Modifier.size(15.dp)
                )
                HSpace(Design.spacing.xs)
            }
            Text(
                label,
                color = ConstructionColors.InkSoft,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            value,
            color = fg,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
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

// ---------- Bookmark heart toggle ----------

/**
 * A togglable heart that animates when switched on/off.
 *
 *  - Icon swaps between an outline heart (off, muted) and a filled gold heart (on),
 *    crossfaded with a small scale so the fill "lands".
 *  - On every toggle the whole heart does a spring pop (overshoot to ~1.3× then
 *    settle) — stronger on turn-on than on turn-off — to confirm the tap.
 *
 * Self-contained: pass the current [bookmarked] state and an [onToggle] callback.
 */
@Composable
fun BookmarkHeart(
    bookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    val scale = remember { Animatable(1f) }
    var firstComposition by remember { mutableStateOf(true) }

    // Pop on every toggle. No `scale.value == 1f` guard: Compose cancels the prior
    // animation when the key changes, and snapTo(1f) gives a clean restart, so a tap
    // landing mid-spring still re-fires (rapid toggles no longer silently drop).
    LaunchedEffect(bookmarked) {
        if (firstComposition) {
            firstComposition = false
            return@LaunchedEffect
        }
        val peak = if (bookmarked) 1.35f else 0.82f
        scale.snapTo(1f)
        scale.animateTo(peak, animationSpec = tween(120))
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val tint by animateColorAsState(
        targetValue = if (bookmarked) ConstructionColors.Gold else ConstructionColors.InkSoft,
        animationSpec = tween(180),
        label = "heartTint"
    )

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressBg by animateColorAsState(
        targetValue = if (pressed) ConstructionColors.Gold.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(120),
        label = "heartPressBg"
    )
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(pressBg)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = bookmarked,
            transitionSpec = {
                (scaleIn(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(tween(140)))
                    .togetherWith(scaleOut(tween(120)) + fadeOut(tween(120)))
            },
            label = "heartIcon"
        ) { on ->
            Icon(
                imageVector = if (on) ConstructionIcons.HeartFilled else ConstructionIcons.HeartOutline,
                contentDescription = if (on) "북마크 해제" else "북마크",
                tint = tint,
                modifier = Modifier
                    .size(size)
                    .scale(scale.value)
            )
        }
    }
}
