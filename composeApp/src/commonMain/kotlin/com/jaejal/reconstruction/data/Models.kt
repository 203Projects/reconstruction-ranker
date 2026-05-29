package com.jaejal.reconstruction.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class District(
    val name: String,
    val address: String,
    val noticeDate: String? = null,
    val noticeName: String? = null,
    val base: DistrictBase,
    val similar: SimilarComplex,
    val types: List<TypeInfo>
) {
    val shortLocation: String
        get() = address.split(" ").take(3).joinToString(" ")
}

@Serializable
data class DistrictBase(
    @SerialName("T") val pyeongConstructionCost: Double,
    @SerialName("L") val unionPyeongPrice: Double,
    @SerialName("J") val publicPyeongPrice: Double,
    @SerialName("H") val totalAfterAsset: Double,
    @SerialName("I") val publicSaleTotal: Double,
    @SerialName("K") val unionSaleTotal: Double,
    @SerialName("M") val rentalIncome: Double,
    @SerialName("N") val nonResidential: Double,
    @SerialName("P") val totalProjectCost: Double,
    @SerialName("V") val totalPriorAsset: Double,
    @SerialName("AH") val unionPrice84: Double,
    @SerialName("AI") val publicPrice84: Double,
    @SerialName("AJ") val proportionRatio: Double,
    @SerialName("AT") val estimatedNewPrice84: Double,
    @SerialName("AS") val multiplier: Double
)

@Serializable
data class SimilarComplex(
    val name: String? = null,
    val address: String? = null,
    @SerialName("price84") val price84: Double? = null,
    val area: Double? = null,
    val pricePerPyeong: Double? = null,
    val ownArea84: Double? = null
)

@Serializable
data class TypeInfo(
    @SerialName("Z") val index: Int,
    @SerialName("AA") val phase: String? = null,
    @SerialName("AB") val label: String,
    @SerialName("AC") val priorUnitPrice: Double,
    @SerialName("AD") val kbPrice: Double,
    @SerialName("AE") val saleArea: String? = null,
    @SerialName("AF") val kbDate: String? = null,
    @SerialName("AZ_ref") val burdenRef: Double,
    @SerialName("AU_ref") val totalInvestRef: Double,
    @SerialName("AV_ref") val marginRef: Double,
    @SerialName("BB_ref") val roiRef: Double
) {
    val displayLabel: String get() = label
}

data class SimulationResult(
    val proportionRatio: Double,
    val rights: Double,
    val unionPrice84: Double,
    val burden: Double,
    val kbPrice: Double,
    val totalInvest: Double,
    val newPrice84: Double,
    val margin: Double,
    val roi: Double
)
