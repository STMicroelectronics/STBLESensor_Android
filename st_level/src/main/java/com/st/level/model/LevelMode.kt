package com.st.level.model

enum class LevelDemo {
    PITCH_ROLL,
    PITCH,
    ROLL;

    override fun toString(): String {
        return when (this) {
            PITCH_ROLL -> "Pitch/Roll"
            PITCH -> "Pitch"
            ROLL -> "Roll"
        }
    }

    companion object {
        fun fromString(value: String): LevelDemo = when (value) {
            "Pitch/Roll" -> PITCH_ROLL
            "Pitch" -> PITCH
            "Roll" -> ROLL
            else -> ROLL
        }
    }
}
