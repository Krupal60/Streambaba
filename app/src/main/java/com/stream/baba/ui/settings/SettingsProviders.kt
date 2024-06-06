package com.stream.baba.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceFragmentCompat
import com.stream.baba.R
import com.stream.baba.ui.SettingFragment.Companion.getPref

class SettingsProviders : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_providers, rootKey)

        getPref(R.string.legal_notice_key)?.setOnPreferenceClickListener {
            val builder: AlertDialog.Builder =
                AlertDialog.Builder(it.context, R.style.AlertDialogCustom)
            builder.setTitle(R.string.detail)
            builder.setMessage(R.string.detail)
            builder.show()
            return@setOnPreferenceClickListener true
        }
    }

}