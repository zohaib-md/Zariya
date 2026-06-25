package com.project.zariya.core.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object ZariyaIcons {
    val PillBottle: ImageVector
        get() = ImageVector.Builder(
            name = "PillBottle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6f, 3f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(-12f)
                close()
                moveTo(7f, 7f)
                horizontalLineToRelative(10f)
                verticalLineToRelative(14f)
                curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
                horizontalLineToRelative(-6f)
                curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
                verticalLineToRelative(-14f)
                close()
                moveTo(11f, 10f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(-3f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-3f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(-3f)
                verticalLineToRelative(-3f)
                horizontalLineToRelative(-2f)
                close()
            }
        }.build()

    val Capsule: ImageVector
        get() = ImageVector.Builder(
            name = "Capsule",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(17.4f, 6.6f)
                curveToRelative(-1.8f, -1.8f, -4.7f, -1.8f, -6.5f, 0f)
                lineTo(4.6f, 12.9f)
                curveToRelative(-1.8f, 1.8f, -1.8f, 4.7f, 0f, 6.5f)
                curveToRelative(1.8f, 1.8f, 4.7f, 1.8f, 6.5f, 0f)
                lineToRelative(6.3f, -6.3f)
                curveToRelative(1.8f, -1.8f, 1.8f, -4.7f, 0f, -6.5f)
                close()
                moveTo(16f, 14.5f)
                lineTo(9.5f, 8f)
                curveToRelative(1f, -1f, 2.6f, -1f, 3.6f, 0f)
                lineToRelative(2.9f, 2.9f)
                curveToRelative(1f, 1f, 1f, 2.6f, 0f, 3.6f)
                close()
            }
        }.build()
}
