package com.st.BlueMS.demos.PianoDemo;

import android.graphics.RectF;

public class PianoKey {

    public int sound;
    public RectF rect;
    public boolean pressed;
    public boolean white;

    public PianoKey(RectF rect, int sound, boolean white) {
        this.sound = sound;
        this.rect = rect;
        this.pressed = false;
        this.white = white;
    }
}
