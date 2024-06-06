package com.stream.baba.utils

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.stream.baba.R
import com.stream.baba.data.Data

object AppUtils {

    fun BottomSheetDialog?.ownHide() {
        this?.hide()
    }

    fun BottomSheetDialog?.ownShow() {
        // the reason for this is because show has a shitty animation we don't want
        this?.window?.setWindowAnimations(-1)
        this?.show()
        Handler(Looper.getMainLooper()).postDelayed({
            this?.window?.setWindowAnimations(R.style.CustomBottomSheetDialogAnimation)
        }, 200)
    }
    /**
     * Observer that is called only once when LiveData value changes.
     * After the first call, the observer will be automatically removed.
     */
    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

}
interface OnItemClickListener {
    fun onPreviewInfoClicked(data: Data)
}