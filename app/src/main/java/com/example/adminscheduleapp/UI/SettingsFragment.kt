package com.example.adminscheduleapp.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_DAY_LIST_UPLOAD_FAILED
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_TOAST_DAY_LIST_UPLOAD_SUCCESS
import com.example.adminscheduleapp.data.Constants.APP_PREFERENCES_AUTOUPDATE
import com.example.adminscheduleapp.data.Constants.APP_PREFERENCES_STAY
import com.example.adminscheduleapp.data.UploadStatus
import com.example.adminscheduleapp.databinding.FragmentSettingsBinding
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var currentUploadStatus: MutableLiveData<UploadStatus>

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

        binding.enableAutoUpdateCheckBox.isChecked = viewModel.getPreference(APP_PREFERENCES_AUTOUPDATE, true)
        binding.staySignedInCheckBox.isChecked = viewModel.getPreference(APP_PREFERENCES_STAY, false)
        binding.enableAutoUpdateCheckBox.setOnCheckedChangeListener(){v, checked ->
            viewModel.editPreferences(APP_PREFERENCES_AUTOUPDATE, checked)
        }
        binding.staySignedInCheckBox.setOnCheckedChangeListener(){v, checked ->
            viewModel.editPreferences(APP_PREFERENCES_STAY, checked)
        }

        binding.manualUpdateTrigger.setOnClickListener {
            viewModel.updateAndUploadTheDayList(currentUploadStatus)
            it.isEnabled = false
        }
        binding.logoutTrigger.setOnClickListener {
            logOut()
        }

        initUploadObservers()
    }

    private fun logOut() {
        viewModel.signOut()
        viewModel.editPreferences(APP_PREFERENCES_STAY, false)
        //(activity as MainActivity).title = resources.getString(R.string.app_name)

        requireView().findNavController()
            .navigate(SettingsFragmentDirections.actionSettingsFragmentToLoginFragment())
    }

    private fun initUploadObservers() {
        currentUploadStatus = MutableLiveData()
        currentUploadStatus.observe(viewLifecycleOwner) { uploadStatus ->
            when (uploadStatus) {
                is UploadStatus.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UploadStatus.WeakProgress -> {
                    Toast.makeText(
                        activity,
                        uploadStatus.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                is UploadStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        "$APP_ADMIN_TOAST_DAY_LIST_UPLOAD_FAILED: ${uploadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.manualUpdateTrigger.isEnabled = true
                }
                is UploadStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        activity,
                        "$APP_ADMIN_TOAST_DAY_LIST_UPLOAD_SUCCESS.",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.manualUpdateTrigger.isEnabled = true
                }
            }
        }
    }
}