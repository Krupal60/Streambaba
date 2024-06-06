package com.stream.baba.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.stream.baba.CommonActivity
import com.stream.baba.R
import com.stream.baba.ui.SettingFragment.Companion.getPref
import com.stream.baba.utils.SingleSelectionHelper.showDialog
import com.stream.baba.utils.SubtitleHelper
import com.stream.baba.utils.Ui_helper.hideKeyboard
import com.stream.baba.utils.Ui_helper.logError

fun getCurrentLocale(context: Context): String {
    val res = context.resources
    // Change locale settings in the app.
    // val dm = res.displayMetrics
    val conf = res.configuration
    return conf?.locale?.toString() ?: "en"
}

// idk, if you find a way of automating this it would be great
// https://www.iemoji.com/view/emoji/1794/flags/antarctica
// Emoji Character Encoding Data --> C/C++/Java Src
// https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes leave blank for auto
val appLanguages = arrayListOf(
    /* begin language list */
    Triple("", "العربية", "ar"),
    Triple("", "ars", "ars"),
    Triple("", "български", "bg"),
    Triple("", "বাংলা", "bn"),
    Triple("\uD83C\uDDE7\uD83C\uDDF7", "português brasileiro", "bp"),
    Triple("", "čeština", "cs"),
    Triple("", "Deutsch", "de"),
    Triple("", "Ελληνικά", "el"),
    Triple("", "English", "en"),
    Triple("", "Esperanto", "eo"),
    Triple("", "español", "es"),
    Triple("", "فارسی", "fa"),
    Triple("", "français", "fr"),
    Triple("", "हिन्दी", "hi"),
    Triple("", "hrvatski", "hr"),
    Triple("", "magyar", "hu"),
    Triple("\uD83C\uDDEE\uD83C\uDDE9", "Bahasa Indonesia", "in"),
    Triple("", "italiano", "it"),
    Triple("\uD83C\uDDEE\uD83C\uDDF1", "עברית", "iw"),
    Triple("", "日本語 (にほんご)", "ja"),
    Triple("", "ಕನ್ನಡ", "kn"),
    Triple("", "한국어", "ko"),
    Triple("", "latviešu valoda", "lv"),
    Triple("", "македонски", "mk"),
    Triple("", "മലയാളം", "ml"),
    Triple("", "bahasa Melayu", "ms"),
    Triple("", "Nederlands", "nl"),
    Triple("", "norsk nynorsk", "nn"),
    Triple("", "norsk bokmål", "no"),
    Triple("", "ଓଡ଼ିଆ", "or"),
    Triple("", "polski", "pl"),
    Triple("\uD83C\uDDF5\uD83C\uDDF9", "português", "pt"),
    Triple("\uD83E\uDD8D", "mmmm... monke", "qt"),
    Triple("", "română", "ro"),
    Triple("", "русский", "ru"),
    Triple("", "slovenčina", "sk"),
    Triple("", "Soomaaliga", "so"),
    Triple("", "svenska", "sv"),
    Triple("", "தமிழ்", "ta"),
    Triple("", "Tagalog", "tl"),
    Triple("", "Türkçe", "tr"),
    Triple("", "українська", "uk"),
    Triple("", "اردو", "ur"),
    Triple("", "Tiếng Việt", "vi"),
    Triple("", "中文", "zh"),
    Triple("\uD83C\uDDF9\uD83C\uDDFC", "正體中文(臺灣)", "zh-rTW"),
    /* end language list */
).sortedBy { it.second.lowercase() } //ye, we go alphabetical, so ppl don't put their lang on top

class SettingsGeneral : PreferenceFragmentCompat() {



    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        hideKeyboard()
        setPreferencesFromResource(R.xml.settins_general, rootKey)
        val settingsManager = PreferenceManager.getDefaultSharedPreferences(requireContext())


        getPref(R.string.locale_key)?.setOnPreferenceClickListener { pref ->
            val tempLangs = appLanguages.toMutableList()
            val current = getCurrentLocale(pref.context)
            val languageCodes = tempLangs.map { (_, _, iso) -> iso }
            val languageNames = tempLangs.map { (emoji, name, iso) ->
                val flag = emoji.ifBlank { SubtitleHelper.getFlagFromIso(iso) ?: "ERROR" }
                "$flag $name"
            }
            val index = languageCodes.indexOf(current)

            activity?.showDialog(
                languageNames, index, getString(R.string.app_language), true, { }
            ) { languageIndex ->
                try {
                    val code = languageCodes[languageIndex]
                    CommonActivity.setLocale(activity, code)
                    settingsManager.edit().putString(getString(R.string.locale_key), code).apply()
                    activity?.recreate()
                } catch (e: Exception) {
                    logError(e)
                }
            }
            return@setOnPreferenceClickListener true
        }


        getPref(R.string.legal_notice_key)?.setOnPreferenceClickListener {
            val builder: AlertDialog.Builder =
                AlertDialog.Builder(it.context, R.style.AlertDialogCustom)
            builder.setTitle(R.string.legal_notice)
            builder.setMessage(R.string.legal_notice_text)
            builder.show()
            return@setOnPreferenceClickListener true
        }


    }
}
