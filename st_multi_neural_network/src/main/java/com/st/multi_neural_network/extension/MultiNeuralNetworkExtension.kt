package com.st.multi_neural_network.extension

import com.st.blue_sdk.features.activity.ActivityType
import com.st.blue_sdk.features.extended.audio_classification.AudioClassType
import com.st.multi_neural_network.R

fun AudioClassType.imageResource(): Int {
    return when (this) {
        AudioClassType.Unknown -> R.drawable.audio_scene_unkown
        AudioClassType.Indoor -> R.drawable.audio_scene_inside
        AudioClassType.Outdoor -> R.drawable.audio_scene_outside
        AudioClassType.InVehicle -> R.drawable.audio_scene_invehicle
        AudioClassType.BabyIsCrying -> R.drawable.audio_scene_babycrying
        AudioClassType.AscOff -> R.drawable.ic_pause
        AudioClassType.AscOn -> R.drawable.ic_play_arrow
        AudioClassType.Error -> R.drawable.ic_error
    }
}

fun AudioClassType.stringResource(): Int {
    return when (this) {
        AudioClassType.Unknown -> R.string.audio_scene_unknown
        AudioClassType.Indoor -> R.string.audio_scene_indoor
        AudioClassType.Outdoor -> R.string.audio_scene_outdoor
        AudioClassType.InVehicle -> R.string.audio_scene_inVehicle
        AudioClassType.BabyIsCrying -> R.string.audio_baby_crying
        AudioClassType.AscOff -> R.string.audio_scene_off
        AudioClassType.AscOn -> R.string.audio_scene_on
        AudioClassType.Error -> R.string.audio_scene_error
    }
}

fun ActivityType.imageResource(): Int {
    return when (this) {
        ActivityType.NoActivity -> R.drawable.activity_unkown
        ActivityType.Stationary -> R.drawable.activity_stationary
        ActivityType.Walking -> R.drawable.activity_walking
        ActivityType.FastWalking -> R.drawable.activity_fastwalking
        ActivityType.Jogging -> R.drawable.activity_jogging
        ActivityType.Biking -> R.drawable.activity_biking
        ActivityType.Driving -> R.drawable.activity_driving
        ActivityType.Stairs -> R.drawable.activity_stairs
        ActivityType.AdultInCar -> R.drawable.activity_adult_in_car
        ActivityType.Error -> R.drawable.activity_unkown
    }
}

fun ActivityType.stringResource(): Int {
    return when (this) {
        ActivityType.NoActivity -> R.string.activityRecognition_unknownImageDesc
        ActivityType.Stationary -> R.string.activityRecognition_stationaryImageDesc
        ActivityType.Walking -> R.string.activityRecognition_walkingImageDesc
        ActivityType.FastWalking -> R.string.activityRecognition_fastWalkingImageDesc
        ActivityType.Jogging -> R.string.activityRecognition_joggingImageDesc
        ActivityType.Biking -> R.string.activityRecognition_bikingImageDesc
        ActivityType.Driving -> R.string.activityRecognition_drivingImageDesc
        ActivityType.Stairs -> R.string.activityRecognition_stairsImageDesc
        ActivityType.AdultInCar -> R.string.activityRecognition_adultInCar
        ActivityType.Error -> R.string.activityRecognition_unknownImageDesc
    }
}