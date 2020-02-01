package com.st.BlueMS.physiobiometrics;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;


public class Sound {

    public   static SoundPool mSoundPool;
    public   static AssetManager mAssetManager;
    public   static int mStreamID;
    public   static boolean loaded;
    private  static final String TAG="SoundPool";


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int soundID, int status) {
                loaded = true;
                Log.e(TAG, ">>>>>>> soundID"+soundID+"    status:"+status);
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static void createOldSoundPool() {
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
    }

    public static int playSound(int sound) {
        if (sound > 0) {
            mStreamID = mSoundPool.play(sound, 1, 1, 1, 0, 1);
        }
        return mStreamID;
    }

    public static int loadSoundID(Context context, int asset) {
        return mSoundPool.load(context, asset, 1);
    }
}
