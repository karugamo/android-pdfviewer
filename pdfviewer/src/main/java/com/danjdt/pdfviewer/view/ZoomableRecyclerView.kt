package com.danjdt.pdfviewer.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

private const val DEFAULT_MAX_ZOOM = 3f
private const val DEFAULT_IS_ZOOM_ENABLED = true
private const val MIN_ZOOM = 1f

class ZoomableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    var isZoomEnabled: Boolean = DEFAULT_IS_ZOOM_ENABLED
    var maxZoom: Float = DEFAULT_MAX_ZOOM
    private var scaleFactor: Float = MIN_ZOOM

    private var isScaling = false

    private val scaleDetector: ScaleGestureDetector by lazy { ScaleGestureDetector(context, ScaleListener()) }
    private val gestureDetector: GestureDetectorCompat by lazy { GestureDetectorCompat(context, GestureListener()) }

    private var scaleFocusX = 0f
    private var scaleFocusY = 0f

    private var tranX = 0f
    private var tranY = 0f
    private var maxTranX = 0f
    private var maxTranY = 0f

    init {
        layoutManager = ZoomableLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(this)
    }

    fun calculateScrollAmount(dy: Int): Int {
        return when {
            dy == 0 -> {
                0
            }
            dy > 0 -> { // Scrolling down
                if (tranY > -maxTranY) { // Don't allow scroll, consume translation first
                    0
                } else {
                    (dy / scaleFactor).toInt()
                }
            }
            else -> { // Scrolling up
                if (tranY < 0) { // Don't allow scroll, consume translation first
                    0
                } else {
                    (dy / scaleFactor).toInt()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isZoomEnabled) {
            return super.onTouchEvent(ev)
        }
        var returnValue = scaleDetector.onTouchEvent(ev)
        returnValue = gestureDetector.onTouchEvent(ev) || returnValue
        return super.onTouchEvent(ev) || returnValue
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.translate(tranX, tranY)
        canvas.scale(scaleFactor, scaleFactor)

        super.dispatchDraw(canvas)
    }

    private inner class ScaleListener : OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val previousScaleFactor = scaleFactor
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(MIN_ZOOM, maxZoom)
            maxTranX = width * scaleFactor - width
            maxTranY = height * scaleFactor - height
            scaleFocusX = detector.focusX
            scaleFocusY = detector.focusY

            tranX += scaleFocusX * (previousScaleFactor - scaleFactor)
            tranX = tranX.coerceIn(-maxTranX, 0f)
            tranY += scaleFocusY * (previousScaleFactor - scaleFactor)
            tranY = tranY.coerceIn(-maxTranY, 0f)

            overScrollMode = if (scaleFactor > MIN_ZOOM) OVER_SCROLL_NEVER else OVER_SCROLL_IF_CONTENT_SCROLLS

            invalidate()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (!isScaling) {
                if (scaleFactor > MIN_ZOOM) {
                    val newTranX = tranX - distanceX
                    tranX = newTranX.coerceIn(-maxTranX, 0f)
                    val newTranY = tranY - distanceY
                    tranY = newTranY.coerceIn(-maxTranY, 0f)
                    invalidate()
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Calculate the tap position relative to the scaled and translated PDF content
            val scaledX = (e.x - tranX) / scaleFactor
            val scaledY = (e.y - tranY) / scaleFactor

            // Print out or use the relative position as needed
            println("Tapped position relative to PDF: x=$scaledX, y=$scaledY")

            // You might want to use this information for other purposes,
            // such as selecting or interacting with specific elements within the PDF.

            return super.onSingleTapConfirmed(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {

            if (scaleFactor == MIN_ZOOM) {
                animateZoomTo(e.x, e.y, scaleFactor, maxZoom) // Zoom in
            } else {
                animateZoomTo(tranX / (1 - scaleFactor), tranY / (1 - scaleFactor), scaleFactor, MIN_ZOOM) // Reset zoom
            }
            return true
        }

        private fun animateZoomTo(x: Float, y: Float, currentScale: Float, targetScale: Float) {
            val animator = ValueAnimator.ofFloat(currentScale, targetScale)
            animator.duration = 300 // Animation duration in milliseconds
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener { animation ->
                scaleFactor = animation.animatedValue as Float
                // Recalculate max translations in case of a scale change
                maxTranX = width * scaleFactor - width
                maxTranY = height * scaleFactor - height
                // Center the content
                tranX = x * (1 - scaleFactor)
                tranY = y * (1 - scaleFactor)
                invalidate()
            }
            animator.start()
        }



    }
}