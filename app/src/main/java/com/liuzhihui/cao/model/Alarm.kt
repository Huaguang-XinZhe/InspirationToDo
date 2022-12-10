package com.liuzhihui.cao.model

import org.litepal.crud.LitePalSupport

class Alarm(val date: String, val timePoint: String,
            val message: String) : LitePalSupport() {
    val id: Long = 0
}