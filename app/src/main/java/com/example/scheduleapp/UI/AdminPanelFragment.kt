package com.example.scheduleapp.UI

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.scheduleapp.data.AuthenticationStatus
import com.example.scheduleapp.data.Constants.APP_MIN_PASSWORD_LENGTH
import com.example.scheduleapp.data.Constants.APP_PREFERENCES_STAY
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.data.DownloadStatus
import com.example.scheduleapp.databinding.FragmentAdminPanelBinding
import com.example.scheduleapp.utils.Utils.getBlankStringsChecker
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

class AdminPanelFragment: Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding: FragmentAdminPanelBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.changingBaseData.setOnClickListener {
            requireView().findNavController()
                .navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToDbFragmentContainer())
        }

        binding.changingBasicSchedule.setOnClickListener{
            requireView().findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToChangeBasicScheduleFragment())
        }

        binding.creatingSchedule.setOnClickListener{
            requireView().findNavController().navigate(AdminPanelFragmentDirections.actionAdminPanelFragmentToChooseDateFragment())
        }
    }
}