package com.st.ui.utils

import com.st.ui.R

fun getBlueStIcon(index: Int): Int {
    if (index < 0 || index > BLUE_ST_SDK_V2_ICONS.lastIndex)
        return BLUE_ST_SDK_V2_ICONS.last()
    return BLUE_ST_SDK_V2_ICONS[index]
}

private val BLUE_ST_SDK_V2_ICONS = intArrayOf(
    /* 0  -> Low Battery */
    R.drawable.ic_battery_00,
    /* 1  -> Battery ok */
    R.drawable.ic_battery_60,
    /* 2  -> Battery Full */
    R.drawable.ic_battery_100,
    /* 3  -> Battery Charging */
    R.drawable.ic_battery_80c,
    /* 4  -> Message */
    R.drawable.ic_message,
    /* 5  -> Warning/Alarm */
    R.drawable.ic_warning,
    /* 6  -> Error */
    R.drawable.ic_error,
    /* 7  -> Ready */
    R.drawable.ic_ready_outline,
    /* 8  -> Waiting Pairing */
    R.drawable.ic_bluetooth_waiting,
    /* 9  -> Paired */
    R.drawable.ic_bluetooth_connected,
    /* 10 -> Log On going */
    R.drawable.ic_log_on_going,
    /* 11 -> Memory Full */
    R.drawable.ic_disc_full,
    /* 12 -> Connected to Cloud */
    R.drawable.ic_cloud_done,
    /* 13 -> Connecting to Cloud */
    R.drawable.ic_cloud_upload,
    /* 14 -> Cloud not Connected */
    R.drawable.ic_cloud_off,
    /* 15 -> GPS found */
    R.drawable.ic_gps_fixed,
    /* 16 -> GPS not Found */
    R.drawable.ic_gps_not_fixed,
    /* 17 -> GPS Off */
    R.drawable.ic_gps_off,
    /* 18 -> Led On */
    R.drawable.ic_flash_on,
    /* 19 -> Led Off */
    R.drawable.ic_flash_off,
    /* 20 -> Link On */
    R.drawable.ic_link_on,
    /* 21 -> Link Off */
    R.drawable.ic_link_off,
    /* 22 -> Wi-Fi On */
    R.drawable.ic_wifi_on,
    /* 23 -> Wi-Fi Off */
    R.drawable.ic_wifi_off,
    /* 24 -> Wi-Fi Tethering */
    R.drawable.ic_wifi_tethering,
    /* 25 -> Low Power */
    R.drawable.ic_battery_saver,
    /* 26 -> Sleeping */
    R.drawable.ic_sleep_hotel,
    /* 27 -> High Power */
    R.drawable.ic_battery_charging_full,
    /* 28 -> Microphone On */
    R.drawable.ic_mic_on,
    /* 29 -> Microphone Off */
    R.drawable.ic_mic_off,
    /* 30 -> Play */
    R.drawable.ic_play,
    /* 31 -> Pause */
    R.drawable.ic_pause,
    /* 32 -> Stop */
    R.drawable.ic_stop,
    /* 33 -> Sync On */
    R.drawable.ic_sync_on,
    /* 34 -> Sync Off */
    R.drawable.ic_sync_off,
    /* 35 -> Sync Error */
    R.drawable.ic_sync_error,
    /* 36 -> Lock */
    R.drawable.ic_lock,
    /* 37 -> Not Lock */
    R.drawable.ic_lock_open,
    /* 38 -> Star */
    R.drawable.ic_star,
    /* 39 -> Very dissatisfied */
    R.drawable.ic_very_dissatisfied,
    /* 40 -> Dissatisfied */
    R.drawable.ic_dissatisfied,
    /* 41 -> Satisfied */
    R.drawable.ic_satisfied,
    /* 42 -> Very satisfied */
    R.drawable.ic_very_satisfied,
    /* 43 -> Sick */
    R.drawable.ic_sick,
    /* 44 -> Share */
    R.drawable.ic_share,
    /* 45 -> Filter 1 */
    R.drawable.ic_baseline_filter_1,
    /* 46 -> Filter 2 */
    R.drawable.ic_baseline_filter_2,
    /* 47 -> Filter 3 */
    R.drawable.ic_baseline_filter_3,
    /* 48 -> Filter 4 */
    R.drawable.ic_baseline_filter_4,
    /* 49 -> Filter 5 */
    R.drawable.ic_baseline_filter_5,
    /* 50 -> Filter 6 */
    R.drawable.ic_baseline_filter_6,
    /* 51 -> Filter 7 */
    R.drawable.ic_baseline_filter_7,
    /* 52 -> Filter 8 */
    R.drawable.ic_baseline_filter_8,
    /* 53 -> Filter 9 */
    R.drawable.ic_baseline_filter_9,
    /* 54 -> Filter 9+ */
    R.drawable.ic_baseline_filter_9_plus,
    /* 55 (mMaxIconCode) -> Icon Code not Recognized  */
    R.drawable.ic_help
)
