package com.seyone22.cook.helper

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeHelper {
    fun LocalDateTime.toIsoString(): String {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        return this.format(formatter)
    }
}