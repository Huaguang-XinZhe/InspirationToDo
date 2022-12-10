package com.liuzhihui.cao

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.drake.brv.utils.bindingAdapter
import com.liuzhihui.cao.databinding.ActivityMainBinding
import com.liuzhihui.cao.model.Alarm
import com.liuzhihui.cao.model.Entry
import com.liuzhihui.cao.model.LeftItem
import com.liuzhihui.cao.model.ToDo
import com.liuzhihui.cao.utils.AlarmUtil
import com.liuzhihui.cao.utils.LogUtil
import com.liuzhihui.cao.utils.TimeUtil
import com.liuzhihui.cao.utils.getContentList
import org.litepal.LitePal
import org.litepal.extension.*

class MainActivity : AppCompatActivity() {

    companion object {
        fun getListCount(tableName: String) = LitePal.count(tableName)
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var fragment: ChecklistFragment
    private val leftFragment by lazy { supportFragmentManager.findFragmentById(R.id.left_fragment) as LeftFragment }

    private val manager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*
        遍历积留的时间，统一设置好当天的闹钟，设置好后从数据库清除记录
        如果用户没有杀死应用，或系统没有回收应用，那以下代码只会执行一次
         */
        var valid = true
        if (valid) {
            val alarmList = LitePal.findAll<Alarm>()
            for (alarm in alarmList) {
                if (alarm.date == TimeUtil.today) {
                    LogUtil.d("setAlarm executed")
                    todayHandle(alarm.timePoint, alarm.message)
                    LitePal.delete<Alarm>(alarm.id)
                }
            }
            valid = false
        }

        // 与 ChecklistFragment 交互
        fragment = supportFragmentManager.findFragmentById(R.id.checklist_fragment) as ChecklistFragment
        fragment.reappearList = { recyclerView ->
            val firstToDo: ToDo? = LitePal.findFirst<ToDo>()
            val contentList = fragment.entryList.getContentList()
            if (firstToDo != null && !contentList.contains(firstToDo.content)) {
                val todoList = LitePal.order("id desc").find<ToDo>()
                for (todo in todoList) {
                    fragment.entryList.add(0, Entry(todo.content))
                    recyclerView.bindingAdapter.notifyItemInserted(0)
                }
                recyclerView.layoutManager?.scrollToPosition(0)
            }
        }
        fragment.saveToDB = { entryList ->
            // 这么做不会保存拖拽后的顺序
            val contentList = entryList.getContentList()
            val size = if (contentList.contains("")) {
                contentList.size - 1
            } else contentList.size
            if (size != LitePal.count<ToDo>()) {
                LitePal.deleteAll<ToDo>()
                for (content in contentList) {
                    if (content != "") ToDo(content).save()
                }
            }
        }

    }

    private fun todayHandle(timePoint: String, message: String) {
        val timePointFormat = TimeUtil.format(timePoint)
        val list = timePointFormat.split(":")
        val hour = list[0].toInt()
        val minutes = list[1].toInt()
        if (SourceHandle.isBehindCurrent(timePoint)) {
            // 输入的时点正确
            AlarmUtil.setSystemAlarm(hour, minutes, message)
        }
    }

    override fun onRestart() {
        super.onRestart()
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    // Toolbar 上图标的点击事件——打开抽屉
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val drawerLayout = binding.drawerLayout
        when (item.itemId) {
            R.id.inspiration -> {
                val intent = Intent(this, InspirationActivity::class.java)
                startActivity(intent)
            }
            android.R.id.home -> {
                manager.hideSoftInputFromWindow(fragment.icEdit.windowToken, 0)
                drawerLayout.openDrawer(GravityCompat.START)
                leftFragment.apply {
                    leftList.clear()
                    val remindItem = LeftItem(R.drawable.left_remind, "提醒", getListCount("remind"))
                    leftList.add(remindItem)
                    val confuseItem = LeftItem(R.drawable.left_confuse, "疑惑", getListCount("confuse"))
                    leftList.add(confuseItem)
                    leftRV.bindingAdapter.notifyDataSetChanged()
                }
            }
        }
        return true
    }

}