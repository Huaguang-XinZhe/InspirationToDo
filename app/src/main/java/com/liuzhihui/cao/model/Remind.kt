package com.liuzhihui.cao.model

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport

class Remind(val content: String,
             @Column(defaultValue = "0") val timeL: Long) : LitePalSupport() {
    val id: Long = 0
}