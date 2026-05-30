package com.jaejal.reconstruction.format

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the Format helpers + slider value-mapping strings BEFORE the UI is wired (standing TDD rule).
 *
 * Verified data (압구정3구역, the screen in the feedback screenshots):
 *  - T  (평당공사비)          = 10.584581299872516 백만원  → "평당 1,058만원"
 *  - AH (전용84 조합원분양가) = 2862.255 백만원            → "28.62억"
 *  - AI (전용84 일반분양가)   = 2931.225 백만원            → "29.31억"
 *  - AC (종전자산 단가, type) = 5063 백만원                → "50.63억"  (40.91억 for AC=4091, etc.)
 */
class FormatTest {

    // ---------- item 3 / 4: 공사비 slider 만원 label + result value ----------

    @Test
    fun manwon_construction_cost_renders_thousands_separated_manwon() {
        // T * 100 = 1058.458... → whole-만원 → "1,058만원"
        assertEquals("1,058만원", Format.manwon(10.584581299872516))
    }

    @Test
    fun manwon_without_unit_drops_suffix() {
        assertEquals("1,058", Format.manwon(10.584581299872516, withUnit = false))
    }

    @Test
    fun manwon_zero() {
        assertEquals("0만원", Format.manwon(0.0))
    }

    @Test
    fun manwon_thousands_separator() {
        // 20 백만원 = 2,000 만원
        assertEquals("2,000만원", Format.manwon(20.0))
    }

    @Test
    fun manwon_nan_uses_round_guard_and_does_not_throw() {
        // round() returns "—" for NaN; manwon must not throw and must surface the guard.
        val out = Format.manwon(Double.NaN)
        assertEquals("—만원", out)
    }

    // ---------- item 5 / 6: AH / AI re-base mapping strings ----------

    @Test
    fun eok_unionPrice84_at_factor_one() {
        // AH = 2862.255 → 28.62억  (slider value at factorL = 1.0)
        assertEquals("28.62억", Format.eok(2862.255 * 1.0))
    }

    @Test
    fun eok_publicPrice84_at_factor_one() {
        // AI = 2931.225 → 29.31억  (slider value at factorJ = 1.0)
        assertEquals("29.31억", Format.eok(2931.225 * 1.0))
    }

    @Test
    fun eok_unionPrice84_range_endpoints_lock_the_slider_domain() {
        val baseAH = 2862.255
        // factor 0.6 .. 1.4 displayed as AH 억원 endpoints
        assertEquals("17.17억", Format.eok(baseAH * 0.6)) // 1717.353 / 100 = 17.17353
        assertEquals("40.07억", Format.eok(baseAH * 1.4)) // 4007.157 / 100 = 40.07157
    }

    // ---------- item 2: 종전자산 추정가액 StatLine value source ----------

    @Test
    fun eok_prior_unit_price_for_statline() {
        // generic eok(type.priorUnitPrice): AC=5063 → 50.63억; AC=4091 → 40.91억
        assertEquals("50.63억", Format.eok(5063.0))
        assertEquals("40.91억", Format.eok(4091.0))
    }

    // ---------- factor <-> display round-trip (validates the re-base wiring) ----------

    @Test
    fun factor_display_roundtrip_is_exact() {
        val baseAH = 2862.255
        val baseAI = 2931.225
        for (f in listOf(0.6, 0.8, 1.0, 1.2, 1.4)) {
            assertTrue(abs((baseAH * f) / baseAH - f) < 1e-9, "AH round-trip failed at f=$f")
            assertTrue(abs((baseAI * f) / baseAI - f) < 1e-9, "AI round-trip failed at f=$f")
        }
    }
}
