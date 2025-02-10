package com.example.adminscheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.adminscheduleapp.data.Constants.APP_PREFERENCES_PUSHES
import com.example.adminscheduleapp.data.Constants.APP_PREFERENCES_STAY
import com.example.adminscheduleapp.databinding.FragmentSettingsBinding
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.enablePushesCheckBox.isChecked = viewModel.getPreference(APP_PREFERENCES_PUSHES, true)
        binding.staySignedInCheckBox.isChecked = viewModel.getPreference(APP_PREFERENCES_STAY, false)
        binding.enablePushesCheckBox.setOnCheckedChangeListener(){v, checked ->
            viewModel.editPreferences(APP_PREFERENCES_PUSHES, checked)
        }
        binding.staySignedInCheckBox.setOnCheckedChangeListener(){v, checked ->
            viewModel.editPreferences(APP_PREFERENCES_STAY, checked)
        }

        binding.logoutTrigger.setOnClickListener {
            logOut()
        }
    }

    private fun logOut() {
        viewModel.signOut()
        viewModel.editPreferences(APP_PREFERENCES_STAY, false)
        //(activity as MainActivity).title = resources.getString(R.string.app_name)

        requireView().findNavController()
            .navigate(SettingsFragmentDirections.actionSettingsFragmentToLoginFragment())
    }
}