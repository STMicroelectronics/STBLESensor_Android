package com.st.BlueMS.demos.PianoDemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.st.BlueSTSDK.Features.FeaturePiano;

import java.util.ArrayList;


public class PianoView extends View {

    public static final int numberWhiteKeys = 14;
    private final Paint black, blue, white,line;
    private final ArrayList<PianoKey> whiteKeys = new ArrayList<>();
    private final ArrayList<PianoKey> blackKeys = new ArrayList<>();
    private int keyWidth, height,width;
    private PianoKey curPlayingKey = null;

    private FeaturePiano mFeature = null;
    private byte[] mKeyNote = null;
    private byte[] mCommand = new byte[2];

    public PianoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        black = new Paint();
        black.setColor(Color.BLACK);
        white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL);
        blue = new Paint();
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);

        line = new Paint();
        line.setColor(Color.BLACK);
        line.setStrokeWidth(20);
    }

    public void SetFeatureAndKeyMap(FeaturePiano feature, byte[] keyMap) {
        mFeature = feature;
        mKeyNote = keyMap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        keyWidth = h / numberWhiteKeys;
        height = w;
        width = h;
        int keyNumber = 0;

        for (int i = 0; i < numberWhiteKeys; i++) {
            int top = i * keyWidth;

            RectF rect = new RectF(0, top, h, top + keyWidth);
            whiteKeys.add(new PianoKey(rect, keyNumber,true));
            keyNumber++;

            //Add Black Keys
            if (i != 2 && i != 6 && i != 9 && i != 13) {
                rect = new RectF(0.40f * height, (float) i * keyWidth + 0.70f * keyWidth,
                        height,(float) (i+1) * keyWidth + 0.30f * keyWidth);
                blackKeys.add(new PianoKey(rect, keyNumber,false));
                keyNumber++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int i=1;
        //Draw White Keys + Border
        for (PianoKey k : whiteKeys) {
            canvas.drawRect(k.rect, k.pressed ? blue : white);
            canvas.drawLine(0, i*keyWidth, height, i * keyWidth, line);
            i++;
        }
        //Draw the first line after for showing it on some devices
        canvas.drawLine(0, 0, height, 0, line);

        //Draw Black Keys
        for (PianoKey k : blackKeys) {
            canvas.drawRect(k.rect, k.pressed ? blue : black);
        }

        //Draw the top and Bottom Lines
        canvas.drawLine(0, 0, 0, width, line);
        canvas.drawLine(height, 0, height, width, line);
    }

    private PianoKey keyForTouch(float x, float y) {
        //The Black are on top of White Keys
        for (PianoKey k : blackKeys) {
            if (k.rect.contains(x, y)) {
                return k;
            }
        }

        for (PianoKey k : whiteKeys) {
            if (k.rect.contains(x, y)) {
                return k;
            }
        }

        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            float x = event.getX(0);
            float y = event.getY(0);

            PianoKey k = keyForTouch(x, y);

            if (k != null) {
                k.pressed = true;
                mCommand[0] = FeaturePiano.START_SOUND;
                mCommand[1] = mKeyNote[k.sound];
                //Log.i("NoteVal", "Sound["+k.sound+"]="+mKeyNote[k.sound]);
                mFeature.writeCommand(mCommand);
                curPlayingKey = k;
                //For redrawing
                invalidate();
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (curPlayingKey != null) {
                curPlayingKey.pressed = false;
                mCommand[0] = FeaturePiano.STOP_SOUND;
                mFeature.writeCommand(mCommand);
                curPlayingKey = null;
                //For redrawing
                invalidate();
            }
        } else if(action == MotionEvent.ACTION_MOVE) {
            float x = event.getX(0);
            float y = event.getY(0);

            PianoKey k = keyForTouch(x, y);
            if(k!=null) {
                if (curPlayingKey != null) {
                    if (k.sound != curPlayingKey.sound) {
                        curPlayingKey.pressed = false;
                        mCommand[0] = FeaturePiano.STOP_SOUND;
                        mFeature.writeCommand(mCommand);
                        k.pressed = true;
                        mCommand[0] = FeaturePiano.START_SOUND;
                        mCommand[1] = mKeyNote[k.sound];
                        //Log.i("NoteVal", "Sound["+k.sound+"]="+mKeyNote[k.sound]);
                        mFeature.writeCommand(mCommand);
                        curPlayingKey = k;
                        //For redrawing
                        invalidate();
                    }
                }
            }
        }
        return true;
    }
}
