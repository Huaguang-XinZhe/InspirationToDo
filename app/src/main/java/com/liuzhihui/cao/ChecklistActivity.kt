package com.liuzhihui.cao

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.drake.brv.utils.bindingAdapter
import com.liuzhihui.cao.databinding.ActivityChecklistBinding
import com.liuzhihui.cao.databinding.FragmentChecklistBinding
import com.liuzhihui.cao.model.Entry
import com.liuzhihui.cao.model.Remind
import com.liuzhihui.cao.model.Confuse
import com.liuzhihui.cao.model.ToDo
import com.liuzhihui.cao.utils.LogUtil
import com.liuzhihui.cao.utils.getContentList
import org.litepal.LitePal
import org.litepal.LitePalApplication
import org.litepal.extension.count
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import org.litepal.extension.findFirst
import org.litepal.tablemanager.Connector

class ChecklistActivity : AppCompatActivity() {

    companion object {
        fun actionStart(context: Context, title: String) {
            val intent = Intent(context, ChecklistActivity::class.java)
            intent.putExtra("title", title)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityChecklistBinding

    private lateinit var fragment: ChecklistFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChecklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titleS = intent.getStringExtra("title")
        // 与 ChecklistFragment 交互
        fragment = supportFragmentManager.findFragmentById(R.id.checklist_fragment) as ChecklistFragment
        fragment.apply {
            title = titleS ?: ""
            when (titleS) {
                "提醒" -> {
                    reappearList = { recyclerView ->
                        val firstRemind: Remind? = LitePal.findFirst<Remind>()
                        val contentList = fragment.entryList.getContentList()
                        // 是否需要重现
                        if (firstRemind != null && !contentList.contains(firstRemind.content)) {
                            val remindList = LitePal.order("timeL desc").find<Remind>()
                            for (Remind in remindList) {
                                fragment.entryList.add(0, Entry(Remind.content))
                                recyclerView.bindingAdapter.notifyItemInserted(0)
                            }
                            recyclerView.layoutManager?.scrollToPosition(0)
                        }
                    }
                    saveToDB = { entryList ->
                        // 这么做不会保存拖拽后的顺序
                        val contentList = entryList.getContentList()
                        LogUtil.d("sizeC=${contentList.size}")
                        val size = if (contentList.contains("")) {
                            contentList.size - 1
                        } else contentList.size
                        val count = LitePal.count<Remind>()
                        LogUtil.d("size=$size")
                        LogUtil.d("count=$count")
                        if (size != LitePal.count<Remind>()) {
                            LitePal.deleteAll<Remind>()
                            for (content in contentList) {
                                // TODO: 2021/9/6 获取 SourceHandle 的实例，看看效果？
                                LogUtil.d("content=$content")
                                val timeL = SourceHandle(content).timeL
                                LogUtil.d("timeL=$timeL")
                                if (content != "") Remind(content, timeL).save()
                            }
                        }
                    }
                }
                "疑惑" -> {
                    reappearList = { recyclerView ->
                        val firstConfuse: Confuse? = LitePal.findFirst<Confuse>()
                        val contentList = fragment.entryList.getContentList()
                        if (firstConfuse != null && !contentList.contains(firstConfuse.content)) {
                            val confuseList = LitePal.order("id desc").find<Confuse>()
                            for (Confuse in confuseList) {
                                fragment.entryList.add(0, Entry(Confuse.content))
                                recyclerView.bindingAdapter.notifyItemInserted(0)
                            }
                            recyclerView.layoutManager?.scrollToPosition(0)
                        }
                    }
                    saveToDB = { entryList ->
                        // 这么做不会保存拖拽后的顺序
                        val contentList = entryList.getContentList()
                        val size = if (contentList.contains("")) {
                            contentList.size - 1
                        } else contentList.size
                        if (size != LitePal.count<Confuse>()) {
                            LitePal.deleteAll<Confuse>()
                            for (content in contentList) {
                                if (content != "") Confuse(content).save()
                            }
                        }
                    }
                }
                // TODO: 2021/9/4 else
            }
        }
    }

    // Toolbar 上图标的点击事件——返回
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.inspiration -> {
                val intent = Intent(this, InspirationActivity::class.java)
                startActivity(intent)
            }
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
}