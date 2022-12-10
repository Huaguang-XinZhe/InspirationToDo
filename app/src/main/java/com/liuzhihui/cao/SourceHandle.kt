package com.liuzhihui.cao

import android.annotation.SuppressLint
import android.widget.Toast
import com.liuzhihui.cao.model.Alarm
import com.liuzhihui.cao.model.Confuse
import com.liuzhihui.cao.model.Inspiration
import com.liuzhihui.cao.model.Remind
import com.liuzhihui.cao.utils.*

@SuppressLint("SimpleDateFormat")
open class SourceHandle(private val source: String) {

    companion object {

        // 匹配日期的正则表达式
        const val datePattern = "(\\d{2,4}年)?(\\d{1,2}月)?\\d{1,2}[号日]|(\\d{2,4}[-/])?\\d{1,2}[-/]\\d{1,2}|" +
                "(明年)?(国庆节?|元旦|儿童节|六一|61|劳动节|五一|51|教师节|情人节|高考|圣诞节?|中秋节?|端午节?|清明节?|元宵节?|重阳节|" +
                "[7七]夕节?|大年三十|除夕|过年|春节|[母父]亲节)|今[天儿]?|明[天儿]?|大?后天|下?(周|礼拜|星期)[一二三四五六七日天\\d]"
        // 匹配时段的正则表达式
        const val periodPattern = "凌晨|[上中下]午|傍晚|[早晚]上?"
        // 匹配时点的正则表达式
        const val timePointPattern = "\\d{1,2}[:：]\\d{1,2}|(十[一二]|[一二三四五六七八九十]|\\d{1,2})点半?"
        val remindRegex = Regex("$datePattern|$periodPattern|$timePointPattern")

        /**
         * 判断传入的时间是否在当前时间的后面
         */
        fun isBehindCurrent(timePoint: String): Boolean {
            val timePointFormat = TimeUtil.format(timePoint)
            val currentHour = TimeUtil.getCurrentTime()[3]
            val currentMinutes = TimeUtil.getCurrentTime()[4]
            val currentTime = strToLong("$currentHour:$currentMinutes", "HH:mm")
            val compareTime = strToLong(timePointFormat, "HH:mm")
            return compareTime > currentTime
        }

    }

    // 删除空格后的源数据
    private val sourceDelSpace = source.replace(" ", "")
    private val message = source.replace(remindRegex, "")
    // 未经格式化
    private var timePoint = sourceDelSpace.matchStr(timePointPattern)
    private val period = sourceDelSpace.matchStr(periodPattern)
    private val date = sourceDelSpace.matchStr(datePattern)

    private val dateFormat by lazy {
        if (date != "") {
            TimeUtil.getFormatDate(date)
        } else TimeUtil.today
    }

    val timeL by lazy {
        timePoint = TimeUtil.format(timePoint)
        val time = "$dateFormat $timePoint"
        strToLong(time, "yyyy-MM-dd HH:mm")
    }

    fun handle(source: String) {
        LogUtil.d("timePoint = $timePoint, period = $period, date = $date")
        sourceDelSpace.apply { when {
            // 疑惑
            contains(Regex("[？?]\$")) -> {
                ToastUtil.showInfoToasty("归入疑惑清单", Toast.LENGTH_SHORT)
                Confuse(source).save()
            }
            // 灵感
            contains(Regex("[。.！!]\$")) -> {
                ToastUtil.showInfoToasty("归入灵感 MarkDown！", Toast.LENGTH_SHORT)
                Inspiration(source).save()
            }
            // 待办——"早于"、"晚于"（在灵感后，提醒前）
            contains(Regex("早于|晚于")) -> todo()
            // 待办——"\"（转义）
            contains("\\") -> {
                todo()
            }
            // 待办——提醒
            contains(remindRegex) -> {
                if (timePoint != "") {
                    if (period == "") {
                        judgeTodayHandle(timePoint)
                    } else {
                        if (TimeUtil.isAfternoon(period)) {
                            timePoint = TimeUtil.transform(timePoint)
                        }
                        judgeTodayHandle(timePoint)
                    }
                } else {
                    // 时段默认时点
                    when (period) {
                        "中午" -> timePoint = "12:00"
                        "傍晚" -> timePoint = "18:00"
                        "凌晨" -> timePoint = "04:00"
                        "早上", "早" -> timePoint = "06:30"
                        "上午", "" -> timePoint = "08:00"
                        "下午" -> timePoint = "15:00"
                        "晚上", "晚" -> timePoint = "20:00"
                        else -> ToastUtil.showWarningToasty("输入的时段 $period 不符合预定格式")
                    }
                    judgeTodayHandle(timePoint)
                }
            }
            // 待办——其他
            else -> todo()
        } }
    }

    /**
     * 判断是否是今天以及其各自的处理逻辑
     * @param timePoint 未经格式化的时点字符串
     */
    private fun judgeTodayHandle(timePoint: String) {
        if (TimeUtil.isToday(date)) {
            todayHandle(timePoint, message)
        } else {
            Remind(source, timeL).save()
            // timePoint 未经格式化
            Alarm(dateFormat, timePoint, message).save()
            ToastUtil.showSuccessToasty("归入提醒清单，将安排在 ${outputIntervalTip(date)}后提醒")
        }
    }

    /**
     * 根据目标日期输出间隔提示
     * @param targetDate 未经格式化的目标日期
     */
    private fun outputIntervalTip(targetDate: String): String {
        val intervalDay = TimeUtil.dateInterval(targetDate)
        return when {
            intervalDay < 30 -> "$intervalDay 天"
            intervalDay in 30..364 -> {
                val month = intervalDay / 30
                "$month 个月……"
            }
            else -> {
                val year = intervalDay / 365
                "$year 年……"
            }
        }
    }

    /**
     * 时点在今天的处理逻辑
     * @param timePoint 未经格式化的时点字符串
     * @param message 给闹钟设置的提示信息
     */
    private fun todayHandle(timePoint: String, message: String) {
        val timePointFormat = TimeUtil.format(timePoint)
        val list = timePointFormat.split(":")
        val hour = list[0].toInt()
        val minutes = list[1].toInt()
        if (isBehindCurrent(timePoint)) {
            // 输入的时点正确
            Remind(source, timeL).save()
            AlarmUtil.setSystemAlarm(hour, minutes, message)

        } else {
            ToastUtil.showWarningToasty("您输入的时间早于当前时间")
        }
    }

    /**
     * 类属为待办后进一步的处理逻辑
     */
    open fun todo() {}

}