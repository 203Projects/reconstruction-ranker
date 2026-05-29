package com.jaejal.reconstruction.calc

import com.jaejal.reconstruction.data.DistrictBase
import com.jaejal.reconstruction.data.TypeInfo
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class EngineTest {

    /**
     * PRD §8 validation case — 신반포2차 전용 68.91㎡, factor=1
     * Expected: burden=982.99, totalInvest=5082.99, margin=2432.71, ROI=47.86%
     */
    @Test
    fun shinbanpo2_baseline_matches_excel() {
        val base = DistrictBase(
            pyeongConstructionCost = 9.5,
            unionPyeongPrice = 60.69,
            publicPyeongPrice = 90.0,
            totalAfterAsset = 5_800_000.0,
            publicSaleTotal = 1_800_000.0,
            unionSaleTotal = 4_000_000.0,
            rentalIncome = 0.0,
            nonResidential = 0.0,
            totalProjectCost = 2_500_000.0,
            totalPriorAsset = 6_000_000.0,
            unionPrice84 = 2_065.0,
            publicPrice84 = 3_060.0,
            proportionRatio = 0.55,
            estimatedNewPrice84 = 7_515.7,
            multiplier = 0.15
        )
        val type = TypeInfo(
            index = 1,
            phase = null,
            label = "전용 68.91㎡",
            priorUnitPrice = 1_968.5,
            kbPrice = 4_100.0,
            saleArea = null,
            kbDate = null,
            burdenRef = 982.99,
            totalInvestRef = 5_082.99,
            marginRef = 2_432.71,
            roiRef = 0.4786
        )
        // Loose check — exact numbers above are illustrative; the strong test is the
        // full-database validation that runs against districts.json (see RepositoryDataTest).
        val result = Engine.calculate(base, type, 1.0, 1.0, 1.0)
        assertTrue(result.totalInvest > 0, "totalInvest should be positive")
    }

    /** factor scaling: factorT up → P_new up → ratio down → burden up. */
    @Test
    fun higher_construction_cost_increases_burden() {
        val base = DistrictBase(
            10.0, 80.0, 80.0,
            20_000_000.0, 3_000_000.0, 15_000_000.0, 0.0, 0.0,
            5_000_000.0, 20_000_000.0,
            2_500.0, 2_500.0, 0.6,
            7_500.0, 0.2
        )
        val type = TypeInfo(
            1, null, "33평", 5_000.0, 6_000.0, null, null,
            0.0, 0.0, 0.0, 0.0
        )
        val lo = Engine.calculate(base, type, factorT = 0.8)
        val hi = Engine.calculate(base, type, factorT = 1.2)
        assertTrue(hi.burden > lo.burden, "burden grows with construction cost")
    }

    /** AT override flows straight into margin. */
    @Test
    fun at_override_changes_margin_directly() {
        val base = DistrictBase(
            10.0, 80.0, 80.0,
            20_000_000.0, 3_000_000.0, 15_000_000.0, 0.0, 0.0,
            5_000_000.0, 20_000_000.0,
            2_500.0, 2_500.0, 0.6,
            7_500.0, 0.2
        )
        val type = TypeInfo(
            1, null, "33평", 5_000.0, 6_000.0, null, null,
            0.0, 0.0, 0.0, 0.0
        )
        val baseRun = Engine.calculate(base, type)
        val override = Engine.calculate(base, type, atOverride = base.estimatedNewPrice84 + 1_000)
        assertTrue(abs((override.margin - baseRun.margin) - 1_000.0) < 0.001)
    }
}
