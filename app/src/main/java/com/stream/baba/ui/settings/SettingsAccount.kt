package com.stream.baba.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.stream.baba.R
import com.stream.baba.ui.SettingFragment.Companion.getPref
import com.stream.baba.utils.Ui_helper.hideKeyboard

class SettingsAccount : PreferenceFragmentCompat() {



    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        hideKeyboard()
        setPreferencesFromResource(R.xml.settings_account, rootKey)

        val syncApis =
            listOf(
                R.string.mal_key to "MAl" ,
                R.string.anilist_key to "AniList",
                R.string.opensubtitles_key to "OpenSubtitles",
            )

        for ((key, api) in syncApis) {
            getPref(key)?.apply {
                title =
                    getString(R.string.login_format).format(api, getString(R.string.account))
               setOnPreferenceClickListener {
//                    val info = api.loginInfo()
//                    if (info != null) {
//                        showLoginInfo(activity, api, info)
//                    } else {
//                        addAccount(activity, api)
//                    }
                  return@setOnPreferenceClickListener true
                }
            }
        }
    }
}
