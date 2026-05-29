package com.jaejal.reconstruction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaejal.reconstruction.calc.Engine
import com.jaejal.reconstruction.data.District
import com.jaejal.reconstruction.data.RankedDistrict
import com.jaejal.reconstruction.data.Repository
import com.jaejal.reconstruction.design.ChipTone
import com.jaejal.reconstruction.design.ConstructionColors
import com.jaejal.reconstruction.design.ConstructionIcons
import com.jaejal.reconstruction.design.Design
import com.jaejal.reconstruction.design.HSpace
import com.jaejal.reconstruction.design.HairlineDivider
import com.jaejal.reconstruction.design.QuietCard
import com.jaejal.reconstruction.design.RankBadge
import com.jaejal.reconstruction.design.SectionHeader
import com.jaejal.reconstruction.design.ToneChip
import com.jaejal.reconstruction.design.TrustCard
import com.jaejal.reconstruction.design.VSpace
import com.jaejal.reconstruction.format.Format
import androidx.compose.material3.Icon

// ============= Districts tab — full searchable list =============

@Composable
fun DistrictListScreen(state: AppState) {
    var query by remember { mutableStateOf("") }
    val all = remember(state.districts) {
        state.districts.map { d ->
            val cheapest = d.types.minBy { it.priorUnitPrice }
            val result = Engine.calculate(d.base, cheapest)
            RankedDistrict(d, cheapest, result.roi)
        }.sortedByDescending { it.headlineRoi }
    }
    val filtered = remember(query, all) {
        if (query.isBlank()) all
        else all.filter { it.district.name.contains(query) || it.district.address.contains(query) }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.lg)
        ) {
            Text(
                "단지",
                style = MaterialTheme.typography.displaySmall,
                color = ConstructionColors.Ink
            )
            Text(
                "${all.size}개 단지 + ${Repository.blurredDistricts.size}개 공개 예정",
                style = MaterialTheme.typography.bodyMedium,
                color = ConstructionColors.InkSoft
            )
            VSpace(Design.spacing.md)
            SearchField(query = query, onChange = { query = it })
        }
        HairlineDivider()
        if (filtered.isEmpty()) {
            EmptyState(
                title = "검색 결과가 없습니다",
                sub = "다른 단지명 또는 주소를 시도해 보세요"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = Design.spacing.gutter, vertical = Design.spacing.md),
                verticalArrangement = Arrangement.spacedBy(Design.spacing.sm)
            ) {
                itemsIndexed(filtered) { i, ranked ->
                    DistrictListRow(rank = i + 1, ranked = ranked) { state.openDistrict(ranked.district) }
                }
                items(Repository.blurredDistricts) { b ->
                    QuietCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(ConstructionColors.MapMist, CircleShape)
                            )
                            HSpace(Design.spacing.md)
                            Column(Modifier.weight(1f)) {
                                Text(b.name, style = MaterialTheme.typography.titleMedium, color = ConstructionColors.InkSoft)
                                Text(b.location, style = MaterialTheme.typography.labelMedium, color = ConstructionColors.InkMuted)
                            }
                            ToneChip(text = "준비중", tone = ChipTone.Neutral)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(Design.radii.md))
            .background(Color.Transparent)
            .padding(horizontal = Design.spacing.md, vertical = Design.spacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ConstructionIcons.Search,
                contentDescription = null,
                tint = ConstructionColors.InkMuted,
                modifier = Modifier.size(18.dp)
            )
            HSpace(Design.spacing.sm)
            Box(Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = ConstructionColors.Ink),
                    cursorBrush = SolidColor(ConstructionColors.Navy),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (query.isEmpty()) {
                    Text(
                        "단지명 또는 주소 검색",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ConstructionColors.InkMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun DistrictListRow(rank: Int, ranked: RankedDistrict, onClick: () -> Unit) {
    QuietCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RankBadge(position = rank)
            HSpace(Design.spacing.md)
            Column(Modifier.weight(1f)) {
                Text(ranked.district.name, style = MaterialTheme.typography.titleLarge, color = ConstructionColors.Ink, fontWeight = FontWeight.SemiBold)
                Text(
                    ranked.district.shortLocation,
                    style = MaterialTheme.typography.labelMedium,
                    color = ConstructionColors.InkMuted
                )
            }
            Text(
                Format.percent(ranked.headlineRoi),
                style = MaterialTheme.typography.titleLarge,
                color = if (ranked.headlineRoi >= 0) ConstructionColors.Gain else ConstructionColors.Loss,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============= Sims tab — history =============

@Composable
fun SimsHistoryScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.lg)
        ) {
            Text("시뮬레이션", style = MaterialTheme.typography.displaySmall, color = ConstructionColors.Ink)
            Text(
                "최근에 살펴본 단지·평형 기록입니다",
                style = MaterialTheme.typography.bodyMedium,
                color = ConstructionColors.InkSoft
            )
        }
        HairlineDivider()

        if (state.simHistory.isEmpty()) {
            EmptyState(
                title = "아직 시뮬레이션 기록이 없습니다",
                sub = "단지를 골라 분담금·매도판단 시뮬레이션을 시작해 보세요",
                ctaLabel = "단지 보기",
                onCta = { state.selectTab(Tab.Districts) }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = Design.spacing.gutter, vertical = Design.spacing.md),
                verticalArrangement = Arrangement.spacedBy(Design.spacing.sm)
            ) {
                items(state.simHistory) { rec ->
                    QuietCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { state.openHistory(rec) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(ConstructionColors.NavyTint, RoundedCornerShape(Design.radii.pill)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ConstructionIcons.Clock,
                                    contentDescription = null,
                                    tint = ConstructionColors.Navy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            HSpace(Design.spacing.md)
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${rec.districtName} · ${rec.typeLabel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ConstructionColors.Ink,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    rec.question.short,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ConstructionColors.InkSoft
                                )
                            }
                            ToneChip(
                                text = if (rec.question == Question.Q1) "Q1" else "Q2",
                                tone = if (rec.question == Question.Q1) ChipTone.Primary else ChipTone.Gold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============= My tab — profile / feedback =============

@Composable
fun MyScreen(state: AppState) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.padding(horizontal = Design.spacing.gutter, vertical = Design.spacing.lg)
        ) {
            Text("마이", style = MaterialTheme.typography.displaySmall, color = ConstructionColors.Ink)
            Text(
                "이 서비스는 사용자 피드백으로 함께 만들어집니다",
                style = MaterialTheme.typography.bodyMedium,
                color = ConstructionColors.InkSoft
            )
        }
        HairlineDivider()
        Column(
            Modifier
                .fillMaxSize()
                .padding(Design.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(Design.spacing.md)
        ) {
            TrustCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "피드백 보내기",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ConstructionColors.Ink
                    )
                    VSpace(Design.spacing.sm)
                    Text(
                        "수치 또는 단지에 오류가 있다고 판단되시면 알려주세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ConstructionColors.InkSoft
                    )
                    VSpace(Design.spacing.md)
                    Row(horizontalArrangement = Arrangement.spacedBy(Design.spacing.sm)) {
                        Button(
                            onClick = { /* stub */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ConstructionColors.NavyTint,
                                contentColor = ConstructionColors.NavyDeep
                            )
                        ) { Text("고쳐주세요", fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = { /* stub */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ConstructionColors.CopperSoft,
                                contentColor = ConstructionColors.Loss
                            )
                        ) { Text("우리 단지도", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }

            TrustCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "이 도구에 대하여",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ConstructionColors.Ink
                    )
                    VSpace(Design.spacing.sm)
                    InfoRow("버전", "0.1.0")
                    InfoRow("데이터 기준일", "2026-05-18")
                    InfoRow("폰트", "Noto Serif KR · Noto Sans KR")
                    InfoRow("저작권", "© 2026 재잘")
                }
            }

            TrustCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "면책조항 다시 보기",
                        style = MaterialTheme.typography.titleLarge,
                        color = ConstructionColors.Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                    VSpace(4.dp)
                    Text(
                        "본 도구의 출력값은 추정치이며 의사결정의 단독 근거가 될 수 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ConstructionColors.InkSoft
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ConstructionColors.InkSoft)
        Text(value, style = MaterialTheme.typography.titleMedium, color = ConstructionColors.Ink, fontWeight = FontWeight.SemiBold)
    }
}

// ============= Shared empty state =============

@Composable
fun EmptyState(
    title: String,
    sub: String,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Design.spacing.xxl, vertical = Design.spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(72.dp)
                .background(ConstructionColors.NavyTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ConstructionIcons.Clock,
                contentDescription = null,
                tint = ConstructionColors.Navy,
                modifier = Modifier.size(32.dp)
            )
        }
        VSpace(Design.spacing.lg)
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = ConstructionColors.Ink,
            fontWeight = FontWeight.SemiBold
        )
        VSpace(Design.spacing.xs)
        Text(
            sub,
            style = MaterialTheme.typography.bodyMedium,
            color = ConstructionColors.InkSoft,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (ctaLabel != null && onCta != null) {
            VSpace(Design.spacing.lg)
            Button(
                onClick = onCta,
                colors = ButtonDefaults.buttonColors(containerColor = ConstructionColors.Navy)
            ) { Text(ctaLabel, color = Color.White, fontWeight = FontWeight.SemiBold) }
        }
    }
}
