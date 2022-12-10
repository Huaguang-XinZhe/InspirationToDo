package com.liuzhihui.cao.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.liuzhihui.cao.model.Entry
import org.litepal.LitePalApplication
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * 获取 Entry 对象中的 content 集合
 */
fun ArrayList<Entry>.getContentList(): List<String> {
    val contentList = ArrayList<String>()
    for (entry in this) {
        contentList.add(entry.content)
    }
    return contentList
}

/**
 * 向 EditText 光标处插入指定文本
 */
fun EditText.insert(text: String) {
    val index = selectionStart
    val edit = editableText
    if (index < 0 || index >= edit.length) {
        edit.append(text)
    } else {
        edit.insert(index, text)
    }
}

/**
 * 使 TextView 中的局部文本变成灰色
 */
fun TextView.localDiscoloration(str: String, startIndex: Int, endIndex: Int) {
    val spannableStr = SpannableString(str)
    val fgColor = ForegroundColorSpan(Color.parseColor("#F388898B"))
    spannableStr.setSpan(fgColor, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.text = spannableStr
}

/**
 * 用正则表达式匹配源数据，得到匹配后的字符串
 */
fun String.matchStr(regexStr: String): String {
    val matchedList = matchToList(regexStr)
    var matchedStr = ""
    if (matchedList.isNotEmpty()) {
         matchedStr = matchedList[0]
    }
    return matchedStr
}

/**
 * 用正则表达式匹配源数据，得到匹配后字符串的列表（有可能返回空集合）
 */
fun String.matchToList(regexStr: String): List<String> {
    val list = ArrayList<String>()
    val matcher = Pattern.compile(regexStr).matcher(this)
    while (matcher.find()) {
        list.add(matcher.group())
    }
    return list
}

/**
 * 获取 colors.xml 中的颜色
 */
fun color(colorId: Int) = LitePalApplication.getContext().resources.getColor(colorId)


/**
 * 将数据集中的元素同步置顶
 */
fun ArrayList<Entry>.eleMovedToTop(fromPosition: Int) {
    val movedEle = get(fromPosition)
    removeAt(fromPosition)
    add(0, movedEle)
}

/**
 * 把长整型时间转换为类 "2021-08-30 08:21" 形式的字符串
 */
@SuppressLint("SimpleDateFormat")
fun longToString(time: Long): String {
    val eightHour = strToLong("08:00", "HH:mm")
    val timePlus8 = time + eightHour
    // 格式：Mon Aug 30 00:15:04 CST 2021
    val date = Date(timePlus8)
    println(date)
    return SimpleDateFormat("yyyy-MM-dd HH:mm").format(date)
}

/**
 * 将时间字符串转换为 Long 型数字
 * @param formatType 要转换的格式："yyyy-MM-dd"、"HH:mm"（这个格式必须和函数前面的时间字符串的格式相同）
 * 注意："HH:mm" 表示的是时点，不能用于分钟小时的 Long 转换
 */
@SuppressLint("SimpleDateFormat")
fun strToLong(str: String, formatType: String = "yyyy-MM-dd"): Long {
    val date = SimpleDateFormat(formatType).parse(str)
    return date!!.time
}

/**
 * 判断网络是否连接
 */
fun isNetWorkConnected(): Boolean {
    val manager = LitePalApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = manager.activeNetworkInfo
    return networkInfo?.isAvailable ?: false
}

