package com.example.fruitrek.data.model

import android.graphics.RectF

/**
 * One fruit detection from the TFLite model.
 *
 * @param label       matches a [Fruit.id] in [FruitCatalog]
 * @param confidence  0.0 – 1.0
 * @param boundingBox normalized coords (0.0–1.0) relative to full image dimensions
 */
data class DetectionResult(
    val label       : String,
    val confidence  : Float,
    val boundingBox : RectF
)
