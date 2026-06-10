package com.jaejal.reconstruction.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jaejal.reconstruction.data.District
import com.jaejal.reconstruction.data.RankedDistrict
import com.jaejal.reconstruction.data.Repository
import com.jaejal.reconstruction.data.TypeInfo

// ============ Tabs ============

enum class Tab(val title: String) {
    Home("홈"),
    Districts("단지"),
    Bookmarks("북마크"),
    My("마이")
}

// ============ Per-tab routes ============

/** Stack-able destinations within a tab. Top-level tabs themselves are not routes. */
sealed interface Route {
    /** Home / Districts default state — list view. */
    data object RankingList : Route                       // Home root
    data object DistrictList : Route                      // Districts root
    data object BookmarkList : Route                      // Bookmarks root
    data object MyProfile : Route                         // My root

    data class TypeSelect(val districtName: String) : Route
    data class QuestionSelect(val districtName: String, val typeIndex: Int) : Route
    data class Disclaimer(val districtName: String, val typeIndex: Int, val question: Question) : Route
    data class Simulation(val districtName: String, val typeIndex: Int, val question: Question) : Route
}

enum class Question(val title: String, val short: String) {
    Q1("나 결국 얼마 더 내야 하는 거야?", "분담금 시뮬레이션"),
    Q2("지금 팔아야 하나?", "매도·보유 판단")
}

/** Routes that should hide the bottom nav bar. */
fun Route.hidesBottomNav(): Boolean = when (this) {
    is Route.Disclaimer, is Route.Simulation -> true
    else -> false
}

// ============ State container ============

/** Per-district quest progress (in-memory v1; // TODO persist via DataStore). */
data class DistrictProgress(
    val bestStars: Int = 0,
    val bestRoi: Double? = null,
    val cleared: Boolean = false
)

class AppState {
    var currentTab by mutableStateOf(Tab.Home)
        private set
    var districts by mutableStateOf<List<District>>(emptyList())
    var ranked by mutableStateOf<List<RankedDistrict>>(emptyList())
    var hasAcceptedDisclaimer by mutableStateOf(false)
    var loading by mutableStateOf(true)
    var loadError by mutableStateOf<String?>(null)

    /** Saved 평형 store (in-memory; see [BookmarkStore]). */
    val bookmarks: BookmarkStore = BookmarkStore(clock = { nowMs() })

    // ----- Gamification progress (in-memory; resets each launch) -----
    val districtProgress = mutableStateMapOf<String, DistrictProgress>()
    var totalStars by mutableStateOf(0)
        private set
    var districtsCleared by mutableStateOf(0)
        private set

    /** Stars earned by a finished quest, derived from the ALREADY-COMPUTED result.
     *  Q2 uses ROI; Q1 (no ROI) uses the refund/비례율 outcome so Q1-only players still climb. */
    fun starsFor(question: Question, roi: Double, burden: Double): Int = when (question) {
        Question.Q2 -> when {
            roi >= 0.40 -> 3
            roi >= 0.15 -> 2
            else -> 1
        }
        Question.Q1 -> when {
            burden < 0 -> 3            // refund — best outcome
            burden < 1000.0 -> 2       // under ~10억 to pay
            else -> 1
        }
    }

    /** Record a finished reveal. Takes the already-computed result so AppState never imports
     *  the engine (avoids a see-vs-store desync). Keeps only the best run per district. */
    fun recordReveal(districtName: String, question: Question, roi: Double, burden: Double) {
        val stars = starsFor(question, roi, burden)
        val prev = districtProgress[districtName] ?: DistrictProgress()
        val wasClearedBefore = prev.cleared
        val next = DistrictProgress(
            bestStars = maxOf(prev.bestStars, stars),
            bestRoi = listOfNotNull(prev.bestRoi, roi).maxOrNull(),
            cleared = true
        )
        districtProgress[districtName] = next
        // recompute aggregates from the map so they stay consistent
        totalStars = districtProgress.values.sumOf { it.bestStars }
        if (!wasClearedBefore) districtsCleared = districtProgress.values.count { it.cleared }
    }

    /** Per-tab back stack. The root route is always at index 0. */
    private val stacks: MutableMap<Tab, SnapshotStateList<Route>> = mutableStateMapOf<Tab, SnapshotStateList<Route>>().also {
        it[Tab.Home] = mutableStateListOf(Route.RankingList)
        it[Tab.Districts] = mutableStateListOf(Route.DistrictList)
        it[Tab.Bookmarks] = mutableStateListOf(Route.BookmarkList)
        it[Tab.My] = mutableStateListOf(Route.MyProfile)
    }

    val currentRoute: Route get() = stacks[currentTab]!!.last()

    suspend fun load() {
        try {
            val data = Repository.loadDistricts()
            districts = data
            ranked = Repository.rank(data)
            loading = false
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러오지 못했습니다"
            loading = false
        }
    }

    fun selectTab(tab: Tab) {
        if (tab == currentTab) {
            // Tap on already-active tab → pop to that tab's root
            stacks[tab]!!.apply { if (size > 1) { while (size > 1) removeAt(size - 1) } }
        } else {
            currentTab = tab
        }
    }

    fun findDistrict(name: String): District? = districts.firstOrNull { it.name == name }
    fun findType(district: District, index: Int): TypeInfo? = district.types.getOrNull(index)

    fun push(route: Route) {
        stacks[currentTab]!!.add(route)
    }

    fun pop() {
        val s = stacks[currentTab]!!
        if (s.size > 1) s.removeAt(s.size - 1)
    }

    fun popToRoot() {
        val s = stacks[currentTab]!!
        while (s.size > 1) s.removeAt(s.size - 1)
    }

    /** True when this tab is showing its root list view. */
    fun atRootOfCurrentTab(): Boolean = stacks[currentTab]!!.size <= 1

    // ----- High-level flow actions -----

    fun openDistrict(d: District) {
        push(Route.TypeSelect(d.name))
    }

    fun openType(d: District, t: TypeInfo) {
        val idx = d.types.indexOf(t)
        push(Route.QuestionSelect(d.name, idx))
    }

    fun openQuestion(d: District, t: TypeInfo, q: Question) {
        val idx = d.types.indexOf(t)
        if (hasAcceptedDisclaimer) {
            startSim(d, idx, q)
        } else {
            push(Route.Disclaimer(d.name, idx, q))
        }
    }

    fun acceptDisclaimer() {
        hasAcceptedDisclaimer = true
        val cur = currentRoute as? Route.Disclaimer ?: return
        // replace disclaimer route with Simulation
        pop()
        startSim(findDistrict(cur.districtName)!!, cur.typeIndex, cur.question)
    }

    private fun startSim(d: District, idx: Int, q: Question) {
        push(Route.Simulation(d.name, idx, q))
    }

    // ----- Bookmarks -----

    fun isBookmarked(d: District, t: TypeInfo): Boolean =
        bookmarks.isBookmarked(d.name, t.label)

    fun toggleBookmark(d: District, t: TypeInfo): Boolean =
        bookmarks.toggle(d.name, t.label)

    /** From the bookmark list — open that 평형's question-select screen. */
    fun openBookmark(b: Bookmark) {
        val d = findDistrict(b.districtName) ?: return
        val idx = d.types.indexOfFirst { it.label == b.typeLabel }.takeIf { it >= 0 } ?: return
        currentTab = Tab.Bookmarks
        push(Route.QuestionSelect(d.name, idx))
    }
}

internal expect fun nowMs(): Long

@Composable
fun rememberAppState(): AppState {
    val state = remember { AppState() }
    LaunchedEffect(Unit) { state.load() }
    return state
}
