package com.danjdt.pdfviewer.view.adapter


import android.view.View
import android.widget.TextView
import com.danjdt.pdfviewer.R

import com.danjdt.pdfviewer.view.adapter.PdfPageViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class DefaultPdfPageViewHolder(
    view: View,
    private val scope: CoroutineScope,
    private val renderBlock: suspend (position: Int) -> Result<String>,
) : PdfPageViewHolder(view) {
    private val textView: TextView = itemView.findViewById(R.id.text)

    private var renderJob: Job? = null

    override fun bind(position: Int) {
        renderJob?.cancel()
        renderJob = scope.launch {
            try {
                val renderResult = renderBlock(position)
                ensureActive()
                renderResult.onSuccess { textContent ->
                    textView.text = textContent
                }
                renderResult.onFailure { error ->
                    println("Failed to render text at position $position: $error")
                    // Handle failure gracefully, for example, show a placeholder text
                    // textView.text = "Error loading content"
                }
            } catch (e: Exception) {
                println("Exception occurred while rendering text at position $position: $e")
                // Handle exception gracefully, for example, show a placeholder text
                // textView.text = "Error loading content"
            }
        }
    }
}
