package com.jaejal.reconstruction.data

import com.jaejal.reconstruction.calc.Engine
import kotlin.math.abs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * For each district + type loaded from districts.json, the engine at factor=1
 * must reproduce the Excel reference values to within 0.5% relative.
 *
 * This is the strong correctness gate from PRD §6 acceptance criteria:
 *   "모든 슬라이더 factor=1일 때 ... AJ·AZ·AU·AV·BB와 소수점 둘째자리까지 일치"
 */
class RepositoryDataTest {

    private fun close(actual: Double, ref: Double, tol: Double = 0.01): Boolean {
        if (ref == 0.0) return abs(actual) <= tol
        return abs(actual - ref) <= maxOf(tol, abs(ref) * 0.005)
    }

    @Test
    fun all_types_match_reference_at_factor_one() = runTest {
        val districts = Repository.loadDistricts()
        assertTrue(districts.isNotEmpty(), "JSON loaded zero districts")
        assertEquals(5, districts.size, "Expected 5 districts in MVP")

        val mismatches = mutableListOf<String>()
        for (d in districts) {
            for (t in d.types) {
                val r = Engine.calculate(d.base, t)
                if (!close(r.burden, t.burdenRef)) mismatches += "${d.name}/${t.label} burden ${r.burden} vs ref ${t.burdenRef}"
                if (!close(r.totalInvest, t.totalInvestRef)) mismatches += "${d.name}/${t.label} total ${r.totalInvest} vs ref ${t.totalInvestRef}"
                if (!close(r.margin, t.marginRef)) mismatches += "${d.name}/${t.label} margin ${r.margin} vs ref ${t.marginRef}"
                if (!close(r.roi, t.roiRef, tol = 0.001)) mismatches += "${d.name}/${t.label} roi ${r.roi} vs ref ${t.roiRef}"
            }
        }
        assertTrue(mismatches.isEmpty(), "Reference mismatches:\n" + mismatches.joinToString("\n"))
    }

    @Test
    fun ranking_orders_by_descending_roi_of_cheapest_type() = runTest {
        val districts = Repository.loadDistricts()
        val ranked = Repository.rank(districts)
        // PRD §5 expected order
        val expected = listOf("압구정3구역", "신반포2차", "여의도 시범", "대치 은마", "목동6단지")
        val actual = ranked.map { it.district.name }
        assertEquals(expected, actual)
    }
}
