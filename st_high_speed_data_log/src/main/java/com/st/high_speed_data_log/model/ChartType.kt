package com.st.high_speed_data_log.model

enum class ChartType(val channelIndex: Int) {
    ALL(channelIndex = -1),
    SUM(channelIndex = -1),
    CHANNEL_1(channelIndex = 0),
    CHANNEL_2(channelIndex = 1),
    CHANNEL_3(channelIndex = 2),
}
