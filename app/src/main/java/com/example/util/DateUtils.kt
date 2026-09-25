package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val standardDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    private val yearMonthQueryFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    fun getTodayString(): String {
        return standardDateFormat.format(Date())
    }

    fun getTodayEpochDay(): Long {
        return toEpochDay(Calendar.getInstance())
    }

    fun toEpochDay(cal: Calendar): Long {
        // Clear time components for pure date comparison
        val c = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
        return c.timeInMillis / (24L * 60L * 60L * 1000L)
    }

    fun toEpochDay(year: Int, monthZeroIndexed: Int, dayOfMonth: Int): Long {
        val c = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, monthZeroIndexed, dayOfMonth)
        }
        return c.timeInMillis / (24L * 60L * 60L * 1000L)
    }

    fun parseDateStringToEpochDay(dateStr: String): Long {
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val day = parts[2].toInt()
                toEpochDay(year, month, day)
            } else {
                getTodayEpochDay()
            }
        } catch (e: Exception) {
            getTodayEpochDay()
        }
    }

    fun epochDayToDateString(epochDay: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = epochDay * 24L * 60L * 60L * 1000L
        }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m, d)
    }

    fun epochDayToCalendar(epochDay: Long): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = epochDay * 24L * 60L * 60L * 1000L
        }
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = standardDateFormat.parse(dateStr)
            if (date != null) displayDateFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatShortDate(dateStr: String): String {
        return try {
            val date = standardDateFormat.parse(dateStr)
            if (date != null) shortDateFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatMonthDisplay(yearMonthStr: String): String {
        return try {
            val date = yearMonthQueryFormat.parse(yearMonthStr)
            if (date != null) monthYearFormat.format(date) else yearMonthStr
        } catch (e: Exception) {
            yearMonthStr
        }
    }

    fun getDayOfWeekChinese(dateStr: String): String {
        return try {
            val date = standardDateFormat.parse(dateStr) ?: return ""
            val cal = Calendar.getInstance().apply { time = date }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "星期日"
                Calendar.MONDAY -> "星期一"
                Calendar.TUESDAY -> "星期二"
                Calendar.WEDNESDAY -> "星期三"
                Calendar.THURSDAY -> "星期四"
                Calendar.FRIDAY -> "星期五"
                Calendar.SATURDAY -> "星期六"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun getCurrentYearMonth(): String {
        return yearMonthQueryFormat.format(Date())
    }

    fun getPreviousMonth(yearMonthStr: String): String {
        return try {
            val date = yearMonthQueryFormat.parse(yearMonthStr) ?: return yearMonthStr
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.MONTH, -1)
            }
            yearMonthQueryFormat.format(cal.time)
        } catch (e: Exception) {
            yearMonthStr
        }
    }

    fun getNextMonth(yearMonthStr: String): String {
        return try {
            val date = yearMonthQueryFormat.parse(yearMonthStr) ?: return yearMonthStr
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.MONTH, 1)
            }
            yearMonthQueryFormat.format(cal.time)
        } catch (e: Exception) {
            yearMonthStr
        }
    }

    fun addDays(dateStr: String, days: Int): String {
        return try {
            val date = standardDateFormat.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, days)
            }
            standardDateFormat.format(cal.time)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getStartAndEndOfMonth(yearMonthStr: String): Pair<Long, Long> {
        return try {
            val date = yearMonthQueryFormat.parse(yearMonthStr) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val startEpoch = toEpochDay(cal)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, maxDay)
            val endEpoch = toEpochDay(cal)
            Pair(startEpoch, endEpoch)
        } catch (e: Exception) {
            val today = getTodayEpochDay()
            Pair(today - 30, today)
        }
    }

    fun getPresetRange(preset: RangePreset): Pair<Long, Long> {
        val today = getTodayEpochDay()
        val cal = Calendar.getInstance()

        return when (preset) {
            RangePreset.LAST_7_DAYS -> Pair(today - 6, today)
            RangePreset.LAST_14_DAYS -> Pair(today - 13, today)
            RangePreset.LAST_30_DAYS -> Pair(today - 29, today)
            RangePreset.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = toEpochDay(cal)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                val end = toEpochDay(cal)
                Pair(start, end)
            }
            RangePreset.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = toEpochDay(cal)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                val end = toEpochDay(cal)
                Pair(start, end)
            }
            RangePreset.ALL_TIME -> Pair(today - 365, today)
        }
    }
}

enum class RangePreset(val label: String) {
    LAST_7_DAYS("最近7天"),
    LAST_14_DAYS("最近14天"),
    LAST_30_DAYS("最近30天"),
    THIS_MONTH("本月"),
    LAST_MONTH("上個月"),
    ALL_TIME("全期間")
}
