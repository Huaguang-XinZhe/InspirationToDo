package com.liuzhihui.cao

import androidx.recyclerview.widget.RecyclerView

class RecycleScrollListener : RecyclerView.OnScrollListener() {

    // 滑动位置参数
    companion object {
        const val SCROLL_TOP = 1
        const val SCROLL_NO_TB = 0
        const val SCROLL_BOTTOM = -1
    }

    // 是否滑动监听
    var mOnScrollStateListener: OnScrollStateListener? = null
    // 滑动方向监听
    var mOnScrollDirectionListener: OnScrollDirectionListener? = null
    // 滑动位置监听
    var mOnScrollPositionListener: OnScrollPositionListener? = null

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        // 是否滑动监听
        if (mOnScrollStateListener != null) {
            if (recyclerView.scrollState != 0) {
                // recyclerView 正在滑动
                mOnScrollStateListener!!.scrollState(true)
            } else {
                mOnScrollStateListener!!.scrollState(false)
            }
        }

        // 滑动位置监听
        if (mOnScrollPositionListener != null) {
            when {
                recyclerView.canScrollVertically(-1) -> {
                    // 滑动到顶部
                    mOnScrollPositionListener!!.scrollPosition(SCROLL_TOP)
                }
                recyclerView.canScrollVertically(1) -> {
                    // 滑动到底部
                    mOnScrollPositionListener!!.scrollPosition(SCROLL_BOTTOM)
                }
                else -> {
                    // 滑动到既不是顶部也不是底部的位置
                    mOnScrollPositionListener!!.scrollPosition(SCROLL_NO_TB)
                }
            }
        }

        // 滑动方向监听
        if (mOnScrollDirectionListener != null) {
            if (dy < 0) {
                // 向上滑动
                mOnScrollDirectionListener!!.scrollUp(dy)
            } else if (dy > 0) {
                // 向下滑动
                mOnScrollDirectionListener!!.scrollDown(dy)
            }
        }

    }

    // 是否在滑动
    interface OnScrollStateListener {
        fun scrollState(isScrolling: Boolean)
    }

    // 滑动位置监听
    interface OnScrollPositionListener {
        // SCROLL_TOP 滑动到顶部
        // SCROLL_NO_TB 滑动到既不是顶部也不是底部的位置
        // SCROLL_BOTTOM 滑动到底部
        fun scrollPosition(position: Int)
    }

    // 滑动方向监听
    interface OnScrollDirectionListener {
        // 向上滑动
        fun scrollUp(dy: Int)
        // 向下滑动
        fun scrollDown(dy: Int)
    }

}