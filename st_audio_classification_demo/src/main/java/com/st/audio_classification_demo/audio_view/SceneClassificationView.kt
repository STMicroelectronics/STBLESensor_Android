package com.st.audio_classification_demo.audio_view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.AttrRes
import com.st.audio_classification_demo.R
import com.st.blue_sdk.features.extended.audio_classification.AudioClassType

class SceneClassificationView : AudioView {
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
     * image to select for the indoor scene
     */
    private lateinit var mIndoorImage: ImageView

    /**
     * image to select for the outdoor scene
     */
    private lateinit var mOutdoorImage: ImageView

    /**
     * image to select for the in-vehicle scene
     */
    private lateinit var mInVehicleImage: ImageView

    private fun initView(context: Context) {
        inflate(context, R.layout.view_audio_scene_classification, this)
        mIndoorImage = findViewById(R.id.audio_scene_classification_indoorImage)
        mOutdoorImage = findViewById(R.id.audio_scene_classification_outdoorImage)
        mInVehicleImage = findViewById(R.id.audio_scene_classification_invehicleImage)

        deselectAllImages()
    }

    override fun getSelectedImage(status: AudioClassType): ImageView? {
        return when (status) {
            AudioClassType.Indoor -> mIndoorImage
            AudioClassType.Outdoor -> mOutdoorImage
            AudioClassType.InVehicle -> mInVehicleImage
            else -> null
        }
    }
}