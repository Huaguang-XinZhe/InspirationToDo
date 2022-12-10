package com.liuzhihui.cao.utils

import android.widget.Toast
import es.dmoral.toasty.Toasty
import org.litepal.LitePalApplication

object ToastUtil {

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(LitePalApplication.getContext(), message, duration).show()
    }

    fun showErrorToasty(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toasty.error(LitePalApplication.getContext(), message, duration).show()
    }

    fun showWarningToasty(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toasty.warning(LitePalApplication.getContext(), message, duration).show()
    }

    fun showSuccessToasty(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toasty.success(LitePalApplication.getContext(), message, duration).show()
    }

    fun showInfoToasty(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toasty.info(LitePalApplication.getContext(), message, duration).show()
    }

}