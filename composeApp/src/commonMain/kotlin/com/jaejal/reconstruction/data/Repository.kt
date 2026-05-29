package com.jaejal.reconstruction.data

import com.jaejal.reconstruction.calc.Engine
import com.jaejal.reconstruction.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

object Repository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private var cache: List<District>? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadDistricts(): List<District> {
        cache?.let { return it }
        val bytes = Res.readBytes("files/districts.json")
        val parsed = json.decodeFromString<List<District>>(bytes.decodeToString())
        cache = parsed
        return parsed
    }

    /** Blurred placeholder districts (PRD §5). */
    val blurredDistricts: List<BlurredDistrict> = listOf(
        BlurredDistrict("서빙고 신동아", "용산구"),
        BlurredDistrict("서초 진흥", "서초구")
    )

    /**
     * Ranks the districts by ROI of the cheapest (min AC) type at factor=1.
     * PRD §6: "단지별 종전자산(AC) 최소 타입의 BB(수익률) 기준 내림차순"
     */
    fun rank(districts: List<District>): List<RankedDistrict> =
        districts.map { d ->
            val cheapest = d.types.minBy { it.priorUnitPrice }
            val result = Engine.calculate(d.base, cheapest)
            RankedDistrict(d, cheapest, result.roi)
        }.sortedByDescending { it.headlineRoi }
}

data class BlurredDistrict(val name: String, val location: String)
data class RankedDistrict(val district: District, val headlineType: TypeInfo, val headlineRoi: Double)
