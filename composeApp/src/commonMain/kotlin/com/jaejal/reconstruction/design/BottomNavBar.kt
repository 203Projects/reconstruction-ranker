package com.jaejal.reconstruction.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class BottomNavItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Custom bottom navigation built to match the construction-theme design system.
 *
 * Why custom instead of Material 3 NavigationBar?
 *  - Tighter typography (Noto Sans KR labels at 11sp)
 *  - Active state uses an oval pill behind the icon — same shape language as ToneChip / RankBadge
 *  - Parchment background ties it to the rest of the app
 *  - Cleaner press feedback (no ripple bleed across rounded corners)
 */
@Composable
fun ConstructionBottomBar(
    items: List<BottomNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HairlineDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .heightIn(min = 64.dp)
                .padding(horizontal = Design.spacing.sm, vertical = Design.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEach { item ->
                BottomNavCell(
                    item = item,
                    selected = item.key == selectedKey,
                    onClick = { onSelect(item.key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavCell(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val iconColor by animateColorAsState(
        targetValue = if (selected) ConstructionColors.Navy else ConstructionColors.InkSoft,
        animationSpec = tween(180),
        label = "iconColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) ConstructionColors.NavyDeep else ConstructionColors.InkMuted,
        animationSpec = tween(180),
        label = "labelColor"
    )
    val pillBg by animateColorAsState(
        targetValue = if (selected) ConstructionColors.NavyTint else Color.Transparent,
        animationSpec = tween(180),
        label = "pillBg"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (selected) 60.dp else 40.dp,
        animationSpec = tween(220),
        label = "pillWidth"
    )

    Column(
        modifier
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .height(28.dp)
                .background(pillBg, RoundedCornerShape(Design.radii.pill))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            color = labelColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
