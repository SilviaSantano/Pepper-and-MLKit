package de.inovex.pepper.intelligence.mlkit.utils

import android.view.View
import kotlin.math.roundToInt

fun View.toggleVisibility() {
    this.visibility = if (this.visibility == View.VISIBLE) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

fun Float.roundConfidence(): Double {
    return ((this * 100.0).roundToInt() / 100.0)
}