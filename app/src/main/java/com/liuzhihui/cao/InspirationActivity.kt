package com.liuzhihui.cao

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.gyf.immersionbar.ktx.immersionBar
import com.liuzhihui.cao.databinding.ActivityInspirationBinding
import com.liuzhihui.cao.databinding.ItemEditBinding
import com.liuzhihui.cao.model.Inspiration
import com.liuzhihui.cao.utils.LogUtil
import com.liuzhihui.cao.utils.insert
import org.litepal.LitePal
import org.litepal.extension.count
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import org.litepal.extension.findFirst


class InspirationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInspirationBinding

    private val manager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private var itemBinding: ItemEditBinding? = null

    private val editList = ArrayList<String>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInspirationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        immersionBar {  }

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val recyclerView = binding.recyclerView

        recyclerView.linear().setup {
            addType<String>(R.layout.item_edit)

            onBind {
                itemBinding = ItemEditBinding.bind(itemView)
                val edit = editList[modelPosition]
                if (!edit.contains("## ")) {
                    itemBinding?.edit?.setText("## \n$edit")
                }
                itemBinding?.edit?.setSelection(3)
            }

        }.models = editList

        // 从数据库复现
        val firstInspiration: Inspiration? = LitePal.findFirst<Inspiration>()
        if (firstInspiration != null && !editList.contains(firstInspiration.content)) {
            val inspirationList = LitePal.order("id desc").find<Inspiration>()
            for (inspiration in inspirationList) {
                editList.add(0, inspiration.content)
                recyclerView.bindingAdapter.notifyItemInserted(0)
            }
            recyclerView.layoutManager?.scrollToPosition(0)
        }

//        binding.h3.setOnClickListener { .insert("### ") }
//        binding.asterisk.setOnClickListener { editText.insert("*") }
//        binding.quote.setOnClickListener { editText.insert("> ") }
//        binding.block.setOnClickListener { editText.insert("`") }

    }

    override fun onPause() {
        super.onPause()
        if (itemBinding != null) {
            manager.hideSoftInputFromWindow(itemBinding!!.edit.windowToken, 0)
        }
        if (editList.size != LitePal.count<Inspiration>()) {
            LitePal.deleteAll<Inspiration>()
            for (edit in editList) {
                Inspiration(edit).save()
            }
        }
    }

    // Toolbar 上图标的点击事件
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.inspiration -> {}
            android.R.id.home -> finish()
        }
        return true
    }
}