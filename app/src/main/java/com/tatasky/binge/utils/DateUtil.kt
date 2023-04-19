package com.tatasky.binge.utils

import com.tatasky.binge.analytics.ANALYTICS_TIME_FORMAT
import com.tatasky.binge.analytics.DEAFULT_DATE_FORMAT_FROM_BE
import com.tatasky.binge.analytics.HH_MM_WITH_MERIDIAN_TIME_FORMAT
import com.tatasky.binge.analytics.START_DATE_FORMAT
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

fun Long?.getEpochTimeInFormat(pattern: String = HH_MM_WITH_MERIDIAN_TIME_FORMAT /*return 05:00 AM/PM*/) =
    this?.let {
        val date = Date(it)
        val sdf = SimpleDateFormat(pattern, Locale.ROOT)
        sdf.format(date)
    }

fun getCurrentDateInMillis(): Long{
    val currentDate = Calendar.getInstance()
    currentDate.apply {
        this.set(Calendar.HOUR_OF_DAY, 0)
        this.set(Calendar.MINUTE, 0)
        this.set(Calendar.SECOND, 0)
        this.set(Calendar.MILLISECOND, 0)
    }
    return currentDate.timeInMillis
}

fun getDifferenceBetweenTwoDates(date1: String /*Tested format: dd/MM/yyyy*/, date2: String, dateFormat: String): String {
    return try {
        val d1 = getDateObject(date1, dateFormat)
        val d2 = getDateObject(date2, dateFormat)
        val difference = (d1?.time?.minus(d2?.time ?: 0) ?: 0)
        val differenceOfDates = difference / (24 * 60 * 60 * 1000)
        if (differenceOfDates < 0L) //date1 is of Past
            "0"
        else
            differenceOfDates.toString() //Eg. 1, 2, 3
    }
    catch (e: Exception) {
        e.printStackTrace()
        "0"
    }
}

fun getCurrentDateInFormat(dateFormat: String): String {
    return try {
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        val netDate = Date()
        sdf.format(netDate)
    } catch (ex: java.lang.Exception) {
        ""
    }
}

fun getDateObjectInNewFormat(
    existingDateFormat: String = DEAFULT_DATE_FORMAT_FROM_BE,
    newDateFormat: String = ANALYTICS_TIME_FORMAT,
    date: String?
): String {
    return try {
        val input = SimpleDateFormat(existingDateFormat, Locale.getDefault())
        val output = SimpleDateFormat(newDateFormat, Locale.getDefault())
        val oldParsedDate = date?.let { input.parse(it) }
        oldParsedDate?.let { output.format(it) }?.let { getDateObject(it, newDateFormat) }
        ""
    } catch (e: Exception) {
        ""
    }
}