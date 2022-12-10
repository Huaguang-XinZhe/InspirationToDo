package com.liuzhihui.cao.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
object TimeUtil {

    private val thisYear = getCurrentTime()[0]
    val today: String = SimpleDateFormat("yyyy-MM-dd").format(Date())

    private val mapChineseNum = mapOf(
        "一" to "1", "二" to "2", "三" to "3", "四" to "4", "五" to "5", "六" to "6",
        "七" to "7", "八" to "8", "九" to "9", "十" to "10", "两" to "2", "日" to "7", "天" to "7",
        "十一" to "11", "十二" to "12"
    )

    /**
     * 固定节日、重要日期（不含年份）
     */
    private val mapFixedFestival by lazy { mapOf(
        "国庆" to "10-01", "国庆节" to "10-01", "元旦" to "01-01", "儿童节" to "06-01",
        "六一" to "06-01", "61" to "06-01", "劳动节" to "05-01", "五一" to "05-01", "51" to "05-01",
        "教师节" to "09-10", "情人节" to "02-14", "高考" to "06-07", "圣诞" to "12-25", "圣诞节" to "12-25"
    ) }

    /**
     * 变化节日（需传入年份）
     */
    private val mapChangeFestival by lazy { mapOf(
        // 用 festivalToSolar()
        "中秋" to "8,15", "中秋节" to "8,15", "端午" to "5,5", "端午节" to "5,5", "元宵" to "1,15",
        "元宵节" to "1,15", "重阳节" to "9,9", "7夕" to "9,9", "七夕" to "7,7", "七夕节" to "7,7",
        "大年三十" to "12,30", "除夕" to "12,30", "过年" to "12,30", "春节" to "1,1",
        // 用 getMdOrFdDate()
        "母亲节" to "5,2", "父亲节" to "6,3",
        // 用 chingMing()
        "清明" to "", "清明节" to "",
    ) }

    /**
     * 最近几天（含年份）
     */
    private val mapRecent by lazy { mapOf(
        "今天" to getRecent(), "今儿" to getRecent(), "今" to getRecent(), "明天" to getRecent(1), "明儿" to getRecent(1),
        "明" to getRecent(1), "后天" to getRecent(3), "大后天" to getRecent(4)
    ) }

    /**
     * 格式化 “周几、礼拜几、下星期几” 为标准日期
     * @param weekDate 周几、礼拜几、下星期几等字符串
     * @return 返回类 “2021-08-27” 形式的字符串
     */
    private fun formatWeekDate(weekDate: String): String {
        val chineseDay = weekDate.matchStr("一|二|三|四|五|六|七|日|天")
        val whichDay = mapChineseNum[chineseDay]!!.toInt()
        return if (!weekDate.contains("下")) getWeekDate(whichDay)
        else getWeekDate(whichDay, true)
    }

    /**
     * 将 2022年7月12日、10月14号、2022-8-14、30号、8-27 等表日期的字符串格式化
     * @return 返回类 “2021-08-27” 型字符串
     */
    fun formatDate(dateStr: String): String {
        val numList = dateStr.matchToList("\\d{1,4}")
        var year = thisYear
        var month = getCurrentTime()[1]
        var day = ""
        var monthSpace = ""
        var daySpace = ""
        when (numList.size) {
            1 -> {
                day = numList[0]
                if (day.toInt() < 10) daySpace = "0"
            }
            2 -> {
                month = numList[0].toInt()
                day = numList[1]
            }
            3 -> {
                val first = numList[0].toInt()
                year = if (numList[0].length == 4) first else "20$first".toInt()
                month = numList[1].toInt()
                day = numList[2]
                if (day.toInt() < 10) daySpace = "0"
            }
        }
        if (month < 10) monthSpace = "0"
        return "$year-$monthSpace$month-$daySpace$day"
    }

    /**
     * 判断匹配到的时段字符串是否表示下半天
     * @param matchedPeriod 匹配到的时段字符串
     */
    fun isAfternoon(matchedPeriod: String) = matchedPeriod.contains(Regex("下午|晚上?"))

    /**
     * 将下半天的未经格式化的时点转换为 24 小时制的格式化时点
     * @param timePoint 下半天的未经格式化的时点字符串
     */
    fun transform(timePoint: String): String {
        val timePointFormat = format(timePoint)
        val list = timePointFormat.matchToList("\\d{2}")
        val newHour = list[0].toInt() + 12
        return "$newHour:${list[1]}"
    }

    /**
     * 对时点进行统一格式转换
     * @param timePoint 待转换的时点字符串
     * @return 转换后的标准时点：08:00
     */
    fun format(timePoint: String): String {
        return when {
            timePoint.contains(Regex("\\d{1,2}:\\d{1,2}")) -> {
                val list = timePoint.split(":")
                val hour = list[0]
                if (hour.toInt() < 10) {
                    "0$hour:${list[1]}"
                } else {
                    timePoint
                }
            }
            timePoint.contains(Regex("\\d{1,2}：\\d{1,2}")) -> {
                val list = timePoint.split("：")
                val hour = list[0]
                if (hour.toInt() < 10) {
                    "0$hour:${list[1]}"
                } else {
                    timePoint.replace("：", ":")
                }
            }
            // 会匹配 11点、11点半
            timePoint.contains(Regex("\\d{1,2}点")) -> {
                val hour = timePoint.matchStr("\\d{1,2}")
                val newHour = if (hour.toInt() < 10) "0$hour" else hour
                if (!timePoint.contains("半")) {
                    "$newHour:00"
                } else {
                    "$newHour:30"
                }
            }
            // 最后三种情形：三点、三点半
            else -> {
                val whichPoint = timePoint.matchStr("十[一二]|[一二三四五六七八九十]")
                val hour = mapChineseNum[whichPoint]!!
                val newHour = if (hour.toInt() < 10) "0$hour" else hour
                if (!timePoint.contains("点半")) {
                    "$newHour:00"
                } else {
                    "$newHour:30"
                }
            }
        }
    }

    /**
     * 获取当前的年、月、日、小时、分钟，并以 Int 列表的形式输出
     */
    fun getCurrentTime(): List<Int> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"))
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)+1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        return listOf(year, month, day, hour, minutes)
    }

    /**
     * 判断未经格式化的日期是不是今天
     * @param dateStr 未经格式化的日期：2021年9月12日、周三、中秋（也有可能为空字符串）
     * @return 返回类 “2021-08-27” 形式的字符串
     */
    fun isToday(dateStr: String): Boolean {
        return if (dateStr.isNotEmpty()) {
            val date = getFormatDate(dateStr)
            date == today
        } else true
    }

    /**
     * 获取格式化后的日期：2021-08-30
     * @param dateStr 所有未经格式化的日期类型的字符串（不为空字符串）
     */
    fun getFormatDate(dateStr: String): String {
        val datePattern = "(\\d{2,4}年)?(\\d{1,2}月)?\\d{1,2}[号日]|(\\d{2,4}[-/])?\\d{1,2}[-/]\\d{1,2}"
        val weekDatePattern = "下?(周|礼拜|星期)[一二三四五六七日天\\d]"
        val recentPattern = "今[天儿]?|明[天儿]?|大?后天"
        val fixedFestivalPattern = "(明年)?(国庆节?|元旦|儿童节|六一|61|劳动节|五一|51|教师节|情人节|高考|圣诞节?)"
        val changeFestivalPattern = "(明年)?(中秋节?|端午节?|清明节?|元宵节?|重阳节|[7七]夕节?|大年三十|除夕|过年|春节|[母父]亲节)"
        val dateWithoutNextYear = if (dateStr.contains("明年")) {
            dateStr.replace("明年", "")
        } else dateStr
        var date = ""
        when {
            dateStr.contains(Regex(datePattern)) -> date = formatDate(dateStr)
            dateStr.contains(Regex(weekDatePattern)) -> date = formatWeekDate(dateStr)
            dateStr.contains(Regex(fixedFestivalPattern)) -> {
                val dateWithoutYear = mapFixedFestival[dateWithoutNextYear]!!
                val dateWithThisYear = "$thisYear-$dateWithoutYear"
                date = dateStr.getChangeFestivalDate(dateWithThisYear) { nextYear ->
                    "$nextYear-$dateWithoutYear"
                }
            }
            dateStr.contains(Regex(changeFestivalPattern)) -> {
                val monthAndDay = mapChangeFestival[dateWithoutNextYear]!!
                val list = monthAndDay.split(",")
                if (list[0] != "") {
                    val month = list[0].toInt()
                    val day = list[1].toInt()
                    date = if (dateStr.contains(Regex("(明年)?[母父]亲节"))) {
                        val mdOrFdThisYear = getMdOrFdDate(thisYear, month, day)
                        dateStr.getChangeFestivalDate(mdOrFdThisYear) { nextYear ->
                            getMdOrFdDate(nextYear, month, day)
                        }
                    } else {
                        val otherFestivalThisYear = festivalToSolar(thisYear, month, day)
                        dateStr.getChangeFestivalDate(otherFestivalThisYear) { nextYear ->
                            festivalToSolar(nextYear, month, day)
                        }
                    }
                } else {
                    // 清明
                    val chingMingThisYear = chingMing(thisYear)
                    date = dateStr.getChangeFestivalDate(chingMingThisYear) { nextYear ->
                        chingMing(nextYear)
                    }
                }
            }
            dateStr.contains(Regex(recentPattern)) -> date = mapRecent[dateStr]!!
        }
        return date
    }

    /**
     * 获取变化节日的真正日期（如果输入早于当前日期或含有明年，就返回该节日的明年日期，否则，返回该节日今年的日期）
     * @param dateThisYear 该节日今年的日期
     * @param block 返回该节日明年日期的 Lambda 表达式（不同的场景有不同的实现）
     */
    private fun String.getChangeFestivalDate(dateThisYear: String, block: (Int) -> String): String {
        val isBeforeToday = strToLong(dateThisYear) < strToLong(today)
        return if (this.contains("明年") || isBeforeToday) {
            val nextYear = thisYear + 1
            block(nextYear)
        } else dateThisYear
    }

    /**
     * 计算目标日期与今天相距几天
     * @param targetDate 未经格式化的目标日期（默认比今天大）
     * @return 返回相距的天数
     */
    fun dateInterval(targetDate: String): Int {
        val dateFormat = getFormatDate(targetDate)
        val todayL = strToLong(today)
        val targetDateL = strToLong(dateFormat)
        val oneDay = 1000 * 60 * 60 * 24
        val intervalDay = (targetDateL - todayL) / oneDay
        return intervalDay.toInt()
    }

    /**
     * 获取 “周几、礼拜几、下星期几” 的具体日期
     * @param whichDay 周几、星期几或礼拜几（数字）
     * @param isNextWeek 是否是下周、下星期或下礼拜
     * @return 返回类 “2021-08-27” 形式的字符串
     */
    private fun getWeekDate(whichDay: Int, isNextWeek: Boolean = false): String {
        var plusDay = 0
        if (!isNextWeek) {
            if (whichDay > todayWhich()) plusDay = whichDay - todayWhich()
            else ToastUtil.showWarningToasty("输入的日期早于当前日期")
        } else plusDay = whichDay + 7 - todayWhich()
        return getRecent(plusDay)
    }

    /**
     * 获取最近几天（今天，明天，后天，大后天）的日期
     * 或者可以理解为在当前日期的基础上再加几天，得到新的日期
     * @return 返回 “2021-08-27” 形式的数据
     */
    fun getRecent(plus: Int = 0): String {
        val calendar = Calendar.getInstance()
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)+1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDays = calendar.getActualMaximum(Calendar.DATE)
        var dayPlus =  day + plus
        if (dayPlus > maxDays) {
            month++
            dayPlus %= maxDays
            if (month > 12) {
                month = 1
                year++
            }
        }
        var monthSpace = ""
        var daySpace = ""
        if (month < 10) monthSpace = "0"
        if (dayPlus < 10) daySpace = "0"
        return "$year-$monthSpace$month-$daySpace$dayPlus"
    }

    /**
     * 获取当天是星期几
     */
    private fun todayWhich(): Int {
        val calendar = Calendar.getInstance()
        var whichDay = calendar.get(Calendar.DAY_OF_WEEK)
        if (calendar.firstDayOfWeek == Calendar.SUNDAY) {
            whichDay--
            if (whichDay == 0) whichDay = 7
        }
        return whichDay
    }

    /**
     * 将农历节日转换为公历（阳历）
     * @return 得到类 2021-8-27 字符串
     */
    private fun festivalToSolar(year: Int, lunarMonth: Int, lunarDay: Int): String {
        return LunarUtil().getTranslateSolarString(year, lunarMonth, lunarDay)
    }


    /**
     * 计算清明节的日期（可计算范围: 1700-3100）
     * @return 清明节在公历中的日期
     */
    private fun chingMing(year: Int) = if (year == 2232)  "$year-04-04" else {
        val coefficient = doubleArrayOf(
            5.15, 5.37, 5.59, 4.82, 5.02, 5.26, 5.48, 4.70, 4.92, 5.135, 5.36, 4.60, 4.81, 5.04, 5.26
        )
        val mod = year % 100
        val day = (mod * 0.2422 + coefficient[year / 100 - 17] - mod / 4).toInt()
        "$year-04-0$day"
    }

    /**
     * 获取母亲节或父亲节的日期
     * @param whichSunday 当月的第几个星期日
     * @param month 是第几个月就输入第几个月，不用减一
     * @return 返回 “2021-08-27” 型数据
     */
    @SuppressLint("SimpleDateFormat")
    private fun getMdOrFdDate(year: Int, month: Int, whichSunday: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        // month 是从 0 开始的，故调用方输入的月份要减一
        calendar.set(Calendar.MONTH, month-1)
        val maxDays = calendar.getActualMaximum(Calendar.DATE)
        var sundayNum = 0
        for (i in 1..maxDays) {
            calendar.set(Calendar.DATE, i)
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                sundayNum++
                if (sundayNum == whichSunday) break
            }
        }
        return SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
    }

}