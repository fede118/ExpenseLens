package com.section11.expenselens.domain

import java.util.Calendar
import java.util.Date

private const val LAST_HOUR = 23
private const val LAST_MINUTE = 59
private const val LAST_SECOND = 59
private const val LAST_MILLISECOND = 999

val firstDayOfCurrentMonth = Date().apply {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    time = calendar.time.time
}

val lastDayOfCurrentMonth = Date().apply {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    calendar.set(Calendar.HOUR_OF_DAY, LAST_HOUR)
    calendar.set(Calendar.MINUTE, LAST_MINUTE)
    calendar.set(Calendar.SECOND, LAST_SECOND)
    calendar.set(Calendar.MILLISECOND, LAST_MILLISECOND)
    time = calendar.time.time
}
