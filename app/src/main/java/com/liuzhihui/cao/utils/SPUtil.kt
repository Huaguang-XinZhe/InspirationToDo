package com.liuzhihui.cao.utils

import android.content.Context
import androidx.core.content.edit
import org.litepal.LitePalApplication

object SPUtil {

    private val sp = LitePalApplication.getContext().getSharedPreferences("just_one", Context.MODE_PRIVATE)

//    /**
//     * 判断当前时间是不是一天之后
//     */
//    fun isADayAfter(currentTime: String): Boolean {
//        val time = sp.getString("time", "${TimeUtil.today} 00:05")
//        val nextDay = TimeUtil.getRecent(1)
//        val timePoint = time!!.split(" ")[1]
//        val timePlusOneDay = "$nextDay $timePoint"
//        val timePlusOneDayL = strToLong(timePlusOneDay, "yyyy-MM-dd HH:mm")
//        val currentTimeL = strToLong(currentTime, "yyyy-MM-dd HH:mm")
//        return currentTimeL > timePlusOneDayL
//    }
//
//    /**
//     * 将当前时间存储在 SP 中
//     */
//    fun save() {
//        val list = TimeUtil.getCurrentTime()
//        val time = "${TimeUtil.today} ${list[3]}:${list[4]}"
//        sp.edit {
//            putString("time", time)
//        }
//    }

}