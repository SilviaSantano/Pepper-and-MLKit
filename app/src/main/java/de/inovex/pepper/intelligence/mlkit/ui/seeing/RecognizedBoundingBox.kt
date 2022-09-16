package de.inovex.pepper.intelligence.mlkit.ui.seeing

import android.graphics.*
import android.widget.ImageView

/**
 * Helper Class to draw a bounding box around an object over the preview.
 */
class RecognizedBoundingBox(imageView: ImageView, color: Int) {
    private val paint: Paint = Paint()
    private val myOptions = BitmapFactory.Options()
    var mutableBitmap: Bitmap

    fun drawRect(canvas: Canvas, rect: RectF) {
        canvas.drawRect(rect, paint)
    }

    fun drawPoint(canvas: Canvas, pointF: PointF) {
        canvas.drawPoint(pointF.x, pointF.y, paint)
    }

    init {
        paint.isAntiAlias = true
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4.0f
        paint.alpha = 255
        myOptions.inScaled = false
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
        imageView.adjustViewBounds = true
        mutableBitmap =
            Bitmap.createBitmap(imageView.width, imageView.height, myOptions.inPreferredConfig)
                .copy(Bitmap.Config.ARGB_8888, true)
    }
}
