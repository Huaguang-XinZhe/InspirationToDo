package com.liuzhihui.cao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.liuzhihui.cao.databinding.FragmentLeftBinding
import com.liuzhihui.cao.databinding.LeftItemBinding
import com.liuzhihui.cao.model.LeftItem
import com.liuzhihui.cao.utils.LogUtil

class LeftFragment : Fragment() {

    private var _binding: FragmentLeftBinding? = null
    private val binding get() = _binding!!

    val leftList = ArrayList<LeftItem>()

    val leftRV by lazy { binding.leftRecyclerView }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 配置 RecyclerView 的 Adapter
        leftRV.linear().setup {
            addType<LeftItem>(R.layout.left_item)

            onBind {
                val itemBinding = LeftItemBinding.bind(itemView)
                val leftItem = leftList[modelPosition]
                itemBinding.apply {
                    itemImage.setImageResource(leftItem.imageRes)
                    itemName.text = leftItem.name
                    itemCount.text = leftItem.count.toString()
                }
            }

            onClick(R.id.left_item) {
                val activity = this@LeftFragment.activity as AppCompatActivity
                val leftItem = leftList[modelPosition]
                val title = leftItem.name
                ChecklistActivity.actionStart(activity, title)
            }
        }.models = leftList

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}