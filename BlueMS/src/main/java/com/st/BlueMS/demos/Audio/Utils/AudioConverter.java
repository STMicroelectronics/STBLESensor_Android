package com.st.BlueMS.demos.Audio.Utils;


import java.io.IOException;
import java.io.OutputStream;

public class AudioConverter {

    public static byte[] toLEByteArray(short[] in){
        byte[] out = new byte[in.length*2];
        int index =0;
        for (short val : in) {
            out[index++] = (byte) (val & 0x00FF);
            out[index++] = (byte) (val >> 8);
        }
        return out;
    }

    public static byte[] upSamplingSignalToLE(short[] data){
        byte[] out = new byte[data.length*4];
        upSamplingSignalToLE(data,out);
        return out;
    }


    public static void upSamplingSignalToLE(short[] in,byte[] out){
        int index =0;
        for (short val : in){
            out[index] =(byte) (val & 0x00FF);
            out[index+1] =(byte) (val >> 8);
            out[index+2] =(byte) (val & 0x00FF);
            out[index+3] =(byte) (val >> 8);
            index+=4;
        }
    }

    public static void upSamplingSignalToLE(short[] data, OutputStream out) throws IOException {
        for (short val : data){
            out.write(val & 0x00FF);
            out.write(val >> 8);
            out.write(val & 0x00FF);
            out.write(val >> 8);
        }
    }

    public static void toLEByteStream(short[] data, OutputStream out) throws IOException {
        for (short val : data) {
            out.write(val & 0x00FF);
            out.write(val >> 8);
        }
    }
}
