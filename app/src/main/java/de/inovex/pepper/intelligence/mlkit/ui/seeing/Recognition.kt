package de.inovex.pepper.intelligence.mlkit.ui.seeing

import android.graphics.RectF

enum class Area(val number: Int) {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), NONE(0)
}

/**
 * Recognition item object with fields for the label, the probability, the location as bounding box
 * and the area of the image the object is in.
 */
class Recognition(
    val label: String,
    val confidence: Double,
    var location: RectF,
    var area: Area = Area.NONE
) {
    override fun toString(): String {
        return label
    }
}
