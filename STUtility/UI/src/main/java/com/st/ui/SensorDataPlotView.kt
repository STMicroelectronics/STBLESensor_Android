/*
 * Copyright (c) 2020  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart

class SensorDataPlotView : FrameLayout {

    private lateinit var titleView: TextView
    var title: CharSequence
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    private lateinit var xTitleView: TextView
    var xTitle: CharSequence?
        get() = xTitleView.text
        set(value) {
            xTitleView.setTextOrHide(value)
        }

    private lateinit var yTitleView: TextView
    var yTitle: CharSequence?
        get() = yTitleView.text
        set(value) {
            yTitleView.setTextOrHide(value)
        }

    private fun TextView.setTextOrHide(content: CharSequence?) {
        if (content != null) {
            text = content
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }

    lateinit var chart: LineChart

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.view_sensor_data_plot, this)
        initViewReference()
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.SensorDataPlotView, defStyle, 0)

        title = a.getString(R.styleable.SensorDataPlotView_plotView_title) ?: ""

        xTitle = a.getString(R.styleable.SensorDataPlotView_plotView_xTitle)
        yTitle = a.getString(R.styleable.SensorDataPlotView_plotView_yTitle)

        a.recycle()
    }

    private fun initViewReference() {
        titleView = findViewById(R.id.tagPlot_title)
        xTitleView = findViewById(R.id.tagPlot_xAxis)
        yTitleView = findViewById(R.id.tagPlot_yAxis)
        chart = findViewById(R.id.tagPlot_chart)
    }

}
