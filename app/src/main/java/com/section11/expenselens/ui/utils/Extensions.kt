package com.section11.expenselens.ui.utils

import java.util.Locale

private const val TWO_DECIMALS_FORMAT = "%.2f"

fun Double.formatToTwoDecimal()  = formatToTwoDecimal(this)

fun Float.formatToTwoDecimal() = formatToTwoDecimal(this)

private fun <T> formatToTwoDecimal(value: T): String {
    return String.format(Locale.getDefault(), TWO_DECIMALS_FORMAT, value)
}
