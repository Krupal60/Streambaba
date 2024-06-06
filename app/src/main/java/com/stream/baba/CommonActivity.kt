package com.stream.baba

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.stream.baba.utils.UiText
import com.stream.baba.utils.Ui_helper.logError
import java.util.Locale



 fun Activity.showSystemUi(){
    WindowCompat.setDecorFitsSystemWindows(window!!,true)
    val windowInsetsController = WindowCompat.getInsetsController(window!!, window!!.decorView)
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
     val lp = window?.attributes
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
         lp?.layoutInDisplayCutoutMode =
             WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
     }
     window?.attributes = lp


}

 fun Activity.hideSystemUi(){
    WindowCompat.setDecorFitsSystemWindows(window!!,false)
    val windowInsetsController = WindowCompat.getInsetsController(window!!, window!!.decorView)
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
     val params =  window?.attributes
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
         params?.layoutInDisplayCutoutMode =
             WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
     }
     window?.attributes = params
}

object CommonActivity {

    var canEnterPipMode: Boolean = false
    var canShowPipMode: Boolean = false

    var keyEventListener: ((Pair<KeyEvent?, Boolean>) -> Boolean)? = null


    var currentToast: Toast? = null

    fun showToast(act: Activity?, text: UiText, duration: Int) {
        if (act == null) return
        text.asStringNull(act)?.let {
            showToast(act, it, duration)
        }
    }

    /** duration is Toast.LENGTH_SHORT if null*/
    @MainThread
    fun showToast(act: Activity?, @StringRes message: Int, duration: Int? = null) {
        if (act == null) return
        showToast(act, act.getString(message), duration)
    }

    const val TAG = "COMPACT"

    /** duration is Toast.LENGTH_SHORT if null*/
    @MainThread
    fun showToast(act: Activity?, message: String?, duration: Int? = null) {
        if (act == null || message == null) {
            Log.w(TAG, "invalid showToast act = $act message = $message")
            return
        }
        Log.i(TAG, "showToast = $message")

        try {
            currentToast?.cancel()
        } catch (e: Exception) {
            logError(e)
        }
        try {
            val inflater =
                act.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val layout: View = inflater.inflate(
                R.layout.toast,
                act.findViewById<View>(R.id.toast_layout_root) as ViewGroup?
            )

            val text = layout.findViewById(R.id.text) as TextView
            text.text = message.trim()

            val toast = Toast(act)
            toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 5)
            toast.duration = duration ?: Toast.LENGTH_SHORT
            toast.view = layout
            //https://github.com/PureWriter/ToastCompat
            toast.show()
            currentToast = toast
        } catch (e: Exception) {
            logError(e)
        }
    }

    /**
     * Not all languages can be fetched from locale with a code.
     * This map allows sidestepping the default Locale(languageCode)
     * when setting the app language.
     **/
    val appLanguageExceptions = hashMapOf(
        "zh-rTW" to Locale.TRADITIONAL_CHINESE
    )

    fun setLocale(context: Context?, languageCode: String?) {
        if (context == null || languageCode == null) return
        val locale = appLanguageExceptions[languageCode] ?: Locale(languageCode)
        val resources: Resources = context.resources
        val config = resources.configuration
        Locale.setDefault(locale)
        config.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            context.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun Context.updateLocale() {
        val settingsManager = PreferenceManager.getDefaultSharedPreferences(this)
        val localeCode = settingsManager.getString(getString(R.string.locale_key), null)
        setLocale(this, localeCode)
    }

    fun init(act: ComponentActivity?) {
        if (act == null) return
        //https://stackoverflow.com/questions/52594181/how-to-know-if-user-has-disabled-picture-in-picture-feature-permission
        //https://developer.android.com/guide/topics/ui/picture-in-picture

        act.updateLocale()


        // Ask for notification permissions on Android 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                act,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val requestPermissionLauncher = act.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                Log.d(TAG, "Notification permission: $isGranted")
            }
            requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }


    private fun getNextFocus(
        act: Activity?,
        view: View?,
        direction: FocusDirection,
        depth: Int = 0
    ): Int? {
        if (view == null || depth >= 10 || act == null) {
            return null
        }

        val nextId = when (direction) {
            FocusDirection.Left -> {
                view.nextFocusLeftId
            }

            FocusDirection.Up -> {
                view.nextFocusUpId
            }

            FocusDirection.Right -> {
                view.nextFocusRightId
            }

            FocusDirection.Down -> {
                view.nextFocusDownId
            }
        }

        return if (nextId != -1) {
            val next = act.findViewById<View?>(nextId)
            //println("NAME: ${next.accessibilityClassName} | ${next?.isShown}" )

            if (next?.isShown == false) {
                getNextFocus(act, next, direction, depth + 1)
            } else {
                if (depth == 0) {
                    null
                } else {
                    nextId
                }
            }
        } else {
            null
        }
    }

    enum class FocusDirection {
        Left,
        Right,
        Up,
        Down,
    }

}
