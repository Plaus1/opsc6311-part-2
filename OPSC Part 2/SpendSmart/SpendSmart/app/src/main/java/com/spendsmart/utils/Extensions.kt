package com.spendsmart.utils

import android.content.Context
import android.widget.Toast
import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun String.toMD5Hash(): String {
    val digest = MessageDigest.getInstance("MD5")
    val hashBytes = digest.digest(this.toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Double.toCurrencyString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(this)
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun getStartOfMonth(month: Int, year: Int): Long {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun getEndOfMonth(month: Int, year: Int): Long {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1, 23, 59, 59)
    cal.set(Calendar.MILLISECOND, 999)
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    return cal.timeInMillis
}

fun getStartOfDay(dateMillis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dateMillis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun getEndOfDay(dateMillis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dateMillis
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}

fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
