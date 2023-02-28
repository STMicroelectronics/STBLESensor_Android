/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
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
package com.st.BlueMS.demos.Audio.BlueVoice.fullBand;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import androidx.annotation.WorkerThread;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Class to extract PCM arrays from an audio file (with SAMPLE_RATE = 48000 kHZ)
 */
public class PCMExtractor {
    private static final String TAG = PCMExtractor.class.getCanonicalName();

    private ByteArrayOutputStream pcmChunkData;
    private static final int TIMEOUT = 10 * 1000;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int PKT_SIZE_48K = 1920;

    private short[] decoded_HChunk;
    private short[] decoded_FChunk;
    private byte[] remn_buff;
    private int index = 0;
    private int totcounter_H = 0;
    private int totcounter_F = 0;
    private int offset = 6;
    private boolean firstTimeConfig = true;
    private boolean firstHalf = true;
    private boolean inPause = false;
    private boolean isEoF = false;
    private boolean forcedStop = false;

    /**
     * it is called to stop the process
     */
    public void enableForcedStop(){
        this.forcedStop = true;
    }

    public boolean isStopped(){
        return forcedStop;
    }

    /**
     * used to check if the PCM Extractor is already configured or not
     * @return true if is already configured, false elsewhere
     */
    public boolean isConfiguredYet() {
        return !firstTimeConfig;
    }

    /**
     * Extract a PCM array of specific size from a bigger MediaCodec extracted PCM chunk. extract the
     * PCM array from decoded_HChunk and decoded_FChunk alternately (double buffer)
     * @return the decoded packet with the right size of {@value PKT_SIZE_48K} shorts (extracted PCM array)
     */
    public short[] getDecoded_Packet(){
        short[] subArray;
        if(firstHalf){
//            Log.d(TAG,"<--->--- get from H. index: " + index);
            subArray = Arrays.copyOfRange(decoded_HChunk, ((PKT_SIZE_48K/2) * index), ((PKT_SIZE_48K/2) * (index + 1)));
            index++;
            if((totcounter_H-index) == offset){
//                Log.d(TAG,"<--->--- H OFFSET " + index);
                inPause = false;
            } else if (index == totcounter_H){
//                Log.d(TAG,"H_index: " + index);
                firstHalf = !firstHalf;
                index = 0;
                if(isEoF) {
//                    Log.d(TAG,"H_return_null");
                    return null;
                }
            }
        } else {
//            Log.d(TAG,"---<---> get from F. index: " + index);
            subArray = Arrays.copyOfRange(decoded_FChunk, ((PKT_SIZE_48K/2) * index), ((PKT_SIZE_48K/2) * (index + 1)));
            index++;
            if((totcounter_F-index) == offset){
//                Log.d(TAG,"---<---> F OFFSET " + index);
                inPause = false;
            } else if (index == totcounter_F){
//                Log.d(TAG,"F_index: " + index);
                firstHalf = !firstHalf;
                index = 0;
                if(isEoF) {
//                    Log.d(TAG,"F_return_null");
                    return null;
                }
            }
        }
        return subArray;
    }

    /**
     * @param filePath Input Audio File
     * @return true if the conversion is completed correctly, false elsewhere, null if the MediaCodec
     * is not created correctly.
     * @throws IOException when decoding failed or opening the file failed.
     */
    @WorkerThread
    public Boolean convert(FileDescriptor filePath) throws IOException {
        pcmChunkData = new ByteArrayOutputStream();
        Log.i(TAG,"---> Extraction Started!");
        MediaExtractor mediaExtractor = new MediaExtractor();
        MediaCodec mediaCodec = null;
        mediaExtractor.setDataSource(filePath);
        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; ++i) {
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("audio")) {
                mediaExtractor.selectTrack(i);
                mediaCodec = MediaCodec.createDecoderByType(mimeType);
                mediaCodec.configure(format, null, null, 0);
                break;
            }
        }
        if (mediaCodec == null) {
            Log.e(TAG,"---> Decode Error! mediaCoded was not created correctly");
            return null;
        }

        mediaCodec.start();
        inPause = false;
        isEoF = false;
        remn_buff = null;
        firstTimeConfig = true;
        index = 0;
        firstHalf = true;
        Log.d(TAG,"isEoF = FALSE");
        Log.i(TAG,"---> Decode Started!");
        while (!decode(mediaCodec, mediaExtractor) && !forcedStop) {
            //Do nothing.
            //Wait for decoding.
        }

        pcmChunkData.close();
        pcmChunkData = null;
        isEoF = true;
        Log.d(TAG,"isEoF = TRUE");
        Log.i(TAG,"---> Decode Completed!");
        forcedStop = false;
        return true;
    }

    private int readData(MediaExtractor extractor, ByteBuffer buffer, int size) {
        int totalSize = 0;
        do {
            int sampleSize = extractor.readSampleData(buffer, 0);
            if (sampleSize < 0) {
                return totalSize;
            }
            totalSize += sampleSize;
            //Advance to the next sample.
            extractor.advance();
        } while (totalSize < size);
        return totalSize;
    }

    /**
     * Realize the decode process filling decoded_HChunk and decoded_FChunk alternately (double buffer)
     * @param codec the MediaCodec
     * @param extractor the MediaExtractor
     * @return true when the decode process terminates, false if the PCM extraction process is still in progress
     * @throws IOException
     */
    private boolean decode(MediaCodec codec, MediaExtractor extractor) throws IOException {
        if(!inPause) {
            ByteBuffer[] inputData = codec.getInputBuffers();
            ByteBuffer[] outputData = codec.getOutputBuffers();

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            //Get the max input size
            int size = DEFAULT_BUFFER_SIZE;
            //NOTE maybe to remove START
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                size = codec.getInputFormat().getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            }*/
            //NOTE maybe to remove STOP
            if(remn_buff!=null) {
                pcmChunkData.write(remn_buff);
//                Log.d(TAG,"remn_buff length: " + remn_buff.length);
            }
            for (int i = 0; i < inputData.length - 1; ++i) {
                int inputIndex = codec.dequeueInputBuffer(-1);
                if (inputIndex < 0) {
                    return true;
                }
                //Input data
                ByteBuffer inputBuffer = inputData[inputIndex];
                inputBuffer.clear();
                //Request audio data chunk.
                int sampleSize = readData(extractor, inputBuffer, size);

                if (sampleSize <= 0) {
                    return true;
                } else {
                    //Get it decoded.
                    codec.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);
                }
                //Get output buffer
                int outputIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT);
                ByteBuffer outputBuffer;
                byte[] chunkPCM;
                while (outputIndex >= 0) {
                    outputBuffer = outputData[outputIndex];
                    chunkPCM = new byte[bufferInfo.size];
                    outputBuffer.get(chunkPCM);
                    outputBuffer.clear();
                    //Write to a byte array.
                    pcmChunkData.write(chunkPCM);
                    codec.releaseOutputBuffer(outputIndex, false);
                    //Get next PCM chuck.
                    outputIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT);
                }
            }

            int totcounter = Math.abs(pcmChunkData.size()/PKT_SIZE_48K);
            offset = totcounter/2;
            byte[] bytes = pcmChunkData.toByteArray();
            byte[] pcmByteData = Arrays.copyOfRange(bytes, 0,(totcounter*PKT_SIZE_48K));
            remn_buff = Arrays.copyOfRange(bytes, totcounter*PKT_SIZE_48K, pcmChunkData.size());

            pcmChunkData.reset();

            if(firstTimeConfig){
//                    Log.d(TAG,"start set bufferH");
                decoded_HChunk = new short[(totcounter*PKT_SIZE_48K)/2];
                ByteBuffer.wrap(pcmByteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(decoded_HChunk);
//                    Log.d(TAG,"stop set bufferH");
                totcounter_H = totcounter;
                firstTimeConfig = false;
            } else {
                if (firstHalf) {
//                        Log.d(TAG,"start set bufferF");
                    decoded_FChunk = new short[(totcounter*PKT_SIZE_48K)/2];
                    ByteBuffer.wrap(pcmByteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(decoded_FChunk);
//                        Log.d(TAG,"stop set bufferF");
                    totcounter_F = totcounter;
                }
                else {
//                        Log.d(TAG,"start set bufferH");
                    decoded_HChunk = new short[(totcounter*PKT_SIZE_48K)/2];
                    ByteBuffer.wrap(pcmByteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(decoded_HChunk);
//                        Log.d(TAG,"stop set bufferH");
                    totcounter_H = totcounter;
                }
            }
            inPause = true;
        }
        //Have not decoded the whole file.
        return false;
    }
}
