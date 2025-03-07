package com.section11.expenselens.framework.utils

import android.os.Build
import android.os.Bundle
import com.section11.expenselens.domain.DomainConstants.EXPECTED_DATE_FORMAT
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Converts a timestamp (milliseconds) to a formatted date string, defaults to [EXPECTED_DATE_FORMAT],
 * ensuring correct handling of time zones.
 *
 * The first `toLocalDate()` extracts the date in **UTC**.
 * The second `toLocalDate()` ensures the date is adjusted correctly to **local time** after applying
 * the change to [ZoneId.systemDefault]
 */
fun Long.toDateString(dateFormat: String = EXPECTED_DATE_FORMAT): String {
    val localDate = Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toLocalDate()

    return localDate.format(DateTimeFormatter.ofPattern(dateFormat))
}

fun Date.toFormattedString(dateFormat: String = EXPECTED_DATE_FORMAT): String {
    val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
    return sdf.format(this)
}

fun String.toDate(dateFormat: String = EXPECTED_DATE_FORMAT): Date? {
    val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
    return sdf.parse(this)
}

inline fun <reified T> Bundle?.getArg(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this?.getParcelable(key) as? T
    }
}
