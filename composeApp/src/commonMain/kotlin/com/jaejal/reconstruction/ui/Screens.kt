package com.jaejal.reconstruction.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.jaejal.reconstruction.calc.Engine
import com.jaejal.reconstruction.data.District
import com.jaejal.reconstruction.data.RankedDistrict
import com.jaejal.reconstruction.data.Repository
import com.jaejal.reconstruction.data.TypeInfo
import com.jaejal.reconstruction.design.BookmarkHeart
import com.jaejal.reconstruction.design.BottomNavItem
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
                    fadeIn(tween(180)) togetherWith fadeOut(tween(120))
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
            CircularProgressIndicator(color = ConstructionColors.Navy, strokeWidth = 2.dp)
            VSpace(Design.spacing.md)
            Text("데이터를 불러오는 중입니다…", color = ConstructionColors.InkMuted)
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

// ============= Top bar shared by detail screens =============

@Composable
private fun DetailTopBar(
    title: String,
    breadcrumb: List<String>,
    onBack: () -> Unit
) {
    val barShape = RoundedCornerShape(bottomStart = Design.radii.lg, bottomEnd = Design.radii.lg)
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = barShape,
                    ambientColor = ConstructionColors.NavyDeep.copy(alpha = 0.05f),
                    spotColor = ConstructionColors.NavyDeep.copy(alpha = 0.07f),
                    clip = false
                )
                .clip(barShape)
                .background(
                    Brush.verticalGradient(
                        listOf(ConstructionColors.PaperAlt, ConstructionColors.Paper)
                    )
                )
                // Fill under the status bar, then pad content below it.
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Column(Modifier.padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.md)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        if (breadcrumb.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                breadcrumb.forEachIndexed { i, p ->
                                    if (i > 0) Text(
                                        " · ",
                                        color = ConstructionColors.InkMuted,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        p,
                                        color = if (i == breadcrumb.lastIndex) ConstructionColors.NavyDeep else ConstructionColors.InkSoft,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (i == breadcrumb.lastIndex) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                }
                            }
                            VSpace(2.dp)
                        }
                        Text(
                            title,
                            style = MaterialTheme.typography.displaySmall,
                            color = ConstructionColors.Ink
                        )
                    }
                    TextButton(onClick = onBack) {
                        Text("← 뒤로", color = ConstructionColors.Navy, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ============= Home (curated ranking) =============

@Composable
private fun RankingScreen(state: AppState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Design.spacing.xxl)
    ) {
        item { RankingHero() }
        item {
            SectionHeader(
                title = "단지 랭킹",
                sub = "종전자산 최소 타입의 기준 수익률 기준 내림차순"
            )
        }
        itemsIndexed(state.ranked) { i, ranked ->
            Box(Modifier.padding(horizontal = Design.spacing.gutter, vertical = 6.dp)) {
                RankCard(rank = i + 1, ranked = ranked) { state.openDistrict(ranked.district) }
            }
        }
        item {
            SectionHeader(
                title = "공개 예정",
                sub = "곧 추가될 단지입니다",
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
private fun RankingHero() {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(ConstructionColors.NavyDeep, ConstructionColors.Navy)
                )
            )
            // Bleed the gradient under the status bar, then pad content below it.
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.lg)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ConstructionLogo(size = 32.dp, onDark = true)
                HSpace(Design.spacing.sm)
                Text(
                    "재건축 랭커",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            VSpace(4.dp)
            Text(
                "공개 고시문 기반 분담금 · 매도판단 시뮬레이터",
                style = MaterialTheme.typography.bodySmall,
                color = ConstructionColors.NavyTint
            )
            VSpace(Design.spacing.md)
            Row {
                StatBadge("단지", "5"); HSpace(Design.spacing.sm)
                StatBadge("공개 예정", "2"); HSpace(Design.spacing.sm)
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
private fun RankCard(rank: Int, ranked: RankedDistrict, onClick: () -> Unit) {
    val gold = rank == 1
    TrustCard(
        modifier = Modifier.fillMaxWidth(),
        emphasized = gold,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RankBadge(position = rank)
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
                    ToneChip(text = "${ranked.district.types.size}개 타입", tone = ChipTone.Neutral)
                    HSpace(6.dp)
                    ToneChip(text = "KB ${Format.eok(ranked.headlineType.kbPrice)}", tone = ChipTone.Primary)
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
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Design.spacing.gutter,
                end = Design.spacing.gutter,
                top = Design.spacing.md,
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
                                color = ConstructionColors.NavyDeep
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
                items(types.chunked(2)) { row ->
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
    val result = Engine.calculate(district.base, type)
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
                StatLine("KB시세", Format.eok(type.kbPrice), tone = ChipTone.Primary)
                StatLine("분담금", Format.burdenWithRefund(result.burden), tone = burdenTone)
                StatLine("기준 수익률", Format.percent(result.roi), tone = roiTone)
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
            title = "어떤 질문을 풀어드릴까요?",
            breadcrumb = listOf(d.name, formatTypeLabel(d, t)),
            onBack = { state.pop() }
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(Design.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(Design.spacing.lg)
        ) {
            QuestionHeroCard(1, Question.Q1.title,
                "공사비·조합원분양가·일반분양가가 움직이면 내 분담금이 얼마가 될지 슬라이더로 살펴봅니다.",
                ChipTone.Primary) { state.openQuestion(d, t, Question.Q1) }
            QuestionHeroCard(2, Question.Q2.title,
                "KB시세에 분담금을 더한 총투자금이 신축 완공 시 예상가액보다 낮은지 비교합니다.",
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
                    if (tone == ChipTone.Primary) "분담금 시뮬레이션" else "매도·보유 판단",
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("시작하기", style = MaterialTheme.typography.titleSmall, color = ConstructionColors.Navy, fontWeight = FontWeight.SemiBold)
                HSpace(6.dp)
                Text("→", color = ConstructionColors.Navy, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DisclaimerScreen(state: AppState) {
    val scroll = rememberScrollState()
    Column(Modifier.fillMaxSize()) {
        DetailTopBar(title = "잠시 안내드립니다", breadcrumb = listOf("면책 동의"), onBack = { state.pop() })
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(Design.spacing.gutter)
        ) {
            Text("시뮬레이션 결과 활용에 관한 안내", style = MaterialTheme.typography.headlineLarge, color = ConstructionColors.Ink)
            VSpace(Design.spacing.sm)
            Text(
                "이 도구가 어떤 한계 안에서 도움이 되는지 먼저 짚어드립니다.",
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
                .padding(Design.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Design.spacing.sm)
        ) {
            OutlinedButton(
                onClick = { state.pop() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(Design.radii.xl),
                border = BorderStroke(1.dp, ConstructionColors.Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ConstructionColors.Navy)
            ) { Text("뒤로") }
            Button(
                onClick = { state.acceptDisclaimer() },
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(Design.radii.xl),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ConstructionColors.Navy,
                    contentColor = ConstructionColors.InkOnDark
                )
            ) { Text("동의하고 계속합니다", fontWeight = FontWeight.SemiBold) }
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
            Text(index, color = ConstructionColors.NavyDeep, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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

    Column(Modifier.fillMaxSize()) {
        DetailTopBar(
            title = if (question == Question.Q1) "분담금 시뮬레이션" else "매도·보유 판단",
            breadcrumb = listOf(d.name, formatTypeLabel(d, t)),
            onBack = { state.pop() }
        )

        // Results region — compacted. Q1 = one card; Q2 = two side-by-side cards.
        // Retains verticalScroll only as a safety net on very short viewports.
        Column(
            Modifier
                .weight(if (question == Question.Q2) 0.35f else 0.45f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.md)
        ) {
            if (question == Question.Q1) {
                // Q1: single 분담금 card. Read order 종전자산 → 권리가액 → 전용84 조합원분양가
                // (권리가액 = 종전자산 × 비례율, so 종전자산 sits above 권리가액).
                TrustCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ToneChip(text = "분담금", tone = ChipTone.Primary)
                            HSpace(Design.spacing.sm)
                            Text("비례율 ${Format.percent(result.proportionRatio)}", style = MaterialTheme.typography.labelLarge, color = ConstructionColors.InkMuted)
                        }
                        VSpace(Design.spacing.sm)
                        HeroNumber(
                            value = Format.burdenWithRefund(result.burden),
                            tone = if (result.burden < 0) ChipTone.Gain else ChipTone.Loss,
                            label = if (result.burden < 0) "환급 받습니다" else "추가로 납부합니다"
                        )
                        VSpace(Design.spacing.md)
                        HairlineDivider()
                        VSpace(Design.spacing.sm)
                        StatLine("종전자산 추정가액", Format.eok(t.priorUnitPrice))
                        StatLine("권리가액", Format.eok(result.rights))
                        StatLine("전용84 조합원분양가", Format.eok(result.unionPrice84))
                    }
                }
            } else {
                // Q2: 분담금 + 투자수익 side-by-side to free vertical room for 4 sliders.
                Row(horizontalArrangement = Arrangement.spacedBy(Design.spacing.md)) {
                    TrustCard(
                        modifier = Modifier.weight(1f),
                        padding = PaddingValues(Design.spacing.md)
                    ) {
                        Column {
                            ToneChip(text = "분담금", tone = ChipTone.Primary)
                            VSpace(Design.spacing.sm)
                            Text("비례율 ${Format.percent(result.proportionRatio)}", style = MaterialTheme.typography.labelMedium, color = ConstructionColors.InkMuted)
                            HeroNumber(
                                value = Format.burdenWithRefund(result.burden),
                                tone = if (result.burden < 0) ChipTone.Gain else ChipTone.Loss
                            )
                            VSpace(Design.spacing.sm)
                            HairlineDivider()
                            VSpace(Design.spacing.xs)
                            StatLine("종전자산", Format.eok(t.priorUnitPrice))
                            StatLine("권리가액", Format.eok(result.rights))
                        }
                    }
                    TrustCard(
                        modifier = Modifier.weight(1f),
                        padding = PaddingValues(Design.spacing.md)
                    ) {
                        Column {
                            ToneChip(text = "투자 수익", tone = ChipTone.Gold)
                            VSpace(Design.spacing.sm)
                            Text("총투자금 vs 신축예상", style = MaterialTheme.typography.labelMedium, color = ConstructionColors.InkMuted)
                            HeroNumber(
                                value = Format.percent(result.roi),
                                tone = if (result.roi >= 0) ChipTone.Gain else ChipTone.Loss,
                                label = "수익률"
                            )
                            VSpace(Design.spacing.sm)
                            HairlineDivider()
                            VSpace(Design.spacing.xs)
                            StatLine("총투자금", Format.eok(result.totalInvest))
                            StatLine("마진", Format.eok(result.margin),
                                tone = if (result.margin >= 0) ChipTone.Gain else ChipTone.Loss)
                        }
                    }
                }
            }
        }

        HairlineDivider()

        // Slider region — NO internal scroll. All sliders fit at once (item 7/8/9).
        // Q2 has 4 sliders, so it runs in compact mode + a larger weight to fit a phone viewport.
        val dense = question == Question.Q2
        Column(
            Modifier
                .weight(if (dense) 0.72f else 0.55f)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.md)
        ) {
            Text("변수를 움직여 보세요", style = MaterialTheme.typography.titleLarge, color = ConstructionColors.NavyDeep, fontWeight = FontWeight.SemiBold)
            Text("기준 × 0.6 ~ 1.4 범위에서 즉시 반영됩니다", style = MaterialTheme.typography.bodySmall, color = ConstructionColors.InkMuted)

            ConstructionSlider(
                title = "공사비",
                sub = "기준 평당 ${Format.manwon(d.base.pyeongConstructionCost)}",
                value = factorT,
                valueDisplay = Format.manwon(d.base.pyeongConstructionCost * factorT),
                onValueChange = { factorT = it },
                range = 0.6..1.4,
                markers = peers.map { PeerMarker(peerShort(it), it.base.pyeongConstructionCost / d.base.pyeongConstructionCost) },
                compact = dense
            )
            ConstructionSlider(
                title = "국민평형 조합원분양가 (전용84)",
                sub = "기준 ${Format.eok(d.base.unionPrice84)}",
                value = factorL,
                valueDisplay = Format.eok(d.base.unionPrice84 * factorL),
                onValueChange = { factorL = it },
                range = 0.6..1.4,
                markers = peers.map { PeerMarker(peerShort(it), it.base.unionPrice84 / d.base.unionPrice84) },
                compact = dense
            )
            ConstructionSlider(
                title = "국민평형 일반분양가 (전용84)",
                sub = "기준 ${Format.eok(d.base.publicPrice84)}",
                value = factorJ,
                valueDisplay = Format.eok(d.base.publicPrice84 * factorJ),
                onValueChange = { factorJ = it },
                range = 0.6..1.4,
                markers = peers.map { PeerMarker(peerShort(it), it.base.publicPrice84 / d.base.publicPrice84) },
                compact = dense
            )
            if (dense) {
                val atMin = d.base.estimatedNewPrice84 * 0.6
                val atMax = d.base.estimatedNewPrice84 * 1.4
                ConstructionSlider(
                    title = "신축 예상가",
                    sub = "기준 ${Format.eok(d.base.estimatedNewPrice84)}",
                    value = atOverride,
                    valueDisplay = Format.eok(atOverride),
                    onValueChange = { atOverride = it },
                    range = atMin..atMax,
                    markers = peers.map { PeerMarker(peerShort(it), it.base.estimatedNewPrice84) },
                    compact = true
                )
            }
            VSpace(if (dense) Design.spacing.xs else Design.spacing.sm)
            ActionRow(
                onReset = {
                    factorT = 1.0; factorL = 1.0; factorJ = 1.0
                    atOverride = d.base.estimatedNewPrice84
                }
            )
        }
    }
}

@Composable
private fun ActionRow(onReset: () -> Unit) {
    // Consolidated to the single reset action. The "고쳐주세요" / "우리 단지도"
    // entries live on the My tab (TabScreens.MyScreen) — keeping them here too
    // was redundant and cost a row of vertical space in the slider panel.
    FilledTonalButton(
        onClick = onReset,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Design.radii.xl),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = ConstructionColors.NavyTint,
            contentColor = ConstructionColors.NavyDeep
        )
    ) {
        Text("기본값으로", fontWeight = FontWeight.SemiBold)
    }
}
