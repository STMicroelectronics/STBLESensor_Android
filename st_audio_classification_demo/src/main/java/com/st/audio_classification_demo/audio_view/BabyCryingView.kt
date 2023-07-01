package com.st.audio_classification_demo.audio_view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.AttrRes
import com.st.audio_classification_demo.R
import com.st.blue_sdk.features.extended.audio_classification.AudioClassType

class BabyCryingView : AudioView {
    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    /**
     * image to select for not baby crying detection
     */
    private lateinit var mBabyIsNotCryingImage: ImageView

    /**
     * image to select for baby crying detection
     */
    private lateinit var mBabyIsCryingImage: ImageView

    private fun initView(context: Context) {
        inflate(context, R.layout.view_audio_baby_crying, this)
        mBabyIsCryingImage = findViewById(R.id.audio_babyCryingImage)
        mBabyIsNotCryingImage = findViewById(R.id.audio_babyNotCryingImage)
        deselectAllImages()
    }

    override fun getSelectedImage(status: AudioClassType): ImageView? {
        return when (status) {
            AudioClassType.BabyIsCrying -> mBabyIsCryingImage
            AudioClassType.Unknown -> mBabyIsNotCryingImage
            else -> null
        }
    }
}