package com.st.piano.model

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset

data class PianoKeyRect(var topLeft: Offset, var size: Size , var sound: Int, var white: Boolean)

fun searchKey(x: Float, y: Float, whiteKeys: List<PianoKeyRect>,blackKeys: List<PianoKeyRect>) : PianoKeyRect? {
    for (key in blackKeys) {
        if((key.topLeft.x<=x) && (key.topLeft.y<=y) && ((key.topLeft.x+key.size.width)>x) &&  ((key.topLeft.y+key.size.height)>y)) {
            return key
        }
    }
    for (key in whiteKeys) {
        if((key.topLeft.x<=x) && (key.topLeft.y<=y) && ((key.topLeft.x+key.size.width)>x) &&  ((key.topLeft.y+key.size.height)>y)) {
            return key
        }
    }
    return null
}