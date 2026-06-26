# 재잘 (Jaejal) — The 분담금 Quest

**Duolingo-style gamified design doc · v1 · for sign-off**

> Synthesis of four design perspectives (game mechanic, pixel art, copy, motion) + engineering and product critiques. Every P0/P1 buildability finding is resolved here; everything below is buildable in KMP Compose-common with hand-coded art and no new dependencies. The verified-correct `calc/Engine.kt` and `data/` model are **untouched** — gamification only *reads* `proportionRatio`/`burden`/`roi`.

---

## 1. Concept & name

**The pitch.** 재잘(Jaejal) turns Korea's scariest real-estate number — your 재건축 분담금 (the check you write to get the keys to your rebuilt apartment) — into a 90-second guided quest. You pick a complex, pick your old unit (your "starting coins"), then **pull four levers and watch a hand-coded pixel apartment light up floor-by-floor as the number swings**. The moment you tap 확정, a treasure chest opens and your 분담금 is revealed — confetti if money comes back to you (환급), a calm gold "quest cleared, here's your number" if you owe. The guide through it all is **재잘이**, the gold `ㅈ`-roof logo brought to life. The point isn't to dress up debt as a toy; it's that **a finance concept becomes physically intuitive under your thumb** — the smile of comprehension, not just reward.

**Tagline:** `복잡한 재건축, 재잘이가 떠먹여 드려요`

**How 재잘/Jaejal branding shows up:**
- **재잘이 mascot** = the existing `ㅈ`-roof logo come alive (gold hard hat, friendly building body). Not a generic owl/worker — it *is* the brand mark, and it morphs into the buildings you construct. This ties logo → mascot → the "building grows" mechanic into one visual language.
- **Product rename** `재건축 랭커` → `재잘` everywhere, centralized in one `const val PRODUCT_NAME` (see §6, §8).
- **Gold-forward, emoji-rationed.** Gold (`#FBBF24`) is the only AA-safe accent on the dark paper, so it carries the playful load. Emoji is seasoning; **where a hand-coded pixel sprite exists (coin, star, chest), we use the sprite, not the emoji.**

---

## 2. The quest journey

The existing funnel **is already a linear quest** (`TypeSelect → QuestionSelect → Disclaimer → Simulation`). We do **not** add a fake snaking node-map (cut — see §10) and we do **not** split `Simulation` into two routes (cut — engineering P0-1). Instead:

- **The progress bar is the rising building**, shown in a **persistent header** across the existing screens. One progress metaphor, not two.
- **The reveal is an in-screen state toggle** inside the single `Simulation` route (`revealed: Boolean`), not a new `Reveal` route. This keeps the four slider factors where they already live (screen-local `remember`) and avoids the lost-state and double-transition bugs.

### Stage map (each = one existing route, re-skinned)

```
  [ HOME HUB ]  ──tap a 단지──▶  ⓵ 내 헌 집      ──▶  ⓶ 퀘스트      ──▶  (⓷ 규칙)  ──▶  ⓸ 레버  ──▶  👑 공개
  RankingList         (the building          TypeSelect           QuestionSelect    Disclaimer      Simulation     Simulation
  (re-skinned)         header begins)      (+ 출발코인 reveal      (Q1/Q2 pick)     (once only,     (levers,        (revealed=true:
                                            folded in here)                         gate kept)      chest closed)   chest opens)
```

The header building gains a floor at each step; the disclaimer (⓷) is shown **once per session** via the existing `hasAcceptedDisclaimer` gate, then auto-skipped on later runs.

### Stage-by-stage

| Node | Route | Goal (one line) | 재잘이 line | Reward | ASCII sketch |
|---|---|---|---|---|---|
| **HUB** | `RankingList` | "어느 단지를 키워볼까?" | "어느 동네가 1등 단지일까요? 👀" | 챔피언 배지 (RankBadge) | ↓ below |
| **⓵** | `TypeSelect` | "내 평형을 골라 출발 코인(종전자산) 확인" | "이제 당신의 헌 집을 골라요 🏠" | 🪙 출발 코인 count-up | ↓ below |
| **⓶** | `QuestionSelect` | "Q1 얼마 낼까 / Q2 팔까 말까 선택" | "뭐가 제일 궁금해요?" | 퀘스트 토큰 (ToneChip) + **출발코인 reveal folded into header** | ↓ below |
| **⓷** | `Disclaimer` | "추정 게임 규칙 4가지에 OK (1회)" | "잠깐, 게임 규칙 하나만! 📋" | 규칙 확인 | (4 speech-bubble rules + 1 CTA) |
| **⓸** | `Simulation` (`revealed=false`) | "4개 레버를 움직여 교환비를 흔들고 확정" | "이제 레버를 움직여 봐요! 🎚️" | 레버 마스터 + live 교환비 meter | ↓ below |
| **👑** | `Simulation` (`revealed=true`) | "두구두구… 내 분담금 공개" | (branches, §5) | ⭐ 별점 + 분담금 + (Q2) ROI flip | ↓ below |

**HUB — Home (re-skinned `RankingScreen`)**
```
┌──────────────────────────────────────┐
│  재잘   ⭐ 12   🏢 3/5         (no 🔥)  │  ← stars + districts-cleared only
│  "어느 동네가 1등 단지일까요? 👀"  🐤    │
├──────────────────────────────────────┤
│  단지 랭킹                              │
│  ┌────────────┐  #1 압구정3구역        │
│  │ [pixel bldg]│  강남 압구정 · ⭐⭐⭐   │  ← building thumbnail + stars per district
│  │  ▓▓ lit ▓▓ │  기준수익률 +48.7%     │
│  └────────────┘                        │
│  ┌────────────┐  #2 신반포2차 …        │
│  공개 예정                              │
│  🔒 곧 공개돼요! 재잘이가 준비 중 🐤     │
└──────────────────────────────────────┘
```

**⓸ 레버 (Simulation, chest CLOSED)**
```
┌──────────────────────────────────────┐
│ ▟ 1단계 ▙ ▟ 2단계 ▙ ▟ 레버 ▙  ← rising building header
│        [ pixel building, floors lit ] │  ← live: brighter as 교환비 rises
│   교환비 💱  [====|·····]  92%         │  ← live needle meter (本전=100%)
│   ┌────────────────────────────────┐  │
│   │ 분담금 ≈ 3.6억?  (chest 🔒 닫힘) │  │  ← wobbling estimate, exact value gated
│   └────────────────────────────────┘  │
│   🧱 공사비   [——●———]  싸게◄►비싸게    │
│   🏠 식구가격 [———●——]                  │
│   📈 바깥분양 [——●———]                  │
│   ✨ 미래몸값 [————●—]  (Q2만)          │
│        [  기본값으로 되돌리기 ↩️  ]      │
│              [   확정 🥁   ]            │
└──────────────────────────────────────┘
```

**👑 공개 (same Simulation, `revealed=true`)**
```
┌──────────────────────────────────────┐
│        ✦  [ building tops out ]  ✦    │  ← scaffolding falls, roof glows
│         🎉 잭팟! 돈을 돌려받아요! 🎉    │   (refund)  /  퀘스트 클리어! (pay)
│                                        │
│              －1.2 억                   │  ← HUGE count-up, Gain emerald (refund)
│                                        │     or Gold (pay), NEVER bounces
│   🪙 내 헌 집 × 💱 교환비 = 💰 내 몫    │  ← teaching chain
│   ── (Q2) 투자 점수: ROI +47.9% ⭐⭐⭐ ──│  ← silver-lining flip, slides up after
│   ── (pay) "9.8억 ≈ 월 ◯◯만 · 단계별" ─│  ← 납부 follow-through (concrete framing)
│   [ 🔖 저장 ] [ 🔁 다시 ] [ 🏆 랭킹 ]   │
└──────────────────────────────────────┘
```

---

## 3. Jargon → human glossary (paste-ready)

**Discipline (from product critique):** only **3 terms get the full playful hero treatment** — the three a user must *feel*. Everything else stays in plain Korean with a one-line tooltip and a **dual label `친구말(원래말)`** so users leave knowing the real word. Dual-label is the rule, not the exception.

### The 3 hero terms (full treatment, big in UI)

| 원래 용어 | 인게임 라벨 | 재잘이 한 줄 |
|---|---|---|
| **종전자산** | 🪙 내 출발 코인 | "지금 살고 있는 헌 집의 값이에요. 재건축 게임의 밑천이에요." |
| **비례율** | 💱 교환비(비례율) | "내 헌 집을 '재건축 머니'로 바꿀 때의 점수예요. 100% 넘으면 이득! **단, 이건 우리가 못 정해요 — 공사비랑 분양가가 정해요.**" |
| **분담금** | 결과 숫자 (pre-reveal: 🥁 보스 숫자) | "새 집 값에서 내 몫을 뺀 차액. ➕더 내거나 ➖돌려받거나예요." (`보스` 라벨은 **reveal 직후 큰 납부 숫자 옆에서는 절대 안 씀**) |

> 교환비 tooltip closes the misleading-metaphor loop (product P2b): it feels like an exchange rate, but it's *derived* from the sliders you just moved, not a market price you wait for.

### The rest — plain Korean + dual label + one-line tooltip (NOT renamed hero tokens)

| 원래 용어 | 표시 | 재잘이 한 줄 (tooltip) |
|---|---|---|
| 권리가액 | 내 몫(권리가액) | "출발 코인 × 교환비. 재건축이 인정하는 내 진짜 지분이에요." |
| 조합원분양가 | 식구 가격(조합원분양가) | "동네 사람만 받는 특가로 새 집을 살 때 내는 값." |
| 일반분양가 | 바깥 가격(일반분양가) | "남는 집을 외부에 파는 값. 비싸게 팔릴수록 내 부담이 줄어요." |
| 신축예상가 | 새 집 미래값(신축예상가) | "다 짓고 나면 시장에서 얼마? 높을수록 남는 장사!" |
| 총투자금 | 내가 쏟은 돈(총투자금) | "헌 집 시세 + 분담금. 통째로 묻은 돈이에요." |
| 마진 | 남는 돈(마진) | "미래값에서 묻은 돈을 뺀 값. 남으면 이득." |
| 수익률(ROI) | 최종 점수(수익률) | "묻은 돈 대비 몇 %. 랭킹은 이 점수로 매겨요!" |
| KB시세 | 헌 집 현재값(KB시세) | "지금 헌 집이 시장에서 거래되는 값 (KB 부동산 기준)." |

**Celebration chain (reveal 하단, the one-sentence teach):**
> 🪙 내 헌 집 × 💱 교환비 = 💰 내 몫. 새 집 값 − 내 몫 = **분담금!**
> 공사비가 오르면 교환비가 내려가 분담금이 커지고, 바깥분양이 비싸게 팔리면 교환비가 올라 분담금이 줄어요.

---

## 4. Pixel-art spec

### Drawing approach — final decision (two renderers, one data model)

Author every sprite **once** as a `List<Cell>` on an integer grid (`data class Cell(x, y, w, h, color)`, `data class Sprite(cols, rows, cells)`). Render two ways:

| Use | Renderer | Why |
|---|---|---|
| **Static sprites** (mascot moods, building thumbnails on cards) | **`ImageVector` + `Image`** via a `Sprite.toImageVector()` factory (sibling to `stroked()`) | Matches the Logo precedent (baked multi-color fills, `Image` not `Icon`). Free scaling, cacheable. |
| **Live "building grows" facade** (the Simulation header) | **`Canvas` + `drawRect` per cell** | `litFraction` changes every slider tick. Plain `Canvas {}` reading `litFraction` directly — **no `drawWithCache`** (engineering P1-4: the input varies per frame, caching buys nothing; ~300 `drawRect`s at 60fps is fine). |

New file `design/Pixels.kt` holds `Cell`, `Sprite`, `Sprite.toImageVector()`, `PixelCanvas`, and the sprite tables. Pure KMP-common; `androidx.compose.foundation.Canvas` is already on the classpath.

### Buildings — ONE parametric generator, not 5 hand-drawn sprites (engineering P0-3)

Hand-authoring 5 bespoke buildings + 5 parallel `windowCells` sets (hundreds of untooled cells) is the #1 schedule risk. Instead, **one generator emits cells AND the window set in the same loop so they can never drift:**

```kotlin
// design/Pixels.kt — params, not 5 sprites. windowCells filled in the SAME loop.
fun building(
    cols: Int, rows: Int,
    facade: Color, light: Color, shadow: Color,   // 3-value flat shading, light=top-left
    feature: Feature                              // TwinTower | Slab | Ziggurat | Brick | Garden
): Pair<Sprite, Set<Int>> {                       // Sprite + window-cell index set
    val cells = buildList { /* foundation, facade band, gold ㅈ-ridge cap, per-feature roof,
                               then a regular 2-wide window grid every other row →
                               each window cell also added to windowIndexes */ }
    ...
}
```

Differentiate the 5 districts by **parameters + one `Feature` boolean-ish enum**, mapped to the domain personalities:

| 단지 | cols×rows | facade material | Feature | Distinct silhouette cue |
|---|---|---|---|---|
| 압구정3구역 (#1, gold) | 16×22 (tallest) | NavySoft glass | `TwinTower` | two unequal glass towers + double gold cornice + river band |
| 신반포2차 | 14×20 | NavySoft glass | `Slab` | single tall uniform slab, flat parapet |
| 여의도 시범 | 16×20 | MapMist concrete | `Ziggurat` | stepped setback tiers + faint skyline behind |
| 대치 은마 | 16×12 (low/wide) | **Brick** (`Brick`/`BrickLight`) | `Brick` | only brick building, mortar courses, big old windows |
| 목동6단지 | 12×16 | Sky | `Garden` | green garden + trees at base, soft rounded top corner |

**Universal rules (the "cute-but-credible" recipe):** 1-cell `PaperAlt` shadow outline on **bottom+right edges only** (architectural mass, not cartoon stroke); 3-value flat shading per material (base / top-left light / bottom-right shadow), light source fixed top-left; windows are `2×2` clusters whose fill encodes state (off=`Paper`, lit=`Gold`, refund=`Gain`); a gold `ㅈ`-ridge cap on every roof (brand tie). Cuteness lives in the mascot, not baked into the sober buildings.

> **Taste gate (product §4):** build **은마 (brick) and 압구정 (twin towers) FIRST** — the two most distinct/iconic. If they sing as proof, generate the other three from params. If they read samey, rethink before sinking effort into five.

### Mascot — 재잘이, 16×16 cell grid

The gold `ㅈ`-roof logo come alive. Anatomy by cell-region (engineer fills `1×1` rects):

```
ROWS 0–3  HEAD = ㅈ roof:  row0 cols3..12 Gold ridge · row1 cols7..8 Gold apex ·
                           cols2,13 Copper hat-brim · rows1–2 cols5..10 Gold dome, Cloud shine (6,1)
ROWS 4–5  FACE:  row4 cols5,10 eyes (Paper) · row5 cols7,8 smile (PaperAlt) · row4 col12 blush (Loss@40%)
ROWS 6–12 BODY = little building: cols4..11 facade Sky, shadow col11 MapMist ·
                  windows (5,7)(8,7)(5,10)(8,10) 2×2 Gold · row9 col9 gold trowel
ROWS 13–15 BASE: row13 cols4..11 foundation PaperAlt · rows14–15 cols4,11 feet MapMist
```

**4 moods = base sprite + ~6 overridden face cells** (one ImageVector family):
- **Happy** (default/idle): round eyes, 2-cell smile, blush.
- **Thinking** ("어라?" teaching beat): `…` eyes, flat mouth, bobbing Gold `?` prop.
- **Celebrating** (환급 jackpot): `^ ^` closed-happy eyes, wide Gold mouth, arms-up, confetti spawns.
- **Worried-but-supportive** (납부): slightly squished eyes, small `o` `Loss` mouth, one `SkySoft` sweat bead. **No frown** — concerned, not failed.

### "Building grows" feedback (the comprehension hook — P0 per product §5)

Map engine outputs to two visual variables, recomputed on every slider change (engine already runs there):

```kotlin
val ratio = result.proportionRatio                          // 0.61 .. 1.01 in data
val litFraction = ((ratio - 0.55f) / 0.50f).coerceIn(0f, 1f)
val isRefund = result.burden < 0
PixelCanvas(sprite = districtBuilding) { cell ->
    if (cell.index !in windowIndexes) cell.color
    else {
        val heightNorm = 1f - cell.y.toFloat() / sprite.rows  // 0 bottom .. 1 top
        if (heightNorm <= litFraction)
            if (isRefund) ConstructionColors.Gain else ConstructionColors.Gold
        else ConstructionColors.Paper                          // unlit
    }
}
```

Drag 공사비 (villain) down → 교환비 up → more floors light. Drag 바깥분양 up → same. **Cause and effect you can feel.** v1 layers: (1) floors light up bottom-to-top, (2) `GoldSoft`/`GainSoft` glow wash on better/refund, (3) one-shot ring pulse when 교환비 crosses 100%. At reveal: scaffolding `alpha` 1→0 + offset down (a `Hairline` lattice overlay), crane slides off. **Building shudder/jitter deferred to v2** (nausea risk, low payoff).

---

## 5. Motion spec

**The one rule everything bends to:** *Confetti on the journey, gravity on the destination.* PLAY tier (bounce/overshoot) on the quest beats; **WEIGHT tier (decelerate, NO bounce, hold still) on the money.** A bouncing "you owe 9.8억" reads as a slot machine mocking the user.

**Two new `Motion` duration constants:** `reveal = 1100`, `celebrate = 700` (existing `fast 120 / medium 220 / slow 360` stay). No new spring/easing tokens — inline the `BookmarkHeart` recipe (the codebase already does this).

### v1 juice list (concrete Compose values)

| Effect | Tier | Recipe |
|---|---|---|
| **Slider → 교환비/preview** (the hook) | GUIDE | `animateFloatAsState(spring(dampingRatio=0.9f, stiffness=StiffnessHigh))` — catches up ~120–160ms, no overshoot |
| **Mascot idle** | ambient | `rememberInfiniteTransition`: bob `±4dp` `tween(1800, FastOutSlowIn) Reverse` + sway `±1.5°` `tween(2600) Reverse` (desynced) |
| **Mascot reaction** (2 variants + cheer) | PLAY | swap via `AnimatedContent` fade+scale; debounce ~120ms on Δburden sign |
| **확정 → reveal pre-roll** | GUIDE | card `scaleIn 0.92→1.0 spring(0.7, MediumLow)`, `Paper` scrim `alpha 0→0.4 tween(220)` — **in-screen `AnimatedContent(revealed)`, not a route push** (so the shell transition does NOT double-fire) |
| **교환비 100% cross** | PLAY | one-shot `GoldSoft` ring, `tween(celebrate=700, LinearOutSlowIn)`, alpha `1-t`, radius lerp |
| **Reduce-motion** | — | `expect val isAnimationEnabled` — v1 stub returns `true` both platforms (matches existing no-op iOS idiom); gates confetti + reveal scale → collapse to `fadeIn/fadeOut(tween(120))`. Real OS flags in v2. |

### The big reveal choreography (the centerpiece)

`CountUpNumber(target: Double, format)` is a **new composable** (engineering P2-3 — do NOT mutate `HeroNumber`, which is used for instant values). `Format.burdenWithRefund` formats each frame.

**Pre-roll (build tension):** 교환비 card counts first (`tween(600)`) → mascot 🥁 anticipation shake `±2° infiniteRepeatable(tween(90), Reverse)` for 600ms → **350ms held silence** (the pause *is* the suspense).

**Count-up (WEIGHT tier):**
```kotlin
display.animateTo(burden.toFloat(), tween(
    durationMillis = reveal,            // magnitude-scaled: 목동 3.07억 ≈880ms, 신반포 9.83억 ≈1430ms, clamp 800–1500
    easing = FastOutSlowIn               // decelerate INTO the number, NO spring/overshoot
))
// then HOLD dead-still ~400ms before CTA row fades in. Stillness = punctuation.
```

| | 환급 / REFUND (`burden < 0`) — JACKPOT | 납부 / PAY (`burden ≥ 0`) — CLEARED, not failed |
|---|---|---|
| Number tone | `Gain` emerald | `Gold` (**never Loss-red on the figure**) |
| Label | "잭팟! 돈을 돌려받아요! 🎉" | "퀘스트 클리어! 당신의 숫자는 {금액}억 💪" |
| Confetti | **YES** — one time-driver, 40 seeded pixel-square particles in a `Canvas` overlay (`pos = x0+drift·t, y0+fall·t²`, `alpha = 1-t²`). **One `Animatable`, not 40** (engineering P2-1). | **NO confetti** — single soft `GoldSoft` glow-pulse |
| Mascot | Celebrating, arms-up jump | Worried-but-supportive, gentle bob |
| Card | `TrustCard(emphasized=true)`, gold→emerald border crossfade | `TrustCard(emphasized=true)`, gold border |
| Follow-up | — | **Q2:** 1.2s later 투자수익 card slides up, ROI count-up (압구정 +48.7%! → 3⭐) — turns 납부 into a win. **납부 always:** one concrete framing line ("{금액}억 ≈ 월 ◯◯만원씩 · 단계별로 나눠 내요") so the number feels surmountable, not abstract-terrifying. |

**Anxiety guard (product §3b):** full theatrical drumroll on the **first** reveal per session; subsequent re-confirms (after reset/re-drag) get a faster dignified count-up, **no drumroll** — the second look is analysis, not celebration.

**Stars (presentation-only, from existing ROI):** 3⭐ ROI≥40% · 2⭐ 15–40% · 1⭐ <15%. **Q1 has no ROI**, so Q1 grants a 비례율/refund-based star (refund or 교환비≥95% → ⭐⭐⭐ feel) so Q1-only players still climb the leaderboard (engineering P2-5).

---

## 6. Copy (paste-ready Korean)

### Rename strings (replace every `재건축 랭커`)
- `Logo.kt:154` `ConstructionWordmark` → `Text(PRODUCT_NAME)` where `const val PRODUCT_NAME = "재잘"`
- `Logo.kt:120` `contentDescription = "재건축 랭커"` → `PRODUCT_NAME`
- `Screens.kt:398` `RankingHero` hero text → `PRODUCT_NAME` + new tagline
- Mascot name centralized: `const val MASCOT_NAME = "재잘이"`

### Stage copy
| Stage | String |
|---|---|
| 홈 히어로 | `안녕, 난 재잘이! 🐤` / `새 아파트, 결국 내가 얼마 내야 할까?` / 서브: `공식 고시문 숫자로 계산해요. 정확하지만, 진짜 결정은 신중하게요!` |
| 홈 스탯 | `단지 5곳` · `곧 공개 2곳 🔒` · `기준일 {날짜}` (NO 🔥 streak) |
| 단지 진입 | "오, 안목 좋은데요? 이 단지로 한판 가볼까요? 🏢" |
| 평형(⓵) | "이제 당신의 헌 집을 골라요. 어떤 평형에 사세요? 🏠" |
| 질문(⓶) | "뭐가 제일 궁금해요?" · Q1: `💰 나 결국 얼마 더 내야 해?` · Q2: `🤔 지금 팔까, 들고 갈까?` |
| 규칙(⓷) | "잠깐, 게임 규칙 하나만! 📋 여기 숫자는 공식 고시문 기준 '추정치'예요. 감 잡는 덴 딱이지만, 진짜 계약 결정은 꼭 신중하게요. 약속? 🤝" — CTA `좋아요, 시작할게요!` / 뒤로 `잠깐, 뒤로` |
| 레버(⓸) 입장 | "이제 레버를 움직여 봐요! 숫자가 살아 움직여요 🎚️" |
| 레버 반응 | 공사비↑ `벽돌값 올렸네요? 분담금이 꿈틀… 😬` · 바깥분양↑ `바깥에 비싸게 팔기! 분담금이 쑥 내려가요 😎` · 식구가격↑ `우리 식구 가격을 올리면 부담도 같이 올라요 🙃` |

### The reveal
| | String |
|---|---|
| 드럼롤 | `결과 공개… 두구두구두구 🥁` |
| 환급 | `잭팟! 돈을 돌려받아요! 🎉` / `분담금이 아니라 환급이에요. 새 집 받고 {금액}억까지 돌아와요!` / 재잘이 `이건 자랑해도 돼요 ✨` |
| 납부 | `퀘스트 클리어! 당신의 숫자는 {금액}억 💪` / `새 집을 받으려면 {금액}억을 더 준비하면 돼요.` / 재잘이 `겁먹지 마세요. 미리 알았으니 이제 계획을 세울 수 있어요 🙆` |
| 납부 후속 | `{금액}억 ≈ 한 번에 내는 게 아니라 단계별로 나눠 내요. 너무 겁먹지 마세요 🫶` |
| 납부→Q2 떡밥 | `근데요… 더 낸다고 손해는 아니에요. 다음 점수👀 한번 볼래요?` |
| Q2 점수 | 마진➕ `남는 장사예요! 🏆 최종 점수 {ROI}%` · 마진➖ `지금은 빠듯한 그림이에요. 점수 {ROI}% — 시장이 더 오르길 기대하는 베팅이에요 ⚖️` |
| 티칭 트위스트 | `어라? 교환비는 1등인데 점수는 꼴찌네요? 🤨 교환비가 좋다고 무조건 남는 장사는 아니에요. 이게 재건축의 함정!` |

### Microcopy
| 위치 | 라벨 |
|---|---|
| 홈 시작 | `재잘이랑 시작하기 🐤` |
| 슬라이더 리셋 | `기본값으로 되돌리기 ↩️` |
| 결과 후 | `다시 도전하기` / `다른 평형 보기` / `🏆 랭킹에서 내 별 확인` |
| 북마크 | 토스트 `저장했어요! 🔖` / `저장 해제` |
| 진행 라벨 | `1단계 · 단지 고르기` → `2단계 · 내 집 고르기` → `3단계 · 질문 고르기` → `규칙 확인` → `4단계 · 레버 돌리기` → `🎉 결과 공개` · 상단 `마지막 한 단계! 💪` |
| 북마크 0 | `아직 저장한 평형이 없어요. 마음에 드는 곳에 하트를 눌러봐요 🔖` |
| 공개 예정 | `🔒 곧 공개돼요! 재잘이가 열심히 준비 중 🐤` |
| 로딩 | `재잘이가 숫자 계산 중… 🧮` |
| 에러 | `앗, 잠깐 삐끗했어요. 다시 한 번만 눌러줄래요? 🙏` |
| 커뮤니티 (마이) | `🛠️ 숫자가 이상해요 (제보하기)` · `📮 우리 동네도 넣어주세요` |

---

## 7. New tokens (append to `Tokens.kt`)

**6 playful hues** (`ConstructionColors`, `// --- Pixel-art playful hues ---`):

| Token | Hex | Role |
|---|---|---|
| `Sky` | `#2D3B59` | mid-tone facade band |
| `SkySoft` | `#3D5280` | cool lit window glass |
| `Brick` | `#9A5B3B` | 은마 old brick base |
| `BrickLight` | `#C17A52` | brick highlight / mortar |
| `Leaf` | `#4ADE80` | 목동6 foliage (brighter than Gain so it never reads "refund") |
| `Cloud` | `#E6ECF5` | mascot belly / hat shine |

(Navy fills are safe *inside outlined sprites*; any **text/number/speech bubble** stays `Ink`/`Gold`.)

**2 motion durations** (`Motion`): `val reveal = 1100`, `val celebrate = 700`.

**No new dependency.** `Canvas` is Foundation (on classpath); `ImageVector`/`Image` already imported.

---

## 8. Architecture plan (engine + data untouched)

### Routes — **NO new routes.** (Resolves P0-1, P0-2, P1-1, P1-5)
- Keep the 4 existing funnel routes exactly. The reveal is an **in-screen `var revealed by remember { mutableStateOf(false) }`** inside `SimulationScreen`; 확정 sets it true; layout swaps via `AnimatedContent(revealed)`. Factors stay in their existing screen-local `remember` — nothing to hoist, no `pendingFactors`, no `SimInputs`, no lost state.
- `acceptDisclaimer()` is **unchanged** — it already correctly reads `currentRoute as? Route.Disclaimer` before `pop()` then `startSim(...)` (verified `AppState.kt:139–145`).
- 출발 코인 reveal is **folded as an animated header into `QuestionSelectScreen`** (which already has `d` + `t` in scope), not a new `StartingGold` route — that route lacked `typeIndex` and couldn't resolve 종전자산.

### AppState additions (additive, in-memory v1)
```kotlin
const val PRODUCT_NAME = "재잘"
const val MASCOT_NAME = "재잘이"

data class DistrictProgress(
    val bestStars: Int = 0,
    val bestRoi: Double? = null,
    val cleared: Boolean = false
)
val districtProgress = mutableStateMapOf<String, DistrictProgress>()
var totalStars by mutableStateOf(0)
var districtsCleared by mutableStateOf(0)
// NO streakRuns — cut (off-brief; in-memory streak resets every launch = sad "🔥 0")

// Called from SimulationScreen at reveal with the ALREADY-COMPUTED result (no engine re-run):
fun recordReveal(name: String, q: Question, roi: Double, burden: Double) { ... }  // P1-2
```
`recordReveal` takes the finished `result.roi`/`result.burden` the screen already computed — `AppState` does **not** import `Engine` (P1-2: avoids see-vs-store desync).

### File-change map
| File | Change |
|---|---|
| `calc/Engine.kt` | **NONE** (locked) |
| `data/Models.kt`, `format/Format.kt` | reference only; reuse `burdenWithRefund`/`eok`/`percent` |
| `design/Tokens.kt` | +6 hues, +2 motion durations |
| `design/Pixels.kt` (**new**) | `Cell`, `Sprite`, `Sprite.toImageVector()`, `PixelCanvas`, parametric `building(...)`, 재잘이 ×8 mood/blink variants, coin/star/chest props |
| `design/Components.kt` | +`CountUpNumber`, +`BuildingHeader` (rising-building progress), +`ConfettiBurst` (one time-driver), lever-skinned slider wrapper |
| `design/Logo.kt` | rename to `PRODUCT_NAME`; drop serif-mentor assumption |
| `ui/AppState.kt` | +`PRODUCT_NAME`/`MASCOT_NAME`, +`DistrictProgress` map + 2 counters + `recordReveal` (no streak, no new routes) |
| `ui/Screens.kt` | `SimulationScreen`: in-screen `revealed` toggle + lever reskin + live `PixelCanvas` + staged reveal; fold 출발코인 into `QuestionSelectScreen`; disclaimer→rules copy |
| `ui/TabScreens.kt` | re-skin `RankingScreen`→hub (building thumbnails + stars, no streak); empty-state + community copy |
| platform `expect/actual` | +`expect val isAnimationEnabled` (v1 stub `true` both targets) |

---

## 9. Build slices (ordered, solo-dev sized)

Each slice is independently shippable and demoable.

1. **Rename + tokens + reduce-motion stub.** `PRODUCT_NAME`/`MASCOT_NAME`, fix all 3 hard-coded `재건축 랭커` sites, +6 hues +2 durations, `expect val isAnimationEnabled = true`. *(Half-day; ships clean rename.)*
2. **`Pixels.kt` core + the two taste-gate buildings.** `Cell`/`Sprite`/`toImageVector`/`PixelCanvas` + parametric `building(...)`, then **은마 (brick) and 압구정 (twin towers)** only. Render as `RankCard` thumbnails. **Taste gate:** if these two read distinct, continue; if samey, stop and rethink.
3. **재잘이 mascot + idle.** 4 moods + blink, `Image` render, idle bob/sway. Drop Happy 재잘이 into the hub + funnel headers.
4. **The comprehension hook (P0 per product).** Live `PixelCanvas` building in `SimulationScreen` header wired to `litFraction`; `animateFloatAsState(spring(0.9, High))` on 교환비/preview; lever reskin + 재잘이 reactions (2 variants). *This is the smile — build it before confetti.*
5. **Closed-chest lever stage + in-screen reveal toggle.** `revealed` boolean, 확정 button, `AnimatedContent` swap, scrim pre-roll, `CountUpNumber` (WEIGHT tier), 350ms pause + 400ms hold, owe/refund branch.
6. **Celebration layer.** `ConfettiBurst` (one time-driver, refund only), `GoldSoft`/`GainSoft` glows, scaffolding-fall, mascot cheer. Anxiety guard (theatrical-once).
7. **Progress + hub + follow-through.** `DistrictProgress`/stars/`recordReveal`, hub building-thumbnails + star counts, Q2 ROI silver-lining flip, 납부 concrete-framing line, Q1 비례율-based star.
8. **Remaining 3 buildings** (slab / ziggurat / garden) from params — only after the taste gate passes.
9. **Copy pass + glossary tooltips** across all screens; dual-label discipline; rules-as-speech-bubbles.

---

## 10. Explicitly cut for v1 (honest scope)

| Cut | Why |
|---|---|
| **Snaking 5-node Duolingo quest-map** | Cosplay padding on a 90-second 5-step funnel; contradicts "the building IS the progress bar." Replaced by the persistent rising-building header. |
| **`LeverStage` + `Reveal` routes / `pendingFactors` / `SimInputs`** | Engineering P0-1: factors are screen-local; splitting routes loses them and double-fires the shell transition. In-screen `revealed` toggle instead. |
| **`StartingGold` route** | Engineering P1-1: lacked `typeIndex`, couldn't show 종전자산. Folded into `QuestionSelectScreen` header. |
| **🔥 Streak counter** | Off-brief for a once-in-a-lifetime decision; in-memory streak resets every launch = sad "🔥 0". |
| **8 of 10 jargon terms as renamed hero tokens** | Metaphor overload = *more* to learn. Only 3 hero terms; rest are plain Korean + dual-label + tooltip. |
| **Lever snap-to-detent on peer markers** | Engineering P2-2: new interaction logic, markers can clip off-range. Ship visual levers + live reactions; defer snapping. |
| **Building shudder/jitter; per-frame shape morphing** | Nausea risk / expensive parametric vectors, low payoff vs the glow wash. |
| **Real haptics/sound actuals; real OS reduce-motion flags** | Ship `expect/actual` stubs + call sites; wire `VibrationEffect`/`UIImpactFeedbackGenerator`/audio + `ANIMATOR_DURATION_SCALE`/`UIAccessibilityIsReduceMotionEnabled` in v2. |
| **DataStore persistence of progress** | v1 is in-memory (session-only); `// TODO persist` seam left. That's why streak is cut, not just hidden. |
| **Mascot multi-emotion library beyond 4 moods** | 4 moods + blink is enough for v1. |

---

### Bottom line for sign-off
Engine and data are untouched; **no new routes** (the reveal is an in-screen toggle), **no invented APIs**, the pixel buildings collapse to **one parametric generator gated on 은마+압구정 proof**, and effort is rebalanced toward the **slider→building comprehension hook** (the real smile) and the **납부 follow-through** (the real product value). The Duolingo energy lives in the journey; the money lands with weight. Ship slices 1–7 for a complete v1; 8–9 polish.
