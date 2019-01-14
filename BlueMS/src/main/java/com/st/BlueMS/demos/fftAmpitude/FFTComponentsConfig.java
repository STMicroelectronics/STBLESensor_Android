package com.st.BlueMS.demos.fftAmpitude;

import android.graphics.Color;

class FFTComponentsConfig {

    static class LineConf {
        final int color;
        final String name;

        private LineConf(int color, String name) {
            this.color = color;
            this.name = name;
        }
    }

    static final LineConf LINES[] = new LineConf[]{
            new LineConf(Color.RED,"X"),
            new LineConf(Color.BLUE,"Y"),
            new LineConf(Color.GREEN,"Z")
    };

}
