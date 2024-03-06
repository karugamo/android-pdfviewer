package com.danjdt.pdfviewer.view.adapter

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.view.View
import android.widget.ImageView
import com.danjdt.pdfviewer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class DefaultPdfPageViewHolder(
    view: View,
    private val scope: CoroutineScope,
    private val renderBlock: suspend (position: Int) -> Result<Bitmap>,
) : PdfPageViewHolder(view) {
    private val imageView: ImageView = itemView.findViewById(R.id.image)

    private var renderJob: Job? = null

    override fun bind(position: Int) {
        renderJob?.cancel()
        renderJob = scope.launch {
            val renderResult = renderBlock(position)
            ensureActive()
            renderResult.onSuccess { page ->
                imageView.layoutParams.height =  (page.height.toDouble() * imageView.width / page.width).toInt()
                println("imageView.set with bitmap ${page.height}/${page.width}")
                imageView.setImageBitmap(page)

                val borderSize = 10 // Adjust border size as needed
                val borderColor = 0xFFFF0000.toInt() // Red color
                val borderDrawable = ShapeDrawable(RectShape())
                borderDrawable.paint.color = borderColor
                borderDrawable.paint.strokeWidth = borderSize.toFloat()
                borderDrawable.paint.style = Paint.Style.STROKE
                imageView.background = borderDrawable
            }
        }
    }
}