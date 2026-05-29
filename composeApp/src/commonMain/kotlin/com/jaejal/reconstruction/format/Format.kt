package com.jaejal.reconstruction.format

import kotlin.math.abs
import kotlin.math.roundToLong

object Format {

    /** Convert million-won internal value into 억 (100M won) and format like "12.34억" (2 decimals). */
    fun eok(millionWon: Double, withUnit: Boolean = true, decimals: Int = 2): String {
        val v = millionWon / 100.0
        val s = round(v, decimals)
        return if (withUnit) "${s}억" else s
    }

    /** Format burden specifically — show negative as "환급". */
    fun burdenWithRefund(millionWon: Double): String {
        val absStr = eok(abs(millionWon))
        return if (millionWon < 0) "$absStr (환급)" else absStr
    }

    fun percent(ratio: Double, decimals: Int = 2): String {
        return "${round(ratio * 100.0, decimals)}%"
    }

    /** Decimal round to N places without depending on java.text. */
    fun round(value: Double, decimals: Int): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        var factor = 1.0
        repeat(decimals) { factor *= 10.0 }
        val rounded = (value * factor).roundToLong() / factor
        val sign = if (rounded < 0) "-" else ""
        val abs = abs(rounded)
        val whole = abs.toLong()
        val frac = (abs - whole) * factor
        val fracLong = frac.roundToLong()
        val wholeStr = withThousands(whole)
        if (decimals == 0) return sign + wholeStr
        val fracStr = fracLong.toString().padStart(decimals, '0')
        return "$sign$wholeStr.$fracStr"
    }

    private fun withThousands(n: Long): String {
        val s = n.toString()
        val sb = StringBuilder()
        for ((i, c) in s.withIndex()) {
            if (i > 0 && (s.length - i) % 3 == 0) sb.append(',')
            sb.append(c)
        }
        return sb.toString()
    }
}
