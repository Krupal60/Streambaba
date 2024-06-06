package com.stream.baba.utils

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

class AutoScrollViewPager : ViewPager {
    private var interval = DEFAULT_INTERVAL
    private var isAutoScroll = false
    private var runnable: Runnable? = null
    private var handler: Handler? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun setAutoScrollInterval(interval: Long) {
        this.interval = interval
    }

    fun startAutoScroll() {
        if (adapter != null && adapter!!.count > 1) {
            isAutoScroll = true
            if (handler == null) {
                handler = Handler()
            }
            if (runnable != null) {
                handler?.removeCallbacks(runnable!!)
            }
            runnable = Runnable {
                if (isAutoScroll) {
                    currentItem = (currentItem + 1) % adapter!!.count
                    handler?.postDelayed(runnable!!, interval)
                }
            }
            handler?.postDelayed(runnable!!, interval)
        }
    }

    fun stopAutoScroll() {
        isAutoScroll = false
        if (handler != null) {
            handler?.removeCallbacks(runnable!!)
        }
    }

    fun getCurrentPosition(): Int {
        return currentItem
    }
    companion object {
        private const val DEFAULT_INTERVAL: Long = 2000
    }
}
