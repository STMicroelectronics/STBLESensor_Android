/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.util

import com.st.blue_sdk.features.extended.ext_configuration.CustomCommand
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfigCommandAnswers
import com.st.ext_config.model.CustomCommandType
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.timeToString(): String {
    val timeFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = timeFormat.format(this)
    return String.format(Locale.getDefault(), "%s", timeStr)
}

fun Date.dateToString(): String {
    val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val dateStr = dateFormat.format(this)
    val cal = Calendar.getInstance()
    cal.time = this
    val dayOfTheWeek = cal[Calendar.DAY_OF_WEEK]
    if (dayOfTheWeek == Calendar.SUNDAY) Calendar.DAY_OF_WEEK else dayOfTheWeek - 1
    return String.format(Locale.getDefault(), "%02d/%s", dayOfTheWeek, dateStr)
}

fun ExtConfigCommandAnswers.update(other: ExtConfigCommandAnswers?): ExtConfigCommandAnswers {
    if (other == null) return this
    return ExtConfigCommandAnswers(
        commandList = other.commandList ?: this.commandList,
        info = other.info ?: this.info,
        help = other.help ?: this.help,
        certificate = other.certificate ?: this.certificate,
        versionFw = other.versionFw ?: this.versionFw,
        stm32UID = other.stm32UID ?: this.stm32UID,
        powerStatus = other.powerStatus ?: this.powerStatus,
        customCommandList = other.customCommandList ?: this.customCommandList,
        sensor = other.sensor ?: this.sensor,
        error = other.error ?: this.error,
        banksStatus = other.banksStatus ?: this.banksStatus
    )
}

val CustomCommand.customCommandType: CustomCommandType
    get() = try {
        CustomCommandType.valueOf(type?.uppercase() ?: "")
    } catch (ex: IllegalArgumentException) {
        CustomCommandType.UNKNOWN
    }
