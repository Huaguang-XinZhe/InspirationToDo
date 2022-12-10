package com.liuzhihui.cao.utils

import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import org.litepal.LitePalApplication

object AlarmUtil {

    /**
     * 设置系统闹钟（默认 8：00）
     * 通过该方法设置的闹钟在停止后会自动消失（多个重复闹钟只会消失一个，其他的将一响一个），手动取消后不会；
     * 使用此方法设置系统闹钟不需要任何权限；
     * 在设置系统闹钟时，若系统闹钟程序已打开，那即使是设置了 AlarmClock.EXTRA_SKIP_UI 为 true 也没用，
     * 照样会打开闹钟界面，有时候设置了闹钟还不会及时刷新，要重启才能看到；
     * 若系统闹钟程序没有打开，那 AlarmClock.EXTRA_SKIP_UI 才会生效；
     * 当同时设置多个闹钟的时候只有第一个闹钟会生效；
     * 同样时间和信息的闹钟可以重复设置，通过 dismissAlarm() 方法可以一起取消。
     * @param message 闹钟的提示信息
     */
    fun setSystemAlarm(hour: Int = 8, minutes: Int = 0, message: String) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
        intent.apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_VIBRATE, true) // 震动
            putExtra(AlarmClock.EXTRA_SKIP_UI, true) // 不显示闹钟界面
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        LitePalApplication.getContext().startActivity(intent)
    }

    /**
     * 根据闹钟的提示信息取消设置的闹钟
     * 该方法始终无效，且始终会跳转到闹钟界面；
     * 当取消不存在的闹钟时不会引发异常；
     */
    fun dismissAlarm(message: String = "") {
        if (Build.VERSION.SDK_INT < 23) {
            ToastUtil.showWarningToasty("您的手机版本较低，需手动取消闹钟")
            val intentS = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            LitePalApplication.getContext().startActivity(intentS)
        } else {
            val intentD = Intent(AlarmClock.ACTION_DISMISS_ALARM)
            intentD.putExtra(AlarmClock.ALARM_SEARCH_MODE_LABEL, message)
            LitePalApplication.getContext().startActivity(intentD)
        }
    }

}