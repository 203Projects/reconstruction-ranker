package com.jaejal.reconstruction.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-authored construction-theme icons.
 *
 * 24dp viewBox, stroke-based, designed to read as a coherent set:
 *  - Home: tower (rebuild silhouette)
 *  - Districts: grid map (4-cell)
 *  - Sims: sliders
 *  - My: bookmark (saved + personal)
 */

object ConstructionIcons {

    private fun stroked(name: String, build: Builder.() -> Builder) = Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).run(build).build()

    val Home: ImageVector
        get() = stroked("Home") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null
            ) {
                // simple house silhouette + line for door
                moveTo(3f, 11f)
                lineTo(12f, 3.5f)
                lineTo(21f, 11f)
                moveTo(5f, 10f)
                lineTo(5f, 20.5f)
                lineTo(19f, 20.5f)
                lineTo(19f, 10f)
                moveTo(10f, 20.5f)
                lineTo(10f, 14f)
                lineTo(14f, 14f)
                lineTo(14f, 20.5f)
            }
        }

    val Districts: ImageVector
        get() = stroked("Districts") {
            // 2×2 grid of rounded rectangles (district plots)
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null
            ) {
                // top-left
                moveTo(3.5f, 3.5f); lineTo(10.5f, 3.5f); lineTo(10.5f, 10.5f); lineTo(3.5f, 10.5f); close()
                // top-right
                moveTo(13.5f, 3.5f); lineTo(20.5f, 3.5f); lineTo(20.5f, 10.5f); lineTo(13.5f, 10.5f); close()
                // bottom-left
                moveTo(3.5f, 13.5f); lineTo(10.5f, 13.5f); lineTo(10.5f, 20.5f); lineTo(3.5f, 20.5f); close()
                // bottom-right
                moveTo(13.5f, 13.5f); lineTo(20.5f, 13.5f); lineTo(20.5f, 20.5f); lineTo(13.5f, 20.5f); close()
            }
        }

    val Sims: ImageVector
        get() = stroked("Sims") {
            // Three horizontal sliders with thumbs
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                fill = null
            ) {
                moveTo(3.5f, 6.5f); lineTo(20.5f, 6.5f)
                moveTo(3.5f, 12f); lineTo(20.5f, 12f)
                moveTo(3.5f, 17.5f); lineTo(20.5f, 17.5f)
            }
            // Thumbs
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                fill = SolidColor(Color.Black)
            ) {
                moveTo(8.5f, 6.5f); arcTo(2f, 2f, 0f, true, true, 8.51f, 6.5f); close()
                moveTo(15.5f, 12f); arcTo(2f, 2f, 0f, true, true, 15.51f, 12f); close()
                moveTo(11.5f, 17.5f); arcTo(2f, 2f, 0f, true, true, 11.51f, 17.5f); close()
            }
        }

    val My: ImageVector
        get() = stroked("My") {
            // Bookmark / saved icon
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null
            ) {
                moveTo(6.5f, 3.5f)
                lineTo(17.5f, 3.5f)
                lineTo(17.5f, 20.5f)
                lineTo(12f, 16f)
                lineTo(6.5f, 20.5f)
                close()
            }
        }

    val Search: ImageVector
        get() = stroked("Search") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                fill = null
            ) {
                moveTo(10.5f, 4f)
                arcTo(6.5f, 6.5f, 0f, true, true, 10.49f, 4f)
                close()
                moveTo(15.5f, 15.5f)
                lineTo(20.5f, 20.5f)
            }
        }

    val Clock: ImageVector
        get() = stroked("Clock") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                fill = null
            ) {
                moveTo(12f, 3f)
                arcTo(9f, 9f, 0f, true, true, 11.99f, 3f)
                close()
                moveTo(12f, 7f); lineTo(12f, 12f); lineTo(15.5f, 13.5f)
            }
        }

    /** Heart silhouette path, shared by outline and filled variants. */
    private fun Builder.heartPath(stroke: Boolean, fill: Boolean): Builder {
        path(
            stroke = if (stroke) SolidColor(Color.Black) else null,
            strokeLineWidth = if (stroke) 1.8f else 0f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            fill = if (fill) SolidColor(Color.Black) else null,
            pathFillType = PathFillType.NonZero
        ) {
            // Two top lobes meeting at the dip (12, 6.4), down to the tip (12, 20.8).
            moveTo(12f, 20.8f)
            curveTo(12f, 20.8f, 3.2f, 14.8f, 3.2f, 8.6f)
            curveTo(3.2f, 5.7f, 5.5f, 3.6f, 8.2f, 3.6f)
            curveTo(10.0f, 3.6f, 11.3f, 4.7f, 12f, 6.4f)
            curveTo(12.7f, 4.7f, 14.0f, 3.6f, 15.8f, 3.6f)
            curveTo(18.5f, 3.6f, 20.8f, 5.7f, 20.8f, 8.6f)
            curveTo(20.8f, 14.8f, 12f, 20.8f, 12f, 20.8f)
            close()
        }
        return this
    }

    /** Outline heart — not-yet-saved state. */
    val HeartOutline: ImageVector
        get() = stroked("HeartOutline") { heartPath(stroke = true, fill = false) }

    /** Filled heart — saved state. */
    val HeartFilled: ImageVector
        get() = stroked("HeartFilled") { heartPath(stroke = false, fill = true) }

    /** Left chevron + shaft — back navigation. */
    val ArrowBack: ImageVector
        get() = stroked("ArrowBack") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null
            ) {
                moveTo(19f, 12f); lineTo(5f, 12f)
                moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f)
            }
        }

    /** Two-tower building silhouette — KB시세 / 종전자산 stat lines. */
    val Building: ImageVector
        get() = stroked("Building") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null
            ) {
                // left tower
                moveTo(4f, 20.5f); lineTo(4f, 8f); lineTo(11f, 8f); lineTo(11f, 20.5f)
                // right (taller) tower
                moveTo(11f, 20.5f); lineTo(11f, 4f); lineTo(20f, 4f); lineTo(20f, 20.5f)
                // baseline
                moveTo(2.5f, 20.5f); lineTo(21.5f, 20.5f)
            }
            // windows
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Round, fill = null) {
                moveTo(7f, 11.5f); lineTo(7f, 12f)
                moveTo(7f, 15.5f); lineTo(7f, 16f)
                moveTo(15.5f, 8f); lineTo(15.5f, 8.5f)
                moveTo(15.5f, 12f); lineTo(15.5f, 12.5f)
                moveTo(15.5f, 16f); lineTo(15.5f, 16.5f)
            }
        }

    /** Calculator outline — 분담금 / 권리가액 stat lines. */
    val Calculator: ImageVector
        get() = stroked("Calculator") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null
            ) {
                moveTo(5.5f, 3.5f); lineTo(18.5f, 3.5f); lineTo(18.5f, 20.5f); lineTo(5.5f, 20.5f); close()
                // display
                moveTo(8f, 7f); lineTo(16f, 7f)
            }
            // key dots
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round, fill = null) {
                moveTo(8.5f, 11f); lineTo(8.51f, 11f)
                moveTo(12f, 11f); lineTo(12.01f, 11f)
                moveTo(15.5f, 11f); lineTo(15.51f, 11f)
                moveTo(8.5f, 14.5f); lineTo(8.51f, 14.5f)
                moveTo(12f, 14.5f); lineTo(12.01f, 14.5f)
                moveTo(15.5f, 14.5f); lineTo(15.51f, 14.5f)
                moveTo(8.5f, 18f); lineTo(8.51f, 18f)
                moveTo(12f, 18f); lineTo(12.01f, 18f)
                moveTo(15.5f, 18f); lineTo(15.51f, 18f)
            }
        }

    /** Upward trend line + arrowhead — 기준 수익률 / 마진 stat lines. */
    val TrendingUp: ImageVector
        get() = stroked("TrendingUp") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null
            ) {
                moveTo(3.5f, 15.5f); lineTo(9f, 10f); lineTo(13f, 14f); lineTo(20.5f, 6.5f)
                // arrowhead
                moveTo(15.5f, 6.5f); lineTo(20.5f, 6.5f); lineTo(20.5f, 11.5f)
            }
        }
}
