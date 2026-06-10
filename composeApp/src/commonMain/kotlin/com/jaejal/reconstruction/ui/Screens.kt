package com.jaejal.reconstruction.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jaejal.reconstruction.calc.Engine
import com.jaejal.reconstruction.data.District
import com.jaejal.reconstruction.data.RankedDistrict
import com.jaejal.reconstruction.data.Repository
import com.jaejal.reconstruction.data.TypeInfo
import com.jaejal.reconstruction.design.BookmarkHeart
import com.jaejal.reconstruction.design.BottomNavItem
import com.jaejal.reconstruction.design.Brand
import com.jaejal.reconstruction.design.BuildingFacade
import com.jaejal.reconstruction.design.Buildings
import com.jaejal.reconstruction.design.ConfettiBurst
import com.jaejal.reconstruction.design.CountUpNumber
import com.jaejal.reconstruction.design.ExchangeMeter
import com.jaejal.reconstruction.design.JaejalMascot
import com.jaejal.reconstruction.design.MascotMood
import com.jaejal.reconstruction.design.PixelSprite
import com.jaejal.reconstruction.design.isAnimationEnabled
import com.jaejal.reconstruction.design.ChipTone
import com.jaejal.reconstruction.design.ConstructionBottomBar
import com.jaejal.reconstruction.design.ConstructionLogo
import com.jaejal.reconstruction.design.ConstructionColors
import com.jaejal.reconstruction.design.ConstructionIcons
import com.jaejal.reconstruction.design.ConstructionSlider
import com.jaejal.reconstruction.design.ConstructionTheme
import com.jaejal.reconstruction.design.Design
import com.jaejal.reconstruction.design.HSpace
import com.jaejal.reconstruction.design.HairlineDivider
import com.jaejal.reconstruction.design.HeroNumber
import com.jaejal.reconstruction.design.PeerMarker
import com.jaejal.reconstruction.design.QuietCard
import com.jaejal.reconstruction.design.RankBadge
import com.jaejal.reconstruction.design.SectionHeader
import com.jaejal.reconstruction.design.StatLine
import com.jaejal.reconstruction.design.ToneChip
import com.jaejal.reconstruction.design.TrustCard
import com.jaejal.reconstruction.design.VSpace
import com.jaejal.reconstruction.format.Format

private val NAV_ITEMS = listOf(
    BottomNavItem("home", "홈", ConstructionIcons.Home),
    BottomNavItem("districts", "단지", ConstructionIcons.Districts),
    BottomNavItem("bookmarks", "북마크", ConstructionIcons.HeartOutline),
    BottomNavItem("my", "마이", ConstructionIcons.My)
)

private fun Tab.toKey(): String = when (this) {
    Tab.Home -> "home"; Tab.Districts -> "districts"; Tab.Bookmarks -> "bookmarks"; Tab.My -> "my"
}

private fun keyToTab(k: String): Tab = when (k) {
    "home" -> Tab.Home; "districts" -> Tab.Districts; "bookmarks" -> Tab.Bookmarks; "my" -> Tab.My; else -> Tab.Home
}

@Composable
fun App() {
    ConstructionTheme {
        val state = rememberAppState()
        when {
            state.loading -> LoadingScreen()
            state.loadError != null -> ErrorScreen(state.loadError!!)
            else -> AppShell(state)
        }
    }
}

@Composable
private fun AppShell(state: AppState) {
    val hideNav = state.currentRoute.hidesBottomNav()

    // Wire system back: pop the current tab's stack; if at root, no-op (let OS exit).
    PlatformBackHandler(enabled = true) {
        if (!state.atRootOfCurrentTab()) state.pop()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = !hideNav,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(240)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) + fadeOut()
            ) {
                ConstructionBottomBar(
                    items = NAV_ITEMS,
                    selectedKey = state.currentTab.toKey(),
                    onSelect = { state.selectTab(keyToTab(it)) }
                )
            }
        }
    ) { padding ->
        // NOTE: we intentionally do NOT apply the status-bar inset here. Each root
        // surface handles it itself so colored areas (the home hero, detail top bars)
        // bleed under the status bar instead of leaving a band of background color.
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = state.currentTab to state.currentRoute,
                transitionSpec = {
                    // Subtle scale+fade — adds depth for the dark-glass look without
                    // feeling cartoonish. Mirrors the BookmarkHeart enter/exit spec.
                    (scaleIn(spring(stiffness = Spring.StiffnessMedium), initialScale = 0.96f) + fadeIn(tween(180)))
                        .togetherWith(scaleOut(spring(stiffness = Spring.StiffnessMedium), targetScale = 0.96f) + fadeOut(tween(120)))
                },
                label = "route"
            ) { (tab, route) ->
                RouteContent(state = state, tab = tab, route = route)
            }
        }
    }
}

@Composable
private fun RouteContent(state: AppState, tab: Tab, route: Route) {
    when (route) {
        is Route.RankingList -> RankingScreen(state)
        is Route.DistrictList -> DistrictListScreen(state)
        is Route.BookmarkList -> BookmarkListScreen(state)
        is Route.MyProfile -> MyScreen(state)
        is Route.TypeSelect -> {
            val d = state.findDistrict(route.districtName) ?: return
            TypeSelectScreen(state, d)
        }
        is Route.QuestionSelect -> {
            val d = state.findDistrict(route.districtName) ?: return
            val t = state.findType(d, route.typeIndex) ?: return
            QuestionSelectScreen(state, d, t)
        }
        is Route.Disclaimer -> DisclaimerScreen(state)
        is Route.Simulation -> {
            val d = state.findDistrict(route.districtName) ?: return
            val t = state.findType(d, route.typeIndex) ?: return
            SimulationScreen(state, d, t, route.question)
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = ConstructionColors.Gold, strokeWidth = 2.dp)
            VSpace(Design.spacing.md)
            Text("${Brand.MASCOT_NAME}가 숫자 계산 중… 🧮", color = ConstructionColors.InkMuted)
        }
    }
}

@Composable
private fun ErrorScreen(msg: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("불러오기에 실패했습니다", style = MaterialTheme.typography.headlineSmall, color = ConstructionColors.Ink)
            VSpace(Design.spacing.sm)
            Text(msg, color = ConstructionColors.InkSoft, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ============= Top app bars =============

/**
 * Compact top app bar height (content row), Material-style. The status-bar inset
 * is consumed ONCE here so the title row sits immediately below the status bar —
 * no stacked padding, uniform across every screen.
 */
private val TopBarHeight = 48.dp

/**
 * Detail-screen app bar: a fixed-height row with a leading back button, the title,
 * and an optional compact breadcrumb/subtitle. Title + back are vertically centered
 * in the bar (no empty band above), breadcrumb sits as a tight subtitle below.
 */
@Composable
private fun DetailTopBar(
    title: String,
    breadcrumb: List<String>,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(ConstructionColors.Paper)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Design.spacing.sm)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(TopBarHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = ConstructionIcons.ArrowBack,
                    contentDescription = "뒤로",
                    tint = ConstructionColors.Ink,
                    modifier = Modifier.size(22.dp)
                )
            }
            HSpace(Design.spacing.xs)
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = ConstructionColors.Ink,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        if (breadcrumb.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(
                    start = Design.spacing.lg,
                    end = Design.spacing.sm,
                    bottom = Design.spacing.sm
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                breadcrumb.forEachIndexed { i, p ->
                    if (i > 0) Text(
                        " · ",
                        color = ConstructionColors.InkMuted,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        p,
                        color = if (i == breadcrumb.lastIndex) ConstructionColors.Gold else ConstructionColors.InkSoft,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (i == breadcrumb.lastIndex) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
        HairlineDivider()
    }
}

/**
 * Root-tab app bar: same fixed-height row + tight subtitle, no back button.
 * Keeps top spacing identical to DetailTopBar so every screen lines up.
 */
@Composable
internal fun TabTopBar(title: String, subtitle: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(
                start = Design.spacing.gutter,
                end = Design.spacing.gutter,
                top = Design.spacing.sm,
                bottom = Design.spacing.sm
            )
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge,
            color = ConstructionColors.Ink,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = ConstructionColors.InkSoft,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
    HairlineDivider()
}

// ============= Home (curated ranking) =============

@Composable
private fun RankingScreen(state: AppState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Design.spacing.xxl)
    ) {
        item { RankingHero(totalStars = state.totalStars, cleared = state.districtsCleared, total = state.ranked.size) }
        item {
            SectionHeader(
                title = "어느 동네가 1등 단지일까요? 👀",
                sub = "기준 수익률이 높은 순서예요"
            )
        }
        itemsIndexed(
            state.ranked,
            key = { _, ranked -> ranked.district.name }
        ) { i, ranked ->
            Box(
                Modifier
                    .animateItem()
                    .padding(horizontal = Design.spacing.gutter, vertical = 6.dp)
            ) {
                RankCard(
                    rank = i + 1,
                    ranked = ranked,
                    stars = state.districtProgress[ranked.district.name]?.bestStars ?: 0
                ) { state.openDistrict(ranked.district) }
            }
        }
        item {
            SectionHeader(
                title = "곧 공개돼요! 🔒",
                sub = "${Brand.MASCOT_NAME}가 열심히 준비 중 🐤",
                modifier = Modifier.padding(top = Design.spacing.md)
            )
        }
        items(Repository.blurredDistricts) { b ->
            Box(Modifier.padding(horizontal = Design.spacing.gutter, vertical = 6.dp)) {
                BlurredCard(name = b.name, location = b.location)
            }
        }
    }
}

@Composable
private fun RankingHero(totalStars: Int, cleared: Int, total: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(ConstructionColors.NavyDeep, ConstructionColors.Navy)
                )
            )
            // Bleed the gradient under the status bar, then pad content below it.
            // Keep the top pad small so the inset doesn't stack into a tall empty gap.
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(
                start = Design.spacing.gutter,
                end = Design.spacing.gutter,
                top = Design.spacing.sm,
                bottom = Design.spacing.lg
            )
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ConstructionLogo(size = 32.dp, onDark = true)
                HSpace(Design.spacing.sm)
                Text(
                    Brand.PRODUCT_NAME,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                HSpace(Design.spacing.sm)
                JaejalMascot(mood = MascotMood.Happy, size = 40.dp)
            }
            VSpace(4.dp)
            Text(
                "안녕, 난 ${Brand.MASCOT_NAME}! 새 아파트, 결국 내가 얼마 내야 할까? 🐤",
                style = MaterialTheme.typography.bodySmall,
                color = ConstructionColors.NavyTint
            )
            VSpace(Design.spacing.md)
            Row {
                StatBadge("⭐ 별", totalStars.toString()); HSpace(Design.spacing.sm)
                StatBadge("🏢 깬 단지", "$cleared/$total"); HSpace(Design.spacing.sm)
                StatBadge("기준일", "2026-05")
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(
        Modifier
            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(Design.radii.lg))
            .padding(horizontal = Design.spacing.md, vertical = Design.spacing.sm)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RankCard(rank: Int, ranked: RankedDistrict, stars: Int, onClick: () -> Unit) {
    val gold = rank == 1
    val building = remember(ranked.district.name) { Buildings.forDistrict(ranked.district.name) }
    TrustCard(
        modifier = Modifier.fillMaxWidth(),
        emphasized = gold,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Pixel building thumbnail + rank badge over its base
            Box(contentAlignment = Alignment.BottomStart) {
                PixelSprite(
                    sprite = building.sprite,
                    modifier = Modifier.size(48.dp),
                    name = "thumb-${ranked.district.name}"
                )
                RankBadge(position = rank, modifier = Modifier.size(24.dp).offset(x = (-6).dp, y = 6.dp))
            }
            HSpace(Design.spacing.lg)
            Column(Modifier.weight(1f)) {
                Text(
                    ranked.district.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = ConstructionColors.Ink,
                    fontWeight = FontWeight.SemiBold
                )
                VSpace(2.dp)
                Text(
                    ranked.district.shortLocation,
                    style = MaterialTheme.typography.bodySmall,
                    color = ConstructionColors.InkSoft
                )
                VSpace(Design.spacing.sm)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (stars > 0) {
                        Text("⭐".repeat(stars), style = MaterialTheme.typography.labelMedium, color = ConstructionColors.Gold)
                        HSpace(6.dp)
                    }
                    ToneChip(text = "${ranked.district.types.size}개 타입", tone = ChipTone.Neutral)
                }
            }
            HSpace(Design.spacing.sm)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "기준 수익률",
                    style = MaterialTheme.typography.labelSmall,
                    color = ConstructionColors.InkMuted
                )
                Text(
                    Format.percent(ranked.headlineRoi),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (ranked.headlineRoi >= 0) ConstructionColors.Gain else ConstructionColors.Loss,
                    fontWeight = FontWeight.Bold
                )
                ToneChip(text = "최저단가 타입", tone = if (ranked.headlineRoi >= 0) ChipTone.Gain else ChipTone.Loss)
            }
        }
    }
}

@Composable
private fun BlurredCard(name: String, location: String) {
    QuietCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(ConstructionColors.MapMist, CircleShape)
            )
            HSpace(Design.spacing.lg)
            Column(Modifier.weight(1f).blur(8.dp)) {
                Text(name, style = MaterialTheme.typography.titleLarge, color = ConstructionColors.InkSoft)
                Text(location, style = MaterialTheme.typography.bodySmall, color = ConstructionColors.InkMuted)
            }
            ToneChip(text = "준비중", tone = ChipTone.Neutral)
        }
    }
}

// ============= Detail screens =============

@Composable
private fun TypeSelectScreen(state: AppState, d: District) {
    Column(Modifier.fillMaxSize()) {
        DetailTopBar(
            title = d.name,
            breadcrumb = listOf(d.shortLocation, d.noticeDate ?: "고시일 미상"),
            onBack = { state.pop() }
        )
        LazyColumn(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(
                start = Design.spacing.gutter,
                end = Design.spacing.gutter,
                top = Design.spacing.sm,
                bottom = Design.spacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Design.spacing.md)
        ) {
            item {
                Text(
                    "관심 평형을 선택해 주십시오.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ConstructionColors.InkSoft
                )
            }
            val grouped = d.types.groupBy { it.phase ?: "" }
            grouped.entries.forEach { (phase, types) ->
                if (phase.isNotBlank()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = Design.spacing.sm)
                        ) {
                            Text(
                                phase,
                                style = MaterialTheme.typography.titleMedium,
                                color = ConstructionColors.InkSoft
                            )
                            HSpace(Design.spacing.sm)
                            Box(
                                Modifier
                                    .height(1.dp)
                                    .weight(1f)
                                    .background(ConstructionColors.Hairline)
                            )
                        }
                    }
                }
                items(
                    types.chunked(2),
                    key = { row -> row.joinToString("|") { it.index.toString() } }
                ) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Design.spacing.md)) {
                        row.forEach { t ->
                            Box(Modifier.weight(1f)) {
                                TypeCard(state = state, district = d, type = t) { state.openType(d, t) }
                            }
                        }
                        if (row.size == 1) Box(Modifier.weight(1f)) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeCard(state: AppState, district: District, type: TypeInfo, onClick: () -> Unit) {
    val result = remember(district.base, type) { Engine.calculate(district.base, type) }
    val burdenTone = if (result.burden < 0) ChipTone.Gain else ChipTone.Loss
    val roiTone = if (result.roi >= 0) ChipTone.Gain else ChipTone.Loss
    val bookmarked = state.isBookmarked(district, type)
    Box {
        TrustCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, padding = PaddingValues(Design.spacing.lg)) {
            Column {
                // Reserve room on the right so the title never runs under the heart.
                Text(
                    formatTypeLabel(district, type),
                    style = MaterialTheme.typography.titleLarge,
                    color = ConstructionColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 28.dp)
                )
                Text(
                    "분양 ${type.saleArea ?: "—"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ConstructionColors.InkMuted
                )
                VSpace(Design.spacing.md)
                StatLine("KB시세", Format.eok(type.kbPrice), tone = ChipTone.Primary, icon = ConstructionIcons.Building)
                StatLine("분담금", Format.burdenWithRefund(result.burden), tone = burdenTone, icon = ConstructionIcons.Calculator)
                StatLine("기준 수익률", Format.percent(result.roi), tone = roiTone, icon = ConstructionIcons.TrendingUp)
            }
        }
        BookmarkHeart(
            bookmarked = bookmarked,
            onToggle = { state.toggleBookmark(district, type) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = Design.spacing.xs, top = Design.spacing.xs)
        )
    }
}

internal fun formatTypeLabel(d: District, t: TypeInfo): String {
    val raw = t.label
    return when {
        d.name.startsWith("압구정") -> if (raw.endsWith("A") || raw.endsWith("B")) "${raw}형" else "${raw}평"
        d.name.startsWith("대치") -> raw
        else -> "전용 ${raw}㎡"
    }
}

@Composable
private fun QuestionSelectScreen(state: AppState, d: District, t: TypeInfo) {
    Column(Modifier.fillMaxSize()) {
        DetailTopBar(
            title = "뭐가 제일 궁금해요?",
            breadcrumb = listOf(d.name, formatTypeLabel(d, t)),
            onBack = { state.pop() }
        )
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(Design.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(Design.spacing.lg)
        ) {
            // 출발 코인 (종전자산) reveal — folded in here where d + t are in scope.
            TrustCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JaejalMascot(mood = MascotMood.Happy, size = 48.dp)
                    HSpace(Design.spacing.md)
                    Column(Modifier.weight(1f)) {
                        Text("🪙 내 출발 코인", style = MaterialTheme.typography.labelLarge, color = ConstructionColors.InkSoft)
                        Text(
                            Format.eok(t.priorUnitPrice),
                            style = MaterialTheme.typography.headlineMedium,
                            color = ConstructionColors.Gold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "지금 살고 있는 헌 집 값이에요. 재건축 게임의 밑천!",
                            style = MaterialTheme.typography.labelMedium,
                            color = ConstructionColors.InkMuted
                        )
                    }
                }
            }
            QuestionHeroCard(1, "💰 나 결국 얼마 더 내야 해?",
                "레버를 움직여 내 분담금이 어떻게 변하는지 직접 봐요.",
                ChipTone.Primary) { state.openQuestion(d, t, Question.Q1) }
            QuestionHeroCard(2, "🤔 지금 팔까, 들고 갈까?",
                "헌 집값 + 분담금이 새 집 미래값보다 싼지 비교해서 점수를 매겨요.",
                ChipTone.Gold) { state.openQuestion(d, t, Question.Q2) }
        }
    }
}

@Composable
private fun QuestionHeroCard(index: Int, title: String, sub: String, tone: ChipTone, onClick: () -> Unit) {
    TrustCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        padding = PaddingValues(Design.spacing.xl)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToneChip(text = "Q$index", tone = tone)
                HSpace(Design.spacing.sm)
                Text(
                    if (tone == ChipTone.Primary) "레버로 분담금 흔들기 🎚️" else "투자 점수 매기기 🏆",
                    style = MaterialTheme.typography.labelLarge,
                    color = ConstructionColors.InkSoft,
                    fontWeight = FontWeight.SemiBold
                )
            }
            VSpace(Design.spacing.md)
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = ConstructionColors.Ink,
                fontWeight = FontWeight.SemiBold
            )
            VSpace(6.dp)
            Text(sub, style = MaterialTheme.typography.bodyMedium, color = ConstructionColors.InkSoft)
            VSpace(Design.spacing.md)
            val ctaColor = if (tone == ChipTone.Primary) ConstructionColors.Ink else ConstructionColors.Gold
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("시작하기", style = MaterialTheme.typography.titleSmall, color = ctaColor, fontWeight = FontWeight.SemiBold)
                HSpace(6.dp)
                Text("→", color = ctaColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DisclaimerScreen(state: AppState) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize()) {
        DetailTopBar(title = "게임 규칙 하나만! 📋", breadcrumb = listOf("규칙 확인"), onBack = { state.pop() })
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(Design.spacing.gutter)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                JaejalMascot(mood = MascotMood.Thinking, size = 56.dp)
                HSpace(Design.spacing.sm)
                Text("잠깐, 시작 전에!", style = MaterialTheme.typography.headlineLarge, color = ConstructionColors.Ink)
            }
            VSpace(Design.spacing.sm)
            Text(
                "여기 숫자는 공식 고시문 기준 '추정치'예요. 감 잡는 덴 딱이지만, 진짜 계약 결정은 꼭 신중하게요. 약속? 🤝",
                style = MaterialTheme.typography.bodyLarge,
                color = ConstructionColors.InkSoft
            )
            VSpace(Design.spacing.xl)
            DisclaimerSection("1", "추정치의 성격", "본 도구가 출력하는 분담금·수익률·총투자금은 공개 고시문과 시장 시세를 바탕으로 한 추정치입니다. 향후 사업계획·조합 결정·시장가격 변동에 따라 달라집니다.")
            DisclaimerSection("2", "의사결정에 대한 책임", "본 결과는 보조 자료이며, 매수·매도·보유 의사결정의 단독 근거가 될 수 없습니다. 최종 결정과 그 결과의 책임은 이용자 본인에게 있습니다.")
            DisclaimerSection("3", "KB시세·유사단지 데이터", "표시되는 KB시세·유사단지 가격은 특정 시점의 공개자료를 옮긴 것으로, 항상 최신이 아닐 수 있습니다. 적용 시점은 각 화면에 명기되어 있습니다.")
            DisclaimerSection("4", "피드백 환영", "수치 또는 단지에 오류가 있다고 판단되시면 “고쳐주세요” 버튼으로 알려주십시오. 이 서비스는 사용자 피드백으로 개선됩니다.")
            VSpace(80.dp)
        }
        HairlineDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(Design.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Design.spacing.sm)
        ) {
            OutlinedButton(
                onClick = { state.pop() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(Design.radii.xl),
                border = BorderStroke(1.dp, ConstructionColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ConstructionColors.Ink)
            ) { Text("뒤로") }
            Button(
                onClick = { state.acceptDisclaimer() },
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(Design.radii.xl),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ConstructionColors.Gold,
                    contentColor = ConstructionColors.NavyDeep
                )
            ) { Text("좋아요, 시작할게요! 🤝", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun DisclaimerSection(index: String, title: String, body: String) {
    Row(modifier = Modifier.padding(vertical = Design.spacing.sm)) {
        Box(
            Modifier
                .size(28.dp)
                .background(ConstructionColors.NavyTint, RoundedCornerShape(Design.radii.pill)),
            contentAlignment = Alignment.Center
        ) {
            Text(index, color = ConstructionColors.InkSoft, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        HSpace(Design.spacing.md)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = ConstructionColors.Ink, fontWeight = FontWeight.SemiBold)
            VSpace(4.dp)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = ConstructionColors.InkSoft)
        }
    }
}

@Composable
private fun SimulationScreen(state: AppState, d: District, t: TypeInfo, question: Question) {
    var factorT by remember { mutableStateOf(1.0) }
    var factorL by remember { mutableStateOf(1.0) }
    var factorJ by remember { mutableStateOf(1.0) }
    var atOverride by remember { mutableStateOf(d.base.estimatedNewPrice84) }

    // The reveal is an in-screen toggle, NOT a route — keeps the four factors where they
    // already live (screen-local). 확정 opens the chest; 다시 closes it for another go.
    var revealed by remember { mutableStateOf(false) }
    var revealCount by remember { mutableStateOf(0) }   // re-triggers the count-up; theatrical only on the 1st

    val result = Engine.calculate(
        base = d.base,
        type = t,
        factorT = factorT,
        factorL = factorL,
        factorJ = factorJ,
        atOverride = if (question == Question.Q2) atOverride else null
    )

    val peers = state.districts.filter { it.name != d.name }
    val peerShort: (District) -> String = { it.name.take(4) }
    val building = remember(d.name) { Buildings.forDistrict(d.name) }
    val isRefund = result.burden < 0
    // 교환비 → how much of the building lights up (data lives ~0.61..1.01).
    val litFraction = (((result.proportionRatio - 0.55) / 0.50).coerceIn(0.0, 1.0)).toFloat()

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(
            title = if (question == Question.Q1) "내 분담금 퀘스트" else "팔까 말까 퀘스트",
            breadcrumb = listOf(d.name, formatTypeLabel(d, t)),
            onBack = { state.pop() }
        )

        AnimatedContent(
            targetState = revealed,
            transitionSpec = {
                (scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f) + fadeIn(tween(220)))
                    .togetherWith(fadeOut(tween(120)))
            },
            label = "reveal",
            modifier = Modifier.weight(1f)
        ) { isRevealed ->
            if (!isRevealed) {
                // ---------- CHEST CLOSED: live building + levers ----------
                LeverStage(
                    d = d, t = t, question = question, result = result,
                    building = building, litFraction = litFraction, isRefund = isRefund,
                    factorT = factorT, factorL = factorL, factorJ = factorJ, atOverride = atOverride,
                    peers = peers, peerShort = peerShort,
                    onFactorT = { factorT = it }, onFactorL = { factorL = it },
                    onFactorJ = { factorJ = it }, onAt = { atOverride = it },
                    onReset = {
                        factorT = 1.0; factorL = 1.0; factorJ = 1.0
                        atOverride = d.base.estimatedNewPrice84
                    },
                    onConfirm = {
                        state.recordReveal(d.name, question, result.roi, result.burden)
                        revealCount += 1
                        revealed = true
                    }
                )
            } else {
                // ---------- CHEST OPEN: the reveal ----------
                RevealStage(
                    d = d, t = t, question = question, result = result,
                    building = building, isRefund = isRefund,
                    stars = state.starsFor(question, result.roi, result.burden),
                    theatrical = revealCount <= 1,
                    revealKey = revealCount,
                    onRetry = { revealed = false },
                    onBookmark = { state.toggleBookmark(d, t) },
                    isBookmarked = state.isBookmarked(d, t),
                    onRanking = { state.popToRoot(); state.selectTab(Tab.Home) }
                )
            }
        }
    }
}

/** Chest-closed stage: the live "building grows" facade + 교환비 meter + lever sliders. */
@Composable
private fun LeverStage(
    d: District, t: TypeInfo, question: Question, result: com.jaejal.reconstruction.data.SimulationResult,
    building: com.jaejal.reconstruction.design.Building, litFraction: Float, isRefund: Boolean,
    factorT: Double, factorL: Double, factorJ: Double, atOverride: Double,
    peers: List<District>, peerShort: (District) -> String,
    onFactorT: (Double) -> Unit, onFactorL: (Double) -> Unit,
    onFactorJ: (Double) -> Unit, onAt: (Double) -> Unit,
    onReset: () -> Unit, onConfirm: () -> Unit
) {
    val dense = question == Question.Q2
    Column(Modifier.fillMaxSize()) {
        // Live building + 교환비 meter + wobbling estimate (chest 🔒 closed)
        Column(
            Modifier
                .weight(if (dense) 0.42f else 0.48f)
                .padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                BuildingFacade(
                    building = building,
                    litFraction = litFraction,
                    isRefund = isRefund,
                    modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight()
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("교환비 💱", style = MaterialTheme.typography.labelLarge, color = ConstructionColors.InkSoft)
                HSpace(Design.spacing.sm)
                Box(Modifier.weight(1f)) { ExchangeMeter(ratio = result.proportionRatio) }
                HSpace(Design.spacing.sm)
                Text(
                    Format.percent(result.proportionRatio),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (result.proportionRatio >= 1.0) ConstructionColors.Gold else ConstructionColors.InkSoft,
                    fontWeight = FontWeight.SemiBold
                )
            }
            VSpace(Design.spacing.sm)
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(ConstructionColors.Surface, RoundedCornerShape(Design.radii.lg))
                    .padding(Design.spacing.md),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isRefund) "분담금 ≈ ${Format.eok(kotlin.math.abs(result.burden))} 돌려받을지도? 🔒"
                    else "분담금 ≈ ${Format.eok(result.burden)} 낼지도? 🔒",
                    style = MaterialTheme.typography.titleMedium,
                    color = ConstructionColors.InkSoft
                )
            }
        }

        HairlineDivider()

        // Lever region — reskinned sliders + 확정.
        Column(
            Modifier
                .weight(if (dense) 0.58f else 0.52f)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.sm)
        ) {
            Text("레버를 움직여 봐요 🎚️", style = MaterialTheme.typography.titleLarge, color = ConstructionColors.Ink, fontWeight = FontWeight.SemiBold)
            Text("숫자가 살아 움직여요 — 건물이 같이 반응해요", style = MaterialTheme.typography.bodySmall, color = ConstructionColors.InkMuted)

            ConstructionSlider(
                title = "🧱 공사비",
                sub = "싸게 ◄ ► 비싸게 · 기준 평당 ${Format.manwon(d.base.pyeongConstructionCost)}",
                value = factorT,
                valueDisplay = Format.manwon(d.base.pyeongConstructionCost * factorT),
                onValueChange = onFactorT,
                range = 0.6..1.4,
                markers = peers.map { PeerMarker(peerShort(it), it.base.pyeongConstructionCost / d.base.pyeongConstructionCost) },
                compact = dense
            )
            ConstructionSlider(
                title = "🏠 식구 가격",
                sub = "조합원분양가 · 기준 ${Format.eok(d.base.unionPrice84)}",
                value = factorL,
                valueDisplay = Format.eok(d.base.unionPrice84 * factorL),
                onValueChange = onFactorL,
                range = 0.6..1.4,
                markers = peers.map { PeerMarker(peerShort(it), it.base.unionPrice84 / d.base.unionPrice84) },
                compact = dense
            )
            ConstructionSlider(
                title = "📈 바깥 분양",
                sub = "일반분양가 · 기준 ${Format.eok(d.base.publicPrice84)}",
                value = factorJ,
                valueDisplay = Format.eok(d.base.publicPrice84 * factorJ),
                onValueChange = onFactorJ,
                range = 0.6..1.4,
                markers = peers.map { PeerMarker(peerShort(it), it.base.publicPrice84 / d.base.publicPrice84) },
                compact = dense
            )
            if (dense) {
                val atMin = d.base.estimatedNewPrice84 * 0.6
                val atMax = d.base.estimatedNewPrice84 * 1.4
                ConstructionSlider(
                    title = "✨ 미래 몸값",
                    sub = "신축 예상가 · 기준 ${Format.eok(d.base.estimatedNewPrice84)}",
                    value = atOverride,
                    valueDisplay = Format.eok(atOverride),
                    onValueChange = onAt,
                    range = atMin..atMax,
                    markers = peers.map { PeerMarker(peerShort(it), it.base.estimatedNewPrice84) },
                    compact = true
                )
            }
            VSpace(Design.spacing.sm)
            Row(horizontalArrangement = Arrangement.spacedBy(Design.spacing.sm)) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Design.radii.xl),
                    border = BorderStroke(1.dp, ConstructionColors.Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ConstructionColors.InkSoft)
                ) { Text("되돌리기 ↩️") }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(Design.radii.xl),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConstructionColors.Gold,
                        contentColor = ConstructionColors.NavyDeep
                    )
                ) { Text("확정 🥁", fontWeight = FontWeight.Bold) }
            }
            VSpace(Design.spacing.sm)
        }
    }
}

/** Chest-open stage: the 분담금 reveal — count-up, celebration/empathy branch, follow-through. */
@Composable
private fun RevealStage(
    d: District, t: TypeInfo, question: Question, result: com.jaejal.reconstruction.data.SimulationResult,
    building: com.jaejal.reconstruction.design.Building, isRefund: Boolean,
    stars: Int, theatrical: Boolean, revealKey: Int,
    onRetry: () -> Unit, onBookmark: () -> Unit, isBookmarked: Boolean, onRanking: () -> Unit
) {
    val scroll = rememberScrollState()
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(scroll)
                .padding(Design.spacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // mascot + building topping out
            JaejalMascot(
                mood = if (isRefund) MascotMood.Celebrating else MascotMood.Worried,
                size = 72.dp
            )
            VSpace(Design.spacing.sm)
            Text(
                if (isRefund) "잭팟! 돈을 돌려받아요! 🎉" else "퀘스트 클리어! 💪",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isRefund) ConstructionColors.Gain else ConstructionColors.Gold,
                fontWeight = FontWeight.Bold
            )
            VSpace(Design.spacing.lg)

            TrustCard(modifier = Modifier.fillMaxWidth(), emphasized = true) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (isRefund) "내 분담금 (환급)" else "내 분담금",
                        style = MaterialTheme.typography.labelLarge, color = ConstructionColors.InkMuted
                    )
                    VSpace(Design.spacing.xs)
                    CountUpNumber(
                        targetMillionWon = kotlin.math.abs(result.burden),
                        format = { Format.eok(it) },
                        tone = if (isRefund) ChipTone.Gain else ChipTone.Gold,
                        key = revealKey,
                        animate = theatrical && isAnimationEnabled
                    )
                    VSpace(Design.spacing.sm)
                    StarRow(stars)
                    VSpace(Design.spacing.md)
                    HairlineDivider()
                    VSpace(Design.spacing.sm)
                    // The teaching chain
                    Text(
                        "🪙 내 헌 집 × 💱 교환비 = 💰 내 몫 · 새 집 값 − 내 몫 = 분담금!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ConstructionColors.InkSoft,
                        textAlign = TextAlign.Center
                    )
                    if (!isRefund) {
                        VSpace(Design.spacing.sm)
                        Text(
                            "${Format.eok(result.burden)}은 한 번에 내는 게 아니라 단계별로 나눠 내요. 미리 알았으니 이제 계획을 세울 수 있어요 🫶",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ConstructionColors.InkSoft,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Q2 silver-lining: the investment score flips a payment into a possible win.
            if (question == Question.Q2) {
                VSpace(Design.spacing.md)
                TrustCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ToneChip(text = "투자 점수", tone = ChipTone.Gold)
                            HSpace(Design.spacing.sm)
                            Text(
                                if (result.margin >= 0) "남는 장사예요! 🏆" else "지금은 빠듯한 그림이에요 ⚖️",
                                style = MaterialTheme.typography.labelLarge, color = ConstructionColors.InkSoft
                            )
                        }
                        VSpace(Design.spacing.sm)
                        HeroNumber(
                            value = Format.percent(result.roi),
                            tone = if (result.roi >= 0) ChipTone.Gain else ChipTone.Loss,
                            label = "최종 점수 (수익률)"
                        )
                        VSpace(Design.spacing.sm)
                        HairlineDivider()
                        VSpace(Design.spacing.xs)
                        StatLine("내가 쏟은 돈 (총투자금)", Format.eok(result.totalInvest))
                        StatLine("남는 돈 (마진)", Format.eok(result.margin),
                            tone = if (result.margin >= 0) ChipTone.Gain else ChipTone.Loss)
                    }
                }
            }

            VSpace(Design.spacing.lg)
            Row(horizontalArrangement = Arrangement.spacedBy(Design.spacing.sm)) {
                OutlinedButton(
                    onClick = onBookmark,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Design.radii.xl),
                    border = BorderStroke(1.dp, ConstructionColors.Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ConstructionColors.Gold)
                ) { Text(if (isBookmarked) "저장됨 🔖" else "저장 🔖") }
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Design.radii.xl),
                    border = BorderStroke(1.dp, ConstructionColors.Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ConstructionColors.InkSoft)
                ) { Text("다시 🔁") }
                Button(
                    onClick = onRanking,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Design.radii.xl),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConstructionColors.Gold,
                        contentColor = ConstructionColors.NavyDeep
                    )
                ) { Text("랭킹 🏆", fontWeight = FontWeight.SemiBold) }
            }
            VSpace(Design.spacing.xxl)
        }

        // Confetti overlay — refund only, one-shot.
        ConfettiBurst(play = isRefund, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun StarRow(stars: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            Text(
                if (i < stars) "⭐" else "☆",
                style = MaterialTheme.typography.headlineSmall,
                color = if (i < stars) ConstructionColors.Gold else ConstructionColors.InkMuted
            )
        }
    }
}
