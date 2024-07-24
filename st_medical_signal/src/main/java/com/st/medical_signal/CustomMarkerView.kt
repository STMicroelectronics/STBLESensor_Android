package com.st.medical_signal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

@SuppressLint("ViewConstructor")
class CustomMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val uiScreenWidth = resources.displayMetrics.widthPixels
    private val tvContent: TextView = findViewById<View>(R.id.tvContent) as TextView

    // callbacks everytime the MarkerView is redrawn, can be used to update the content (user-interface)
    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry, highlight: Highlight) {

        tvContent.text = "[${e.y}]"

        // this will perform necessary layouting
        super.refreshContent(e, highlight)
    }

    override fun draw(canvas: Canvas, posx: Float, posy: Float) {
        // Check marker position and update offsets.
        var posX = posx
        val w = width
        if (uiScreenWidth - posX - w < w) {
            posX -= w.toFloat()
        }

        // translate to the correct position and draw
        canvas.translate(posX, posy)
        draw(canvas)
        canvas.translate(-posX, -posy)
    }
}