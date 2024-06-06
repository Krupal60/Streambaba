package com.stream.baba.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.stream.baba.R

object Ui_helper {
    fun Activity?.navigate(@IdRes navigation: Int, arguments: Bundle? = null) {
        try {
            if (this is FragmentActivity) {
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment?)?.navController?.navigate(
                    navigation, arguments
                )
            }
        } catch (t: Throwable) {
            logError(t)
        }
    }
    fun logError(throwable: Throwable) {
        Log.d("ApiError", "-------------------------------------------------------------------")
        Log.d("ApiError", "safeApiCall: " + throwable.localizedMessage)
        Log.d("ApiError", "safeApiCall: " + throwable.message)
        throwable.printStackTrace()
        Log.d("ApiError", "-------------------------------------------------------------------")
    }

    fun Context?.fixPaddingStatusbar(v: View?) {
        if (v == null || this == null) return
        v.setPadding(
            v.paddingLeft,
            v.paddingTop + getStatusBarHeight(),
            v.paddingRight,
            v.paddingBottom
        )
    }

    fun Fragment.hideKeyboard() {
        activity?.window?.decorView?.clearFocus()
        view?.let {
            hideKeyboard(it)
        }
    }


    fun Dialog?.dismissSafe(activity: Activity?) {
        if (this?.isShowing == true && activity?.isFinishing == false) {
            this.dismiss()
        }
    }
    fun hideKeyboard(view: View?) {
        if (view == null) return

        val inputMethodManager =
            view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Context.getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

}