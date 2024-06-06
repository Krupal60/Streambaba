package com.stream.baba.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.stream.baba.R
import com.stream.baba.databinding.FragmentSettingBinding
import com.stream.baba.utils.Ui_helper.logError
import com.stream.baba.utils.Ui_helper.navigate

class SettingFragment : Fragment() {

    companion object {

        fun PreferenceFragmentCompat?.getPref(id: Int): Preference? {
            if (this == null) return null

            return try {
                findPreference(getString(id))
            } catch (e: Exception) {
                logError(e)
                null
            }
        }


    }

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fun navigate(id: Int) {
            activity?.navigate(id, Bundle())
        }

        binding.apply {

            listOf(
                Pair(
                    settingsGeneral,
                    R.id.action_navigation_settings_to_navigation_settings_general
                ),
                Pair(
                    settingsCredits,
                    R.id.action_navigation_settings_to_navigation_settings_account
                ),
                Pair(
                    settingsProviders,
                    R.id.action_navigation_settings_to_navigation_settings_providers
                ),
                Pair(
                    settingsUpdates,
                    R.id.action_navigation_settings_to_navigation_settings_updates
                )
            ).forEach { (view, navigationId) ->
                view.apply {
                    setOnClickListener {
                        navigate(navigationId)
                    }
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}