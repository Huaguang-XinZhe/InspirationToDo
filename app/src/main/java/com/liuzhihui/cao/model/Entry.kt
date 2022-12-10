package com.liuzhihui.cao.model

import com.drake.brv.annotaion.ItemOrientation
import com.drake.brv.item.ItemDrag

data class Entry(val content: String, var isVisible: Boolean = true,
     var isChecked: Boolean = false) : ItemDrag {

    override var itemOrientationDrag: Int = 0
        get() = ItemOrientation.ALL

}
