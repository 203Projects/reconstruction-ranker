package com.jaejal.reconstruction.calc

import com.jaejal.reconstruction.data.DistrictBase
import com.jaejal.reconstruction.data.SimulationResult
import com.jaejal.reconstruction.data.TypeInfo

object Engine {

    fun calculate(
        base: DistrictBase,
        type: TypeInfo,
        factorT: Double = 1.0,
        factorL: Double = 1.0,
        factorJ: Double = 1.0,
        atOverride: Double? = null
    ): SimulationResult {
        val pNew = base.totalProjectCost * factorT
        val kNew = base.unionSaleTotal * factorL
        val iNew = base.publicSaleTotal * factorJ
        val hNew = iNew + kNew + base.rentalIncome + base.nonResidential
        val ratio = (hNew - pNew) / base.totalPriorAsset

        val unionPrice84New = base.unionPrice84 * factorL
        val rights = type.priorUnitPrice * ratio
        val burden = unionPrice84New - rights

        val atNew = atOverride ?: base.estimatedNewPrice84
        val totalInvest = type.kbPrice + burden
        val margin = atNew - totalInvest
        val roi = if (totalInvest > 0) margin / totalInvest else 0.0

        return SimulationResult(
            proportionRatio = ratio,
            rights = rights,
            unionPrice84 = unionPrice84New,
            burden = burden,
            kbPrice = type.kbPrice,
            totalInvest = totalInvest,
            newPrice84 = atNew,
            margin = margin,
            roi = roi
        )
    }
}
