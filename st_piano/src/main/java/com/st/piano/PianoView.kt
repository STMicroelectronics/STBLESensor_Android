/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.piano

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PianoView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val black: Paint = Paint()
    private val grey: Paint
    private val blue: Paint
    private val white: Paint
    private val line: Paint
    private val whiteKeys = ArrayList<PianoKey>()
    private val blackKeys = ArrayList<PianoKey>()
    private var keyWidth = 0
    private var heightW = 0
    private var widthW = 0
    private var curPlayingKey: PianoKey? = null

    private val mKeyByte = byteArrayOf(
        1, //NOTE_C1
        2, // NOTE_CS1
        3, // NOTE_D1
        4, // NOTE_DS1
        5, // NOTE_E1
        6, // NOTE_F1
        7, // NOTE_FS1
        8, // NOTE_G1
        9, // NOTE_GS1
        10, // NOTE_A1
        11, // NOTE_AS1
        12, // NOTE_B1
        13, // NOTE_C2
        14, // NOTE_CS2
        15, // NOTE_D2
        16, // NOTE_DS2
        17, // NOTE_E2
        18, // NOTE_F2
        19, // NOTE_FS2
        20, // NOTE_G2
        21, // NOTE_GS2
        22, // NOTE_A2
        23, // NOTE_AS2
        24 // NOTE_B2
    )

    private lateinit var viewModel: PianoViewModel
    private lateinit var nodeId: String

    init {
        black.color = Color.BLACK
        grey = Paint()
        grey.color = Color.GRAY
        white = Paint()
        white.color = Color.WHITE
        white.style = Paint.Style.FILL
        blue = Paint()
        blue.color = Color.BLUE
        blue.style = Paint.Style.FILL
        line = Paint()
        line.color = Color.BLACK
        line.strokeWidth = 20f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        keyWidth = h / numberWhiteKeys
        heightW = w
        widthW = h
        var keyNumber = 0
        for (i in 0 until numberWhiteKeys) {
            val top = i * keyWidth
            var rect = RectF(0F, top.toFloat(), h.toFloat(), (top + keyWidth).toFloat())
            whiteKeys.add(PianoKey(rect, null, keyNumber, true))
            keyNumber++

            //Add Black Keys
            if (i != 2 && i != 6 && i != 9 && i != 13) {
                rect = RectF(
                    0.40f * heightW, i.toFloat() * keyWidth + 0.70f * keyWidth,
                    heightW.toFloat(), (i + 1).toFloat() * keyWidth + 0.30f * keyWidth
                )
                val rectInt =  RectF(
                    0.45f * heightW, i.toFloat() * keyWidth + 0.80f * keyWidth,
                    heightW.toFloat(), (i + 1).toFloat() * keyWidth + 0.20f * keyWidth
                )
                blackKeys.add(PianoKey(rect, rectInt, keyNumber, false))
                keyNumber++
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        var i = 1
        //Draw White Keys + Border
        for (k in whiteKeys) {
            canvas.drawRect(k.rect, if (k.pressed) blue else white)
            canvas.drawLine(
                0f,
                (i * keyWidth).toFloat(),
                heightW.toFloat(),
                (i * keyWidth).toFloat(),
                line
            )
            i++
        }
        //Draw the first line after for showing it on some devices
        canvas.drawLine(0f, 0f, heightW.toFloat(), 0f, line)

        //Draw Black Keys
        for (k in blackKeys) {
            canvas.drawRect(k.rect, if (k.pressed) blue else black)
            k.rectInt?.let {
                if (!k.pressed) {
                    canvas.drawRect(k.rectInt!!, grey)
                }
            }
        }

        //Draw the top and Bottom Lines
        canvas.drawLine(0f, 0f, 0f, widthW.toFloat(), line)
        canvas.drawLine(heightW.toFloat(), 0f, heightW.toFloat(), widthW.toFloat(), line)
    }

    private fun keyForTouch(x: Float, y: Float): PianoKey? {
        //The Black are on top of White Keys
        for (k in blackKeys) {
            if (k.rect.contains(x, y)) {
                return k
            }
        }
        for (k in whiteKeys) {
            if (k.rect.contains(x, y)) {
                return k
            }
        }
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            val x = event.getX(0)
            val y = event.getY(0)
            val k = keyForTouch(x, y)
            if (k != null) {
                k.pressed = true
                viewModel.writePianoStartCommand(mKeyByte[k.sound], nodeId)
                curPlayingKey = k
                //For redrawing
                invalidate()
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (curPlayingKey != null) {
                curPlayingKey!!.pressed = false
                viewModel.writePianoStopCommand(nodeId)
                curPlayingKey = null
                //For redrawing
                invalidate()
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            val x = event.getX(0)
            val y = event.getY(0)
            val k = keyForTouch(x, y)
            if (k != null) {
                if (curPlayingKey != null) {
                    if ((k.sound - curPlayingKey!!.sound) != 0) {
                        curPlayingKey!!.pressed = false
                        k.pressed = true
                        viewModel.writePianoStartCommand(mKeyByte[k.sound], nodeId)
                        curPlayingKey = k
                        //For redrawing
                        invalidate()
                    }
                }
            }
        }
        return true
    }

    fun setViewModel(model: PianoViewModel, Idnode: String) {
        viewModel = model
        nodeId = Idnode
    }

    companion object {
        const val numberWhiteKeys = 14
    }
}