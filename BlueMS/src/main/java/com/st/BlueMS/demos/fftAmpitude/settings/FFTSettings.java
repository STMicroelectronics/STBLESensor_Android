package com.st.BlueMS.demos.fftAmpitude.settings;

class FFTSettings {

    enum WindowType{
        RECTANGULAR,
        HANNING,
        HAMMING,
        FLAT_TOP;

        static WindowType fromByte(byte val){
            switch (val){
                case 0:
                    return RECTANGULAR;
                case 1:
                    return HANNING;
                case 2:
                    return HAMMING;
                case 3:
                    return FLAT_TOP;
                default:
                    return RECTANGULAR;
            }
        }

    }

    final short odr;
    final byte fullScale;
    final short size;
    final WindowType winType;
    final int acquisitionTime_s;
    final byte subRange;
    final byte overlap;

    FFTSettings(short odr, byte fullScale, short size, WindowType winType, int acquisitionTime_s,
                byte overlap, byte subRange) {
        this.odr = odr;
        this.fullScale = fullScale;
        this.size = size;
        this.winType = winType;
        this.acquisitionTime_s = acquisitionTime_s;
        this.subRange = subRange;
        this.overlap = overlap;
    }

}
