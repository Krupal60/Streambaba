package com.stream.baba.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.stream.baba.utils.Ui_helper.logError

const val DOWNLOAD_HEADER_CACHE = "download_header_cache"

//const val WATCH_HEADER_CACHE = "watch_header_cache"
const val DOWNLOAD_EPISODE_CACHE = "download_episode_cache"
const val VIDEO_PLAYER_BRIGHTNESS = "video_player_alpha_key"
const val USER_SELECTED_HOMEPAGE_API = "home_api_used"
const val USER_PROVIDER_API = "user_custom_sites"

const val PREFERENCES_NAME = "rebuild_preference"

object DataStore {

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun Context.getSharedPrefs(): SharedPreferences {
        return getPreferences(this)
    }

    fun getFolderName(folder: String, path: String): String {
        return "${folder}/${path}"
    }

    fun <T> Context.setKeyRaw(path: String, value: T, isEditingAppSettings: Boolean = false) {
        try {
            val editor: SharedPreferences.Editor =
                if (isEditingAppSettings) getDefaultSharedPrefs().edit() else getSharedPrefs().edit()
            when (value) {
                is Boolean -> editor.putBoolean(path, value)
                is Int -> editor.putInt(path, value)
                is String -> editor.putString(path, value)
                is Float -> editor.putFloat(path, value)
                is Long -> editor.putLong(path, value)
                (value as? Set<String> != null) -> editor.putStringSet(path, value as Set<String>)
            }
            editor.apply()
        } catch (e: Exception) {
            logError(e)
        }
    }

    fun Context.getDefaultSharedPrefs(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

    fun Context.getKeys(folder: String): List<String> {
        return this.getSharedPrefs().all.keys.filter { it.startsWith(folder) }
    }

    fun Context.removeKey(folder: String, path: String) {
        removeKey(getFolderName(folder, path))
    }

    fun Context.containsKey(folder: String, path: String): Boolean {
        return containsKey(getFolderName(folder, path))
    }

    fun Context.containsKey(path: String): Boolean {
        val prefs = getSharedPrefs()
        return prefs.contains(path)
    }

    fun Context.removeKey(path: String) {
        try {
            val prefs = getSharedPrefs()
            if (prefs.contains(path)) {
                val editor: SharedPreferences.Editor = prefs.edit()
                editor.remove(path)
                editor.apply()
            }
        } catch (e: Exception) {
            logError(e)
        }
    }

    fun Context.removeKeys(folder: String): Int {
        val keys = getKeys(folder)
        keys.forEach { value ->
            removeKey(value)
        }
        return keys.size
    }

}