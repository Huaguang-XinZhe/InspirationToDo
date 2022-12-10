package com.liuzhihui.cao

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.listener.DefaultItemTouchCallback
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.gyf.immersionbar.components.SimpleImmersionFragment
import com.gyf.immersionbar.ktx.immersionBar
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.applyLinks
import com.liuzhihui.cao.databinding.FragmentChecklistBinding
import com.liuzhihui.cao.databinding.ItemEntryBinding
import com.liuzhihui.cao.model.Entry
import com.liuzhihui.cao.utils.*

class ChecklistFragment : SimpleImmersionFragment() {

    private var _binding: FragmentChecklistBinding? = null
    private val binding get() = _binding!!

    private val manager by lazy { activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private val clipboard by lazy { activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val  icEdit by lazy { binding.icEdit }

    private val soundPool by lazy { createSoundPool() }
    private val soundId = soundPool.load()

    val entryList = ArrayList<Entry>()

    private val isFabExist by lazy { activity is MainActivity }

    // 以下 3 个变量有待宿主 Activity 实现
    lateinit var reappearList: (recyclerView: RecyclerView) -> Unit
    lateinit var saveToDB: (entryList: ArrayList<Entry>) -> Unit
    var title = "待办"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?    ): View {
        _binding = FragmentChecklistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 为 Fragment 设置 Toolbar
        val toolbar = binding.toolbar
        val compatActivity = activity as AppCompatActivity
        compatActivity.setSupportActionBar(toolbar)
        compatActivity.supportActionBar?.let {
            if (!isFabExist) {
                it.setDisplayHomeAsUpEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.back)
            }
        }
        toolbar.title = title

        // RecyclerView 的 Adapter
        val recyclerView = binding.recyclerView
        recyclerView.linear().setup {
            addType<Entry>(R.layout.item_entry)

            onBind {
                val itemBinding = ItemEntryBinding.bind(itemView)
                val textView = itemBinding.entryContent
                val entry = entryList[modelPosition]
                val content = entry.content
                textView.text = content

                // 特殊显示与动作
                when {
                    content.contains("\\") -> {
                        val startIndex = content.indexOf("\\")
                        val endIndex = content.indexOf(" ")
                        if (endIndex != -1) textView.localDiscoloration(content, startIndex, endIndex)
                    }
                    content.contains(Regex("[？?]\$")) -> {
                        val sourceLink = Link(content).setOnClickListener {
                            if (isNetWorkConnected()) {
                                val baiduUri = Uri.parse("https://www.baidu.com/s?wd=$content")
                                val intent = Intent(Intent.ACTION_VIEW, baiduUri)
                                startActivity(intent)
                            } else {
                                ToastUtil.showErrorToasty("当前网络未连接！")
                            }
                        }
                        textView.applyLinks(sourceLink)
                    }
                    content.contains(SourceHandle.remindRegex) -> {
                        val contentDelSpace = content.replace(" ", "")
                        val date = contentDelSpace.matchStr(SourceHandle.datePattern)
                        val period = contentDelSpace.matchStr(SourceHandle.periodPattern)
                        val timePoint = contentDelSpace.matchStr(SourceHandle.timePointPattern)
                        val timeContent = "$date$period$timePoint"
                        val timeLink = Link(timeContent).setTypeface(Typeface.DEFAULT_BOLD)
                            .setOnClickListener {
                                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                                startActivity(intent)
                            }
                        textView.applyLinks(timeLink)

                        if (TimeUtil.isToday(date)) {
                            itemBinding.slantedView.visibility = View.VISIBLE
                        }
                    }
                }

                // 状态保持
                if (!entry.isVisible) {
                    itemView.visibility = View.INVISIBLE
                } else {
                    itemView.visibility = View.VISIBLE
                }
                if (!entry.isChecked) {
                    textView.apply {
                        setTextColor(color(R.color.black2))
                        paintFlags = Paint.STRIKE_THRU_TEXT_FLAG.inv() and paintFlags
                        paint.isAntiAlias = true
                        itemBinding.radioButton.isChecked = false
                    }
                }
            }

            onClick(R.id.radioButton) {
                soundPool.play(soundId, 0.2f, 0.2f, 1, 0, 1f)
                val itemBinding = ItemEntryBinding.bind(itemView)
                val textView = itemBinding.entryContent
                textView.setTextColor(color(R.color.gray))
                textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                entryList.removeAt(modelPosition)
                adapter.notifyItemRemoved(adapterPosition)
            }

            onLongClick(R.id.entryContent) {
                val itemBinding = ItemEntryBinding.bind(itemView)
                clipboard.text = itemBinding.entryContent.text
                ToastUtil.showToast("文本复制成功！")
            }

        }.models = entryList

        // 从数据库取出 content，并重现在 RecyclerView 中
        reappearList(recyclerView)

        // 允许 fab 存在才会执行以下代码
        if (isFabExist) {
            // 发送后自动类属，待办添加到当前界面
            binding.sortAndSend.setOnClickListener {
                val source = icEdit.text.toString()
                if (source != "") {
                    object : SourceHandle(source) {
                        override fun todo() {
                            entryList.add(0, Entry(source))
                            recyclerView.bindingAdapter.notifyItemInserted(0)
                            recyclerView.layoutManager?.scrollToPosition(0)
                        }
                    }.handle(source)
                    icEdit.setText("")
                }
            }
            // 触碰软键盘外的区域隐藏软键盘
            val fab = binding.fab
            recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    manager.hideSoftInputFromWindow(rv.windowToken, 0)
                    return super.onInterceptTouchEvent(rv, e)
                }
            })
            // RecyclerView 向下滑动隐藏输入框；
            // 在 RecyclerView 底部加一个不可见占位 item（Activity 创建后只执行一次）
            var valid = true
            val scrollListener = RecycleScrollListener()
            scrollListener.mOnScrollDirectionListener =
                object : RecycleScrollListener.OnScrollDirectionListener {
                    // 当 item 比较少（不满一屏）的时候，不会执行
                    override fun scrollUp(dy: Int) {}

                    @SuppressLint("NotifyDataSetChanged")
                    override fun scrollDown(dy: Int) {
                        if (valid) {
                            entryList.add(entryList.size, Entry("", false))
                            recyclerView.bindingAdapter.notifyDataSetChanged()
                            valid = false
                        }
                    }
                }
            recyclerView.addOnScrollListener(scrollListener)
            // 点击悬浮按钮开始输入
            fab.setOnClickListener {
                fab.visibility = View.GONE
                binding.bottomEdit.visibility = View.VISIBLE
                icEdit.requestFocus()
                manager.showSoftInput(icEdit, InputMethodManager.SHOW_FORCED)
            }
        } else {
            binding.fab.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        manager.hideSoftInputFromWindow(icEdit.windowToken, 0)
        saveToDB(entryList)
        // TODO: 2021/9/7 在返回时 item 的数量不会更新 
    }

    override fun onDestroyView() {
        super.onDestroyView()
        soundPool.releaseAndUnload(soundId)
        _binding = null
    }

    override fun initImmersionBar() {
        immersionBar {}
    }

}