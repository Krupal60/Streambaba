package com.stream.baba.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.stream.baba.R
import com.stream.baba.ui.SettingFragment.Companion.getPref

class SettingsUpdates : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_updates, rootKey)
        getPref(R.string.Notification)?.setOnPreferenceClickListener {
            return@setOnPreferenceClickListener true
        }
    }



}