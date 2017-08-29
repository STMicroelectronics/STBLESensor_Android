/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
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

package com.st.BlueMS.demos.Audio.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.st.BlueMS.R;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A view that displays audio data on the screen as a waveform.
 */
public class WaveformView extends SurfaceView implements SurfaceHolder.Callback {
    // To make quieter sounds still show up well on the display, we use +/- 8192 as the amplitude
    // that reaches the top/bottom of the view instead of +/- 32767. Any samples that have
    // magnitude higher than this limit will simply be clipped during drawing.
    private static final float MAX_AMPLITUDE_TO_DRAW = Short.MAX_VALUE;

    // The number of buffer frames to keep around (for a nice fade-out visualization).
    private int mNSampleToPlot;

    // The queue that will hold historical audio data.
    private final LinkedList<Float> mAudioData;

    /////// lock stuff used to pause the rending thread
    private final Lock mRenderLock = new ReentrantLock();
    private final Condition mPauseDrawCondition = mRenderLock.newCondition();
    //when true the rendering thread will update the plot with the new data
    private boolean mUpdatePlot = false;
    private boolean mFirstTime = true;
    private float mYOffset;

    //render plot
    private  PlotDrawThread mRenderThread=null;

    //paint to use to plot the plot line
    private final Paint mPaint;

    public WaveformView(Context context) {
        this(context, null, 0);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mAudioData = new LinkedList<>();
        mNSampleToPlot =1;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.green));
        mPaint.setStrokeWidth(3);
        mPaint.setAntiAlias(true);
        getHolder().addCallback(this);

        mFirstTime = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mFirstTime = true;
    }

    /**
     * Updates the waveform view with a new "frame" of samples and renders it. The new frame gets
     * added to the front of the rendering queue, pushing the previous frames back, causing them to
     * be faded out visually.
     *
     * @param buffer the most recent buffer of audio samples
     */
    @WorkerThread
    public void updateAudioData(short[] buffer) {
        float newSample = buffer[0]/MAX_AMPLITUDE_TO_DRAW;
        //add the data to the plot buffer
        synchronized (mAudioData){
            if (mAudioData.size() == mNSampleToPlot) {
                mAudioData.removeFirst();
            }
            mAudioData.addLast(newSample);
        }
    }

    /**
     * Paint the audio data into the canvas
     * @param canvas canvas where paint the plot
     * @param dataToPlot data to plot
     * a maximum of 1 sample for each pixel will be plotted to avoid to overload the system
     */
    private void drawWaveform(Canvas canvas, Float[] dataToPlot) {

        if(mFirstTime) {
            mNSampleToPlot = canvas.getWidth();
            mFirstTime =false;
            mYOffset = (canvas.getHeight() / 2);
        }

        // Clear the screen each time because SurfaceView won't do this for us.
        canvas.drawColor(Color.BLACK);

        int x=0;
        float lastY = mYOffset;
        for (Float sample : dataToPlot) {
            float y = sample * mYOffset + mYOffset;
            if (x != 0)
                canvas.drawLine(x-1, lastY, x, y, mPaint);

            lastY = y;
            x++;
        }
    }

    /**
     * call this method when the view became visible
     * this method will un block the render thread that will start to update the plot view
     */
    public void startPlotting(){
        enablePlotRepainting();
    }

    /**
     * this method will un block the render thread that will start to update the plot view
     */
    void enablePlotRepainting(){
        mRenderLock.lock();
        mUpdatePlot = true;
        mPauseDrawCondition.signal();
        mRenderLock.unlock();
    }

    /**
     * call this method when the view became invisible
     * this method will block the render thread that will stop to update the plot view
     */
    public void stopPlotting(){
        mRenderLock.lock();
        mUpdatePlot = false;
        mPauseDrawCondition.signal();
        mRenderLock.unlock();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    /**
     * Thread that update the view
     */
    private class PlotDrawThread extends Thread{

        /**
         * true if the thread have to run
         */
        private boolean mRunning=true;

        /**
         * surface where the thread will draw the plot
         */
        private SurfaceHolder mSurfaceHolder;

        PlotDrawThread(SurfaceHolder sh){
            mSurfaceHolder=sh;
        }

        //keep the buffer where we copy the data to plot to avoid to do an allocation each frame.
        private Float mDataSnapshot[] = new Float[0];

        /**
         * create a copy of the data to plot this will avoid to lock for a long time the
         * audio data or have ghost update
         * @return data to plot
         */
        private Float[] getDataSnapshot(){
            synchronized (mAudioData){ //take a snapshot of the data to plot
                if(mAudioData.isEmpty())
                    return null;
                //else

                //reuse the old array if the size is correct
                if(mDataSnapshot.length!=mAudioData.size())
                    mDataSnapshot = new Float[mAudioData.size()];

                return mAudioData.toArray(mDataSnapshot);
            }//syncronized
        }

        /**
         * block the thread until the variable mUpdatePlot isn't true
         * this will avoid to have the thread running when the view is not displayed
         */
        void waitUpdatePlotRequest(){
            mRenderLock.lock();
            try {
                while (!mUpdatePlot) {
                    mPauseDrawCondition.await();
                }//while
            }catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                mRenderLock.unlock();
            }
        }

        @Override
        public void run() {
            while (mRunning) {

                waitUpdatePlotRequest();

                //after the wait we check again the runnig state
                if(!mRunning)
                    break;

                Float[] data = getDataSnapshot();

                if(data!=null) {
                    Canvas c = mSurfaceHolder.lockCanvas();
                    if (c != null) {
                        drawWaveform(c, data);
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }//while
        }

        void stopThread(){
            mRunning=false;
            //be secure that the thread is not waiting to repaing
            enablePlotRepainting();
        }

    }

    //when the surface is created, create also the thread that will update its view
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mRenderThread==null)
            mRenderThread = new PlotDrawThread(holder);
        mRenderThread.start();

    }

    // stop and destroy the thread that update the surface
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mRenderThread.stopThread();
        boolean retry = true;
        while (retry) {
            try {
                mRenderThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
        mRenderThread=null;

    }
}
