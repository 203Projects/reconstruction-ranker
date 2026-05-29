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
}
