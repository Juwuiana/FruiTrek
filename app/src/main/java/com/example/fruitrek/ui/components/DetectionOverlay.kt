package com.example.fruitrek.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fruitrek.data.model.DetectionResult
import com.example.fruitrek.data.model.FruitCatalog

/**
 * Transparent Canvas overlay that draws child-friendly bounding boxes
 * directly on top of the CameraX PreviewView.
 *
 * Detection coords are normalized (0.0–1.0), so we simply
 * multiply by the canvas width/height at draw time.
 */
@Composable
fun DetectionOverlay(
    detections: List<DetectionResult>,
    modifier  : Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        detections.forEach { det ->
            val fruit    = FruitCatalog.findById(det.label)
            val boxColor = if (fruit != null) Color(fruit.color) else Color(0xFF66BB6A)
            drawBox(det, boxColor, measurer)
        }
    }
}

// ── Drawing helpers ───────────────────────────────────────────────────────────

private fun DrawScope.drawBox(
    det     : DetectionResult,
    color   : Color,
    measurer: TextMeasurer
) {
    val b      = det.boundingBox
    val left   = b.left   * size.width
    val top    = b.top    * size.height
    val right  = b.right  * size.width
    val bottom = b.bottom * size.height
    val w = right  - left
    val h = bottom - top

    // Rounded rect fill (translucent)
    drawRoundRect(
        color     = color.copy(alpha = 0.14f),
        topLeft   = Offset(left, top),
        size      = Size(w, h),
        cornerRadius = CornerRadius(18f)
    )

    // Rounded rect stroke
    drawRoundRect(
        color     = color,
        topLeft   = Offset(left, top),
        size      = Size(w, h),
        cornerRadius = CornerRadius(18f),
        style     = Stroke(width = 5.dp.toPx())
    )

    // Corner brackets — more playful than a plain rectangle
    val arm  = minOf(w, h) * 0.20f
    val sw   = 7.dp.toPx()
    drawCornerBrackets(left, top, right, bottom, arm, sw, color)

    // Label pill above the box
    drawLabel(det, color, left, top, measurer)
}

private fun DrawScope.drawCornerBrackets(
    left: Float, top: Float,
    right: Float, bottom: Float,
    arm: Float, sw: Float, color: Color
) {
    val cap = StrokeCap.Round
    // Top-left
    drawLine(color, Offset(left, top + arm), Offset(left, top), strokeWidth = sw, cap = cap)
    drawLine(color, Offset(left, top), Offset(left + arm, top), strokeWidth = sw, cap = cap)
    // Top-right
    drawLine(color, Offset(right - arm, top), Offset(right, top), strokeWidth = sw, cap = cap)
    drawLine(color, Offset(right, top), Offset(right, top + arm), strokeWidth = sw, cap = cap)
    // Bottom-left
    drawLine(color, Offset(left, bottom - arm), Offset(left, bottom), strokeWidth = sw, cap = cap)
    drawLine(color, Offset(left, bottom), Offset(left + arm, bottom), strokeWidth = sw, cap = cap)
    // Bottom-right
    drawLine(color, Offset(right - arm, bottom), Offset(right, bottom), strokeWidth = sw, cap = cap)
    drawLine(color, Offset(right, bottom), Offset(right, bottom - arm), strokeWidth = sw, cap = cap)
}

private fun DrawScope.drawLabel(
    det     : DetectionResult,
    color   : Color,
    left    : Float,
    top     : Float,
    measurer: TextMeasurer
) {
    val fruit    = FruitCatalog.findById(det.label)
    val labelStr = buildString {
        if (fruit != null) append("${fruit.emoji} ${fruit.englishName}")
        else               append(det.label)
        append("  ${(det.confidence * 100).toInt()}%")
    }

    val measured = measurer.measure(
        text  = labelStr,
        style = TextStyle(
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 13.sp
        )
    )

    val pad    = 8.dp.toPx()
    val pillW  = measured.size.width  + pad * 2
    val pillH  = measured.size.height + pad * 1.2f
    val pillY  = (top - pillH - 4.dp.toPx()).coerceAtLeast(0f)

    drawRoundRect(
        color        = color,
        topLeft      = Offset(left, pillY),
        size         = Size(pillW, pillH),
        cornerRadius = CornerRadius(20f)
    )
    drawText(
        textMeasurer = measurer,
        text         = labelStr,
        style        = TextStyle(
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 13.sp
        ),
        topLeft = Offset(left + pad, pillY + pad * 0.6f)
    )
}
